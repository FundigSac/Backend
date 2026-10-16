package com.fundigsac.identity.infrastructure;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.domain.RefreshToken;
import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de refresh tokens (solo hashes SHA-256). */
@Repository
class JdbcRefreshTokenRepository implements RefreshTokenRepository {

	private final JdbcClient jdbc;

	JdbcRefreshTokenRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void insert(RefreshToken t) {
		this.jdbc.sql("""
				INSERT INTO refresh_tokens (id, user_id, family_id, token_hash, issued_at, expires_at)
				VALUES (:id, :userId, :familyId, :hash, :issuedAt, :expiresAt)""")
			.param("id", t.id())
			.param("userId", t.userId())
			.param("familyId", t.familyId())
			.param("hash", t.tokenHash())
			.param("issuedAt", SqlTypes.ts(t.issuedAt()))
			.param("expiresAt", SqlTypes.ts(t.expiresAt()))
			.update();
	}

	@Override
	public Optional<RefreshToken> findByHash(String tokenHash) {
		return this.jdbc.sql("SELECT * FROM refresh_tokens WHERE token_hash = :hash")
			.param("hash", tokenHash)
			.query((rs, n) -> new RefreshToken(SqlTypes.uuid(rs, "id"), SqlTypes.uuid(rs, "user_id"),
					SqlTypes.uuid(rs, "family_id"), rs.getString("token_hash"), SqlTypes.instant(rs, "issued_at"),
					SqlTypes.instant(rs, "expires_at"), SqlTypes.instant(rs, "revoked_at"), SqlTypes.uuid(rs, "replaced_by")))
			.optional();
	}

	@Override
	public boolean rotate(UUID id, UUID replacedBy, Instant now) {
		return this.jdbc.sql("""
				UPDATE refresh_tokens SET revoked_at = :now, replaced_by = :replacedBy
				 WHERE id = :id AND revoked_at IS NULL""")
			.param("now", SqlTypes.ts(now))
			.param("replacedBy", replacedBy)
			.param("id", id)
			.update() == 1;
	}

	@Override
	public void revokeFamily(UUID familyId, Instant now) {
		this.jdbc.sql("UPDATE refresh_tokens SET revoked_at = :now WHERE family_id = :familyId AND revoked_at IS NULL")
			.param("now", SqlTypes.ts(now))
			.param("familyId", familyId)
			.update();
	}

	@Override
	public void revokeAllForUser(UUID userId, Instant now) {
		this.jdbc.sql("UPDATE refresh_tokens SET revoked_at = :now WHERE user_id = :userId AND revoked_at IS NULL")
			.param("now", SqlTypes.ts(now))
			.param("userId", userId)
			.update();
	}

	@Override
	@Transactional
	public int purgeExpired(Instant before) {
		return this.jdbc.sql("DELETE FROM refresh_tokens WHERE expires_at < :before")
			.param("before", SqlTypes.ts(before))
			.update();
	}

}
