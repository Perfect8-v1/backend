/* package com.perfect8.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class PaymentMetricsResponse {
    private BigDecimal totalPaymentsToday;
    private BigDecimal totalPaymentsThisMonth;
    private Integer successfulPayments;
    private Integer failedPayments;
    private BigDecimal successRate;
    private List<PaymentMethodStats> paymentMethodStats = new ArrayList<>();
    private List<FailedPayment> recentFailures = new ArrayList<>();
    private BigDecimal averageTransactionValue;
    private BigDecimal refundAmount;
    private Integer refundCount;

    public PaymentMetricsResponse() {}

    // Getters and setters
    public BigDecimal getTotalPaymentsToday() { return totalPaymentsToday; }
    public void setTotalPaymentsToday(BigDecimal totalPaymentsToday) { this.totalPaymentsToday = totalPaymentsToday; }

    public BigDecimal getTotalPaymentsThisMonth() { return totalPaymentsThisMonth; }
    public void setTotalPaymentsThisMonth(BigDecimal totalPaymentsThisMonth) { this.totalPaymentsThisMonth = totalPaymentsThisMonth; }

    public Integer getSuccessfulPayments() { return successfulPayments; }
    public void setSuccessfulPayments(Integer successfulPayments) { this.successfulPayments = successfulPayments; }

    public Integer getFailedPayments() { return failedPayments; }
    public void setFailedPayments(Integer failedPayments) { this.failedPayments = failedPayments; }

    public BigDecimal getSuccessRate() { return successRate; }
    public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }

    public List<PaymentMethodStats> getPaymentMethodStats() { return paymentMethodStats; }
    public void setPaymentMethodStats(List<PaymentMethodStats> paymentMethodStats) { this.paymentMethodStats = paymentMethodStats; }

    public List<FailedPayment> getRecentFailures() { return recentFailures; }
    public void setRecentFailures(List<FailedPayment> recentFailures) { this.recentFailures = recentFailures; }

    public BigDecimal getAverageTransactionValue() { return averageTransactionValue; }
    public void setAverageTransactionValue(BigDecimal averageTransactionValue) { this.averageTransactionValue = averageTransactionValue; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public Integer getRefundCount() { return refundCount; }
    public void setRefundCount(Integer refundCount) { this.refundCount = refundCount; }

    public static class PaymentMethodStats {
        private String paymentMethod;
        private Integer transactionCount;
        private BigDecimal totalAmount;
        private BigDecimal successRate;
        private BigDecimal averageValue;

        public PaymentMethodStats() {}

        // Getters and setters
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public Integer getTransactionCount() { return transactionCount; }
        public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public BigDecimal getSuccessRate() { return successRate; }
        public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }

        public BigDecimal getAverageValue() { return averageValue; }
        public void setAverageValue(BigDecimal averageValue) { this.averageValue = averageValue; }
    }

    public static class FailedPayment {
        private String orderNumber;
        private String customerEmail;
        private BigDecimal amount;
        private String paymentMethod;
        private String failureReason;
        private LocalDateTime failedDate;

        public FailedPayment() {}

        // Getters and setters
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

        ***NOTE THAT REFACTORING IS NOT ASSURED WHEN LOMBOK ISN'T USED***

        public LocalDateTime getFailedAt() { return failedDate; }
        public void setFailedAt(LocalDateTime failedDate) { this.failedDate = failedDate; }
    }
}

 */