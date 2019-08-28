#!/usr/bin/groovy
package io.stakater


def shOutput(String command) {
    return sh(
        script: """
            ${command}
        """,
        returnStdout: true).toString().trim()
}

def getEnvValue(String key) {
    sh "echo \$${key} > ${key}"
    value = readFile(key).trim()
    sh "rm ${key}"
    return value
}

String replaceCredentialsInHttpURL(String URL, String username, String password) {
    String protocol = ""
    if (URL.startsWith("http://")) {
        protocol = "http://"
    } else {
        protocol = "https://"
    }

    String strippedURL = ""
    Integer tokenIndex = URL.indexOf('@')
    if (tokenIndex != -1) {
        strippedURL = URL.substring(tokenIndex + 1, URL.size())
    } else {
        tokenIndex = URL.indexOf(protocol)
        if (tokenIndex != -1) {
            strippedURL = URL.substring(tokenIndex + protocol.size(), URL.size())
        }
    }

    //Convert bitbucket.org:owner/repo to bitbucket.org/owner/repo
    strippedURL = strippedURL.replaceAll(':', '/')
    echo "before exiting HTTPUrl"
    return protocol + username + ":" + password + "@" + strippedURL
}

def isDockerSocketExists() {
    def dockerSocket = env['DOCKER_SOCKET']
    echo "Value for DOCKER_SOCKET environment variable: ${dockerSocket}"
    if (env['DOCKER_SOCKET']) { 
      return true
    }
    else {
      return false
    }
}

return this
