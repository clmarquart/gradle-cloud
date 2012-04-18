package { 'openjdk-6-jdk':
    ensure => installed
}
package { 'tomcat6':
    ensure => installed
}
package { 'tomcat6-admin':
    ensure => installed
}
service { 'tomcat6':
    ensure => running
}