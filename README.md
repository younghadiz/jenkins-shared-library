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

This shared library follows a clean separation of concerns:

- vars/ → Public API (simple callable pipeline steps)
- src/ → Internal implementation (structured classes and logic)

This approach aligns with Jenkins Shared Library best practices and improves maintainability and scalability.

---

## 🚀 Usage

### Option 1 — Global Library (Recommended)

Register this repository in Jenkins:

Manage Jenkins → System → Global Pipeline Libraries

Then use in your Jenkinsfile:

@Library('jenkins-shared-library') _

---

### Option 2 — Load Dynamically (Project-level Control)

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

    tools {
        maven 'Maven3.9'
    }

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

## 🧩 Shared Library Design Decision

Initially, the Jenkins Shared Library was created as a nested folder within the main project repository:

https://github.com/younghadiz/devops-and-cloud-projects-lab/tree/main/02_labs/phase-03/08_jenkins/jenkins-shared-library

While this structure worked for learning purposes, it introduced some limitations:

- It required configuration through Jenkins Global Pipeline Libraries
- It made dynamic library loading (library(...)) difficult
- It limited flexibility in controlling which projects could use the library
- It was less scalable for larger teams and multi-project environments

To address these limitations, the shared library was refactored into a separate repository:

https://github.com/younghadiz/jenkins-shared-library

---

## 🚀 Benefits of the Separate Shared Library

- Enables dynamic loading of the library per project using library(...)
- Provides better access control (project-level vs global)
- Improves scalability for multi-service and multi-team environments
- Aligns with industry best practices for CI/CD architecture
- Simplifies reuse across different repositories and pipelines

---

## 🧠 Learning Approach

The nested shared library is still retained in the original repository for:

- Reference and revision
- Tutorial continuity
- Understanding the evolution from simple to production-ready architecture

This demonstrates a progression from:

Learning-focused structure → Production-ready design

---

## 🧩 Future Improvements

- Add image security scanning (Trivy)
- Add Kubernetes/Helm deployment stages
- Implement versioned shared library releases (v1.0.0, v1.1.0)
- Support multiple registries (ECR, Nexus)
- Add pipeline testing strategies

---

## 👨‍💻 Author

Gafari Salaudeen (younghadiz)

---

## 📜 License

This project is for learning and demonstration purposes.