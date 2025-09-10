package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a payment transaction.
 * Version 1.0 - Core payment functionality with PayPal focus
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_transaction", columnList = "transaction_id", unique = true),
        @Index(name = "idx_payment_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"order"})
@ToString(exclude = {"order"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    /**
     * Payment method as String for v1.0 flexibility
     * Primary method is PAYPAL in v1.0
     * Future methods: CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, etc.
     */
    @Column(name = "payment_method", nullable = false, length = 50)
    @Builder.Default
    private String paymentMethod = "PAYPAL";

    /**
     * Payment status
     * Values: PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED
     */
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";

    // Transaction identifiers
    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    // Payment details
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payer_email", length = 255)
    private String payerEmail;

    @Column(name = "payer_name", length = 255)
    private String payerName;

    // Verification
    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    // Refund information
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "is_partial_refund")
    @Builder.Default
    private Boolean isPartialRefund = false;

    // Retry information
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_retry_date")
    private LocalDateTime lastRetryDate;

    // Failure information
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // Additional notes
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================
    // Business methods
    // ========================================

    /**
     * Check if payment is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * Check if payment failed
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * Check if payment is pending
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * Check if payment has been refunded (fully or partially)
     */
    public boolean isRefunded() {
        return "REFUNDED".equals(status) || "PARTIALLY_REFUNDED".equals(status);
    }

    /**
     * Check if payment can be refunded
     */
    public boolean canBeRefunded() {
        return isCompleted() || "PARTIALLY_REFUNDED".equals(status);
    }

    /**
     * Calculate remaining refundable amount
     */
    public BigDecimal getRefundableAmount() {
        if (!canBeRefunded()) {
            return BigDecimal.ZERO;
        }

        BigDecimal refunded = refundAmount != null ? refundAmount : BigDecimal.ZERO;
        return amount.subtract(refunded);
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        if (retryCount == null) {
            retryCount = 0;
        }
        retryCount++;
        lastRetryDate = LocalDateTime.now();
    }

    /**
     * Check if maximum retries exceeded
     */
    public boolean isMaxRetriesExceeded() {
        return retryCount != null && retryCount >= 3;
    }

    /**
     * Get display status
     */
    public String getDisplayStatus() {
        switch (status) {
            case "PENDING":
                return "Payment Pending";
            case "PROCESSING":
                return "Processing Payment";
            case "COMPLETED":
                return "Payment Completed";
            case "FAILED":
                return failureReason != null ? "Failed: " + failureReason : "Payment Failed";
            case "CANCELLED":
                return "Payment Cancelled";
            case "REFUNDED":
                return "Fully Refunded";
            case "PARTIALLY_REFUNDED":
                return "Partially Refunded";
            default:
                return status;
        }
    }

    /**
     * Check if this is a PayPal payment
     */
    public boolean isPayPalPayment() {
        return "PAYPAL".equalsIgnoreCase(paymentMethod);
    }

    @PrePersist
    public void prePersist() {
        if (transactionId == null) {
            transactionId = generateTransactionId();
        }
        if (currency == null) {
            currency = "USD";
        }
        if (paymentMethod == null) {
            paymentMethod = "PAYPAL";
        }
        if (status == null) {
            status = "PENDING";
        }
        if (isVerified == null) {
            isVerified = false;
        }
        if (isPartialRefund == null) {
            isPartialRefund = false;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        return "PAY-" + System.currentTimeMillis() + "-" +
                String.valueOf(System.nanoTime()).substring(7, 13);
    }

    // ========================================
    // Backward compatibility (if needed)
    // ========================================

    /**
     * Get order ID directly
     */
    public Long getOrderId() {
        return order != null ? order.getOrderId() : null;
    }

    /**
     * Get payment amount as string for display
     */
    public String getFormattedAmount() {
        return currency + " " + amount.toString();
    }

    /**
     * Version 2.0 features - commented out
     *
     * In v2.0, we'll add:
     * - Multiple payment method support with proper enum
     * - Payment gateway response details
     * - Fraud detection scores
     * - Payment tokenization
     * - Subscription/recurring payment support
     * - Split payments
     * - Payment method validation rules
     */
}