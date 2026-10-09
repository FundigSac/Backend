package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.MediaApprovalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "media_assets", indexes = {
        @Index(name = "idx_media_storage_key", columnList = "storage_key", unique = true),
        @Index(name = "idx_media_approval", columnList = "approval_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "storage_key", nullable = false, length = 400, unique = true)
    private String storageKey;

    @Column(name = "mime_type", nullable = false, length = 90)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "alt_text", length = 280)
    private String altText;

    @Column(name = "license_ref", length = 260)
    private String licenseRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 32)
    @Builder.Default
    private MediaApprovalStatus approvalStatus = MediaApprovalStatus.pending;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;
}
