package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.Inquiry;
import com.fundigsac.backend.model.enums.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {
    Optional<Inquiry> findByReference(String reference);
    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);
}
