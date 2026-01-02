package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Tax Calculation Request DTO - Version 1.0
 * Simple tax calculation based on order amount
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Order amount to calculate tax on
     */
    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "0.00", message = "Order amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal orderAmount;

    /**
     * Country code for tax calculation
     */
    @Size(max = 2, message = "Country code must be 2 characters")
    private String country;

    /**
     * Optional state/region for US state tax
     */
    @Size(max = 50, message = "State cannot exceed 50 characters")
    private String state;

    /**
     * Optional postal code for specific tax rates
     */
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;
}
