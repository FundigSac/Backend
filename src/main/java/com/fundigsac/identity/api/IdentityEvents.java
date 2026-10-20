package com.fundigsac.identity.api;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.AuditableEvent;
import com.fundigsac.shared.domain.DomainEvent;

/**
 * Eventos del módulo de identidad. Los que llevan un token de un solo uso existen solo para que el módulo de
 * notificaciones envíe el enlace: el registro de eventos se borra al completarse y el token caduca en minutos u horas.
 */
public final class IdentityEvents {

	private IdentityEvents() {
	}

	/** Enviar enlace de verificación de correo (registro o reenvío). */
	public record VerificationEmailRequested(UUID userId, String email, String fullName, String token, Instant expiresAt,
			Instant occurredAt) implements DomainEvent {

		@Override
		public String toString() {
			return "VerificationEmailRequested[userId=" + this.userId + "]";
		}

	}

	/** Alguien intentó registrarse con un correo que ya tiene cuenta (se avisa al dueño, sin revelarlo a terceros). */
	public record RegistrationAttemptOnExistingAccount(UUID userId, String email, String fullName, Instant occurredAt)
			implements DomainEvent {
	}

	/** Enviar enlace de recuperación de contraseña. */
	public record PasswordResetRequested(UUID userId, String email, String fullName, String token, Instant expiresAt,
			Instant occurredAt) implements DomainEvent {

		@Override
		public String toString() {
			return "PasswordResetRequested[userId=" + this.userId + "]";
		}

	}

	/** Enviar invitación para que un miembro del personal defina su contraseña. */
	public record StaffInvited(UUID userId, String email, String fullName, String token, Instant expiresAt,
			@Nullable UUID actorId, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "STAFF_INVITED";
		}

		@Override
		public String auditResourceType() {
			return "USER";
		}

		@Override
		public String auditResourceId() {
			return this.userId.toString();
		}

		@Override
		public String toString() {
			return "StaffInvited[userId=" + this.userId + "]";
		}

	}

	/** Enviar el código de acceso al panel interno (personal, sin contraseña). */
	public record StaffLoginCodeRequested(UUID userId, String email, String fullName, String code, Instant expiresAt,
			Instant occurredAt) implements DomainEvent {

		@Override
		public String toString() {
			return "StaffLoginCodeRequested[userId=" + this.userId + "]";
		}

	}

	/** Inicio de sesión correcto (contraseña, código del personal o Google). */
	public record SignedIn(UUID userId, String method, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "SIGNED_IN";
		}

		@Override
		public String auditResourceType() {
			return "USER";
		}

		@Override
		public String auditResourceId() {
			return this.userId.toString();
		}

		@Override
		public @Nullable UUID actorId() {
			return this.userId;
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("method", this.method);
		}

	}

	public record EmailVerified(UUID userId, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "EMAIL_VERIFIED";
		}

		@Override
		public String auditResourceType() {
			return "USER";
		}

		@Override
		public String auditResourceId() {
			return this.userId.toString();
		}

		@Override
		public @Nullable UUID actorId() {
			return this.userId;
		}

	}

	/** La contraseña cambió (por el usuario o por recuperación): se avisa al dueño por correo. */
	public record PasswordChanged(UUID userId, String email, String fullName, boolean viaReset, Instant occurredAt)
			implements AuditableEvent {

		@Override
		public String auditAction() {
			return this.viaReset ? "PASSWORD_RESET" : "PASSWORD_CHANGED";
		}

		@Override
		public String auditResourceType() {
			return "USER";
		}

		@Override
		public String auditResourceId() {
			return this.userId.toString();
		}

		@Override
		public @Nullable UUID actorId() {
			return this.userId;
		}

	}

	/** Bloqueo temporal por intentos fallidos: se avisa al dueño (posible ataque). */
	public record AccountLocked(UUID userId, String email, String fullName, Instant lockedUntil, Instant occurredAt)
			implements AuditableEvent {

		@Override
		public String auditAction() {
			return "ACCOUNT_LOCKED";
		}

		@Override
		public String auditResourceType() {
			return "USER";
		}

		@Override
		public String auditResourceId() {
			return this.userId.toString();
		}

		@Override
		public @Nullable UUID actorId() {
			return null;
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("lockedUntil", this.lockedUntil.toString());
		}

	}

	/** El personal corrigió los datos de perfil de una cuenta. */
	public record AccountUpdatedByStaff(UUID userId, UUID actorId, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "USER_UPDATED";
		}

		@Override
		public String auditResourceType() {
			return "USER";
		}

		@Override
		public String auditResourceId() {
			return this.userId.toString();
		}

	}

	public record UserRolesChanged(UUID userId, Set<String> roles, UUID actorId, Instant occurredAt)
			implements AuditableEvent {

		@Override
		public String auditAction() {
			return "USER_ROLES_CHANGED";
		}

		@Override
		public String auditResourceType() {
			return "USER";
		}

		@Override
		public String auditResourceId() {
			return this.userId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("roles", String.join(",", this.roles.stream().sorted().toList()));
		}

	}

	public record UserStatusChanged(UUID userId, String status, UUID actorId, Instant occurredAt)
			implements AuditableEvent {

		@Override
		public String auditAction() {
			return "USER_STATUS_CHANGED";
		}

		@Override
		public String auditResourceType() {
			return "USER";
		}

		@Override
		public String auditResourceId() {
			return this.userId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("status", this.status);
		}

	}

	/** El cliente eliminó su cuenta: los módulos que guardan datos suyos los retiran (p. ej. el carrito). */
	/** Cuenta anonimizada a pedido del titular (deletedBy nulo) o por el personal que atendió su solicitud. */
	public record AccountDeleted(UUID userId, @Nullable UUID deletedBy, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "ACCOUNT_DELETED";
		}

		@Override
		public String auditResourceType() {
			return "USER";
		}

		@Override
		public String auditResourceId() {
			return this.userId.toString();
		}

		@Override
		public @Nullable UUID actorId() {
			return this.deletedBy == null ? this.userId : this.deletedBy;
		}

	}

}
