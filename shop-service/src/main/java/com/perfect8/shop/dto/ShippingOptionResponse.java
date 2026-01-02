package com.perfect8.shop.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOptionResponse {

    private List<ShippingOptionDTO> availableOptions;
    private String defaultOption;
    private BigDecimal freeShippingThreshold;  // Detta saknades
    private BigDecimal currentOrderValue;
    private BigDecimal amountToFreeShipping;
    private Boolean freeShippingEligible;
    private String estimatedDeliveryDate;
    private String shippingAddress;
}