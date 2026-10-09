package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "technical_documents", indexes = {
        @Index(name = "idx_tech_doc_storage", columnList = "storage_key", unique = true),
        @Index(name = "idx_tech_doc_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 240)
    private String title;

    @Column(name = "storage_key", nullable = false, length = 400, unique = true)
    private String storageKey;

    @Column(name = "version_label", length = 80)
    private String versionLabel;

    @Column(name = "issued_at")
    private LocalDate issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.pending;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Column(name = "approved_at")
    private Instant approvedAt;
}
