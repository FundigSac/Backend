package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.TechnicalAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechnicalAttributeRepository extends JpaRepository<TechnicalAttribute, UUID> {
    Optional<TechnicalAttribute> findByCode(String code);
    List<TechnicalAttribute> findByFilterableTrue();
}
