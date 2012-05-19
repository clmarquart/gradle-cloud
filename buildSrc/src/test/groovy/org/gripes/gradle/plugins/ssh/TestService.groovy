package org.gripes.gradle.plugins.ssh

import static org.junit.Assert.*

import org.junit.Test
import org.junit.Ignore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class TestService {

  @Test void testService() {
    assert Service.TOMCAT.serviceLocation == "/etc/init.d/tomcat6"
  }
  
  @Test void testServiceRestart() {
    assert Service.TOMCAT.restart() == "/etc/init.d/tomcat6 restart"
  }
}