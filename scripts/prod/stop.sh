#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT=""

# Supports running from:
# 1) repository scripts/prod/stop.sh, or
# 2) a copied script in project root.
if [ -f "$SCRIPT_DIR/../../docker-compose.yml" ] && [ -f "$SCRIPT_DIR/../../docker-compose.prod.yml" ]; then
  PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
elif [ -f "$SCRIPT_DIR/docker-compose.yml" ] && [ -f "$SCRIPT_DIR/docker-compose.prod.yml" ]; then
  PROJECT_ROOT="$SCRIPT_DIR"
fi

if [ -z "$PROJECT_ROOT" ]; then
  echo "ERROR: Could not locate project root."
  echo "Expected docker-compose.yml and docker-compose.prod.yml either in:"
  echo "  - $SCRIPT_DIR/../.."
  echo "  - $SCRIPT_DIR"
  exit 1
fi

BASE_COMPOSE="$PROJECT_ROOT/docker-compose.yml"
PROD_COMPOSE="$PROJECT_ROOT/docker-compose.prod.yml"
ENV_FILE="$PROJECT_ROOT/.env"

if [ ! -f "$BASE_COMPOSE" ]; then
  echo "ERROR: Missing compose file: $BASE_COMPOSE"
  exit 1
fi

if [ ! -f "$PROD_COMPOSE" ]; then
  echo "ERROR: Missing compose file: $PROD_COMPOSE"
  exit 1
fi

# Load variables from .env when present for consistency with start.sh.
if [ -f "$ENV_FILE" ]; then
  set -a
  . "$ENV_FILE"
  set +a
fi

docker compose -f "$BASE_COMPOSE" -f "$PROD_COMPOSE" down
