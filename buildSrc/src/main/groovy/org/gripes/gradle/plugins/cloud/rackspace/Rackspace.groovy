package org.gripes.gradle.plugins.cloud.rackspace

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import org.gripes.gradle.plugins.cloud.CloudService
import groovyx.net.http.*

import java.util.concurrent.*

class Rackspace implements CloudService {
  String base
  String path
  
  Map connection = [:]
  
  String serviceBase
  String authToken
  
  RackspaceAuthentication authentication
  
  /**
   * Connect to Rackspace and receive authorization token for
   * future querys.
   */
  void connect() {
    new HTTPBuilder(base).get(path: path, headers: authentication.headers) { resp, reader ->      
      serviceBase = resp.headers['X-Server-Management-Url'].value
      authToken = resp.headers['X-Auth-Token'].value
    }
  }
  
  def service(String servicePath, Closure success) {
    println "PATH: " + serviceBase+servicePath
    def serverHTTP = new HTTPBuilder(serviceBase+servicePath)
    serverHTTP.get([headers: ['X-Auth-Token' : authToken]], success)
  }
  
  def service(String servicePath, Map params, Closure success) {    
/*    def serverHTTP = new HTTPBuilder("https://${(serviceBase+servicePath).toURI().host}")*/
    println "PATH: " + serviceBase+servicePath
    def serverHTTP = new HTTPBuilder(serviceBase+servicePath)
    
    serverHTTP.headers = [
      'X-Auth-Token' : authToken
    ]
    
    serverHTTP.request( POST, JSON ) {
      uri.path = (serviceBase+servicePath).toURI().path
      body = params
      response.success = success
    }
  }
  
  def delete(String servicePath, Closure success) {
    def serverHTTP = new HTTPBuilder(serviceBase+servicePath)
    
    serverHTTP.headers = [
      'X-Auth-Token' : authToken
    ]
    
    serverHTTP.request( DELETE, JSON ) {
      uri.path = (serviceBase+servicePath).toURI().path
      response.success = success
    }
  }
  
  /**
   * Finds the latest version of the provided image
   * 
   * Example return from API:
   *  {
   *    "images" : [
   *      {
   *          "id" : 1,
   *          "name" : "CentOS 5.2",
   *          "updated" : "2010-10-10T12:00:00Z",
   *          "created" : "2010-08-10T12:00:00Z",
   *          "status" : "ACTIVE"
   *      },
   *      {
   *          "id" : 743,
   *          "name" : "My Server Backup",
   *          "serverId" : 12,
   *          "updated" : "2010-10-10T12:00:00Z",
   *          "created" : "2010-08-10T12:00:00Z",
   *          "status" : "SAVING",
   *          "progress" : 80
   *      }
   *    ]
   *  }
   * 
   * @param img - String of the type of image to look for (i.e. Ubuntu)
   * @return image - JSON representation
   */
  def findImage(String img) {
    def foundImage
    service('/images', { resp, json ->
      foundImage = json.images.findAll{it.name.find("(?i)"+img)}.max{a,b->a.id<=>b.id}
    })
    foundImage?:null
  }
}