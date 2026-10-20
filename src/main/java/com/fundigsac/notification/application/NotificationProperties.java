package com.fundigsac.notification.application;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración de correos transaccionales.
 *
 * @param delivery smtp (envío real) o log (desarrollo y pruebas: solo registra que se enviaría)
 * @param from remitente
 * @param siteUrl sitio web para los enlaces de los correos (verificación y recuperación)
 * @param adminUrl panel interno del personal (gestor de usuarios), al que llevan las invitaciones
 * @param salesInbox buzón de Ventas (nuevas cotizaciones y consultas)
 * @param legalInbox buzón de Atención Legal (Libro de Reclamaciones)
 * @param privacyInbox buzón de privacidad (derechos del titular)
 * @param maxAttempts intentos antes de marcar el correo como fallido
 * @param retryBase espera inicial entre intentos (se duplica en cada fallo)
 * @param batchSize correos por ciclo del despachador
 */
@ConfigurationProperties("fundigsac.notifications")
public record NotificationProperties(@DefaultValue("log") String delivery,
		@DefaultValue("FUNDIGSAC <no-responder@fundigsac.local>") String from,
		@DefaultValue("http://localhost:3000") String siteUrl, @DefaultValue("http://localhost:3110") String adminUrl,
		@DefaultValue("ventas@fundigsac.local") String salesInbox,
		@DefaultValue("legal@fundigsac.local") String legalInbox,
		@DefaultValue("privacidad@fundigsac.local") String privacyInbox, @DefaultValue("8") int maxAttempts,
		@DefaultValue("PT30S") Duration retryBase, @DefaultValue("20") int batchSize) {
}
