package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.CatalogChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CatalogChangeRequestRepository extends JpaRepository<CatalogChangeRequest, UUID> {
    List<CatalogChangeRequest> findByProductId(UUID productId);
}
