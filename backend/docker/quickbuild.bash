#!/bin/bash

docker compose down

../shared/gradlew publishToMavenLocal

docker compose build
docker compose up -d
