package com.fundigsac.shared.infrastructure;

import java.time.Clock;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.shared.application.FolioGenerator;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.PeruTime;

/**
 * Correlativo por prefijo y año con un UPSERT atómico: la fila queda bloqueada hasta el fin de la transacción, así
 * dos solicitudes simultáneas nunca reciben el mismo número y un rollback no deja huecos.
 */
@Repository
class JdbcFolioGenerator implements FolioGenerator {

	private final JdbcClient jdbc;

	private final Clock clock;

	JdbcFolioGenerator(JdbcClient jdbc, Clock clock) {
		this.jdbc = jdbc;
		this.clock = clock;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Folio next(String prefix) {
		int year = PeruTime.dateOf(this.clock.instant()).getYear();
		Integer value = this.jdbc.sql("""
				INSERT INTO folio_sequences (prefix, year, last_value) VALUES (:prefix, :year, 1)
				ON CONFLICT (prefix, year) DO UPDATE SET last_value = folio_sequences.last_value + 1
				RETURNING last_value""")
			.param("prefix", prefix)
			.param("year", year)
			.query(Integer.class)
			.single();
		return new Folio(prefix, year, value);
	}

}
