-- Acceso del personal sin contraseña: código de 6 dígitos enviado al correo. Vence a los 10 minutos, sirve una sola
-- vez y admite 5 intentos. Solo se guarda el hash del código (SHA-256 con el id de la cuenta).

CREATE TABLE staff_login_codes (
    id             uuid        PRIMARY KEY,
    user_id        uuid        NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    code_hash      char(64)    NOT NULL,
    created_at     timestamptz NOT NULL,
    expires_at     timestamptz NOT NULL,
    attempts_left  integer     NOT NULL CHECK (attempts_left >= 0),
    used_at        timestamptz,
    CHECK (expires_at > created_at)
);
CREATE INDEX staff_login_codes_user_idx ON staff_login_codes (user_id, created_at DESC);
CREATE INDEX staff_login_codes_expires_idx ON staff_login_codes (expires_at);

-- El personal ya no necesita contraseña para estar activo: entra con el código. Los clientes por correo la siguen
-- necesitando. La restricción original no tenía nombre, así que se busca por su definición.
DO $$
DECLARE
    constraint_name text;
BEGIN
    SELECT c.conname INTO constraint_name
      FROM pg_constraint c
     WHERE c.conrelid = 'user_accounts'::regclass AND c.contype = 'c'
       AND pg_get_constraintdef(c.oid) LIKE '%password_hash IS NOT NULL%';
    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE user_accounts DROP CONSTRAINT %I', constraint_name);
    END IF;
END
$$;
ALTER TABLE user_accounts ADD CONSTRAINT user_accounts_active_credentials_ck
    CHECK (status <> 'ACTIVE' OR password_hash IS NOT NULL OR kind = 'STAFF');
