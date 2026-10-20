package com.fundigsac.identity.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.identity.domain.AccountKind;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.Role;
import com.fundigsac.identity.domain.SignupProfile;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de cuentas: SQL explícito y bloqueo optimista por la columna version. */
@Repository
class JdbcUserAccountRepository implements UserAccountRepository {

	private static final String SELECT = """
			SELECT u.*, COALESCE((SELECT array_agg(r.role ORDER BY r.role) FROM user_roles r WHERE r.user_id = u.id),
			                     '{}') AS roles
			  FROM user_accounts u""";

	private final JdbcClient jdbc;

	JdbcUserAccountRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Optional<UserAccount> findById(UUID id) {
		return this.jdbc.sql(SELECT + " WHERE u.id = :id").param("id", id).query(this::map).optional();
	}

	@Override
	public Optional<UserAccount> findByEmail(Email email) {
		return this.jdbc.sql(SELECT + " WHERE u.email = :email AND u.status <> 'DELETED'")
			.param("email", email.value())
			.query(this::map)
			.optional();
	}

	@Override
	public Optional<UserAccount> findByGoogleSubject(String subject) {
		return this.jdbc.sql(SELECT + " WHERE u.google_subject = :subject AND u.status <> 'DELETED'")
			.param("subject", subject)
			.query(this::map)
			.optional();
	}

	@Override
	public boolean existsByEmail(Email email) {
		return this.jdbc.sql("SELECT EXISTS (SELECT 1 FROM user_accounts WHERE email = :email AND status <> 'DELETED')")
			.param("email", email.value())
			.query(Boolean.class)
			.single();
	}

	@Override
	public void insert(UserAccount a) {
		this.jdbc.sql("""
				INSERT INTO user_accounts (id, kind, email, password_hash, full_name, company_name, ruc, phone, status,
				    email_verified_at, failed_login_attempts, locked_until, security_stamp, last_login_at,
				    privacy_notice_version, created_at, updated_at, deleted_at, version, signup_method, signup_source,
				    newsletter_opt_in_at, google_subject)
				VALUES (:id, :kind, :email, :passwordHash, :fullName, :companyName, :ruc, :phone, :status,
				    :emailVerifiedAt, :failedLoginAttempts, :lockedUntil, :securityStamp, :lastLoginAt,
				    :privacyNoticeVersion, :createdAt, :updatedAt, :deletedAt, 0, :signupMethod, :signupSource,
				    :newsletterOptInAt, :googleSubject)""")
			.param("id", a.id())
			.param("kind", a.kind().name())
			.param("email", a.email().value())
			.param("passwordHash", a.passwordHash())
			.param("fullName", a.fullName())
			.param("companyName", a.companyName())
			.param("ruc", a.ruc() == null ? null : a.ruc().value())
			.param("phone", a.phone() == null ? null : a.phone().value())
			.param("status", a.status().name())
			.param("emailVerifiedAt", SqlTypes.ts(a.emailVerifiedAt()))
			.param("failedLoginAttempts", a.failedLoginAttempts())
			.param("lockedUntil", SqlTypes.ts(a.lockedUntil()))
			.param("securityStamp", a.securityStamp())
			.param("lastLoginAt", SqlTypes.ts(a.lastLoginAt()))
			.param("privacyNoticeVersion", a.privacyNoticeVersion())
			.param("createdAt", SqlTypes.ts(a.createdAt()))
			.param("updatedAt", SqlTypes.ts(a.updatedAt()))
			.param("deletedAt", SqlTypes.ts(a.deletedAt()))
			.param("signupMethod", a.signup().method().name())
			.param("signupSource", a.signup().source())
			.param("newsletterOptInAt", SqlTypes.ts(a.signup().newsletterOptInAt()))
			.param("googleSubject", a.signup().googleSubject())
			.update();
		insertRoles(a.id(), a.roles());
	}

