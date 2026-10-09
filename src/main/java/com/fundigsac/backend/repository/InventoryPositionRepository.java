package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.InventoryPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryPositionRepository extends JpaRepository<InventoryPosition, UUID> {
    Optional<InventoryPosition> findByVariantIdAndLocationCode(UUID variantId, String locationCode);
    List<InventoryPosition> findByVariantId(UUID variantId);
}
