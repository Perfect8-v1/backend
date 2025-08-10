package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatusDto {
    private String emailId;
    private String status;
    private String recipient;
    private String subject;
    private LocalDateTime sentAt;
    private String failureReason;
    private Integer retryCount;
    private LocalDateTime lastRetryAt;
}
