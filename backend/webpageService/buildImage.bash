#!/bin/bash
docker build --build-arg JAR_FILE=build/libs/\*.jar -t springio/gs-spring-boot-docker .
