package com.younghadiz.devops


class Docker implements Serializable {
   def script


   Docker(script) {
       this.script = script
   }


   void buildAndPush(String appDir, String imageName, String tag, String credentialsId = 'dockerhub-creds') {
       script.dir(appDir) {
           script.echo "Building the application for branch ${script.env.BRANCH_NAME}"


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
   }
}
