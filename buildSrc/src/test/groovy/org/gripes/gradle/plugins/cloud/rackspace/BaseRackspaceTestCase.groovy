package org.gripes.gradle.plugins.cloud.rackspace

import org.junit.Before

class BaseRackspaceTestCase {  
  private static String user = System.getProperty("rackspaceUser")
  private static String token= System.getProperty("rackspaceAPI")
  
  Rackspace rackspace
  RackspaceAuthentication auth
  
  @Before void setupTest() { 
    auth = new RackspaceAuthentication(user: user, key: token)
    rackspace = new Rackspace(base: 'http://auth.api.rackspacecloud.com', path: '/v1.0', authentication: auth)
    
    rackspace.connect()
  }
}