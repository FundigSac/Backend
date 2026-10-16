package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.IdentityEvents.RegistrationAttemptOnExistingAccount;
import com.fundigsac.identity.api.IdentityEvents.VerificationEmailRequested;
import com.fundigsac.identity.domain.AccountKind;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.OneTimeToken;
import com.fundigsac.identity.domain.PasswordHasher;
import com.fundigsac.identity.domain.PasswordPolicy;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;

/**
 * Registro de clientes con verificación de correo. La respuesta es siempre la misma exista o no la cuenta (evita
 * enumerar correos); si ya existía, se avisa al dueño por correo.
 */
@Service
public class RegistrationService {

	public record RegisterCustomer(String email, String password, String fullName, @Nullable String companyName,
			@Nullable String ruc, @Nullable String phone, String privacyNoticeVersion) {

		@Override
		public String toString() {
			return "RegisterCustomer[***]";
		}

	}

	private final UserAccountRepository accounts;

	private final PasswordHasher hasher;

	private final OneTimeTokens tokens;

	private final DomainEvents events;

	private final IdentityProperties properties;

	private final Clock clock;

	RegistrationService(UserAccountRepository accounts, PasswordHasher hasher, OneTimeTokens tokens, DomainEvents events,
			IdentityProperties properties, Clock clock) {
		this.accounts = accounts;
		this.hasher = hasher;
		this.tokens = tokens;
		this.events = events;
		this.properties = properties;
		this.clock = clock;
	}

	@Transactional
	public void register(RegisterCustomer command) {
		Instant now = this.clock.instant();
		Email email = Email.of("email", command.email());
		PasswordPolicy.check("password", command.password(), email.value());
		Ruc ruc = Ruc.optional("ruc", command.ruc());
		PhoneNumber phone = PhoneNumber.optional("phone", command.phone());
		var existing = this.accounts.findByEmail(email);
		if (existing.isPresent()) {
			this.hasher.hash(command.password()); // mismo costo de tiempo que un registro real
			UserAccount owner = existing.get();
			this.events.publish(new RegistrationAttemptOnExistingAccount(owner.id(), owner.email().value(),
					owner.fullName(), now));
			return;
		}
		UserAccount account = UserAccount.registerCustomer(Ids.newId(), email, this.hasher.hash(command.password()),
				command.fullName(), command.companyName(), ruc, phone, command.privacyNoticeVersion(), now);
		this.accounts.insert(account);
		requestVerification(account, now);
	}

	@Transactional
	public void verifyEmail(String rawToken) {
		Instant now = this.clock.instant();
		OneTimeToken token = this.tokens.redeem(rawToken, OneTimeToken.Purpose.EMAIL_VERIFICATION, now);
		UserAccount account = this.accounts.findById(token.userId()).orElseThrow();
		account.verifyEmail(now);
		this.accounts.update(account);
		this.events.publishFrom(account);
	}

	/** Reenvía el enlace solo si la cuenta de cliente sigue pendiente; la respuesta no cambia en ningún caso. */
	@Transactional
	public void resendVerification(String rawEmail) {
		Instant now = this.clock.instant();
		Email email;
		try {
			email = new Email(rawEmail);
		}
		catch (RuntimeException ex) {
			return;
		}
		this.accounts.findByEmail(email)
			.filter((a) -> a.kind() == AccountKind.CUSTOMER && a.status() == AccountStatus.PENDING_VERIFICATION)
			.ifPresent((account) -> requestVerification(account, now));
	}

	private void requestVerification(UserAccount account, Instant now) {
		OneTimeTokens.Issued issued = this.tokens.issue(account, OneTimeToken.Purpose.EMAIL_VERIFICATION,
				this.properties.tokens().emailVerificationTtl(), now);
		this.events.publish(new VerificationEmailRequested(account.id(), account.email().value(), account.fullName(),
				issued.rawToken(), issued.expiresAt(), now));
	}

}
