#!/bin/sh
set -e

vault server -config=/vault/config.hcl &
VPID=$!

export VAULT_ADDR=http://127.0.0.1:8200

for i in $(seq 1 30); do
  if vault status >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if [ -f /vault/file/.unseal-key ]; then
  KEY=$(cat /vault/file/.unseal-key)
  vault operator unseal "$KEY" 2>/dev/null || true
fi

wait $VPID
