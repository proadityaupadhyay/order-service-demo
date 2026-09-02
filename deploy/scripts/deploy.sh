#!/usr/bin/env bash
# Generic deploy engine, parameterized by ENV. Runs on the remote Docker
# host (invoked over SSH by the Jenkinsfile). Thin per-env wrappers in
# deploy/ci-cd/deploy/deploy-<env>.sh call this with ENV fixed.
#
# Required env vars: ENV, IMAGE
# Optional: PORT (default 8080), CONTAINER_NAME (default order-service-$ENV)
set -euo pipefail

: "${ENV:?ENV not set (dev|qa|uat|prod)}"
: "${IMAGE:?IMAGE not set, e.g. registry.example.com/order-service:dev-42}"

PORT="${PORT:-8080}"
CONTAINER_NAME="${CONTAINER_NAME:-order-service-${ENV}}"
CONFIG_FILE="$(dirname "$0")/../config/${ENV}/application.properties"

if [[ ! -f "${CONFIG_FILE}" ]]; then
    echo "Missing runtime config: ${CONFIG_FILE}" >&2
    exit 1
fi

echo "Deploying ${IMAGE} as ${CONTAINER_NAME} (env=${ENV}, port=${PORT})"

docker pull "${IMAGE}"

if docker ps -a --format '{{.Names}}' | grep -qx "${CONTAINER_NAME}"; then
    docker stop "${CONTAINER_NAME}" || true
    docker rm "${CONTAINER_NAME}" || true
fi

docker run -d \
    --name "${CONTAINER_NAME}" \
    --restart unless-stopped \
    --env-file "${CONFIG_FILE}" \
    -p "${PORT}:${PORT}" \
    "${IMAGE}"

echo "Deployed ${CONTAINER_NAME}"
