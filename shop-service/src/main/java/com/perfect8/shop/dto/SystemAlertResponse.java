package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemAlertResponse {

    private Long id;
    private String type;
    private String severity;
    private String message;
    private String description;

    private LocalDateTime createdAt;
    private Boolean isRead;
    private Boolean isResolved;
    private LocalDateTime resolvedAt;

    private String source;
    private String category;
    private Integer priority;

    private String actionRequired;
    private String recommendedAction;
}