-- Búsqueda del panel interno sin distinguir tildes ni mayúsculas ("Peña" encuentra "pena" y al revés). La extensión
-- unaccent necesitaría superusuario; para el español basta con traducir las vocales acentuadas, la diéresis y la eñe.

CREATE FUNCTION unaccent_text(value text) RETURNS text
LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT
AS $$ SELECT translate(lower(value), 'áéíóúàèìòùäëïöüâêîôûñç', 'aeiouaeiouaeiouaeiounc') $$;
