package com.fundigsac.identity.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.StaffDirectory;
import com.fundigsac.identity.domain.AccountKind;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.security.Permission;

/** Implementación del contrato público StaffDirectory. */
@Service
class StaffDirectoryService implements StaffDirectory {

	private final UserAccountRepository accounts;

	StaffDirectoryService(UserAccountRepository accounts) {
		this.accounts = accounts;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isActiveStaffWith(UUID userId, Permission permission) {
		return this.accounts.findById(userId)
			.filter((a) -> a.kind() == AccountKind.STAFF && a.status() == AccountStatus.ACTIVE)
			.map((a) -> a.permissions().contains(permission))
			.orElse(false);
	}

}
