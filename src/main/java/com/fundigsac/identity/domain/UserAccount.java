package com.fundigsac.identity.domain;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.identity.api.IdentityEvents.AccountDeleted;
import com.fundigsac.identity.api.IdentityEvents.AccountLocked;
import com.fundigsac.identity.api.IdentityEvents.AccountUpdatedByStaff;
import com.fundigsac.identity.api.IdentityEvents.EmailVerified;
import com.fundigsac.identity.api.IdentityEvents.PasswordChanged;
import com.fundigsac.identity.api.IdentityEvents.UserRolesChanged;
import com.fundigsac.identity.api.IdentityEvents.UserStatusChanged;
import com.fundigsac.shared.domain.AggregateRoot;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.ForbiddenException;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.UnauthorizedException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Permission;

/**
 * Cuenta de usuario (agregado). El sello de seguridad (securityStamp) se incrementa con cada cambio sensible
 * (contraseña, roles, desactivación, eliminación): los JWT emitidos con el sello anterior dejan de ser válidos.
 */
public final class UserAccount extends AggregateRoot {

	private final UUID id;

	private final AccountKind kind;

	private Email email;

	private @Nullable String passwordHash;

	private String fullName;

	private @Nullable String companyName;

	private @Nullable Ruc ruc;

	private @Nullable PhoneNumber phone;

	private AccountStatus status;

	private Set<Role> roles;

	private @Nullable Instant emailVerifiedAt;

	private int failedLoginAttempts;

	private @Nullable Instant lockedUntil;

	private int securityStamp;

	private @Nullable Instant lastLoginAt;

	private @Nullable String privacyNoticeVersion;

	private final Instant createdAt;

	private Instant updatedAt;

	private @Nullable Instant deletedAt;

	private final long version;

	private SignupProfile signup = SignupProfile.email();

	private UserAccount(UUID id, AccountKind kind, Email email, @Nullable String passwordHash, String fullName,
			@Nullable String companyName, @Nullable Ruc ruc, @Nullable PhoneNumber phone, AccountStatus status,
			Set<Role> roles, @Nullable Instant emailVerifiedAt, int failedLoginAttempts, @Nullable Instant lockedUntil,
			int securityStamp, @Nullable Instant lastLoginAt, @Nullable String privacyNoticeVersion, Instant createdAt,
			Instant updatedAt, @Nullable Instant deletedAt, long version) {
		this.id = id;
		this.kind = kind;
		this.email = email;
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.companyName = companyName;
		this.ruc = ruc;
		this.phone = phone;
		this.status = status;
		this.roles = EnumSet.copyOf(roles);
		this.emailVerifiedAt = emailVerifiedAt;
		this.failedLoginAttempts = failedLoginAttempts;
		this.lockedUntil = lockedUntil;
		this.securityStamp = securityStamp;
		this.lastLoginAt = lastLoginAt;
		this.privacyNoticeVersion = privacyNoticeVersion;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
		this.version = version;
	}

	public static UserAccount registerCustomer(UUID id, Email email, String passwordHash, String fullName,
			@Nullable String companyName, @Nullable Ruc ruc, @Nullable PhoneNumber phone, String privacyNoticeVersion,
			Instant now) {
		return new UserAccount(id, AccountKind.CUSTOMER, email, passwordHash, Guard.text("fullName", fullName, 2, 120),
				Guard.optionalText("companyName", companyName, 160), ruc, phone, AccountStatus.PENDING_VERIFICATION,
				EnumSet.of(Role.CUSTOMER), null, 0, null, 0, null,
				Guard.text("privacyNoticeVersion", privacyNoticeVersion, 1, 20), now, now, null, 0);
	}

	/** Personal invitado: sin contraseña hasta que acepte la invitación (que además verifica su correo). */
	public static UserAccount inviteStaff(UUID id, Email email, String fullName, Set<Role> roles, Instant now) {
		return new UserAccount(id, AccountKind.STAFF, email, null, Guard.text("fullName", fullName, 2, 120), null, null,
				null, AccountStatus.PENDING_VERIFICATION, requireStaffRoles(roles), null, 0, null, 0, null, null, now, now,
				null, 0);
	}

	/** Primer administrador, creado al arrancar desde secretos de despliegue. */
	public static UserAccount bootstrapAdministrator(UUID id, Email email, String fullName, String passwordHash,
			Instant now) {
		return new UserAccount(id, AccountKind.STAFF, email, passwordHash, Guard.text("fullName", fullName, 2, 120), null,
				null, null, AccountStatus.ACTIVE, EnumSet.of(Role.ADMINISTRATOR), now, 0, null, 0, null, null, now, now, null,
				0);
	}

