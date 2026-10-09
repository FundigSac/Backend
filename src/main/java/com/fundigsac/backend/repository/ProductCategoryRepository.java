package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    Optional<ProductCategory> findBySlug(String slug);
    List<ProductCategory> findByPublishedTrueOrderBySortOrderAsc();
    List<ProductCategory> findByParentId(UUID parentId);
}
