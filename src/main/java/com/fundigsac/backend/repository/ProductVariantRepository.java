package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.ProductVariant;
import com.fundigsac.backend.model.enums.VariantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    List<ProductVariant> findByProductId(UUID productId);
    List<ProductVariant> findByProductIdAndStatus(UUID productId, VariantStatus status);
    Optional<ProductVariant> findByReferenceCode(String referenceCode);
}
