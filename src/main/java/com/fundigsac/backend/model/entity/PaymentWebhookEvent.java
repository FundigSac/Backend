package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.WebhookProcessingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_webhook_events", indexes = {
        @Index(name = "idx_webhook_event_unique", columnList = "provider, provider_event_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String provider = "IZIPAY";

    @Column(name = "provider_event_id", nullable = false, length = 180)
    private String providerEventId;

    @Column(name = "signature_valid", nullable = false)
    @Builder.Default
    private Boolean signatureValid = false;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 32)
    @Builder.Default
    private WebhookProcessingStatus processingStatus = WebhookProcessingStatus.received;

    @Column(name = "received_at", nullable = false)
    @Builder.Default
    private Instant receivedAt = Instant.now();
}
