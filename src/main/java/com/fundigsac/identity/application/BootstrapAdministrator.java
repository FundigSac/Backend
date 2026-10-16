package com.fundigsac.identity.application;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.domain.PasswordHasher;
import com.fundigsac.identity.domain.PasswordPolicy;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Ids;

/**
 * Crea el primer administrador al arrancar si todavía no hay ninguno, con el correo y la contraseña de los secretos
 * de despliegue. Si ya existe un administrador activo no hace nada (no sobrescribe contraseñas).
 */
@Component
class BootstrapAdministrator implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(BootstrapAdministrator.class);

	private final UserAccountRepository accounts;

	private final PasswordHasher hasher;

	private final IdentityProperties properties;

	private final Clock clock;

	BootstrapAdministrator(UserAccountRepository accounts, PasswordHasher hasher, IdentityProperties properties,
			Clock clock) {
		this.accounts = accounts;
		this.hasher = hasher;
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		IdentityProperties.BootstrapAdmin admin = this.properties.bootstrapAdmin();
		if (admin.email() == null || admin.email().isBlank() || admin.password() == null || admin.password().isBlank()) {
			return;
		}
		if (this.accounts.countActiveAdministrators() > 0) {
			return;
		}
		Email email = new Email(admin.email());
		if (this.accounts.existsByEmail(email)) {
			log.warn("No se crea el administrador inicial: el correo ya pertenece a otra cuenta.");
			return;
		}
		// Los archivos de secretos suelen terminar en salto de línea (CRLF en Windows): no forma parte de la clave.
		String password = admin.password().replaceAll("[\\r\\n]+$", "");
		PasswordPolicy.check("bootstrapAdmin.password", password, email.value());
		this.accounts.insert(UserAccount.bootstrapAdministrator(Ids.newId(), email, admin.fullName(),
				this.hasher.hash(password), this.clock.instant()));
		log.info("Administrador inicial creado ({}).", email.masked());
	}

}
