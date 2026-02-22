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

detect_compose_os_arch() {
  local os arch
  os="$(uname -s | tr '[:upper:]' '[:lower:]')"
  arch="$(uname -m)"

  case "$arch" in
    x86_64|amd64)
      arch="x86_64"
      ;;
    aarch64|arm64)
      arch="aarch64"
      ;;
    *)
      echo "ERROR: unsupported architecture for compose binary: $arch"
      exit 1
      ;;
  esac

  echo "${os}-${arch}"
}

install_compose_from_github() {
  local platform compose_version plugin_dir plugin_path compose_url

  if ! command -v curl >/dev/null 2>&1; then
    if command -v dnf >/dev/null 2>&1; then
      run_as_root dnf install -y curl
    elif command -v yum >/dev/null 2>&1; then
      run_as_root yum install -y curl
    else
      echo "ERROR: curl is required to install Docker Compose fallback."
      exit 1
    fi
  fi

  platform="$(detect_compose_os_arch)"
  compose_version="$(curl -fsSL https://api.github.com/repos/docker/compose/releases/latest | sed -n 's/.*\"tag_name\": \"\\([^\"]*\\)\".*/\\1/p' | head -n1)"

  if [ -z "$compose_version" ]; then
    echo "ERROR: could not detect latest Docker Compose version from GitHub API."
    exit 1
  fi

  plugin_dir="/usr/libexec/docker/cli-plugins"
  plugin_path="${plugin_dir}/docker-compose"
  compose_url="https://github.com/docker/compose/releases/download/${compose_version}/docker-compose-${platform}"

  run_as_root mkdir -p "$plugin_dir"
  run_as_root curl -fSL "$compose_url" -o "$plugin_path"
  run_as_root chmod +x "$plugin_path"
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

if ! docker compose version >/dev/null 2>&1 && ! command -v docker-compose >/dev/null 2>&1; then
  install_compose_from_github
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
