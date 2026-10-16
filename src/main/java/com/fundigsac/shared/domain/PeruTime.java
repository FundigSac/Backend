package com.fundigsac.shared.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Los instantes se guardan en UTC; las fechas de negocio (folios, plazos) se calculan en hora de Lima. */
public final class PeruTime {

	public static final ZoneId ZONE = ZoneId.of("America/Lima");

	private PeruTime() {
	}

	public static LocalDate dateOf(Instant instant) {
		return instant.atZone(ZONE).toLocalDate();
	}

}
