package com.fundigsac.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "variant_attribute_values", indexes = {
        @Index(name = "idx_var_attr_unique", columnList = "variant_id, attribute_id", unique = true),
        @Index(name = "idx_var_attr_val_text", columnList = "attribute_id, value_text")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Column(name = "attribute_id", nullable = false)
    private UUID attributeId;

    @Column(name = "value_text", length = 250)
    private String valueText;

    @Column(name = "value_number", precision = 16, scale = 4)
    private BigDecimal valueNumber;

    @Column(name = "value_bool")
    private Boolean valueBool;
}
