package org.gripes.gradle.plugins.ssh


public enum Service {  
  TOMCAT ('/etc/init.d/tomcat6','start','stop','restart')
  
  String serviceLocation
  String startCmd
  String stopCmd
  String restartCmd
  
  Service(String serviceLocation, String startCmd, String stopCmd, String restartCmd) {
    this.serviceLocation = serviceLocation
    this.startCmd = startCmd
    this.stopCmd = stopCmd
    this.restartCmd = restartCmd
  }
  
  String restart() {
    serviceLocation + " " + restartCmd
  }
}