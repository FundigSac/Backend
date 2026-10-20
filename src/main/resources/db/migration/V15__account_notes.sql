-- Notas internas del personal sobre una cuenta (el cliente no las ve). Se borran al anonimizar la cuenta porque
-- pueden contener datos personales.

CREATE TABLE account_notes (
    user_id     uuid          PRIMARY KEY REFERENCES user_accounts (id) ON DELETE CASCADE,
    notes       varchar(2000) NOT NULL,
    updated_at  timestamptz   NOT NULL,
    updated_by  uuid          NOT NULL
);
