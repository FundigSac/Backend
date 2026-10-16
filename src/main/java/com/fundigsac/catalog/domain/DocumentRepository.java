package com.fundigsac.catalog.domain;

import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de documentos técnicos. */
public interface DocumentRepository {

	Optional<TechnicalDocument> findById(UUID id);

	boolean slugTaken(String slug, UUID excludingId);

	void insert(TechnicalDocument document);

	void update(TechnicalDocument document);

}
