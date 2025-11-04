package com.perfect8.shop.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOptionDTO {

    private Long shippingOptionId;
    private String shippingMethod;  // Rätt namn, inte "method"
    private String carrier;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private Integer estimatedDaysMin;
    private Integer estimatedDaysMax;
    private String deliveryTimeframe;
    private Boolean trackingAvailable;
    private Boolean signatureRequired;
    private Boolean insuranceAvailable;
    private BigDecimal insurancePrice;
    private BigDecimal maxWeight;
    private String serviceLevel;
    private Boolean available;
    private String unavailableReason;
}