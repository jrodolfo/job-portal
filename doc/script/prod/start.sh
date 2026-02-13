#!/bin/bash
set -e

if [ -z "${OTEL_UPSTREAM_OTLP_ENDPOINT}" ]; then
  echo "ERROR: OTEL_UPSTREAM_OTLP_ENDPOINT is required for prod startup."
  echo "Example (US): export OTEL_UPSTREAM_OTLP_ENDPOINT=https://otlp.nr-data.net"
  echo "Example (EU): export OTEL_UPSTREAM_OTLP_ENDPOINT=https://otlp.eu01.nr-data.net"
  exit 1
fi

if [ -z "${OTEL_UPSTREAM_API_KEY}" ]; then
  echo "ERROR: OTEL_UPSTREAM_API_KEY is required for prod startup."
  echo "Set it to your New Relic ingest/license key."
  exit 1
fi

docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
