package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a payment transaction.
 * Version 1.0 - Core functionality only
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_transaction_id", columnList = "transaction_id", unique = true),
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_payment_status", columnList = "payment_status"),
        @Index(name = "idx_created_at", columnList = "created_at")
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
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    // Payment gateway information
    @Column(name = "gateway", nullable = false, length = 50)
    @Builder.Default
    private String gateway = "PAYPAL";

    @Column(name = "gateway_payment_id", length = 255)
    private String gatewayPaymentId;

    @Column(name = "gateway_order_id", length = 255)
    private String gatewayOrderId;

    @Column(name = "gateway_status", length = 100)
    private String gatewayStatus;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    // Payer information (from payment gateway)
    @Column(name = "payer_email", length = 255)
    private String payerEmail;

    @Column(name = "payer_name", length = 255)
    private String payerName;

    @Column(name = "payer_id", length = 100)
    private String payerId;

    // Refund information
    @Column(name = "is_refunded", nullable = false)
    @Builder.Default
    private Boolean isRefunded = false;

    @Column(name = "refunded_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @Column(name = "refund_transaction_id", length = 100)
    private String refundTransactionId;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // Additional payment details
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    // Processing fees
    @Column(name = "processing_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal processingFee = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    // Important dates
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================
    // Inner Enums
    // ========================================

    /**
     * Payment method enum
     */
    public enum PaymentMethod {
        PAYPAL("PayPal"),
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        BANK_TRANSFER("Bank Transfer"),
        STRIPE("Stripe"),
        SQUARE("Square"),
        APPLE_PAY("Apple Pay"),
        GOOGLE_PAY("Google Pay"),
        CASH_ON_DELIVERY("Cash on Delivery"),
        CHECK("Check"),
        OTHER("Other");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * Convert from string to enum
         */
        public static PaymentMethod fromString(String value) {
            if (value == null) {
                return null;
            }
            for (PaymentMethod method : PaymentMethod.values()) {
                if (method.name().equalsIgnoreCase(value) ||
                        method.displayName.equalsIgnoreCase(value)) {
                    return method;
                }
            }
            return OTHER;
        }
    }

    /**
     * Payment status enum
     */
    public enum PaymentStatus {
        PENDING("Pending", false),
        PROCESSING("Processing", false),
        COMPLETED("Completed", true),
        FAILED("Failed", true),
        CANCELLED("Cancelled", true),
        REFUNDED("Refunded", true),
        PARTIALLY_REFUNDED("Partially Refunded", true),
        EXPIRED("Expired", true),
        REQUIRES_ACTION("Requires Action", false),
        AUTHORIZED("Authorized", false);

        private final String displayName;
        private final boolean isFinal;

        PaymentStatus(String displayName, boolean isFinal) {
            this.displayName = displayName;
            this.isFinal = isFinal;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isFinal() {
            return isFinal;
        }

        public boolean isSuccessful() {
            return this == COMPLETED || this == AUTHORIZED;
        }

        /**
         * Convert from string to enum
         */
        public static PaymentStatus fromString(String value) {
            if (value == null) {
                return null;
            }
            for (PaymentStatus status : PaymentStatus.values()) {
                if (status.name().equalsIgnoreCase(value) ||
                        status.displayName.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            return PENDING;
        }
    }

    // ========================================
    // Backward compatibility methods
    // ========================================

    /**
     * Backward compatibility getter for paymentId
     * @return the payment id
     */
    public Long getPaymentId() {
        return this.id;
    }

    /**
     * Backward compatibility setter for paymentId
     * @param paymentId the payment id to set
     */
    public void setPaymentId(Long paymentId) {
        this.id = paymentId;
    }

    /**
     * Get customer from order (backward compatibility)
     * @return the customer associated with the order
     */
    public Customer getCustomer() {
        return order != null ? order.getCustomer() : null;
    }

    // ========================================
    // Business logic methods
    // ========================================

    /**
     * Generate transaction ID if not set
     */
    @PrePersist
    public void generateTransactionId() {
        if (transactionId == null) {
            // Format: PAY-YYYYMMDD-XXXXXX (where X is random alphanumeric)
            String timestamp = LocalDateTime.now().toString()
                    .replaceAll("[^0-9]", "").substring(0, 14);
            String random = String.valueOf(System.nanoTime()).substring(7, 13);
            this.transactionId = String.format("PAY-%s-%s", timestamp, random);
        }

        // Calculate net amount
        if (amount != null && processingFee != null) {
            this.netAmount = amount.subtract(processingFee);
        }
    }

    /**
     * Update payment status with timestamp
     */
    public void updateStatus(PaymentStatus newStatus) {
        this.paymentStatus = newStatus;
        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case PROCESSING:
                this.processedAt = now;
                break;
            case COMPLETED:
                this.completedAt = now;
                break;
            case FAILED:
                this.failedAt = now;
                break;
            case CANCELLED:
                this.cancelledAt = now;
                break;
            case REFUNDED:
            case PARTIALLY_REFUNDED:
                this.refundedAt = now;
                break;
            default:
                // Other statuses don't need specific timestamp tracking
                break;
        }
    }

    /**
     * Process refund
     */
    public void processRefund(BigDecimal refundAmount, String reason, String refundTransId) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }

        if (refundAmount.compareTo(amount) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
        }

        this.refundedAmount = this.refundedAmount.add(refundAmount);
        this.refundReason = reason;
        this.refundTransactionId = refundTransId;

        if (this.refundedAmount.compareTo(amount) >= 0) {
            this.isRefunded = true;
            updateStatus(PaymentStatus.REFUNDED);
        } else {
            updateStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
    }

    /**
     * Check if payment is successful
     */
    public boolean isSuccessful() {
        return paymentStatus != null && paymentStatus.isSuccessful();
    }

    /**
     * Check if payment can be refunded
     */
    public boolean canBeRefunded() {
        return isSuccessful() && !Boolean.TRUE.equals(isRefunded);
    }

    /**
     * Check if payment is in final state
     */
    public boolean isFinalState() {
        return paymentStatus != null && paymentStatus.isFinal();
    }

    /**
     * Get remaining refundable amount
     */
    public BigDecimal getRefundableAmount() {
        if (!canBeRefunded()) {
            return BigDecimal.ZERO;
        }
        return amount.subtract(refundedAmount != null ? refundedAmount : BigDecimal.ZERO);
    }

    /**
     * Mark payment as failed
     */
    public void markAsFailed(String reason, String code) {
        this.failureReason = reason;
        this.failureCode = code;
        updateStatus(PaymentStatus.FAILED);
    }

    /**
     * Mark payment as completed
     */
    public void markAsCompleted(String gatewayPaymentId, String gatewayOrderId) {
        this.gatewayPaymentId = gatewayPaymentId;
        this.gatewayOrderId = gatewayOrderId;
        updateStatus(PaymentStatus.COMPLETED);
    }

    /**
     * Get payment summary
     */
    public String getPaymentSummary() {
        return String.format("%s payment of %s %s - Status: %s",
                paymentMethod != null ? paymentMethod.getDisplayName() : "Unknown",
                currency,
                amount,
                paymentStatus != null ? paymentStatus.getDisplayName() : "Unknown");
    }
}