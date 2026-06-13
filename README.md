# Jenkins Shared Library 🚀

This repository contains a reusable Jenkins Shared Library designed to standardize CI/CD pipelines across multiple projects.

It provides common pipeline functions for building Java applications, building Docker images, authenticating with container registries, and pushing images to either Docker Hub or AWS Elastic Container Registry (ECR).

---

## 📦 Repository Structure

```text
jenkins-shared-library/
├── vars/                         # Global pipeline steps (public entry points)
│   ├── buildJar.groovy
│   └── buildImage.groovy
│
├── src/                          # Reusable classes (internal implementation logic)
│   └── com/younghadiz/devops/
│       └── Docker.groovy
│
├── README.md
└── .gitignore
```

---

## ⚙️ Features

* Build Java applications using Maven
* Build Docker images
* Push images to Docker Hub
* Push images to AWS ECR
* Dynamic registry selection using `registryType`
* Reusable `buildJar()` pipeline step
* Reusable `buildImage()` pipeline step
* Supports Docker Hub credentials
* Supports AWS ECR credentials
* Supports project-level dynamic library loading
* Clean separation of pipeline entry points and internal implementation logic
* Designed for multi-project CI/CD reuse

---

## 🧠 Design Pattern

This shared library follows a clean separation of concerns:

```text
vars/ → Public API used directly inside Jenkinsfiles
src/  → Internal implementation using reusable Groovy classes
```

### Example

The Jenkinsfile calls:

```groovy
buildImage(...)
```

The `vars/buildImage.groovy` file receives the call and passes the parameters to:

```text
src/com/younghadiz/devops/Docker.groovy
```

This approach keeps Jenkinsfiles clean while allowing more advanced CI/CD logic to live in structured Groovy classes.

---

## 🚀 Usage

### Option 1 — Global Library

Register this repository in Jenkins:

```text
Manage Jenkins
↓
System
↓
Global Pipeline Libraries
```

Then use it in your Jenkinsfile:

```groovy
@Library('jenkins-shared-library') _
```

---

### Option 2 — Load Dynamically

This is the preferred approach when each project should control which shared library version or branch it uses.

```groovy
library(
  identifier: 'jenkins-shared-library@master',
  retriever: modernSCM([
      $class: 'GitSCMSource',
      remote: 'https://github.com/younghadiz/jenkins-shared-library.git',
      credentialsId: 'github-token'
  ])
)
```

This approach gives better project-level control and avoids depending only on Jenkins global configuration.

---

## 🛠 Available Shared Library Steps

### `buildJar`

Builds a Java Maven application.

```groovy
buildJar(APP_DIR)
```

Example:

```groovy
stage('Build Jar') {
    steps {
        buildJar(APP_DIR)
    }
}
```

The default command is:

```bash
mvn clean package
```

---

### `buildImage`

Builds and pushes a Docker image to either Docker Hub or AWS ECR.

```groovy
buildImage(
    appDir,
    imageName,
    imageTag,
    registryType,
    credentialsId,
    awsRegion,
    ecrRegistryServer
)
```

### Parameters

| Parameter           | Description                                       |
| ------------------- | ------------------------------------------------- |
| `appDir`            | Application directory where the Dockerfile exists |
| `imageName`         | Full image name or repository URL                 |
| `imageTag`          | Docker image tag                                  |
| `registryType`      | Registry type: `dockerhub` or `ecr`               |
| `credentialsId`     | Jenkins credential ID                             |
| `awsRegion`         | AWS region, required for ECR                      |
| `ecrRegistryServer` | ECR registry server, required for ECR             |

---

## 🐳 Docker Hub Example

### Jenkinsfile Environment

```groovy
environment {
    APP_DIR = 'app'

    REGISTRY_TYPE = 'dockerhub'

    DOCKERHUB_IMAGE_NAME = 'yourdockerhubuser/java-app'
    DOCKERHUB_CREDS      = 'dockerhub-creds'

    IMAGE_NAME = "${DOCKERHUB_IMAGE_NAME}"
    IMAGE_TAG  = "1.0.${env.BUILD_NUMBER}"
}
```

### Jenkinsfile Stage

```groovy
stage('Build & Push Image') {
    steps {
        buildImage(
            env.APP_DIR,
            env.IMAGE_NAME,
            env.IMAGE_TAG,
            env.REGISTRY_TYPE,
            env.DOCKERHUB_CREDS
        )
    }
}
```

This will run the Docker Hub flow:

