package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.*;
import com.fundigsac.backend.service.IdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "08. Identidad y Autenticación", description = "Registro de clientes corporativos, inicio de sesión seguro y recuperación de credenciales")
public class AuthController {

    private final IdentityService identityService;

    @PostMapping("/register")
    @Operation(summary = "API-020: Registro de cuenta con email verificado", description = "Crea una cuenta en estado pendiente emitiendo token de activación único")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(identityService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "API-021: Inicio de sesión seguro", description = "Autentica credenciales y genera sesión segura / token para portal B2B")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(identityService.login(req));
    }

    @PostMapping("/logout")
    @Operation(summary = "API-022: Cierre de sesión", description = "Invalida las credenciales activas del cliente")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Sesión finalizada con éxito"));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "API-023: Verificación de correo", description = "Confirma la dirección de email del cliente mediante token de un solo uso")
    public ResponseEntity<Map<String, Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        return ResponseEntity.ok(identityService.verifyEmail(req.getToken()));
    }

    @PostMapping("/password/forgot")
    @Operation(summary = "API-024: Solicitud de recuperación de contraseña", description = "Genera enlace de un solo uso con respuesta neutra para evitar enumeración de usuarios")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.accepted().body(identityService.forgotPassword(req.getEmail()));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "API-025: Restablecimiento de contraseña", description = "Actualiza la clave utilizando un token de restablecimiento válido")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return ResponseEntity.ok(identityService.resetPassword(req));
    }
}
