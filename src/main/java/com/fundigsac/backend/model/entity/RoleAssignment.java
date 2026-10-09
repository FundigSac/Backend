package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "role_assignments", indexes = {
        @Index(name = "idx_role_subject_code", columnList = "subject_id, role_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "subject_id", nullable = false, length = 120)
    private String subjectId;

    @Column(name = "role_code", nullable = false, length = 60)
    private String roleCode;

    @Column(name = "granted_by", nullable = false, length = 120)
    private String grantedBy;

    @Column(name = "granted_at", nullable = false)
    @Builder.Default
    private Instant grantedAt = Instant.now();

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
