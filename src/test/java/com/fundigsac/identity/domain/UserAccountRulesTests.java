package com.fundigsac.identity.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.ForbiddenException;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.UnauthorizedException;
import com.fundigsac.shared.domain.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Transiciones de la cuenta que no se alcanzan fácilmente por HTTP. */
class UserAccountRulesTests {

	private static final Instant NOW = Instant.parse("2026-11-09T15:00:00Z");

	private static final LoginPolicy POLICY = new LoginPolicy(5, Duration.ofMinutes(15));

	private static final UUID ACTOR = UUID.randomUUID();

	private static UserAccount customer() {
		return UserAccount.registerCustomer(UUID.randomUUID(), new Email("cliente@empresa.pe"), "hash", "Cliente Uno", null, null, null, "2026-10", NOW);
	}

	private static UserAccount staff() {
		return UserAccount.inviteStaff(UUID.randomUUID(), new Email("personal@fundigsac.pe"), "Personal Uno", Set.of(Role.SALES), NOW);
	}

	private static UserAccount google() {
		return UserAccount.registerWithGoogle(UUID.randomUUID(), new Email("g@gmail.com"), "g-1", "Cuenta Google", "2026-10", NOW);
	}

	private static void lock(UserAccount account) {
		for (int i = 0; i < POLICY.maxFailedAttempts(); i++) {
			account.recordFailedLogin(NOW, POLICY);
		}
	}

	@Test
	void aPasswordLockDoesNotStopTheStaffCode() {
		UserAccount admin = UserAccount.bootstrapAdministrator(UUID.randomUUID(), new Email("a@fundigsac.pe"), "Admin Uno", "hash", NOW);
		lock(admin);
		assertThat(admin.isLocked(NOW)).isTrue();
		assertThat(admin.canRequestLoginCode()).isTrue();

		admin.signInWithLoginCode(NOW);

		assertThat(admin.isLocked(NOW)).isFalse();
		assertThat(admin.failedLoginAttempts()).isZero();
	}

	@Test
	void aLockExpiresOnItsOwn() {
		UserAccount account = customer();
		lock(account);
		assertThat(account.isLocked(NOW.plus(Duration.ofMinutes(15)))).isFalse();
		assertThat(account.isLocked(NOW.plus(Duration.ofMinutes(14)))).isTrue();
	}

	@Test
	void customersDisabledAndDeletedStaffCannotUseTheCode() {
		assertThat(customer().canRequestLoginCode()).isFalse();
		UserAccount disabled = staff();
		disabled.disable(ACTOR, NOW);
		assertThat(disabled.canRequestLoginCode()).isFalse();
		assertThatThrownBy(() -> disabled.signInWithLoginCode(NOW)).isInstanceOf(RuleViolationException.class);
		UserAccount deleted = staff();
		deleted.delete(NOW);
		assertThat(deleted.canRequestLoginCode()).isFalse();
	}

	@Test
	void anActiveStaffMemberKeepsItsStatusWhenSigningInWithTheCode() {
		UserAccount admin = UserAccount.bootstrapAdministrator(UUID.randomUUID(), new Email("a@fundigsac.pe"), "Admin Uno", "hash", NOW);
		admin.signInWithLoginCode(NOW);
		assertThat(admin.status()).isEqualTo(AccountStatus.ACTIVE);
	}

	@Test
	void hasPasswordReflectsTheStoredHash() {
		assertThat(customer().hasPassword()).isTrue();
		assertThat(staff().hasPassword()).isFalse();
		assertThat(google().hasPassword()).isFalse();
	}

	@Test
	void googleSignInRespectsAccountStateAndLinkedSubject() {
		UserAccount disabled = customer();
		disabled.verifyEmail(NOW);
		disabled.disable(ACTOR, NOW);
		assertThatThrownBy(() -> disabled.signInWithGoogle("g-1", NOW)).isInstanceOf(UnauthorizedException.class);

		UserAccount linked = google();
		assertThatThrownBy(() -> linked.signInWithGoogle("g-otra", NOW)).isInstanceOf(UnauthorizedException.class);
		linked.signInWithGoogle("g-1", NOW);
		assertThat(linked.lastLoginAt()).isEqualTo(NOW);

		assertThatThrownBy(() -> staff().signInWithGoogle("g-1", NOW)).isInstanceOf(ForbiddenException.class);
		UserAccount deleted = customer();
		deleted.delete(NOW);
		assertThatThrownBy(() -> deleted.signInWithGoogle("g-1", NOW)).isInstanceOf(ConflictException.class);
	}

	@Test
	void verifyEmailOnlyAppliesToPendingAccountsWithPassword() {
		UserAccount verified = customer();
		verified.verifyEmail(NOW);
		assertThatThrownBy(() -> verified.verifyEmail(NOW)).isInstanceOf(ConflictException.class);
		assertThatThrownBy(() -> staff().verifyEmail(NOW)).isInstanceOf(ConflictException.class);
	}

