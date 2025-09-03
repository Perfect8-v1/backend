package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for creating new orders
 * Version 1.0 - Core order creation functionality
 *
 * Critical: This is where orders begin!
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Customer ID - will be set by controller from authenticated user
     */
    private Long customerId;

    /**
     * Order items - CRITICAL!
     */
    @NotEmpty(message = "Order must contain at least one item")
    @Size(max = 100, message = "Cannot order more than 100 different items")
    @Valid
    private List<OrderItemDTO> items;

    /**
     * Shipping address ID from customer's saved addresses
     */
    @NotNull(message = "Shipping address is required")
    @Positive(message = "Invalid shipping address ID")
    private Long shippingAddressId;

    /**
     * Billing address ID (optional, defaults to shipping)
     */
    @Positive(message = "Invalid billing address ID")
    private Long billingAddressId;

    /**
     * Shipping method - affects cost and delivery time
     */
    @NotBlank(message = "Shipping method is required")
    @Pattern(regexp = "^(STANDARD|EXPRESS|OVERNIGHT|ECONOMY|PICKUP)$",
            message = "Invalid shipping method. Must be STANDARD, EXPRESS, OVERNIGHT, ECONOMY, or PICKUP")
    @Builder.Default
    private String shippingMethod = "STANDARD";

    /**
     * Payment method
     */
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(CREDIT_CARD|DEBIT_CARD|PAYPAL|STRIPE|BANK_TRANSFER|COD)$",
            message = "Invalid payment method")
    private String paymentMethod;

    /**
     * Payment token/reference from payment processor
     * Critical for payment processing!
     */
    private String paymentToken;

    /**
     * Order notes / special instructions
     */
    @Size(max = 1000, message = "Order notes cannot exceed 1000 characters")
    private String orderNotes;

    /**
     * Delivery instructions (gate code, etc.)
     */
    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;

    /**
     * Preferred delivery date (optional)
     */
    @Future(message = "Delivery date must be in the future")
    private LocalDate preferredDeliveryDate;

    /**
     * Contact phone for delivery issues - CRITICAL!
     */
    @NotBlank(message = "Contact phone is required for delivery coordination")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Invalid phone number format")
    private String contactPhone;

    /**
     * Email for order updates
     */
    @Email(message = "Invalid email format")
    private String notificationEmail;

    /**
     * Gift order flag
     */
    @Builder.Default
    private boolean isGift = false;

    /**
     * Gift message (if gift order)
     */
    @Size(max = 500, message = "Gift message cannot exceed 500 characters")
    private String giftMessage;

    /**
     * Gift recipient name
     */
    @Size(max = 100, message = "Gift recipient name cannot exceed 100 characters")
    private String giftRecipientName;

    /**
     * Whether to include prices on packing slip (for gifts)
     */
    @Builder.Default
    private boolean includePricesOnSlip = true;

    /**
     * Signature required on delivery
     */
    @Builder.Default
    private boolean signatureRequired = false;

    /**
     * Insurance requested
     */
    @Builder.Default
    private boolean insuranceRequested = false;

    /**
     * Insurance amount (if requested)
     */
    @DecimalMin(value = "0.00", message = "Insurance amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid insurance amount format")
    private BigDecimal insuranceAmount;

    /**
     * Order source/channel
     */
    @Pattern(regexp = "^(WEB|MOBILE_APP|API|PHONE|STORE)$",
            message = "Invalid order source")
    @Builder.Default
    private String orderSource = "WEB";

    /**
     * IP address for fraud prevention
     */
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$",
            message = "Invalid IP address format")
    private String customerIp;

    /**
     * Device fingerprint for fraud prevention
     */
    @Size(max = 255, message = "Device fingerprint cannot exceed 255 characters")
    private String deviceFingerprint;

    /**
     * Calculated subtotal (for validation)
     */
    @DecimalMin(value = "0.01", message = "Subtotal must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid subtotal format")
    private BigDecimal subtotal;

    /**
     * Shipping cost
     */
    @DecimalMin(value = "0.00", message = "Shipping cost cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid shipping cost format")
    private BigDecimal shippingAmount;

    /**
     * Tax amount
     */
    @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid tax amount format")
    private BigDecimal taxAmount;

    /**
     * Total amount to charge
     */
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid total format")
    private BigDecimal totalAmount;

    /**
     * Currency code
     */
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3-letter ISO code")
    @Builder.Default
    private String currency = "USD";

    /**
     * Terms accepted flag
     */
    @AssertTrue(message = "You must accept the terms and conditions")
    private boolean termsAccepted;

    /**
     * Newsletter opt-in
     */
    @Builder.Default
    private boolean newsletterOptIn = false;

    // Utility methods

    /**
     * Get effective billing address ID
     */
    public Long getEffectiveBillingAddressId() {
        return billingAddressId != null ? billingAddressId : shippingAddressId;
    }

    /**
     * Check if express shipping
     */
    public boolean isExpressShipping() {
        return "EXPRESS".equals(shippingMethod) ||
                "OVERNIGHT".equals(shippingMethod);
    }

    /**
     * Check if this requires immediate processing
     */
    public boolean isPriorityOrder() {
        return "OVERNIGHT".equals(shippingMethod);
    }

    /**
     * Check if this is a pickup order
     */
    public boolean isPickupOrder() {
        return "PICKUP".equals(shippingMethod);
    }

    /**
     * Check if COD payment
     */
    public boolean isCashOnDelivery() {
        return "COD".equals(paymentMethod);
    }

    /**
     * Validate order totals
     */
    @AssertTrue(message = "Order total does not match calculated total")
    private boolean isTotalValid() {
        if (subtotal == null || shippingAmount == null ||
                taxAmount == null || totalAmount == null) {
            return false;
        }

        BigDecimal calculated = subtotal
                .add(shippingAmount)
                .add(taxAmount);

        // Allow small rounding difference (0.01)
        return totalAmount.subtract(calculated).abs()
                .compareTo(new BigDecimal("0.01")) <= 0;
    }

    /**
     * Validate gift fields
     */
    @AssertTrue(message = "Gift message required for gift orders")
    private boolean isGiftValid() {
        if (!isGift) {
            return true;
        }
        return giftMessage != null && !giftMessage.trim().isEmpty();
    }

    /**
     * Validate insurance
     */
    @AssertTrue(message = "Insurance amount required when insurance requested")
    private boolean isInsuranceValid() {
        if (!insuranceRequested) {
            return true;
        }
        return insuranceAmount != null &&
                insuranceAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Validate items have required fields
     */
    @AssertTrue(message = "All items must have valid product ID and quantity")
    private boolean areItemsValid() {
        if (items == null || items.isEmpty()) {
            return false;
        }

        for (OrderItemDTO item : items) {
            if (item.getProductId() == null ||
                    item.getQuantity() == null ||
                    item.getQuantity() <= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get total items count
     */
    public int getTotalItemsCount() {
        if (items == null) {
            return 0;
        }
        return items.stream()
                .mapToInt(OrderItemDTO::getQuantity)
                .sum();
    }

    /**
     * Check if order has digital items (affects shipping)
     */
    public boolean hasDigitalItems() {
        if (items == null) {
            return false;
        }
        return items.stream()
                .anyMatch(item -> Boolean.TRUE.equals(item.getIsDigital()));
    }

    /**
     * Check if order has physical items
     */
    public boolean hasPhysicalItems() {
        if (items == null) {
            return false;
        }
        return items.stream()
                .anyMatch(item -> !Boolean.TRUE.equals(item.getIsDigital()));
    }

    // Version 2.0 fields - commented out
    /*
    // Coupon/discount - Version 2.0
    private String couponCode;
    private BigDecimal discountAmount;
    private BigDecimal storeCredit;

    // Loyalty points - Version 2.0
    private Integer loyaltyPointsUsed;
    private Integer loyaltyPointsEarned;

    // Subscription - Version 2.0
    private boolean isSubscriptionOrder;
    private String subscriptionFrequency;

    // Affiliate tracking - Version 2.0
    private String affiliateCode;
    private String referralSource;
    */
}