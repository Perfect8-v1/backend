package com.perfect8.email.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * EmailRequestDto - Alias/wrapper for EmailRequest to maintain compatibility
 * Version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailRequestDto extends EmailRequest {

    // This class extends EmailRequest to provide compatibility
    // for code that uses EmailRequestDto

    /**
     * Create EmailRequestDto from EmailRequest
     */
    public static EmailRequestDto fromEmailRequest(EmailRequest request) {
        if (request == null) {
            return null;
        }

        if (request instanceof EmailRequestDto) {
            return (EmailRequestDto) request;
        }

        return EmailRequestDto.builder()
                .to(request.getTo())
                .cc(request.getCc())
                .bcc(request.getBcc())
                .subject(request.getSubject())
                .body(request.getBody())
                .htmlBody(request.getHtmlBody())
                .content(request.getContent())
                .emailType(request.getEmailType())
                .templateName(request.getTemplateName())
                .templateVariables(request.getTemplateVariables())
                .fromEmail(request.getFromEmail())
                .fromName(request.getFromName())
                .replyTo(request.getReplyTo())
                .attachments(request.getAttachments())
                .isHighPriority(request.isHighPriority())
                .customerId(request.getCustomerId())
                .orderId(request.getOrderId())
                .referenceId(request.getReferenceId())
                .headers(request.getHeaders())
                .metadata(request.getMetadata())
                .build();
    }

    /**
     * Convert to EmailRequest (though it already is one through inheritance)
     */
    public EmailRequest toEmailRequest() {
        return this;
    }
}