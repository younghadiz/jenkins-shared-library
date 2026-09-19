#!/usr/bin/env groovy

import com.younghadiz.devops.KubernetesUtils

def call(
    String appDir,
    String manifestDir,
    String appName,
    String imageName,
    String imageTag,
    String namespace = 'default'
) {
    echo 'Deploying application to Amazon EKS...'

    new KubernetesUtils(this).deployToEks(
        appDir,
        manifestDir,
        appName,
        imageName,
        imageTag,
        namespace
    )
}
