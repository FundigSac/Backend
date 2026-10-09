package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.LegalNoticeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "legal_notices", indexes = {
        @Index(name = "idx_legal_notice_unique", columnList = "notice_type, version", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false, length = 32)
    private LegalNoticeType noticeType;

    @Column(nullable = false, length = 60)
    private String version;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "approved_by", length = 120)
    private String approvedBy;
}
