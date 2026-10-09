package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.ComplaintStatus;
import com.fundigsac.backend.model.enums.ComplaintType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "complaints", indexes = {
        @Index(name = "idx_complaints_ref", columnList = "reference", unique = true),
        @Index(name = "idx_complaints_due", columnList = "status, due_at"),
        @Index(name = "idx_complaints_sub", columnList = "submitted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_type", nullable = false, length = 32)
    @Builder.Default
    private ComplaintType complaintType = ComplaintType.reclamo;

    @Column(name = "consumer_name", nullable = false, length = 200)
    private String consumerName;

    @Column(name = "consumer_contact", nullable = false, length = 254)
    private String consumerContact;

    @Column(name = "consumer_doc_type", length = 24)
    private String consumerDocType;

    @Column(name = "consumer_doc_number", length = 32)
    private String consumerDocNumber;

    @Column(name = "product_service", columnDefinition = "TEXT")
    private String productService;

    @Column(name = "incident_description", nullable = false, columnDefinition = "TEXT")
    private String incidentDescription;

    @Column(name = "request_description", nullable = false, columnDefinition = "TEXT")
    private String requestDescription;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private Instant submittedAt = Instant.now();

    @Column(name = "due_at")
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.received;

    @Column(name = "privacy_notice_version", nullable = false, length = 60)
    @Builder.Default
    private String privacyNoticeVersion = "v1.0-PE";
}
