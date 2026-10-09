package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "content_localizations", indexes = {
        @Index(name = "idx_content_loc_unique", columnList = "entity_type, entity_id, locale", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentLocalization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 120)
    private String entityId;

    @Column(nullable = false, length = 12)
    @Builder.Default
    private String locale = "es-PE";

    @Column(name = "localized_payload", nullable = false, columnDefinition = "TEXT")
    private String localizedPayload;

    @Column(name = "approved_at")
    private Instant approvedAt;
}
