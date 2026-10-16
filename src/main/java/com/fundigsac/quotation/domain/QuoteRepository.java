package com.fundigsac.quotation.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de solicitudes de cotización. */
public interface QuoteRepository {

	void insert(QuoteRequest quote);

	/** Guarda estado, responsable e historial nuevo; StaleVersionException si la versión cambió. */
	void update(QuoteRequest quote);

	Optional<QuoteRequest> findById(UUID id);

	Optional<QuoteRequest> findByFolio(String folio);

	List<StatusChange> history(UUID quoteId);

}