	/**
	 * Cliente que entra por primera vez con Google: Google ya verificó el correo, así que la cuenta nace activa y sin
	 * contraseña. Igual que en el registro por correo, debe constar qué versión del aviso de privacidad aceptó.
	 */
	public static UserAccount registerWithGoogle(UUID id, Email email, String googleSubject, String fullName,
			String privacyNoticeVersion, Instant now) {
		UserAccount account = new UserAccount(id, AccountKind.CUSTOMER, email, null,
				Guard.text("fullName", fullName, 2, 120), null, null, null, AccountStatus.ACTIVE, EnumSet.of(Role.CUSTOMER),
				now, 0, null, 0, null, Guard.text("privacyNoticeVersion", privacyNoticeVersion, 1, 20), now, now, null, 0);
		account.signup = new SignupProfile(SignupProfile.Method.GOOGLE, null, null,
				Guard.text("googleSubject", googleSubject, 1, 255));
		account.register(new EmailVerified(id, now));
		return account;
	}

	public static UserAccount rehydrate(UUID id, AccountKind kind, Email email, @Nullable String passwordHash,
			String fullName, @Nullable String companyName, @Nullable Ruc ruc, @Nullable PhoneNumber phone,
			AccountStatus status, Set<Role> roles, @Nullable Instant emailVerifiedAt, int failedLoginAttempts,
			@Nullable Instant lockedUntil, int securityStamp, @Nullable Instant lastLoginAt,
			@Nullable String privacyNoticeVersion, Instant createdAt, Instant updatedAt, @Nullable Instant deletedAt,
			long version, SignupProfile signup) {
		UserAccount account = new UserAccount(id, kind, email, passwordHash, fullName, companyName, ruc, phone, status,
				roles.isEmpty() ? EnumSet.noneOf(Role.class) : roles, emailVerifiedAt, failedLoginAttempts, lockedUntil,
				securityStamp, lastLoginAt, privacyNoticeVersion, createdAt, updatedAt, deletedAt, version);
		account.signup = signup;
		return account;
	}

	// ----- alta -----

	/** Desde qué página se registró y si aceptó el boletín (solo al crear la cuenta). */
	public void recordSignupContext(@Nullable String source, boolean newsletter, Instant now) {
		this.signup = this.signup.withContext(SignupProfile.source(source), newsletter, now);
	}

	/**
	 * Ingreso con Google de una cuenta de cliente ya existente: queda vinculada a esa cuenta de Google y, como Google
	 * verificó el correo, una cuenta pendiente de verificación se activa.
	 */
	public void signInWithGoogle(String googleSubject, Instant now) {
		requireLive();
		if (this.kind != AccountKind.CUSTOMER) {
			throw new ForbiddenException("STAFF_USE_PANEL", "El personal ingresa por el panel interno.");
		}
		if (this.status == AccountStatus.DISABLED) {
			throw new UnauthorizedException("INVALID_CREDENTIALS", "No se pudo iniciar sesión con Google.");
		}
		String linked = this.signup.googleSubject();
		if (linked != null && !linked.equals(googleSubject)) {
			throw new UnauthorizedException("INVALID_CREDENTIALS", "No se pudo iniciar sesión con Google.");
		}
		if (linked == null) {
			this.signup = this.signup.withGoogle(Guard.text("googleSubject", googleSubject, 1, 255));
		}
		if (this.status == AccountStatus.PENDING_VERIFICATION) {
			this.status = AccountStatus.ACTIVE;
			this.emailVerifiedAt = now;
			register(new EmailVerified(this.id, now));
		}
		recordSuccessfulLogin(now);
	}

	// ----- inicio de sesión -----

	public boolean isLocked(Instant now) {
		return this.lockedUntil != null && this.lockedUntil.isAfter(now);
	}

	public void recordFailedLogin(Instant now, LoginPolicy policy) {
		this.failedLoginAttempts++;
		if (this.failedLoginAttempts >= policy.maxFailedAttempts()) {
			this.lockedUntil = now.plus(policy.lockDuration());
			this.failedLoginAttempts = 0;
			register(new AccountLocked(this.id, this.email.value(), this.fullName, this.lockedUntil, now));
		}
		this.updatedAt = now;
	}

	public void recordSuccessfulLogin(Instant now) {
		this.failedLoginAttempts = 0;
		this.lockedUntil = null;
		this.lastLoginAt = now;
		this.updatedAt = now;
	}

