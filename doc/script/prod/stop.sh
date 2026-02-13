#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Load variables from .env when present for consistency with start.sh.
if [ -f ".env" ]; then
  set -a
  . ./.env
  set +a
elif [ -f "$SCRIPT_DIR/../../../.env" ]; then
  set -a
  . "$SCRIPT_DIR/../../../.env"
  set +a
fi

docker compose -f docker-compose.yml -f docker-compose.prod.yml down
