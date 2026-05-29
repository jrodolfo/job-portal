#!/bin/bash
# seed-demo-data.sh
# Purpose: Seeds the local MySQL database with demo data.
# Usage: ./seed-demo-data.sh
# Tools: docker, mysql client (inside container)
# Output: Status message from mysql execution.
# Exit behavior: Exits with the exit code of the docker exec command.

# Configuration: Set repository root relative to script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# Execute SQL seed script inside the mysql-db container
docker exec -i mysql-db mysql -u"${MYSQL_USER:-jobuser}" -p"${MYSQL_PASSWORD:-jobpass}" "${MYSQL_DATABASE:-jobportal}" <"${REPO_ROOT}/docs/database/demo-seed.sql"
