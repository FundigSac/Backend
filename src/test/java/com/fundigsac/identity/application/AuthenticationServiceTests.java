package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.PasswordHasher;
import com.fundigsac.identity.domain.RefreshToken;
import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.identity.domain.Role;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.DomainException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.ForbiddenException;
import com.fundigsac.shared.domain.SecureTokens;
import com.fundigsac.shared.domain.UnauthorizedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Decisiones del inicio de sesión y de la rotación de sesiones que el flujo HTTP no permite provocar por sí solo. */
class AuthenticationServiceTests {

	private static final Instant NOW = Instant.parse("2026-11-09T15:00:00Z");

	private static final String RAW_REFRESH = "refresh-token-de-prueba";

	private final UserAccountRepository accounts = mock(UserAccountRepository.class);

	private final RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);

	private final PasswordHasher hasher = mock(PasswordHasher.class);

	private final SessionIssuer sessions = mock(SessionIssuer.class);

	private final DomainEvents events = mock(DomainEvents.class);

	private AuthenticationService service;

	private final AuthTokens issued = new AuthTokens("jwt", 900, "refresh", 1200);

	@BeforeEach
	void setUp() {
		IdentityProperties properties = mock(IdentityProperties.class, Answers.RETURNS_DEEP_STUBS);
		when(properties.login().maxFailedAttempts()).thenReturn(5);
		when(properties.login().lockDuration()).thenReturn(Duration.ofMinutes(15));
		this.service = new AuthenticationService(this.accounts, this.refreshTokens, this.hasher, this.sessions, this.events, properties,
				Clock.fixed(NOW, ZoneOffset.UTC));
		when(this.sessions.issue(any(), any(), any(), any())).thenReturn(this.issued);
	}

	private static String codeOf(Throwable thrown) {
		return ((DomainException) thrown).code();
	}

	private static UserAccount customer(AccountStatus status) {
		UserAccount account = UserAccount.registerCustomer(UUID.randomUUID(), new Email("cliente@empresa.pe"), "hash", "Cliente Uno", null,
				null, null, "2026-10", NOW);
		if (status == AccountStatus.ACTIVE) {
			account.verifyEmail(NOW);
		}
		if (status == AccountStatus.DISABLED) {
			account.verifyEmail(NOW);
			account.disable(UUID.randomUUID(), NOW);
		}
		if (status == AccountStatus.DELETED) {
			account.delete(NOW);
		}
		account.pullEvents();
		return account;
	}

	private void known(UserAccount account) {
		when(this.accounts.findByEmail(any())).thenReturn(Optional.of(account));
	}

	private RefreshToken storedToken(UUID userId, Instant expiresAt, Instant revokedAt) {
		RefreshToken token = new RefreshToken(UUID.randomUUID(), userId, UUID.randomUUID(), SecureTokens.sha256(RAW_REFRESH), NOW.minusSeconds(60),
				expiresAt, revokedAt, null);
		when(this.refreshTokens.findByHash(SecureTokens.sha256(RAW_REFRESH))).thenReturn(Optional.of(token));
		return token;
	}

	@Test
	void anUnknownEmailStillCostsAHashCalculation() {
		when(this.accounts.findByEmail(any())).thenReturn(Optional.empty());
		assertThatThrownBy(() -> this.service.login("nadie@empresa.pe", "clave-mala")).isInstanceOf(UnauthorizedException.class)
			.satisfies((ex) -> assertThat(codeOf(ex)).isEqualTo("INVALID_CREDENTIALS"));
		verify(this.hasher).simulateVerification("clave-mala");
		verify(this.hasher, never()).matches(anyString(), anyString());
	}

	@Test
	void aMalformedEmailStillCostsAHashCalculation() {
		assertThatThrownBy(() -> this.service.login("no-es-correo", "clave-mala")).isInstanceOf(UnauthorizedException.class);
		assertThatThrownBy(() -> this.service.login(null, "clave-mala")).isInstanceOf(UnauthorizedException.class);
		verify(this.hasher, org.mockito.Mockito.times(2)).simulateVerification("clave-mala");
		verify(this.accounts, never()).findByEmail(any());
	}

	@Test
	void disabledDeletedAndLockedAccountsStillCostAHashCalculation() {
		UserAccount locked = customer(AccountStatus.ACTIVE);
		for (int i = 0; i < 5; i++) {
			locked.recordFailedLogin(NOW, new com.fundigsac.identity.domain.LoginPolicy(5, Duration.ofMinutes(15)));
		}
		for (UserAccount account : new UserAccount[] { customer(AccountStatus.DISABLED), customer(AccountStatus.DELETED), locked }) {
			known(account);
			assertThatThrownBy(() -> this.service.login("cliente@empresa.pe", "clave")).isInstanceOf(UnauthorizedException.class);
		}
		verify(this.hasher, org.mockito.Mockito.times(3)).simulateVerification("clave");
		verify(this.hasher, never()).matches(anyString(), any());
	}

	@Test
	void aPasswordlessAccountGoesThroughTheHasherAndIsNotPenalized() {
		UserAccount staff = UserAccount.inviteStaff(UUID.randomUUID(), new Email("personal@fundigsac.pe"), "Personal Uno", java.util.Set.of(Role.SALES), NOW);
		known(staff);
		when(this.hasher.matches("clave", null)).thenReturn(false);

		assertThatThrownBy(() -> this.service.login("personal@fundigsac.pe", "clave")).isInstanceOf(UnauthorizedException.class);

		verify(this.hasher).matches("clave", null);
		assertThat(staff.failedLoginAttempts()).isZero();
		verify(this.accounts, never()).update(any());
	}

	@Test
	void aWrongPasswordIsCountedAndTheFifthLocks() {
		UserAccount account = customer(AccountStatus.ACTIVE);
		known(account);
		when(this.hasher.matches("clave", "hash")).thenReturn(false);

		assertThatThrownBy(() -> this.service.login("cliente@empresa.pe", "clave")).isInstanceOf(UnauthorizedException.class);
		assertThat(account.failedLoginAttempts()).isEqualTo(1);
		verify(this.accounts).update(account);

		for (int i = 0; i < 4; i++) {
			assertThatThrownBy(() -> this.service.login("cliente@empresa.pe", "clave")).isInstanceOf(UnauthorizedException.class);
		}
		assertThat(account.isLocked(NOW)).isTrue();
	}

	@Test
	void anUnverifiedAccountWithTheRightPasswordIsToldToVerify() {
		known(customer(AccountStatus.PENDING_VERIFICATION));
		when(this.hasher.matches("clave", "hash")).thenReturn(true);
		assertThatThrownBy(() -> this.service.login("cliente@empresa.pe", "clave")).isInstanceOf(ForbiddenException.class)
			.satisfies((ex) -> assertThat(codeOf(ex)).isEqualTo("EMAIL_NOT_VERIFIED"));
	}

	@Test
	void theRightPasswordOpensASession() {
		UserAccount account = customer(AccountStatus.ACTIVE);
		known(account);
		when(this.hasher.matches("clave", "hash")).thenReturn(true);
		assertThat(this.service.login("cliente@empresa.pe", "clave")).isEqualTo(this.issued);
		assertThat(account.lastLoginAt()).isEqualTo(NOW);
		verify(this.accounts).update(account);
	}

	@Test
	void anExpiredRefreshTokenIsNotRotated() {
		storedToken(UUID.randomUUID(), NOW.minusSeconds(1), null);
		assertThatThrownBy(() -> this.service.refresh(RAW_REFRESH)).isInstanceOf(UnauthorizedException.class)
			.satisfies((ex) -> assertThat(codeOf(ex)).isEqualTo("INVALID_SESSION"));
		verify(this.refreshTokens, never()).rotate(any(), any(), any());
		verify(this.sessions, never()).issue(any(), any(), any(), any());
	}

	@Test
	void aReusedRefreshTokenRevokesTheWholeFamily() {
		RefreshToken token = storedToken(UUID.randomUUID(), NOW.plusSeconds(3600), NOW.minusSeconds(10));
		assertThatThrownBy(() -> this.service.refresh(RAW_REFRESH)).isInstanceOf(UnauthorizedException.class);
		verify(this.refreshTokens).revokeFamily(token.familyId(), NOW);
	}

	@Test
	void losingTheRotationRaceRevokesTheFamily() {
		UserAccount account = customer(AccountStatus.ACTIVE);
		RefreshToken token = storedToken(account.id(), NOW.plusSeconds(3600), null);
		when(this.accounts.findById(account.id())).thenReturn(Optional.of(account));
		when(this.refreshTokens.rotate(eq(token.id()), any(), eq(NOW))).thenReturn(false);

		assertThatThrownBy(() -> this.service.refresh(RAW_REFRESH)).isInstanceOf(UnauthorizedException.class);

		verify(this.refreshTokens).revokeFamily(token.familyId(), NOW);
		verify(this.sessions, never()).issue(any(), any(), any(), any());
	}

	@Test
	void aRefreshTokenOfAnInactiveAccountIsRejected() {
		UserAccount account = customer(AccountStatus.DISABLED);
		storedToken(account.id(), NOW.plusSeconds(3600), null);
		when(this.accounts.findById(account.id())).thenReturn(Optional.of(account));
		assertThatThrownBy(() -> this.service.refresh(RAW_REFRESH)).isInstanceOf(UnauthorizedException.class);
		verify(this.refreshTokens, never()).rotate(any(), any(), any());
	}

	@Test
	void aValidRefreshTokenRotatesWithinItsFamily() {
		UserAccount account = customer(AccountStatus.ACTIVE);
		RefreshToken token = storedToken(account.id(), NOW.plusSeconds(3600), null);
		when(this.accounts.findById(account.id())).thenReturn(Optional.of(account));
		when(this.refreshTokens.rotate(eq(token.id()), any(), eq(NOW))).thenReturn(true);
		assertThat(this.service.refresh(RAW_REFRESH)).isEqualTo(this.issued);
		verify(this.sessions).issue(eq(account), eq(token.familyId()), any(), eq(NOW));
	}

	@Test
	void malformedRefreshTokensNeverReachTheStore() {
		for (String raw : new String[] { null, "   ", "x".repeat(129) }) {
			assertThatThrownBy(() -> this.service.refresh(raw)).isInstanceOf(UnauthorizedException.class);
			this.service.logout(raw);
		}
		verify(this.refreshTokens, never()).findByHash(anyString());
	}

	@Test
	void logoutRevokesOnlyAKnownToken() {
		when(this.refreshTokens.findByHash(anyString())).thenReturn(Optional.empty());
		this.service.logout(RAW_REFRESH);
		verify(this.refreshTokens, never()).revokeFamily(any(), any());

		RefreshToken token = storedToken(UUID.randomUUID(), NOW.plusSeconds(3600), null);
		this.service.logout(RAW_REFRESH);
		verify(this.refreshTokens).revokeFamily(token.familyId(), NOW);
	}

}
