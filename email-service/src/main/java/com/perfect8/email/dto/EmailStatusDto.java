package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for email status tracking
 * Version 1.0 - Core email status functionality
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatusDto {

    private String emailId;
    private String messageId; // External provider message ID (e.g., from SMTP server)
    private String status; // QUEUED, SENDING, SENT, DELIVERED, FAILED, BOUNCED, COMPLAINED, UNSUBSCRIBED

    private String recipient;
    private String sender;
    private String subject;
    private String emailType;

    // Timestamps
    private LocalDateTime queuedDate;
    private LocalDateTime sentDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime failedDate;
    private LocalDateTime lastStatusUpdateDate;

    // Delivery details
    private Integer attemptCount;
    private String lastAttemptError;
    private String providerResponse;
    private String smtpResponse;

    // Bounce/Complaint details
    private String bounceType; // HARD, SOFT, TEMPORARY
    private String bounceReason;
    private String complaintType;

    // Tracking (for v2.0)
    // private LocalDateTime openedAt;
    // private Integer openCount;
    // private List<ClickEvent> clicks;
    // private String userAgent;
    // private String ipAddress;

    // Related entities
    private String orderId;
    private String customerId;
    private String campaignId;

    // Retry information
    private boolean canRetry;
    private LocalDateTime nextRetryDate;
    private Integer maxRetries;

    // Events history
    private List<EmailEvent> events;

    /**
     * Email event for tracking status changes
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailEvent {
        private LocalDateTime timestamp;
        private String eventType; // QUEUED, SENT, DELIVERED, FAILED, BOUNCED, etc.
        private String description;
        private String details;
        private String source; // SYSTEM, PROVIDER, USER
    }

    // Helper methods

    public boolean isDelivered() {
        return "DELIVERED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status) || "BOUNCED".equals(status);
    }

    public boolean isPending() {
        return "QUEUED".equals(status) || "SENDING".equals(status);
    }

    public boolean canBeRetried() {
        return canRetry && attemptCount != null && maxRetries != null && attemptCount < maxRetries;
    }

    public boolean isHardBounce() {
        return "HARD".equals(bounceType);
    }

    public boolean isSoftBounce() {
        return "SOFT".equals(bounceType) || "TEMPORARY".equals(bounceType);
    }

    public String getStatusColor() {
        switch (status) {
            case "DELIVERED":
                return "green";
            case "SENT":
                return "blue";
            case "QUEUED":
            case "SENDING":
                return "yellow";
            case "FAILED":
            case "BOUNCED":
                return "red";
            case "COMPLAINED":
            case "UNSUBSCRIBED":
                return "orange";
            default:
                return "gray";
        }
    }

    public String getStatusIcon() {
        switch (status) {
            case "DELIVERED":
                return "✓✓";
            case "SENT":
                return "✓";
            case "QUEUED":
                return "⏳";
            case "SENDING":
                return "📤";
            case "FAILED":
            case "BOUNCED":
                return "✗";
            case "COMPLAINED":
                return "⚠";
            case "UNSUBSCRIBED":
                return "🚫";
            default:
                return "?";
        }
    }
}