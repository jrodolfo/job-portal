#!/bin/bash
# start.sh
# Purpose: Builds and starts the local development environment in detached mode.
# Usage: ./start.sh
# Tools: docker, docker compose
# Output: Docker build and container startup logs.
# Exit behavior: Exits with the exit code of the docker compose command.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

cd "${REPO_ROOT}"
docker compose up -d --build
