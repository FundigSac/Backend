package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_categories", indexes = {
        @Index(name = "idx_prod_cat_slug", columnList = "slug", unique = true),
        @Index(name = "idx_prod_cat_parent_sort", columnList = "parent_id, sort_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;
}
