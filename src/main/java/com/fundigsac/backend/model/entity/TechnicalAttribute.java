package com.fundigsac.backend.model.entity;

import com.fundigsac.backend.model.enums.AttributeDataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "technical_attributes", indexes = {
        @Index(name = "idx_tech_attr_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80, unique = true)
    private String code;

    @Column(nullable = false, length = 160)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 32)
    @Builder.Default
    private AttributeDataType dataType = AttributeDataType.text;

    @Column(length = 32)
    private String unit;

    @Column(nullable = false)
    @Builder.Default
    private Boolean filterable = false;
}
