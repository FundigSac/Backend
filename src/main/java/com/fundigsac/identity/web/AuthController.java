package com.fundigsac.identity.web;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.identity.application.AuthTokens;
import com.fundigsac.identity.application.AuthenticationService;
import com.fundigsac.identity.application.PasswordResetService;
import com.fundigsac.identity.application.RegistrationService;
import com.fundigsac.shared.web.RateLimited;

/** Acceso: registro, verificación, ingreso, renovación, salida, recuperación de contraseña e invitaciones. */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Acceso", description = "Registro, inicio de sesión y recuperación de contraseña")
class AuthController {

	private static final Map<String, String> ACCEPTED = Map.of("message",
			"Si los datos son válidos, recibirá un correo con las instrucciones.");

	record RegisterRequest(
			@NotBlank(message = "Ingrese su correo.") @Email(message = "Ingrese un correo válido.") @Size(max = 254) String email,
			@NotBlank(message = "Ingrese una contraseña.") @Size(min = 12, max = 128, message = "La contraseña debe tener entre 12 y 128 caracteres.") String password,
			@NotBlank(message = "Ingrese su nombre.") @Size(min = 2, max = 120) String fullName,
			@Size(max = 160) String companyName, @Size(max = 11) String ruc, @Size(max = 24) String phone,
			@NotBlank(message = "Indique la versión del aviso de privacidad mostrado.") @Size(max = 20) String privacyNoticeVersion,
			@AssertTrue(message = "Debe aceptar el aviso de privacidad.") boolean acceptPrivacyNotice) {

		@Override
		public String toString() {
			return "RegisterRequest[***]";
		}

	}

	record LoginRequest(@NotBlank @Size(max = 254) String email, @NotBlank @Size(max = 128) String password) {

		@Override
		public String toString() {
			return "LoginRequest[***]";
		}

	}

	record TokenRequest(@NotBlank @Size(max = 128) String token) {

		@Override
		public String toString() {
			return "TokenRequest[***]";
		}

	}

	record RefreshRequest(@NotBlank @Size(max = 128) String refreshToken) {

		@Override
		public String toString() {
			return "RefreshRequest[***]";
		}

	}

	record EmailRequest(@NotBlank @Email @Size(max = 254) String email) {
	}

	record NewPasswordRequest(@NotBlank @Size(max = 128) String token,
			@NotBlank @Size(min = 12, max = 128, message = "La contraseña debe tener entre 12 y 128 caracteres.") String password) {

		@Override
		public String toString() {
			return "NewPasswordRequest[***]";
		}

	}

	record TokenResponse(String tokenType, String accessToken, long expiresIn, String refreshToken, long refreshExpiresIn) {

		static TokenResponse of(AuthTokens tokens) {
			return new TokenResponse("Bearer", tokens.accessToken(), tokens.expiresIn(), tokens.refreshToken(),
					tokens.refreshExpiresIn());
		}

		@Override
		public String toString() {
			return "TokenResponse[***]";
		}

	}

	private final RegistrationService registration;

	private final AuthenticationService authentication;

	private final PasswordResetService passwords;

	AuthController(RegistrationService registration, AuthenticationService authentication,
			PasswordResetService passwords) {
		this.registration = registration;
		this.authentication = authentication;
		this.passwords = passwords;
	}

	@PostMapping("/register")
	@RateLimited("register")
	@Operation(summary = "Registrar cuenta de cliente", description = "Responde 202 siempre igual (no revela si el correo ya existía) y envía el enlace de verificación.")
	ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
		this.registration.register(new RegistrationService.RegisterCustomer(request.email(), request.password(),
				request.fullName(), request.companyName(), request.ruc(), request.phone(), request.privacyNoticeVersion()));
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ACCEPTED);
	}

	@PostMapping("/verify-email")
	@RateLimited("token")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Confirmar correo con el token del enlace")
	void verifyEmail(@Valid @RequestBody TokenRequest request) {
		this.registration.verifyEmail(request.token());
	}

	@PostMapping("/verify-email/resend")
	@RateLimited("password-reset")
	@Operation(summary = "Reenviar enlace de verificación")
	ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody EmailRequest request) {
		this.registration.resendVerification(request.email());
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ACCEPTED);
	}

	@PostMapping("/login")
	@RateLimited("login")
	@Operation(summary = "Iniciar sesión", description = "Devuelve un JWT de acceso (15 min) y un refresh token rotativo. Tras 5 intentos fallidos la cuenta se bloquea 15 minutos.")
	TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return TokenResponse.of(this.authentication.login(request.email(), request.password()));
	}

	@PostMapping("/refresh")
	@RateLimited("token")
	@Operation(summary = "Renovar sesión", description = "Cada refresh token sirve una sola vez; reutilizarlo cierra todas las sesiones de esa familia.")
	TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return TokenResponse.of(this.authentication.refresh(request.refreshToken()));
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Cerrar sesión")
	void logout(@Valid @RequestBody RefreshRequest request) {
		this.authentication.logout(request.refreshToken());
	}

	@PostMapping("/password/forgot")
	@RateLimited("password-reset")
	@Operation(summary = "Solicitar enlace de recuperación", description = "Responde 202 siempre igual.")
	ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody EmailRequest request) {
		this.passwords.requestReset(request.email());
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ACCEPTED);
	}

	@PostMapping("/password/reset")
	@RateLimited("token")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Definir nueva contraseña con el token del enlace", description = "Cierra todas las sesiones abiertas.")
	void resetPassword(@Valid @RequestBody NewPasswordRequest request) {
		this.passwords.resetPassword(request.token(), request.password());
	}

	@PostMapping("/invitation/accept")
	@RateLimited("token")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Aceptar invitación del personal y definir contraseña")
	void acceptInvitation(@Valid @RequestBody NewPasswordRequest request) {
		this.passwords.acceptInvitation(request.token(), request.password());
	}

}
