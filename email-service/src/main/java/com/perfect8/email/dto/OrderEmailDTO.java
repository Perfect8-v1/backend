package com.perfect8.email.dto;

import com.perfect8.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Email DTO - Version 1.0
 * Följer "Less Strings, More Objects" principen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEmailDTO {

    // Order details
    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;  // CHANGED: Enum istället för String!

    // Customer information
    private Long customerId;
    private String customerName;
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
    private String currency;  // OK som String (ISO kod som "SEK", "USD")

    // Addresses - Version 1.0: Behåll som String för enkelhet
    // TODO v2.0: Skapa Address objekt med street, city, postal, country
    private String billingAddress;
    private String shippingAddress;

    // Shipping details
    private String shippingMethod;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime shippedDate;

    // Payment
    private String paymentMethod;  // OK som String i v1.0
    private String paymentStatus;   // OK som String i v1.0
    private String transactionId;

    // Additional information
    private String orderNotes;
    private String giftMessage;
    private boolean isGift;  // Boolean är bättre än String
    private String couponCode;  // Tas bort i v2.0 (Coupon-funktionalitet)
    private String invoiceNumber;

    // Store information - v1.0: Behåll enkelt
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
    private String language;  // OK som ISO kod
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

    /**
     * Hjälpmetod för att få status som text när det behövs
     */
    public String getOrderStatusText() {
        return orderStatus != null ? orderStatus.name() : "UNKNOWN";
    }

    /**
     * Hjälpmetod för att sätta status från String (för bakåtkompatibilitet)
     */
    public void setOrderStatusFromString(String statusString) {
        if (statusString != null && !statusString.isEmpty()) {
            try {
                this.orderStatus = OrderStatus.valueOf(statusString.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Log och sätt default
                this.orderStatus = OrderStatus.PENDING;
            }
        }
    }
}