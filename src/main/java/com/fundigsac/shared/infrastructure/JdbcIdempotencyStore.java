package com.fundigsac.shared.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.shared.application.IdempotencyStore;

/**
 * La reserva es un INSERT … ON CONFLICT: si otra transacción tiene la misma clave, PostgreSQL espera a que termine y
 * luego informa el conflicto, así la segunda petición ve el folio ya confirmado. Las claves vencidas se reutilizan.
 */
@Repository
class JdbcIdempotencyStore implements IdempotencyStore {

	private final JdbcClient jdbc;

	JdbcIdempotencyStore(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public boolean reserve(String scope, String key, String fingerprint, Instant now, Instant expiresAt) {
		int rows = this.jdbc.sql("""
				INSERT INTO idempotency_records (scope, idem_key, fingerprint, created_at, expires_at)
				VALUES (:scope, :key, :fingerprint, :now, :expiresAt)
				ON CONFLICT (scope, idem_key) DO UPDATE
				   SET fingerprint = EXCLUDED.fingerprint, resource_ref = NULL,
				       created_at = EXCLUDED.created_at, expires_at = EXCLUDED.expires_at
				 WHERE idempotency_records.expires_at < EXCLUDED.created_at""")
			.param("scope", scope)
			.param("key", key)
			.param("fingerprint", fingerprint)
			.param("now", Timestamp.from(now))
			.param("expiresAt", Timestamp.from(expiresAt))
			.update();
		return rows == 1;
	}

	@Override
	public Optional<Entry> find(String scope, String key) {
		return this.jdbc.sql("SELECT fingerprint, resource_ref FROM idempotency_records WHERE scope = :scope AND idem_key = :key")
			.param("scope", scope)
			.param("key", key)
			.query((rs, n) -> new Entry(rs.getString("fingerprint"), rs.getString("resource_ref")))
			.optional();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void complete(String scope, String key, String resourceRef) {
		this.jdbc.sql("UPDATE idempotency_records SET resource_ref = :ref WHERE scope = :scope AND idem_key = :key")
			.param("ref", resourceRef)
			.param("scope", scope)
			.param("key", key)
			.update();
	}

	@Override
	@Transactional
	public int purgeExpired(Instant now) {
		return this.jdbc.sql("DELETE FROM idempotency_records WHERE expires_at < :now")
			.param("now", Timestamp.from(now))
			.update();
	}

}
