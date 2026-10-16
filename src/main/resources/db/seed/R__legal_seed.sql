-- Datos de demostración (solo perfiles local y test): versiones vigentes de los textos legales que hoy publica el sitio.
-- El texto completo vive en el sitio (content_url); al publicar por la API se guarda también el cuerpo y su SHA-256.
INSERT INTO legal_notices (id, notice_type, version, title, summary, content_url, effective_from, published_at)
VALUES
    ('6f7d0c1e-2a10-5c3b-9d1e-0a4b5c6d7e01', 'PRIVACY_POLICY', '2026-10', 'Política de privacidad',
     'Cómo FUNDIGSAC trata los datos personales (Ley N.° 29733 y D.S. N.° 016-2024-JUS) y cómo ejercer sus derechos.',
     '/privacidad', TIMESTAMPTZ '2026-10-01 05:00:00+00', TIMESTAMPTZ '2026-10-01 05:00:00+00'),
    ('6f7d0c1e-2a10-5c3b-9d1e-0a4b5c6d7e02', 'COOKIE_POLICY', '2026-10', 'Política de cookies',
     'Almacenamiento técnico necesario y contenido de terceros (mapa) solo con consentimiento.',
     '/cookies', TIMESTAMPTZ '2026-10-01 05:00:00+00', TIMESTAMPTZ '2026-10-01 05:00:00+00'),
    ('6f7d0c1e-2a10-5c3b-9d1e-0a4b5c6d7e03', 'TERMS_OF_USE', '2026-10', 'Términos y condiciones de uso',
     'Condiciones de uso del sitio web de FUNDIGSAC.',
     '/terminos', TIMESTAMPTZ '2026-10-01 05:00:00+00', TIMESTAMPTZ '2026-10-01 05:00:00+00'),
    ('6f7d0c1e-2a10-5c3b-9d1e-0a4b5c6d7e04', 'B2B_TERMS', '2026-10', 'Términos comerciales B2B',
     'Condiciones para cotizaciones y relaciones comerciales con empresas.',
     '/terminos-b2b', TIMESTAMPTZ '2026-10-01 05:00:00+00', TIMESTAMPTZ '2026-10-01 05:00:00+00'),
    ('6f7d0c1e-2a10-5c3b-9d1e-0a4b5c6d7e05', 'COMPLAINTS_BOOK', '2026-10', 'Libro de Reclamaciones',
     'Hoja de reclamación virtual: reclamo o queja con respuesta en el plazo legal.',
     '/reclamaciones', TIMESTAMPTZ '2026-10-01 05:00:00+00', TIMESTAMPTZ '2026-10-01 05:00:00+00')
ON CONFLICT (notice_type, version) DO NOTHING;
