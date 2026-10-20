package com.fundigsac.identity.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.identity.application.StaffCodeLoginService;
import com.fundigsac.shared.web.RateLimited;

/** Acceso del personal al panel interno sin contraseña: código de un solo uso enviado al correo. */
@RestController
@RequestMapping("/api/v1/auth/staff")
@Tag(name = "Acceso", description = "Registro, inicio de sesión y recuperación de contraseña")
class StaffAccessController {

	record CodeRequest(@NotBlank @Size(max = 254) String email) {
	}

	record VerifyRequest(@NotBlank @Size(max = 254) String email, @NotBlank @Size(max = 12) String code) {

		@Override
		public String toString() {
			return "VerifyRequest[***]";
		}

	}

	record CodeResponse(String message, long expiresInSeconds, long resendAfterSeconds) {
	}

	private final StaffCodeLoginService codes;

	StaffAccessController(StaffCodeLoginService codes) {
		this.codes = codes;
	}

	@PostMapping("/code")
	@RateLimited("login-code")
	@Operation(summary = "Enviar código de acceso al correo del personal",
			description = "Responde 202 siempre igual, exista o no la cuenta. El código vence a los 10 minutos y pedir otro exige esperar 60 segundos.")
	ResponseEntity<CodeResponse> requestCode(@Valid @RequestBody CodeRequest request) {
		StaffCodeLoginService.CodeIssued issued = this.codes.requestCode(request.email());
		return ResponseEntity.status(HttpStatus.ACCEPTED)
			.body(new CodeResponse("Si el correo pertenece al personal, recibirá un código de acceso.",
					issued.expiresInSeconds(), issued.resendAfterSeconds()));
	}

	@PostMapping("/code/verify")
	@RateLimited("token")
	@Operation(summary = "Ingresar con el código",
			description = "Devuelve el mismo par de tokens que el inicio de sesión. Cualquier fallo responde INVALID_CODE; tras 5 intentos el código deja de servir.")
	AuthController.TokenResponse verify(@Valid @RequestBody VerifyRequest request) {
		return AuthController.TokenResponse.of(this.codes.verify(request.email(), request.code()));
	}

}
