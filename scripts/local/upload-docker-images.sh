#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BAKE_FILE="$PROJECT_ROOT/docker-bake.hcl"

if [ ! -f "$BAKE_FILE" ]; then
  echo "ERROR: docker-bake.hcl not found at $BAKE_FILE"
  exit 1
fi

cd "$PROJECT_ROOT"
docker buildx bake -f docker-bake.hcl --push
