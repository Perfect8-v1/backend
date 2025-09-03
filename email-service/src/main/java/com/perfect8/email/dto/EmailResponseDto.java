package com.perfect8.email.dto;

import com.perfect8.email.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for email operations
 * Version 1.0 - Core email response functionality
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponseDto {

    private String emailId;
    private String messageId; // External message ID from email provider
    private String recipient;
    private String subject;
    private EmailStatus status;
    private String statusMessage;
    private LocalDateTime sentAt;
    private LocalDateTime scheduledAt;

    // Request tracking
    private String requestId;
    private String correlationId;

    // Error information
    private boolean success;
    private String errorCode;
    private String errorMessage;
    private String errorDetails;

    // Retry information
    private Integer attemptNumber;
    private Integer maxRetries;
    private LocalDateTime nextRetryAt;

    // Provider response
    private String providerName; // SMTP, SendGrid, AWS SES, etc.
    private String providerResponse;
    private Map<String, String> providerMetadata;

    // Related entities
    private String orderId;
    private String customerId;
    private String templateUsed;

    // Delivery tracking (basic for v1.0)
    private boolean queued;
    private LocalDateTime queuedAt;
    private Integer queuePosition;

    // Version 2.0 features - commented out
    // private String trackingPixelId;
    // private List<String> clickTrackingIds;
    // private DeliveryMetrics deliveryMetrics;

    // Static factory methods for common responses

    public static EmailResponseDto success(String emailId, String recipient, EmailStatus status) {
        return EmailResponseDto.builder()
                .emailId(emailId)
                .recipient(recipient)
                .status(status)
                .success(true)
                .sentAt(LocalDateTime.now())
                .statusMessage("Email successfully processed")
                .build();
    }

    public static EmailResponseDto queued(String emailId, String recipient) {
        return EmailResponseDto.builder()
                .emailId(emailId)
                .recipient(recipient)
                .status(EmailStatus.QUEUED)
                .success(true)
                .queued(true)
                .queuedAt(LocalDateTime.now())
                .statusMessage("Email queued for delivery")
                .build();
    }

    public static EmailResponseDto failed(String recipient, String errorMessage, String errorCode) {
        return EmailResponseDto.builder()
                .recipient(recipient)
                .status(EmailStatus.FAILED)
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .statusMessage("Failed to send email")
                .build();
    }

    public static EmailResponseDto retrying(String emailId, String recipient, int attemptNumber, LocalDateTime nextRetry) {
        return EmailResponseDto.builder()
                .emailId(emailId)
                .recipient(recipient)
                .status(EmailStatus.RETRYING)
                .success(false)
                .attemptNumber(attemptNumber)
                .nextRetryAt(nextRetry)
                .statusMessage(String.format("Retrying... Attempt %d", attemptNumber))
                .build();
    }

    // Helper methods

    public boolean isSuccessful() {
        return success && (status == EmailStatus.SENT || status == EmailStatus.DELIVERED || status == EmailStatus.QUEUED);
    }

    public boolean isFailed() {
        return !success || status == EmailStatus.FAILED || status == EmailStatus.BOUNCED;
    }

    public boolean isRetryable() {
        return status == EmailStatus.RETRYING ||
                (isFailed() && attemptNumber != null && maxRetries != null && attemptNumber < maxRetries);
    }

    public boolean isPending() {
        return status == EmailStatus.QUEUED || status == EmailStatus.SENDING || status == EmailStatus.RETRYING;
    }

    public String getStatusDescription() {
        if (status == null) {
            return "Unknown status";
        }

        switch (status) {
            case QUEUED:
                return queued ? String.format("Queued at position %d", queuePosition != null ? queuePosition : 0) : "In queue";
            case SENDING:
                return "Currently sending";
            case SENT:
                return "Successfully sent";
            case DELIVERED:
                return "Delivered to recipient";
            case FAILED:
                return errorMessage != null ? errorMessage : "Failed to send";
            case BOUNCED:
                return "Email bounced";
            case RETRYING:
                return String.format("Retrying (attempt %d of %d)", attemptNumber, maxRetries);
            default:
                return status.toString();
        }
    }
}