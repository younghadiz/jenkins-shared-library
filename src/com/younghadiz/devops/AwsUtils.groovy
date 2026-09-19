package com.younghadiz.devops

class AwsUtils implements Serializable {

    def script

    AwsUtils(script) {
        this.script = script
    }

    void pushToEcr(
        String imageName,
        String imageTag,
        String awsRegion,
        String ecrRegistryServer,
        String credentialsId
    ) {
        validateEcrConfig(
            imageName,
            imageTag,
            awsRegion,
            ecrRegistryServer
        )

        script.echo "Pushing Docker image to AWS ECR: ${imageName}:${imageTag}"

        script.withCredentials([
            script.usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: 'AWS_ACCESS_KEY_ID',
                passwordVariable: 'AWS_SECRET_ACCESS_KEY'
            )
        ]) {
            script.withEnv([
                "DOCKER_IMAGE_NAME=${imageName}",
                "DOCKER_IMAGE_TAG=${imageTag}",
                "AWS_REGION_VALUE=${awsRegion}",
                "ECR_REGISTRY_SERVER_VALUE=${ecrRegistryServer}"
            ]) {
                script.sh '''
                    set -e

                    command -v aws >/dev/null 2>&1 || {
                        echo "ERROR: AWS CLI is not installed on the Jenkins agent."
                        exit 1
                    }

                    command -v docker >/dev/null 2>&1 || {
                        echo "ERROR: Docker is not available on the Jenkins agent."
                        exit 1
                    }

                    echo "Logging in to AWS ECR..."

                    aws ecr get-login-password \
                        --region "$AWS_REGION_VALUE" |
                        docker login \
                            --username AWS \
                            --password-stdin "$ECR_REGISTRY_SERVER_VALUE"

                    echo "Pushing Docker image to AWS ECR..."

                    docker push \
                        "$DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG"
                '''
            }
        }
    }

    private void validateEcrConfig(
        String imageName,
        String imageTag,
        String awsRegion,
        String ecrRegistryServer
    ) {
        if (!imageName?.trim() ||
            imageName == 'null' ||
            imageName.contains('null')) {
            script.error(
                "ECR image name is empty or invalid: ${imageName}"
            )
        }

        if (!imageTag?.trim()) {
            script.error 'ECR image tag is required.'
        }

        if (!awsRegion?.trim()) {
            script.error 'AWS region is required for ECR.'
        }

        if (!ecrRegistryServer?.trim()) {
            script.error 'ECR registry server is required.'
        }
    }
}
