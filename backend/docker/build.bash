#!/bin/bash

(
  cd ..

  set -e #faz o programa terminar em caso de erro

  for dir in *Service/; do
    if [ -d "$dir" ]; then
      echo  "Buildando $dir"
      (
        cd "$dir" || exit
        ./gradlew build -x test
      )
    fi
  done
)

docker compose build
docker compose up -d
