package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quote_status_events", indexes = {
        @Index(name = "idx_quote_status_ev", columnList = "quote_id, changed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteStatusEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "quote_id", nullable = false)
    private UUID quoteId;

    @Column(name = "from_status", length = 30)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 30)
    private String toStatus;

    @Column(name = "actor_ref", nullable = false, length = 120)
    private String actorRef;

    @Column(name = "changed_at", nullable = false)
    @Builder.Default
    private Instant changedAt = Instant.now();

    @Column(length = 500)
    private String note;
}
