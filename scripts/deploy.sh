#!/usr/bin/env bash
set -euo pipefail

PROJECT="${COMPOSE_PROJECT_NAME:-waypoint}"

echo "=== Deploying Waypoint (${PROJECT}) ==="
echo ""

if [ "${RESTART_INFRA:-false}" = "true" ]; then
  echo "Infrastructure changed — full restart"
  docker compose down --remove-orphans
  docker compose up -d --build
elif [ "${BUILD_API:-false}" = "true" ] || [ "${BUILD_WEB:-false}" = "true" ]; then
  SERVICES=""
  [ "${BUILD_API}" = "true" ] && SERVICES="$SERVICES api"
  [ "${BUILD_WEB}" = "true" ] && SERVICES="$SERVICES web"
  echo "Selective restart for:${SERVICES}"
  docker compose up -d --build $SERVICES
else
  echo "No service changes — ensuring all services are running"
  docker compose up -d
fi

echo ""
echo "=== Deploy Complete ==="
docker compose ps
