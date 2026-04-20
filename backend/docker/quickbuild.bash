#!/bin/bash

name=$(dirname "$0")
cd $name

docker compose down

DOCKER_BUILDKIT=1 docker compose build
docker compose up -d

