-- Libro de Reclamaciones (Código de Protección y Defensa del Consumidor, Ley N.° 29571, y su reglamento del Libro de
-- Reclamaciones). Cada hoja tiene número correlativo, es inmutable en lo que presentó el consumidor y conserva su
-- historial; la base de datos impide borrarla o reescribirla aunque falle la aplicación.

CREATE TABLE complaints (
    id                        uuid           PRIMARY KEY,
    folio                     varchar(16)    NOT NULL UNIQUE CHECK (folio ~ '^REC-[0-9]{4}-[0-9]{6}$'),
    kind                      varchar(10)    NOT NULL CHECK (kind IN ('CLAIM', 'GRIEVANCE')),
    consumer_name             varchar(160)   NOT NULL,
    document_type             varchar(10)    NOT NULL CHECK (document_type IN ('DNI', 'CE', 'PASSPORT', 'RUC')),
    document_number           varchar(12)    NOT NULL,
    email                     varchar(254)   NOT NULL,
    phone                     varchar(16)    NOT NULL,
    address                   varchar(300)   NOT NULL,
    is_minor                  boolean        NOT NULL,
    guardian_name             varchar(160),
    guardian_document_type    varchar(10)    CHECK (guardian_document_type IN ('DNI', 'CE', 'PASSPORT')),
    guardian_document_number  varchar(12),
    good_type                 varchar(10)    CHECK (good_type IN ('PRODUCT', 'SERVICE')),
    good_description          varchar(1000)  NOT NULL,
    claimed_amount            numeric(12, 2) CHECK (claimed_amount >= 0),
    claimed_currency          char(3)        CHECK (claimed_currency IN ('PEN', 'USD')),
    service_location          varchar(200),
    incident_date             date           NOT NULL,
    related_reference         varchar(60),
    facts                     text           NOT NULL CHECK (char_length(facts) BETWEEN 20 AND 4000),
    consumer_request          text           NOT NULL CHECK (char_length(consumer_request) BETWEEN 5 AND 2000),
    response_channel          varchar(16)    NOT NULL CHECK (response_channel IN ('EMAIL', 'POSTAL_ADDRESS')),
    privacy_notice_version    varchar(20)    NOT NULL,
    status                    varchar(12)    NOT NULL CHECK (status IN ('FILED', 'IN_REVIEW', 'ANSWERED', 'CLOSED')),
    filed_at                  timestamptz    NOT NULL,
    response_due_on           date           NOT NULL,
    assignee_id               uuid,
    response_text             text           CHECK (char_length(response_text) <= 8000),
    responded_at              timestamptz,
    responded_by              uuid,
    closed_at                 timestamptz,
    updated_at                timestamptz    NOT NULL,
    version                   bigint         NOT NULL DEFAULT 0,
    CHECK (NOT is_minor OR (guardian_name IS NOT NULL AND guardian_document_number IS NOT NULL)),
    CHECK ((claimed_amount IS NULL) = (claimed_currency IS NULL)),
    CHECK (kind <> 'CLAIM' OR good_type IS NOT NULL),
    CHECK (response_due_on >= (filed_at AT TIME ZONE 'America/Lima')::date),
    CHECK (status NOT IN ('ANSWERED', 'CLOSED') OR (response_text IS NOT NULL AND responded_at IS NOT NULL))
);
CREATE INDEX complaints_status_due_idx ON complaints (status, response_due_on);
SELECT restrict_app_role('complaints', 'DELETE, TRUNCATE');

-- Lo presentado por el consumidor no se puede reescribir: solo cambian estado, responsable, respuesta y cierre.
CREATE FUNCTION complaints_protect_filing() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF ROW(NEW.id, NEW.folio, NEW.kind, NEW.consumer_name, NEW.document_type, NEW.document_number, NEW.email, NEW.phone,
           NEW.address, NEW.is_minor, NEW.guardian_name, NEW.guardian_document_type, NEW.guardian_document_number,
           NEW.good_type, NEW.good_description, NEW.claimed_amount, NEW.claimed_currency, NEW.service_location,
           NEW.incident_date, NEW.related_reference, NEW.facts, NEW.consumer_request, NEW.response_channel,
           NEW.privacy_notice_version, NEW.filed_at, NEW.response_due_on)
       IS DISTINCT FROM
       ROW(OLD.id, OLD.folio, OLD.kind, OLD.consumer_name, OLD.document_type, OLD.document_number, OLD.email, OLD.phone,
           OLD.address, OLD.is_minor, OLD.guardian_name, OLD.guardian_document_type, OLD.guardian_document_number,
           OLD.good_type, OLD.good_description, OLD.claimed_amount, OLD.claimed_currency, OLD.service_location,
           OLD.incident_date, OLD.related_reference, OLD.facts, OLD.consumer_request, OLD.response_channel,
           OLD.privacy_notice_version, OLD.filed_at, OLD.response_due_on) THEN
        RAISE EXCEPTION 'La hoja de reclamación % no se puede modificar', OLD.folio USING ERRCODE = 'integrity_constraint_violation';
    END IF;
    IF OLD.response_text IS NOT NULL AND NEW.response_text IS DISTINCT FROM OLD.response_text THEN
        RAISE EXCEPTION 'La respuesta de la hoja % ya fue emitida', OLD.folio USING ERRCODE = 'integrity_constraint_violation';
    END IF;
    RETURN NEW;
END
$$;
CREATE TRIGGER complaints_protect_filing BEFORE UPDATE ON complaints
    FOR EACH ROW EXECUTE FUNCTION complaints_protect_filing();

CREATE TABLE complaint_events (
    id           uuid          PRIMARY KEY,
    complaint_id uuid          NOT NULL REFERENCES complaints (id),
    event_type   varchar(20)   NOT NULL CHECK (event_type IN ('FILED', 'ASSIGNED', 'IN_REVIEW', 'ANSWERED', 'CLOSED', 'NOTE')),
    note         varchar(1000),
    actor_id     uuid,
    occurred_at  timestamptz   NOT NULL
);
CREATE INDEX complaint_events_complaint_idx ON complaint_events (complaint_id, occurred_at);
SELECT restrict_app_role('complaint_events', 'UPDATE, DELETE, TRUNCATE');
