#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

docker exec -i mysql-db mysql -u"${MYSQL_USER:-jobuser}" -p"${MYSQL_PASSWORD:-jobpass}" "${MYSQL_DATABASE:-jobportal}" <"${REPO_ROOT}/docs/database/demo-seed.sql"
