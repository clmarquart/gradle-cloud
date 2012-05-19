package org.gripes.gradle.plugins.cloud.rackspace

import static org.junit.Assert.*

import org.junit.Ignore
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestRackspaceAuthentication extends BaseRackspaceTestCase {
  static Logger log = LoggerFactory.getLogger(TestRackspaceAuthentication.class)
  
  @Test void testGetServerList() {
    rackspace.service('/servers', { resp, json ->
      assert json.servers.size() > 0
    })
  }
  
  @Test void testServersAreActive() {
    rackspace.service('/servers', { resp, json -> 
      json.servers.each {
        rackspace.service("/servers/${it.id}", { resp2, json2 ->
          assert json2.server.status == 'ACTIVE'
        })
      }
    })
  }
}