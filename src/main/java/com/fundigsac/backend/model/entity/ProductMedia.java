package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_media", indexes = {
        @Index(name = "idx_prod_media_unique", columnList = "product_id, media_id", unique = true),
        @Index(name = "idx_prod_media_sort", columnList = "product_id, sort_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "media_id", nullable = false)
    private UUID mediaId;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_cover", nullable = false)
    @Builder.Default
    private Boolean isCover = false;
}
