#!/usr/bin/env groovy
import com.younghadiz.devops.Docker

// real implementation (class file) lives in src/com/younghadiz/devops/Docker.groovy. So this pass the parameters to the Docker.groovy and get the reult back to the Jenkinsfile
def call(String appDir, String imageName, String tag, String credentialsId = 'dockerhub-creds') {
   return new Docker(this).buildAndPush(appDir, imageName, tag, credentialsId)
}
