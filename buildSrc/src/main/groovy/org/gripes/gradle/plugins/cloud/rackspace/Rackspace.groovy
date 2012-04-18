package org.gripes.gradle.plugins.cloud.rackspace

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import org.gripes.gradle.plugins.cloud.CloudService
import groovyx.net.http.*

import java.util.concurrent.*

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.connection.channel.direct.Session.Command
import net.schmizz.sshj.xfer.scp.SCPFileTransfer

class Rackspace implements CloudService {
  String base
  String path
  
  SSHClient ssh = new SSHClient()
  Map connection = [:]
  
  String serviceBase
  String authToken
  
  RackspaceAuthentication authentication
  
  void connect() {
    new HTTPBuilder(base).get(path: path, headers: authentication.headers) { resp, reader ->      
      serviceBase = resp.headers['X-Server-Management-Url'].value
      authToken = resp.headers['X-Auth-Token'].value
    }
  }
  
  def service(String path, Closure success) {
    def serverHTTP = new HTTPBuilder(serviceBase+path)
    serverHTTP.get([headers: ['X-Auth-Token' : authToken]], success)
  }
  
  def service(String servicePath, Map params, Closure success) {    
    def serverHTTP = new HTTPBuilder("https://${(serviceBase+servicePath).toURI().host}")
    
    serverHTTP.headers = [
      'X-Auth-Token' : authToken
    ]
    
    serverHTTP.request( POST, JSON ) {
      uri.path = (serviceBase+servicePath).toURI().path
      body = params
      response.success = success
    }
  }
  
  /**
   * Finds the latest version of the provided image
   *
   * @param img - String of the type of image to look for (i.e. Ubuntu)
   *s
   * @return image - JSON representation
   */
  def findImage(String img) {
    println "Image: " + img
    def foundImage
    service('/images', { resp, json ->
      println "JSON: " + json
      foundImage = json.images.findAll{it.name.find("(?i)"+img)}.max{a,b->a.id<=>b.id}
    })
    foundImage?:null
  } 
  
  /**
   * 
   */
  def runCommand(String cmdStr) { 
    Session session
    Command cmd
         
    try {
      session = ssh.startSession();
      cmd = session.exec(cmdStr);
//        cmd = session.exec("apt-get -y install puppet")
      println(IOUtils.readFully(cmd.getInputStream()).toString());
      cmd.join(5, TimeUnit.SECONDS);
      println("\n** exit status: " + cmd.getExitStatus());
    } catch (e) {  
      e.printStackTrace()
      session.close()
    } 
  }
  
  def transfer(src, dest) {
    SCPFileTransfer tfr
    tfr = ssh.newSCPFileTransfer()
    tfr.upload(src, dest)
  }
  
  void connectSSH() {
    try {
      ssh.loadKnownHosts();
      ssh.connect(connection['ip']);
  
      try {
        ssh.authPassword(connection['user'], connection['password'])
      } catch (e) {  
        e.printStackTrace()
      }
    } catch (e) {
      e.printStackTrace()
    }
  }
  
  void disconnectSSH() {
    ssh.disconnect()
  }  
}