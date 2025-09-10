package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Payment Request DTO - Version 1.0
 * Simplified for core PayPal payment functionality
 *
 * Version 1.0 focuses on essential payment fields:
 * - Order and amount information
 * - PayPal integration fields
 * - Basic transaction tracking
 *
 * Version 2.0 will add:
 * - Credit card processing
 * - Recurring payments/subscriptions
 * - Advanced fraud detection
 * - Multiple payment gateways
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    // Order information - Essential for v1.0
    @NotNull(message = "Order ID is required")
    private Long orderId;

    // Payment amount - Essential for v1.0
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    // Currency - Essential for v1.0
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Builder.Default
    private String currency = "USD"; // ISO 4217 currency code

    // Payment method - Essential for v1.0
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "PAYPAL|CREDIT_CARD|DEBIT_CARD", message = "Invalid payment method")
    @Builder.Default
    private String paymentMethod = "PAYPAL"; // Default to PayPal for v1.0

    // Transaction ID - Essential for tracking
    private String transactionId;  // External payment provider transaction ID

    // PayPal specific fields - Essential for v1.0
    private String paypalOrderId;
    private String paypalPayerId;
    private String paypalPaymentId;
    private String paypalToken;

    // Return URLs for PayPal redirect flow - Essential for v1.0
    private String returnUrl;
    private String cancelUrl;

    // Basic payment information
    private String description;
    private String invoiceNumber;
    private String referenceNumber;

    // Customer notes
    private String customerNotes;

    // Tax and shipping (needed for accurate payment processing)
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal subtotalAmount;

    // Validation helpers for v1.0
    public boolean isPayPalPayment() {
        return "PAYPAL".equalsIgnoreCase(paymentMethod);
    }

    public boolean hasValidAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasTransactionId() {
        return transactionId != null && !transactionId.trim().isEmpty();
    }

    public boolean hasPayPalData() {
        return paypalOrderId != null || paypalPayerId != null ||
                paypalPaymentId != null || paypalToken != null;
    }

    // Calculate total amount including tax and shipping
    public BigDecimal calculateTotalAmount() {
        BigDecimal total = amount != null ? amount : BigDecimal.ZERO;

        if (taxAmount != null) {
            total = total.add(taxAmount);
        }

        if (shippingAmount != null) {
            total = total.add(shippingAmount);
        }

        return total;
    }

    // Validate required fields for v1.0
    public boolean isValid() {
        // Basic validation
        if (!hasValidAmount() || orderId == null) {
            return false;
        }

        // PayPal requires at least one PayPal identifier or transaction ID
        if (isPayPalPayment()) {
            return hasPayPalData() || hasTransactionId();
        }

        // For v1.0, we primarily support PayPal
        return true;
    }

    /* ============================================
     * VERSION 2.0 FIELDS - Reserved for future use
     * ============================================
     * These fields will be added in version 2.0:
     *
     * // Customer information (v1.0 gets from JWT token)
     * - Long customerId
     *
     * // Credit/Debit card fields
     * - String cardToken
     * - String cardLast4Digits
     * - String cardType (VISA, MASTERCARD, AMEX)
     * - String cardHolderName
     *
     * // Billing address
     * - AddressDTO billingAddress
     *
     * // 3D Secure verification
     * - String verificationCode
     * - boolean threeDSecureRequired
     * - String threeDSecureToken
     *
     * // Risk assessment & fraud detection
     * - String ipAddress
     * - String userAgent
     * - String deviceId
     *
     * // Advanced features
     * - String metadata (JSON for additional data)
     * - boolean isRecurring
     * - String subscriptionId
     * - Integer installmentNumber
     * - Integer totalInstallments
     *
     * // Coupons & discounts (v2.0 feature)
     * - String couponCode
     * - BigDecimal discountAmount
     *
     * // Webhooks & callbacks
     * - String webhookUrl
     *
     * // Idempotency for duplicate prevention
     * - String idempotencyKey
     *
     * // Status tracking
     * - String status
     * - LocalDateTime requestedAt
     * - LocalDateTime processedAt
     *
     * // Internal notes
     * - String internalNotes
     */
}