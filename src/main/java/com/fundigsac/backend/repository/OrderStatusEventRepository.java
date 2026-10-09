package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.OrderStatusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderStatusEventRepository extends JpaRepository<OrderStatusEvent, UUID> {
    List<OrderStatusEvent> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
