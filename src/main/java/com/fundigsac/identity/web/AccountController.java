package com.fundigsac.identity.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.identity.application.AccountService;
import com.fundigsac.identity.application.AccountView;
import com.fundigsac.shared.security.Authz;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.RateLimited;

/** Cuenta propia del cliente. */
@RestController
@RequestMapping("/api/v1/account")
@PreAuthorize(Authz.CUSTOMER)
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Mi cuenta", description = "Perfil, contraseña y eliminación de la cuenta del cliente")
class AccountController {

	record ProfileRequest(@NotBlank @Size(min = 2, max = 120) String fullName, @Size(max = 160) String companyName,
			@Size(max = 11) String ruc, @Size(max = 24) String phone) {
	}

	record ChangePasswordRequest(@NotBlank @Size(max = 128) String currentPassword,
			@NotBlank @Size(min = 12, max = 128, message = "La contraseña debe tener entre 12 y 128 caracteres.") String newPassword) {

		@Override
		public String toString() {
			return "ChangePasswordRequest[***]";
		}

	}

	record DeleteAccountRequest(@NotBlank @Size(max = 128) String password) {

		@Override
		public String toString() {
			return "DeleteAccountRequest[***]";
		}

	}

	private final AccountService accounts;

	AccountController(AccountService accounts) {
		this.accounts = accounts;
	}

	@GetMapping("/me")
	@Operation(summary = "Ver mi perfil")
	AccountView me() {
		return this.accounts.me();
	}

	@PatchMapping("/me")
	@Operation(summary = "Actualizar mi perfil")
	AccountView update(@Valid @RequestBody ProfileRequest request) {
		return this.accounts.updateProfile(
				new AccountService.UpdateProfile(request.fullName(), request.companyName(), request.ruc(), request.phone()));
	}

	@PostMapping("/password")
	@RateLimited("login")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Cambiar mi contraseña", description = "Cierra todas las sesiones; vuelva a iniciar sesión.")
	void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		this.accounts.changePassword(request.currentPassword(), request.newPassword());
	}

	@DeleteMapping
	@RateLimited("login")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar mi cuenta", description = "Anonimiza los datos de la cuenta (Ley 29733). Las solicitudes y reclamaciones presentadas se conservan por obligación legal.")
	void delete(@Valid @RequestBody DeleteAccountRequest request) {
		this.accounts.deleteAccount(request.password());
	}

}
