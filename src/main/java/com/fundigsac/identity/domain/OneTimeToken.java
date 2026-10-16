package com.fundigsac.identity.domain;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Token de un solo uso enviado por correo; solo se guarda su SHA-256. */
public record OneTimeToken(UUID id, UUID userId, Purpose purpose, String tokenHash, Instant createdAt, Instant expiresAt,
		@Nullable Instant usedAt) {

	public enum Purpose {
		EMAIL_VERIFICATION, PASSWORD_RESET, STAFF_INVITATION
	}

	public boolean isUsable(Purpose expected, Instant now) {
		return this.purpose == expected && this.usedAt == null && this.expiresAt.isAfter(now);
	}

}
