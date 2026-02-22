#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

failed=0

while IFS= read -r -d '' file; do
  if [ ! -x "$file" ]; then
    echo "ERROR: shell script is not executable: $file"
    failed=1
  fi
done < <(find . -type f -name "*.sh" -print0)

while IFS= read -r -d '' file; do
  if [ -x "$file" ]; then
    echo "ERROR: windows script should not be executable on unix: $file"
    failed=1
  fi
done < <(find . -type f \( -name "*.bat" -o -name "*.ps1" \) -print0)

if [ "$failed" -ne 0 ]; then
  echo "Script mode check failed."
  exit 1
fi

echo "Script mode check passed."
