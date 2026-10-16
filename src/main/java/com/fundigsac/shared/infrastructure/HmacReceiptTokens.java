package com.fundigsac.shared.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fundigsac.shared.application.ReceiptTokens;
import com.fundigsac.shared.domain.SecureTokens;

/** Token de constancia = HMAC-SHA256(clave, propósito:folio) en Base64 URL. Verificación en tiempo constante. */
@Component
class HmacReceiptTokens implements ReceiptTokens {

	private static final Logger log = LoggerFactory.getLogger(HmacReceiptTokens.class);

	private final byte[] key;

	HmacReceiptTokens(SecretsProperties properties) {
		this.key = resolveKey(properties);
	}

	@Override
	public String issue(String purpose, String reference) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(this.key, "HmacSHA256"));
			byte[] digest = mac.doFinal((purpose + ":" + reference).getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
		}
		catch (GeneralSecurityException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public boolean matches(String purpose, String reference, @Nullable String token) {
		return token != null && !token.isBlank() && SecureTokens.constantTimeEquals(issue(purpose, reference), token.strip());
	}

	private static byte[] resolveKey(SecretsProperties properties) {
		String configured = properties.receiptKey();
		if (configured != null && !configured.isBlank()) {
			byte[] decoded = Base64.getDecoder().decode(configured.strip());
			if (decoded.length < 32) {
				throw new IllegalStateException("fundigsac.security.receipt-key debe tener al menos 32 bytes");
			}
			return decoded;
		}
		if (!properties.allowEphemeralKeys()) {
			throw new IllegalStateException("Falta el secreto receipt_key (fundigsac.security.receipt-key)");
		}
		log.warn("receipt_key no configurada: se usa una clave temporal (solo desarrollo). Las constancias no sobreviven a un reinicio.");
		byte[] random = new byte[32];
		new SecureRandom().nextBytes(random);
		return Arrays.copyOf(random, random.length);
	}

}
