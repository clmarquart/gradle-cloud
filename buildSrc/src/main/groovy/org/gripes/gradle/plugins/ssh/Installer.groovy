package org.gripes.gradle.plugins.ssh


public enum Installer {
  UBUNTU ('apt-get'),
  CENTOS ('yum')
  
  String command
  
  Installer(String command) {
    this.command = command
  }
}