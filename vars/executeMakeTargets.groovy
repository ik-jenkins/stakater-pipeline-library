#!/usr/bin/groovy
//execute make target

def call(body) {
    Map config = [:]
    String[] methodParameters = ["target"]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def app = new io.stakater.app.App()
    config = app.configure(config)
    timestamps {
        toolsNode(toolsImage: 'stakater/builder-tool:terraform-0.11.11-v0.0.13') {
            withSCM { String repoUrl, String repoName, String repoOwner, String repoBranch ->
                checkout scm

                def appConfig = new io.stakater.app.AppConfig()
                Map packageConfig = appConfig.getPackageConfig(config)
                Map deploymentConfig = appConfig.getDeploymentConfig(config)
                Map gitConfig = appConfig.getGitConfig(config)
                Map notificationConfig = appConfig.getNotificationConfig(config)
                Map baseConfig = appConfig.getBaseConfig(config, repoName, repoOwner, WORKSPACE)
                Map ecrConfig = appConfig.getEcrConfig(config)

                def docker = new io.stakater.containers.Docker()
                def stakaterCommands = new io.stakater.StakaterCommands()
                def git = new io.stakater.vc.Git()
                def utils = new io.fabric8.Utils()
                def chartManager = new io.stakater.charts.ChartManager()
                def notificationManager = new io.stakater.notifications.NotificationManager()
                def nexus = new io.stakater.repository.Nexus()
                def aws = new io.stakater.cloud.Amazon()


                container(name: 'tools') {
                    try {
                        // echo "Image NAME: ${baseConfig.imageName}"
                        // echo "Repo Owner: ${baseConfig.repoOwner}"

                        stage('Create Version') {
                            // dockerImage = "${packageConfig.dockerRepositoryURL}/${baseConfig.repoOwner.toLowerCase()}/${baseConfig.imageName}"
                            // version = app.getImageVersion(repoUrl, baseConfig.imagePrefix, repoBranch, "${env.BUILD_NUMBER}")
                            // echo "Version: ${version}"
                            sh "make ${config.target} ${parameters.join(" ")}"
                        }
                    }
                    catch (e) {
                        print "caught exception during build phase"
                        buildException = e
                    }
                }
    }
    



    // config.target = config.target ? config.target : "install-dry-run"

    // timestamps {
    //     ArrayList<String> parameters = new ArrayList<String>()
    //     config.keySet().each { key ->
    //         if (! (key in methodParameters)) {
    //             parameters.add("$key=${config[key]}")
    //         }
    //     }
    //     sh "make ${config.target} ${parameters.join(" ")}"
    // }
}}}