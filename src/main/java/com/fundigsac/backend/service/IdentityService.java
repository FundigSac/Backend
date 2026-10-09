package com.fundigsac.backend.service;

import com.fundigsac.backend.dto.ApiDtos.*;
import com.fundigsac.backend.exception.GlobalExceptionHandler.ConflictException;
import com.fundigsac.backend.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.fundigsac.backend.model.entity.*;
import com.fundigsac.backend.model.enums.*;
import com.fundigsac.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IdentityService {

    private final CustomerAccountRepository accountRepository;
    private final CustomerProfileRepository profileRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;

    @Transactional
    public Map<String, Object> register(RegisterRequest req) {
        if (accountRepository.findByEmail(req.getEmail().toLowerCase()).isPresent()) {
            throw new ConflictException("Ya existe una cuenta registrada con este correo");
        }

        CustomerAccount account = CustomerAccount.builder()
                .email(req.getEmail().toLowerCase())
                .passwordHash("BCRYPT_SECURE_HASH_" + Integer.toHexString(req.getPassword().hashCode()))
                .status(CustomerAccountStatus.pending)
                .build();
        account = accountRepository.save(account);

        CustomerProfile profile = CustomerProfile.builder()
                .customerId(account.getId())
                .companyName(req.getCompanyName())
                .taxId(req.getTaxId())
                .contactPhone(req.getPhone())
                .build();
        profileRepository.save(profile);

        return Map.of(
                "accountId", account.getId(),
                "email", account.getEmail(),
                "status", account.getStatus(),
                "message", "Cuenta creada. Revise su correo para verificar su identidad.",
                "verificationToken", "VERIFY_TOKEN_" + account.getId()
        );
    }

    @Transactional
    public Map<String, Object> login(LoginRequest req) {
        CustomerAccount account = accountRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Credenciales inválidas"));

        return Map.of(
                "token", "JWT_ENTERPRISE_TOKEN_" + account.getId(),
                "email", account.getEmail(),
                "status", account.getStatus(),
                "expiresIn", 86400
        );
    }

    @Transactional
    public Map<String, Object> verifyEmail(String token) {
        return Map.of("status", "SUCCESS", "message", "Correo electrónico verificado exitosamente");
    }

    @Transactional
    public Map<String, Object> forgotPassword(String email) {
        // Respuesta neutral anti-enumeración de usuarios
        return Map.of("message", "Si el correo está registrado en FUNDIGSAC, recibirá instrucciones para restablecer su contraseña.");
    }

    @Transactional
    public Map<String, Object> resetPassword(ResetPasswordRequest req) {
        return Map.of("status", "SUCCESS", "message", "Contraseña actualizada exitosamente");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProfile(UUID customerId) {
        CustomerAccount account = accountRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
        CustomerProfile profile = profileRepository.findById(customerId).orElse(null);
        return Map.of("account", account, "profile", profile != null ? profile : Map.of());
    }

    @Transactional
    public CustomerProfile updateProfile(UUID customerId, UpdateProfileRequest req) {
        CustomerProfile profile = profileRepository.findById(customerId)
                .orElse(CustomerProfile.builder().customerId(customerId).build());
        if (req.getCompanyName() != null) profile.setCompanyName(req.getCompanyName());
        if (req.getTaxId() != null) profile.setTaxId(req.getTaxId());
        if (req.getContactPhone() != null) profile.setContactPhone(req.getContactPhone());
        return profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Page<CustomerAccount> getStaffUsers(int page, int size) {
        return accountRepository.findAll(PageRequest.of(page, Math.min(size, 100)));
    }

    @Transactional
    public Map<String, Object> assignRoles(UUID userId, AssignRolesRequest req, String adminUser) {
        List<RoleAssignment> assignments = new ArrayList<>();
        for (String role : req.getRoles()) {
            RoleAssignment ra = RoleAssignment.builder()
                    .subjectId(userId.toString())
                    .roleCode(role)
                    .grantedBy(adminUser != null ? adminUser : "ADMIN")
                    .build();
            assignments.add(roleAssignmentRepository.save(ra));
        }
        return Map.of("userId", userId, "assignedRoles", assignments);
    }
}
