package com.perfect8.email.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterRequestDto {

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    // V1.0 - Segment behålls för framtida användning, defaultar till "ALL"
    private String segment;

    // V2.0 - Förberedda fält (används inte än)
    private String templateId;
    private List<String> tags;
    private Boolean trackOpens;
    private Boolean trackClicks;

    // Getter med default-värde för v1.0
    public String getSegment() {
        // V1.0 - Alltid returnera "ALL" oavsett vad som skickats in
        // Detta hårdkodar segment till ALL som diskuterat
        return "ALL";
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }
}