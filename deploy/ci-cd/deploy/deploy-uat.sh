#!/usr/bin/env bash
set -euo pipefail
export ENV=uat
exec bash "$(dirname "$0")/../../scripts/deploy.sh"
