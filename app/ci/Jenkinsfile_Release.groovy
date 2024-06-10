#!/usr/bin/env groovy

def DOCKER_CONF_FILE = "telus_docker"
def IMAGE_REPOSITORY = "northamerica-northeast1-docker.pkg.dev/cto-bpintegration-np-4ea75a/applications/sales-bp-integration"
def DOCKER_CREDENTIALS = "cip_telus_svc"
def HELM_REPOSITORY = "https://artifactory-devops-platform.bearingpoint.com/artifactory/telus-helm-local/"
def GIT_EMAIL = "cip.telus.svc@beyondnow.com" // required to "git push". This value is shown in git history and is NOT used for authentication
def GIT_USER = "beyond" // required for "git push" command. This value is shown in git history and is NOT used for authentication
def NETRC_FILE = "telus_netrc"
def SSH_CONFIG = "telus_ssh_config"
def CHART_NAME = "test-bpintegration-app"
def IMAGE_NAME = "test-bpintegration-app"
def HELM_VERSION = "v3.8.0"
def YQ_VERSION = "v4.19.1"
def GITHUB_REPO_URL = "git@github.com:telus/sales-bp-integration-test-bpintegration.git"

// safeguard, no releases from Pull Request branches
if (!env.BRANCH_NAME.startsWith("main") && !env.BRANCH_NAME.startsWith("hotfix/") && !env.BRANCH_NAME.startsWith("release/")) {
    error("Releasing only allowed from master, hotfix or release branches")
    return
}

if (params.EXECUTE_PIPELINE == null || params.EXECUTE_PIPELINE == false) {
    echo "Rebuilding job config"
    def onHotfixBranch = env.BRANCH_NAME.startsWith("hotfix/")
    properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '0', artifactNumToKeepStr: '0', daysToKeepStr: '', numToKeepStr: '25')), parameters([booleanParam(defaultValue: false, description: 'If true, run the deploy pipeline, if false refresh the job configuration', name: 'EXECUTE_PIPELINE'),
                                                                                                                                                      string(defaultValue: '', description: 'If provided the JIRA_ID is added to the commit messages', name: 'JIRA_ID'),
                                                                                                                                                      string(defaultValue: '', description: 'The release version to build. Overrules all INCREASE_* params if set', name: 'RELEASE_VERSION'),
                                                                                                                                                      string(defaultValue: '', description: 'The new development Version to be used. Overrules all INCREASE_* params if set', name: 'DEVELOPMENT_VERSION'),
                                                                                                                                                      booleanParam(defaultValue: true, description: 'Create a git tag', name: 'CREATE_TAG'),
                                                                                                                                                      booleanParam(defaultValue: true, description: 'no changes are performed in git or artifactory', name: 'IS_DRY_RUN')])])
    return
}

