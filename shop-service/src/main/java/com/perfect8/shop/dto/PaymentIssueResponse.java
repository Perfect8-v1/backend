package com.perfect8.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentIssueResponse {
    private Long paymentId;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String paymentMethod;
    private String issueType; // FAILED, DECLINED, DISPUTED, CHARGEBACK, REFUND_FAILED
    private String issueDescription;
    private String failureReason;
    private LocalDateTime issueDate;
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private String status; // OPEN, IN_PROGRESS, RESOLVED, ESCALATED
    private Integer daysSinceIssue;
    private boolean requiresManualReview;

    public PaymentIssueResponse() {}

    // Getters and setters
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDaysSinceIssue() { return daysSinceIssue; }
    public void setDaysSinceIssue(Integer daysSinceIssue) { this.daysSinceIssue = daysSinceIssue; }

    public boolean isRequiresManualReview() { return requiresManualReview; }
    public void setRequiresManualReview(boolean requiresManualReview) { this.requiresManualReview = requiresManualReview; }
}