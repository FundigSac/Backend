package com.fundigsac.complaint.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Puerto de persistencia del Libro de Reclamaciones. */
public interface ComplaintRepository {

	void insert(Complaint complaint);

	void update(Complaint complaint);

	Optional<Complaint> findById(UUID id);

	Optional<Complaint> findByFolio(String folio);

	List<Complaint.Event> history(UUID complaintId);

	/** @param overdueAsOf si no es nulo, solo hojas abiertas con plazo vencido a esa fecha */
	List<Complaint> search(@Nullable String status, @Nullable LocalDate overdueAsOf, @Nullable String query, int limit,
			long offset);

	long count(@Nullable String status, @Nullable LocalDate overdueAsOf, @Nullable String query);

}
