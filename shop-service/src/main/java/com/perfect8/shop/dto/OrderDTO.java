package com.perfect8.shop.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Order
 * Version 1.0 - Core functionality with OrderItems (CORRECTED)
 *
 * MAGNUM OPUS PRINCIPLE: Only essential fields for v1.0
 * CORRECTION: orderItems IS essential - moved back to v1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    // ========== CORE FIELDS (v1.0) ==========
    
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    
    @Builder.Default
    private String customerEmail = "";
    
    @Builder.Default
    private String customerName = "";
    
    private String status;

    // Pricing (v1.0)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private String currency = "SEK";

    // Order items (v1.0 - ESSENTIAL!)
    @Builder.Default
    private List<OrderItemDTO> orderItems = new ArrayList<>();

    // Shipping address (v1.0 - basic)
    @Builder.Default
    private String shippingFirstName = "";
    
    @Builder.Default
    private String shippingLastName = "";
    
    @Builder.Default
    private String shippingEmail = "";
    
    @Builder.Default
    private String shippingPhone = "";
    
    @Builder.Default
    private String shippingAddressLine1 = "";
    
    @Builder.Default
    private String shippingAddressLine2 = "";
    
    @Builder.Default
    private String shippingCity = "";
    
    @Builder.Default
    private String shippingState = "";
    
    @Builder.Default
    private String shippingPostalCode = "";
    
    @Builder.Default
    private String shippingCountry = "";

    // Timestamps (v1.0)
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /* ========== VERSION 2.0 FEATURES - COMMENTED OUT ==========
     * These fields will be uncommented and implemented in version 2.0
     * Reason: Simplify v1.0 to core functionality only
     */
    
    // Billing address (v2.0)
    // private Boolean billingSameAsShipping;
    // private String billingFirstName;
    // private String billingLastName;
    // private String billingAddressLine1;
    // private String billingAddressLine2;
    // private String billingCity;
    // private String billingState;
    // private String billingPostalCode;
    // private String billingCountry;

    // Payment info (v2.0)
    // private Long paymentId;
    // private String paymentStatus;
    // private String paymentMethod;

    // Shipment info (v2.0)
    // private Long shipmentId;
    // private String shipmentStatus;
    // private String trackingNumber;
    // private String carrier;

    // Notes (v2.0)
    // private String customerNotes;
    // private String internalNotes;

    // Extended timestamps (v2.0)
    // private LocalDateTime confirmedAt;
    // private LocalDateTime shippedAt;
    // private LocalDateTime deliveredDate;
    // private LocalDateTime cancelledAt;

    // Computed fields (v2.0)
    // private boolean cancellable;
    // private boolean returnable;
    // private boolean paid;

    // Discounts and loyalty (v2.0)
    // private BigDecimal discountAmount;
    // private String couponCode;
    // private Integer loyaltyPointsEarned;
    // private Integer loyaltyPointsUsed;
    
    /* ========== END VERSION 2.0 FEATURES ========== */
}
