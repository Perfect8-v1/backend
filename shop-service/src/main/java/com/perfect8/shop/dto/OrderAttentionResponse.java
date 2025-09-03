package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAttentionResponse {

    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String status;
    private String priorityLevel;
    private String attentionType;
    private String reason;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime orderDate;
    private LocalDateTime lastUpdated;
    private LocalDateTime dueDate;
    private Integer daysPending;
    private Boolean isUrgent;
    private Boolean isOverdue;
    private String assignedTo;
    private String department;
    private List<String> actionRequired;
    private List<String> tags;
    private String paymentStatus;
    private String shippingStatus;
    private String fulfillmentStatus;
    private Boolean hasComplaints;
    private Boolean hasRefundRequest;
    private Boolean hasDispute;
    private String lastContactDate;
    private String nextFollowUpDate;
    private Integer contactAttempts;
    private String notes;
    private String escalationLevel;
    private LocalDateTime escalatedAt;
    private String escalatedBy;
    private Boolean requiresManagerApproval;
    private String riskLevel;
    private BigDecimal potentialLoss;
    private String resolutionTarget;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionItem {
        private String action;
        private String description;
        private String priority;
        private LocalDateTime dueDate;
        private String assignedTo;
    }
}