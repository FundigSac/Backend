-- Ingreso de clientes con Google. Cada cuenta registra cómo se creó (correo, Google o Facebook); las de Google guardan
-- su identificador estable (sub) y no necesitan contraseña porque Google ya verificó el correo.

ALTER TABLE user_accounts
    ADD COLUMN signup_method   varchar(10) NOT NULL DEFAULT 'EMAIL'
        CHECK (signup_method IN ('EMAIL', 'GOOGLE', 'FACEBOOK')),
    ADD COLUMN google_subject  varchar(255);

CREATE UNIQUE INDEX user_accounts_google_subject_uq ON user_accounts (google_subject)
    WHERE google_subject IS NOT NULL AND status <> 'DELETED';

ALTER TABLE user_accounts DROP CONSTRAINT user_accounts_active_credentials_ck;
ALTER TABLE user_accounts ADD CONSTRAINT user_accounts_active_credentials_ck
    CHECK (status <> 'ACTIVE' OR password_hash IS NOT NULL OR kind = 'STAFF' OR signup_method <> 'EMAIL');
