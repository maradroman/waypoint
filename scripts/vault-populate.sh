#!/usr/bin/env bash
set -euo pipefail

VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"
SECRETS_DIR=".vault-secrets"

export VAULT_ADDR
export VAULT_TOKEN=$(cat "$SECRETS_DIR/root-token")

echo "=== Vault Populate Secrets ==="
echo "Storing secrets in Vault at $VAULT_ADDR"
echo "Press Enter to keep the [current] value, or type a new value."
echo ""

prompt() {
  local path="$1"
  local key="$2"
  local current="${3:-}"
  local label="$4"

  local current_display=""
  if [ -n "$current" ]; then
    current_display=" [$current]"
  fi

  read -rp "$label$current_display: " value
  if [ -z "$value" ] && [ -n "$current" ]; then
    value="$current"
  fi
  echo "$value"
}

echo "--- Database ---"
DB_PASSWORD=$(prompt "db" "password" "" "PostgreSQL password")
DB_USERNAME=$(prompt "db" "username" "waypoint" "PostgreSQL username")
DB_NAME=$(prompt "db" "name" "waypoint" "PostgreSQL database name")
vault kv put secret/waypoint/db \
  password="$DB_PASSWORD" \
  username="$DB_USERNAME" \
  name="$DB_NAME"
echo "  Stored: secret/waypoint/db"

echo ""
echo "--- JWT ---"
JWT_SECRET=$(prompt "jwt" "secret" "" "JWT signing secret (min 32 chars)")
vault kv put secret/waypoint/jwt secret="$JWT_SECRET"
echo "  Stored: secret/waypoint/jwt"

echo ""
echo "--- MinIO / Storage ---"
MINIO_ROOT_USER=$(prompt "storage" "root-user" "minioadmin" "MinIO root user")
MINIO_ROOT_PASSWORD=$(prompt "storage" "root-password" "" "MinIO root password (min 8 chars)")
STORAGE_BUCKET=$(prompt "storage" "bucket" "waypoint-bug-reports" "Storage bucket name")
vault kv put secret/waypoint/storage \
  access-key="$MINIO_ROOT_USER" \
  secret-key="$MINIO_ROOT_PASSWORD" \
  bucket="$STORAGE_BUCKET" \
  root-user="$MINIO_ROOT_USER" \
  root-password="$MINIO_ROOT_PASSWORD"
echo "  Stored: secret/waypoint/storage"

echo ""
echo "--- New Relic ---"
NR_LICENSE_KEY=$(prompt "newrelic" "license-key" "" "New Relic license key (leave empty to skip)")
NR_APP_NAME=$(prompt "newrelic" "app-name" "waypoint-api" "New Relic app name")
NR_ACCOUNT_ID=$(prompt "newrelic" "account-id" "" "NR account ID (frontend, leave empty to skip)")
NR_APP_ID=$(prompt "newrelic" "app-id" "" "NR browser app ID (frontend, leave empty to skip)")
NR_BROWSER_KEY=$(prompt "newrelic" "browser-license-key" "" "NR browser license key (frontend, leave empty to skip)")

vault kv put secret/waypoint/newrelic \
  license-key="$NR_LICENSE_KEY" \
  app-name="$NR_APP_NAME" \
  account-id="$NR_ACCOUNT_ID" \
  app-id="$NR_APP_ID" \
  browser-license-key="$NR_BROWSER_KEY"
echo "  Stored: secret/waypoint/newrelic"

echo ""
echo "--- Dozzle (logs UI) ---"
DOZZLE_USERNAME=$(prompt "dozzle" "username" "admin" "Dozzle username")
DOZZLE_PASSWORD=$(prompt "dozzle" "password" "" "Dozzle password")
vault kv put secret/waypoint/dozzle \
  username="$DOZZLE_USERNAME" \
  password="$DOZZLE_PASSWORD"
echo "  Stored: secret/waypoint/dozzle"

echo ""
echo "--- Cloudflare Tunnel ---"
TUNNEL_TOKEN=$(prompt "cloudflare" "tunnel-token" "" "Cloudflare tunnel token (leave empty to skip)")
vault kv put secret/waypoint/cloudflare tunnel-token="$TUNNEL_TOKEN"
echo "  Stored: secret/waypoint/cloudflare"

echo ""
echo "=== Populate Complete ==="
echo "Verify with: ./scripts/load-secrets.sh"
