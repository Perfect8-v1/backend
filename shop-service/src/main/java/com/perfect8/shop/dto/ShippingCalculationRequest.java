package com.perfect8.shop.dto;

import lombok.*;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingCalculationRequest {

    @NotNull(message = "Destination address is required")
    private String destinationAddress;

    @NotNull(message = "Postal code is required")
    private String postalCode;

    @NotNull(message = "Country is required")
    private String country;

    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal orderAmount;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal weight;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageDimensions {
        private Double length;
        private Double width;
        private Double height;
        private String unit;
    }

    private PackageDimensions packageDimensions;
}