-- Privacidad (Ley N.° 29733 y D.S. N.° 016-2024-JUS): versiones de textos legales, evidencia de consentimiento de
-- cookies (sin IP ni identificadores de seguimiento) y solicitudes de derechos del titular (ARCO y eliminación).

CREATE TABLE legal_notices (
    id              uuid         PRIMARY KEY,
    notice_type     varchar(24)  NOT NULL CHECK (notice_type IN ('PRIVACY_POLICY', 'COOKIE_POLICY', 'TERMS_OF_USE',
                                                                 'B2B_TERMS', 'COMPLAINTS_BOOK')),
    version         varchar(20)  NOT NULL CHECK (version ~ '^[0-9A-Za-z._-]{1,20}$'),
    title           varchar(200) NOT NULL,
    summary         varchar(1000),
    content_url     varchar(300) NOT NULL,
    body            text         CHECK (char_length(body) <= 200000),
    content_sha256  char(64)     CHECK (content_sha256 ~ '^[0-9a-f]{64}$'),
    effective_from  timestamptz  NOT NULL,
    published_by    uuid,
    published_at    timestamptz  NOT NULL,
    UNIQUE (notice_type, version)
);
CREATE INDEX legal_notices_current_idx ON legal_notices (notice_type, effective_from DESC);
-- Una versión publicada es evidencia: no se edita ni se borra; se publica otra.
SELECT restrict_app_role('legal_notices', 'UPDATE, DELETE, TRUNCATE');

CREATE TABLE consent_records (
    id              uuid        PRIMARY KEY,
    consent_id      uuid        NOT NULL,
    choices         jsonb       NOT NULL CHECK (jsonb_typeof(choices) = 'object'),
    policy_version  varchar(20) NOT NULL,
    source          varchar(16) NOT NULL CHECK (source IN ('BANNER', 'PREFERENCES', 'POLICY', 'EMBED')),
    recorded_at     timestamptz NOT NULL
);
CREATE INDEX consent_records_consent_idx ON consent_records (consent_id, recorded_at DESC);
SELECT restrict_app_role('consent_records', 'UPDATE, DELETE, TRUNCATE');

CREATE TABLE data_subject_requests (
    id               uuid          PRIMARY KEY,
    folio            varchar(16)   NOT NULL UNIQUE CHECK (folio ~ '^ARC-[0-9]{4}-[0-9]{6}$'),
    request_type     varchar(20)   NOT NULL CHECK (request_type IN ('ACCESS', 'RECTIFICATION', 'CANCELLATION',
                                                                    'OPPOSITION', 'ACCOUNT_DELETION')),
    full_name        varchar(160)  NOT NULL,
    email            varchar(254)  NOT NULL,
    document_type    varchar(10)   CHECK (document_type IN ('DNI', 'CE', 'PASSPORT', 'RUC')),
    document_number  varchar(12),
    details          text          NOT NULL CHECK (char_length(details) BETWEEN 10 AND 4000),
    status           varchar(12)   NOT NULL CHECK (status IN ('RECEIVED', 'IN_PROGRESS', 'RESOLVED', 'REJECTED')),
    resolution_note  text          CHECK (char_length(resolution_note) <= 4000),
    received_at      timestamptz   NOT NULL,
    resolved_at      timestamptz,
    resolved_by      uuid,
    updated_at       timestamptz   NOT NULL,
    version          bigint        NOT NULL DEFAULT 0,
    CHECK ((document_type IS NULL) = (document_number IS NULL)),
    CHECK (status NOT IN ('RESOLVED', 'REJECTED') OR (resolution_note IS NOT NULL AND resolved_at IS NOT NULL))
);
CREATE INDEX data_subject_requests_status_idx ON data_subject_requests (status, received_at);
SELECT restrict_app_role('data_subject_requests', 'DELETE, TRUNCATE');
