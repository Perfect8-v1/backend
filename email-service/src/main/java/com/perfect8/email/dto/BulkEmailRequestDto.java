package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * DTO for bulk email requests
 * Version 1.0 - Core functionality for marketing and transactional bulk emails
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmailRequestDto {

    @NotEmpty(message = "Recipients list cannot be empty")
    @Size(max = 1000, message = "Cannot send to more than 1000 recipients at once")
    private List<String> recipients;

    private String subject;
    private String body;
    private String htmlBody;

    // Template-based bulk email
    private String templateName;
    private Map<String, Object> commonVariables; // Variables common to all recipients
    private List<RecipientVariable> recipientVariables; // Per-recipient variables

    private String emailType; // MARKETING, TRANSACTIONAL, NEWSLETTER

    private String fromEmail;
    private String fromName;
    private String replyTo;

    // Bulk email settings
    private boolean personalizeGreeting;
    private boolean includeUnsubscribeLink;
    private boolean trackOpens;
    private boolean trackClicks;

    // Scheduling (for v2.0)
    // private LocalDateTime scheduledTime;
    // private String timezone;

    // Segmentation
    private String segmentId;
    private String campaignId;

    // Rate limiting
    private Integer batchSize; // Number of emails to send per batch
    private Integer delayBetweenBatches; // Delay in seconds

    @Valid
    private List<AttachmentDto> attachments;

    /**
     * Per-recipient personalization variables
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientVariable {
        private String recipientEmail;
        private Map<String, Object> variables;
        private String recipientName;
        private String customGreeting;
    }

    // Helper methods

    public boolean isTemplateBased() {
        return templateName != null && !templateName.trim().isEmpty();
    }

    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    public boolean isMarketing() {
        return "MARKETING".equalsIgnoreCase(emailType) || "NEWSLETTER".equalsIgnoreCase(emailType);
    }

    public boolean isTransactional() {
        return "TRANSACTIONAL".equalsIgnoreCase(emailType);
    }

    public int getEffectiveBatchSize() {
        return batchSize != null && batchSize > 0 ? batchSize : 50; // Default 50 emails per batch
    }

    public int getEffectiveDelay() {
        return delayBetweenBatches != null && delayBetweenBatches > 0 ? delayBetweenBatches : 1; // Default 1 second
    }
}