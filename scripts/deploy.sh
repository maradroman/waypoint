#!/usr/bin/env bash
set -euo pipefail

echo "=== Deploying Waypoint ==="
echo ""

echo "1/3  Starting Vault..."
docker compose up -d vault

echo "     Waiting for Vault to unseal..."
for i in $(seq 1 30); do
  if docker compose exec -T vault sh -c \
    'VAULT_ADDR=http://127.0.0.1:8200 vault status >/dev/null 2>&1' 2>/dev/null; then
    echo "     Vault is ready"
    break
  fi
  if [ "$i" -eq 30 ]; then
    echo "     ERROR: Vault did not become ready in 60s"
    echo "     If this is the first deploy, run:"
    echo "       ./scripts/vault-setup.sh && ./scripts/vault-populate.sh"
    exit 1
  fi
  sleep 2
done

echo "2/3  Loading secrets from Vault..."
./scripts/load-secrets.sh

echo "3/3  Building and starting all services..."
docker compose up -d --build

echo ""
echo "=== Deploy Complete ==="
docker compose ps
