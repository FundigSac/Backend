package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    Optional<MediaAsset> findByStorageKey(String storageKey);
}
