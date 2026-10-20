-- Desde qué página del sitio se creó la cuenta y si aceptó el boletín. Se guarda la fecha del consentimiento como
-- constancia (Ley 29733); al eliminar la cuenta se borra.

ALTER TABLE user_accounts
    ADD COLUMN signup_source         varchar(12)
        CHECK (signup_source IN ('registro', 'carrito', 'cotizar', 'login')),
    ADD COLUMN newsletter_opt_in_at  timestamptz;

CREATE INDEX user_accounts_created_idx ON user_accounts (created_at DESC);
