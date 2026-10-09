package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.Complaint;
import com.fundigsac.backend.model.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
    Optional<Complaint> findByReference(String reference);
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
}
