package com.fundigsac.backend.repository;

import com.fundigsac.backend.model.entity.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, UUID> {
    Optional<CustomerAccount> findByEmail(String email);
}
