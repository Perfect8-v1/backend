package com.perfect8.shop.entity;

import com.perfect8.shop.enums.PaymentMethod;
import com.perfect8.shop.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity - Version 1.0
 * Magnum Opus Compliant: Consistent naming (createdDate/updatedDate)
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_status", columnList = "payment_status"),
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

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.PAYPAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

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

    // FIXED: Changed from createdAt/updatedAt to createdDate/updatedDate for consistency
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (transactionId == null) {
            transactionId = generateTransactionId();
        }
        if (currency == null) {
            currency = "USD";
        }
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.PAYPAL;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
        if (isVerified == null) {
            isVerified = Boolean.FALSE;
        }
        if (isPartialRefund == null) {
            isPartialRefund = Boolean.FALSE;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ========== Business methods ==========

    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(paymentStatus);
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(paymentStatus);
    }

    public boolean isPending() {
        return PaymentStatus.PENDING.equals(paymentStatus);
    }

    public boolean isRefunded() {
        return PaymentStatus.REFUNDED.equals(paymentStatus) ||
                PaymentStatus.PARTIALLY_REFUNDED.equals(paymentStatus);
    }

    public boolean canBeRefunded() {
        return paymentStatus.isRefundable();
    }

    public BigDecimal getRefundableAmount() {
        if (!canBeRefunded()) {
            return BigDecimal.ZERO;
        }

        BigDecimal refundedAmount = refundAmount != null ? refundAmount : BigDecimal.ZERO;
        return amount.subtract(refundedAmount);
    }

    public void incrementRetryCount() {
        if (retryCount == null) {
            retryCount = 0;
        }
        retryCount++;
        lastRetryDate = LocalDateTime.now();
    }

    public boolean isMaxRetriesExceeded() {
        Integer maxRetries = 3;
        return retryCount != null && retryCount >= maxRetries;
    }

    public String getDisplayStatus() {
        String statusDisplay = paymentStatus.getDisplayName();

        if (PaymentStatus.FAILED.equals(paymentStatus) && failureReason != null) {
            return statusDisplay + ": " + failureReason;
        }

        return statusDisplay;
    }

    public boolean isPayPalPayment() {
        return PaymentMethod.PAYPAL.equals(paymentMethod);
    }

    private String generateTransactionId() {
        Long currentTimeMillis = System.currentTimeMillis();
        String nanoTimePart = String.valueOf(System.nanoTime()).substring(7, 13);
        return "PAY-" + currentTimeMillis + "-" + nanoTimePart;
    }

    public Long getOrderId() {
        return order != null ? order.getOrderId() : null;
    }

    public String getFormattedAmount() {
        return currency + " " + amount.toString();
    }
}