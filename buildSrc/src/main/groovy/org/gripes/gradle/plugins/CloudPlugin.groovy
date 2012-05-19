package org.gripes.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gripes.gradle.plugins.cloud.CloudService
import org.gripes.gradle.plugins.cloud.rackspace.Rackspace
import org.gripes.gradle.plugins.cloud.rackspace.RackspaceAuthentication

class CloudPlugin implements Plugin<Project> {
  
  void apply(Project project) {
    project.extensions.create("cloud", CloudPluginExtension)

    project.task('deploy') << {
      if(project.cloud.serverID) {
        println "Use server: ${project.cloud.serverID}"
      } else {
        ClassLoader classLoader = this.class.classLoader
        String serviceString = "org.gripes.gradle.plugins.cloud.${project.cloud.provider}.${project.cloud.provider.capitalize()}"
        Object<? extends CloudService> service = classLoader.loadClass(serviceString)

        println "Service: ${service}"
        
        /**
         * 1. Create Rackspace object
         * 2. Setup connection parameters
         * 3. Get authentication token
         */
        RackspaceAuthentication auth = new RackspaceAuthentication(user: System.getProperty("rackspaceUser"), key: System.getProperty("rackspaceAPI"))
        Rackspace rackspace = new Rackspace(base: 'http://auth.api.rackspacecloud.com', path: '/v1.0', authentication: auth)

        rackspace.connect()
        
        println "Newest ubuntu: " + rackspace.findNewestImage("ubuntu")
      }
    }
  }
}