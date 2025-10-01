package com.perfect8.shop.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingUpdateDTO {

    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    @NotBlank(message = "Carrier is required")
    private String carrier;

    private String estimatedDeliveryDate;
    private String currentStatus;
    private String currentLocation;
    private String notes;
}