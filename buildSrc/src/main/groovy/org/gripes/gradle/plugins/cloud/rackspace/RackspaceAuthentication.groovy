package org.gripes.gradle.plugins.cloud.rackspace

import org.gripes.gradle.plugins.cloud.*

class RackspaceAuthentication implements Authentication {
  String user
  String key
  
  Map headers
  
  Map getHeaders() {
    [
      'X-Auth-User' : user,
      'X-Auth-Key'  : key
    ]
  }
} 