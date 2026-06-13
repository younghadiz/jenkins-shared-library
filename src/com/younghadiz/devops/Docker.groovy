```groovy
package com.younghadiz.devops

class Docker implements Serializable {
    def script

    Docker(script) {
        this.script = script
    }

    void buildAndPush(
        String appDir,
        String imageName,
        String tag,
        String registryType = 'dockerhub',
        String credentialsId = '',
        String awsRegion = '',
        String ecrRegistryServer = ''
    ) {
        script.dir(appDir) {
            script.echo "Building the application for branch ${script.env.BRANCH_NAME}"
            script.echo "Registry type selected: ${registryType}"
            script.echo "Docker image tag: ${tag}"

            if (!imageName?.trim() || imageName == 'null' || imageName.contains('null')) {
                script.error "Docker imageName is empty or invalid. Received: ${imageName}"
            }

            if (!tag?.trim()) {
                script.error "Docker image tag is empty."
            }

            if (registryType == 'dockerhub') {
                buildAndPushToDockerHub(
                    imageName,
                    tag,
                    credentialsId ?: 'dockerhub-creds'
                )
            } else if (registryType == 'ecr') {
                buildAndPushToECR(
                    imageName,
                    tag,
                    credentialsId ?: 'aws_ecr_creds',
                    awsRegion,
                    ecrRegistryServer
                )
            } else {
                script.error "Unsupported registryType: ${registryType}. Use 'dockerhub' or 'ecr'."
            }
        }
    }

    private void buildAndPushToDockerHub(
        String imageName,
        String tag,
        String credentialsId
    ) {
        script.echo "Logging in to Docker Hub using credentials ID: ${credentialsId}"

        script.withCredentials([script.usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'USER',
            passwordVariable: 'PASS'
        )]) {
            script.withEnv([
                "IMAGE_NAME=${imageName}",
                "IMAGE_TAG=${tag}"
            ]) {
                script.sh '''
                    set -e

                    command -v docker >/dev/null 2>&1 || {
                        echo "ERROR: Docker is not available inside Jenkins container."
                        exit 1
                    }

                    echo "Building Docker image for Docker Hub..."
                    docker build -t "$IMAGE_NAME:$IMAGE_TAG" .

                    echo "Logging in to Docker Hub..."
                    echo "$PASS" | docker login -u "$USER" --password-stdin

                    echo "Pushing Docker image to Docker Hub..."
                    docker push "$IMAGE_NAME:$IMAGE_TAG"
                '''
            }
        }
    }

    private void buildAndPushToECR(
        String imageName,
        String tag,
        String credentialsId,
        String awsRegion,
        String ecrRegistryServer
    ) {
        if (!awsRegion?.trim()) {
            script.error "awsRegion is required when registryType is 'ecr'."
        }

        if (!ecrRegistryServer?.trim()) {
            script.error "ecrRegistryServer is required when registryType is 'ecr'."
        }

        script.echo "Logging in to AWS ECR using credentials ID: ${credentialsId}"
        script.echo "AWS region provided."
        script.echo "ECR registry server provided."

        script.withCredentials([script.usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'AWS_ACCESS_KEY_ID',
            passwordVariable: 'AWS_SECRET_ACCESS_KEY'
        )]) {
            script.withEnv([
                "IMAGE_NAME=${imageName}",
                "IMAGE_TAG=${tag}",
                "AWS_REGION_VALUE=${awsRegion}",
                "ECR_REGISTRY_SERVER_VALUE=${ecrRegistryServer}"
            ]) {
                script.sh '''
                    set -e

                    command -v aws >/dev/null 2>&1 || {
                        echo "ERROR: AWS CLI is not installed inside Jenkins container."
                        exit 1
                    }

                    command -v docker >/dev/null 2>&1 || {
                        echo "ERROR: Docker is not available inside Jenkins container."
                        exit 1
                    }

                    echo "Logging in to AWS ECR..."
                    aws ecr get-login-password --region "$AWS_REGION_VALUE" | docker login --username AWS --password-stdin "$ECR_REGISTRY_SERVER_VALUE"

                    echo "Building Docker image for AWS ECR..."
                    docker build -t "$IMAGE_NAME:$IMAGE_TAG" .

                    echo "Pushing Docker image to AWS ECR..."
                    docker push "$IMAGE_NAME:$IMAGE_TAG"
                '''
            }
        }
    }
}
```
