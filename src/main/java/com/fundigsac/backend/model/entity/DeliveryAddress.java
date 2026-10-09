package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "delivery_addresses", indexes = {
        @Index(name = "idx_deliv_addr_cust", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "recipient_name", nullable = false, length = 180)
    private String recipientName;

    @Column(name = "address_lines", nullable = false, columnDefinition = "TEXT")
    private String addressLines;

    @Column(length = 8)
    private String ubigeo;

    @Column(nullable = false, length = 120)
    private String region;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;
}
