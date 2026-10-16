package com.fundigsac.notification.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Guard;

/**
 * Correo en la bandeja de salida. Reintenta con espera exponencial; tras el máximo de intentos queda FAILED para
 * revisión del personal. El cuerpo se borra al enviarse (puede contener enlaces con tokens).
 */
public final class OutgoingEmail {

	public enum Status {
		PENDING, SENT, FAILED
	}

	private final UUID id;

	private final String dedupeKey;

	private final String template;

	private final Email recipient;

	private final String subject;

	private @Nullable String body;

	private Status status;

	private int attempts;

	private Instant nextAttemptAt;

	private @Nullable String lastError;

	private final @Nullable String reference;

	private final Instant createdAt;

	private @Nullable Instant sentAt;

	public OutgoingEmail(UUID id, String dedupeKey, String template, Email recipient, String subject, @Nullable String body,
			Status status, int attempts, Instant nextAttemptAt, @Nullable String lastError, @Nullable String reference,
			Instant createdAt, @Nullable Instant sentAt) {
		this.id = id;
		this.dedupeKey = Guard.text("dedupeKey", dedupeKey, 1, 200);
		this.template = template;
		this.recipient = recipient;
		this.subject = Guard.text("subject", subject, 1, 200);
		this.body = body;
		this.status = status;
		this.attempts = attempts;
		this.nextAttemptAt = nextAttemptAt;
		this.lastError = lastError;
		this.reference = reference;
		this.createdAt = createdAt;
		this.sentAt = sentAt;
	}

	public static OutgoingEmail pending(UUID id, String dedupeKey, String template, Email recipient, String subject,
			String body, @Nullable String reference, Instant now) {
		return new OutgoingEmail(id, dedupeKey, template, recipient, subject, Guard.multiline("body", body, 1, 20_000),
				Status.PENDING, 0, now, null, reference, now, null);
	}

	public void markSent(Instant now) {
		this.status = Status.SENT;
		this.attempts++;
		this.sentAt = now;
		this.body = null;
		this.lastError = null;
	}

	public void markFailed(String error, int maxAttempts, Duration retryBase, Instant now) {
		this.attempts++;
		this.lastError = error.length() > 500 ? error.substring(0, 500) : error;
		if (this.attempts >= maxAttempts) {
			this.status = Status.FAILED;
		}
		else {
			long factor = 1L << Math.min(this.attempts - 1, 10);
			this.nextAttemptAt = now.plus(retryBase.multipliedBy(factor));
		}
	}

	/** Reintento manual de un correo fallido (si aún conserva su cuerpo). */
	public boolean retry(Instant now) {
		if (this.status != Status.FAILED || this.body == null) {
			return false;
		}
		this.status = Status.PENDING;
		this.attempts = 0;
		this.nextAttemptAt = now;
		return true;
	}

	public UUID id() {
		return this.id;
	}

	public String dedupeKey() {
		return this.dedupeKey;
	}

	public String template() {
		return this.template;
	}

	public Email recipient() {
		return this.recipient;
	}

	public String subject() {
		return this.subject;
	}

	public @Nullable String body() {
		return this.body;
	}

	public Status status() {
		return this.status;
	}

	public int attempts() {
		return this.attempts;
	}

	public Instant nextAttemptAt() {
		return this.nextAttemptAt;
	}

	public @Nullable String lastError() {
		return this.lastError;
	}

	public @Nullable String reference() {
		return this.reference;
	}

	public Instant createdAt() {
		return this.createdAt;
	}

	public @Nullable Instant sentAt() {
		return this.sentAt;
	}

}
