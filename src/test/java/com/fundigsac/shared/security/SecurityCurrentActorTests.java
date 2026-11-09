package com.fundigsac.shared.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

/** El actor sale solo de un JWT válido: un anónimo o un sujeto malformado nunca se convierte en un usuario. */
class SecurityCurrentActorTests {

	private final SecurityCurrentActor actor = new SecurityCurrentActor();

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
	}

	private static void authenticate(String subject, String... authorities) {
		Jwt.Builder jwt = Jwt.withTokenValue("t").header("alg", "RS256").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).claim("x", "y");
		if (subject != null) {
			jwt.subject(subject);
		}
		SecurityContextHolder.getContext()
			.setAuthentication(new JwtAuthenticationToken(jwt.build(), List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList()));
	}

	@Test
	void readsTheUserFromTheToken() {
		UUID id = UUID.randomUUID();
		authenticate(id.toString());
		assertThat(this.actor.userId()).contains(id);
	}

	@Test
	void aMissingOrMalformedSubjectIsNoUser() {
		authenticate("no-es-un-uuid");
		assertThat(this.actor.userId()).isEmpty();
		authenticate(null);
		assertThat(this.actor.userId()).isEmpty();
	}

	@Test
	void anAnonymousOrAbsentAuthenticationIsNoUser() {
		assertThat(this.actor.userId()).isEmpty();
		assertThat(this.actor.has(Permission.AUDIT_READ)).isFalse();
		SecurityContextHolder.getContext()
			.setAuthentication(new AnonymousAuthenticationToken("clave", "anonimo", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
		assertThat(this.actor.userId()).isEmpty();
	}

	@Test
	void checksOnlyTheExactPermission() {
		authenticate(UUID.randomUUID().toString(), "quote:read", "ROLE_STAFF");
		assertThat(this.actor.has(Permission.QUOTE_READ)).isTrue();
		assertThat(this.actor.has(Permission.QUOTE_MANAGE)).isFalse();
	}

}
