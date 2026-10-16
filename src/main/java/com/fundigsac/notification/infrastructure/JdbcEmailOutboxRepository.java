package com.fundigsac.notification.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.notification.domain.EmailOutboxRepository;
import com.fundigsac.notification.domain.OutgoingEmail;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de la bandeja de salida de correos. */
@Repository
class JdbcEmailOutboxRepository implements EmailOutboxRepository {

	private final JdbcClient jdbc;

	JdbcEmailOutboxRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public boolean enqueue(OutgoingEmail e) {
		return this.jdbc.sql("""
				INSERT INTO email_outbox (id, dedupe_key, template, recipient, subject, body, status, attempts,
				    next_attempt_at, reference, created_at)
				VALUES (:id, :key, :template, :recipient, :subject, :body, 'PENDING', 0, :next, :reference, :createdAt)
				ON CONFLICT (dedupe_key) DO NOTHING""")
			.param("id", e.id())
			.param("key", e.dedupeKey())
			.param("template", e.template())
			.param("recipient", e.recipient().value())
			.param("subject", e.subject())
			.param("body", e.body())
			.param("next", SqlTypes.ts(e.nextAttemptAt()))
			.param("reference", e.reference())
			.param("createdAt", SqlTypes.ts(e.createdAt()))
			.update() == 1;
	}

	@Override
	public List<OutgoingEmail> claimDue(Instant now, int limit) {
		return this.jdbc.sql("""
				SELECT * FROM email_outbox WHERE status = 'PENDING' AND next_attempt_at <= :now
				 ORDER BY next_attempt_at LIMIT :limit FOR UPDATE SKIP LOCKED""")
			.param("now", SqlTypes.ts(now))
			.param("limit", limit)
			.query(JdbcEmailOutboxRepository::map)
			.list();
	}

	@Override
	public void update(OutgoingEmail e) {
		this.jdbc.sql("""
				UPDATE email_outbox SET body = :body, status = :status, attempts = :attempts, next_attempt_at = :next,
				    last_error = :error, sent_at = :sentAt
				 WHERE id = :id""")
			.param("body", e.body())
			.param("status", e.status().name())
			.param("attempts", e.attempts())
			.param("next", SqlTypes.ts(e.nextAttemptAt()))
			.param("error", e.lastError())
			.param("sentAt", SqlTypes.ts(e.sentAt()))
			.param("id", e.id())
			.update();
	}

	@Override
	public Optional<OutgoingEmail> findById(UUID id) {
		return this.jdbc.sql("SELECT * FROM email_outbox WHERE id = :id").param("id", id).query(JdbcEmailOutboxRepository::map).optional();
	}

	@Override
	public List<OutgoingEmail> search(@Nullable String status, int limit, long offset) {
		Map<String, Object> params = new HashMap<>();
		params.put("limit", limit);
		params.put("offset", offset);
		if (status != null) {
			params.put("status", status);
		}
		return this.jdbc.sql("SELECT * FROM email_outbox" + (status == null ? "" : " WHERE status = :status")
				+ " ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset")
			.params(params)
			.query(JdbcEmailOutboxRepository::map)
			.list();
	}

	@Override
	public long count(@Nullable String status) {
		var sql = this.jdbc.sql("SELECT count(*) FROM email_outbox" + (status == null ? "" : " WHERE status = :status"));
		if (status != null) {
			sql = sql.param("status", status);
		}
		return sql.query(Long.class).single();
	}

	private static OutgoingEmail map(ResultSet rs, int row) throws SQLException {
		return new OutgoingEmail(SqlTypes.uuid(rs, "id"), rs.getString("dedupe_key"), rs.getString("template"),
				new Email(rs.getString("recipient")), rs.getString("subject"), rs.getString("body"),
				OutgoingEmail.Status.valueOf(rs.getString("status")), rs.getInt("attempts"),
				SqlTypes.instant(rs, "next_attempt_at"), rs.getString("last_error"), rs.getString("reference"),
				SqlTypes.instant(rs, "created_at"), SqlTypes.instant(rs, "sent_at"));
	}

}
