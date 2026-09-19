#!/usr/bin/env groovy

import com.younghadiz.devops.PipelineConfig

def call(Map config = [:]) {

    def appDir = PipelineConfig.optional(
        config,
        'appDir',
        '.'
    )

    def manifestDir = PipelineConfig.optional(
        config,
        'manifestDir',
        'kubernetes'
    )

    def appName = PipelineConfig.required(
        config,
        'appName'
    )

    def registryType = PipelineConfig.optional(
        config,
        'registryType',
        'ecr'
    ).toLowerCase()

    def imageName = PipelineConfig.required(
        config,
        'imageName'
    )

    def awsRegion = PipelineConfig.optional(
        config,
        'awsRegion',
        ''
    )

    def ecrRegistryServer = PipelineConfig.optional(
        config,
        'ecrRegistryServer',
        ''
    )

    def ecrCredentialsId = PipelineConfig.optional(
        config,
        'ecrCredentialsId',
        'aws_ecr_creds'
    )

    def dockerHubCredentialsId = PipelineConfig.optional(
        config,
        'dockerHubCredentialsId',
        'dockerhub-creds'
    )

    def gitCredentialsId = PipelineConfig.optional(
        config,
        'gitCredentialsId',
        'github-token'
    )

    def repositoryUrl = PipelineConfig.required(
        config,
        'repositoryUrl'
    )

    def namespace = PipelineConfig.optional(
        config,
        'namespace',
        'default'
    )

    pipeline {
        agent any

        tools {
            maven 'Maven'
        }

        stages {

            stage('Increment Version') {
                steps {
                    script {
                        echo "Running pipeline for branch: ${env.BRANCH_NAME}"

                        def version = incrementVersion(appDir)

                        env.APP_VERSION = version
                        env.IMAGE_TAG = "${version}-${env.BUILD_NUMBER}"

                        echo "Application version: ${env.APP_VERSION}"
                        echo "Docker image tag: ${env.IMAGE_TAG}"
                    }
                }
            }

            stage('Build Application') {
                steps {
                    script {
                        buildMaven(
                            appDir,
                            'mvn clean package'
                        )
                    }
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        buildDockerImage(
                            appDir,
                            imageName,
                            env.IMAGE_TAG
                        )
                    }
                }
            }

            stage('Push Docker Image') {
                steps {
                    script {

                        if (registryType == 'ecr') {

                            pushToEcr(
                                imageName,
                                env.IMAGE_TAG,
                                awsRegion,
                                ecrRegistryServer,
                                ecrCredentialsId
                            )

                        } else if (registryType == 'dockerhub') {

                            pushToDockerHub(
                                imageName,
                                env.IMAGE_TAG,
                                dockerHubCredentialsId
                            )

                        } else {
                            error(
                                "Unsupported registry type: ${registryType}"
                            )
                        }
                    }
                }
            }

            stage('Deploy') {
                steps {
                    script {
                        deployToEks(
                            appDir,
                            manifestDir,
                            appName,
                            imageName,
                            env.IMAGE_TAG,
                            namespace
                        )
                    }
                }
            }

            stage('Commit Version Update') {
                steps {
                    script {
                        commitVersion(
                            appDir,
                            gitCredentialsId,
                            repositoryUrl,
                            env.BRANCH_NAME,
                            'ci: version bump',
                            'jenkins',
                            'jenkins@example.com'
                        )
                    }
                }
            }
        }

        post {
            success {
                echo 'Pipeline completed successfully.'
            }

            failure {
                echo 'Pipeline failed.'
            }

            always {
                echo 'Pipeline finished.'
            }
        }
    }
}
