package com.perfect8.shop.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Order
 * Version 1.0 - Core functionality only
 *
 * Uses descriptive field names (orderId instead of id) for clarity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private String status;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal totalAmount;
    private String currency;

    // Shipping address
    private String shippingFirstName;
    private String shippingLastName;
    private String shippingEmail;
    private String shippingPhone;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    // Billing address
    private Boolean billingSameAsShipping;
    private String billingFirstName;
    private String billingLastName;
    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    // Order items
    private List<OrderItemDTO> orderItems;

    // Payment info
    private Long paymentId;
    private String paymentStatus;
    private String paymentMethod;

    // Shipment info
    private Long shipmentId;
    private String shipmentStatus;
    private String trackingNumber;
    private String carrier;

    // Notes
    private String customerNotes;
    private String internalNotes;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    // Computed fields
    private boolean cancellable;
    private boolean returnable;
    private boolean paid;

    // Version 2.0 - Commented out for future
    // private BigDecimal discountAmount;
    // private String couponCode;
    // private Integer loyaltyPointsEarned;
    // private Integer loyaltyPointsUsed;
}