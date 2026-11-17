#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export PATH="$HOME/.local/bin:$HOME/.local/share/mise/shims:$PATH"
# Docker-in-Docker starts asynchronously with the container entrypoint.
for attempt in {1..30}; do
  if docker info >/dev/null 2>&1; then
    break
  fi
  echo "Aguardando Docker ($attempt/30)..."
  sleep 2
done
if ! docker info; then
  echo "Docker não iniciou no prazo de 60 segundos." >&2
  if [ -r /tmp/dockerd.log ]; then
    tail -n 100 /tmp/dockerd.log >&2
  fi
  exit 1
fi
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
