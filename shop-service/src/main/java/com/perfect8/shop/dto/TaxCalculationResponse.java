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
public class TaxCalculationResponse {

    private BigDecimal subtotal;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalAmount;
    private String currency;

    private BigDecimal taxRate;
    private String taxJurisdiction;
    private String calculationMethod;

    private List<TaxBreakdownItem> taxBreakdown;
    private List<LineItemTax> lineItemTaxes;

    private Boolean isTaxExempt;
    private String exemptionReason;

    private String calculationId;
    private String provider;
    private Map<String, Object> providerResponse;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxBreakdownItem {
        private String taxType;
        private String description;
        private BigDecimal rate;
        private BigDecimal amount;
        private String jurisdiction;
        private String authority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItemTax {
        private Long productId;
        private String productSku;
        private BigDecimal lineSubtotal;
        private BigDecimal lineTaxAmount;
        private BigDecimal effectiveTaxRate;
        private List<TaxBreakdownItem> lineTaxBreakdown;
        private Boolean isTaxable;
        private String taxCategory;
    }
}