package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.LegalNotice;
import com.fundigsac.backend.model.enums.LegalNoticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LegalNoticeRepository extends JpaRepository<LegalNotice, UUID> {
    Optional<LegalNotice> findByNoticeTypeAndVersion(LegalNoticeType noticeType, String version);
    Optional<LegalNotice> findFirstByNoticeTypeOrderByPublishedAtDesc(LegalNoticeType noticeType);
}
