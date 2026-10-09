package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.QuoteSource;
import com.fundigsac.backend.model.enums.QuoteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quote_requests", indexes = {
        @Index(name = "idx_quotes_ref", columnList = "reference", unique = true),
        @Index(name = "idx_quotes_status_created", columnList = "status, created_at"),
        @Index(name = "idx_quotes_staff", columnList = "assigned_staff_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 40, unique = true)
    private String reference;

    @Column(name = "contact_name", nullable = false, length = 160)
    private String contactName;

    @Column(name = "contact_email", nullable = false, length = 254)
    private String contactEmail;

    @Column(name = "company_name", length = 180)
    private String companyName;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private QuoteStatus status = QuoteStatus.received;

    @Column(name = "assigned_staff_id", length = 100)
    private String assignedStaffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private QuoteSource source = QuoteSource.general;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
