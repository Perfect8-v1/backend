package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order DTO for email service
 * Contains order information needed for sending order-related emails
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEmailDTO {

    private Long id;
    private String orderNumber;
    private String status;
    private LocalDateTime orderDate;

    // Customer information
    private String customerEmail;
    private String customerName;
    private String customerPhone;

    // Amounts
    private BigDecimal subtotal;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currency;

    // Shipping information
    private String shippingMethod;
    private String trackingNumber;
    private String estimatedDeliveryDate;

    // Shipping address
    private String shippingName;
    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    // Billing address (if different)
    private String billingName;
    private String billingStreet;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    // Order items
    private List<OrderItemEmailDTO> items;

    // Payment information
    private String paymentMethod;
    private String paymentStatus;

    // Additional information
    private String orderNotes;
    private String giftMessage;

    // Helper methods
    public String getFormattedOrderNumber() {
        return orderNumber != null ? orderNumber : "ORD-" + id;
    }

    public String getFormattedTotalAmount() {
        if (totalAmount != null && currency != null) {
            return currency + " " + totalAmount.toString();
        }
        return totalAmount != null ? totalAmount.toString() : "0.00";
    }

    public String getShippingAddress() {
        StringBuilder address = new StringBuilder();
        if (shippingName != null) address.append(shippingName).append("\n");
        if (shippingStreet != null) address.append(shippingStreet).append("\n");
        if (shippingCity != null) address.append(shippingCity);
        if (shippingState != null) address.append(", ").append(shippingState);
        if (shippingPostalCode != null) address.append(" ").append(shippingPostalCode);
        if (shippingCountry != null) address.append("\n").append(shippingCountry);
        return address.toString();
    }

    public boolean hasTrackingNumber() {
        return trackingNumber != null && !trackingNumber.trim().isEmpty();
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Nested DTO for order items
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEmailDTO {
        private Long productId;
        private String productName;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public String getFormattedTotalPrice() {
            return totalPrice != null ? totalPrice.toString() : "0.00";
        }
    }
}