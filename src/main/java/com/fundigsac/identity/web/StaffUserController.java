package com.fundigsac.identity.web;

import java.net.URI;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.identity.application.AccountView;
import com.fundigsac.identity.application.StaffUserService;
import com.fundigsac.identity.domain.Role;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.ETags;

/** Administración de usuarios del personal (permiso users:manage). */
@RestController
@RequestMapping("/api/v1/staff")
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Personal: usuarios", description = "Invitaciones, roles y estado de las cuentas del personal")
class StaffUserController {

	record InviteRequest(@NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(min = 2, max = 120) String fullName,
			@NotEmpty(message = "Asigne al menos un rol.") Set<Role> roles) {
	}

	record RolesRequest(@NotEmpty(message = "Asigne al menos un rol.") Set<Role> roles) {
	}

	private final StaffUserService users;

	StaffUserController(StaffUserService users) {
		this.users = users;
	}

	@GetMapping("/profile")
	@Operation(summary = "Mi perfil de personal (roles y permisos efectivos)")
	AccountView profile() {
		return this.users.self();
	}

	@GetMapping("/users")
	@Operation(summary = "Listar personal")
	PageResult<AccountView> list(@RequestParam(required = false) @Size(max = 100) String q,
			@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
		return this.users.list(q, PageQuery.of(page, size));
	}

	@GetMapping("/users/{id}")
	@Operation(summary = "Ver usuario del personal", description = "Devuelve ETag con la versión para modificarlo.")
	ResponseEntity<AccountView> get(@PathVariable UUID id) {
		AccountView view = this.users.get(id);
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/users")
	@Operation(summary = "Invitar a un miembro del personal", description = "Envía un enlace de 72 horas para definir su contraseña.")
	ResponseEntity<AccountView> invite(@Valid @RequestBody InviteRequest request) {
		AccountView view = this.users.invite(request.email(), request.fullName(), EnumSet.copyOf(request.roles()));
		return ResponseEntity.created(URI.create("/api/v1/staff/users/" + view.id())).eTag(ETags.of(view.version())).body(view);
	}

	@PutMapping("/users/{id}/roles")
	@Operation(summary = "Cambiar roles", description = "Requiere If-Match. Invalida los tokens vigentes del usuario.")
	ResponseEntity<AccountView> changeRoles(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody RolesRequest request) {
		AccountView view = this.users.changeRoles(id, EnumSet.copyOf(request.roles()), ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/users/{id}/disable")
	@Operation(summary = "Desactivar usuario", description = "Requiere If-Match. Cierra sus sesiones al instante.")
	ResponseEntity<AccountView> disable(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		AccountView view = this.users.disable(id, ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/users/{id}/enable")
	@Operation(summary = "Reactivar usuario", description = "Requiere If-Match.")
	ResponseEntity<AccountView> enable(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		AccountView view = this.users.enable(id, ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

}
