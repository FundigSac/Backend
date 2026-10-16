package com.fundigsac.shared.infrastructure;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Secretos de la plataforma. Llegan como archivos de Docker secrets (/run/secrets) o variables de entorno; nunca se
 * escriben en el repositorio.
 *
 * @param receiptKey clave HMAC en Base64 (32 bytes o más) para tokens de constancia
 * @param allowEphemeralKeys solo desarrollo y pruebas: genera claves temporales si faltan
 */
@ConfigurationProperties("fundigsac.security")
public record SecretsProperties(@Nullable String receiptKey, boolean allowEphemeralKeys) {
}
