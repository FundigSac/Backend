package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {
    Optional<PaymentAttempt> findByIdempotencyKey(String idempotencyKey);
    List<PaymentAttempt> findByOrderId(UUID orderId);
}