node("build4G-mesos") {
    ansiColor('xterm') {
        configFileProvider([configFile(fileId: NETRC_FILE, targetLocation: '/home/jenkins/.netrc'),configFile(fileId: SSH_CONFIG, targetLocation: '/home/jenkins/.ssh/config')]) {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: DOCKER_CREDENTIALS, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                withCredentials([[$class: 'SSHUserPrivateKeyBinding', credentialsId: "github", keyFileVariable: 'SSH_PRIVATE_KEY', passphraseVariable: '', usernameVariable: 'SSH_USERNAME']]) {
                    sh 'ssh-agent /bin/bash'
                    env.USERNAME = USERNAME
                    env.PASSWORD = PASSWORD
                    env.GIT_USER = GIT_USER
                    env.GIT_EMAIL = GIT_EMAIL
                    env.JIRA_ID = JIRA_ID
                    env.HELM_VERSION = HELM_VERSION
                    env.YQ_VERSION = YQ_VERSION
                    env.GITHUB_REPO_URL = GITHUB_REPO_URL
                    dir('releasebuild') {
                        deleteDir()
                    }
                    dir('releasebuild') {
                        withMaven(jdk: 'OPENJDK11_0_2_PreInstalled (only for *-mesos Slavelabels)',
                                maven: 'maven-3.6.3_PreInstalled (only for *-mesos Slavelabels)',
                                mavenSettingsConfig: 'telus_maven_settings',
                                mavenOpts: '-Xmx3120m -Xms1024m',
                                options: [openTasksPublisher(disabled: true),
                                          junitPublisher(disabled: true, ignoreAttachments: false),
                                          artifactsPublisher(disabled: true)]) {
                            stage("Checkout") {
                                checkout scm
                            }
                            dir('app') {
                                stage("Prepare tools") {
                                    sh " sed -i -e 's|\\[USERNAME\\]|${USERNAME}|g' '/home/jenkins/.netrc'"
                                    sh " sed -i -e 's|\\[PASSWORD\\]|${PASSWORD}|g' '/home/jenkins/.netrc'"
                                    sh '''
                                git config --global user.email "${GIT_EMAIL}"
                                git config --global user.name "${GIT_USER}"
                                git checkout ${BRANCH_NAME}

                                mkdir $HOME/bin
                                wget -qO - "https://get.helm.sh/helm-${HELM_VERSION}-linux-amd64.tar.gz" | tar xz linux-amd64/helm
                                mv linux-amd64/helm $HOME/bin/
                                wget https://github.com/mikefarah/yq/releases/download/${YQ_VERSION}/yq_linux_amd64.tar.gz -O - | tar xz
                                mv yq_linux_amd64 $HOME/bin/yq
                                '''
                                }
                                if (!params.JIRA_ID.isEmpty()) {
                                    JIRA_ID = "[" + params.JIRA_ID + "]"
                                }

                                stage("Set Release Version") {
                                    updateChartVersion(CHART_NAME, RELEASE_VERSION)
                                    updateServiceTestsChartVersion(CHART_NAME, RELEASE_VERSION)
                                    updateCloudBuildVersion(RELEASE_VERSION)

                                    sh '''
                            git add ..
                            git commit --allow-empty -m "${JIRA_ID}[SCM] Set Release Version ${RELEASE_VERSION}"
                            '''
                                    if (params.CREATE_TAG == true) {
                                        sh "git tag buildv${RELEASE_VERSION}"
                                    }
                                }

                                stage("Release Build") {
                                    configFileProvider([configFile(fileId: DOCKER_CONF_FILE, targetLocation: '/home/jenkins/.docker/config.json')]) {
                                        sh ''' AUTH=\$(echo -n "${USERNAME}:${PASSWORD}" | base64) && sed -i -e 's|\\[AUTH\\]|'"\$AUTH"'|g' '/home/jenkins/.docker/config.json' '''
                                        env.IMAGE_REPOSITORY = IMAGE_REPOSITORY
                                        env.IMAGE_TAG = RELEASE_VERSION
                                        env.CHART_NAME = CHART_NAME
                                        env.HELM_REPOSITORY = HELM_REPOSITORY
                                        env.IMAGE_NAME = IMAGE_NAME
                                        if (params.IS_DRY_RUN == true) {
                                            echo "IS_DRY_RUN is true. Dry run build only"
                                            sh '''
                                    mvn clean package
                                    docker-compose build
                                    pushd ci
                                    export PATH=$HOME/bin:$PATH
                                    helm package ${CHART_NAME}
                                    '''
                                        } else {
                                            sh '''
                                    export PATH=$HOME/bin:$PATH
                                    mvn clean install -Pdocker -Dimage=${IMAGE_REPOSITORY}/${IMAGE_NAME}:${IMAGE_TAG}
                                    docker-compose build
                                    docker-compose push
                                    
                                    pushd ci
                                    helm package ${CHART_NAME}
                                    curl -f -u $USERNAME:$PASSWORD -T "${CHART_NAME}-${RELEASE_VERSION}.tgz" "${HELM_REPOSITORY}"
                                    '''
                                        }
                                    }
                                }
                                stage("Set Development Version") {
                                    if (!params.DEVELOPMENT_VERSION.endsWith("-latest")) {
                                        DEVELOPMENT_VERSION = params.DEVELOPMENT_VERSION + "-latest"
                                    }
                                    updateChartVersion(CHART_NAME, DEVELOPMENT_VERSION)
                                    updateServiceTestsChartVersion(CHART_NAME, DEVELOPMENT_VERSION)
                                    updateCloudBuildVersion(DEVELOPMENT_VERSION)
                                    env.DEVELOPMENT_VERSION = DEVELOPMENT_VERSION
                                    sh '''
                            git add ..
                            git commit --allow-empty -m "${JIRA_ID}[SCM] Set Development Version ${DEVELOPMENT_VERSION}"
                            '''
                                }
                                stage("Push Release") {
                                    if (params.IS_DRY_RUN == true) {
                                        echo "IS_DRY_RUN is true. Skipping Step...."
                                    } else {
                                        sh '''
                                git status
                                git log --pretty=oneline | tail -n 20
                                git push origin ${BRANCH_NAME}
                                git remote add github ${GITHUB_REPO_URL}
                                eval \$(ssh-agent -s) && ssh-add ${SSH_PRIVATE_KEY} && ssh-add -l  
                                git push github ${BRANCH_NAME}
                                '''
                                        if (params.CREATE_TAG == true) {
                                            sh "git push origin buildv${RELEASE_VERSION}"
                                            sh "eval \$(ssh-agent -s) && ssh-add ${SSH_PRIVATE_KEY} && ssh-add -l && git push github buildv${RELEASE_VERSION}"
                                        }
                                    }
                                }
                            }
                        }//env
                    } // custom directory
                } // withCredentials
            }
        } // configFileProvider NETRC
    } // ansiColor
}

def updateChartVersion(String chartName, String chartVersion) {
    def chartFile = 'ci/' + chartName + '/Chart.yaml'
    def chart = readYaml file: chartFile
    chart.version = chartVersion.replace("-SNAPSHOT", "-latest")
    sh "rm " + chartFile
    writeYaml file: chartFile, data: chart
}

def updateServiceTestsChartVersion(String chartName, String chartVersion) {
    def chartFileServiceTests = 'service-tests/ci/' + chartName + '-dev/Chart.yaml'
    def chart = readYaml file: chartFileServiceTests
    chart.version = chartVersion.replace("-SNAPSHOT", "-latest")
    sh "rm " + chartFileServiceTests
    writeYaml file: chartFileServiceTests, data: chart
    sh """
       yq -i '.dependencies[].version="${chartVersion}"' ${chartFileServiceTests}
    """
}

def updateCloudBuildVersion(String version) {
    def cloudBuildFile = '../cloud-build/cloudbuild.yaml'
    def cloudBuildFileContent = readYaml file: cloudBuildFile
    cloudBuildFileContent.substitutions._IMAGE_VERSION = version.replace("-SNAPSHOT", "-latest")
    sh "rm " + cloudBuildFile
    writeYaml file: cloudBuildFile, data: cloudBuildFileContent
}