package org.gripes.gradle.plugins.cloud.rackspace

import static org.junit.Assert.*

import java.util.concurrent.*

import org.junit.Test
import org.junit.Ignore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.gripes.gradle.plugins.cloud.SSH

class TestRackspaceServerCreate extends BaseRackspaceTestCase {
  static Logger log = LoggerFactory.getLogger(TestRackspaceServerCreate.class)

  @Ignore
  @Test void testCreateServerFlavors() {
    rackspace.service('/flavors/1', { resp, json ->
      assert json!=null
    })
  }
  
  @Ignore
  @Test void testListServerImages() {
    assert rackspace.findImage("ubuntu") != null
  }

  @Ignore
  @Test void testCopyKey() {
    rackspace.service("/servers/"+System.getProperty("rackspaceServer"), { resp, json ->
      URL templateURL = this.class.classLoader.getResource("ssh/sftp-expect.txt")
      File template = File.createTempFile("sftp-expect","")
      template.text = templateURL.text
      println "Have template: " + template.exists()    
        
      template.text = template.text
                        .replaceAll(/\[USER\]/,System.getProperty("rackspaceServerUser"))
                        .replaceAll(/\[PASSWORD\]/,System.getProperty("rackspaceServerPassword"))    
      template.setExecutable(true)
    
      def sout = new StringBuffer()
      def serr = new StringBuffer()    
      Process proc = "./${template.name}".execute(null, template.parentFile)
      proc.waitForOrKill(30000)
      println "OUT>"+proc.text
      println "ERR>"+proc.err.text
      
    })
  }

  @Test void testSSHConnection() {
    rackspace.service("/servers/"+System.getProperty("rackspaceServer"), { resp, json ->
      SSH ssh = new SSH()
      
      ssh.connection = [
          ip : json.server.addresses.public[0],
          user : System.getProperty("rackspaceServerUser"),
          password :  System.getProperty("rackspaceServerPassword")
        ] 
        
      ssh.with {
        exec("ping -c 1 google.com")
        transfer("./src/test/resources/tomcat.pp", "/root/gradle-cloud")
        exec("cd /root/gradle-cloud && puppet apply tomcat.pp")
        transfer((new File("./src/test/resources/tomcat-users.xml").canonicalPath), "/etc/tomcat6/")
        exec("/etc/init.d/tomcat6 restart")
      }

      assertNotNull "http://50.56.182.9:8080".toURL().openConnection().getHeaderField(0).find("200") 
      
      ssh.with {
        transfer("./src/test/resources/tomcat-remove.pp", "/root/gradle-cloud")
        exec("cd /root/gradle-cloud && puppet apply tomcat-remove.pp")
      }
      
      assertNull  "http://50.56.182.9:8080".toURL().openConnection().getHeaderField(0)
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