	@Test
	void invitationsOnlyApplyToPendingStaff() {
		assertThatThrownBy(() -> customer().acceptInvitation("hash", NOW)).isInstanceOf(ConflictException.class);
		UserAccount accepted = staff();
		accepted.acceptInvitation("hash", NOW);
		assertThatThrownBy(() -> accepted.acceptInvitation("hash", NOW)).isInstanceOf(ConflictException.class);
	}

	@Test
	void resetPasswordHonoursDisabledAndActivatesPending() {
		UserAccount disabled = customer();
		disabled.disable(ACTOR, NOW);
		assertThatThrownBy(() -> disabled.resetPassword("nuevo", NOW)).isInstanceOf(RuleViolationException.class);

		UserAccount pending = customer();
		pending.resetPassword("nuevo", NOW);
		assertThat(pending.status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(pending.emailVerifiedAt()).isEqualTo(NOW);

		UserAccount active = customer();
		active.verifyEmail(NOW);
		lock(active);
		active.resetPassword("nuevo", NOW);
		assertThat(active.isLocked(NOW)).isFalse();
		assertThat(active.status()).isEqualTo(AccountStatus.ACTIVE);
	}

	@Test
	void rolesOnlyChangeForLiveStaffAndNeverIncludeCustomer() {
		assertThatThrownBy(() -> customer().changeRoles(Set.of(Role.SALES), ACTOR, NOW)).isInstanceOf(RuleViolationException.class);
		assertThatThrownBy(() -> staff().changeRoles(Set.of(), ACTOR, NOW)).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> staff().changeRoles(Set.of(Role.CUSTOMER), ACTOR, NOW)).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> UserAccount.inviteStaff(UUID.randomUUID(), new Email("x@fundigsac.pe"), "Personal Dos", null, NOW))
			.isInstanceOf(ValidationException.class);
		UserAccount deleted = staff();
		deleted.delete(NOW);
		assertThatThrownBy(() -> deleted.changeRoles(Set.of(Role.SALES), ACTOR, NOW)).isInstanceOf(ConflictException.class);
	}

	@Test
	void disablingTwiceConflictsAndEnablingRequiresDisabled() {
		UserAccount account = customer();
		account.disable(ACTOR, NOW);
		assertThatThrownBy(() -> account.disable(ACTOR, NOW)).isInstanceOf(ConflictException.class);
		assertThatThrownBy(() -> customer().enable(ACTOR, NOW)).isInstanceOf(ConflictException.class);
	}

	@Test
	void enablingRestoresActiveOnlyWhenTheAccountHasCredentials() {
		UserAccount withPassword = customer();
		withPassword.disable(ACTOR, NOW);
		withPassword.enable(ACTOR, NOW);
		assertThat(withPassword.status()).isEqualTo(AccountStatus.ACTIVE);

		UserAccount verifiedStaff = staff();
		verifiedStaff.signInWithLoginCode(NOW);
		verifiedStaff.disable(ACTOR, NOW);
		verifiedStaff.enable(ACTOR, NOW);
		assertThat(verifiedStaff.status()).isEqualTo(AccountStatus.ACTIVE);

		UserAccount invited = staff();
		invited.disable(ACTOR, NOW);
		invited.enable(ACTOR, NOW);
		assertThat(invited.status()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
	}

	@Test
	void aDeletedAccountLosesItsPersonalDataAndRejectsChanges() {
		UserAccount account = customer();
		account.delete(NOW);
		assertThat(account.email().value()).startsWith("eliminada-");
		assertThat(account.fullName()).isEqualTo("Cuenta eliminada");
		assertThat(account.passwordHash()).isNull();
		assertThat(account.status()).isEqualTo(AccountStatus.DELETED);
		assertThatThrownBy(() -> account.delete(NOW)).isInstanceOf(ConflictException.class);
		assertThatThrownBy(() -> account.changePassword("otro", NOW)).isInstanceOf(ConflictException.class);
		assertThatThrownBy(() -> account.updateProfile("Otro Nombre", null, null, null, NOW)).isInstanceOf(ConflictException.class);
	}

	@Test
	void rehydrationToleratesAnAccountWithoutRoles() {
		UserAccount account = UserAccount.rehydrate(UUID.randomUUID(), AccountKind.CUSTOMER, new Email("a@empresa.pe"), "hash", "Nombre Uno", null, null, null,
				AccountStatus.ACTIVE, Set.of(), NOW, 0, null, 0, null, "2026-10", NOW, NOW, null, 3, SignupProfile.email());
		assertThat(account.roles()).isEmpty();
		assertThat(account.permissions()).isEmpty();
		assertThat(account.version()).isEqualTo(3);
	}

}