	/** El personal puede pedir un código de acceso si está activo o invitado, y no bloqueado. */
	public boolean canRequestLoginCode(Instant now) {
		return this.kind == AccountKind.STAFF && !isLocked(now)
				&& (this.status == AccountStatus.ACTIVE || this.status == AccountStatus.PENDING_VERIFICATION);
	}

	/**
	 * Acceso con el código enviado al correo: demuestra la propiedad del correo, así que el personal invitado queda
	 * activo y verificado con su primer ingreso.
	 */
	public void signInWithLoginCode(Instant now) {
		if (!canRequestLoginCode(now)) {
			throw new RuleViolationException("LOGIN_CODE_NOT_ALLOWED", "La cuenta no puede ingresar con código.");
		}
		if (this.status == AccountStatus.PENDING_VERIFICATION) {
			this.status = AccountStatus.ACTIVE;
			this.emailVerifiedAt = now;
			register(new EmailVerified(this.id, now));
		}
		recordSuccessfulLogin(now);
	}

	// ----- verificación y contraseña -----

	public void verifyEmail(Instant now) {
		if (this.status != AccountStatus.PENDING_VERIFICATION || this.passwordHash == null) {
			throw new ConflictException("ALREADY_VERIFIED", "La cuenta ya está verificada.");
		}
		this.status = AccountStatus.ACTIVE;
		this.emailVerifiedAt = now;
		this.updatedAt = now;
		register(new EmailVerified(this.id, now));
	}

	/** Aceptar la invitación del personal: define contraseña, verifica el correo y activa la cuenta. */
	public void acceptInvitation(String newPasswordHash, Instant now) {
		if (this.kind != AccountKind.STAFF || this.status != AccountStatus.PENDING_VERIFICATION) {
			throw new ConflictException("INVITATION_NOT_PENDING", "La invitación ya no está vigente.");
		}
		this.passwordHash = newPasswordHash;
		this.status = AccountStatus.ACTIVE;
		this.emailVerifiedAt = now;
		this.securityStamp++;
		this.updatedAt = now;
		register(new EmailVerified(this.id, now));
	}

	public void changePassword(String newPasswordHash, Instant now) {
		requireLive();
		this.passwordHash = newPasswordHash;
		this.securityStamp++;
		this.updatedAt = now;
		register(new PasswordChanged(this.id, this.email.value(), this.fullName, false, now));
	}

	/** Recuperación por correo: demuestra la propiedad del correo, así que también lo verifica y desbloquea. */
	public void resetPassword(String newPasswordHash, Instant now) {
		requireLive();
		if (this.status == AccountStatus.DISABLED) {
			throw new RuleViolationException("ACCOUNT_DISABLED", "La cuenta está desactivada.");
		}
		this.passwordHash = newPasswordHash;
		this.failedLoginAttempts = 0;
		this.lockedUntil = null;
		if (this.status == AccountStatus.PENDING_VERIFICATION) {
			this.status = AccountStatus.ACTIVE;
			this.emailVerifiedAt = now;
		}
		this.securityStamp++;
		this.updatedAt = now;
		register(new PasswordChanged(this.id, this.email.value(), this.fullName, true, now));
	}

	// ----- perfil -----

	public void updateProfile(String fullName, @Nullable String companyName, @Nullable Ruc ruc,
			@Nullable PhoneNumber phone, Instant now) {
		requireLive();
		this.fullName = Guard.text("fullName", fullName, 2, 120);
		this.companyName = Guard.optionalText("companyName", companyName, 160);
		this.ruc = ruc;
		this.phone = phone;
		this.updatedAt = now;
	}

	/** Corrección de datos hecha por el personal (queda en la auditoría con quién la hizo). */
	public void updateProfileByStaff(String fullName, @Nullable String companyName, @Nullable Ruc ruc,
			@Nullable PhoneNumber phone, UUID actorId, Instant now) {
		updateProfile(fullName, companyName, ruc, phone, now);
		register(new AccountUpdatedByStaff(this.id, actorId, now));
	}

	// ----- administración del personal -----

