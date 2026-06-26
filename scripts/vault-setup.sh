#!/usr/bin/env bash
set -euo pipefail

VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"
SECRETS_DIR=".vault-secrets"

echo "=== Vault Setup ==="
echo "Connecting to Vault at $VAULT_ADDR"
echo ""

export VAULT_ADDR

mkdir -p "$SECRETS_DIR"

INIT_OUTPUT=$(vault status 2>&1 || true)
if echo "$INIT_OUTPUT" | grep -qE "Initialized[[:space:]]+false"; then
  echo "Vault is not initialized. Initializing..."
  INIT_JSON=$(vault operator init -key-shares=1 -key-threshold=1 -format=json)

  UNSEAL_KEY=$(echo "$INIT_JSON" | grep -o '"unseal_keys_b64": \["[^"]*"\]' | cut -d'"' -f4)
  ROOT_TOKEN=$(echo "$INIT_JSON" | grep -o '"root_token": "[^"]*"' | cut -d'"' -f4)

  echo "$UNSEAL_KEY" > "$SECRETS_DIR/unseal-key"
  echo "$ROOT_TOKEN" > "$SECRETS_DIR/root-token"
  chmod 600 "$SECRETS_DIR/unseal-key" "$SECRETS_DIR/root-token"

  docker exec -e VAULT_ADDR=http://127.0.0.1:8200 "$(docker compose ps -q vault)" sh -c "echo '$UNSEAL_KEY' > /vault/file/.unseal-key" 2>/dev/null || true

  echo "  Unseal key saved to $SECRETS_DIR/unseal-key"
  echo "  Root token saved to $SECRETS_DIR/root-token"
  echo ""
  echo "  IMPORTANT: Back up $SECRETS_DIR/ to a safe location!"
  echo "  These files are gitignored."
else
  echo "Vault is already initialized."
fi

echo ""
echo "Unsealing Vault..."
UNSEAL_KEY=$(cat "$SECRETS_DIR/unseal-key")
vault operator unseal "$UNSEAL_KEY" || true

export VAULT_TOKEN=$(cat "$SECRETS_DIR/root-token")

echo ""
echo "Enabling KV secrets engine (v2)..."
vault secrets enable -path=secret kv-v2 2>/dev/null || echo "  Already enabled."

echo ""
echo "Creating CI/CD policy..."
cat <<'POLICY' | vault policy write ci-cd -
path "secret/data/waypoint/*" {
  capabilities = ["read"]
}
path "secret/metadata/waypoint/*" {
  capabilities = ["list", "read"]
}
POLICY

echo ""
echo "Creating CI/CD token..."
CI_TOKEN=$(vault token create -policy=ci-cd -period=24h -format=json | grep -o '"client_token": "[^"]*"' | cut -d'"' -f4)
echo "$CI_TOKEN" > "$SECRETS_DIR/ci-cd-token"
chmod 600 "$SECRETS_DIR/ci-cd-token"
echo "  CI/CD token saved to $SECRETS_DIR/ci-cd-token"
echo "  This is the ONE secret your CI/CD pipeline needs."

echo ""
echo "=== Setup Complete ==="
echo ""
echo "Next steps:"
echo "  1. Run: ./scripts/vault-populate.sh   (store your secrets in Vault)"
echo "  2. Run: ./scripts/load-secrets.sh     (verify secrets load correctly)"
echo "  3. Add VAULT_CI_CD_TOKEN to GitHub Actions secrets:"
echo "     echo -n '$CI_TOKEN' | pbcopy"
