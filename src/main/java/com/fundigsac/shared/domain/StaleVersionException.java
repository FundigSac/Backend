package com.fundigsac.shared.domain;

/**
 * Bloqueo optimista: el recurso cambió desde que se leyó (If-Match distinto o actualización concurrente). HTTP 412;
 * el cliente debe recargar y volver a intentar.
 */
public final class StaleVersionException extends DomainException {

	public StaleVersionException() {
		super("STALE_VERSION", "El registro cambió desde que lo consultó. Recárguelo y vuelva a intentar.");
	}

}
