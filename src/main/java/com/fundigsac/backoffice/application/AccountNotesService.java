package com.fundigsac.backoffice.application;

import java.time.Clock;
import java.util.UUID;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.backoffice.application.AccountViews.AccountRow;
import com.fundigsac.identity.api.IdentityEvents.AccountDeleted;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;

/** Notas internas del personal sobre una cuenta (hasta 2000 caracteres). Se borran al anonimizar la cuenta. */
@Service
public class AccountNotesService {

	private static final int MAX_LENGTH = 2000;

	private final AccountNotesStore notes;

	private final AccountReadModel accounts;

	private final CurrentActor actor;

	private final Clock clock;

	AccountNotesService(AccountNotesStore notes, AccountReadModel accounts, CurrentActor actor, Clock clock) {
		this.notes = notes;
		this.accounts = accounts;
		this.actor = actor;
		this.clock = clock;
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public AccountRow save(UUID userId, String text) {
		String value = text.strip();
		if (value.length() > MAX_LENGTH) {
			throw new ValidationException("notes", "TOO_LONG", "La nota admite hasta 2000 caracteres.");
		}
		this.accounts.account(userId).orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "La cuenta no existe."));
		this.notes.save(userId, value, this.actor.requireUserId(), this.clock.instant());
		return this.accounts.account(userId).orElseThrow();
	}

	/** Las notas pueden tener datos personales: no sobreviven a la supresión de la cuenta. */
	@ApplicationModuleListener
	public void on(AccountDeleted event) {
		this.notes.delete(event.userId());
	}

}
