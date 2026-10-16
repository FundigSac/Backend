-- Solicitudes de cotización B2B (sin cuenta). Cada línea guarda una copia (snapshot) del producto al momento de la
-- solicitud: si el producto cambia o se archiva, la solicitud conserva lo que la persona pidió. Sin precios.

CREATE TABLE quote_requests (
    id                       uuid          PRIMARY KEY,
    folio                    varchar(16)   NOT NULL UNIQUE CHECK (folio ~ '^(COT|REQ)-[0-9]{4}-[0-9]{6}$'),
    origin                   varchar(16)   NOT NULL CHECK (origin IN ('GENERAL_FORM', 'PRODUCT_FORM', 'CART', 'PROCUREMENT')),
    status                   varchar(12)   NOT NULL
        CHECK (status IN ('RECEIVED', 'ASSIGNED', 'IN_REVIEW', 'RESPONDED', 'CLOSED', 'CANCELLED')),
    company_name             varchar(160)  NOT NULL,
    ruc                      char(11)      NOT NULL CHECK (ruc ~ '^[0-9]{11}$'),
    contact_name             varchar(120)  NOT NULL,
    contact_position         varchar(120),
    email                    varchar(254)  NOT NULL,
    phone                    varchar(16)   NOT NULL,
    project_type             varchar(120),
    delivery_department      varchar(60),
    delivery_place           varchar(200),
    requested_delivery_date  date,
    message                  text          CHECK (char_length(message) <= 4000),
    privacy_notice_version   varchar(20)   NOT NULL,
    customer_id              uuid,
    assignee_id              uuid,
    status_reason            varchar(500),
    submitted_at             timestamptz   NOT NULL,
    updated_at               timestamptz   NOT NULL,
    version                  bigint        NOT NULL DEFAULT 0
);
CREATE INDEX quote_requests_status_idx ON quote_requests (status, submitted_at DESC);
CREATE INDEX quote_requests_customer_idx ON quote_requests (customer_id, submitted_at DESC) WHERE customer_id IS NOT NULL;
CREATE INDEX quote_requests_assignee_idx ON quote_requests (assignee_id) WHERE assignee_id IS NOT NULL;
SELECT restrict_app_role('quote_requests', 'DELETE, TRUNCATE');

CREATE TABLE quote_items (
    quote_id       uuid           NOT NULL REFERENCES quote_requests (id),
    line_no        smallint       NOT NULL CHECK (line_no BETWEEN 1 AND 50),
    product_id     uuid,
    product_slug   varchar(120),
    product_name   varchar(200)   NOT NULL,
    sku            varchar(40),
    variant_label  varchar(120),
    quantity       numeric(12, 3) NOT NULL CHECK (quantity > 0 AND quantity <= 1000000),
    unit           varchar(10)    NOT NULL CHECK (unit IN ('UNIT', 'SET', 'LOT', 'METER', 'KILOGRAM')),
    addons         text[]         NOT NULL DEFAULT '{}',
    notes          varchar(1000),
    PRIMARY KEY (quote_id, line_no)
);
CREATE INDEX quote_items_product_idx ON quote_items (product_id) WHERE product_id IS NOT NULL;
-- Las líneas son inmutables una vez registradas.
SELECT restrict_app_role('quote_items', 'UPDATE, DELETE, TRUNCATE');

CREATE TABLE quote_status_events (
    id           uuid          PRIMARY KEY,
    quote_id     uuid          NOT NULL REFERENCES quote_requests (id),
    from_status  varchar(12),
    to_status    varchar(12)   NOT NULL,
    note         varchar(1000),
    actor_id     uuid,
    occurred_at  timestamptz   NOT NULL
);
CREATE INDEX quote_status_events_quote_idx ON quote_status_events (quote_id, occurred_at);
SELECT restrict_app_role('quote_status_events', 'UPDATE, DELETE, TRUNCATE');
