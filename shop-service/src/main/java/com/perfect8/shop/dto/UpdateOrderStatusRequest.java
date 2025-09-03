package com.perfect8.shop.dto;

import com.perfect8.shop.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating order status.
 * Version 1.0 - Core functionality only
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    // For shipping status
    private String trackingNumber;
    private String carrier;
    private String estimatedDeliveryDate;

    // For cancellation
    private String cancellationReason;

    // For return
    private String returnReason;
    private String returnTrackingNumber;

    // For delivery
    private String deliveryNotes;
    private String deliveredTo;

    // Notification settings
    @Builder.Default
    private Boolean notifyCustomer = true;

    private String customNotificationMessage;
}