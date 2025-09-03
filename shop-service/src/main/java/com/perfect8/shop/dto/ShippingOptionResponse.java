package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for shipping options calculation.
 * Used by CartController and ShippingService.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOptionResponse {

    /**
     * The address these shipping options are calculated for
     */
    private String address;

    /**
     * List of available shipping options
     */
    private List<ShippingOptionDTO> options;

    /**
     * Estimated delivery date for standard shipping
     */
    private LocalDate estimatedDeliveryDate;

    /**
     * Any shipping restrictions or warnings
     */
    private List<String> restrictions;

    /**
     * Whether express options are available
     */
    @Builder.Default
    private Boolean expressAvailable = true;

    /**
     * Whether international shipping is available
     */
    @Builder.Default
    private Boolean internationalAvailable = false;

    /**
     * Error message if calculation failed
     */
    private String errorMessage;

    /**
     * Whether the calculation was successful
     */
    @Builder.Default
    private Boolean success = true;

    /**
     * Get cheapest shipping option
     */
    public ShippingOptionDTO getCheapestOption() {
        if (options == null || options.isEmpty()) {
            return null;
        }

        return options.stream()
                .min((o1, o2) -> o1.getPrice().compareTo(o2.getPrice()))
                .orElse(null);
    }

    /**
     * Get fastest shipping option
     */
    public ShippingOptionDTO getFastestOption() {
        if (options == null || options.isEmpty()) {
            return null;
        }

        return options.stream()
                .min((o1, o2) -> o1.getEstimatedDays().compareTo(o2.getEstimatedDays()))
                .orElse(null);
    }

    /**
     * Check if free shipping is available
     */
    public boolean hasFreeShipping() {
        if (options == null || options.isEmpty()) {
            return false;
        }

        return options.stream()
                .anyMatch(option -> option.getPrice().compareTo(BigDecimal.ZERO) == 0);
    }

    /**
     * Get standard shipping option
     */
    public ShippingOptionDTO getStandardOption() {
        if (options == null || options.isEmpty()) {
            return null;
        }

        return options.stream()
                .filter(option -> !option.getIsExpress())
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if any options are available
     */
    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }

    /**
     * Get option by carrier
     */
    public ShippingOptionDTO getOptionByCarrier(String carrier) {
        if (options == null || options.isEmpty() || carrier == null) {
            return null;
        }

        return options.stream()
                .filter(option -> carrier.equalsIgnoreCase(option.getCarrier()))
                .findFirst()
                .orElse(null);
    }
}