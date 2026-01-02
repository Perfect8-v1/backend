package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartCalculationResponse {

    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private Integer totalItems;
    private Integer totalQuantity;

    private TaxBreakdown taxBreakdown;
    private ShippingBreakdown shippingBreakdown;
    private DiscountBreakdown discountBreakdown;

    private List<String> appliedPromotions;
    private List<String> availablePromotions;
    private Map<String, BigDecimal> fees;

    private Boolean isValid;
    private List<String> warnings;
    private List<String> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxBreakdown {
        private BigDecimal stateTax;
        private BigDecimal federalTax;
        private BigDecimal localTax;
        private BigDecimal vatTax;
        private String taxJurisdiction;
        private List<TaxLineItem> lineItems;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxLineItem {
        private String description;
        private BigDecimal rate;
        private BigDecimal amount;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingBreakdown {
        private BigDecimal baseCost;
        private BigDecimal handlingFee;
        private BigDecimal insuranceFee;
        private BigDecimal expediteFee;
        private String method;
        private String carrier;
        private Integer estimatedDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountBreakdown {
        private String couponCode;
        private String promotionName;
        private BigDecimal amount;
        private BigDecimal percentage;
        private String type;
        private List<String> appliedToItems;
        private BigDecimal maxDiscount;
        private BigDecimal remainingDiscount;
    }
}