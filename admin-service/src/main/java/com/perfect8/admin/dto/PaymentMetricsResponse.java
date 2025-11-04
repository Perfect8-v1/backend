package com.perfect8.admin.dto;

public class PaymentMetricsResponse {
    private String paymentMetrics;

    public PaymentMetricsResponse() {}
    public PaymentMetricsResponse(String paymentMetrics) { this.paymentMetrics = paymentMetrics; }

    public String getPaymentMetrics() { return paymentMetrics; }
    public void setPaymentMetrics(String paymentMetrics) { this.paymentMetrics = paymentMetrics; }
}
