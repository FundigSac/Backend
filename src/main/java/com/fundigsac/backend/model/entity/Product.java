package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.CommercialMode;
import com.fundigsac.backend.model.enums.ProductVisibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_slug", columnList = "slug", unique = true),
        @Index(name = "idx_products_cat_vis", columnList = "category_id, visibility"),
        @Index(name = "idx_products_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "internal_code", length = 100)
    private String internalCode;

    @Column(nullable = false, length = 220)
    private String name;

    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "technical_description", columnDefinition = "TEXT")
    private String technicalDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ProductVisibility visibility = ProductVisibility.draft;

    @Enumerated(EnumType.STRING)
    @Column(name = "commercial_mode", nullable = false, length = 32)
    @Builder.Default
    private CommercialMode commercialMode = CommercialMode.quote_only;

    @Column(name = "approved_by", length = 120)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
