package com.fundigsac.identity.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.CustomerDirectory;
import com.fundigsac.identity.domain.AccountKind;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.UserAccountRepository;

/** Implementación del contrato público CustomerDirectory (solo clientes activos, solo datos de contacto). */
@Service
class CustomerDirectoryService implements CustomerDirectory {

	private final UserAccountRepository accounts;

	CustomerDirectoryService(UserAccountRepository accounts) {
		this.accounts = accounts;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CustomerContact> findActiveCustomer(UUID customerId) {
		return this.accounts.findById(customerId)
			.filter((a) -> a.kind() == AccountKind.CUSTOMER && a.status() == AccountStatus.ACTIVE)
			.map((a) -> new CustomerContact(a.id(), a.fullName(), a.email().value(), a.companyName(),
					a.ruc() == null ? null : a.ruc().value(), a.phone() == null ? null : a.phone().value()));
	}

}
