#!/bin/bash
# stop.sh
# Purpose: Stops and removes the local development environment containers and networks.
# Usage: ./stop.sh
# Tools: docker, docker compose
# Output: Docker container stopping and removal logs.
# Exit behavior: Exits with the exit code of the docker compose command.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

cd "${REPO_ROOT}"
docker compose down