	public void changeRoles(Set<Role> newRoles, UUID actorId, Instant now) {
		requireLive();
		if (this.kind != AccountKind.STAFF) {
			throw new RuleViolationException("NOT_STAFF", "Solo se asignan roles al personal interno.");
		}
		this.roles = requireStaffRoles(newRoles);
		this.securityStamp++;
		this.updatedAt = now;
		register(new UserRolesChanged(this.id, this.roles.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()),
				actorId, now));
	}

	public void disable(UUID actorId, Instant now) {
		requireLive();
		if (this.status == AccountStatus.DISABLED) {
			throw new ConflictException("ALREADY_DISABLED", "La cuenta ya está desactivada.");
		}
		this.status = AccountStatus.DISABLED;
		this.securityStamp++;
		this.updatedAt = now;
		register(new UserStatusChanged(this.id, this.status.name(), actorId, now));
	}

	public void enable(UUID actorId, Instant now) {
		if (this.status != AccountStatus.DISABLED) {
			throw new ConflictException("NOT_DISABLED", "La cuenta no está desactivada.");
		}
		// El personal que ya verificó su correo entra con código: no necesita contraseña para volver a estar activo.
		boolean hasCredentials = this.passwordHash != null || (this.kind == AccountKind.STAFF && this.emailVerifiedAt != null);
		this.status = hasCredentials ? AccountStatus.ACTIVE : AccountStatus.PENDING_VERIFICATION;
		this.failedLoginAttempts = 0;
		this.lockedUntil = null;
		this.updatedAt = now;
		register(new UserStatusChanged(this.id, this.status.name(), actorId, now));
	}

	/**
	 * Eliminación a pedido del titular (Ley 29733): se anonimizan los datos personales de la cuenta. Las solicitudes y
	 * reclamaciones que presentó conservan su copia por obligación legal.
	 */
	public void delete(Instant now) {
		anonymize(null, now);
	}

	/** Eliminación hecha por el personal al atender la solicitud de supresión del titular. */
	public void deleteByStaff(UUID actorId, Instant now) {
		anonymize(actorId, now);
	}

	private void anonymize(@Nullable UUID actorId, Instant now) {
		requireLive();
		this.email = new Email("eliminada-" + this.id.toString().replace("-", "") + "@cuentas-eliminadas.invalid");
		this.fullName = "Cuenta eliminada";
		this.companyName = null;
		this.ruc = null;
		this.phone = null;
		this.passwordHash = null;
		this.signup = new SignupProfile(this.signup.method(), this.signup.source(), null, null);
		this.status = AccountStatus.DELETED;
		this.securityStamp++;
		this.deletedAt = now;
		this.updatedAt = now;
		register(new AccountDeleted(this.id, actorId, now));
	}

	public Set<Permission> permissions() {
		Set<Permission> permissions = EnumSet.noneOf(Permission.class);
		this.roles.forEach((role) -> permissions.addAll(role.permissions()));
		return permissions;
	}

	public boolean isAdministrator() {
		return this.roles.contains(Role.ADMINISTRATOR);
	}

	private void requireLive() {
		if (this.status == AccountStatus.DELETED) {
			throw new ConflictException("ACCOUNT_DELETED", "La cuenta fue eliminada.");
		}
	}

	private static Set<Role> requireStaffRoles(Set<Role> roles) {
		if (roles == null || roles.isEmpty()) {
			throw new ValidationException("roles", "REQUIRED", "Asigne al menos un rol.");
		}
		if (roles.stream().anyMatch((role) -> !role.isStaff())) {
			throw new ValidationException("roles", "INVALID_ROLE", "Los roles del personal no pueden incluir CUSTOMER.");
		}
		return EnumSet.copyOf(roles);
	}

	// ----- lectura -----

	public UUID id() {
		return this.id;
	}

	public AccountKind kind() {
		return this.kind;
	}

	public Email email() {
		return this.email;
	}

	public @Nullable String passwordHash() {
		return this.passwordHash;
	}

	public String fullName() {
		return this.fullName;
	}

	public @Nullable String companyName() {
		return this.companyName;
	}

	public @Nullable Ruc ruc() {
		return this.ruc;
	}

	public @Nullable PhoneNumber phone() {
		return this.phone;
	}

	public AccountStatus status() {
		return this.status;
	}

	public Set<Role> roles() {
		return this.roles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(this.roles);
	}

	public @Nullable Instant emailVerifiedAt() {
		return this.emailVerifiedAt;
	}

	public int failedLoginAttempts() {
		return this.failedLoginAttempts;
	}

	public @Nullable Instant lockedUntil() {
		return this.lockedUntil;
	}

	public int securityStamp() {
		return this.securityStamp;
	}

	public @Nullable Instant lastLoginAt() {
		return this.lastLoginAt;
	}

	public @Nullable String privacyNoticeVersion() {
		return this.privacyNoticeVersion;
	}

	public Instant createdAt() {
		return this.createdAt;
	}

	public Instant updatedAt() {
		return this.updatedAt;
	}

	public @Nullable Instant deletedAt() {
		return this.deletedAt;
	}

	public long version() {
		return this.version;
	}

	public SignupProfile signup() {
		return this.signup;
	}

}
