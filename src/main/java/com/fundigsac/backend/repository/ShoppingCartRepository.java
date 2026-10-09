package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.ShoppingCart;
import com.fundigsac.backend.model.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {
    Optional<ShoppingCart> findFirstByCustomerIdAndStatus(UUID customerId, CartStatus status);
}
