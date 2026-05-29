#!/usr/bin/env bash
# check-script-modes.sh
# Purpose: Ensures that all shell scripts are executable and Windows scripts are not.
# Usage: ./check-script-modes.sh
# Tools: bash, find, cd, dirname, pwd
# Output: Success or error messages per file, and a final status message.
# Exit behavior: Exits with 0 if all checks pass, 1 otherwise.

set -euo pipefail

# Configuration: Set project root relative to script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

failed=0

# Check if all .sh files have the executable bit set
while IFS= read -r -d '' file; do
  if [ ! -x "$file" ]; then
    echo "ERROR: shell script is not executable: $file"
    failed=1
  fi
done < <(find . -type f -name "*.sh" -print0)

# Check if Windows scripts (.bat, .ps1) mistakenly have the executable bit set
while IFS= read -r -d '' file; do
  if [ -x "$file" ]; then
    echo "ERROR: windows script should not be executable on unix: $file"
    failed=1
  fi
done < <(find . -type f \( -name "*.bat" -o -name "*.ps1" \) -print0)

# Final validation result
if [ "$failed" -ne 0 ]; then
  echo "Script mode check failed."
  exit 1
fi

echo "Script mode check passed."
