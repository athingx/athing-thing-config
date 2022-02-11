#!/bin/bash

projects[i++]="io.github.athingx.athing.thing.config:thing-config"
projects[i++]="io.github.athingx.athing.thing.config:thing-config-aliyun"

mvn clean install \
  -f ../pom.xml \
  -pl "$(printf "%s," "${projects[@]}")" -am \
  '-Dmaven.test.skip=true'