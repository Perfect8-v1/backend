package com.perfect8.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentProcessingResponse {
    private String paymentId;
    private String transactionId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime processedAt;
    private String message;

    // Constructors
    public PaymentProcessingResponse() {}

    // Getters and Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
