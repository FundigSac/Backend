package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipping_quotes", indexes = {
        @Index(name = "idx_ship_quote_exp", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Column(nullable = false, length = 100)
    private String method;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "PEN";

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "provider_ref", length = 120)
    private String providerRef;
}
