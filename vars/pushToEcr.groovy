#!/usr/bin/env groovy

import com.younghadiz.devops.AwsUtils

def call(
    String imageName,
    String imageTag,
    String awsRegion,
    String ecrRegistryServer,
    String credentialsId = 'aws_ecr_creds'
) {
    echo 'Pushing Docker image to AWS ECR...'

    new AwsUtils(this).pushToEcr(
        imageName,
        imageTag,
        awsRegion,
        ecrRegistryServer,
        credentialsId
    )
}
