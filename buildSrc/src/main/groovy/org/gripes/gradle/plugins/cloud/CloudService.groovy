package org.gripes.gradle.plugins.cloud

import net.schmizz.sshj.SSHClient

interface CloudService {
  String base
  String path
  
  SSHClient ssh
  
  Map connection
  
  Authentication authentication
}