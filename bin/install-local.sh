#!/bin/bash

projects[i++]="io.github.athingx.athing.aliyun.config:config-thing"
projects[i++]="io.github.athingx.athing.aliyun.config:config-thing-impl"

mvn clean install \
  -f ../pom.xml \
  -pl "$(printf "%s," "${projects[@]}")" -am \
  '-Dmaven.test.skip=true'