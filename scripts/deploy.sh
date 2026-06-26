#!/usr/bin/env bash
set -euo pipefail

PROJECT="${COMPOSE_PROJECT_NAME:-waypoint}"

echo "=== Deploying Waypoint (${PROJECT}) ==="
echo ""

echo "1/2  Building images..."
docker compose build

echo "2/2  Deploying..."
docker compose down --remove-orphans
docker compose up -d --build

echo ""
echo "=== Deploy Complete ==="
docker compose ps
