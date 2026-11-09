package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.PasswordHasher;
import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.identity.domain.Role;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.DomainException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.RuleViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Siempre debe quedar un administrador activo. Por HTTP solo otro administrador puede intentarlo, así que el caso límite
 * (una carrera entre dos administradores) se prueba directamente sobre las reglas.
 */
class LastAdministratorRulesTests {

	private static final Instant NOW = Instant.parse("2026-11-09T15:00:00Z");

	private static final UUID ACTOR = UUID.randomUUID();

	private final UserAccountRepository accounts = mock(UserAccountRepository.class);

	private final RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);

	private final CurrentActor actor = mock(CurrentActor.class);

	private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

	private AccountAdministrationService administration;

	private StaffUserService staffUsers;

	private final UserAccount admin = UserAccount.bootstrapAdministrator(UUID.randomUUID(), new Email("otro@fundigsac.pe"), "Otro Admin", "hash", NOW);

	@BeforeEach
	void setUp() {
		IdentityProperties properties = mock(IdentityProperties.class, Answers.RETURNS_DEEP_STUBS);
		OneTimeTokens tokens = mock(OneTimeTokens.class);
		DomainEvents events = mock(DomainEvents.class);
		this.administration = new AccountAdministrationService(this.accounts, this.refreshTokens, tokens, this.actor, events, properties, this.clock);
		this.staffUsers = new StaffUserService(this.accounts, this.refreshTokens, tokens, this.actor, events, properties, this.clock);
		when(this.actor.requireUserId()).thenReturn(ACTOR);
		when(this.accounts.findById(this.admin.id())).thenReturn(Optional.of(this.admin));
	}

	private static String codeOf(Throwable thrown) {
		return ((DomainException) thrown).code();
	}

	private void assertLastAdministrator(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
		assertThatThrownBy(action).isInstanceOf(RuleViolationException.class)
			.satisfies((ex) -> assertThat(codeOf(ex)).isEqualTo("LAST_ADMINISTRATOR"));
		verify(this.accounts, never()).update(any());
	}

	@Test
	void theLastActiveAdministratorCannotBeDisabledFromTheDirectory() {
		when(this.accounts.countActiveAdministrators()).thenReturn(1L);
		assertLastAdministrator(() -> this.administration.disable(this.admin.id(), 0));
	}

	@Test
	void theLastActiveAdministratorCannotBeDeleted() {
		when(this.accounts.countActiveAdministrators()).thenReturn(1L);
		assertLastAdministrator(() -> this.administration.delete(this.admin.id(), 0));
	}

	@Test
	void theLastActiveAdministratorCannotBeDisabledFromTheStaffList() {
		when(this.accounts.countActiveAdministrators()).thenReturn(1L);
		assertLastAdministrator(() -> this.staffUsers.disable(this.admin.id(), 0));
	}

	@Test
	void theLastActiveAdministratorCannotLoseTheRole() {
		when(this.accounts.countActiveAdministrators()).thenReturn(1L);
		assertLastAdministrator(() -> this.staffUsers.changeRoles(this.admin.id(), Set.of(Role.SALES), 0));
	}

	@Test
	void withAnotherAdministratorTheActionsProceed() {
		when(this.accounts.countActiveAdministrators()).thenReturn(2L);
		this.administration.disable(this.admin.id(), 0);
		assertThat(this.admin.status()).isEqualTo(AccountStatus.DISABLED);
		verify(this.refreshTokens).revokeAllForUser(this.admin.id(), NOW);
	}

	@Test
	void aDisabledAdministratorDoesNotNeedAReplacement() {
		this.admin.disable(UUID.randomUUID(), NOW);
		this.admin.pullEvents();
		this.administration.delete(this.admin.id(), 0);
		assertThat(this.admin.status()).isEqualTo(AccountStatus.DELETED);
		verify(this.accounts, never()).countActiveAdministrators();
	}

	@Test
	void nobodyCanDisableOrDeleteTheirOwnAccount() {
		when(this.actor.requireUserId()).thenReturn(this.admin.id());
		assertThatThrownBy(() -> this.administration.disable(this.admin.id(), 0)).satisfies((ex) -> assertThat(codeOf(ex)).isEqualTo("CANNOT_DISABLE_SELF"));
		assertThatThrownBy(() -> this.administration.delete(this.admin.id(), 0)).satisfies((ex) -> assertThat(codeOf(ex)).isEqualTo("CANNOT_DELETE_SELF"));
		assertThatThrownBy(() -> this.staffUsers.disable(this.admin.id(), 0)).satisfies((ex) -> assertThat(codeOf(ex)).isEqualTo("CANNOT_DISABLE_SELF"));
		assertThatThrownBy(() -> this.staffUsers.changeRoles(this.admin.id(), Set.of(Role.SALES), 0))
			.satisfies((ex) -> assertThat(codeOf(ex)).isEqualTo("CANNOT_DEMOTE_SELF"));
	}

	@Test
	void keepingTheAdministratorRoleNeedsNoReplacement() {
		this.staffUsers.changeRoles(this.admin.id(), Set.of(Role.ADMINISTRATOR, Role.SALES), 0);
		assertThat(this.admin.roles()).contains(Role.SALES);
		verify(this.accounts, never()).countActiveAdministrators();
	}

	@Test
	void bootstrapCreatesTheFirstAdministratorOnlyWhenNeeded() {
		PasswordHasher hasher = mock(PasswordHasher.class);
		when(hasher.hash("clave-inicial-larga-2026")).thenReturn("hash-inicial");
		IdentityProperties properties = mock(IdentityProperties.class, Answers.RETURNS_DEEP_STUBS);
		BootstrapAdministrator bootstrap = new BootstrapAdministrator(this.accounts, hasher, properties, this.clock);
		IdentityProperties.BootstrapAdmin configured = new IdentityProperties.BootstrapAdmin("inicio@fundigsac.pe", "Administrador", "clave-inicial-larga-2026\r\n");

		for (IdentityProperties.BootstrapAdmin incomplete : new IdentityProperties.BootstrapAdmin[] {
				new IdentityProperties.BootstrapAdmin(null, "A", "x"), new IdentityProperties.BootstrapAdmin(" ", "A", "x"),
				new IdentityProperties.BootstrapAdmin("a@b.pe", "A", null), new IdentityProperties.BootstrapAdmin("a@b.pe", "A", " ") }) {
			when(properties.bootstrapAdmin()).thenReturn(incomplete);
			bootstrap.run(null);
		}
		verify(this.accounts, never()).countActiveAdministrators();

		when(properties.bootstrapAdmin()).thenReturn(configured);
		when(this.accounts.countActiveAdministrators()).thenReturn(1L);
		bootstrap.run(null);
		verify(this.accounts, never()).insert(any());

		when(this.accounts.countActiveAdministrators()).thenReturn(0L);
		when(this.accounts.existsByEmail(any())).thenReturn(true);
		bootstrap.run(null);
		verify(this.accounts, never()).insert(any());

		when(this.accounts.existsByEmail(any())).thenReturn(false);
		bootstrap.run(null);
		verify(this.accounts).insert(any());
		verify(hasher).hash("clave-inicial-larga-2026");
	}

}
