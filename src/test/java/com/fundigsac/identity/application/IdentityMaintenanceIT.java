package com.fundigsac.identity.application;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Limpieza programada: sesiones vencidas hace más de 30 días y códigos de acceso vencidos hace más de un día. */
class IdentityMaintenanceIT extends IntegrationTest {

	@Autowired
	IdentityMaintenance maintenance;

	private String insertSession(String interval) {
		String id = UUID.randomUUID().toString();
		this.jdbc.sql("""
				INSERT INTO refresh_tokens (id, user_id, family_id, token_hash, issued_at, expires_at)
				VALUES (CAST(:id AS uuid), CAST(:user AS uuid), gen_random_uuid(), :hash,
				        now() - CAST(:interval AS interval) - interval '14 days', now() - CAST(:interval AS interval))""")
			.param("id", id)
			.param("user", userId(ADMIN_EMAIL))
			.param("hash", id.replace("-", "") + "0".repeat(32))
			.param("interval", interval)
			.update();
		return id;
	}

	private String insertCode(String interval) {
		String id = UUID.randomUUID().toString();
		this.jdbc.sql("""
				INSERT INTO staff_login_codes (id, user_id, code_hash, created_at, expires_at, attempts_left)
				VALUES (CAST(:id AS uuid), CAST(:user AS uuid), :hash, now() - CAST(:interval AS interval) - interval '10 minutes',
				        now() - CAST(:interval AS interval), 5)""")
			.param("id", id)
			.param("user", userId(ADMIN_EMAIL))
			.param("hash", id.replace("-", "") + "0".repeat(32))
			.param("interval", interval)
			.update();
		return id;
	}

	private boolean exists(String table, String id) {
		return count("SELECT count(*) FROM " + table + " WHERE id = CAST(:id AS uuid)", Map.of("id", id)) == 1;
	}

	@Test
	void onlySessionsAndCodesPastTheirRetentionAreRemoved() {
		String oldSession = insertSession("31 days");
		String recentSession = insertSession("29 days");
		String oldCode = insertCode("25 hours");
		String recentCode = insertCode("23 hours");

		this.maintenance.purgeExpiredSessions();

		assertThat(exists("refresh_tokens", oldSession)).isFalse();
		assertThat(exists("refresh_tokens", recentSession)).as("se conserva 30 días para detectar reutilización").isTrue();
		assertThat(exists("staff_login_codes", oldCode)).isFalse();
		assertThat(exists("staff_login_codes", recentCode)).isTrue();
	}

}
