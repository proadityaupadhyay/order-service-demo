#!/usr/bin/env bash
set -euo pipefail
export ENV=qa
exec bash "$(dirname "$0")/../../scripts/deploy.sh"
