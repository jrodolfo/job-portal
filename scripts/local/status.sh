#!/bin/bash
# status.sh
# Purpose: Shows the status of the local Docker Compose stack.
# Usage: ./status.sh
# Tools: docker, docker compose
# Output: Docker Compose service status and common local URLs.
# Exit behavior: Exits with the exit code of the docker compose command.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

cd "${REPO_ROOT}"
docker compose ps

cat <<'EOF'

Local URLs:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Jaeger UI: http://localhost:16686
EOF