```text
docker build
docker login to Docker Hub
docker push to Docker Hub
```

---

## ☁️ AWS ECR Example

### Jenkinsfile Environment

```groovy
environment {
    APP_DIR = 'app'

    REGISTRY_TYPE = 'ecr'

    AWS_REGION          = 'ca-central-1'
    ECR_REGISTRY_SERVER = '330673547330.dkr.ecr.ca-central-1.amazonaws.com'
    ECR_REPOSITORY      = 'java-maven-app'
    ECR_IMAGE_NAME      = "${ECR_REGISTRY_SERVER}/${ECR_REPOSITORY}"
    ECR_CREDS           = 'aws_ecr_creds'

    IMAGE_NAME = "${ECR_IMAGE_NAME}"
    IMAGE_TAG  = "1.0.${env.BUILD_NUMBER}"
}
```

### Jenkinsfile Stage

```groovy
stage('Build & Push Image') {
    steps {
        buildImage(
            env.APP_DIR,
            env.IMAGE_NAME,
            env.IMAGE_TAG,
            env.REGISTRY_TYPE,
            env.ECR_CREDS,
            env.AWS_REGION,
            env.ECR_REGISTRY_SERVER
        )
    }
}
```

This will run the ECR flow:

```text
docker build
aws ecr get-login-password
docker login to ECR
docker push to ECR
```

---

## 🧪 Full Example Jenkinsfile

```groovy
library(
  identifier: 'jenkins-shared-library@master',
  retriever: modernSCM([
      $class: 'GitSCMSource',
      remote: 'https://github.com/younghadiz/jenkins-shared-library.git',
      credentialsId: 'github-token'
  ])
)

pipeline {
    agent any

    tools {
        maven 'Maven3.9'
    }

    environment {
        APP_DIR = 'app'

        /*
          Registry options:
          dockerhub = push image to Docker Hub
          ecr       = push image to AWS ECR
        */
        REGISTRY_TYPE = 'ecr'

        /*
          Docker Hub configuration
        */
        DOCKERHUB_IMAGE_NAME = 'younghadiz/java-maven-app'
        DOCKERHUB_CREDS      = 'dockerhub-creds'

        /*
          AWS ECR configuration
        */
        AWS_REGION          = 'ca-central-1'
        ECR_REGISTRY_SERVER = '330673547330.dkr.ecr.ca-central-1.amazonaws.com'
        ECR_REPOSITORY      = 'java-maven-app'
        ECR_IMAGE_NAME      = "${ECR_REGISTRY_SERVER}/${ECR_REPOSITORY}"
        ECR_CREDS           = 'aws_ecr_creds'

        IMAGE_NAME = ''
        IMAGE_TAG  = "1.0.${env.BUILD_NUMBER}"
    }

    stages {
        stage('Select Image Repository') {
            steps {
                script {
                    if (env.REGISTRY_TYPE == 'ecr') {
                        env.IMAGE_NAME = env.ECR_IMAGE_NAME
                    } else if (env.REGISTRY_TYPE == 'dockerhub') {
                        env.IMAGE_NAME = env.DOCKERHUB_IMAGE_NAME
                    } else {
                        error "Unsupported REGISTRY_TYPE: ${env.REGISTRY_TYPE}. Use 'dockerhub' or 'ecr'."
                    }

                    echo "Registry type: ${env.REGISTRY_TYPE}"
                    echo "Image name: ${env.IMAGE_NAME}"
                    echo "Image tag: ${env.IMAGE_TAG}"
                }
            }
        }

        stage('Build Jar') {
            steps {
                buildJar(env.APP_DIR)
            }
        }

        stage('Build & Push Image') {
            steps {
                script {
                    def selectedCredentialsId = env.REGISTRY_TYPE == 'ecr'
                        ? env.ECR_CREDS
                        : env.DOCKERHUB_CREDS

                    buildImage(
                        env.APP_DIR,
                        env.IMAGE_NAME,
                        env.IMAGE_TAG,
                        env.REGISTRY_TYPE,
                        selectedCredentialsId,
                        env.AWS_REGION,
                        env.ECR_REGISTRY_SERVER
                    )
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished'
        }
        success {
            echo 'Pipeline succeeded'
        }
        failure {
            echo 'Pipeline failed'
        }
    }
}
```

---

## 🔐 Jenkins Credentials

Ensure Jenkins has the required credentials configured.

### GitHub Credential

Used to load the shared library dynamically.

```text
Kind: Username with password or Personal Access Token
ID: github-token
Purpose: Access GitHub repository
```

