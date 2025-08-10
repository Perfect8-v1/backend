package com.perfect8.email.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class BulkEmailRequestDto {

    @NotEmpty(message = "Recipients list cannot be empty")
    @Size(max = 100, message = "Maximum 100 recipients allowed per batch")
    private List<String> recipients;

    @NotNull(message = "Subject is required")
    private String subject;

    @NotNull(message = "Template name is required")
    private String templateName;

    private Map<String, Object> templateVariables;

    private boolean sendAsynchronously = true;

    private String campaignId;

    private Map<String, String> headers;

    private List<String> cc;

    private List<String> bcc;

    private boolean trackOpens = true;

    private boolean trackClicks = true;
}