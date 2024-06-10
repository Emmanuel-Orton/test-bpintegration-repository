def DOCKER_CONF_FILE = "telus_docker"
def IMAGE_REPOSITORY = "northamerica-northeast1-docker.pkg.dev/cto-bpintegration-np-4ea75a/applications/sales-bp-integration"
def DOCKER_CREDENTIALS = "cip_telus_svc"
def HELM_REPOSITORY = "https://artifactory-devops-platform.bearingpoint.com/artifactory/telus-helm-local/"
def CHART_NAME = "test-bpintegration-app"
def HELM_VERSION = "v3.8.0"

node('build4G-mesos') {
    ansiColor('xterm') {
        properties([
                buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10')),
                disableConcurrentBuilds(),
                pipelineTriggers([bitbucketPush(), pollSCM('*/1 * * * * ')])
        ])
        checkout scm
        try {
            stage('[Build] Install') {
                dir('app') {
                    def version = calculateVersion(CHART_NAME)
                    env.IMAGE_REPOSITORY = IMAGE_REPOSITORY
                    env.IMAGE_TAG = version
                    env.HELM_REPOSITORY = HELM_REPOSITORY
                    env.CHART_NAME = CHART_NAME
                    env.HELM_VERSION = HELM_VERSION
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: DOCKER_CREDENTIALS, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                        withMaven(
                                jdk: 'OPENJDK11_0_2_PreInstalled (only for *-mesos Slavelabels)',
                                maven: 'maven-3.6.3_PreInstalled (only for *-mesos Slavelabels)',
                                mavenSettingsConfig: 'telus_maven_settings',
                                mavenOpts: '-Xmx3120m -Xms1024m',
                                options: [openTasksPublisher(disabled: true),
                                          junitPublisher(disabled: true, ignoreAttachments: false),
                                          artifactsPublisher(disabled: true)]) {
                            configFileProvider([configFile(fileId: DOCKER_CONF_FILE, targetLocation: '/home/jenkins/.docker/config.json')]) {
                                silentsh ''' AUTH=\$(echo -n "${USERNAME}:${PASSWORD}" | base64 -w 0) && sed -i -e 's|\\[AUTH\\]|'"\$AUTH"'|g' '/home/jenkins/.docker/config.json' '''
                                sh '''
                                mkdir $HOME/bin
                                wget -qO - "https://get.helm.sh/helm-${HELM_VERSION}-linux-amd64.tar.gz" | tar xz linux-amd64/helm
                                mv linux-amd64/helm $HOME/bin/
                                export PATH=$HOME/bin:$PATH
                                helm version
                                
                                mvn clean install -Pdocker -Dimage=${IMAGE_REPOSITORY}/${CHART_NAME}:${IMAGE_TAG}
                                docker-compose build
                                docker-compose push
                                
                                COMMIT=$(git rev-parse --short HEAD)
                                helm package --version $IMAGE_TAG --app-version "\\"$COMMIT\\"" ci/${CHART_NAME}
                                curl -f -u $USERNAME:$PASSWORD -T "${CHART_NAME}-${IMAGE_TAG}.tgz" "${HELM_REPOSITORY}"
'''
                            }
                        }
                    }
                }
            }
        } catch (e) {
            currentBuild.result = 'FAILURE'
            GIT_COMMIT_EMAIL = sh(
                    script: 'git --no-pager show -s --format=\'%ae\'',
                    returnStdout: true
            ).trim()
            configFileProvider(
                    [configFile(fileId: 'SWA_EMAIL', variable: 'SWA_EMAIL_FILE')]) {

                mail(bcc: '', body: "<b>Error</b><br><br>Project: ${env.JOB_NAME} <br>Build Number: ${env.BUILD_NUMBER} <br> URL: <a href='${env.BUILD_URL}'>${env.BUILD_URL}</a>"
                        , cc: readFile(file: SWA_EMAIL_FILE)
                        , charset: 'UTF-8'
                        , mimeType: 'text/html'
                        , replyTo: ''
                        , subject: "CI ERROR: Job name -> ${env.JOB_NAME}"
                        , to: "${GIT_COMMIT_EMAIL}")
            }
            throw e
        }
    }
}

def calculateVersion(String chartName) {
    return currentVersion("ci/" + chartName + "/Chart.yaml") + "-" + calculateBranchVersion()
}

def currentVersion(String helmChartFilePath) {
    def chart = readYaml file: helmChartFilePath
    return chart.version.replace("-latest", "")
}

def silentsh(cmd) {
    steps.sh('#!/bin/sh -e\n set +x \n' + cmd + " >/dev/null 2>&1")
}

def calculateBranchVersion() {
    def version = 'unknown'
    if (env.BRANCH_NAME ==~ /^PR-.*/ || env.BRANCH_NAME ==~ /^(feature)\/.*/ || env.BRANCH_NAME ==~ /^(hotfix)\/.*/ || env.BRANCH_NAME ==~ /^(bugfix)\/.*/) {
        version = env.BRANCH_NAME.toUpperCase().replace("/", "-").replace("_", "-") + "-latest"
    } else if (env.BRANCH_NAME == "master" || env.BRANCH_NAME == "main") {
        version = "latest"
    } else {
        error "Branch " + env.BRANCH_NAME + "can not be built!"
    }
    echo "Building version: " + version
    return version
}
