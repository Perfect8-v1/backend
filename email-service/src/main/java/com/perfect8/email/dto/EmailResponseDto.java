package com.perfect8.email.dto;

import com.perfect8.email.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Email Response DTO - Version 1.0
 * Enkel response för Gmail-baserad email-sändning
 * Gmail hanterar all retry, loggning och tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponseDto {

    // Grundläggande identifiering
    private String trackingId;  // Vårt interna tracking-id
    private String recipientEmail;
    private String subject;

    // Status
    private EmailStatus status;  // SENT, FAILED, QUEUED
    private String statusMessage;
    private LocalDateTime sentAt;

    // Felhantering (om något går fel)
    private String errorMessage;
    private String errorCode;

    // Metadata för egen tracking
    private Map<String, String> metadata;

    // V1.0 Factory-metoder för vanliga scenarion

    public static EmailResponseDto success(String recipientEmail, String subject) {
        return EmailResponseDto.builder()
                .trackingId("email-" + System.currentTimeMillis())
                .recipientEmail(recipientEmail)
                .subject(subject)
                .status(EmailStatus.SENT)
                .statusMessage("Email sent successfully via Gmail")
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static EmailResponseDto failed(String recipientEmail, String errorMessage) {
        return EmailResponseDto.builder()
                .trackingId("error-" + System.currentTimeMillis())
                .recipientEmail(recipientEmail)
                .status(EmailStatus.FAILED)
                .statusMessage("Failed to send email")
                .errorMessage(errorMessage)
                .errorCode("SEND_FAILED")
                .build();
    }

    public static EmailResponseDto queued(String recipientEmail, String subject) {
        return EmailResponseDto.builder()
                .trackingId("queue-" + System.currentTimeMillis())
                .recipientEmail(recipientEmail)
                .subject(subject)
                .status(EmailStatus.QUEUED)
                .statusMessage("Email queued for sending")
                .build();
    }

    // Helper-metoder
    public boolean isSuccessful() {
        return status == EmailStatus.SENT;
    }

    public boolean isFailed() {
        return status == EmailStatus.FAILED;
    }

    // V2.0 - Kommenterat bort för framtida användning
    /*
    private RecipientInfo recipientInfo;  // Komplex mottagarinfo
    private StatusInfo statusInfo;         // Detaljerad statusinfo
    private RetryInfo retryInfo;          // Retry-logik
    private ProviderInfo providerInfo;    // Provider-specifik info
    private QueueInfo queueInfo;          // Köhantering
    private ReferenceInfo referenceInfo;  // Referenser till order/kund

    // Alla inner classes för v2.0...
    */
}