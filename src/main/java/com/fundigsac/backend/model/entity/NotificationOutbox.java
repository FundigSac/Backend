package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_outbox", indexes = {
        @Index(name = "idx_outbox_key", columnList = "event_key", unique = true),
        @Index(name = "idx_outbox_status_next", columnList = "status, next_attempt_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_key", nullable = false, length = 120, unique = true)
    private String eventKey;

    @Column(name = "template_key", nullable = false, length = 80)
    private String templateKey;

    @Column(nullable = false, length = 254)
    private String recipient;

    @Column(name = "payload_ref", nullable = false, length = 150)
    private String payloadRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.pending;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
