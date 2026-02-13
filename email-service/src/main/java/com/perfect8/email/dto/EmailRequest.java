package com.perfect8.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email Request DTO - Version 1.1
 * Enkel DTO för att skicka email
 * 
 * v1.1: Lagt till email-validering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String recipientEmail;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private boolean html;

    // Ytterligare fält
    private String recipientName;
    
    @Email(message = "Invalid reply-to email format")
    private String replyTo;
    
    @Email(message = "Invalid CC email format")
    private String cc;
    
    @Email(message = "Invalid BCC email format")
    private String bcc;

    public boolean isHtml() {
        return html;
    }
}
