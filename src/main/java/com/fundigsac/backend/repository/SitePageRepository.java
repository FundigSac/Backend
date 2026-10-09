package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.SitePage;
import com.fundigsac.backend.model.enums.PageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SitePageRepository extends JpaRepository<SitePage, UUID> {
    Optional<SitePage> findBySlug(String slug);
    List<SitePage> findByStatus(PageStatus status);
}
