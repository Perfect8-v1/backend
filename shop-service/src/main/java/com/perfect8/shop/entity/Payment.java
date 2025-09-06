package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Column(name = "gateway_response_code", length = 50)
    private String gatewayResponseCode;

    @Column(name = "gateway_response_message", columnDefinition = "TEXT")
    private String gatewayResponseMessage;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    // Refund fields
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "refund_transaction_id", length = 100)
    private String refundTransactionId;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @Column(name = "is_partial_refund")
    private Boolean isPartialRefund = false;

    // Retry fields for failed payments
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_retry_date")
    private LocalDateTime lastRetryDate;

    // Additional fields
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum PaymentMethod {
        PAYPAL,
        CREDIT_CARD,
        DEBIT_CARD,
        BANK_TRANSFER,
        CASH_ON_DELIVERY
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        EXPIRED,
        PENDING_VERIFICATION
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (isVerified == null) {
            isVerified = false;
        }
        if (isPartialRefund == null) {
            isPartialRefund = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void incrementRetryCount() {
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        this.retryCount++;
        this.lastRetryDate = LocalDateTime.now();
    }

    public boolean isRefundable() {
        return paymentStatus == PaymentStatus.COMPLETED &&
                (refundAmount == null || refundAmount.compareTo(amount) < 0);
    }

    public boolean isFullyRefunded() {
        return refundAmount != null && refundAmount.compareTo(amount) == 0;
    }

    public boolean canRetry() {
        return (paymentStatus == PaymentStatus.FAILED || paymentStatus == PaymentStatus.PENDING)
                && retryCount < 3;
    }

    // For compatibility with existing code that uses String status
    public void setStatus(String status) {
        this.paymentStatus = PaymentStatus.valueOf(status);
    }

    public String getStatus() {
        return this.paymentStatus != null ? this.paymentStatus.name() : null;
    }
}