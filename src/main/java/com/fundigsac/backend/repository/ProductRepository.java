package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.Product;
import com.fundigsac.backend.model.enums.CommercialMode;
import com.fundigsac.backend.model.enums.ProductVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySlug(String slug);
    Page<Product> findByVisibility(ProductVisibility visibility, Pageable pageable);
    Page<Product> findByCategoryIdAndVisibility(UUID categoryId, ProductVisibility visibility, Pageable pageable);
    List<Product> findByCommercialMode(CommercialMode commercialMode);
}
