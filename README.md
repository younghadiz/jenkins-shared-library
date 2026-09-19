# Jenkins Shared Library

Reusable Jenkins Shared Library for standardizing CI/CD workflows across application repositories.

The library provides reusable pipeline orchestration and helper functions for Maven builds, Docker image creation, Docker Hub and Amazon ECR publishing, Kubernetes deployment to Amazon EKS, application version management, and automated Git version commits.

## Features

- Reusable single-service CI/CD pipeline
- Maven application build and packaging
- Automated Maven version increment
- Docker image build
- Docker Hub authentication and image push
- Amazon ECR authentication and image push
- Kubernetes deployment to Amazon EKS
- Kubernetes rollout verification
- Automated Git version commit
- Jenkins Multibranch Pipeline support
- Jenkins Ignore Committer Strategy compatibility
- Configurable Jenkins credential IDs
- Reusable Groovy utility classes
- Separation between pipeline orchestration and implementation logic

## Repository Structure

```text
jenkins-shared-library/
├── vars/
│   ├── singleServicePipeline.groovy
│   ├── multiServicePipeline.groovy
│   ├── incrementVersion.groovy
│   ├── buildMaven.groovy
│   ├── buildDockerImage.groovy
│   ├── pushToDockerHub.groovy
│   ├── pushToEcr.groovy
│   ├── deployToEks.groovy
│   └── commitVersion.groovy
│
├── src/
│   └── com/younghadiz/devops/
│       ├── PipelineConfig.groovy
│       ├── DockerUtils.groovy
│       ├── AwsUtils.groovy
│       └── KubernetesUtils.groovy
│
├── resources/
│   └── com/younghadiz/templates/
│       └── deployment.yaml.template
│
├── test/
│   └── README.md
│
├── .gitignore
└── README.md
```

## Architecture

The library separates Jenkins-facing pipeline functions from reusable implementation classes.

```text
Application Repository
        │
        │ Jenkinsfile
        ▼
Jenkins Shared Library
        │
        ▼
singleServicePipeline(...)
        │
        ├── Increment Version
        │      └── incrementVersion()
        │
        ├── Build Application
        │      └── buildMaven()
        │
        ├── Build Docker Image
        │      └── buildDockerImage()
        │             └── DockerUtils
        │
        ├── Push Docker Image
        │      ├── pushToEcr()
        │      │      └── AwsUtils
        │      │
        │      └── pushToDockerHub()
        │             └── DockerUtils
        │
        ├── Deploy
        │      └── deployToEks()
        │             └── KubernetesUtils
        │
        └── Commit Version Update
               └── commitVersion()
```

The `vars/` directory contains functions available directly to Jenkins pipelines.

The `src/` directory contains reusable implementation classes used internally by those functions.

The `resources/` directory contains reusable reference resources. Application-specific Kubernetes manifests should normally remain in the application repository.

## Single-Service Pipeline

`singleServicePipeline()` provides the primary pipeline orchestration for a repository containing one deployable application.

The pipeline runs the following stages:

```text
Increment Version
        ↓
Build Application
        ↓
Build Docker Image
        ↓
Push Docker Image
        ↓
Deploy
        ↓
Commit Version Update
```

The pipeline supports either:

```text
registryType: 'ecr'
```

or:

```text
registryType: 'dockerhub'
```

Registry values are normalized to lowercase before evaluation.

## Example Jenkinsfile

An application repository can keep its Jenkinsfile small by delegating the pipeline implementation to this library.

```groovy
@Library('jenkins-shared-library') _

singleServicePipeline(
    appDir: '.',
    manifestDir: 'kubernetes',
    appName: 'java-maven-app',

    registryType: 'ecr',

    imageName: '<aws-account-id>.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app',

    awsRegion: 'ca-central-1',
    ecrRegistryServer: '<aws-account-id>.dkr.ecr.ca-central-1.amazonaws.com',

    ecrCredentialsId: 'aws_ecr_creds',
    gitCredentialsId: 'github-token',

    repositoryUrl: 'https://github.com/<github-user>/<repository>.git',

    namespace: 'default'
)
```

Replace placeholder values with the configuration for the consuming application.

The `imageName` used with ECR should be the complete ECR repository URI:

```text
<aws-account-id>.dkr.ecr.<aws-region>.amazonaws.com/<repository-name>
```

The `ecrRegistryServer` contains only the registry server:

