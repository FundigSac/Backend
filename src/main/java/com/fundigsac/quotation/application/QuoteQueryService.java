package com.fundigsac.quotation.application;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.quotation.application.QuoteViews.QuoteDetailView;
import com.fundigsac.quotation.application.QuoteViews.QuoteReceiptView;
import com.fundigsac.quotation.application.QuoteViews.QuoteSummaryView;
import com.fundigsac.quotation.domain.QuoteRepository;
import com.fundigsac.quotation.domain.QuoteRequest;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.application.ReceiptTokens;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.security.Authz;

/**
 * Consultas: constancia pública con token, solicitudes propias del cliente (autorización por objeto: una solicitud
 * ajena responde 404, igual que una inexistente) y lectura del personal.
 */
@Service
@Transactional(readOnly = true)
public class QuoteQueryService {

	private final QuoteRepository quotes;

	private final QuoteReadModel readModel;

	private final ReceiptTokens receipts;

	private final CurrentActor actor;

	QuoteQueryService(QuoteRepository quotes, QuoteReadModel readModel, ReceiptTokens receipts, CurrentActor actor) {
		this.quotes = quotes;
		this.readModel = readModel;
		this.receipts = receipts;
		this.actor = actor;
	}

	public QuoteReceiptView receipt(String folio, @Nullable String token) {
		String normalized = Folio.parse(folio).value();
		if (!this.receipts.matches(QuoteSubmissionService.RECEIPT_PURPOSE, normalized, token)) {
			throw notFound();
		}
		return QuoteReceiptView.of(this.quotes.findByFolio(normalized).orElseThrow(QuoteQueryService::notFound));
	}

	@PreAuthorize(Authz.CUSTOMER)
	public PageResult<QuoteSummaryView> mine(PageQuery page) {
		return this.readModel.ofCustomer(this.actor.requireUserId(), page);
	}

	@PreAuthorize(Authz.CUSTOMER)
	public QuoteDetailView mineByFolio(String folio) {
		UUID me = this.actor.requireUserId();
		QuoteRequest quote = this.quotes.findByFolio(Folio.parse(folio).value())
			.filter((q) -> q.belongsTo(me))
			.orElseThrow(QuoteQueryService::notFound);
		return QuoteDetailView.of(quote, this.quotes.history(quote.id()));
	}

	@PreAuthorize(Authz.QUOTE_READ)
	public PageResult<QuoteSummaryView> inbox(QuoteReadModel.StaffFilter filter, PageQuery page) {
		return this.readModel.staffInbox(filter, page);
	}

	@PreAuthorize(Authz.QUOTE_READ)
	public QuoteDetailView detail(UUID id) {
		QuoteRequest quote = this.quotes.findById(id).orElseThrow(QuoteQueryService::notFound);
		return QuoteDetailView.of(quote, this.quotes.history(id));
	}

	private static NotFoundException notFound() {
		return new NotFoundException("QUOTE_NOT_FOUND", "La solicitud no existe.");
	}

}
