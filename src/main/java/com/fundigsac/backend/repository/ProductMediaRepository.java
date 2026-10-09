package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, UUID> {
    List<ProductMedia> findByProductIdOrderBySortOrderAsc(UUID productId);
}
