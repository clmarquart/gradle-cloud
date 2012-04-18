package org.gripes.gradle.plugins.cloud.rackspace

import static org.junit.Assert.*

import java.util.concurrent.*

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.connection.channel.direct.Session.Command
import net.schmizz.sshj.xfer.scp.SCPFileTransfer

import org.junit.Test
import org.junit.Ignore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestRackspaceServerCreate extends BaseRackspaceTestCase {
  static Logger log = LoggerFactory.getLogger(TestRackspaceServerCreate.class)

  @Test void testCreateServerFlavors() {
    rackspace.service('/flavors/1', { resp, json ->
      assert json!=null
    })
  }
  
  @Test void testListServerImages() {
    assert rackspace.findImage("ubuntu") != null
  }

  @Ignore
  @Test void testSSHConnection() {
    rackspace.service("/servers/"+System.getProperty("rackspaceServer"), { resp, json ->
      rackspace.connection = [
        ip : json.server.addresses.public,
        user : System.getProperty("rackspaceServerUser"),
        password :  System.getProperty("rackspaceServerPassword")
      ]
      println json.server.addresses.public
      
      rackspace.with {
        connectSSH()
//        runCommand("ping -c 1 google.com")
//        runCommand("apt-get -y install puppet")
        transfer((new File("./src/test/resources/tomcat.pp").canonicalPath), "/root/gradle-cloud")
        runCommand("cd /root/gradle-cloud && puppet apply tomcat.pp")
        transfer((new File("./src/test/resources/tomcat-users.xml").canonicalPath), "/etc/tomcat6/")
        runCommand("/etc/init.d/tomcat6 restart")
        disconnectSSH()
      }
    })
  }
  
  /**
   * Create the server
   */
  @Ignore
  @Test void testCreateAServer() {
    rackspace.service('/servers',[
        "server" : [
          "name": "testServer",
          "flavorId": 1, 
          "imageId": 119,
          "metadata": [
            "My Server Name": "Gradle Cloud Test"
          ]
        ]
      ], { resp, json ->
        println "JSON: ${json}"
      })
  }
}