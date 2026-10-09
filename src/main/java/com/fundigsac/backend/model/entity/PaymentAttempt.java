package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.PaymentAttemptStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts", indexes = {
        @Index(name = "idx_pay_attempt_idemp", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_pay_attempt_prov_ref", columnList = "provider, provider_payment_ref")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String provider = "IZIPAY";

    @Column(name = "provider_payment_ref", length = 180)
    private String providerPaymentRef;

    @Column(name = "idempotency_key", nullable = false, length = 160, unique = true)
    private String idempotencyKey;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private PaymentAttemptStatus status = PaymentAttemptStatus.created;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
