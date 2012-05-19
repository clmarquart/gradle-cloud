service { 'tomcat6':
    ensure => stopped
}
package { 'tomcat6-admin':
    ensure => purged
}
package { 'tomcat6':
    ensure => purged
}
package { 'openjdk-6-jdk':
    ensure => purged
}