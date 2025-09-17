package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEmailDTO {

    // Order details
    private Long orderId;
    private String orderNumber;  // ADDED: This was missing
    private LocalDateTime orderDate;
    private String orderStatus;

    // Customer information
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Items
    @Builder.Default
    private List<OrderItemDto> items = new ArrayList<>();  // ADDED: This was missing

    // Amounts
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;

    // Addresses
    private String billingAddress;
    private String shippingAddress;

    // Shipping details
    private String shippingMethod;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime shippedDate;

    // Payment
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;

    // Additional information
    private String orderNotes;
    private String giftMessage;
    private boolean isGift;
    private String couponCode;
    private String invoiceNumber;

    // Store information
    private String storeName;
    private String storeEmail;
    private String storePhone;
    private String storeAddress;
    private String storeWebsite;

    // URLs for customer actions
    private String orderViewUrl;
    private String trackingUrl;
    private String invoiceUrl;
    private String returnUrl;

    // Metadata
    private String language;
    private String templateVersion;

    // Helper methods
    public void addItem(OrderItemDto item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public int getTotalItemCount() {
        if (items == null) return 0;
        return items.stream()
                .mapToInt(OrderItemDto::getQuantity)
                .sum();
    }

    public boolean hasTracking() {
        return trackingNumber != null && !trackingNumber.isEmpty();
    }

    public boolean hasDiscount() {
        return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getFormattedOrderNumber() {
        return orderNumber != null ? orderNumber : "ORD-" + orderId;
    }

    public String getFormattedTotalAmount() {
        if (totalAmount == null) return "0.00";
        return String.format("%.2f %s", totalAmount, currency != null ? currency : "SEK");
    }
}