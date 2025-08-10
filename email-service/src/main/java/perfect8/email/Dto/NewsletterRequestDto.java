package com.perfect8.email.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class NewsletterRequestDto {

    @NotEmpty(message = "Recipients list cannot be empty")
    private List<String> recipients;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    private String blogPostUrl;

    // Getters and setters
    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getBlogPostUrl() { return blogPostUrl; }
    public void setBlogPostUrl(String blogPostUrl) { this.blogPostUrl = blogPostUrl; }
}
