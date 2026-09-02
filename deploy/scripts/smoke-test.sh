#!/usr/bin/env bash
# Post-deploy health check, run by the Jenkinsfile (locally or over SSH on
# the remote Docker host). Usage: smoke-test.sh [host] [port]
set -euo pipefail

HOST="${1:-localhost}"
PORT="${2:-8080}"
URL="http://${HOST}:${PORT}/health"

for attempt in 1 2 3 4 5; do
    if curl -sf "${URL}" >/dev/null; then
        echo "Smoke test passed: ${URL}"
        exit 0
    fi
    echo "Attempt ${attempt}/5: ${URL} not ready yet, retrying..."
    sleep 3
done

echo "Smoke test FAILED: ${URL} did not become healthy" >&2
exit 1
