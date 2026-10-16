package com.fundigsac.shared.web;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Políticas de límite de frecuencia (token bucket por IP).
 *
 * @param enabled permite apagar los límites (nunca en producción)
 * @param policies capacidad y periodo de recarga por nombre de política
 */
@ConfigurationProperties("fundigsac.rate-limits")
public record RateLimitProperties(@DefaultValue("true") boolean enabled, @DefaultValue Map<String, Policy> policies) {

	/**
	 * @param capacity peticiones permitidas de golpe
	 * @param period tiempo en que se recargan por completo
	 */
	public record Policy(int capacity, Duration period) {
	}

}
