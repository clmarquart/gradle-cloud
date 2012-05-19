package org.gripes.gradle.plugins.cloud.rackspace

import static groovyx.net.http.Method.*
import static org.gripes.gradle.plugins.ssh.Service.*

import java.util.concurrent.*

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Ignore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.gripes.gradle.plugins.ssh.SSH

import org.gripes.gradle.plugins.puppet.Puppet

class TestRackspaceServerCreate extends BaseRackspaceTestCase {
  static Logger log = LoggerFactory.getLogger(TestRackspaceServerCreate.class)
  
  def testServer = [
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
  ]
  
  @Test void testFindResources() {
    URL installManfifest = this.class.classLoader.getResource("puppet/tomcat/manifests/install.pp")
    File installManfifestFile = new File(installManfifest.getFile())
    assertTrue installManfifestFile.exists()
  }
  
  /**
   * Create a server
   * -Flavor: id: 1, ram: 256
   * -Image: id: 119, os: ubuntu
   */
  @Ignore
  @Test void testCreateAServer() {
    rackspace.create(testServer, { newServer ->              
      testInstallPuppetTomcat(newServer)
      deleteServerWithId(newServer.id)
    })
  }
  
  @Ignore
  @Test void testDeleteServer() {
    deleteServerWithId('20816466')
  }
  
  void deleteServerWithId(id) {
    rackspace.delete(id, { success ->
      println "Successful delete: "+ success
      assertTrue success
    })
  }
  
  void testInstallPuppetTomcat(def newServer) {
    SSH ssh = new SSH()
    
    ssh.connection = [
      ip : newServer.addresses.public[0],
      user : "root",
      password : "" //newServer.adminPass
    ]
    
    Puppet puppet = new Puppet(os:"UBUNTU",connection:[ip:newServer.addresses.public[0],user:"root"])
    puppet.with {
      install()
      exec("tomcat/manifests/install")
    }
    
    ssh.with {
      transfer("puppet/tomcat/conf/tomcat-users.xml", "/etc/tomcat6/")
      exec(TOMCAT.restart())
    }
    
    assertTrue (("http://"+newServer.addresses.public[0]+":8080").toString().toURL().openConnection().getHeaderField(0).find("200")!=null)
    
    puppet.exec("tomcat/manifests/remove")
    
    assertTrue (("http://"+newServer.addresses.public[0]+":8080").toString().toURL().openConnection().getHeaderField(0)==null)
  }
}