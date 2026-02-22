#!/bin/bash
set -euo pipefail

TARGET_USER="${1:-ec2-user}"

run_as_root() {
  if [ "$(id -u)" -eq 0 ]; then
    "$@"
  else
    sudo "$@"
  fi
}

install_with_dnf() {
  run_as_root dnf install -y docker

  if run_as_root dnf install -y docker-compose-plugin; then
    return 0
  fi

  run_as_root dnf install -y docker-compose
}

install_with_yum() {
  run_as_root yum install -y docker

  if run_as_root yum install -y docker-compose-plugin; then
    return 0
  fi

  run_as_root yum install -y docker-compose
}

if ! command -v docker >/dev/null 2>&1; then
  if command -v dnf >/dev/null 2>&1; then
    install_with_dnf
  elif command -v yum >/dev/null 2>&1; then
    install_with_yum
  else
    echo "ERROR: neither dnf nor yum was found; install Docker manually."
    exit 1
  fi
fi

run_as_root systemctl enable docker
run_as_root systemctl start docker

if id -u "$TARGET_USER" >/dev/null 2>&1; then
  run_as_root usermod -aG docker "$TARGET_USER"
else
  echo "WARNING: user '$TARGET_USER' not found; skipping docker group update."
fi

echo "Docker bootstrap completed."
echo "Verify:"
echo "  docker info"
echo "  docker compose version || docker-compose version"
echo "If group membership was updated, logout/login (or run: newgrp docker)."
