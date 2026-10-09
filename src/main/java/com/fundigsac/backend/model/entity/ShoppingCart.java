package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.CartStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts", indexes = {
        @Index(name = "idx_cart_cust_status", columnList = "customer_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private CartStatus status = CartStatus.active;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
