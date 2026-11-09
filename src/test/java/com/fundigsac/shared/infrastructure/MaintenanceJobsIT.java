package com.fundigsac.shared.infrastructure;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/** La limpieza programada borra solo las claves de idempotencia ya vencidas. */
class MaintenanceJobsIT extends IntegrationTest {

	@Autowired
	MaintenanceJobs jobs;

	private void insertKey(String key, Instant expiresAt) {
		Instant createdAt = expiresAt.minus(Duration.ofHours(24));
		this.jdbc.sql("""
				INSERT INTO idempotency_records (scope, idem_key, fingerprint, created_at, expires_at)
				VALUES ('prueba-limpieza', :key, :fingerprint, :created, :expires)""")
			.param("key", key)
			.param("fingerprint", "0".repeat(64))
			.param("created", Timestamp.from(createdAt))
			.param("expires", Timestamp.from(expiresAt))
			.update();
	}

	private boolean exists(String key) {
		return count("SELECT count(*) FROM idempotency_records WHERE scope = 'prueba-limpieza' AND idem_key = :key", Map.of("key", key)) == 1;
	}

	@Test
	void onlyExpiredIdempotencyKeysAreRemoved() {
		insertKey("vencida-hace-un-minuto", Instant.now().minus(Duration.ofMinutes(1)));
		insertKey("vigente-una-hora-mas", Instant.now().plus(Duration.ofHours(1)));

		this.jobs.purgeExpiredIdempotencyKeys();

		assertThat(exists("vencida-hace-un-minuto")).isFalse();
		assertThat(exists("vigente-una-hora-mas")).isTrue();
	}

}
