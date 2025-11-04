package com.perfect8.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentProcessingRequest {
    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Payer ID is required")
    private String payerId;

    // Constructors
    public PaymentProcessingRequest() {}

    // Getters and Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getPayerId() { return payerId; }
    public void setPayerId(String payerId) { this.payerId = payerId; }
}
