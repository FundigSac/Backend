package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.ImportBatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "catalog_import_batches", indexes = {
        @Index(name = "idx_import_batch_status_created", columnList = "status, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_file_key", nullable = false, length = 400)
    private String sourceFileKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ImportBatchStatus status = ImportBatchStatus.uploaded;

    @Column(name = "created_by", nullable = false, length = 120)
    private String createdBy;

    @Column(name = "validation_report_key", length = 400)
    private String validationReportKey;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
