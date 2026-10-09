package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_positions", indexes = {
        @Index(name = "idx_inv_var_loc", columnList = "variant_id, location_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Column(name = "location_code", nullable = false, length = 80)
    @Builder.Default
    private String locationCode = "ALMACEN_CENTRAL_LIMA";

    @Column(name = "available_qty", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal availableQty = BigDecimal.ZERO;

    @Column(name = "as_of", nullable = false)
    @Builder.Default
    private Instant asOf = Instant.now();

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;
}