```text
<aws-account-id>.dkr.ecr.<aws-region>.amazonaws.com
```

## Pipeline Configuration

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `appDir` | No | `.` | Application directory |
| `manifestDir` | No | `kubernetes` | Kubernetes manifest directory |
| `appName` | Yes | — | Application and Kubernetes Deployment name |
| `registryType` | No | `ecr` | Container registry: `ecr` or `dockerhub` |
| `imageName` | Yes | — | Complete Docker image/repository name |
| `awsRegion` | ECR only | — | AWS region |
| `ecrRegistryServer` | ECR only | — | Amazon ECR registry server |
| `ecrCredentialsId` | No | `aws_ecr_creds` | Jenkins AWS credential ID |
| `dockerHubCredentialsId` | No | `dockerhub-creds` | Jenkins Docker Hub credential ID |
| `gitCredentialsId` | No | `github-token` | Jenkins Git credential ID |
| `repositoryUrl` | Yes | — | Git repository URL used for version commit push |
| `namespace` | No | `default` | Kubernetes namespace |

## Version Management

`incrementVersion()` uses Maven Build Helper and Versions plugins to increment the application's incremental version.

Conceptually:

```text
1.1.0-SNAPSHOT
      ↓
parse Maven version
      ↓
increment incremental component
      ↓
1.1.1
```

The pipeline then combines the application version with the Jenkins build number:

```text
<application-version>-<jenkins-build-number>
```

For example:

```text
1.1.1-42
```

This value becomes the Docker image tag for that pipeline execution.

## Maven Build

`buildMaven()` runs the Maven build for the application.

Default command:

```bash
mvn clean package
```

A different Maven command can be supplied when required.

Example:

```groovy
buildMaven(
    '.',
    'mvn clean package'
)
```

## Docker Image Build

`buildDockerImage()` builds the container image independently from registry authentication and image publishing.

Example:

```groovy
buildDockerImage(
    '.',
    'example/java-maven-app',
    '1.1.1-42'
)
```

The implementation is provided by:

```text
src/com/younghadiz/devops/DockerUtils.groovy
```

Separating the build and push operations allows Jenkins to expose them as independent pipeline stages.

## Docker Hub

`pushToDockerHub()` authenticates with Docker Hub using Jenkins credentials and pushes an already-built image.

Example:

```groovy
pushToDockerHub(
    'example/java-maven-app',
    '1.1.1-42',
    'dockerhub-creds'
)
```

The Jenkins credential is expected to provide a username and password or access token.

## Amazon ECR

`pushToEcr()` authenticates Docker to Amazon ECR and pushes an already-built image.

Example:

```groovy
pushToEcr(
    '<aws-account-id>.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app',
    '1.1.1-42',
    'ca-central-1',
    '<aws-account-id>.dkr.ecr.ca-central-1.amazonaws.com',
    'aws_ecr_creds'
)
```

Authentication uses:

```bash
aws ecr get-login-password
```

followed by Docker registry authentication.

The AWS credentials are supplied by Jenkins Credentials and are not stored in the repository.

## Amazon EKS Deployment

`deployToEks()` deploys application-owned Kubernetes manifests.

Expected application repository structure:

```text
application-repository/
├── Jenkinsfile
├── Dockerfile
├── pom.xml
└── kubernetes/
    ├── deployment.yaml
    └── service.yaml
```

Example:

```groovy
deployToEks(
    '.',
    'kubernetes',
    'java-maven-app',
    '<aws-account-id>.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app',
    '1.1.1-42',
    'default'
)
```

The deployment helper:

1. Verifies that `kubectl` is available.
2. Verifies that `envsubst` is available.
3. Verifies that the deployment and service manifests exist.
4. Substitutes environment variables into the manifests.
5. Applies the Deployment.
6. Applies the Service.
7. Waits for the Kubernetes Deployment rollout to complete.

Application manifests can reference:

```text
${APP_NAME}
${IMAGE_NAME}
${IMAGE_TAG}
```

The Jenkins environment must already be authenticated to the target EKS cluster.

## Git Version Commit

After a successful deployment, `commitVersion()` commits the updated Maven version.

The helper stages only:

```text
pom.xml
```

This prevents unrelated workspace changes from being included in the automated version commit.

The default Jenkins Git identity is:

```text
jenkins <jenkins@example.com>
```

The version commit is pushed using the configured Jenkins Git credentials without permanently embedding the credential in the repository remote URL.