	@Override
	public void update(UserAccount a) {
		int rows = this.jdbc.sql("""
				UPDATE user_accounts
				   SET email = :email, password_hash = :passwordHash, full_name = :fullName, company_name = :companyName,
				       ruc = :ruc, phone = :phone, status = :status, email_verified_at = :emailVerifiedAt,
				       failed_login_attempts = :failedLoginAttempts, locked_until = :lockedUntil,
				       security_stamp = :securityStamp, last_login_at = :lastLoginAt, updated_at = :updatedAt,
				       deleted_at = :deletedAt, google_subject = :googleSubject,
				       newsletter_opt_in_at = :newsletterOptInAt, version = version + 1
				 WHERE id = :id AND version = :version""")
			.param("email", a.email().value())
			.param("passwordHash", a.passwordHash())
			.param("fullName", a.fullName())
			.param("companyName", a.companyName())
			.param("ruc", a.ruc() == null ? null : a.ruc().value())
			.param("phone", a.phone() == null ? null : a.phone().value())
			.param("status", a.status().name())
			.param("emailVerifiedAt", SqlTypes.ts(a.emailVerifiedAt()))
			.param("failedLoginAttempts", a.failedLoginAttempts())
			.param("lockedUntil", SqlTypes.ts(a.lockedUntil()))
			.param("securityStamp", a.securityStamp())
			.param("lastLoginAt", SqlTypes.ts(a.lastLoginAt()))
			.param("updatedAt", SqlTypes.ts(a.updatedAt()))
			.param("deletedAt", SqlTypes.ts(a.deletedAt()))
			.param("googleSubject", a.signup().googleSubject())
			.param("newsletterOptInAt", SqlTypes.ts(a.signup().newsletterOptInAt()))
			.param("id", a.id())
			.param("version", a.version())
			.update();
		if (rows != 1) {
			throw new StaleVersionException();
		}
		this.jdbc.sql("DELETE FROM user_roles WHERE user_id = :id").param("id", a.id()).update();
		insertRoles(a.id(), a.roles());
	}

	@Override
	public long countActiveAdministrators() {
		return this.jdbc.sql("""
				SELECT count(*) FROM user_accounts u JOIN user_roles r ON r.user_id = u.id
				 WHERE r.role = 'ADMINISTRATOR' AND u.status = 'ACTIVE'""")
			.query(Long.class)
			.single();
	}

	@Override
	public List<UserAccount> findStaff(@Nullable String query, int limit, long offset) {
		// Espacio explícito: el bloque de texto empieza sin sangría y quedaría pegado al alias ("uWHERE").
		return this.jdbc.sql(SELECT + " " + """
				 WHERE u.kind = 'STAFF' AND u.status <> 'DELETED'
				   AND (CAST(:q AS text) IS NULL OR u.email ILIKE '%' || :q || '%' OR u.full_name ILIKE '%' || :q || '%')
				 ORDER BY u.full_name, u.id
				 LIMIT :limit OFFSET :offset""")
			.param("q", escapeLike(query))
			.param("limit", limit)
			.param("offset", offset)
			.query(this::map)
			.list();
	}

	@Override
	public long countStaff(@Nullable String query) {
		return this.jdbc.sql("""
				SELECT count(*) FROM user_accounts u
				 WHERE u.kind = 'STAFF' AND u.status <> 'DELETED'
				   AND (CAST(:q AS text) IS NULL OR u.email ILIKE '%' || :q || '%' OR u.full_name ILIKE '%' || :q || '%')""")
			.param("q", escapeLike(query))
			.query(Long.class)
			.single();
	}

	@Override
	public Optional<SecurityState> findSecurityState(UUID id) {
		return this.jdbc.sql("SELECT security_stamp, status FROM user_accounts WHERE id = :id")
			.param("id", id)
			.query((rs, n) -> new SecurityState(rs.getInt("security_stamp"), AccountStatus.valueOf(rs.getString("status"))))
			.optional();
	}

	private void insertRoles(UUID userId, Set<Role> roles) {
		for (Role role : roles) {
			this.jdbc.sql("INSERT INTO user_roles (user_id, role) VALUES (:userId, :role)")
				.param("userId", userId)
				.param("role", role.name())
				.update();
		}
	}

	private static @Nullable String escapeLike(@Nullable String query) {
		return query == null ? null : query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	private UserAccount map(ResultSet rs, int row) throws SQLException {
		Set<Role> roles = EnumSet.noneOf(Role.class);
		SqlTypes.textArray(rs, "roles").forEach((role) -> roles.add(Role.valueOf(role)));
		String ruc = rs.getString("ruc");
		String phone = rs.getString("phone");
		return UserAccount.rehydrate(SqlTypes.uuid(rs, "id"), AccountKind.valueOf(rs.getString("kind")),
				new Email(rs.getString("email")), rs.getString("password_hash"), rs.getString("full_name"),
				rs.getString("company_name"), ruc == null ? null : new Ruc(ruc), phone == null ? null : new PhoneNumber(phone),
				AccountStatus.valueOf(rs.getString("status")), roles, SqlTypes.instant(rs, "email_verified_at"),
				rs.getInt("failed_login_attempts"), SqlTypes.instant(rs, "locked_until"), rs.getInt("security_stamp"),
				SqlTypes.instant(rs, "last_login_at"), rs.getString("privacy_notice_version"),
				SqlTypes.instant(rs, "created_at"), SqlTypes.instant(rs, "updated_at"), SqlTypes.instant(rs, "deleted_at"),
				rs.getLong("version"), new SignupProfile(SignupProfile.Method.valueOf(rs.getString("signup_method")),
						rs.getString("signup_source"), SqlTypes.instant(rs, "newsletter_opt_in_at"), rs.getString("google_subject")));
	}

}
