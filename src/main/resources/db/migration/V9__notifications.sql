-- Bandeja de salida de correos transaccionales (patrón outbox): se escribe en la misma transacción que reacciona al
-- evento y un despachador la envía con reintentos y espera exponencial. La clave de deduplicación evita correos
-- dobles si un evento se reprocesa. El cuerpo se borra al enviarse (puede contener enlaces con tokens).

CREATE TABLE email_outbox (
    id               uuid          PRIMARY KEY,
    dedupe_key       varchar(200)  NOT NULL UNIQUE,
    template         varchar(40)   NOT NULL,
    recipient        varchar(254)  NOT NULL,
    subject          varchar(200)  NOT NULL,
    body             text,
    status           varchar(10)   NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    attempts         integer       NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    next_attempt_at  timestamptz   NOT NULL,
    last_error       varchar(500),
    reference        varchar(40),
    created_at       timestamptz   NOT NULL,
    sent_at          timestamptz,
    CHECK (status <> 'PENDING' OR body IS NOT NULL)
);
CREATE INDEX email_outbox_pending_idx ON email_outbox (next_attempt_at) WHERE status = 'PENDING';
CREATE INDEX email_outbox_status_idx ON email_outbox (status, created_at DESC);
