package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.ComplaintEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "complaint_events", indexes = {
        @Index(name = "idx_comp_ev_occurred", columnList = "complaint_id, occurred_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "complaint_id", nullable = false)
    private UUID complaintId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    @Builder.Default
    private ComplaintEventType eventType = ComplaintEventType.created;

    @Column(name = "actor_ref", nullable = false, length = 120)
    private String actorRef;

    @Column(name = "occurred_at", nullable = false)
    @Builder.Default
    private Instant occurredAt = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "response_document_key", length = 400)
    private String responseDocumentKey;
}
