package com.fundigsac.identity.infrastructure;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.identity.domain.OneTimeToken;
import com.fundigsac.identity.domain.OneTimeTokenRepository;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de tokens de un solo uso. */
@Repository
class JdbcOneTimeTokenRepository implements OneTimeTokenRepository {

	private final JdbcClient jdbc;

	JdbcOneTimeTokenRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void insert(OneTimeToken t) {
		this.jdbc.sql("""
				INSERT INTO one_time_tokens (id, user_id, purpose, token_hash, created_at, expires_at)
				VALUES (:id, :userId, :purpose, :hash, :createdAt, :expiresAt)""")
			.param("id", t.id())
			.param("userId", t.userId())
			.param("purpose", t.purpose().name())
			.param("hash", t.tokenHash())
			.param("createdAt", SqlTypes.ts(t.createdAt()))
			.param("expiresAt", SqlTypes.ts(t.expiresAt()))
			.update();
	}

	@Override
	public Optional<OneTimeToken> findByHash(String tokenHash) {
		return this.jdbc.sql("SELECT * FROM one_time_tokens WHERE token_hash = :hash")
			.param("hash", tokenHash)
			.query((rs, n) -> new OneTimeToken(SqlTypes.uuid(rs, "id"), SqlTypes.uuid(rs, "user_id"),
					OneTimeToken.Purpose.valueOf(rs.getString("purpose")), rs.getString("token_hash"),
					SqlTypes.instant(rs, "created_at"), SqlTypes.instant(rs, "expires_at"), SqlTypes.instant(rs, "used_at")))
			.optional();
	}

	@Override
	public boolean markUsed(UUID id, Instant now) {
		return this.jdbc.sql("UPDATE one_time_tokens SET used_at = :now WHERE id = :id AND used_at IS NULL")
			.param("now", SqlTypes.ts(now))
			.param("id", id)
			.update() == 1;
	}

	@Override
	public void invalidatePending(UUID userId, OneTimeToken.Purpose purpose, Instant now) {
		this.jdbc.sql("""
				UPDATE one_time_tokens SET used_at = :now
				 WHERE user_id = :userId AND purpose = :purpose AND used_at IS NULL""")
			.param("now", SqlTypes.ts(now))
			.param("userId", userId)
			.param("purpose", purpose.name())
			.update();
	}

}
