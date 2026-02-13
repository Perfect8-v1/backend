package com.perfect8.email.dto;

import com.perfect8.common.enums.OrderStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Email DTO - Version 1.1
 * Följer "Less Strings, More Objects" principen
 * 
 * v1.1: Lagt till email-validering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEmailDTO {

    // Order details
    private Long orderId;
    
    @NotBlank(message = "Order number is required")
    private String orderNumber;
    
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;

    // Customer information
    private Long customerId;
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    private String customerPhone;

    // Items
    @Builder.Default
    private List<OrderItemDto> items = new ArrayList<>();

    // Amounts (Objects redan - BigDecimal)
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

    public boolean hasShipping() {
        return shippingAddress != null && !shippingAddress.isEmpty();
    }

    public String getFormattedOrderNumber() {
        return orderNumber != null ? orderNumber : "ORD-" + orderId;
    }

    public String getFormattedTotalAmount() {
        if (totalAmount == null) return "0.00";
        return String.format("%.2f %s", totalAmount, currency != null ? currency : "SEK");
    }

    public String getOrderStatusText() {
        return orderStatus != null ? orderStatus.name() : "UNKNOWN";
    }

    public void setOrderStatusFromString(String statusString) {
        if (statusString != null && !statusString.isEmpty()) {
            try {
                this.orderStatus = OrderStatus.valueOf(statusString.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.orderStatus = OrderStatus.PENDING;
            }
        }
    }
}
