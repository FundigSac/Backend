-- Plataforma: correlativos de folio, idempotencia y registro de eventos entre módulos (outbox de Spring Modulith).
-- Convenciones: instantes en timestamptz (UTC), dinero en numeric, estados con CHECK, nada de borrado en cascada de
-- registros con valor legal. El rol de la aplicación (${appRole}) solo tiene DML; las migraciones las ejecuta el
-- rol propietario del esquema.

-- Quita privilegios al rol de la aplicación (si existe en este entorno) para tablas de solo inserción.
CREATE FUNCTION restrict_app_role(target regclass, privileges text) RETURNS void
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '${appRole}') THEN
        EXECUTE format('REVOKE %s ON %s FROM %I', privileges, target, '${appRole}');
    END IF;
END
$$;

-- El historial de migraciones solo lo escribe el propietario.
SELECT restrict_app_role('flyway_schema_history', 'INSERT, UPDATE, DELETE, TRUNCATE');

CREATE TABLE folio_sequences (
    prefix      char(3) NOT NULL CHECK (prefix ~ '^[A-Z]{3}$'),
    year        integer NOT NULL CHECK (year BETWEEN 2000 AND 9999),
    last_value  integer NOT NULL CHECK (last_value BETWEEN 1 AND 999999),
    PRIMARY KEY (prefix, year)
);
SELECT restrict_app_role('folio_sequences', 'DELETE, TRUNCATE');

CREATE TABLE idempotency_records (
    scope         varchar(40)  NOT NULL,
    idem_key      varchar(128) NOT NULL,
    fingerprint   char(64)     NOT NULL,
    resource_ref  varchar(80),
    created_at    timestamptz  NOT NULL,
    expires_at    timestamptz  NOT NULL,
    PRIMARY KEY (scope, idem_key)
);
CREATE INDEX idempotency_records_expires_idx ON idempotency_records (expires_at);

-- Esquema v2 del registro de publicaciones de eventos de Spring Modulith 2.1 (PostgreSQL).
CREATE TABLE event_publication (
    id                      uuid        NOT NULL,
    listener_id             text        NOT NULL,
    event_type              text        NOT NULL,
    serialized_event        text        NOT NULL,
    publication_date        timestamptz NOT NULL,
    completion_date         timestamptz,
    status                  text,
    completion_attempts     integer,
    last_resubmission_date  timestamptz,
    PRIMARY KEY (id)
);
CREATE INDEX event_publication_serialized_event_hash_idx ON event_publication USING hash (serialized_event);
CREATE INDEX event_publication_by_completion_date_idx ON event_publication (completion_date);
