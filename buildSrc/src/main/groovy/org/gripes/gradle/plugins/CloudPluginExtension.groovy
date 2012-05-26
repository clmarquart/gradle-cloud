package org.gripes.gradle.plugins

class CloudPluginExtension {
  
  /**
   * Service provider
   */
  String provider
  
  /**
   * Name of the server being created
   */
  String serverName
  
  /**
   * Server OS being created
   */
  String serverOS
  
  /**
   * The ID of the server being used.  Null if
   * we are creating a new one.
   */
  String serverID
    
  /**
   * Service base URL
   */
  String base
  
  /**
   * Service URL path
   */
  String path
    
  /**
   *  server = [
   *    "server" : [
   *      "name": "gradleCloud",
   *      "flavorId": 1, 
   *      "imageId": 119,
   *      "metadata": [
   *        "My Server Name": "Gradle Cloud Server"
   *      ],
   *      "personality" : [
   *        [
   *          "path": "/root/.ssh/authorized_keys",
   *          "contents": (new File("/Users/cody/.ssh/id_dsa.pub")).bytes.encodeBase64().toString()
   *        ]
   *      ]
   *    ]
   *  ]
   */
  Map server
}