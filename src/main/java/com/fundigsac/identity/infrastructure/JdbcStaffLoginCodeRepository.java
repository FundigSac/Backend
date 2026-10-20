package com.fundigsac.identity.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.identity.domain.StaffLoginCode;
import com.fundigsac.identity.domain.StaffLoginCodeRepository;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de los códigos de acceso del personal. */
@Repository
class JdbcStaffLoginCodeRepository implements StaffLoginCodeRepository {

	private final JdbcClient jdbc;

	JdbcStaffLoginCodeRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void insert(StaffLoginCode c) {
		this.jdbc.sql("""
				INSERT INTO staff_login_codes (id, user_id, code_hash, created_at, expires_at, attempts_left, used_at)
				VALUES (:id, :userId, :codeHash, :createdAt, :expiresAt, :attemptsLeft, :usedAt)""")
			.param("id", c.id())
			.param("userId", c.userId())
			.param("codeHash", c.codeHash())
			.param("createdAt", SqlTypes.ts(c.createdAt()))
			.param("expiresAt", SqlTypes.ts(c.expiresAt()))
			.param("attemptsLeft", c.attemptsLeft())
			.param("usedAt", SqlTypes.ts(c.usedAt()))
			.update();
	}

	@Override
	public Optional<StaffLoginCode> findLatest(UUID userId) {
		return this.jdbc.sql("SELECT * FROM staff_login_codes WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1")
			.param("userId", userId)
			.query(this::map)
			.optional();
	}

	@Override
	public void invalidatePending(UUID userId, Instant now) {
		this.jdbc.sql("UPDATE staff_login_codes SET used_at = :now WHERE user_id = :userId AND used_at IS NULL")
			.param("now", SqlTypes.ts(now))
			.param("userId", userId)
			.update();
	}

	@Override
	public int consumeAttempt(UUID codeId) {
		return this.jdbc.sql("""
				UPDATE staff_login_codes SET attempts_left = attempts_left - 1
				 WHERE id = :id AND attempts_left > 0
				RETURNING attempts_left""")
			.param("id", codeId)
			.query(Integer.class)
			.optional()
			.orElse(0);
	}

	@Override
	public boolean markUsed(UUID codeId, Instant now) {
		return this.jdbc.sql("UPDATE staff_login_codes SET used_at = :now WHERE id = :id AND used_at IS NULL AND attempts_left > 0")
			.param("now", SqlTypes.ts(now))
			.param("id", codeId)
			.update() == 1;
	}

	@Override
	public int deleteExpired(Instant before) {
		return this.jdbc.sql("DELETE FROM staff_login_codes WHERE expires_at < :before")
			.param("before", SqlTypes.ts(before))
			.update();
	}

	private StaffLoginCode map(ResultSet rs, int row) throws SQLException {
		return new StaffLoginCode(SqlTypes.uuid(rs, "id"), SqlTypes.uuid(rs, "user_id"), rs.getString("code_hash"),
				SqlTypes.instant(rs, "created_at"), SqlTypes.instant(rs, "expires_at"), rs.getInt("attempts_left"),
				SqlTypes.instant(rs, "used_at"));
	}

}
