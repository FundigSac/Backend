package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.CartLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartLineRepository extends JpaRepository<CartLine, UUID> {
    List<CartLine> findByCartId(UUID cartId);
    Optional<CartLine> findByCartIdAndVariantId(UUID cartId, UUID variantId);
}
