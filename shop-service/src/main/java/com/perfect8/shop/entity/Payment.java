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
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_status", columnList = "payment_status"),
        @Index(name = "idx_payment_transaction", columnList = "transaction_id", unique = true),
        @Index(name = "idx_payment_created", columnList = "created_date")
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
    private Long paymentId;  // → DB: payment_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;  // → DB: amount

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";  // → DB: currency

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.PAYPAL;  // → DB: payment_method

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;  // → DB: payment_status

    // Transaction identifiers
    @Column(nullable = false, unique = true, length = 100)
    private String transactionId;  // → DB: transaction_id

    @Column(length = 100)
    private String gatewayPaymentId;  // → DB: gateway_payment_id

    // Payment details
    private LocalDateTime paymentDate;  // → DB: payment_date

    @Column(length = 255)
    private String payerEmail;  // → DB: payer_email

    @Column(length = 255)
    private String payerName;  // → DB: payer_name

    // Verification
    @Column
    @Builder.Default
    private Boolean isVerified = false;  // → DB: is_verified

    private LocalDateTime verificationDate;  // → DB: verification_date

    // Refund information
    @Column(precision = 10, scale = 2)
    private BigDecimal refundAmount;  // → DB: refund_amount

    @Column(length = 500)
    private String refundReason;  // → DB: refund_reason

    private LocalDateTime refundDate;  // → DB: refund_date

    @Column
    @Builder.Default
    private Boolean isPartialRefund = false;  // → DB: is_partial_refund

    // Retry information
    @Column
    @Builder.Default
    private Integer retryCount = 0;  // → DB: retry_count

    private LocalDateTime lastRetryDate;  // → DB: last_retry_date

    // Failure information
    @Column(length = 500)
    private String failureReason;  // → DB: failure_reason

    // Additional notes
    @Column(columnDefinition = "TEXT")
    private String notes;  // → DB: notes

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    @Column
    private LocalDateTime updatedDate;  // → DB: updated_date (Magnum Opus)

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