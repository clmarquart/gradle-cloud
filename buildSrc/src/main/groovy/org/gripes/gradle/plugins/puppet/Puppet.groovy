package org.gripes.gradle.plugins.puppet

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.gripes.gradle.plugins.ssh.*

class Puppet {
  private static Logger logger = LoggerFactory.getLogger(Puppet.class)
  
  public Map connection = [:]
  public SSH ssh //= new SSH()
  public String os
  
  /**
   * Location for all puppet files to be copied to and created in.
   *
   * @todo This should probably be configurable at a high-level. (i.e. Within the build.gradle)
   */
  public String workdir = "/root/gradle-cloud"
  
  /**
   * Install puppet to the server represented in
   * connection
   *
   * @return Puppet
   */
  public void install() {
    ssh.connection = [
      ip : connection.ip,
      user : connection.user,
      password : ""
    ]
    
    ssh.exec(Installer."${os}".command + " -y update && " + Installer."${os}".command + " -y install puppet")
  } 
  
  /**
   * Copies the puppet file to the remote server and executes it 
   *
   * @param pp - File
   * @return Puppet
   */
  public void exec(File pp) {
    ssh.connection = [
      ip : connection.ip,
      user : connection.user,
      password : ""
    ]
    ssh.with {      
      exec("if [ -e ${workdir} ]; then echo 'exists'; else mkdir ${workdir}; fi")
      transfer(pp, "${workdir}")
      exec("cd ${workdir} && puppet apply ${pp.name}")
    }
  }
  
  public void exec(String manifest) {
    URL manifestURL = this.class.classLoader.getResource("puppet/${manifest}.pp")
    if(manifestURL) {
      File manifestFile = new File(manifestURL.getFile())
      if(manifestFile.exists()) {
        exec(manifestFile)
      } else {
        throw new RuntimeException("The file for resource ${manifestURL}, does not exist.")
      }
    } else {
      throw new RuntimeException("Unable to locate resource: ${manifestURL}")
    }   
  }
}