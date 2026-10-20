package com.fundigsac.identity.web;

import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.identity.application.AccountAdministrationService;
import com.fundigsac.identity.application.AccountView;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.ETags;

/** Acciones del personal sobre cualquier cuenta del sitio (permiso users:manage). */
@RestController
@RequestMapping("/api/v1/staff/accounts/{id}")
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Personal: cuentas", description = "Directorio de todas las cuentas del sitio, indicadores y actividad")
class StaffAccountController {

	record ProfileRequest(@NotBlank @Size(min = 2, max = 120) String fullName, @Size(max = 160) String companyName,
			@Size(max = 11) String ruc, @Size(max = 24) String phone) {
	}

	private static final Map<String, String> SENT = Map.of("message", "Enviamos el correo a la cuenta.");

	private final AccountAdministrationService administration;

	StaffAccountController(AccountAdministrationService administration) {
		this.administration = administration;
	}

	@PatchMapping
	@Operation(summary = "Corregir datos de perfil", description = "Requiere If-Match. El correo no se cambia desde el panel.")
	ResponseEntity<AccountView> correct(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody ProfileRequest request) {
		AccountView view = this.administration.correctProfile(id, new AccountAdministrationService.ProfileCorrection(
				request.fullName(), request.companyName(), request.ruc(), request.phone()), ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/disable")
	@Operation(summary = "Desactivar cuenta", description = "Requiere If-Match. Cierra sus sesiones al instante.")
	ResponseEntity<AccountView> disable(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		AccountView view = this.administration.disable(id, ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/enable")
	@Operation(summary = "Reactivar cuenta", description = "Requiere If-Match.")
	ResponseEntity<AccountView> enable(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		AccountView view = this.administration.enable(id, ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/verification-email")
	@Operation(summary = "Reenviar enlace de verificación", description = "Solo clientes pendientes de verificar.")
	ResponseEntity<Map<String, String>> resendVerification(@PathVariable UUID id) {
		this.administration.resendVerification(id);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(SENT);
	}

	@PostMapping("/password-reset")
	@Operation(summary = "Enviar enlace para definir contraseña", description = "Solo clientes: el personal entra con código.")
	ResponseEntity<Map<String, String>> passwordReset(@PathVariable UUID id) {
		this.administration.sendPasswordReset(id);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(SENT);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Anonimizar cuenta (supresión de datos)",
			description = "Requiere If-Match. Borra los datos personales de la cuenta y cierra sus sesiones; las solicitudes y reclamaciones conservan su copia legal.")
	void delete(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		this.administration.delete(id, ETags.requireVersion(ifMatch));
	}

}
