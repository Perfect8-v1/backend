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
public class TaxEstimationResponse {

    private BigDecimal subtotal;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String taxJurisdiction;
    private String taxRegion;
    private String postalCode;
    private String country;
    private String state;
    private String city;
    private Boolean taxExempt;
    private String taxExemptReason;
    private List<TaxBreakdown> taxBreakdowns;
    private Map<String, BigDecimal> taxRatesByType;
    private BigDecimal federalTax;
    private BigDecimal stateTax;
    private BigDecimal localTax;
    private BigDecimal salesTax;
    private BigDecimal vatTax;
    private BigDecimal gstTax;
    private BigDecimal hstTax;
    private String calculationMethod;
    private String taxProvider;
    private Boolean isEstimate;
    private String disclaimerText;
    private String lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxBreakdown {
        private String taxType;
        private String taxName;
        private BigDecimal rate;
        private BigDecimal amount;
        private String jurisdiction;
        private String description;
    }
}