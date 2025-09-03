package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryResponse {

    private Long cartId;
    private Long customerId;
    private String sessionId;
    private Integer totalItems;
    private Integer uniqueItems;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String currency;
    private Boolean isEmpty;
    private Boolean hasUnavailableItems;
    private Boolean hasPriceChanges;
    private Integer unavailableItemCount;
    private BigDecimal totalWeight;
    private String weightUnit;
    private Boolean requiresShipping;
    private Boolean isEligibleForFreeShipping;
    private BigDecimal freeShippingThreshold;
    private BigDecimal freeShippingRemaining;
    private List<String> appliedCoupons;
    private List<String> availableOffers;
    private BigDecimal totalSavings;
    private BigDecimal originalTotal;
    private String estimatedDelivery;
    private Boolean hasPerishableItems;
    private Boolean hasDigitalItems;
    private Boolean hasPhysicalItems;
    private LocalDateTime lastModified;
    private LocalDateTime expiresAt;
    private String status;
    private List<ValidationError> validationErrors;
    private List<Warning> warnings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private String errorCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Warning {
        private String type;
        private String message;
        private String actionRequired;
    }
}