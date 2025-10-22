package com.perfect8.email.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Email template entity for storing reusable email templates
 * Version 1.0 - Core template functionality
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 * FIXED: createdAt/updatedAt → createdDate/updatedDate (Magnum Opus)
 * FIXED: id → emailTemplateId (Magnum Opus)
 */
@Entity
@Table(name = "email_templates",
        indexes = {
                @Index(name = "idx_template_name", columnList = "name"),
                @Index(name = "idx_template_active", columnList = "active")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailTemplateId;  // → DB: email_template_id (Magnum Opus)

    @Column(unique = true, nullable = false, length = 100)
    private String name;  // → DB: name

    @Column(nullable = false, length = 500)
    private String subject;  // → DB: subject

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;  // → DB: content

    @Column(columnDefinition = "TEXT")
    private String htmlContent;  // → DB: html_content

    @Column(length = 500)
    private String description;  // → DB: description

    @Column(length = 50)
    private String templateType;  // → DB: template_type (ORDER, MARKETING, SYSTEM, NEWSLETTER)

    @Column(length = 50)
    private String category;  // → DB: category (TRANSACTIONAL, PROMOTIONAL, INFORMATIONAL)

    @Column
    @Builder.Default
    private boolean active = true;  // → DB: active

    @Column
    @Version
    private Long version;  // → DB: version

    @Column(length = 100)
    private String createdBy;  // → DB: created_by

    @Column(length = 100)
    private String updatedBy;  // → DB: updated_by

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    @Column
    private LocalDateTime updatedDate;  // → DB: updated_date (Magnum Opus)

    // Template variables tracking
    @Column(length = 1000)
    private String requiredVariables;  // → DB: required_variables (Comma-separated list)

    @Column(length = 1000)
    private String optionalVariables;  // → DB: optional_variables (Comma-separated list)

    // Usage tracking
    @Column
    @Builder.Default
    private Long usageCount = 0L;  // → DB: usage_count

    private LocalDateTime lastUsedAt;  // → DB: last_used_at

    // Version 2.0 features - commented out
    // @Column(length = 10)
    // private String language; // en, es, fr, etc.

    // @Column(length = 10)
    // private String abTestVariant; // A, B, C for A/B testing

    // private Double performanceScore; // Based on open/click rates

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (usageCount == null) {
            usageCount = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Helper methods

    public boolean hasHtmlContent() {
        return htmlContent != null && !htmlContent.trim().isEmpty();
    }

    public boolean isTransactional() {
        return "TRANSACTIONAL".equalsIgnoreCase(category) ||
                "ORDER".equalsIgnoreCase(templateType);
    }

    public boolean isMarketing() {
        return "PROMOTIONAL".equalsIgnoreCase(category) ||
                "MARKETING".equalsIgnoreCase(templateType) ||
                "NEWSLETTER".equalsIgnoreCase(templateType);
    }

    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
        this.lastUsedAt = LocalDateTime.now();
    }

    public String[] getRequiredVariablesArray() {
        if (requiredVariables == null || requiredVariables.trim().isEmpty()) {
            return new String[0];
        }
        return requiredVariables.split(",");
    }

    public String[] getOptionalVariablesArray() {
        if (optionalVariables == null || optionalVariables.trim().isEmpty()) {
            return new String[0];
        }
        return optionalVariables.split(",");
    }
}