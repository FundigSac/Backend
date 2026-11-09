package com.fundigsac.shared.infrastructure;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Arranque del servicio de constancias: clave corta, ausente o temporal. */
class HmacReceiptTokensTests {

	private static String keyOf(int bytes) {
		return Base64.getEncoder().encodeToString(new byte[bytes]);
	}

	private static HmacReceiptTokens tokens(String key, boolean allowEphemeral) {
		return new HmacReceiptTokens(new SecretsProperties(key, allowEphemeral));
	}

	@Test
	void aKeyShorterThan32BytesPreventsStartup() {
		assertThatThrownBy(() -> tokens(keyOf(31), true)).isInstanceOf(IllegalStateException.class).hasMessageContaining("al menos 32 bytes");
	}

	@Test
	void aMissingKeyPreventsStartupOutsideDevelopment() {
		for (String missing : new String[] { null, "  " }) {
			assertThatThrownBy(() -> tokens(missing, false)).isInstanceOf(IllegalStateException.class).hasMessageContaining("receipt_key");
		}
	}

	@Test
	void aMissingKeyIsReplacedByARandomOneOnlyInDevelopment() {
		String first = tokens(null, true).issue("QUOTE", "COT-2026-000001");
		String second = tokens(null, true).issue("QUOTE", "COT-2026-000001");
		assertThat(first).isNotEqualTo(second);
	}

	@Test
	void aConfiguredKeyGivesStableTokensBoundToTheirPurposeAndReference() {
		HmacReceiptTokens tokens = tokens(keyOf(32), false);
		String token = tokens.issue("QUOTE", "COT-2026-000001");
		assertThat(tokens(keyOf(32), false).issue("QUOTE", "COT-2026-000001")).isEqualTo(token);
		assertThat(tokens.matches("QUOTE", "COT-2026-000001", token)).isTrue();
		assertThat(tokens.matches("COMPLAINT", "COT-2026-000001", token)).isFalse();
		assertThat(tokens.matches("QUOTE", "COT-2026-000002", token)).isFalse();
		assertThat(tokens.matches("QUOTE", "COT-2026-000001", null)).isFalse();
		assertThat(tokens.matches("QUOTE", "COT-2026-000001", " ")).isFalse();
	}

}
