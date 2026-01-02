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
public class ActivityFeedResponse {

    private Long activityFeedResponseId;
    private String type;
    private String message;
    private String description;

    private LocalDateTime timestamp;
    private Long userId;
    private String userName;
    private String userEmail;

    private String entityType;
    private Long entityId;
    private String entityName;

    private String action;
    private String severity;
    private String category;

    private Object metadata;
}