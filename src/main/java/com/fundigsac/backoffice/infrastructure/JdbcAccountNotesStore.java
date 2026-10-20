package com.fundigsac.backoffice.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.backoffice.application.AccountNotesStore;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de las notas internas. */
@Repository
class JdbcAccountNotesStore implements AccountNotesStore {

	private final JdbcClient jdbc;

	JdbcAccountNotesStore(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void save(UUID userId, String notes, UUID updatedBy, Instant now) {
		if (notes.isBlank()) {
			delete(userId);
			return;
		}
		this.jdbc.sql("""
				INSERT INTO account_notes (user_id, notes, updated_at, updated_by)
				VALUES (:userId, :notes, :now, :by)
				ON CONFLICT (user_id) DO UPDATE SET notes = EXCLUDED.notes, updated_at = EXCLUDED.updated_at,
				    updated_by = EXCLUDED.updated_by""")
			.param("userId", userId)
			.param("notes", notes)
			.param("now", SqlTypes.ts(now))
			.param("by", updatedBy)
			.update();
	}

	@Override
	public void delete(UUID userId) {
		this.jdbc.sql("DELETE FROM account_notes WHERE user_id = :userId").param("userId", userId).update();
	}

}
