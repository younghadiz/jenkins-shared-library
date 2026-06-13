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
            script.sh """
                docker build -t ${imageName}:${tag} .
                echo "\$PASS" | docker login -u "\$USER" --password-stdin
                docker push ${imageName}:${tag}
            """
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
            script.error "awsRegion is required when registryType is 'ecr'. Pass it from Jenkinsfile environment, for example env.AWS_REGION."
        }

        if (!ecrRegistryServer?.trim()) {
            script.error "ecrRegistryServer is required when registryType is 'ecr'. Pass it from Jenkinsfile environment."
        }

        script.echo "Logging in to AWS ECR using credentials ID: ${credentialsId}"
        script.echo "AWS region provided from Jenkins credentials."
        script.echo "ECR registry server provided from Jenkins credentials."

        script.withCredentials([script.usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'AWS_ACCESS_KEY_ID',
            passwordVariable: 'AWS_SECRET_ACCESS_KEY'
        )]) {
            script.withEnv([
                "AWS_DEFAULT_REGION=${awsRegion}",
                "AWS_REGION=${awsRegion}"
            ]) {
                script.sh """
                    aws ecr get-login-password --region ${awsRegion} | docker login --username AWS --password-stdin ${ecrRegistryServer}

                    docker build -t ${imageName}:${tag} .
                    docker push ${imageName}:${tag}
                """
            }
        }
    }
}