package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "quote_items", indexes = {
        @Index(name = "idx_quote_items_quote", columnList = "quote_id"),
        @Index(name = "idx_quote_items_prod", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "quote_id", nullable = false)
    private UUID quoteId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "product_name_snapshot", nullable = false, length = 220)
    private String productNameSnapshot;

    @Column(name = "reference_snapshot", length = 100)
    private String referenceSnapshot;

    @Column(nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "uom_snapshot", length = 24)
    private String uomSnapshot;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
