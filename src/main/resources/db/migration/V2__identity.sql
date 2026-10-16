-- Identidad y acceso: cuentas de clientes y del personal, roles, sesiones (refresh tokens rotativos) y tokens de un
-- solo uso (verificación de correo, recuperación de contraseña, invitación). Solo se guardan hashes de los tokens.

CREATE TABLE user_accounts (
    id                     uuid         PRIMARY KEY,
    kind                   varchar(10)  NOT NULL CHECK (kind IN ('CUSTOMER', 'STAFF')),
    email                  varchar(254) NOT NULL CHECK (email = lower(email)),
    password_hash          varchar(255),
    full_name              varchar(120) NOT NULL,
    company_name           varchar(160),
    ruc                    char(11)     CHECK (ruc ~ '^[0-9]{11}$'),
    phone                  varchar(16),
    status                 varchar(24)  NOT NULL
        CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'DISABLED', 'DELETED')),
    email_verified_at      timestamptz,
    failed_login_attempts  integer      NOT NULL DEFAULT 0 CHECK (failed_login_attempts >= 0),
    locked_until           timestamptz,
    security_stamp         integer      NOT NULL DEFAULT 0,
    last_login_at          timestamptz,
    privacy_notice_version varchar(20),
    created_at             timestamptz  NOT NULL,
    updated_at             timestamptz  NOT NULL,
    deleted_at             timestamptz,
    version                bigint       NOT NULL DEFAULT 0,
    CHECK (status <> 'ACTIVE' OR password_hash IS NOT NULL),
    -- Un cliente siempre registra qué versión del aviso de privacidad aceptó (Ley 29733).
    CHECK (kind <> 'CUSTOMER' OR status = 'DELETED' OR privacy_notice_version IS NOT NULL)
);
-- Un correo por cuenta viva; las cuentas eliminadas quedan anonimizadas.
CREATE UNIQUE INDEX user_accounts_email_uq ON user_accounts (email) WHERE status <> 'DELETED';
CREATE INDEX user_accounts_kind_status_idx ON user_accounts (kind, status);

CREATE TABLE user_roles (
    user_id  uuid        NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    role     varchar(32) NOT NULL CHECK (role IN ('CUSTOMER', 'ADMINISTRATOR', 'SALES', 'CATALOG_EDITOR',
                                                  'CATALOG_REVIEWER', 'LEGAL_SUPPORT', 'READ_ONLY')),
    PRIMARY KEY (user_id, role)
);

CREATE TABLE refresh_tokens (
    id           uuid        PRIMARY KEY,
    user_id      uuid        NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    family_id    uuid        NOT NULL,
    token_hash   char(64)    NOT NULL UNIQUE,
    issued_at    timestamptz NOT NULL,
    expires_at   timestamptz NOT NULL,
    revoked_at   timestamptz,
    replaced_by  uuid,
    CHECK (expires_at > issued_at)
);
CREATE INDEX refresh_tokens_user_idx ON refresh_tokens (user_id);
CREATE INDEX refresh_tokens_family_idx ON refresh_tokens (family_id);
CREATE INDEX refresh_tokens_expires_idx ON refresh_tokens (expires_at);

CREATE TABLE one_time_tokens (
    id          uuid        PRIMARY KEY,
    user_id     uuid        NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    purpose     varchar(24) NOT NULL CHECK (purpose IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'STAFF_INVITATION')),
    token_hash  char(64)    NOT NULL UNIQUE,
    created_at  timestamptz NOT NULL,
    expires_at  timestamptz NOT NULL,
    used_at     timestamptz,
    CHECK (expires_at > created_at)
);
CREATE INDEX one_time_tokens_user_purpose_idx ON one_time_tokens (user_id, purpose);
