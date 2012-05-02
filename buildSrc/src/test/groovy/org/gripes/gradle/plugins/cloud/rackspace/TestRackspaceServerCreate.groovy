package org.gripes.gradle.plugins.cloud.rackspace

import static org.junit.Assert.*
import static groovyx.net.http.Method.*

import java.util.concurrent.*

import org.junit.Test
import org.junit.Ignore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.gripes.gradle.plugins.cloud.SSH

class TestRackspaceServerCreate extends BaseRackspaceTestCase {
  static Logger log = LoggerFactory.getLogger(TestRackspaceServerCreate.class)

  def newServer = [
    id : "20806805",
    addresses : [
      "public" : ["50.56.211.125"]
    ]
  ];
  
/*  @Ignore @Test*/
  void testGetServerFlavors() {
    rackspace.service('/flavors/1', { resp, json ->
      assert json.flavor != null
      assert json.flavor.ram == 256
    })
  }
  
/*  @Ignore @Test */
  void testGetLimits() {
    rackspace.service('/limits', { resp, json ->
      println "LIMITS: $json"
    })
  }  
  
/*  @Ignore @Test */
  void testListServerImages() {
    assert rackspace.findImage("ubuntu") != null
  }
  
  /**
   * Create a server
   * -Flavor: id: 1, ram: 256
   * -Image: id: 119, os: ubuntu
   */
  @Test void testCreateAServer() {
/*    testGetServerFlavors()
    testGetLimits()
    testListServerImages()*/

    rackspace.service('/servers',[
        "server" : [
          "name": "testServer",
          "flavorId": 1, 
          "imageId": 119,
          "metadata": [
            "My Server Name": "Gradle Cloud Test"
          ],
          "personality" : [
            [
              "path": "/root/.ssh/authorized_keys",
              "contents": (new File("/Users/cody/.ssh/id_dsa.pub")).bytes.encodeBase64().toString()
            ]
          ]
        ]
      ], { resp, json ->
        println "JSON: ${json}"
        newServer = json.server
        print "Building."
        while(!isComplete(newServer)) {
          print "."
          Thread.sleep(30000);
        }
        println "Done."
            
//        testCopySSHKey()
//        testSSHConnection()
        testInstallPuppetTomcat()
        testDeleteAServer()
      }
    )
  }
  
  private isComplete(newServer) {
    def done = false;
    rackspace.service("/servers/${newServer.id}",{ resp, json ->
      println "SERVER BUILD: " + json.server.status
      if(json.server.status == 'ACTIVE') {
        done = true
      }
    })
    return done;
  }
  
  void testCopySSHKey() {
    rackspace.service("/servers/"+newServer.id, { resp, json ->
      /* "/servers/"+System.getProperty("rackspaceServer") */
      
      URL templateURL = this.class.classLoader.getResource("ssh/sftp-expect.txt")
      File template = File.createTempFile("sftp-expect","")
      template.text = templateURL.text
      
      println "Have template: " + template.exists()    
      
      /*
      template.text = template.text
                        .replaceAll(/\[USER\]/,System.getProperty("rackspaceServerUser"))
                        .replaceAll(/\[PASSWORD\]/,System.getProperty("rackspaceServerPassword"))
                        .replaceAll(/\[IP\]/,System.getProperty("rackspaceServerIP")))
      */

      template.text = template.text
                        .replaceAll(/\[USER\]/,"root")
                        .replaceAll(/\[PASSWORD\]/,newServer.adminPass)
                        .replaceAll(/\[IP\]/,newServer.addresses.public[0])
                        
      template.setExecutable(true)
    
      def sout = new StringBuffer()
      def serr = new StringBuffer()    
      Process proc = "./${template.name}".execute(null, template.parentFile)
      proc.waitForOrKill(60000)
      try {
        println "OUT>"+proc.text
        println "ERR>"+proc.err.text      
      } catch (e) {
        e.printStackTrace()
      }
    })
  }
  
  void testSSHConnection() {
/*    rackspace.service("/servers/"+System.getProperty("rackspaceServer"), { resp, json ->*/
/*    ssh.connection = [*/
/*        ip : json.server.addresses.public[0],*/
/*        user : System.getProperty("rackspaceServerUser"),*/
/*        password :  System.getProperty("rackspaceServerPassword")*/
/*    ]*/

    rackspace.service("/servers/"+newServer.id, { resp, json ->
      SSH ssh = new SSH()

      ssh.connection = [
        ip : newServer.addresses.public[0],
        user : "root",
        password : newServer.adminPass
      ] 

      def result = "";
      ssh.with {
        result = exec("ping -c 1 google.com")
      }
      
      println "RESULT: $result"
    })
  }
  
  void testInstallPuppetTomcat() {
    rackspace.service("/servers/"+newServer.id, { resp, json ->
      SSH ssh = new SSH()
      
      ssh.connection = [
        ip : newServer.addresses.public[0],
        user : "root",
        password : "" //newServer.adminPass
      ] 
        
      ssh.with {
        exec("apt-get -y update && apt-get -y install puppet")
        exec("if [ -e /root/gradle-cloud ]; then echo 'exists'; else mkdir /root/gradle-cloud; fi")
        transfer("./src/test/resources/tomcat.pp", "/root/gradle-cloud")
        exec("cd /root/gradle-cloud && puppet apply tomcat.pp")
        transfer((new File("./src/test/resources/tomcat-users.xml").canonicalPath), "/etc/tomcat6/")
        exec("/etc/init.d/tomcat6 restart")
      }
      
      println "URL: " + ("http://"+newServer.addresses.public[0]+":8080").toString().toURL().openConnection().getHeaderField(0).find("200")
      assertTrue (("http://"+newServer.addresses.public[0]+":8080").toString().toURL().openConnection().getHeaderField(0).find("200")!=null)
      
      ssh.with {
        transfer("./src/test/resources/tomcat-remove.pp", "/root/gradle-cloud")
        exec("cd /root/gradle-cloud && puppet apply tomcat-remove.pp")
      }
      
      println "NULL: " + ("http://"+newServer.addresses.public[0]+":8080").toString().toURL().openConnection().getHeaderField(0)
    })
  }
  
  void testDeleteAServer() {
    rackspace.delete('/servers/'+newServer.id, { resp, json ->
        println "DELETE RESPONSE: ${resp}"
      }
    )
  }
  
  private def getTestServer() {
    def server
    rackspace.service('/servers', { resp, json ->
      server = json.servers.find { it.name == "testServer" }
    })
    server
  }
}