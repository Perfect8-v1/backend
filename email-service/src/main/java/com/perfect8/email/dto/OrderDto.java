package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order DTO for email service communication with shop service
 * Version 1.0 - Core order functionality for transactional emails
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long orderDtoId;
    private String orderReference;
    private String status; // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED

    // Customer information
    private Long customerId;
    private String customerEmail;
    private String customerFirstName;
    private String customerLastName;
    private String customerPhone;

    // Order details
    private List<OrderItemDto> items;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String currency;

    // Shipping information
    private ShippingInfo shippingInfo;
    private String shippingMethod;
    private String deliveryInstructions;

    // Billing information
    private BillingAddress billingAddress;

    // Payment information
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private String transactionId;

    // Timestamps
    private LocalDateTime orderDate;
    private LocalDateTime confirmationDate;
    private LocalDateTime shippingDate;
    private LocalDateTime deliveryDate;
    private LocalDateTime cancelledDate;

    // Additional fields for email context
    private String orderUrl; // Link to view order in customer portal
    private String invoiceUrl; // Link to download invoice
    private String trackingUrl; // Direct tracking link

    // Error handling for fallback scenarios
    private String errorMessage;

    // Nested classes for complex objects

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInfo {
        private Long orderId;
        private String recipientName;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String trackingNumber;
        private String carrier;
        private String estimatedDelivery;
        private String shippingStatus;
        private String shippingAddress; // Formatted full address

        // Helper method to get formatted address
        public String getFormattedAddress() {
            if (shippingAddress != null) {
                return shippingAddress;
            }

            StringBuilder sb = new StringBuilder();
            if (recipientName != null) sb.append(recipientName).append("\n");
            if (addressLine1 != null) sb.append(addressLine1).append("\n");
            if (addressLine2 != null) sb.append(addressLine2).append("\n");
            if (city != null) sb.append(city);
            if (state != null) sb.append(", ").append(state);
            if (postalCode != null) sb.append(" ").append(postalCode);
            if (country != null) sb.append("\n").append(country);

            return sb.toString();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingAddress {
        private String name;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String phone;

        public String getFormattedAddress() {
            StringBuilder sb = new StringBuilder();
            if (name != null) sb.append(name).append("\n");
            if (addressLine1 != null) sb.append(addressLine1).append("\n");
            if (addressLine2 != null) sb.append(addressLine2).append("\n");
            if (city != null) sb.append(city);
            if (state != null) sb.append(", ").append(state);
            if (postalCode != null) sb.append(" ").append(postalCode);
            if (country != null) sb.append("\n").append(country);

            return sb.toString();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentStatus {
        private Long orderId;
        private String status; // PENDING, COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED
        private String paymentMethod; // PAYPAL, CREDIT_CARD, DEBIT_CARD, etc.
        private BigDecimal amount;
        private String currency;
        private String transactionId;
        private String paypalOrderId; // For PayPal specific
        private LocalDateTime paymentDate;
        private String failureReason;
        private BigDecimal refundedAmount;
        private LocalDateTime refundDate;
    }

    // Helper methods for email templates

    public String getCustomerFullName() {
        if (customerFirstName != null && customerLastName != null) {
            return customerFirstName + " " + customerLastName;
        } else if (customerFirstName != null) {
            return customerFirstName;
        } else if (customerLastName != null) {
            return customerLastName;
        }
        return "Valued Customer";
    }

    public boolean isShipped() {
        return "SHIPPED".equals(status) || "DELIVERED".equals(status);
    }

    public boolean isDelivered() {
        return "DELIVERED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isRefunded() {
        return "REFUNDED".equals(status) ||
                (paymentStatus != null && "REFUNDED".equals(paymentStatus.getStatus()));
    }

    public boolean isPaid() {
        return paymentStatus != null && "COMPLETED".equals(paymentStatus.getStatus());
    }

    public boolean hasTrackingInfo() {
        return shippingInfo != null &&
                shippingInfo.getTrackingNumber() != null &&
                !shippingInfo.getTrackingNumber().isEmpty();
    }

    public String getFormattedOrderTotal() {
        if (totalAmount != null && currency != null) {
            return currency + " " + totalAmount.toString();
        }
        return "";
    }

    // Version 2.0 features - commented out
    /*
    private List<String> appliedCoupons;
    private BigDecimal discountAmount;
    private CustomerLoyaltyInfo loyaltyInfo;
    private List<OrderNote> orderNotes;
    private MarketingPreferences marketingPrefs;
    */
}