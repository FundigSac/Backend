package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.IdentityEvents.SignedIn;
import com.fundigsac.identity.domain.GoogleIdTokenVerifier;
import com.fundigsac.identity.domain.GoogleIdTokenVerifier.GoogleIdentity;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.UnauthorizedException;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Ingreso de clientes con Google. El frontend completa el flujo OAuth (PKCE) y entrega el ID token; aquí se valida con
 * las claves de Google y se abre la sesión de la API. Primer ingreso: crea la cuenta (activa, sin contraseña) y exige
 * la versión del aviso de privacidad aceptada. Una cuenta de correo existente queda vinculada y, si estaba pendiente,
 * verificada.
 */
@Service
public class GoogleSignInService {

	public record GoogleSignIn(String idToken, @Nullable String privacyNoticeVersion) {

		@Override
		public String toString() {
			return "GoogleSignIn[***]";
		}

	}

	private final GoogleIdTokenVerifier verifier;

	private final UserAccountRepository accounts;

	private final SessionIssuer sessions;

	private final DomainEvents events;

	private final Clock clock;

	GoogleSignInService(GoogleIdTokenVerifier verifier, UserAccountRepository accounts, SessionIssuer sessions,
			DomainEvents events, Clock clock) {
		this.verifier = verifier;
		this.accounts = accounts;
		this.sessions = sessions;
		this.events = events;
		this.clock = clock;
	}

	@Transactional
	public AuthTokens signIn(GoogleSignIn command) {
		if (!this.verifier.isEnabled()) {
			throw new RuleViolationException("GOOGLE_SIGN_IN_DISABLED", "El ingreso con Google no está habilitado.");
		}
		Instant now = this.clock.instant();
		GoogleIdentity identity = this.verifier.verify(command.idToken())
			.orElseThrow(() -> new UnauthorizedException("INVALID_GOOGLE_TOKEN", "No se pudo validar la cuenta de Google."));
		Email email = Email.of("email", identity.email());
		Optional<UserAccount> existing = this.accounts.findByGoogleSubject(identity.subject())
			.or(() -> this.accounts.findByEmail(email));

		UserAccount account;
		if (existing.isPresent()) {
			account = existing.get();
			account.signInWithGoogle(identity.subject(), now);
			this.accounts.update(account);
		}
		else {
			String version = command.privacyNoticeVersion();
			if (version == null || version.isBlank()) {
				throw new ValidationException("privacyNoticeVersion", "REQUIRED",
						"Indique la versión del aviso de privacidad aceptada.");
			}
			account = UserAccount.registerWithGoogle(Ids.newId(), email, identity.subject(), displayName(identity),
					version, now);
			account.recordSuccessfulLogin(now);
			this.accounts.insert(account);
		}
		this.events.publishFrom(account);
		this.events.publish(new SignedIn(account.id(), "GOOGLE", now));
		return this.sessions.issue(this.accounts.findById(account.id()).orElseThrow(), Ids.newId(), Ids.newId(), now);
	}

	/** El nombre de Google puede ser muy corto o muy largo para la cuenta: se ajusta a 2–120 caracteres. */
	private static String displayName(GoogleIdentity identity) {
		String name = identity.name().strip();
		if (name.length() < 2) {
			name = identity.email().substring(0, identity.email().indexOf('@'));
		}
		if (name.length() < 2) {
			name = "Cliente FUNDIGSAC";
		}
		return name.length() > 120 ? name.substring(0, 120) : name;
	}

}
