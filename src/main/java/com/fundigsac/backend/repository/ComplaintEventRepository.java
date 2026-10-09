package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.ComplaintEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintEventRepository extends JpaRepository<ComplaintEvent, UUID> {
    List<ComplaintEvent> findByComplaintIdOrderByOccurredAtAsc(UUID complaintId);
}
