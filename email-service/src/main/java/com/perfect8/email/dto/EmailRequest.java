package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.List;

/**
 * Email request DTO for email service
 * Used for requesting emails to be sent
 * Version 1.0 - Core functionality
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    private String cc;
    private String bcc;

    @NotBlank(message = "Email subject is required")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    private String subject;

    private String body;
    private String htmlBody;
    private String content; // Alias for compatibility

    @NotBlank(message = "Email type is required")
    private String emailType; // ORDER_CONFIRMATION, SHIPPING_NOTIFICATION, etc.

    private String templateName;
    private Map<String, Object> templateVariables;

    private String fromEmail;
    private String fromName;
    private String replyTo;

    private List<AttachmentDto> attachments;

    private boolean isHighPriority;

    // Version 2.0 features - commented out
    // private boolean trackOpens;
    // private boolean trackClicks;

    private String customerId;
    private String orderId;
    private String referenceId;

    private Map<String, String> headers;
    private Map<String, Object> metadata;

    // Helper methods
    public boolean hasHtmlContent() {
        return htmlBody != null && !htmlBody.trim().isEmpty();
    }

    public boolean hasTemplate() {
        return templateName != null && !templateName.trim().isEmpty();
    }

    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    // Override getContent for compatibility
    public String getContent() {
        if (content != null && !content.trim().isEmpty()) {
            return content;
        } else if (htmlBody != null && !htmlBody.trim().isEmpty()) {
            return htmlBody;
        } else if (body != null && !body.trim().isEmpty()) {
            return body;
        }
        return "";
    }

    // Ensure all fields have proper getters for compatibility
    public String getTo() { return to; }
    public String getCc() { return cc; }
    public String getBcc() { return bcc; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getHtmlBody() { return htmlBody; }
    public String getEmailType() { return emailType; }
    public String getTemplateName() { return templateName; }
    public Map<String, Object> getTemplateVariables() { return templateVariables; }
    public String getFromEmail() { return fromEmail; }
    public String getFromName() { return fromName; }
    public String getReplyTo() { return replyTo; }
    public List<AttachmentDto> getAttachments() { return attachments; }
    public boolean isHighPriority() { return isHighPriority; }
    public String getCustomerId() { return customerId; }
    public String getOrderId() { return orderId; }
    public String getReferenceId() { return referenceId; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, Object> getMetadata() { return metadata; }
}