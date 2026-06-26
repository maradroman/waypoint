#!/usr/bin/env bash
set -euo pipefail

echo "=== Deploying Waypoint ==="
echo ""

export VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"

if ! command -v vault >/dev/null 2>&1; then
  echo "ERROR: 'vault' CLI not found on PATH."
  echo "       Install it: https://developer.hashicorp.com/vault/install"
  exit 1
fi

echo "1/3  Starting Vault..."
docker compose up -d vault

echo "     Waiting for Vault to become ready..."
READY=false
for i in $(seq 1 30); do
  if vault status >/dev/null 2>&1; then
    READY=true
    break
  fi
  STATUS=$(vault status 2>&1 || true)
  if echo "$STATUS" | grep -qE "Initialized[[:space:]]+false"; then
    echo "     Vault is not initialized. Auto-bootstrapping..."
    ./scripts/vault-setup.sh
    ./scripts/vault-populate-defaults.sh
    for j in $(seq 1 30); do
      if vault status >/dev/null 2>&1; then
        READY=true
        break
      fi
      sleep 2
    done
    break
  fi
  sleep 2
done

if [ "$READY" != "true" ]; then
  echo "     ERROR: Vault did not become ready"
  echo "     If bootstrap failed, check the logs above or run manually:"
  echo "       ./scripts/vault-setup.sh && ./scripts/vault-populate.sh"
  exit 1
fi
echo "     Vault is ready"

echo "2/3  Loading secrets from Vault..."
./scripts/load-secrets.sh

echo "3/3  Building and starting all services..."
docker compose up -d --build

echo ""
echo "=== Deploy Complete ==="
docker compose ps
