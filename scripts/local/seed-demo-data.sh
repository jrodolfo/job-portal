#!/bin/bash

docker exec -i mysql-db mysql -u"${MYSQL_USER:-jobuser}" -p"${MYSQL_PASSWORD:-jobpass}" "${MYSQL_DATABASE:-jobportal}" < docs/database/demo-seed.sql
