package com.fundigsac.identity.web;

import java.lang.reflect.Method;
import java.util.Arrays;

import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** El contrato publicado debe describir el flujo real de invitación: acceso con código, sin enlace para definir contraseña. */
class AccessDocumentationTests {

	private static Operation operation(Class<?> controller, String method) {
		Method found = Arrays.stream(controller.getDeclaredMethods()).filter((m) -> m.getName().equals(method)).findFirst().orElseThrow();
		return found.getAnnotation(Operation.class);
	}

	@Test
	void theInvitationDescribesTheCodeLoginAndPromisesNoPasswordLink() {
		String description = operation(StaffUserController.class, "invite").description();
		assertThat(description).contains("código de un solo uso").doesNotContain("72 horas").doesNotContain("Envía un enlace");
	}

	@Test
	void theInvitationAcceptanceIsMarkedAsDeprecated() {
		Operation accept = operation(AuthController.class, "acceptInvitation");
		assertThat(accept.deprecated()).isTrue();
		assertThat(accept.description()).contains("Sin uso en el flujo actual");
	}

	@Test
	void googleAccountsAreToldHowToConfirmTheirIdentity() {
		assertThat(operation(AccountController.class, "delete").description()).contains("googleIdToken");
		assertThat(operation(AccountController.class, "changePassword").description()).contains("googleIdToken");
	}

}
