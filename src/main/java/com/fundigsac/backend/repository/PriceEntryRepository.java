package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.PriceEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceEntryRepository extends JpaRepository<PriceEntry, UUID> {
    Optional<PriceEntry> findFirstByVariantIdOrderByValidFromDesc(UUID variantId);
    List<PriceEntry> findByVariantId(UUID variantId);
}
