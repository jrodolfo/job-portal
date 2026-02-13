#!/bin/bash
set -e

if [ -z "${OTEL_UPSTREAM_OTLP_ENDPOINT}" ]; then
  echo "ERROR: OTEL_UPSTREAM_OTLP_ENDPOINT is required for prod startup."
  echo "Example: export OTEL_UPSTREAM_OTLP_ENDPOINT=https://otlp.your-vendor.com/v1/traces"
  exit 1
fi

docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
