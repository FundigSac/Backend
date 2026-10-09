package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analytics_events", indexes = {
        @Index(name = "idx_analytics_name_created", columnList = "event_name, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_name", nullable = false, length = 90)
    private String eventName;

    @Column(name = "subject_hash", length = 64)
    private String subjectHash;

    @Column(name = "entity_ref", length = 120)
    private String entityRef;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "safe_props", columnDefinition = "TEXT")
    private String safeProps;
}
