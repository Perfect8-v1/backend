package com.perfect8.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    private Long emailId;
    private String messageType; // ORDER_CONFIRMATION, SHIPPING_NOTIFICATION, etc.

    // Recipients
    @Builder.Default
    private List<String> recipients = new ArrayList<>();

    @Builder.Default
    private List<String> ccRecipients = new ArrayList<>();

    @Builder.Default
    private List<String> bccRecipients = new ArrayList<>();

    // Content
    private String subject;
    private Object contentObject; // Can be OrderEmailDTO, CustomerEmailDTO, etc.

    // Template data
    private String templateName;

    @Builder.Default
    private Map<String, Object> templateVariables = new HashMap<>();

    // Metadata
    private LocalDateTime scheduledTime;
    private Integer priority; // 1 = high, 2 = normal, 3 = low
    private String language; // sv-SE, en-US, etc.

    // Tracking
    private String trackingId;
    private String campaignId;

    // Status
    private String status;
    private LocalDateTime sentAt;
    private Integer retryCount;

    // Attachments (for future use)
    @Builder.Default
    private List<EmailAttachment> attachments = new ArrayList<>();

    // Helper methods
    public void addRecipient(String email) {
        if (this.recipients == null) {
            this.recipients = new ArrayList<>();
        }
        this.recipients.add(email);
    }

    public void addTemplateVariable(String key, Object value) {
        if (this.templateVariables == null) {
            this.templateVariables = new HashMap<>();
        }
        this.templateVariables.put(key, value);
    }

    public boolean isHtml() {
        return templateName != null && !templateName.isEmpty();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailAttachment {
        private String fileName;
        private byte[] content;
        private String contentType;
        private Long fileSize;
    }
}