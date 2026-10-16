-- Consultas generales del formulario de contacto (separadas de cotizaciones y reclamaciones: otra finalidad).

CREATE TABLE inquiries (
    id                      uuid         PRIMARY KEY,
    folio                   varchar(16)  NOT NULL UNIQUE CHECK (folio ~ '^CON-[0-9]{4}-[0-9]{6}$'),
    reason                  varchar(20)  NOT NULL
        CHECK (reason IN ('GENERAL', 'TECHNICAL', 'COMMERCIAL', 'DOCUMENTATION', 'MEETING', 'AFTER_SALES', 'OTHER')),
    full_name               varchar(120) NOT NULL,
    company_name            varchar(160) NOT NULL,
    ruc                     char(11)     CHECK (ruc ~ '^[0-9]{11}$'),
    position                varchar(120),
    email                   varchar(254) NOT NULL,
    phone                   varchar(16)  NOT NULL,
    message                 text         NOT NULL CHECK (char_length(message) BETWEEN 10 AND 4000),
    requested_document      varchar(200),
    privacy_notice_version  varchar(20)  NOT NULL,
    status                  varchar(12)  NOT NULL CHECK (status IN ('RECEIVED', 'IN_PROGRESS', 'ANSWERED', 'CLOSED')),
    assignee_id             uuid,
    received_at             timestamptz  NOT NULL,
    updated_at              timestamptz  NOT NULL,
    version                 bigint       NOT NULL DEFAULT 0
);
CREATE INDEX inquiries_status_received_idx ON inquiries (status, received_at DESC);
SELECT restrict_app_role('inquiries', 'DELETE, TRUNCATE');

CREATE TABLE inquiry_events (
    id           uuid          PRIMARY KEY,
    inquiry_id   uuid          NOT NULL REFERENCES inquiries (id),
    from_status  varchar(12),
    to_status    varchar(12)   NOT NULL,
    note         varchar(1000),
    actor_id     uuid,
    occurred_at  timestamptz   NOT NULL
);
CREATE INDEX inquiry_events_inquiry_idx ON inquiry_events (inquiry_id, occurred_at);
SELECT restrict_app_role('inquiry_events', 'UPDATE, DELETE, TRUNCATE');