## Jenkins Multibranch and Version-Commit Loop Prevention

A Multibranch Pipeline may receive another SCM event when Jenkins pushes the automated version commit.

The intended flow is:

```text
Developer Commit
      ↓
SCM Event
      ↓
Jenkins Multibranch Pipeline
      ↓
CI/CD Pipeline
      ↓
Jenkins Version Commit
      ↓
SCM Event
      ↓
Ignore Committer Strategy
      ↓
Jenkins-generated commit ignored
```

The Jenkins-generated commit uses:

```text
jenkins@example.com
```

When the Jenkins Ignore Committer Strategy is configured to ignore this committer, automated version commits can be excluded from triggering another application build.

This prevents the version-update stage from creating a continuous CI loop while allowing normal developer commits to trigger the pipeline.

The exact Jenkins build-strategy configuration is managed on the Jenkins controller rather than in this repository.

## Multi-Service Pipeline

`multiServicePipeline()` is reserved for repositories containing multiple independently buildable or deployable services, for example:

```text
services/
├── frontend/
├── backend/
└── worker/
```

The multi-service implementation is intentionally not enabled yet.

Single-application repositories should use:

```groovy
singleServicePipeline(...)
```

Keeping the two entry points separate prevents multi-service requirements from adding unnecessary complexity to single-service pipelines.

## Jenkins Credentials

Secrets must be stored in Jenkins Credentials and must not be committed to application or shared-library repositories.

Typical credential IDs used by this library are:

| Credential ID | Purpose |
| --- | --- |
| `github-token` | Push automated Git version commits |
| `dockerhub-creds` | Authenticate to Docker Hub |
| `aws_ecr_creds` | Authenticate AWS CLI for Amazon ECR |

Credential IDs are references only. Actual usernames, passwords, tokens, access keys, and secret keys must remain in Jenkins.

For the current AWS credential implementation, Jenkins provides the configured username/password values to the AWS CLI environment as:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

Use a dedicated AWS identity with only the permissions required by the pipeline.

## Jenkins Agent Requirements

Depending on the enabled pipeline stages, the Jenkins agent requires:

- Git
- Java
- Maven
- Docker CLI with access to a Docker daemon
- AWS CLI for Amazon ECR and EKS workflows
- `kubectl`
- `envsubst`
- Network access to the configured Git provider
- Network access to the configured container registry
- Network access to the target Kubernetes API server

The Maven tool used by `singleServicePipeline()` is currently expected to be configured in Jenkins with the name:

```text
Maven
```

## Kubernetes Reference Template

The repository contains:

```text
resources/com/younghadiz/templates/deployment.yaml.template
```

This is a reusable reference template.

The current deployment helper does not automatically load this template. Application-specific Kubernetes manifests should normally remain with the application source code so that application and deployment changes can be versioned together.

## Testing

The library currently relies on integration validation through consuming Jenkins pipelines.

Automated shared-library unit testing is not implemented yet.

See:

```text
test/README.md
```

for the current testing scope.

## Security

The library follows these repository security rules:

- Never commit AWS access keys or secret access keys.
- Never commit GitHub or GitLab tokens.
- Never commit Docker Hub passwords or access tokens.
- Never commit kubeconfig files containing sensitive cluster access data.
- Never commit private SSH keys.
- Store pipeline secrets in Jenkins Credentials.
- Reference credentials by Jenkins credential ID.
- Use dedicated CI/CD identities instead of personal credentials where practical.
- Apply least-privilege permissions to AWS and Git identities.

## Current Scope

The current library focuses on a readable CI/CD workflow for:

```text
Java + Maven
      ↓
Docker
      ↓
Docker Hub or Amazon ECR
      ↓
Amazon EKS / Kubernetes
      ↓
Git version update
```

The implementation intentionally avoids introducing unrelated deployment frameworks or infrastructure tooling into the shared-library API.

## Future Improvements

Potential extensions include:

- Jenkins Pipeline Unit tests
- Versioned shared-library releases
- Additional container registries
- Multi-service pipeline implementation
- Additional deployment strategies
- Automated rollback helpers
- Container security scanning
- Multi-environment deployment support
- Additional Kubernetes deployment abstractions
- Short-lived or workload-based cloud authentication where supported

These improvements can be introduced as project requirements grow without changing the core separation between pipeline entry points and reusable implementation classes.

## Author

**Gafari Salaudeen**

GitHub: `younghadiz`
GitLab: `younghadiz`
