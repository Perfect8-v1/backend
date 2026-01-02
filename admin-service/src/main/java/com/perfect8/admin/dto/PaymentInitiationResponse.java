package com.perfect8.admin.dto;

public class PaymentInitiationResponse {
    private String paymentId;
    private String approvalUrl;
    private String status;
    private String message;

    // Constructors
    public PaymentInitiationResponse() {}

    public PaymentInitiationResponse(String paymentId, String approvalUrl, String status) {
        this.paymentId = paymentId;
        this.approvalUrl = approvalUrl;
        this.status = status;
    }

    // Getters and Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getApprovalUrl() { return approvalUrl; }
    public void setApprovalUrl(String approvalUrl) { this.approvalUrl = approvalUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}