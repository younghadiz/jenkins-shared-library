#!/usr/bin/env groovy


def call(String appDir) {
   dir(appDir) {
       sh 'mvn clean package'
   }
}
