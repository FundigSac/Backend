#!/bin/sh
# Se ejecuta una sola vez al crear el volumen de datos. Separa responsabilidades (menor privilegio):
#   fundigsac_migrator: dueño del esquema; solo lo usa Flyway para migrar.
#   fundigsac_app:      la aplicación; solo SELECT/INSERT/UPDATE/DELETE (sin DDL). Las migraciones le quitan además
#                       UPDATE/DELETE en tablas de evidencia (auditoría, historial, reclamaciones, textos legales).
set -eu

migrator_password="$(cat /run/secrets/db_migrator_password)"
app_password="$(cat /run/secrets/db_app_password)"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  -v dbname="$POSTGRES_DB" -v migrator_password="$migrator_password" -v app_password="$app_password" <<-'EOSQL'
	CREATE ROLE fundigsac_migrator LOGIN PASSWORD :'migrator_password' CONNECTION LIMIT 5;
	CREATE ROLE fundigsac_app LOGIN PASSWORD :'app_password' CONNECTION LIMIT 60;

	REVOKE ALL ON DATABASE :"dbname" FROM PUBLIC;
	GRANT CONNECT ON DATABASE :"dbname" TO fundigsac_migrator, fundigsac_app;
	REVOKE CREATE ON SCHEMA public FROM PUBLIC;

	CREATE SCHEMA fundigsac AUTHORIZATION fundigsac_migrator;
	GRANT USAGE ON SCHEMA fundigsac TO fundigsac_app;
	ALTER DEFAULT PRIVILEGES FOR ROLE fundigsac_migrator IN SCHEMA fundigsac
	    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO fundigsac_app;
	ALTER DEFAULT PRIVILEGES FOR ROLE fundigsac_migrator IN SCHEMA fundigsac
	    GRANT USAGE, SELECT ON SEQUENCES TO fundigsac_app;

	ALTER ROLE fundigsac_migrator SET search_path = fundigsac;
	ALTER ROLE fundigsac_app SET search_path = fundigsac;
	ALTER ROLE fundigsac_app SET statement_timeout = '15s';
	ALTER ROLE fundigsac_app SET idle_in_transaction_session_timeout = '60s';
	ALTER ROLE fundigsac_app SET lock_timeout = '5s';
EOSQL
