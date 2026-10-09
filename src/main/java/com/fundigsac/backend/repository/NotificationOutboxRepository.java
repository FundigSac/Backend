package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.NotificationOutbox;
import com.fundigsac.backend.model.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {
    Optional<NotificationOutbox> findByEventKey(String eventKey);
    List<NotificationOutbox> findByStatus(OutboxStatus status);
}
