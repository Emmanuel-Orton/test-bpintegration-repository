def DOCKER_CONF_FILE = "beyond_techstack_docker"
def DOCKER_CREDENTIALS = "cip_beyond_svc"
def AWS_JENKINS_CREDENTIALS = "aws_techstack_jenkins"
def HELM_FETCH_REPOSITORY = "https://artifactory-devops-platform.bearingpoint.com/artifactory/beyond-helm/"
def CLUSTER_NAME = "ts-op"
def AWS_REGION = "eu-west-1"

node('build4G-mesos') {
    checkout scm
    dir('project-config') {
        environments = readFile encoding: 'utf-8', file: 'ci/environments.txt'
    }
    if (params.EXECUTE_PIPELINE == null || params.EXECUTE_PIPELINE == false) {
        echo "Rebuilding job config"
        properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '0', artifactNumToKeepStr: '0', daysToKeepStr: '', numToKeepStr: '25'))
                    , parameters([
                        booleanParam(defaultValue: false, description: 'Run the deploy pipeline or refresh the job configuration only', name: 'EXECUTE_PIPELINE')
                      , choice(choices: environments, description: '', name: 'TARGET')
                      , booleanParam(defaultValue: false, description: 'Skip deployment of the application ', name: 'SKIP_DEPLOYMENT')
                      , booleanParam(defaultValue: false, description: 'Skip switching the application to mock', name: 'SKIP_MOCK_SETTING')
                    ])
        ])
        return
    }
    if ('<choose environment>' == params.TARGET) {
        currentBuild.description = "Target: Undefined"
        error('You need to choose an environment')
    }
    ansiColor('xterm') {
        try {
            dir('test-bpintegration-app') {
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: DOCKER_CREDENTIALS, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD'],
                                 [$class: 'UsernamePasswordMultiBinding', credentialsId: AWS_JENKINS_CREDENTIALS, usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY'],
                                 file(credentialsId: 'helm_keys', variable: 'HELM_KEYS_FILE')]) {
                    configFileProvider([configFile(fileId: DOCKER_CONF_FILE, targetLocation: '/home/jenkins/.docker/config.json')]) {
                        env.CLUSTER_NAME = CLUSTER_NAME
                        env.HELM_FETCH_REPOSITORY=HELM_FETCH_REPOSITORY
                        env.AWS_REGION = AWS_REGION
                        env.HELM_KEYS = readFile encoding: 'utf-8', file: HELM_KEYS_FILE
                        env.tenantShort= TARGET.toUpperCase()
                        env.target= TARGET

                        sh '''                        
                            AUTH=\$(echo -n "${USERNAME}:${PASSWORD}" | base64 -w 0) && sed -i -e 's|\\[AUTH\\]|'"\$AUTH"'|g' '/home/jenkins/.docker/config.json'
                            chmod go+r /home/jenkins/.docker/config.json
                        '''
                        stage('Deploying application') {
                            if (params.SKIP_DEPLOYMENT == null || params.SKIP_DEPLOYMENT == false) {
                                // here I trigger the master because this one always exists. In the next step switch to this version and change the mocked values
                                build job: "BEYOND/Techstack/ts-op-scenarios-deploy-integration/master", parameters: [
                                        [$class: 'BooleanParameterValue', name: 'EXECUTE_PIPELINE', value: true],
                                        [$class: 'StringParameterValue', name: 'TARGET', value: params.TARGET],
                                        [$class: 'StringParameterValue', name: 'DEPLOYMENT_NAME', value: 'test-bpintegration-app-mocked'],
                                        [$class: 'StringParameterValue', name: 'CHART_NAME_FILTER', value: 'test-bpintegration-app']
                                ]
                            }
                        }



                       stage('Change application to mock') {
                           if (params.SKIP_MOCK_SETTING == null || params.SKIP_MOCK_SETTING == false) {
                            environments = readFile encoding: 'utf-8', file: 'test-bpintegration-app/service-tests/ci/mocking-values/values-dev0x.yaml'
                            writeFile encoding:'utf-8', file: 'dev/values-dev0x.yaml', text: environments.replace('dev0x', TARGET).replace('DEV0X', env.TENANT)

                                sh '''                                   
                            COMMIT=$(git rev-parse --short HEAD)
                            chmod o+w service-tests/ci/telus-test-bpintegration-dev
                                                        
                                                        
                            docker-compose -f service-tests/docker-compose.yaml run helm helm dependency update "./test-bpintegration-app/service-tests/ci/telus-test-bpintegration-dev"                                                                                                               
                            docker-compose -f service-tests/docker-compose.yaml run helm helm upgrade test-bpintegration-app-mocked "./test-bpintegration-app/service-tests/ci/telus-test-bpintegration-dev" \
                                           -f ./test-bpintegration-app/service-tests/ci/mocking-values/values-dev0x.yaml --reuse-values --namespace infonova-$TARGET --wait --timeout 5m --description "application pointing to mock. Last commit: $COMMIT" 
                        '''
                            } // stage
                        }

                        stage('Run service tests') {
                            withMaven(
                                    jdk: 'OPENJDK11_0_2_PreInstalled (only for *-mesos Slavelabels)',
                                    maven: 'maven-3.6.3_PreInstalled (only for *-mesos Slavelabels)',
                                    mavenSettingsConfig: 'telus_maven_settings',
                                    mavenOpts: '-Xmx3120m -Xms1024m',
                                    options: [openTasksPublisher(disabled: true),
                                              junitPublisher(disabled: true, ignoreAttachments: true),
                                              artifactsPublisher(disabled: true)]) {

                                def status = 'FAILED '
                                try {
                                    sh "mvn verify -Dtest.type=service-tests -Denvironment=$params.TARGET"
                                    status = ''
                                } finally {

                                    publishHTML(target: [
                                            allowMissing         : false,
                                            alwaysLinkToLastBuild: false,
                                            keepAll              : true,
                                            reportDir            : 'service-tests/test-definition/target/cucumber-reports',
                                            reportFiles          : 'CucumberTestReport.html',
                                            reportName           : "${status} Service Tests ${env.BUILD_NUMBER}"
                                    ])
                                }

                            } // maven install
                        } // stage
                    } // configFileProvider
                } // withCredentials
            } // dir test-bpintegration-app
        } catch (e) {
            currentBuild.result = 'FAILURE'
            mail(bcc: '', body: "<b>Error</b><br><br>Project: ${env.JOB_NAME} <br>Build Number: ${env.BUILD_NUMBER} <br> URL: <a href='${env.BUILD_URL}'>${env.BUILD_URL}</a>"
                    , cc: ''
                    , charset: 'UTF-8'
                    , mimeType: 'text/html'
                    , replyTo: ''
                    , subject: "CI ERROR: Job name -> ${env.JOB_NAME}"
                    , to: 'backtothefeature.devteam@beyondnow.com')
            throw e
        }
    }
}
