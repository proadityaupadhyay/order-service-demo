#!/usr/bin/env bash
# Build and run order-service locally against the dev config, for
# developer use -- not invoked by Jenkins. Usage:
#   deploy/scripts/run-local.sh [env]   (env defaults to dev)
set -euo pipefail

ENV="${1:-dev}"
ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
CONFIG_FILE="${ROOT_DIR}/deploy/config/${ENV}/application.properties"
IMAGE="order-service:local-${ENV}"

if [[ ! -f "${CONFIG_FILE}" ]]; then
    echo "No config for env '${ENV}' at ${CONFIG_FILE}" >&2
    exit 1
fi

echo "Building ${IMAGE} (design-time env: ${ENV})..."
docker build \
    -f "${ROOT_DIR}/app/OrderService.application/docker/Dockerfile" \
    --build-arg DESIGN_TIME_ENV="${ENV}" \
    -t "${IMAGE}" \
    "${ROOT_DIR}/app"

docker rm -f order-service-local >/dev/null 2>&1 || true

echo "Running ${IMAGE} on http://localhost:8080 ..."
docker run -d \
    --name order-service-local \
    --env-file "${CONFIG_FILE}" \
    -p 8080:8080 \
    "${IMAGE}"

echo "Started. Try: curl http://localhost:8080/health"
echo "(Optional) start the downstream stub separately with:"
echo "  java ${ROOT_DIR}/app/mock-service/src/MockService.java"
