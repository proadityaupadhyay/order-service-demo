#!/usr/bin/env bash
# Thin per-env wrapper called by the Jenkinsfile over SSH on the remote
# Docker host. Expects IMAGE (and optionally PORT/CONTAINER_NAME) already
# exported by the caller.
set -euo pipefail
export ENV=dev
exec "$(dirname "$0")/../../scripts/deploy.sh"
