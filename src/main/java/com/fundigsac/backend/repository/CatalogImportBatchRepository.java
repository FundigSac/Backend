package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.CatalogImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CatalogImportBatchRepository extends JpaRepository<CatalogImportBatch, UUID> {
}
