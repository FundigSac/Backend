package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, UUID> {
    List<RoleAssignment> findBySubjectIdAndRevokedAtIsNull(String subjectId);
}
