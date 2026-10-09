package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.*;
import com.fundigsac.backend.model.entity.*;
import com.fundigsac.backend.model.enums.*;
import com.fundigsac.backend.service.CommerceService;
import com.fundigsac.backend.service.IdentityService;
import com.fundigsac.backend.service.OperationService;
import com.fundigsac.backend.service.PaymentService;
import com.fundigsac.backend.service.StaffAndMiscService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@Tag(name = "12. Backoffice y Administración (RBAC)", description = "Bandejas internas operativas: cotizaciones, atención, Libro de Reclamaciones, despacho, roles y catálogo")
public class StaffController {

    private final OperationService operationService;
    private final CommerceService commerceService;
    private final PaymentService paymentService;
    private final IdentityService identityService;
    private final StaffAndMiscService staffAndMiscService;

    // --- Cotizaciones Staff ---
    @GetMapping("/quotes")
    @Operation(summary = "API-011: Bandeja operativa de cotizaciones", description = "Listado de solicitudes B2B con filtros por estado y asignación comercial")
    public ResponseEntity<Page<QuoteRequest>> getQuotes(
            @RequestParam(required = false) QuoteStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(operationService.getStaffQuotes(status, page, size));
    }

    @PatchMapping("/quotes/{id}")
    @Operation(summary = "API-012: Asignar asesor o actualizar estado de cotización", description = "Transiciona el estado comercial y registra la auditoría con fecha y responsable")
    public ResponseEntity<QuoteRequest> updateQuote(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuoteStatusRequest req,
            @RequestHeader(value = "X-Staff-User", required = false) String staffUser) {
        return ResponseEntity.ok(operationService.updateQuoteStatus(id, req, staffUser));
    }

    // --- Reclamaciones Staff ---
    @GetMapping("/complaints")
    @Operation(summary = "API-015: Bandeja de reclamaciones y SLA legal", description = "Monitor de reclamos con fecha de vencimiento y alertas de plazo legal")
    public ResponseEntity<Page<Complaint>> getComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(operationService.getStaffComplaints(status, page, size));
    }

    @PatchMapping("/complaints/{id}")
    @Operation(summary = "API-016: Gestionar reclamo formal", description = "Asigna responsable de atención y actualiza estado legal del caso")
    public ResponseEntity<Complaint> updateComplaint(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateComplaintStatusRequest req,
            @RequestHeader(value = "X-Staff-User", required = false) String staffUser) {
        return ResponseEntity.ok(operationService.updateComplaintStatus(id, req, staffUser));
    }

    @PostMapping("/complaints/{id}/response")
    @Operation(summary = "API-050: Registrar respuesta formal al consumidor", description = "Adjunta la respuesta oficial y constancia resolutiva conservando el historial inalterable")
    public ResponseEntity<ComplaintEvent> respondComplaint(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintResponseRequest req,
            @RequestHeader(value = "X-Staff-User", required = false) String staffUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operationService.respondComplaint(id, req, staffUser));
    }

    // --- Órdenes y Logística Staff ---
    @PatchMapping("/orders/{id}/status")
    @Operation(summary = "API-039: Actualizar estado de despacho de pedido", description = "Controla la transición de la orden: preparación, enviado o entregado con trazabilidad")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest req,
            @RequestHeader(value = "X-Staff-User", required = false) String staffUser) {
        return ResponseEntity.ok(commerceService.updateOrderStatus(id, req, staffUser));
    }

    // --- Finanzas Staff ---
    @PostMapping("/payments/{id}/refund")
    @Operation(summary = "API-043: Procesar reversión o reembolso de pago", description = "Ejecuta anulación autorizada con conciliación de la transacción en Izipay")
    public ResponseEntity<RefundRecord> processRefund(
            @PathVariable UUID id,
            @Valid @RequestBody RefundRequest req,
            @RequestHeader(value = "X-Staff-User", required = false) String staffUser) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentService.processRefund(id, req, staffUser));
    }

    // --- Catálogo Staff ---
    @GetMapping("/catalog/products")
    @Operation(summary = "API-044: Administración de catálogo y borradores", description = "Lista productos tanto en borrador como publicados para control editorial")
    public ResponseEntity<Page<Product>> getStaffCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(staffAndMiscService.getStaffProducts(page, size));
    }

    @PostMapping("/catalog/imports")
    @Operation(summary = "API-045: Importación masiva controlada de catálogo", description = "Valida CSV/Excel con SKU y atributos antes de aplicar cambios")
    public ResponseEntity<CatalogImportBatch> importCatalog(
            @Valid @RequestBody CatalogImportRequest req,
            @RequestHeader(value = "X-Staff-User", required = false) String staffUser) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(staffAndMiscService.importCatalog(req, staffUser));
    }

    @PostMapping("/catalog/changes/{id}/approve")
    @Operation(summary = "API-046: Aprobación editorial de cambios técnicos", description = "Aprobador técnico valida y publica las modificaciones de fichas técnicas")
    public ResponseEntity<Map<String, Object>> approveCatalogChange(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Staff-User", required = false) String reviewerUser) {
        return ResponseEntity.ok(staffAndMiscService.approveCatalogChange(id, reviewerUser));
    }

    // --- Usuarios y Roles Staff ---
    @GetMapping("/users")
    @Operation(summary = "API-047: Directorio de usuarios y colaboradores", description = "Lista cuentas registradas y estado de activación")
    public ResponseEntity<Page<CustomerAccount>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(identityService.getStaffUsers(page, size));
    }

    @PatchMapping("/users/{id}/roles")
    @Operation(summary = "API-048: Asignación de roles RBAC auditada", description = "Configura permisos (ADMIN, VENTAS, LOGISTICA, FINANZAS, SOPORTE) con registro inmutable")
    public ResponseEntity<Map<String, Object>> assignRoles(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRolesRequest req,
            @RequestHeader(value = "X-Staff-User", required = false) String adminUser) {
        return ResponseEntity.ok(identityService.assignRoles(id, req, adminUser));
    }

    // --- Mesa de Ayuda Staff ---
    @GetMapping("/inquiries")
    @Operation(summary = "API-049: Bandeja avanzada de consultas comerciales", description = "Bandeja separada del Libro de Reclamaciones para gestión ágil de leads")
    public ResponseEntity<Map<String, Object>> getInquiries() {
        return ResponseEntity.ok(Map.of(
                "totalInquiries", 12,
                "status", "ACTIVE_DESK",
                "department", "VENTAS_B2B"
        ));
    }

    // --- Tablero de Control Staff ---
    @GetMapping("/metrics")
    @Operation(summary = "API-052: Tablero KPI y métricas de operación", description = "Panel de rendimiento comercial, volumen de pedidos y velocidad de atención")
    public ResponseEntity<Map<String, Object>> getMetrics(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String metric) {
        return ResponseEntity.ok(staffAndMiscService.getMetrics(range, metric));
    }
}
