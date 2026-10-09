package com.fundigsac.backend.dto;

import com.fundigsac.backend.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ApiDtos {

    // --- Atencion / Inquiries ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateInquiryRequest {
        @NotBlank(message = "El nombre de contacto es obligatorio")
        @Size(max = 160)
        private String name;

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        @Size(max = 254)
        private String email;

        @Size(max = 32)
        private String phone;

        @Size(max = 200)
        private String subject;

        @NotBlank(message = "El mensaje es obligatorio")
        private String message;

        private Boolean consent;
    }

    // --- Cotizaciones / Quotes ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateQuoteRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String contactName;

        @NotBlank(message = "El correo es obligatorio")
        @Email
        private String contactEmail;

        private String companyName;
        private String contactPhone;
        private String message;
        private QuoteSource source;
        private List<QuoteItemDto> items;
        private String idempotencyKey;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class QuoteItemDto {
        private UUID productId;
        private UUID variantId;
        @NotBlank
        private String productName;
        private String referenceCode;
        @NotNull @DecimalMin("0.001")
        private BigDecimal quantity;
        private String uom;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateQuoteStatusRequest {
        @NotNull
        private QuoteStatus status;
        private String assignee;
        private String note;
    }

    // --- Libro de Reclamaciones ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateComplaintRequest {
        @NotNull
        private ComplaintType type; // reclamo o queja

        @NotBlank
        @Size(max = 200)
        private String consumerName;

        @NotBlank
        @Email
        private String consumerContact;

        @NotBlank
        private String consumerDocType; // DNI, RUC, CE, Pasaporte

        @NotBlank
        private String consumerDocNumber;

        private String productService;

        @NotBlank
        private String incidentDescription;

        @NotBlank
        private String requestDescription;

        private String privacyNoticeVersion;
        private String idempotencyKey;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateComplaintStatusRequest {
        @NotNull
        private ComplaintStatus status;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ComplaintResponseRequest {
        @NotBlank
        private String response;
        private String attachmentKey;
    }

    // --- Privacidad & Cookies ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PrivacyChoiceRequest {
        @NotBlank
        private String noticeVersion;
        @NotBlank
        private String purpose;
        @NotNull
        private ConsentChoice choice;
        private String subjectRef;
        private String evidenceMethod;
    }

    // --- Identidad & Cuentas ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegisterRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
        private String password;
        private String companyName;
        private String taxId;
        private String phone;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VerifyEmailRequest {
        @NotBlank
        private String token;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ForgotPasswordRequest {
        @NotBlank @Email
        private String email;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResetPasswordRequest {
        @NotBlank
        private String token;
        @NotBlank @Size(min = 8)
        private String newPassword;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateProfileRequest {
        private String companyName;
        private String taxId;
        private String contactPhone;
    }

    // --- Comercio & Carrito ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AddToCartRequest {
        @NotNull
        private UUID variantId;
        @NotNull @DecimalMin("0.001")
        private BigDecimal quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateCartItemRequest {
        @NotNull @DecimalMin("0.001")
        private BigDecimal quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ShippingQuoteRequest {
        @NotBlank
        private String recipientName;
        @NotBlank
        private String addressLines;
        private String ubigeo;
        @NotBlank
        private String region;
        private String contactPhone;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CheckoutPreviewRequest {
        private UUID addressId;
        private String taxId;
        private String invoiceType; // BOLETA o FACTURA
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateOrderRequest {
        private UUID addressId;
        private String idempotencyKey;
        private String invoiceType;
        private String taxId;
        private String buyerEmail;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateOrderStatusRequest {
        @NotNull
        private OrderStatus status;
        private String note;
    }

    // --- Pasarela de Pagos Izipay ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InitializePaymentRequest {
        @NotNull
        private UUID orderId;
        @NotBlank
        private String idempotencyKey;
        private String returnUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class IzipayWebhookRequest {
        private String providerEventId;
        private String orderNumber;
        private BigDecimal amount;
        private String currency;
        private String signature;
        private String status;
        private String rawPayload;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RefundRequest {
        @NotNull @DecimalMin("0.01")
        private BigDecimal amount;
        @NotBlank
        private String reason;
    }

    // --- Staff Admin & Catálogo ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CatalogImportRequest {
        @NotBlank
        private String sourceFileKey;
        private Boolean dryRun;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AssignRolesRequest {
        @NotEmpty
        private List<String> roles;
        private String reason;
    }

    // --- Analítica ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AnalyticsEventRequest {
        @NotBlank
        private String eventName;
        private String entityRef;
        private String safeProps;
    }
}
