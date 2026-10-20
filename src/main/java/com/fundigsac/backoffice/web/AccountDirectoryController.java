package com.fundigsac.backoffice.web;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.backoffice.application.AccountDirectoryService;
import com.fundigsac.backoffice.application.AccountNotesService;
import com.fundigsac.backoffice.application.AccountViews.AccountPage;
import com.fundigsac.backoffice.application.AccountViews.AccountRow;
import com.fundigsac.backoffice.application.AccountViews.AccountStats;
import com.fundigsac.backoffice.application.AccountViews.ActivityRow;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.ETags;

/** Directorio de cuentas del panel interno: clientes y personal, indicadores y actividad (permiso users:manage). */
@RestController
@RequestMapping("/api/v1/staff/accounts")
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Personal: cuentas", description = "Directorio de todas las cuentas del sitio, indicadores y actividad")
class AccountDirectoryController {

	record NotesRequest(@NotNull @Size(max = 2000) String notes) {
	}

	private final AccountDirectoryService directory;

	private final AccountNotesService notes;

	AccountDirectoryController(AccountDirectoryService directory, AccountNotesService notes) {
		this.directory = directory;
		this.notes = notes;
	}

	@GetMapping
	@Operation(summary = "Listar cuentas",
			description = "status: ACTIVE, PENDING_VERIFICATION o DISABLED. kind: CUSTOMER o STAFF. method: EMAIL, GOOGLE o FACEBOOK. "
					+ "range: 7d, 30d, 90d o all. sort: name, registeredAt, lastActiveAt, quotes o status. Devuelve además los "
					+ "conteos por estado con los mismos filtros.")
	AccountPage list(@RequestParam(required = false) @Size(max = 100) String q,
			@RequestParam(required = false) @Size(max = 24) String status, @RequestParam(required = false) @Size(max = 10) String kind,
			@RequestParam(required = false) @Size(max = 10) String method, @RequestParam(required = false) @Size(max = 20) String role,
			@RequestParam(required = false) @Size(max = 4) String range, @RequestParam(required = false) @Size(max = 20) String sort,
			@RequestParam(defaultValue = "true") boolean desc, @RequestParam(required = false) @Min(0) Integer page,
			@RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return this.directory.list(new AccountDirectoryService.Query(q, status, kind, method, role, range, sort, desc),
				PageQuery.of(page, size));
	}

	@GetMapping("/stats")
	@Operation(summary = "Indicadores de las cuentas de clientes", description = "range: 7d, 30d, 90d o all.")
	AccountStats stats(@RequestParam(required = false) @Size(max = 4) String range) {
		return this.directory.stats(range);
	}

	@GetMapping("/activity")
	@Operation(summary = "Actividad de las cuentas",
			description = "type: SIGNUP, LOGIN, VERIFY, QUOTE, PASSWORD, DISABLE, ENABLE, ROLES, UPDATE, DELETE o INVITE.")
	PageResult<ActivityRow> activity(@RequestParam(required = false) @Size(max = 100) String q,
			@RequestParam(required = false) @Size(max = 10) String type, @RequestParam(required = false) UUID userId,
			@RequestParam(required = false) @Min(0) Integer page, @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return this.directory.activity(q, type, userId, PageQuery.of(page, size));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Ficha de una cuenta", description = "Devuelve ETag con la versión para modificarla.")
	ResponseEntity<AccountRow> get(@PathVariable UUID id) {
		AccountRow row = this.directory.get(id);
		return ResponseEntity.ok().eTag(ETags.of(row.version())).body(row);
	}

	@PutMapping("/{id}/notes")
	@Operation(summary = "Guardar la nota interna de una cuenta", description = "Hasta 2000 caracteres; vacía la borra. El cliente no la ve.")
	ResponseEntity<AccountRow> saveNotes(@PathVariable UUID id, @Valid @RequestBody NotesRequest request) {
		AccountRow row = this.notes.save(id, request.notes());
		return ResponseEntity.ok().eTag(ETags.of(row.version())).body(row);
	}

}
