package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Tax Calculation Response DTO - Version 1.0
 * Contains calculated tax information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Calculated tax amount
     */
    private BigDecimal taxAmount;

    /**
     * Tax rate applied (e.g., 0.25 for 25%)
     */
    private BigDecimal taxRate;

    /**
     * Amount tax was calculated on
     */
    private BigDecimal taxableAmount;

    /**
     * Country code
     */
    private String country;

    /**
     * Optional state/region
     */
    private String state;

    /**
     * Tax description (e.g., "Swedish VAT 25%")
     */
    private String taxDescription;

    /**
     * Whether tax is included in product prices
     */
    @Builder.Default
    private boolean taxIncluded = true;
}
