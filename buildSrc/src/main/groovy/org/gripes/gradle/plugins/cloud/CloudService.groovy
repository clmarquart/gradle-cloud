package org.gripes.gradle.plugins.cloud

interface CloudService {
  String base
  String path
  
  Map connection
  
  Authentication authentication

  void connect()
}