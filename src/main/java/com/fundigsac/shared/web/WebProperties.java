package com.fundigsac.shared.web;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración HTTP: orígenes CORS permitidos (lista cerrada, sin comodines) y tamaño máximo del cuerpo.
 *
 * @param allowedOrigins orígenes del frontend autorizados a llamar a la API desde el navegador
 * @param maxBodyBytes tamaño máximo del cuerpo de una petición
 */
@ConfigurationProperties("fundigsac.web")
public record WebProperties(@DefaultValue List<String> allowedOrigins, @DefaultValue("65536") long maxBodyBytes) {
}
