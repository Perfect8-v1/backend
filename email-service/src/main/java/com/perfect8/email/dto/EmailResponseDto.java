package com.perfect8.email.dto;

import com.perfect8.email.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Response DTO for email operations
 * Version 1.0 - Core email response functionality with object-oriented design
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponseDto {

    // Primary identifiers as objects
    private Long emailId;  // Changed from String to Long to match entity
    private String trackingId;  // For cross-system tracking
    private String messageType;  // ORDER_CONFIRMATION, SHIPPING_NOTIFICATION, etc.

    // Message identifiers
    private String smtpMessageId; // External message ID from SMTP provider
    private String campaignId;

    // Recipient information object
    private RecipientInfo recipientInfo;

    // Email content summary
    private EmailContentSummary contentSummary;

    // Status information object
    private StatusInfo statusInfo;

    // Retry information object
    private RetryInfo retryInfo;

    // Provider information object
    private ProviderInfo providerInfo;

    // Reference information object
    private ReferenceInfo referenceInfo;

    // Queue information object
    private QueueInfo queueInfo;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime updatedAt;

    // Metadata
    private Map<String, Object> metadata;

    // Inner classes for better object orientation

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientInfo {
        private String primaryEmail;
        private String recipientName;
        private String recipientType; // CUSTOMER, ADMIN, STAFF, etc.
        private String language;
        private String timezone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailContentSummary {
        private String subject;
        private String templateName;
        private Integer characterCount;
        private Integer attachmentCount;
        private Long totalSizeBytes;
        private Boolean isHtml;
        private String contentType; // text/plain, text/html, multipart/mixed
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusInfo {
        private EmailStatus currentStatus;
        private EmailStatus previousStatus;
        private String statusMessage;
        private String statusReason;
        private LocalDateTime statusChangedAt;
        private Boolean isSuccess;
        private Boolean isFinal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryInfo {
        private Integer retryCount;
        private Integer maxRetries;
        private LocalDateTime nextRetryAt;
        private String retryStrategy; // EXPONENTIAL, LINEAR, FIXED
        private Long retryDelayMillis;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderInfo {
        private String providerName; // SMTP, SendGrid, AWS SES, etc.
        private String providerResponse;
        private String providerMessageId;
        private Integer providerStatusCode;
        private Map<String, String> providerHeaders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferenceInfo {
        private String referenceId;
        private String referenceType; // ORDER, CUSTOMER, SHIPMENT, etc.
        private Long orderId;
        private Long customerId;
        private String correlationId;
        private String requestId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueInfo {
        private Boolean isQueued;
        private LocalDateTime queuedAt;
        private Integer queuePosition;
        private String queueName;
        private Integer queuePriority;
        private Long estimatedProcessingMillis;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        private String errorCode;
        private String errorMessage;
        private String errorDetails;
        private String errorCategory; // NETWORK, VALIDATION, AUTHENTICATION, etc.
        private LocalDateTime errorOccurredAt;
        private String errorStackTrace; // Only in development mode
    }

    // Add errorInfo field
    private ErrorInfo errorInfo;

    // Static factory methods using objects

    public static EmailResponseDto success(Long emailId, String trackingId, String recipientEmail, EmailStatus status) {
        return EmailResponseDto.builder()
                .emailId(emailId)
                .trackingId(trackingId)
                .recipientInfo(RecipientInfo.builder()
                        .primaryEmail(recipientEmail)
                        .build())
                .statusInfo(StatusInfo.builder()
                        .currentStatus(status)
                        .isSuccess(true)
                        .statusMessage("Email successfully processed")
                        .statusChangedAt(LocalDateTime.now())
                        .build())
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static EmailResponseDto queued(Long emailId, String trackingId, String recipientEmail, Integer queuePosition) {
        return EmailResponseDto.builder()
                .emailId(emailId)
                .trackingId(trackingId)
                .recipientInfo(RecipientInfo.builder()
                        .primaryEmail(recipientEmail)
                        .build())
                .statusInfo(StatusInfo.builder()
                        .currentStatus(EmailStatus.QUEUED)
                        .isSuccess(true)
                        .statusMessage("Email queued for delivery")
                        .statusChangedAt(LocalDateTime.now())
                        .build())
                .queueInfo(QueueInfo.builder()
                        .isQueued(true)
                        .queuedAt(LocalDateTime.now())
                        .queuePosition(queuePosition)
                        .build())
                .build();
    }

    public static EmailResponseDto failed(String recipientEmail, ErrorInfo errorInfo) {
        return EmailResponseDto.builder()
                .recipientInfo(RecipientInfo.builder()
                        .primaryEmail(recipientEmail)
                        .build())
                .statusInfo(StatusInfo.builder()
                        .currentStatus(EmailStatus.FAILED)
                        .isSuccess(false)
                        .statusMessage("Failed to send email")
                        .statusChangedAt(LocalDateTime.now())
                        .build())
                .errorInfo(errorInfo)
                .build();
    }

    // Helper methods for backward compatibility

    public String getRecipient() {
        return recipientInfo != null ? recipientInfo.getPrimaryEmail() : null;
    }

    public void setRecipient(String recipient) {
        if (recipientInfo == null) {
            recipientInfo = new RecipientInfo();
        }
        recipientInfo.setPrimaryEmail(recipient);
    }

    public String getSubject() {
        return contentSummary != null ? contentSummary.getSubject() : null;
    }

    public void setSubject(String subject) {
        if (contentSummary == null) {
            contentSummary = new EmailContentSummary();
        }
        contentSummary.setSubject(subject);
    }

    public EmailStatus getStatus() {
        return statusInfo != null ? statusInfo.getCurrentStatus() : null;
    }

    public void setStatus(EmailStatus status) {
        if (statusInfo == null) {
            statusInfo = new StatusInfo();
        }
        statusInfo.setCurrentStatus(status);
    }

    public Integer getRetryCount() {
        return retryInfo != null ? retryInfo.getRetryCount() : null;
    }

    public void setRetryCount(Integer retryCount) {
        if (retryInfo == null) {
            retryInfo = new RetryInfo();
        }
        retryInfo.setRetryCount(retryCount);
    }

    public String getErrorMessage() {
        return errorInfo != null ? errorInfo.getErrorMessage() : null;
    }

    public void setErrorMessage(String errorMessage) {
        if (errorInfo == null) {
            errorInfo = new ErrorInfo();
        }
        errorInfo.setErrorMessage(errorMessage);
    }

    // Business logic helper methods

    public boolean isSuccessful() {
        return statusInfo != null && statusInfo.getIsSuccess() != null && statusInfo.getIsSuccess();
    }

    public boolean isFailed() {
        return statusInfo != null && statusInfo.getCurrentStatus() != null &&
                (statusInfo.getCurrentStatus() == EmailStatus.FAILED ||
                        statusInfo.getCurrentStatus() == EmailStatus.BOUNCED);
    }

    public boolean isRetryable() {
        return retryInfo != null && retryInfo.getRetryCount() != null &&
                retryInfo.getMaxRetries() != null &&
                retryInfo.getRetryCount() < retryInfo.getMaxRetries();
    }

    public boolean isPending() {
        return statusInfo != null && statusInfo.getCurrentStatus() != null &&
                statusInfo.getCurrentStatus().isPendingState();
    }
}