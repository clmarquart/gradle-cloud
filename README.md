# Gradle-Cloud Plugin

## Overview

Ever need to deploy an application just for a meeting? Maybe for QA?  With the 
gradle-cloud plugin, by adding a few simple configuration items, you'll have
the ability to deploy to a server, using just the Gradle build.

## Features

Cloud providers:

* Rackspace
* AWS (future)

## Configuration

A few items will need to be added to `~/.gradle/gradle.properties`.  Specifically:

`systemProp.rackspaceUser=__someuser__`

`systemProp.rackspaceAPI=__YOUR_API_KEY__`


## Example

    //Use an existing server
    cloud {
      serverID = "12345"
      provider = "rackspace"
      serverOS = "UBUNTU"
    }

    //Create a new server on the go
    cloud {
      provider = "rackspace"
      serverOS = "UBUNTU"
      server = [
        "server" : [
          "name": "gradleCloud",
          "flavorId": 1, //256mb Rackspace flavor
          "imageId": 119, //New ubuntu image
          "metadata": [
            "My Server Name": "Gradle Cloud Server"
          ],
          "personality" : [[
            "path": "/root/.ssh/authorized_keys",
            "contents": new File("~/.ssh/id_dsa.pub").bytes.encodeBase64().toString()
          ]]
        ]
      ]
    }

## Tasks

`deploy` 
  - Does just that, builds the war and deploys to the server specified/created by the `cloud` settings
  
          