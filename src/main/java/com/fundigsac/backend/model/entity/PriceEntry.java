package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "price_entries", indexes = {
        @Index(name = "idx_price_var_curr_valid", columnList = "variant_id, currency, valid_from")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "PEN";

    @Column(name = "net_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal netPrice;

    @Column(name = "tax_class", nullable = false, length = 80)
    @Builder.Default
    private String taxClass = "IGV_18";

    @Column(name = "valid_from", nullable = false)
    @Builder.Default
    private Instant validFrom = Instant.now();

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "approved_at", nullable = false)
    @Builder.Default
    private Instant approvedAt = Instant.now();
}
