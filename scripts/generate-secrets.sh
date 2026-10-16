#!/bin/sh
# Genera los secretos locales en ./secrets (ignorado por Git). No sobrescribe los existentes.
# Requiere openssl (incluido en Git for Windows). Uso: sh scripts/generate-secrets.sh
set -eu

dir="$(cd "$(dirname "$0")/.." && pwd)/secrets"
mkdir -p "$dir"
# Carpeta privada para el usuario; los archivos quedan legibles para que los contenedores (otro UID) los lean.
chmod 700 "$dir"

# Sin \r: openssl en Windows termina las líneas con CRLF.
random() { openssl rand -base64 "$1" | tr -d '\r\n=' | tr '+/' '-_'; }

create() {
  if [ -s "$dir/$1" ]; then
    echo "  existe: $1"
  else
    printf '%s' "$2" > "$dir/$1"
    echo "  creado: $1"
  fi
}

echo "Secretos en $dir"
create postgres_superuser_password "$(random 32)"
create db_migrator_password "$(random 32)"
create db_app_password "$(random 32)"
create receipt_key "$(openssl rand -base64 48 | tr -d '\r\n')"
create bootstrap_admin_password "Fdg-$(random 18)"
if [ ! -s "$dir/jwt_private_key" ]; then
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out "$dir/jwt_private_key" 2>/dev/null
  echo "  creado: jwt_private_key (RSA 3072, PKCS#8)"
else
  echo "  existe: jwt_private_key"
fi

echo
echo "Administrador inicial: admin@fundigsac.local"
echo "Contraseña:            $(cat "$dir/bootstrap_admin_password")"
