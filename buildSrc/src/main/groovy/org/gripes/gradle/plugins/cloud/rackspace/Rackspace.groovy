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
}