package com.fundigsac.identity.application;

import java.time.Duration;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración de identidad: emisión de JWT, duración de sesiones y tokens, bloqueo por intentos y administrador
 * inicial. Los secretos (clave privada, contraseña inicial) llegan por Docker secrets.
 */
@ConfigurationProperties("fundigsac.identity")
public record IdentityProperties(@DefaultValue Jwt jwt, @DefaultValue Sessions sessions, @DefaultValue Tokens tokens,
		@DefaultValue Login login, @DefaultValue BootstrapAdmin bootstrapAdmin, @DefaultValue Google google) {

	/**
	 * @param issuer emisor (claim iss)
	 * @param audience audiencia (claim aud) que la API exige
	 * @param accessTokenTtl vida del token de acceso
	 * @param privateKey clave RSA privada PKCS#8 en PEM (2048 bits o más)
	 * @param revocationCacheTtl cuánto se recuerda el sello de seguridad de una cuenta al validar un JWT (retraso
	 * máximo con que una desactivación o cambio de rol invalida tokens ya emitidos)
	 */
	public record Jwt(@DefaultValue("https://api.fundigsac.com") String issuer,
			@DefaultValue("fundigsac-api") String audience, @DefaultValue("PT15M") Duration accessTokenTtl,
			@Nullable String privateKey, @DefaultValue("PT10S") Duration revocationCacheTtl) {
	}

	/** El personal tiene sesiones más cortas: sus permisos son más sensibles. */
	public record Sessions(@DefaultValue("P14D") Duration customerRefreshTtl,
			@DefaultValue("PT12H") Duration staffRefreshTtl) {
	}

	/**
	 * @param staffLoginCodeTtl vida del código de acceso del personal
	 * @param staffLoginCodeResend espera mínima para pedir otro código
	 * @param staffLoginCodeAttempts intentos por código
	 */
	public record Tokens(@DefaultValue("P1D") Duration emailVerificationTtl,
			@DefaultValue("PT30M") Duration passwordResetTtl, @DefaultValue("P3D") Duration staffInvitationTtl,
			@DefaultValue("PT10M") Duration staffLoginCodeTtl, @DefaultValue("PT60S") Duration staffLoginCodeResend,
			@DefaultValue("5") int staffLoginCodeAttempts) {
	}

	public record Login(@DefaultValue("5") int maxFailedAttempts, @DefaultValue("PT15M") Duration lockDuration) {
	}

	public record BootstrapAdmin(@Nullable String email, @DefaultValue("Administrador FUNDIGSAC") String fullName,
			@Nullable String password) {
	}

	/**
	 * @param clientId cliente OAuth "Aplicación web" de Google (el mismo del frontend); vacío deshabilita el ingreso con
	 * Google
	 */
	public record Google(@Nullable String clientId) {
	}

}
