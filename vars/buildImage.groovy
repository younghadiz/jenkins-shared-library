#!/usr/bin/env groovy
import com.younghadiz.devops.Docker

/*
  Real implementation lives in:
  src/com/younghadiz/devops/Docker.groovy

  This file is the shared-library entry point used in Jenkinsfile as:

  buildImage(...)
*/

def call(
    String appDir,
    String imageName,
    String tag,
    String registryType = 'dockerhub',
    String credentialsId = '',
    String awsRegion = '',
    String ecrRegistryServer = ''
) {
    return new Docker(this).buildAndPush(
        appDir,
        imageName,
        tag,
        registryType,
        credentialsId,
        awsRegion,
        ecrRegistryServer
    )
}