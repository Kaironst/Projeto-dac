#!/usr/bin/env bash

set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

"$root_dir/backend/docker/quickbuild.bash"

cd "$root_dir/frontend"

#roda npm install se node_modules não existir ou se package.json ou package-lock.json 
#for mais recente que node_modules
if [[ ! -d node_modules || package.json -nt node_modules || package-lock.json -nt node_modules ]]; then
  npm install
fi

npm start
