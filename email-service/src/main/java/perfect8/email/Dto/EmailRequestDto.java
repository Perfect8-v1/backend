package com.perfect8.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String htmlContent;

    private String plainTextContent;

    private String templateName;

    private Map<String, Object> templateVariables;

    private List<String> cc;

    private List<String> bcc;

    private List<AttachmentDto> attachments;

    private Map<String, String> headers;

    private boolean highPriority = false;
}