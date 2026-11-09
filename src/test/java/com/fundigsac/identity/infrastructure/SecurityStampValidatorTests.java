package com.fundigsac.identity.infrastructure;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.identity.domain.UserAccountRepository.SecurityState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Sello de seguridad con la caché real de producción: revoca tokens viejos sin rechazar sesiones recién abiertas. */
class SecurityStampValidatorTests {

	private final UUID userId = UUID.randomUUID();

	private final UserAccountRepository accounts = mock(UserAccountRepository.class);

	private SecurityStampValidator validator;

	@BeforeEach
	void setUp() {
		IdentityProperties properties = mock(IdentityProperties.class);
		when(properties.jwt()).thenReturn(new IdentityProperties.Jwt("emisor", "audiencia", Duration.ofMinutes(15), null, Duration.ofSeconds(10)));
		this.validator = new SecurityStampValidator(this.accounts, properties);
	}

	private Jwt token(Object stamp) {
		Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "RS256").subject(this.userId.toString());
		return (stamp == null ? builder.claim("otro", 1) : builder.claim("sv", stamp)).build();
	}

	private void accountIs(int stamp, AccountStatus status) {
		when(this.accounts.findSecurityState(this.userId)).thenReturn(Optional.of(new SecurityState(stamp, status)));
	}

	private boolean accepted(Object stamp) {
		return !this.validator.validate(token(stamp)).hasErrors();
	}

	@Test
	void repeatedValidationsUseTheCache() {
		accountIs(3, AccountStatus.ACTIVE);
		assertThat(accepted(3)).isTrue();
		assertThat(accepted(3)).isTrue();
		verify(this.accounts, times(1)).findSecurityState(this.userId);
	}

	@Test
	void aTokenIssuedAfterTheCachedStampIsAccepted() {
		accountIs(3, AccountStatus.ACTIVE);
		assertThat(accepted(3)).isTrue();
		// La contraseña cambió y el cliente volvió a entrar: su token nuevo llega antes de que venza la caché.
		accountIs(4, AccountStatus.ACTIVE);
		assertThat(accepted(4)).isTrue();
	}

	@Test
	void anOlderTokenIsRejectedWithoutExtraQueries() {
		accountIs(5, AccountStatus.ACTIVE);
		assertThat(accepted(5)).isTrue();
		assertThat(accepted(4)).isFalse();
		assertThat(accepted(1)).isFalse();
		verify(this.accounts, times(1)).findSecurityState(this.userId);
	}

	@Test
	void aTokenFromAReactivatedAccountIsAccepted() {
		accountIs(6, AccountStatus.DISABLED);
		assertThat(accepted(5)).isFalse();
		// Reactivar no cambia el sello: el token emitido tras reactivar lleva el mismo sello que la caché desactivada.
		accountIs(6, AccountStatus.ACTIVE);
		assertThat(accepted(6)).isTrue();
	}

	@Test
	void aDisabledAccountStaysRejectedEvenWithTheCurrentStamp() {
		accountIs(7, AccountStatus.DISABLED);
		assertThat(accepted(7)).isFalse();
	}

	@Test
	void unknownAccountsAndTokensWithoutStampAreRejected() {
		when(this.accounts.findSecurityState(this.userId)).thenReturn(Optional.empty());
		assertThat(accepted(1)).isFalse();
		assertThat(accepted(null)).isFalse();
		assertThat(accepted("texto")).isFalse();
	}

	@Test
	void aTokenWithoutAUuidSubjectIsRejected() {
		Jwt token = Jwt.withTokenValue("token").header("alg", "RS256").subject("no-es-uuid").claim("sv", 1).build();
		assertThat(this.validator.validate(token).hasErrors()).isTrue();
	}

}
