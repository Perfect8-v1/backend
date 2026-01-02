package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment issues in version 1.0
 * Focuses on critical payment problems that need immediate attention
 *
 * Version 1.0 handles essential payment issues:
 * - Failed payments (must be retried or cancelled)
 * - Declined transactions (customer needs to be notified)
 * - Refund failures (critical for customer service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIssueResponse {

    private Long paymentId;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String paymentMethod;

    // Issue types for v1.0: FAILED, DECLINED, REFUND_FAILED
    // v2.0 will add: DISPUTED, CHARGEBACK
    private String issueType;

    private String issueDescription;
    private String failureReason;
    private LocalDateTime issueDate;

    // Priority for v1.0: HIGH (failed payments), CRITICAL (refund failures)
    // v2.0 will add: LOW, MEDIUM
    private String priority;

    // Status for v1.0: OPEN, RESOLVED
    // v2.0 will add: IN_PROGRESS, ESCALATED
    private String status;

    private Integer daysSinceIssue;
    private boolean requiresManualReview;

    /* ============================================
     * VERSION 2.0 FIELDS - Reserved for future use
     * ============================================
     * These fields will be added in version 2.0:
     * - String resolutionNotes
     * - LocalDateTime resolvedDate
     * - String resolvedBy
     * - BigDecimal estimatedLoss
     * - Integer customerLoyaltyScore
     * - List<String> previousIssues
     */
}