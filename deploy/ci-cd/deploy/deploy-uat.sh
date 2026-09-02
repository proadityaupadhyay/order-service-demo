#!/usr/bin/env bash
set -euo pipefail
export ENV=uat
exec "$(dirname "$0")/../../scripts/deploy.sh"
