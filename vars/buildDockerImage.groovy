#!/usr/bin/env groovy

import com.younghadiz.devops.DockerUtils

def call(
    String appDir,
    String imageName,
    String imageTag
) {
    echo 'Building Docker image...'

    new DockerUtils(this).buildImage(
        appDir,
        imageName,
        imageTag
    )
}