---

### Docker Hub Credential

Used when `REGISTRY_TYPE = 'dockerhub'`.

```text
Kind: Username with password
ID: dockerhub-creds
Username: Docker Hub username
Password: Docker Hub password or access token
```

---

### AWS ECR Credential

Used when `REGISTRY_TYPE = 'ecr'`.

```text
Kind: Username with password
ID: aws_ecr_creds
Username: AWS Access Key ID
Password: AWS Secret Access Key
```

The shared library maps this credential internally as:

```groovy
usernameVariable: 'AWS_ACCESS_KEY_ID'
passwordVariable: 'AWS_SECRET_ACCESS_KEY'
```

Then it uses:

```bash
aws ecr get-login-password
```

to authenticate Docker to ECR.

---

## 📌 Requirements

Jenkins agent must have:

* Jenkins Pipeline Plugin
* Jenkins Credentials Plugin
* Jenkins Git Plugin
* Docker installed
* Maven configured in Jenkins
* AWS CLI installed, required for ECR
* Network access to Docker Hub or AWS ECR

For AWS ECR usage, the AWS IAM user or role must have permissions such as:

```text
ecr:GetAuthorizationToken
ecr:BatchCheckLayerAvailability
ecr:CompleteLayerUpload
ecr:UploadLayerPart
ecr:InitiateLayerUpload
ecr:PutImage
ecr:BatchGetImage
```

---

## 🧩 Shared Library Design Decision

Initially, the Jenkins Shared Library was created as a nested folder inside the main project repository:

```text
devops-and-cloud-projects-lab/
└── 02_labs/phase-03/08_jenkins/jenkins-shared-library
```

While this structure worked for learning purposes, it introduced limitations:

* It required more dependency on Jenkins Global Pipeline Library configuration
* It made dynamic library loading with `library(...)` less flexible
* It reduced project-level control
* It was less scalable for multi-project environments
* It mixed reusable CI/CD logic with one learning repository

To improve this, the shared library was refactored into a separate repository:

```text
https://github.com/younghadiz/jenkins-shared-library
```

---

## 🚀 Benefits of the Separate Shared Library

* Enables dynamic loading of the library per project using `library(...)`
* Provides better project-level control
* Improves scalability for multiple services and repositories
* Aligns with real-world CI/CD architecture
* Simplifies reuse across different Jenkins pipelines
* Allows shared library updates without changing every project repository
* Separates reusable pipeline logic from application code

---

## 🧠 Learning Approach

The nested shared library is still retained in the original repository for:

* Reference and revision
* Tutorial continuity
* Understanding the evolution from simple CI/CD scripts to reusable shared libraries

This demonstrates a progression from:

```text
Learning-focused pipeline logic
        ↓
Reusable Jenkins shared library
        ↓
Production-style CI/CD architecture
```

---

## 🏗 Current Architecture

```text
Jenkinsfile
   ↓
vars/buildJar.groovy
   ↓
mvn clean package
```

```text
Jenkinsfile
   ↓
vars/buildImage.groovy
   ↓
src/com/younghadiz/devops/Docker.groovy
   ↓
Docker Hub or AWS ECR
```

The Jenkinsfile decides:

```text
REGISTRY_TYPE = dockerhub
```

or:

```text
REGISTRY_TYPE = ecr
```

The shared library then runs the correct build, login, and push logic.

---

## ✅ Current Supported Registry Types

| Registry Type             |             Status | Credential ID Example |
| ------------------------- | -----------------: | --------------------- |
| Docker Hub                |          Supported | `dockerhub-creds`     |
| AWS ECR                   |          Supported | `aws_ecr_creds`       |
| Nexus Repository          | Future improvement | `nexus-creds`         |
| GitHub Container Registry | Future improvement | `ghcr-creds`          |

---

## 🧩 Future Improvements

* Add Trivy image security scanning
* Add Kubernetes deployment helper
* Add Helm deployment helper
* Add rollback helper
* Add support for GitHub Container Registry, GHCR
* Add support for Nexus Docker registry
* Add support for semantic versioning
* Add unit testing for shared library logic
* Implement versioned shared library releases such as `v1.0.0`, `v1.1.0`
* Add branch-based registry selection
* Add multi-environment deployment support for dev, staging, and production

---

## 👨‍💻 Author

Gafari Salaudeen
GitHub: `younghadiz`
Gitlab: `younghadiz`

---

## 📜 License

This project is for learning, DevOps best practices, and portfolio demonstration purposes.
