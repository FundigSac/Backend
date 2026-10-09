package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.VariantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variant_prod_status", columnList = "product_id, status"),
        @Index(name = "idx_variant_prod_ref", columnList = "product_id, reference_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(length = 32)
    private String dn;

    @Column(length = 32)
    private String pn;

    @Column(length = 32)
    private String sdr;

    @Column(length = 120)
    private String material;

    @Column(length = 24)
    private String uom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private VariantStatus status = VariantStatus.active;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
