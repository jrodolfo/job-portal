#!/bin/bash
# start.sh
# Purpose: Builds and starts the local development environment in detached mode.
# Usage: ./start.sh
# Tools: docker, docker compose
# Output: Docker build and container startup logs.
# Exit behavior: Exits with the exit code of the docker compose command.

docker compose up -d --build
