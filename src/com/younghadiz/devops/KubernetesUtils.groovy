package com.younghadiz.devops

class KubernetesUtils implements Serializable {

    def script

    KubernetesUtils(script) {
        this.script = script
    }

    void deployToEks(
        String appDir,
        String manifestDir,
        String appName,
        String imageName,
        String imageTag,
        String namespace = 'default'
    ) {
        validateDeploymentConfig(
            manifestDir,
            appName,
            imageName,
            imageTag,
            namespace
        )

        script.dir(appDir) {
            script.echo "Deploying ${appName} to Kubernetes..."
            script.echo "Namespace: ${namespace}"
            script.echo "Image: ${imageName}:${imageTag}"

            script.withEnv([
                "APP_NAME=${appName}",
                "IMAGE_NAME=${imageName}",
                "IMAGE_TAG=${imageTag}",
                "K8S_NAMESPACE=${namespace}"
            ]) {
                script.sh """
                    set -e

                    command -v kubectl >/dev/null 2>&1 || {
                        echo "ERROR: kubectl is not installed on the Jenkins agent."
                        exit 1
                    }

                    command -v envsubst >/dev/null 2>&1 || {
                        echo "ERROR: envsubst is not installed on the Jenkins agent."
                        exit 1
                    }

                    test -f "${manifestDir}/deployment.yaml" || {
                        echo "ERROR: ${manifestDir}/deployment.yaml was not found."
                        exit 1
                    }

                    test -f "${manifestDir}/service.yaml" || {
                        echo "ERROR: ${manifestDir}/service.yaml was not found."
                        exit 1
                    }

                    echo "Applying Kubernetes deployment..."

                    envsubst < "${manifestDir}/deployment.yaml" |
                        kubectl apply \
                            --namespace "\$K8S_NAMESPACE" \
                            -f -

                    echo "Applying Kubernetes service..."

                    envsubst < "${manifestDir}/service.yaml" |
                        kubectl apply \
                            --namespace "\$K8S_NAMESPACE" \
                            -f -

                    echo "Waiting for deployment rollout..."

                    kubectl rollout status \
                        deployment/"\$APP_NAME" \
                        --namespace "\$K8S_NAMESPACE" \
                        --timeout=180s
                """
            }
        }
    }

    private void validateDeploymentConfig(
        String manifestDir,
        String appName,
        String imageName,
        String imageTag,
        String namespace
    ) {
        if (!manifestDir?.trim()) {
            script.error 'Kubernetes manifest directory is required.'
        }

        if (!appName?.trim()) {
            script.error 'Application name is required.'
        }

        if (!imageName?.trim()) {
            script.error 'Docker image name is required.'
        }

        if (!imageTag?.trim()) {
            script.error 'Docker image tag is required.'
        }

        if (!namespace?.trim()) {
            script.error 'Kubernetes namespace is required.'
        }
    }
}
