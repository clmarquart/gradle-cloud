package org.gripes.gradle.plugins.cloud

interface Authentication {
  String user
  String key
  
  Map headers
}