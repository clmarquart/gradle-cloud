package org.gripes.gradle.plugins.cloud.rackspace

import static org.junit.Assert.*
import static groovyx.net.http.Method.*

import java.util.concurrent.*

import org.junit.Test
import org.junit.Ignore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestRackspaceFlavors extends BaseRackspaceTestCase {
  static Logger log = LoggerFactory.getLogger(TestRackspaceFlavors.class)

  @Test
  void testGetServerFlavors() {
    rackspace.service('/flavors/1', { resp, json ->
      assert json.flavor != null
      assert json.flavor.ram == 256
    })
  }

  @Test 
  void testListServerImages() {
    assert rackspace.findNewestImage("ubuntu") != null
  }

  @Test 
  void testGetLimits() {
    rackspace.service('/limits', { resp, json ->
      println "LIMITS: $json"
      assertNotNull json.limits
    })
  }
}