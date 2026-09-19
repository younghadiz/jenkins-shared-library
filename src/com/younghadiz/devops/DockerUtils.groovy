package com.younghadiz.devops

class DockerUtils implements Serializable {

    def script

    DockerUtils(script) {
        this.script = script
    }

    void buildImage(
        String appDir,
        String imageName,
        String imageTag
    ) {
        validateImage(imageName, imageTag)

        script.dir(appDir) {
            script.echo "Building Docker image: ${imageName}:${imageTag}"

            script.withEnv([
                "DOCKER_IMAGE_NAME=${imageName}",
                "DOCKER_IMAGE_TAG=${imageTag}"
            ]) {
                script.sh '''
                    set -e

                    command -v docker >/dev/null 2>&1 || {
                        echo "ERROR: Docker is not available on the Jenkins agent."
                        exit 1
                    }

                    docker build \
                        -t "$DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG" \
                        .
                '''
            }
        }
    }

    void pushToDockerHub(
        String imageName,
        String imageTag,
        String credentialsId
    ) {
        validateImage(imageName, imageTag)

        script.echo "Pushing Docker image to Docker Hub: ${imageName}:${imageTag}"

        script.withCredentials([
            script.usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: 'DOCKERHUB_USER',
                passwordVariable: 'DOCKERHUB_PASS'
            )
        ]) {
            script.withEnv([
                "DOCKER_IMAGE_NAME=${imageName}",
                "DOCKER_IMAGE_TAG=${imageTag}"
            ]) {
                script.sh '''
                    set -e

                    echo "$DOCKERHUB_PASS" |
                        docker login \
                            --username "$DOCKERHUB_USER" \
                            --password-stdin

                    docker push \
                        "$DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG"
                '''
            }
        }
    }

    private void validateImage(
        String imageName,
        String imageTag
    ) {
        if (!imageName?.trim() ||
            imageName == 'null' ||
            imageName.contains('null')) {
            script.error(
                "Docker image name is empty or invalid: ${imageName}"
            )
        }

        if (!imageTag?.trim()) {
            script.error 'Docker image tag is empty.'
        }
    }
}
