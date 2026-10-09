package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.ContentLocalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentLocalizationRepository extends JpaRepository<ContentLocalization, UUID> {
    Optional<ContentLocalization> findByEntityTypeAndEntityIdAndLocale(String entityType, String entityId, String locale);
}
