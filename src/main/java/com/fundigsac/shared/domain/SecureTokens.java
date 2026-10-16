package com.fundigsac.shared.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/** Tokens opacos de un solo uso: se entrega el valor aleatorio y solo se guarda su SHA-256. */
public final class SecureTokens {

	private static final SecureRandom RANDOM = new SecureRandom();

	private SecureTokens() {
	}

	/** 256 bits aleatorios en Base64 URL sin relleno (43 caracteres). */
	public static String random() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/** Comparación en tiempo constante (evita ataques de tiempo al verificar tokens). */
	public static boolean constantTimeEquals(String a, String b) {
		return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
	}

}
