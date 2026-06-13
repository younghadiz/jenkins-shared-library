#!/usr/bin/env groovy

def call(String appDir, String mavenCommand = 'mvn clean package') {
    dir(appDir) {
        echo "Building Java Maven application in ${appDir}"
        echo "Running Maven command: ${mavenCommand}"

        sh "${mavenCommand}"
    }
}