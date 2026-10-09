package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.PageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "site_pages", indexes = {
        @Index(name = "idx_site_pages_slug", columnList = "slug", unique = true),
        @Index(name = "idx_site_pages_status_pub", columnList = "status, published_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SitePage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(name = "body_blocks", columnDefinition = "TEXT")
    private String bodyBlocks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private PageStatus status = PageStatus.draft;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
