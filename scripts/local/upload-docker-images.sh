#!/bin/bash
# upload-docker-images.sh
# Purpose: Builds and pushes Docker images to the registry using Docker Buildx Bake.
# Usage: ./upload-docker-images.sh
# Tools: docker, docker buildx
# Output: Build logs and push status messages.
# Exit behavior: Exits with 0 on success, 1 if docker-bake.hcl is missing, or the exit code of docker buildx.

set -euo pipefail

# Configuration: Set paths relative to script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BAKE_FILE="$PROJECT_ROOT/docker-bake.hcl"

# Validation: Ensure the bake file exists before proceeding
if [ ! -f "$BAKE_FILE" ]; then
  echo "ERROR: docker-bake.hcl not found at $BAKE_FILE"
  exit 1
fi

# Build and push images
cd "$PROJECT_ROOT"
docker buildx bake -f docker-bake.hcl --push
