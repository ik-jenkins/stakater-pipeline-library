#!/usr/bin/groovy
import io.fabric8.Fabric8Commands

def call(Map parameters = [:], body) {
    def flow = new Fabric8Commands()

    def defaultLabel = buildId('tools')
    def label = parameters.get('label', defaultLabel)

    def toolsImage = parameters.get('toolsImage', 'stakater/pipeline-tools:1.5.1')
    def inheritFrom = parameters.get('inheritFrom', 'base')

    def cloud = flow.getCloudConfig()

    echo 'using cloud: ' + cloud
    echo 'Using toolsImage : ' + toolsImage
    echo 'Mounting docker socket to build docker images'
    podTemplate(cloud: cloud, serviceAccount: 'jenkins',
            containers: [
                    containerTemplate(
                            name: 'tools',
                            image: "${toolsImage}",
                            command: '/bin/sh -c',
                            args: 'cat',
                            privileged: true,
                            workingDir: '/home/jenkins/',
                            ttyEnabled: true,
                            envVars: [
                                    envVar(key: 'DOCKER_API_VERSION', value: '1.32'),
                                    envVar(key: 'MAVEN_OPTS', value: '-Duser.home=/root/ -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn')
                                    ]
                    )],
            volumes: [
                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')]) {
        echo "inside pod template"
        body()
    }
}
