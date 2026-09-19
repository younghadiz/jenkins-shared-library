#!/usr/bin/env groovy

import com.younghadiz.devops.DockerUtils

def call(
    String imageName,
    String imageTag,
    String credentialsId = 'dockerhub-creds'
) {
    echo 'Pushing Docker image to Docker Hub...'

    new DockerUtils(this).pushToDockerHub(
        imageName,
        imageTag,
        credentialsId
    )
}
