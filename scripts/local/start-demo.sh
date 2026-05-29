#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

echo "Starting local stack..."
bash "${SCRIPT_DIR}/start.sh"

echo "Loading demo seed data..."
seed_succeeded=false
for attempt in 1 2 3 4 5; do
  if bash "${SCRIPT_DIR}/seed-demo-data.sh"; then
    seed_succeeded=true
    break
  fi

  echo "Demo seed attempt ${attempt} failed. Waiting for MySQL to become ready..."
  sleep 5
done

if [ "${seed_succeeded}" != "true" ]; then
  echo "Unable to load demo seed data after multiple attempts."
  echo "Check the backend and MySQL containers with:"
  echo "  docker compose ps"
  echo "  docker compose logs -f backend"
  exit 1
fi

cat <<'EOF'

Demo environment is ready.

URLs:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Jaeger UI: http://localhost:16686

Frontend login credentials:
- Applicant user: user / user123
- Admin user: admin / admin123

Helpful commands:
- Stop the stack: bash scripts/local/stop.sh
- Reset local data completely: docker compose down -v
EOF
