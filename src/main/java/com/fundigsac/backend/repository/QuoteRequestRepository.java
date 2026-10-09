package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.QuoteRequest;
import com.fundigsac.backend.model.enums.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, UUID> {
    Optional<QuoteRequest> findByReference(String reference);
    Page<QuoteRequest> findByStatus(QuoteStatus status, Pageable pageable);
    List<QuoteRequest> findByContactEmail(String contactEmail);
}
