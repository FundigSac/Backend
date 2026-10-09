package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.ConsentChoice;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consent_evidence", indexes = {
        @Index(name = "idx_consent_purpose_rec", columnList = "purpose, recorded_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "notice_version", nullable = false, length = 60)
    private String noticeVersion;

    @Column(nullable = false, length = 90)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConsentChoice choice;

    @Column(name = "subject_ref", length = 120)
    private String subjectRef;

    @Column(name = "recorded_at", nullable = false)
    @Builder.Default
    private Instant recordedAt = Instant.now();

    @Column(name = "evidence_method", nullable = false, length = 80)
    private String evidenceMethod;
}
