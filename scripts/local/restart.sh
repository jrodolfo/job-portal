#!/bin/bash
# restart.sh
# Purpose: Restarts the local Docker Compose stack.
# Usage: ./restart.sh
# Tools: bash, docker, docker compose
# Output: Docker Compose stop and startup logs.
# Exit behavior: Exits if either stop or start fails.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

bash "${SCRIPT_DIR}/stop.sh"
bash "${SCRIPT_DIR}/start.sh"
