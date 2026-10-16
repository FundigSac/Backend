package com.fundigsac.notification.web;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.notification.application.NotificationAdminService;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;

/** Monitoreo de correos transaccionales. */
@RestController
@RequestMapping("/api/v1/staff/notifications")
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Personal: notificaciones", description = "Estado de la bandeja de salida de correos")
class StaffNotificationController {

	private final NotificationAdminService notifications;

	StaffNotificationController(NotificationAdminService notifications) {
		this.notifications = notifications;
	}

	@GetMapping
	@Operation(summary = "Listar correos", description = "status: PENDING, SENT o FAILED. Destinatarios enmascarados y sin cuerpo.")
	PageResult<NotificationAdminService.EmailView> list(@RequestParam(required = false) @Size(max = 10) String status,
			@RequestParam(required = false) @Min(0) Integer page, @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return this.notifications.list(status, PageQuery.of(page, size));
	}

	@PostMapping("/{id}/retry")
	@Operation(summary = "Reintentar un correo fallido")
	NotificationAdminService.EmailView retry(@PathVariable UUID id) {
		return this.notifications.retry(id);
	}

}
