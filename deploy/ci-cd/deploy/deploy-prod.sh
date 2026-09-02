#!/usr/bin/env bash
set -euo pipefail
export ENV=prod
exec "$(dirname "$0")/../../scripts/deploy.sh"
