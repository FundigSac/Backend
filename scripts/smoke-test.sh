#!/bin/sh
# Prueba de humo contra el backend en marcha (docker compose up -d). Recorre los flujos principales y muestra el
# código HTTP de cada paso. Uso: sh scripts/smoke-test.sh [http://localhost:8080]
# Los JSON van por stdin (--data-binary @-) para conservar UTF-8 también en Windows.
set -u

B="${1:-http://localhost:8080}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
J="Content-Type: application/json"
FAILED=0

check() { # nombre esperado obtenido
  if [ "$2" = "$3" ]; then printf '  OK   %-58s %s\n' "$1" "$3"; else printf '  FAIL %-58s esperado %s, obtenido %s\n' "$1" "$2" "$3"; FAILED=1; fi
}
field() { sed -n "s/.*\"$1\":\"\([^\"]*\)\".*/\1/p" | head -1; }
code() { curl -s -o /tmp/fundigsac-smoke.json -w '%{http_code}' "$@"; }

echo "Catálogo público"
check "GET categorías" 200 "$(code "$B/api/v1/catalog/categories")"
check "GET búsqueda q=válvulas&dn=150" 200 "$(code "$B/api/v1/catalog/products?q=v%C3%A1lvulas&dn=150")"
check "GET ficha vcp-f4-01" 200 "$(code "$B/api/v1/catalog/products/vcp-f4-01")"
check "GET facetas" 200 "$(code "$B/api/v1/catalog/filters?category=valvulas")"
check "GET filtro inválido (joint=soldada)" 400 "$(code "$B/api/v1/catalog/products?joint=soldada")"
check "GET documento descargable (redirección)" 302 "$(code "$B/api/v1/resources/ficha-tecnica-valvula-compuerta-fundigsac/download")"
check "GET documento bajo solicitud" 409 "$(code "$B/api/v1/resources/catalogo-general-2026/download")"
check "GET texto legal vigente" 200 "$(code "$B/api/v1/legal/privacy-policy")"

echo "Cotización (pública, idempotente)"
QUOTE='{"origin":"PRODUCT_FORM","contact":{"companyName":"Constructora Andina SAC","ruc":"20131312955","contactName":"Luis Torres","email":"compras@andina.pe","phone":"+51 987 654 321"},"project":{"projectType":"Saneamiento","deliveryDepartment":"Lima","message":"Obra de agua potable en Ate."},"items":[{"productSlug":"acople-gran-rango-agr","variantLabel":"DN100 · 105–125 mm","quantity":12,"unit":"und","addons":["Kit de pernos"]}],"privacyNoticeVersion":"2026-10","acceptPrivacyNotice":true}'
KEY="smoke-$(date +%s)"
check "POST cotización" 201 "$(printf '%s' "$QUOTE" | code -X POST "$B/api/v1/quotes" -H "$J" -H "Idempotency-Key: $KEY" --data-binary @-)"
FOLIO=$(field folio < /tmp/fundigsac-smoke.json); TOKEN=$(field receiptToken < /tmp/fundigsac-smoke.json)
printf '%s' "$QUOTE" | code -X POST "$B/api/v1/quotes" -H "$J" -H "Idempotency-Key: $KEY" --data-binary @- > /dev/null
check "Reintento con la misma Idempotency-Key = mismo folio" "$FOLIO" "$(field folio < /tmp/fundigsac-smoke.json)"
check "GET constancia con token" 200 "$(code "$B/api/v1/quotes/$FOLIO/receipt" -H "X-Receipt-Token: $TOKEN")"
check "GET constancia sin token" 404 "$(code "$B/api/v1/quotes/$FOLIO/receipt")"

echo "Libro de Reclamaciones"
CLAIM='{"kind":"CLAIM","consumerName":"Ana Pérez Rojas","documentType":"DNI","documentNumber":"45678912","email":"ana.perez@correo.pe","phone":"987654321","address":"Av. Los Próceres 456, Lima","minor":false,"goodType":"PRODUCT","goodDescription":"Válvula de compuerta DN150","claimedAmount":1500.50,"currency":"PEN","incidentDate":"2026-10-14","facts":"La válvula llegó con la brida dañada y no cierra herméticamente.","consumerRequest":"Cambio de la válvula por una nueva.","responseChannel":"EMAIL","privacyNoticeVersion":"2026-10","acceptPrivacyNotice":true}'
check "POST hoja de reclamación" 201 "$(printf '%s' "$CLAIM" | code -X POST "$B/api/v1/complaints" -H "$J" --data-binary @-)"
REC=$(field folio < /tmp/fundigsac-smoke.json)

echo "Contacto y privacidad"
INQUIRY='{"reason":"DOCUMENTATION","fullName":"María Quispe","companyName":"Agua Limpia SAC","email":"maria@agualimpia.pe","phone":"987654321","message":"Solicito la ficha técnica de la válvula DN200.","privacyNoticeVersion":"2026-10","acceptPrivacyNotice":true}'
check "POST consulta" 201 "$(printf '%s' "$INQUIRY" | code -X POST "$B/api/v1/inquiries" -H "$J" --data-binary @-)"
check "POST consentimiento de cookies" 201 "$(printf '%s' '{"choices":{"necessary":true,"thirdParty":false},"policyVersion":"2026-10","source":"BANNER"}' | code -X POST "$B/api/v1/privacy/consents" -H "$J" --data-binary @-)"
check "POST campo no permitido (asignación masiva)" 400 "$(printf '%s' '{"choices":{"necessary":true,"thirdParty":false},"policyVersion":"2026-10","source":"BANNER","isAdmin":true}' | code -X POST "$B/api/v1/privacy/consents" -H "$J" --data-binary @-)"

echo "Personal (administrador inicial)"
PW="$(tr -d '\r\n' < "$ROOT/secrets/bootstrap_admin_password")"
check "POST login administrador" 200 "$(printf '{"email":"admin@fundigsac.local","password":"%s"}' "$PW" | code -X POST "$B/api/v1/auth/login" -H "$J" --data-binary @-)"
AT=$(field accessToken < /tmp/fundigsac-smoke.json)
check "GET bandeja de cotizaciones sin token" 401 "$(code "$B/api/v1/staff/quotes")"
check "GET bandeja de cotizaciones" 200 "$(code "$B/api/v1/staff/quotes?q=$FOLIO" -H "Authorization: Bearer $AT")"
QID=$(field id < /tmp/fundigsac-smoke.json)
check "POST cambio de estado sin If-Match" 428 "$(printf '%s' '{"status":"IN_REVIEW"}' | code -X POST "$B/api/v1/staff/quotes/$QID/status" -H "$J" -H "Authorization: Bearer $AT" --data-binary @-)"
check "POST cambio de estado con If-Match" 200 "$(printf '%s' '{"status":"IN_REVIEW","note":"Revisando stock"}' | code -X POST "$B/api/v1/staff/quotes/$QID/status" -H "$J" -H 'If-Match: "0"' -H "Authorization: Bearer $AT" --data-binary @-)"
check "GET bandeja legal" 200 "$(code "$B/api/v1/staff/complaints?q=$REC" -H "Authorization: Bearer $AT")"
check "GET auditoría" 200 "$(code "$B/api/v1/staff/audit?resourceType=QUOTE" -H "Authorization: Bearer $AT")"
check "GET correos (bandeja de salida)" 200 "$(code "$B/api/v1/staff/notifications" -H "Authorization: Bearer $AT")"

echo
[ "$FAILED" = 0 ] && echo "Todo correcto. Folios: $FOLIO, $REC" || { echo "Hubo fallos."; exit 1; }
