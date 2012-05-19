package org.gripes.gradle.plugins.ssh

import static org.junit.Assert.*

import org.gripes.gradle.plugins.ssh.Installer

import org.junit.Test
import org.junit.Ignore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class TestInstaller {
  String os = "CENTOS"
  
  @Test void testUbuntu() {
    assertTrue Installer.UBUNTU.command == "apt-get"
    assertTrue Installer."${os}".command == "yum"
  }
}