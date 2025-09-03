package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    // Order information
    @NotNull(message = "Order ID is required")
    private Long orderId;

    // Customer information
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    // Payment amount
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    // Payment method
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // "PAYPAL", "CREDIT_CARD", "DEBIT_CARD", etc.

    // Transaction information - FIXED: Added transactionId field
    private String transactionId;  // External payment provider transaction ID

    // Currency
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currency; // ISO 4217 currency code (e.g., "USD", "EUR")

    // PayPal specific fields
    private String paypalOrderId;
    private String paypalPayerId;
    private String paypalPaymentId;
    private String paypalToken;

    // Credit/Debit card fields (for future use)
    private String cardToken; // Tokenized card data for security
    private String cardLast4Digits;
    private String cardType; // "VISA", "MASTERCARD", "AMEX", etc.
    private String cardHolderName;

    // Billing address
    private AddressDTO billingAddress;

    // Additional payment information
    private String description;
    private String invoiceNumber;
    private String referenceNumber;

    // 3D Secure / verification
    private String verificationCode;
    private boolean threeDSecureRequired;
    private String threeDSecureToken;

    // Risk assessment
    private String ipAddress;
    private String userAgent;
    private String deviceId;

    // Metadata
    private String metadata; // JSON string for additional data

    // Recurring payment information (for subscriptions)
    private boolean isRecurring;
    private String subscriptionId;
    private Integer installmentNumber;
    private Integer totalInstallments;

    // Discount/Coupon
    private String couponCode;
    private BigDecimal discountAmount;

    // Tax information
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal subtotalAmount;

    // Status and timestamps
    private String status; // "PENDING", "PROCESSING", "COMPLETED", "FAILED"
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    // Return URL for payment gateway redirects
    private String returnUrl;
    private String cancelUrl;
    private String webhookUrl;

    // Idempotency key to prevent duplicate payments
    private String idempotencyKey;

    // Notes
    private String customerNotes;
    private String internalNotes;

    // Validation helpers
    public boolean isPayPalPayment() {
        return "PAYPAL".equalsIgnoreCase(paymentMethod);
    }

    public boolean isCreditCardPayment() {
        return "CREDIT_CARD".equalsIgnoreCase(paymentMethod) ||
                "DEBIT_CARD".equalsIgnoreCase(paymentMethod);
    }

    public boolean hasValidAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasTransactionId() {
        return transactionId != null && !transactionId.trim().isEmpty();
    }

    public boolean hasPayPalData() {
        return paypalOrderId != null || paypalPayerId != null || paypalPaymentId != null;
    }

    public boolean hasCardData() {
        return cardToken != null && !cardToken.trim().isEmpty();
    }

    public boolean hasBillingAddress() {
        return billingAddress != null;
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

        if (discountAmount != null) {
            total = total.subtract(discountAmount);
        }

        return total;
    }

    // Generate idempotency key if not provided
    public String generateIdempotencyKey() {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            idempotencyKey = String.format("%d-%d-%d-%s",
                    orderId,
                    customerId,
                    System.currentTimeMillis(),
                    paymentMethod);
        }
        return idempotencyKey;
    }

    // Set current timestamp for request
    public void setRequestTimestamp() {
        this.requestedAt = LocalDateTime.now();
    }

    // Mark as processed
    public void markAsProcessed() {
        this.processedAt = LocalDateTime.now();
        if (this.status == null || "PENDING".equals(this.status)) {
            this.status = "PROCESSING";
        }
    }

    // Validate required fields based on payment method
    public boolean isValid() {
        // Basic validation
        if (!hasValidAmount() || orderId == null || customerId == null) {
            return false;
        }

        // Payment method specific validation
        if (isPayPalPayment()) {
            // PayPal requires at least one PayPal identifier
            return hasPayPalData() || hasTransactionId();
        } else if (isCreditCardPayment()) {
            // Card payment requires card token
            return hasCardData();
        }

        // Other payment methods just need basic fields
        return true;
    }

    @Override
    public String toString() {
        // Mask sensitive data in toString
        return "PaymentRequestDTO{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", transactionId='" + (transactionId != null ? "***" + transactionId.substring(Math.max(0, transactionId.length() - 4)) : null) + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}