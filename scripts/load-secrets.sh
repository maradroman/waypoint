#!/usr/bin/env bash
set -euo pipefail

VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"
OUTPUT="${1:-.env}"

export VAULT_ADDR

if [ -n "${VAULT_CI_CD_TOKEN:-}" ]; then
  export VAULT_TOKEN="$VAULT_CI_CD_TOKEN"
elif [ -f ".vault-secrets/ci-cd-token" ]; then
  export VAULT_TOKEN=$(cat .vault-secrets/ci-cd-token)
else
  echo "Error: No Vault token. Set VAULT_CI_CD_TOKEN or run vault-setup.sh first."
  exit 1
fi

field() {
  vault kv get -field="$2" "secret/waypoint/$1" 2>/dev/null || echo ""
}

{
  echo "# Generated from Vault — DO NOT COMMIT"
  echo ""

  echo "# Database"
  echo "SPRING_DATASOURCE_PASSWORD=$(field db password)"
  echo "SPRING_DATASOURCE_USERNAME=$(field db username)"
  echo "POSTGRES_DB=$(field db name)"
  echo ""

  echo "# JWT"
  echo "JWT_SECRET=$(field jwt secret)"
  echo ""

  echo "# MinIO / Storage"
  echo "MINIO_ROOT_USER=$(field storage root-user)"
  echo "MINIO_ROOT_PASSWORD=$(field storage root-password)"
  echo "APP_STORAGE_ACCESS_KEY=$(field storage access-key)"
  echo "APP_STORAGE_SECRET_KEY=$(field storage secret-key)"
  echo "APP_STORAGE_BUCKET=$(field storage bucket)"
  echo ""

  echo "# New Relic"
  echo "NEW_RELIC_LICENSE_KEY=$(field newrelic license-key)"
  echo "NEW_RELIC_APP_NAME=$(field newrelic app-name)"
  echo "VITE_NEW_RELIC_ACCOUNT_ID=$(field newrelic account-id)"
  echo "VITE_NEW_RELIC_APP_ID=$(field newrelic app-id)"
  echo "VITE_NEW_RELIC_LICENSE_KEY=$(field newrelic browser-license-key)"
  echo ""

  echo "# Dozzle"
  echo "DOZZLE_USERNAME=$(field dozzle username)"
  echo "DOZZLE_PASSWORD=$(field dozzle password)"
  echo ""

  echo "# Cloudflare"
  echo "TUNNEL_TOKEN=$(field cloudflare tunnel-token)"

} > "$OUTPUT"

echo "Secrets loaded → $OUTPUT"
