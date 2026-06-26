#!/usr/bin/env bash
set -euo pipefail

VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"
SECRETS_DIR=".vault-secrets"

export VAULT_ADDR

if [ ! -f "$SECRETS_DIR/root-token" ]; then
  echo "Error: $SECRETS_DIR/root-token not found."
  echo "       Run ./scripts/vault-setup.sh first."
  exit 1
fi
export VAULT_TOKEN=$(cat "$SECRETS_DIR/root-token")

if vault kv get secret/waypoint/db >/dev/null 2>&1 && [ "${1:-}" != "--force" ]; then
  echo "Vault already populated. Use --force to overwrite with defaults."
  exit 0
fi

echo "=== Populating Vault with default dev values ==="

vault kv put secret/waypoint/db \
  password="waypoint-dev-pw" \
  username="waypoint" \
  name="waypoint"

vault kv put secret/waypoint/jwt \
  secret="waypoint-dev-jwt-secret-please-change-in-production-32chars"

vault kv put secret/waypoint/storage \
  access-key="minioadmin" \
  secret-key="minioadmin" \
  bucket="waypoint-bug-reports" \
  root-user="minioadmin" \
  root-password="minioadmin"

vault kv put secret/waypoint/newrelic \
  license-key="" \
  app-name="waypoint-api" \
  account-id="" \
  app-id="" \
  browser-license-key=""

vault kv put secret/waypoint/dozzle \
  username="admin" \
  password="admin"

vault kv put secret/waypoint/cloudflare \
  tunnel-token=""

echo ""
echo "=== Defaults populated ==="
echo "Replace these via the Vault UI before going to production."
