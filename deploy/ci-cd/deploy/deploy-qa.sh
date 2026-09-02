#!/usr/bin/env bash
set -euo pipefail
export ENV=qa
exec "$(dirname "$0")/../../scripts/deploy.sh"
