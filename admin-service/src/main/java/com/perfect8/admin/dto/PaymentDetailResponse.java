package com.perfect8.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDetailResponse {
    private Long paymentDetailId;
    private String paymentId;
    private String transactionId;
    private Long orderId;
    private String paymentMethod;
    private String status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdDate;
    private LocalDateTime processedDate;

    // Constructors
    public PaymentDetailResponse() {}

    // Getters and Setters
    public Long getPaymentDetailId() { return paymentDetailId; }
    public void setPaymentDetailId(Long paymentDetailId) { this.paymentDetailId = paymentDetailId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDateTime processedDate) { this.processedDate = processedDate; }
}
