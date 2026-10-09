package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.FaqEntry;
import com.fundigsac.backend.model.enums.FaqStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaqEntryRepository extends JpaRepository<FaqEntry, UUID> {
    List<FaqEntry> findByStatus(FaqStatus status);
}
