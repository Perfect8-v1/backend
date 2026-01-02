package com.perfect8.admin.dto;

public class PaymentIssueResponse {
    private String paymentIssue;

    public PaymentIssueResponse() {}
    public PaymentIssueResponse(String paymentIssue) { this.paymentIssue = paymentIssue; }

    public String getPaymentIssue() { return paymentIssue; }
    public void setPaymentIssue(String paymentIssue) { this.paymentIssue = paymentIssue; }
}
