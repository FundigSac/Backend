package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.UpdateProfileRequest;
import com.fundigsac.backend.model.entity.CustomerProfile;
import com.fundigsac.backend.model.entity.Order;
import com.fundigsac.backend.service.CommerceService;
import com.fundigsac.backend.service.IdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "09. Portal del Cliente (Mi Cuenta)", description = "Gestión de perfil comercial, consulta de cotizaciones y pedidos propios")
public class AccountController {

    private final IdentityService identityService;
    private final CommerceService commerceService;

    @GetMapping("/me")
    @Operation(summary = "API-026: Consultar perfil propio", description = "Devuelve información del cliente autenticado y datos de facturación de la empresa")
    public ResponseEntity<Map<String, Object>> getMyProfile(@RequestParam(required = false) UUID customerId) {
        UUID id = customerId != null ? customerId : UUID.fromString("00000000-0000-0000-0000-000000000001");
        return ResponseEntity.ok(identityService.getProfile(id));
    }

    @PatchMapping("/me")
    @Operation(summary = "API-027: Actualizar datos de contacto y empresa", description = "Edita datos comerciales de la empresa sin modificar pedidos históricos inmutables")
    public ResponseEntity<CustomerProfile> updateProfile(
            @RequestParam(required = false) UUID customerId,
            @RequestBody UpdateProfileRequest req) {
        UUID id = customerId != null ? customerId : UUID.fromString("00000000-0000-0000-0000-000000000001");
        return ResponseEntity.ok(identityService.updateProfile(id, req));
    }

    @GetMapping("/quotes")
    @Operation(summary = "API-028: Mis cotizaciones vinculadas", description = "Historial de solicitudes de cotización generadas por la empresa autenticada")
    public ResponseEntity<List<Map<String, Object>>> getMyQuotes() {
        return ResponseEntity.ok(List.of(
                Map.of("reference", "COT-2026-001", "product", "Válvula Compuerta Bridada DN 100 PN 16", "quantity", 10, "status", "responded")
        ));
    }

    @GetMapping("/orders")
    @Operation(summary = "API-038: Mis pedidos de compra", description = "Historial paginado de órdenes de compra con estado de despacho")
    public ResponseEntity<Page<Order>> getMyOrders(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID id = customerId != null ? customerId : UUID.fromString("00000000-0000-0000-0000-000000000001");
        return ResponseEntity.ok(commerceService.getCustomerOrders(id, page, size));
    }
}
