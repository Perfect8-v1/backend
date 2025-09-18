package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email Request DTO - Version 1.0
 * Enkel DTO för att skicka email
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    private String recipientEmail;
    private String subject;
    private String content;
    private boolean html;  // true för HTML, false för plain text

    // Ytterligare fält som kan vara användbara
    private String recipientName;  // Optional - för personalisering
    private String replyTo;        // Optional - reply-to adress
    private String cc;             // Optional - carbon copy
    private String bcc;            // Optional - blind carbon copy

    /**
     * Hjälpmetod för bakåtkompatibilitet om någon kod använder isHtml()
     */
    public boolean isHtml() {
        return html;
    }
}