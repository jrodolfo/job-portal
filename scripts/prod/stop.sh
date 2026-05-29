#!/bin/bash
# stop.sh
# Purpose: Stops and removes the production Docker stack.
# Usage: ./stop.sh
# Tools: bash, docker, docker compose (or docker-compose)
# Output: Status messages and Docker output.
# Exit behavior: Exits with 0 on success, 1 if requirements are missing or commands fail.

set -e

# Configuration: Locate project root and compose files
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT=""
COMPOSE_CMD=()

# Supports running from:
# 1) repository scripts/prod/stop.sh, or
# 2) a copied script in project root.
if [ -f "$SCRIPT_DIR/../../docker-compose.yml" ] && [ -f "$SCRIPT_DIR/../../docker-compose.prod.yml" ]; then
  PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
elif [ -f "$SCRIPT_DIR/docker-compose.yml" ] && [ -f "$SCRIPT_DIR/docker-compose.prod.yml" ]; then
  PROJECT_ROOT="$SCRIPT_DIR"
fi

# Validation: Ensure project root is found
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

# Dependency Check: Verify docker installation
if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker is not installed or not in PATH."
  exit 1
fi

# Dependency Check: Verify docker daemon connectivity
if ! docker info >/dev/null 2>&1; then
  echo "ERROR: docker daemon is not reachable. Start docker and retry."
  echo "Try:"
  echo "  sudo systemctl start docker"
  echo "  sudo systemctl enable docker"
  echo "If docker is running but this user still cannot connect:"
  echo "  sudo usermod -aG docker \$USER"
  echo "  # then logout/login (or run: newgrp docker)"
  exit 1
fi

# Dependency Check: Locate docker compose or docker-compose
if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
else
  echo "ERROR: Docker Compose is not available."
  echo "Try on Amazon Linux:"
  echo "  sudo dnf install -y docker-compose-plugin"
  echo "  # or, if unavailable:"
  echo "  sudo dnf install -y docker-compose"
  exit 1
fi

# Validation: Ensure compose files exist
if [ ! -f "$BASE_COMPOSE" ]; then
  echo "ERROR: Missing compose file: $BASE_COMPOSE"
  exit 1
fi

if [ ! -f "$PROD_COMPOSE" ]; then
  echo "ERROR: Missing compose file: $PROD_COMPOSE"
  exit 1
fi

# Configuration: Load variables from .env when present
if [ -f "$ENV_FILE" ]; then
  set -a
  . "$ENV_FILE"
  set +a
fi

# Execution: Stop and remove containers
echo "Using compose command: ${COMPOSE_CMD[*]}"
"${COMPOSE_CMD[@]}" -f "$BASE_COMPOSE" -f "$PROD_COMPOSE" down
