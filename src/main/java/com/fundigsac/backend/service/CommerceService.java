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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommerceService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final PriceEntryRepository priceRepository;
    private final InventoryPositionRepository inventoryRepository;
    private final ShoppingCartRepository cartRepository;
    private final CartLineRepository cartLineRepository;
    private final DeliveryAddressRepository addressRepository;
    private final ShippingQuoteRepository shippingQuoteRepository;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEligibleProducts() {
        List<Product> products = productRepository.findByCommercialMode(CommercialMode.buy_enabled);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : products) {
            List<ProductVariant> variants = variantRepository.findByProductIdAndStatus(p.getId(), VariantStatus.active);
            result.add(Map.of("product", p, "variants", variants));
        }
        return result;
    }

    @Transactional
    public ShoppingCart getOrCreateCart(UUID customerId) {
        if (customerId != null) {
            return cartRepository.findFirstByCustomerIdAndStatus(customerId, CartStatus.active)
                    .orElseGet(() -> cartRepository.save(ShoppingCart.builder()
                            .customerId(customerId)
                            .currency("PEN")
                            .status(CartStatus.active)
                            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                            .build()));
        }
        return cartRepository.save(ShoppingCart.builder()
                .currency("PEN")
                .status(CartStatus.active)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCartDetails(UUID cartId) {
        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));
        List<CartLine> lines = cartLineRepository.findByCartId(cartId);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<Map<String, Object>> lineDetails = new ArrayList<>();

        for (CartLine line : lines) {
            ProductVariant variant = variantRepository.findById(line.getVariantId()).orElse(null);
            PriceEntry price = priceRepository.findFirstByVariantIdOrderByValidFromDesc(line.getVariantId()).orElse(null);
            BigDecimal unitPrice = price != null ? price.getNetPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(line.getQuantity());
            subtotal = subtotal.add(lineTotal);

            Map<String, Object> item = new HashMap<>();
            item.put("lineId", line.getId());
            item.put("variant", variant);
            item.put("quantity", line.getQuantity());
            item.put("unitPrice", unitPrice);
            item.put("lineTotal", lineTotal);
            lineDetails.add(item);
        }

        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        return Map.of(
                "cart", cart,
                "lines", lineDetails,
                "subtotal", subtotal,
                "igv", igv,
                "grandTotal", total
        );
    }

    @Transactional
    public CartLine addToCart(UUID cartId, AddToCartRequest req) {
        ProductVariant variant = variantRepository.findById(req.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variante de producto no encontrada"));

        // Validar inventario disponible
        List<InventoryPosition> positions = inventoryRepository.findByVariantId(variant.getId());
        BigDecimal totalStock = positions.stream()
                .map(InventoryPosition::getAvailableQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalStock.compareTo(req.getQuantity()) < 0) {
            throw new ConflictException("Stock insuficiente para el SKU solicitado. Disponible: " + totalStock);
        }

        CartLine line = cartLineRepository.findByCartIdAndVariantId(cartId, req.getVariantId())
                .orElse(CartLine.builder()
                        .cartId(cartId)
                        .variantId(req.getVariantId())
                        .quantity(BigDecimal.ZERO)
                        .build());

        line.setQuantity(line.getQuantity().add(req.getQuantity()));
        return cartLineRepository.save(line);
    }

    @Transactional
    public CartLine updateCartItem(UUID lineId, UpdateCartItemRequest req) {
        CartLine line = cartLineRepository.findById(lineId)
                .orElseThrow(() -> new ResourceNotFoundException("Línea de carrito no encontrada"));
        line.setQuantity(req.getQuantity());
        return cartLineRepository.save(line);
    }

    @Transactional
    public void removeCartItem(UUID lineId) {
        cartLineRepository.deleteById(lineId);
    }

    @Transactional
    public ShippingQuote calculateShipping(ShippingQuoteRequest req) {
        DeliveryAddress address = addressRepository.save(DeliveryAddress.builder()
                .recipientName(req.getRecipientName())
                .addressLines(req.getAddressLines())
                .ubigeo(req.getUbigeo())
                .region(req.getRegion())
                .contactPhone(req.getContactPhone())
                .build());

        // Tarifa industrial basada en región
        BigDecimal freight = req.getRegion().equalsIgnoreCase("Lima")
                ? new BigDecimal("80.00")
                : new BigDecimal("250.00");

        return shippingQuoteRepository.save(ShippingQuote.builder()
                .addressId(address.getId())
                .method("TRANSPORTE_INDUSTRIAL_PESADO")
                .amount(freight)
                .currency("PEN")
                .expiresAt(Instant.now().plus(48, ChronoUnit.HOURS))
                .providerRef("OPERADOR_LOGISTICO_NACIONAL")
                .build());
    }

    @Transactional
    public Order createOrder(UUID cartId, CreateOrderRequest req) {
        // Idempotencia
        if (req.getIdempotencyKey() != null) {
            Optional<Order> existing = orderRepository.findByOrderNumber("ORD-" + Math.abs(req.getIdempotencyKey().hashCode()));
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));
        List<CartLine> lines = cartLineRepository.findByCartId(cartId);
        if (lines.isEmpty()) {
            throw new ConflictException("No se puede generar una orden con carrito vacío");
        }

        String orderNo = "ORD-" + (System.currentTimeMillis() % 10000000);
        BigDecimal subtotal = BigDecimal.ZERO;

        Order order = Order.builder()
                .orderNumber(orderNo)
                .customerId(cart.getCustomerId())
                .buyerEmail(req.getBuyerEmail() != null ? req.getBuyerEmail() : "cliente@empresa.com")
                .currency("PEN")
                .status(OrderStatus.pending_payment)
                .build();
        order = orderRepository.save(order);

        for (CartLine line : lines) {
            ProductVariant variant = variantRepository.findById(line.getVariantId()).orElse(null);
            Product product = variant != null ? productRepository.findById(variant.getProductId()).orElse(null) : null;
            PriceEntry price = priceRepository.findFirstByVariantIdOrderByValidFromDesc(line.getVariantId()).orElse(null);

            BigDecimal unitPrice = price != null ? price.getNetPrice() : new BigDecimal("100.00");
            BigDecimal lineTotal = unitPrice.multiply(line.getQuantity());
            subtotal = subtotal.add(lineTotal);

            orderLineRepository.save(OrderLine.builder()
                    .orderId(order.getId())
                    .variantId(line.getVariantId())
                    .nameSnapshot(product != null ? product.getName() : "Producto Industrial")
                    .skuSnapshot(variant != null ? variant.getReferenceCode() : "SKU-GEN")
                    .quantity(line.getQuantity())
                    .unitPrice(unitPrice)
                    .taxAmount(lineTotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP))
                    .lineTotal(lineTotal)
                    .build());
        }

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = new BigDecimal("80.00");
        BigDecimal grandTotal = subtotal.add(tax).add(shipping);

        order.setSubtotal(subtotal);
        order.setTaxTotal(tax);
        order.setShippingTotal(shipping);
        order.setGrandTotal(grandTotal);
        order = orderRepository.save(order);

        orderStatusEventRepository.save(OrderStatusEvent.builder()
                .orderId(order.getId())
                .fromStatus(null)
                .toStatus(OrderStatus.pending_payment.name())
                .actorRef("CLIENTE_CHECKOUT")
                .build());

        cart.setStatus(CartStatus.converted);
        cartRepository.save(cart);

        return order;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrderDetails(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        List<OrderLine> lines = orderLineRepository.findByOrderId(orderId);
        List<OrderStatusEvent> timeline = orderStatusEventRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        return Map.of("order", order, "lines", lines, "timeline", timeline);
    }

    @Transactional(readOnly = true)
    public Page<Order> getCustomerOrders(UUID customerId, int page, int size) {
        return orderRepository.findByCustomerId(customerId, PageRequest.of(page, Math.min(size, 100)));
    }

    @Transactional
    public Order updateOrderStatus(UUID orderId, UpdateOrderStatusRequest req, String staffUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(req.getStatus());
        order = orderRepository.save(order);

        orderStatusEventRepository.save(OrderStatusEvent.builder()
                .orderId(order.getId())
                .fromStatus(oldStatus.name())
                .toStatus(req.getStatus().name())
                .actorRef(staffUser != null ? staffUser : "STAFF_LOGISTICA")
                .build());
        return order;
    }
}
