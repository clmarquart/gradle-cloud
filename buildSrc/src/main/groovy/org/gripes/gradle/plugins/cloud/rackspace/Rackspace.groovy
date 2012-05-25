package org.gripes.gradle.plugins.cloud.rackspace

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import org.gripes.gradle.plugins.cloud.CloudService
import groovyx.net.http.*

import java.util.concurrent.*

/**
 * Implements the CloudService interface to provide access to the Rackspace
 * API
 *
 * @todo Helper methods for finding a particular Server by ID
 * 
 * @author clmarquart
 */
class Rackspace implements CloudService {
  String base = 'http://auth.api.rackspacecloud.com'
  String path = '/v1.0'
  
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
  
  /**
   * Just calls {service(String, Closure)}, used for better
   * readibility of the service calls.
   *
   * @param servicePath - String service to call from Rackspace
   * @param success - Closure to call upon successful API call
   */
  def get(String servicePath, Closure success) {
    service(servicePath, success)
  }
  
  /**
   * Performs a GET request and calls {success} when the API
   * call is successful.
   *
   * @param servicePath - String service to call from Rackspace
   * @param success - Closure to call upon successful API call
   */
  def service(String servicePath, Closure success) {
    def serverHTTP = createHTTPBuilder(servicePath)
    
    serverHTTP.request( GET, JSON ) { req ->
      response.success = success
    }
  }
  
  /**
   * Performs a POST request with {params} and calls {success} 
   * when the API call is successful.
   *
   * @param servicePath - String service to call from Rackspace
   * @param success - Closure to call upon successful API call
   */
  def service(String servicePath, Map params, Closure success) {
    def serverHTTP = createHTTPBuilder(servicePath)
    
    serverHTTP.request( POST, JSON ) {
      uri.path = (serviceBase+servicePath).toURI().path
      body = params
      response.success = success
    }
  }
  
  /**
   * Create a new Rackspace server with the given params
   *
   * @param servicePath
   * @param params
   * @param success
   */
  def create(Map params, Closure success) {
    service("/servers", params, { resp, json ->    
      def newServer = json.server
      
      while(!isComplete(newServer.id)) {
        Thread.sleep(30000);
      }
      
      success.call(newServer)
    })
  }
  
  /**
   * Delete a server as defined by servicePath
   * 
   * @param servicePath
   * @param success
   */
  def delete(String servicePath, Closure success) {
    servicePath = '/servers/'+servicePath
    
    def serverHTTP = createHTTPBuilder(servicePath)

    serverHTTP.request( DELETE, JSON ) {      
      uri.path = (serviceBase+servicePath).toURI().path
      
      response.success = { resp, json ->
        success.call(true)
      }
      response.'404' = { resp, json ->
        success.call(false)
      }
    }
  }
  
  /**
   * Delete a server as defined by servicePath
   * 
   * @param servicePath
   * @param success
   */
  def delete(int servicePath, Closure success) {
    delete(servicePath.toString(), success)
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
  def findNewestImage(String img) {
    def foundImage
    service('/images', { resp, json ->
      foundImage = json.images.findAll{it.name.find("(?i)"+img)}.max{a,b->a.id<=>b.id}
    })
    foundImage?:null
  }

  /**
   * Poll a server to determine if the server is built
   * and active.
   *
   * @param newServer - String id of the server to check
   * @return boolean - true if 'ACTIVE', false otherwise
   */
  public boolean isComplete(int newServer) {
    def done = false;
    service("/servers/${newServer}",{ resp, json ->
      if(json.server.status == 'ACTIVE') {
        done = true
      }
    })
    return done;
  }
  
  /**
   * Find specific server by name
   * 
   * @param name - String server name
   * @return JSON representation of server, or null if not found
   */
  public def findServer(String name) {
    def server
    service('/servers', { resp, json ->
      server = json.servers.find { it.name == name }
    })
    server
  }
  
  /**
   * Helper method to construct the HTTPBuilder with
   * the proper headers.
   *
   * @param servicePath
   */
  private createHTTPBuilder(servicePath) {
    def serverHTTP = new HTTPBuilder(serviceBase+servicePath)

    serverHTTP.headers = [
      'X-Auth-Token' : authToken
    ]

    serverHTTP
  }
}