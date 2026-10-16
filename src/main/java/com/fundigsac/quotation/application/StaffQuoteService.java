package com.fundigsac.quotation.application;

import java.time.Clock;
import java.util.Locale;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.StaffDirectory;
import com.fundigsac.quotation.application.QuoteViews.QuoteDetailView;
import com.fundigsac.quotation.domain.QuoteRepository;
import com.fundigsac.quotation.domain.QuoteRequest;
import com.fundigsac.quotation.domain.QuoteStatus;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;
import com.fundigsac.shared.security.Permission;

/** Atención de solicitudes por Ventas: asignación y cambios de estado con historial y bloqueo optimista. */
@Service
public class StaffQuoteService {

	private final QuoteRepository quotes;

	private final StaffDirectory staff;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final Clock clock;

	StaffQuoteService(QuoteRepository quotes, StaffDirectory staff, CurrentActor actor, DomainEvents events, Clock clock) {
		this.quotes = quotes;
		this.staff = staff;
		this.actor = actor;
		this.events = events;
		this.clock = clock;
	}

	@PreAuthorize(Authz.QUOTE_MANAGE)
	@Transactional
	public QuoteDetailView assign(UUID id, UUID assigneeId, long expectedVersion) {
		if (!this.staff.isActiveStaffWith(assigneeId, Permission.QUOTE_MANAGE)) {
			throw new ValidationException("assigneeId", "INVALID_ASSIGNEE", "La persona no puede atender cotizaciones.");
		}
		QuoteRequest quote = load(id, expectedVersion);
		quote.assign(assigneeId, this.actor.requireUserId(), this.clock.instant());
		return save(quote);
	}

	@PreAuthorize(Authz.QUOTE_MANAGE)
	@Transactional
	public QuoteDetailView changeStatus(UUID id, String status, @Nullable String note, long expectedVersion) {
		QuoteStatus to;
		try {
			to = QuoteStatus.valueOf(status == null ? "" : status.strip().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationException("status", "INVALID_STATUS", "Estado no válido.");
		}
		QuoteRequest quote = load(id, expectedVersion);
		quote.changeStatus(to, note, this.actor.requireUserId(), this.clock.instant());
		return save(quote);
	}

	private QuoteDetailView save(QuoteRequest quote) {
		this.quotes.update(quote);
		this.events.publishFrom(quote);
		QuoteRequest saved = this.quotes.findById(quote.id()).orElseThrow();
		return QuoteDetailView.of(saved, this.quotes.history(quote.id()));
	}

	private QuoteRequest load(UUID id, long expectedVersion) {
		QuoteRequest quote = this.quotes.findById(id)
			.orElseThrow(() -> new NotFoundException("QUOTE_NOT_FOUND", "La solicitud no existe."));
		if (quote.version() != expectedVersion) {
			throw new StaleVersionException();
		}
		return quote;
	}

}
