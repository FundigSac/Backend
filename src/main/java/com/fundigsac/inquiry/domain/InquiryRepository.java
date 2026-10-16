package com.fundigsac.inquiry.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Puerto de persistencia de consultas. */
public interface InquiryRepository {

	void insert(Inquiry inquiry);

	void update(Inquiry inquiry);

	Optional<Inquiry> findById(UUID id);

	Optional<Inquiry> findByFolio(String folio);

	List<Inquiry.Event> history(UUID inquiryId);

	List<Inquiry> search(@Nullable String status, @Nullable String query, int limit, long offset);

	long count(@Nullable String status, @Nullable String query);

}
