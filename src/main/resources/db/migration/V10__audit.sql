-- Auditoría de acciones relevantes (quién, qué, sobre qué recurso y cuándo). Solo inserción: la aplicación no puede
-- modificar ni borrar registros. No se guardan secretos, tokens ni datos personales innecesarios en los detalles.

CREATE TABLE audit_events (
    id             uuid         PRIMARY KEY,
    occurred_at    timestamptz  NOT NULL,
    recorded_at    timestamptz  NOT NULL,
    actor_id       uuid,
    action         varchar(60)  NOT NULL,
    resource_type  varchar(40)  NOT NULL,
    resource_id    varchar(80)  NOT NULL,
    details        jsonb        NOT NULL DEFAULT '{}' CHECK (jsonb_typeof(details) = 'object'),
    UNIQUE (action, resource_type, resource_id, occurred_at)
);
CREATE INDEX audit_events_resource_idx ON audit_events (resource_type, resource_id, occurred_at DESC);
CREATE INDEX audit_events_actor_idx ON audit_events (actor_id, occurred_at DESC) WHERE actor_id IS NOT NULL;
CREATE INDEX audit_events_occurred_idx ON audit_events (occurred_at DESC);
SELECT restrict_app_role('audit_events', 'UPDATE, DELETE, TRUNCATE');
