package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"order"})
@ToString(exclude = {"order"})
public class Payment {

    // Enum definitions
    public enum PaymentMethod {
        PAYPAL("PayPal"),
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        BANK_TRANSFER("Bank Transfer"),
        CRYPTO("Cryptocurrency"),
        CASH_ON_DELIVERY("Cash on Delivery");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentStatus {
        PENDING("Pending"),
        PROCESSING("Processing"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        REFUNDED("Refunded"),
        CANCELLED("Cancelled"),
        PARTIALLY_REFUNDED("Partially Refunded");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "authorization_code", length = 50)
    private String authorizationCode;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "is_partial_refund")
    @Builder.Default
    private Boolean isPartialRefund = false;

    @Column(name = "payer_email", length = 255)
    private String payerEmail;

    @Column(name = "payer_name", length = 255)
    private String payerName;

    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;

    @Column(name = "processing_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal processingFee = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes", length = 1000)
    private String notes;

    // Additional fields for v1.0
    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_retry_date")
    private LocalDateTime lastRetryDate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (currency == null) {
            currency = "USD";
        }
        if (refundAmount == null) {
            refundAmount = BigDecimal.ZERO;
        }
        if (processingFee == null) {
            processingFee = BigDecimal.ZERO;
        }
        if (isPartialRefund == null) {
            isPartialRefund = false;
        }
        if (isVerified == null) {
            isVerified = false;
        }
        if (retryCount == null) {
            retryCount = 0;
        }

        // Calculate net amount if not set
        if (netAmount == null && amount != null && processingFee != null) {
            netAmount = amount.subtract(processingFee);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean isSuccessful() {
        return PaymentStatus.COMPLETED == status;
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED == status;
    }

    public boolean isPending() {
        return PaymentStatus.PENDING == status;
    }

    public boolean isProcessing() {
        return PaymentStatus.PROCESSING == status;
    }

    public boolean isRefunded() {
        return PaymentStatus.REFUNDED == status || PaymentStatus.PARTIALLY_REFUNDED == status;
    }

    public boolean isFullyRefunded() {
        return PaymentStatus.REFUNDED == status;
    }

    public boolean isCancelled() {
        return PaymentStatus.CANCELLED == status;
    }

    public boolean canBeRefunded() {
        return isSuccessful() && !isFullyRefunded();
    }

    public boolean hasRefund() {
        return refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isPartiallyRefunded() {
        return PaymentStatus.PARTIALLY_REFUNDED == status ||
                (hasRefund() && refundAmount.compareTo(amount) < 0 && Boolean.TRUE.equals(isPartialRefund));
    }

    public BigDecimal getRemainingAmount() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (refundAmount == null) {
            return amount;
        }
        BigDecimal remaining = amount.subtract(refundAmount);
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
    }

    public void markAsCompleted(String transactionId, String authCode) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.authorizationCode = authCode;
        this.paymentDate = LocalDateTime.now();
        this.isVerified = true;
        this.verificationDate = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.retryCount++;
        this.lastRetryDate = LocalDateTime.now();
    }

    public void processRefund(BigDecimal refundAmount, String reason, boolean isPartial) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid refund amount");
        }

        if (refundAmount.compareTo(amount) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
        }

        this.refundAmount = refundAmount;
        this.refundReason = reason;
        this.isPartialRefund = isPartial;
        this.refundDate = LocalDateTime.now();

        if (refundAmount.compareTo(amount) >= 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    public void cancelPayment(String reason) {
        if (isSuccessful()) {
            throw new IllegalStateException("Cannot cancel completed payment");
        }
        this.status = PaymentStatus.CANCELLED;
        this.notes = reason;
    }

    public boolean canRetry() {
        return isFailed() && retryCount < 3;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryDate = LocalDateTime.now();
    }

    public String getDisplayStatus() {
        return status != null ? status.getDisplayName() : "Unknown";
    }

    public boolean requiresVerification() {
        return isSuccessful() && !Boolean.TRUE.equals(isVerified);
    }

    public void verify() {
        this.isVerified = true;
        this.verificationDate = LocalDateTime.now();
    }

    // Helper method to check if payment is using card
    public boolean isCardPayment() {
        return paymentMethod == PaymentMethod.CREDIT_CARD ||
                paymentMethod == PaymentMethod.DEBIT_CARD;
    }

    // Helper method to get payment method display name
    public String getPaymentMethodDisplayName() {
        return paymentMethod != null ? paymentMethod.getDisplayName() : "Unknown";
    }
}