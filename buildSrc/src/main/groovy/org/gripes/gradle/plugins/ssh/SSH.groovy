package org.gripes.gradle.plugins.ssh

class SSH {
  public def ant //= new AntBuilder()
  public def project
  public Map connection = [:]
  
  /**
   * User's home directory on the local machine
   *
   * @todo Compensate for non *nix machines for this with different sh location
   * @todo Have configurable for fallback
   */
  public String home = ["/bin/sh", "-c", "echo ~"].execute().text.replace("\n","")
  
  /**
   * Execute an SSH command against the using the current connection settings
   *
   * @todo Make the keyfile configurable
   *
   * @param cmd - String command to execute
   * @return Object - the output of the command
   */
  def exec(String cmd) {
    ant.sshexec(
      host: connection.ip,
      port: 22,
      trust: "yes",
      username: connection.user,
      keyfile: home+"/.ssh/id_dsa",
      command: cmd,
      outputproperty: 'result'
    )  

    return ant.project.properties['result']
  }
  
  /**
   * Transfer {src} to {dest} using SCP
   *
   * @todo Find a way to have this return boolean: true if successful
   *
   * @param src - String location of the file of
   */
  void transfer(File srcFile, String dest) {
    ant.scp(
      host: connection.ip,
      port: 22,
      trust: "yes",
      keyfile: home+"/.ssh/id_dsa",
      file: srcFile.canonicalPath,
      todir: "${connection.user}@${connection.ip}:${dest}"
    )
  }
  
  void transfer(String src, String dest) {
    URL srcURL = this.class.classLoader.getResource(src)
    transfer(srcURL, dest)
  }
  
  void transfer(URL srcURL, String dest) {
    if(srcURL) {
      File srcFile = new File(srcURL.getFile())
      if(srcFile.exists()) {
        transfer(srcFile, dest)
      } else {
        throw new RuntimeException("File for resource ${srcURL} does not exist.")
      }
    } else {
      throw new RuntimeException("Unable to transfer ${srcURL}")
    }
  }
}