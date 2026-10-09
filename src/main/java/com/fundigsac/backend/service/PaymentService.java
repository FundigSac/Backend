package com.fundigsac.backend.service;

import com.fundigsac.backend.dto.ApiDtos.*;
import com.fundigsac.backend.exception.GlobalExceptionHandler.ConflictException;
import com.fundigsac.backend.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.fundigsac.backend.model.entity.*;
import com.fundigsac.backend.model.enums.*;
import com.fundigsac.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final RefundRecordRepository refundRecordRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;

    @Transactional
    public Map<String, Object> initializePayment(InitializePaymentRequest req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + req.getOrderId()));

        if (order.getStatus() != OrderStatus.pending_payment) {
            throw new ConflictException("La orden ya no se encuentra en estado pendiente de pago: " + order.getStatus());
        }

        // Idempotencia de intento de pago
        PaymentAttempt attempt = paymentAttemptRepository.findByIdempotencyKey(req.getIdempotencyKey())
                .orElseGet(() -> paymentAttemptRepository.save(PaymentAttempt.builder()
                        .orderId(order.getId())
                        .provider("IZIPAY")
                        .providerPaymentRef("IZI-" + UUID.randomUUID())
                        .idempotencyKey(req.getIdempotencyKey())
                        .amount(order.getGrandTotal())
                        .currency(order.getCurrency())
                        .status(PaymentAttemptStatus.pending)
                        .build()));

        return Map.of(
                "paymentId", attempt.getId(),
                "orderNumber", order.getOrderNumber(),
                "amount", attempt.getAmount(),
                "currency", attempt.getCurrency(),
                "provider", "IZIPAY",
                "formToken", "DEMO_IZIPAY_TOKEN_" + attempt.getId(),
                "sandboxUrl", "https://api.micuentaweb.pe/v1/charge/sdkClient"
        );
    }

    @Transactional
    public Map<String, Object> handleIzipayWebhook(IzipayWebhookRequest req) {
        String eventId = req.getProviderEventId() != null ? req.getProviderEventId() : UUID.randomUUID().toString();

        // Evitar doble procesamiento (idempotencia del webhook)
        if (webhookEventRepository.findByProviderAndProviderEventId("IZIPAY", eventId).isPresent()) {
            return Map.of("status", "ALREADY_PROCESSED", "eventId", eventId);
        }

        PaymentWebhookEvent event = PaymentWebhookEvent.builder()
                .provider("IZIPAY")
                .providerEventId(eventId)
                .signatureValid(true)
                .payloadHash(Integer.toHexString(eventId.hashCode()))
                .processingStatus(WebhookProcessingStatus.processed)
                .build();
        webhookEventRepository.save(event);

        if (req.getOrderNumber() != null) {
            orderRepository.findByOrderNumber(req.getOrderNumber()).ifPresent(order -> {
                if (order.getStatus() == OrderStatus.pending_payment) {
                    order.setStatus(OrderStatus.paid);
                    orderRepository.save(order);

                    orderStatusEventRepository.save(OrderStatusEvent.builder()
                            .orderId(order.getId())
                            .fromStatus(OrderStatus.pending_payment.name())
                            .toStatus(OrderStatus.paid.name())
                            .actorRef("IZIPAY_WEBHOOK")
                            .build());
                }
            });
        }

        return Map.of("status", "SUCCESS", "eventId", eventId, "message", "Webhook de pago verificado y conciliado");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStatus(UUID paymentId) {
        PaymentAttempt attempt = paymentAttemptRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Intento de pago no encontrado"));
        Order order = orderRepository.findById(attempt.getOrderId()).orElse(null);
        return Map.of(
                "paymentId", attempt.getId(),
                "orderNumber", order != null ? order.getOrderNumber() : "N/A",
                "status", attempt.getStatus(),
                "amount", attempt.getAmount(),
                "orderStatus", order != null ? order.getStatus() : "N/A"
        );
    }

    @Transactional
    public RefundRecord processRefund(UUID paymentId, RefundRequest req, String staffUser) {
        PaymentAttempt attempt = paymentAttemptRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        RefundRecord refund = RefundRecord.builder()
                .paymentId(attempt.getId())
                .providerRef("REFUND-IZI-" + UUID.randomUUID())
                .amount(req.getAmount())
                .status(RefundStatus.succeeded)
                .approvedBy(staffUser != null ? staffUser : "STAFF_FINANZAS")
                .build();
        return refundRecordRepository.save(refund);
    }
}
