#!/usr/bin/env groovy

def call(String appDir = '.') {

    def version = ''

    dir(appDir) {
        echo 'Incrementing application version...'

        sh '''
            mvn build-helper:parse-version versions:set \
                '-DnewVersion=${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}' \
                versions:commit
        '''

        def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'

        if (!matcher.find()) {
            error 'Unable to read application version from pom.xml'
        }

        version = matcher.group(1)

        echo "Application version: ${version}"
    }

    return version
}
