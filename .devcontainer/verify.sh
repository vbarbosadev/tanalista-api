#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export PATH="$HOME/.local/bin:$HOME/.local/share/mise/shims:$PATH"
docker info >/dev/null
mise run ci

# Isolated project name; no user volumes or existing projects are removed.
export COMPOSE_PROJECT_NAME="codespaces-verification"
export COMPOSE_FILE="docker-compose.yml:compose.smoke.yml"
cleanup() {
  docker compose logs --no-color
  docker compose down
}
trap cleanup EXIT
docker compose up --build --wait -d
mise exec -- node scripts/smoke-sprint1.mjs
mise exec -- node scripts/smoke-barcode.mjs
docker compose stop barcode
mise exec -- node scripts/smoke-barcode.mjs --down
mise exec -- node scripts/smoke-sprint1.mjs
