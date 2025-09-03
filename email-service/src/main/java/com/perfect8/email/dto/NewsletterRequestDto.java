package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * DTO for newsletter email requests
 * Version 1.0 - Core newsletter functionality
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterRequestDto {

    @NotEmpty(message = "Recipients list cannot be empty")
    @Size(max = 5000, message = "Cannot send to more than 5000 recipients in one newsletter batch")
    private List<String> recipients;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    private String htmlContent;

    private String blogPostUrl;

    private String blogPostTitle;

    private String blogPostExcerpt;

    private String blogPostAuthor;

    private String blogPostImageUrl;

    // Template variables for newsletter
    private Map<String, Object> templateVariables;

    // Newsletter metadata
    private String newsletterType; // WEEKLY, MONTHLY, SPECIAL, BLOG_UPDATE

    private String category;

    private List<String> tags;

    // Personalization options
    private boolean personalizeGreeting;

    @lombok.Builder.Default
    private boolean includeUnsubscribeLink = true;

    @lombok.Builder.Default
    private boolean includeViewInBrowserLink = true;

    // Segmentation
    private String segmentId;

    private List<String> excludeRecipients;

    // Version 2.0 features - commented out
    // private boolean trackOpens;
    // private boolean trackClicks;
    // private LocalDateTime scheduledSendTime;
    // private String abTestVariant;

    // Helper methods

    public boolean hasHtmlContent() {
        return htmlContent != null && !htmlContent.trim().isEmpty();
    }

    public boolean isBlogRelated() {
        return blogPostUrl != null && !blogPostUrl.trim().isEmpty();
    }

    public boolean hasTemplateVariables() {
        return templateVariables != null && !templateVariables.isEmpty();
    }

    public int getRecipientCount() {
        return recipients != null ? recipients.size() : 0;
    }

    public boolean shouldPersonalize() {
        return personalizeGreeting && getRecipientCount() <= 1000; // Limit personalization for performance
    }
}