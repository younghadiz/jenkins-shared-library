# Jenkins Shared Library 🚀

This repository contains a reusable Jenkins Shared Library designed to standardize CI/CD pipelines across multiple projects.

It provides common pipeline functions such as building Java applications, building Docker images, and pushing images to a container registry.

---

## 📦 Repository Structure

jenkins-shared-library/
├── vars/                # Global pipeline steps (entry points)
│   ├── buildJar.groovy
│   ├── buildImage.groovy
│
├── src/                 # Reusable classes (internal logic)
│   └── com/younghadiz/devops/
│       └── Docker.groovy
│
├── README.md
├── .gitignore

---

## ⚙️ Features

- Build Java applications using Maven
- Build Docker images
- Authenticate with Docker registry
- Push images to Docker Hub
- Parameterized and reusable pipeline steps
- Clean separation of logic (vars/ vs src/)

---

## 🧠 Design Pattern

- vars/ → Public API (simple pipeline steps)
- src/ → Internal implementation (classes, logic)

This follows Jenkins Shared Library best practices.

---

## 🚀 Usage

### Option 1 — Global Library (Recommended)

Register this repository in Jenkins:

Manage Jenkins → System → Global Pipeline Libraries

Then use in your Jenkinsfile:

@Library('jenkins-shared-library') _

---

### Option 2 — Load Dynamically

library(
    identifier: 'jenkins-shared-library@main',
    retriever: modernSCM([
        $class: 'GitSCMSource',
        remote: 'https://github.com/younghadiz/jenkins-shared-library.git',
        credentialsId: 'github-token'
    ])
)

---

## 🛠 Example Jenkinsfile

@Library('jenkins-shared-library') _

pipeline {
    agent any

    environment {
        APP_DIR    = 'app'
        IMAGE_NAME = 'yourdockerhubuser/java-app'
        IMAGE_TAG  = "1.0.${env.BUILD_NUMBER}"
    }

    stages {
        stage('Build Jar') {
            steps {
                buildJar(APP_DIR)
            }
        }

        stage('Build & Push Image') {
            steps {
                buildImage(APP_DIR, IMAGE_NAME, IMAGE_TAG)
            }
        }
    }
}

---

## 🔐 Credentials

Ensure Jenkins has the following credentials configured:

- Docker Hub credentials
  Type: Username/Password
  ID: dockerhub-creds

---

## 📌 Requirements

- Jenkins with:
  - Pipeline Plugin
  - Credentials Plugin
  - Git Plugin
- Docker installed on Jenkins agent
- Maven configured in Jenkins

---

## 🧩 Future Improvements

- Add image scanning (Trivy)
- Add Helm/Kubernetes deployment
- Add version tagging strategy
- Support multiple registries (ECR, Nexus)

---

## 👨‍💻 Author

Gafari Salaudeen (younghadiz) 

---

## 📜 License

This project is for learning and demonstration purposes.