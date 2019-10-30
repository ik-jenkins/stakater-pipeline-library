#!/usr/bin/groovy
//execute make target

def call(body) {
    Map config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    String[] methodParameters = config.requiredParams
    
    def app = new io.stakater.app.App()
    config = app.configure(config)
    timestamps {
        toolsNode(toolsImage: config.image) {
            withSCM { String repoUrl, String repoName, String repoOwner, String repoBranch ->
                checkout scm
                
                def appConfig = new io.stakater.app.AppConfig()
                Map notificationConfig = appConfig.getNotificationConfig(config)
                Map gitConfig = appConfig.getGitConfig(config)
                def notificationManager = new io.stakater.notifications.NotificationManager()
                
                container(name: 'tools') {
                    try {
                        stage('run') {
                            ArrayList<String> parameters = new ArrayList<String>()
                                config.keySet().each { key ->
                                    if ((key in methodParameters)) {
                                        parameters.add("$key=${config[key]}")
                                    }
                            }
                            sh "make ${config.target} ${parameters.join(" ")}"
                            withAWS(credentials:'aws-credentials', region: 'eu-west-1') {
                                s3Upload(file:'tests_results.tar.gz', bucket:'cypress-test-bucket', path:'home/jenkins/workspace/tests_results.tar.gz')
                            }
                        }
                    }
                    catch (e) {
                        print "caught exception during build phase"
                        print e
                        notificationManager.sendError(notificationConfig, gitConfig, "${env.BUILD_NUMBER}", "${env.BUILD_URL}", repoBranch, e)
                    }
                }
            }
        }
    }
}