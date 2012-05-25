package org.gripes.gradle.plugins

import static org.gripes.gradle.plugins.ssh.Service.*

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gripes.gradle.plugins.cloud.CloudService
import org.gripes.gradle.plugins.cloud.Authentication
import org.gripes.gradle.plugins.cloud.rackspace.Rackspace
import org.gripes.gradle.plugins.cloud.rackspace.RackspaceAuthentication
import org.gripes.gradle.plugins.puppet.Puppet
import org.gripes.gradle.plugins.ssh.SSH

class CloudPlugin implements Plugin<Project> {
  
  void apply(Project project) {
    project.extensions.create("cloud", CloudPluginExtension)

    def deployTask = project.task('deploy') << {
      println "SSH: " + project.configurations.antSshTask.asPath
      ant.with {
        taskdef(
          name: 'sshexec', 
          classname: 'org.apache.tools.ant.taskdefs.optional.ssh.SSHExec',                    
          classpath: project.configurations.antSshTask.asPath
        )
        taskdef(
          name: 'scp', 
          classname: 'org.apache.tools.ant.taskdefs.optional.ssh.Scp',                    
          classpath: project.configurations.antSshTask.asPath
        )
        taskdef(
          name: 'deploy', 
          classname: 'org.apache.catalina.ant.DeployTask',                    
          classpath: project.configurations.antDeployTask.asPath
        )
        taskdef(
          name: 'undeploy', 
          classname: 'org.apache.catalina.ant.UndeployTask',                    
          classpath: project.configurations.antDeployTask.asPath
        )
      }
      
      /**
       * 1. Create Rackspace object
       * 2. Setup connection parameters
       * 3. Get authentication token
       */
       
      // FIXME This needs to check {project.cloud.provider} to determine what to load
      ClassLoader classLoader = this.class.classLoader
      String serviceString = "org.gripes.gradle.plugins.cloud.${project.cloud.provider}.${project.cloud.provider.capitalize()}"
      String serviceAuthString = "org.gripes.gradle.plugins.cloud.${project.cloud.provider}.${project.cloud.provider.capitalize()}Authentication"
      Class serviceClass = classLoader.loadClass(serviceString)
      Class serviceAuthClass = classLoader.loadClass(serviceAuthString)
      Object<? extends CloudService> service = serviceClass.newInstance()
      Object<? extends Authentication> auth = serviceAuthClass.newInstance()
            
      auth.with {
        user = System.getProperty('rackspaceUser')
        key = System.getProperty('rackspaceAPI')
      }
      service.with {
        base = project.cloud.base?:delegate.base
        path = project.cloud.path?:delegate.path
        authentication = auth
        connect()
      }     
      
      if(!project.cloud.serverID) {
        println "Service: ${service}"
        println "Newest ubuntu: " + service.findNewestImage(project.cloud.serverOS)
        // Make a server and save it too project.cloud.serverID
        
      } 
      
      println "Use server: ${project.cloud.serverID}"

      String ipAddress
      service.service("/servers/${project.cloud.serverID}", { resp2, json2 ->
        ipAddress = json2.server.addresses.public[0]
        println "PUBLIC IP: " + ipAddress
        
        SSH ssh = new SSH(
          project: project,
          ant: ant,
          connection: [
            ip : ipAddress,
            user : "root",
            password : ""
        ])
        Puppet puppet = new Puppet(
          os:project.cloud.serverOS,
          connection:[
            ip:ipAddress,
            user:"root"
          ],
          ssh: ssh
        )
        
        puppet.with {
          install()
          exec("tomcat/manifests/install")
        }
        ssh.with {
          transfer("puppet/tomcat/conf/tomcat-users.xml", "/etc/tomcat6/")
          exec(TOMCAT.restart())
        }
        
        ant.undeploy(url: "http://"+ipAddress+":8080/manager",
          username: "tomcat", 
          password: "t0mc@t",
          failonerror: false,
          path: "/${project.archivesBaseName}")
        ant.deploy(url: "http://"+ipAddress+":8080/manager",
          username: "tomcat", 
          password: "t0mc@t",
          path: "/${project.archivesBaseName}",
          war: project.buildDir.canonicalPath+"/libs/${project.archivesBaseName}.war")
          
        println "Application deployed to: http://${ipAddress}:8080/${project.archivesBaseName}"
      })
    }
    deployTask.dependsOn<<project.war
  }
}