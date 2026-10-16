package com.fundigsac.shared.web;

/** Se superó el límite de frecuencia (HTTP 429 con Retry-After). */
public final class TooManyRequestsException extends RuntimeException {

	private final long retryAfterSeconds;

	public TooManyRequestsException(long retryAfterSeconds) {
		super("Too many requests");
		this.retryAfterSeconds = retryAfterSeconds;
	}

	public long retryAfterSeconds() {
		return this.retryAfterSeconds;
	}

}
