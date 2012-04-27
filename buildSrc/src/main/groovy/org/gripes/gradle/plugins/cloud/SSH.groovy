package org.gripes.gradle.plugins.cloud

class SSH {
  Map connection = [:]
  
  def ant = new AntBuilder()
  String home = ["/bin/sh", "-c", "echo ~"].execute().text.replace("\n","")
  
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
  
  def transfer(String src, String dest) {
    ant.scp(
      host: connection.ip,
      port: 22,
      trust: "yes",
      keyfile: home+"/.ssh/id_dsa",
      file: src,
      todir: "${connection.user}@${connection.ip}:${dest}"
    )
  }
}