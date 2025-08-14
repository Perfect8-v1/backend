package com.perfect8.shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonIgnore
    private Order order;

    @NotBlank(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount format invalid")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase 3-letter code")
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    // PayPal specific fields
    @Size(max = 100, message = "PayPal payment ID cannot exceed 100 characters")
    @Column(length = 100)
    private String paypalPaymentId;

    @Size(max = 100, message = "PayPal payer ID cannot exceed 100 characters")
    @Column(length = 100)
    private String paypalPayerId;

    @Size(max = 50, message = "PayPal payment token cannot exceed 50 characters")
    @Column(length = 50)
    private String paypalToken;

    // Generic transaction fields
    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    @Column(length = 100)
    private String transactionId;

    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    @Column(length = 100)
    private String referenceNumber;

    @Size(max = 50, message = "Authorization code cannot exceed 50 characters")
    @Column(length = 50)
    private String authorizationCode;

    // Fee and processing information
    @DecimalMin(value = "0.0", message = "Processing fee cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Processing fee format invalid")
    @Column(precision = 10, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Net amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Net amount format invalid")
    @Column(precision = 12, scale = 2)
    private BigDecimal netAmount;

    // Payment gateway response
    @Size(max = 20, message = "Gateway response code cannot exceed 20 characters")
    @Column(length = 20)
    private String gatewayResponseCode;

    @Size(max = 255, message = "Gateway response message cannot exceed 255 characters")
    private String gatewayResponseMessage;

    @Column(columnDefinition = "TEXT")
    private String gatewayRawResponse;

    // Failure information
    @Size(max = 255, message = "Failure reason cannot exceed 255 characters")
    private String failureReason;

    @Min(value = 0, message = "Retry count cannot be negative")
    @Column(nullable = false)
    private Integer retryCount = 0;

    // Timestamps
    private LocalDateTime initiatedAt;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private LocalDateTime failedAt;
    private LocalDateTime refundedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Payment() {}

    public Payment(Order order, PaymentMethod paymentMethod, BigDecimal amount) {
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.netAmount = amount;
        this.initiatedAt = LocalDateTime.now();
    }

    public Payment(Order order, PaymentMethod paymentMethod, BigDecimal amount, String currency) {
        this(order, paymentMethod, amount);
        this.currency = currency;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
        updateStatusTimestamp(status);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        calculateNetAmount();
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaypalPaymentId() {
        return paypalPaymentId;
    }

    public void setPaypalPaymentId(String paypalPaymentId) {
        this.paypalPaymentId = paypalPaymentId;
    }

    public String getPaypalPayerId() {
        return paypalPayerId;
    }

    public void setPaypalPayerId(String paypalPayerId) {
        this.paypalPayerId = paypalPayerId;
    }

    public String getPaypalToken() {
        return paypalToken;
    }

    public void setPaypalToken(String paypalToken) {
        this.paypalToken = paypalToken;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
        calculateNetAmount();
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getGatewayResponseCode() {
        return gatewayResponseCode;
    }

    public void setGatewayResponseCode(String gatewayResponseCode) {
        this.gatewayResponseCode = gatewayResponseCode;
    }

    public String getGatewayResponseMessage() {
        return gatewayResponseMessage;
    }

    public void setGatewayResponseMessage(String gatewayResponseMessage) {
        this.gatewayResponseMessage = gatewayResponseMessage;
    }

    public String getGatewayRawResponse() {
        return gatewayRawResponse;
    }

    public void setGatewayRawResponse(String gatewayRawResponse) {
        this.gatewayRawResponse = gatewayRawResponse;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getInitiatedAt() {
        return initiatedAt;
    }

    public LocalDateTime getAuthorizedAt() {
        return authorizedAt;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Business Methods
    public boolean isPaid() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isRefunded() {
        return status == PaymentStatus.REFUNDED;
    }

    public boolean canBeRetried() {
        return (status == PaymentStatus.FAILED || status == PaymentStatus.PENDING)
                && retryCount < 3;
    }

    public boolean isPayPalPayment() {
        return paymentMethod == PaymentMethod.PAYPAL;
    }

    public void authorize() {
        if (status == PaymentStatus.PENDING) {
            setStatus(PaymentStatus.AUTHORIZED);
        }
    }

    public void capture() {
        if (status == PaymentStatus.AUTHORIZED) {
            setStatus(PaymentStatus.COMPLETED);
        }
    }

    public void fail(String reason) {
        setStatus(PaymentStatus.FAILED);
        this.failureReason = reason;
        this.retryCount++;
    }

    public void refund() {
        if (status == PaymentStatus.COMPLETED) {
            setStatus(PaymentStatus.REFUNDED);
        }
    }

    public void cancel() {
        if (status == PaymentStatus.PENDING || status == PaymentStatus.AUTHORIZED) {
            setStatus(PaymentStatus.CANCELLED);
        }
    }

    private void calculateNetAmount() {
        if (amount != null && processingFee != null) {
            this.netAmount = amount.subtract(processingFee);
        }
    }

    private void updateStatusTimestamp(PaymentStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case AUTHORIZED -> this.authorizedAt = now;
            case COMPLETED -> this.capturedAt = now;
            case FAILED -> this.failedAt = now;
            case REFUNDED -> this.refundedAt = now;
        }
    }

    public String getDisplayStatus() {
        return status.getDisplayName();
    }

    public String getFormattedAmount() {
        return String.format("%.2f %s", amount, currency);
    }

    public boolean isProcessingFeeApplicable() {
        return paymentMethod == PaymentMethod.PAYPAL || paymentMethod == PaymentMethod.CREDIT_CARD;
    }

    @PrePersist
    @PreUpdate
    private void calculateNetAmountBeforeSave() {
        calculateNetAmount();
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return id != null && id.equals(payment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}