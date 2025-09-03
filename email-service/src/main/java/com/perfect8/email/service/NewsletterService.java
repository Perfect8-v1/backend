package com.perfect8.email.service;

import com.perfect8.email.dto.EmailRequestDto;
import com.perfect8.email.dto.EmailResponseDto;
import com.perfect8.email.dto.NewsletterRequestDto;
import com.perfect8.email.dto.BulkEmailRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Newsletter service for handling newsletter emails
 * Version 1.0 - Core newsletter functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final EmailService emailService;
    private final TemplateService templateService;

    /**
     * Send newsletter to all recipients
     */
    public List<EmailResponseDto> sendNewsletter(NewsletterRequestDto request) {
        log.info("Sending newsletter to {} recipients", request.getRecipientCount());

        // Prepare the newsletter content
        String htmlContent = prepareNewsletterContent(request);

        // Convert to bulk email request
        BulkEmailRequestDto bulkRequest = BulkEmailRequestDto.builder()
                .recipients(request.getRecipients())
                .subject(request.getSubject())
                .htmlBody(htmlContent)
                .emailType("NEWSLETTER")
                .personalizeGreeting(request.isPersonalizeGreeting())
                .includeUnsubscribeLink(request.isIncludeUnsubscribeLink())
                .build();

        // Send using bulk email service
        return emailService.sendBulkEmails(bulkRequest);
    }

    /**
     * Send blog post notification newsletter
     */
    public List<EmailResponseDto> sendBlogPostNotification(NewsletterRequestDto request) {
        log.info("Sending blog post notification to {} recipients", request.getRecipientCount());

        // Prepare template variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("postTitle", request.getBlogPostTitle());
        variables.put("postExcerpt", request.getBlogPostExcerpt());
        variables.put("postUrl", request.getBlogPostUrl());
        variables.put("postAuthor", request.getBlogPostAuthor());
        variables.put("postImageUrl", request.getBlogPostImageUrl());

        // Process template
        String htmlContent = templateService.processTemplate("new-post-notification", variables);

        // Convert to bulk email request
        BulkEmailRequestDto bulkRequest = BulkEmailRequestDto.builder()
                .recipients(request.getRecipients())
                .subject("New Blog Post: " + request.getBlogPostTitle())
                .htmlBody(htmlContent)
                .emailType("BLOG_UPDATE")
                .includeUnsubscribeLink(true)
                .build();

        return emailService.sendBulkEmails(bulkRequest);
    }

    /**
     * Send personalized newsletter
     */
    public List<EmailResponseDto> sendPersonalizedNewsletter(NewsletterRequestDto request,
                                                             Map<String, Map<String, Object>> recipientVariables) {
        log.info("Sending personalized newsletter to {} recipients", request.getRecipientCount());

        // Create recipient-specific variables
        BulkEmailRequestDto.RecipientVariable[] recipientVars = request.getRecipients().stream()
                .map(email -> {
                    Map<String, Object> vars = recipientVariables.getOrDefault(email, new HashMap<>());
                    return BulkEmailRequestDto.RecipientVariable.builder()
                            .recipientEmail(email)
                            .variables(vars)
                            .build();
                })
                .toArray(BulkEmailRequestDto.RecipientVariable[]::new);

        // Convert to bulk email request with personalization
        BulkEmailRequestDto bulkRequest = BulkEmailRequestDto.builder()
                .recipients(request.getRecipients())
                .subject(request.getSubject())
                .templateName("newsletter")
                .commonVariables(request.getTemplateVariables())
                .recipientVariables(List.of(recipientVars))
                .emailType("NEWSLETTER")
                .personalizeGreeting(true)
                .includeUnsubscribeLink(true)
                .build();

        return emailService.sendBulkEmails(bulkRequest);
    }

    /**
     * Prepare newsletter HTML content
     */
    private String prepareNewsletterContent(NewsletterRequestDto request) {
        if (request.hasHtmlContent()) {
            return request.getHtmlContent();
        } else if (request.hasTemplateVariables()) {
            // Use template if variables are provided
            Map<String, Object> variables = new HashMap<>(request.getTemplateVariables());
            variables.put("title", request.getSubject());
            variables.put("content", request.getContent());
            return templateService.processTemplate("newsletter", variables);
        } else {
            // Create simple HTML wrapper for plain text content
            return wrapInHtmlTemplate(request.getSubject(), request.getContent());
        }
    }

    /**
     * Wrap plain text content in a simple HTML template
     */
    private String wrapInHtmlTemplate(String subject, String content) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>%s</h1>
                </div>
                <div class="content">
                    %s
                </div>
                <div class="footer">
                    <p>© 2024 Perfect8. All rights reserved.</p>
                    <p><a href="{{unsubscribeUrl}}">Unsubscribe</a></p>
                </div>
            </body>
            </html>
            """, subject, content);
    }
}