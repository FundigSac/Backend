package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.TechnicalDocument;
import com.fundigsac.backend.model.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechnicalDocumentRepository extends JpaRepository<TechnicalDocument, UUID> {
    List<TechnicalDocument> findByStatus(DocumentStatus status);
}
