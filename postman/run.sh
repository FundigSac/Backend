#!/bin/sh
# Ejecuta la colección de Postman contra el entorno local (docker compose up -d). No publica nada en la nube de Postman.
# Uso: sh postman/run.sh [opciones adicionales de "postman collection run"]
# La contraseña del administrador se toma de secrets/bootstrap_admin_password y no se guarda en la colección.
set -eu

DIR="$(cd "$(dirname "$0")" && pwd)"
SECRET="$DIR/../secrets/bootstrap_admin_password"

if [ ! -s "$SECRET" ]; then
  echo "Falta $SECRET. Ejecute primero: sh scripts/generate-secrets.sh" >&2
  exit 1
fi

exec postman collection run "$DIR/fundigsac-backend.postman_collection.json" \
  --environment "$DIR/local.postman_environment.json" \
  --env-var "adminPassword=$(tr -d '\r\n' < "$SECRET")" \
  --timeout-request 30000 \
  "$@"
