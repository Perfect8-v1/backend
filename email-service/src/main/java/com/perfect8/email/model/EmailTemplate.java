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
    private Long emailTemplateId;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "template_type", length = 50)
    private String templateType; // ORDER, MARKETING, SYSTEM, NEWSLETTER

    @Column(name = "category", length = 50)
    private String category; // TRANSACTIONAL, PROMOTIONAL, INFORMATIONAL

    @Column(name = "active")
    @lombok.Builder.Default
    private boolean active = true;

    @Column(name = "version")
    @Version
    private Long version;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Template variables tracking
    @Column(name = "required_variables", length = 1000)
    private String requiredVariables; // Comma-separated list of required variable names

    @Column(name = "optional_variables", length = 1000)
    private String optionalVariables; // Comma-separated list of optional variable names

    // Usage tracking
    @Column(name = "usage_count")
    @lombok.Builder.Default
    private Long usageCount = 0L;

    @Column(name = "last_used_date")
    private LocalDateTime lastUsedDate;

    // Version 2.0 features - commented out
    // @Column(name = "language", length = 10)
    // private String language; // en, es, fr, etc.

    // @Column(name = "ab_test_variant", length = 10)
    // private String abTestVariant; // A, B, C for A/B testing

    // @Column(name = "performance_score")
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
        this.lastUsedDate = LocalDateTime.now();
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
