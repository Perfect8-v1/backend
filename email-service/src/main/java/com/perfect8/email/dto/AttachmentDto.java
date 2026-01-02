package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for email attachments
 * Version 1.0 - Core functionality for order documents
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {

    private String filename;
    private String contentType;
    private byte[] content;
    private String contentId; // For inline attachments (e.g., embedded images)
    private boolean isInline;

    // Size in bytes
    public long getSize() {
        return content != null ? content.length : 0;
    }

    // Size in KB
    public double getSizeInKB() {
        return getSize() / 1024.0;
    }

    // Size in MB
    public double getSizeInMB() {
        return getSizeInKB() / 1024.0;
    }

    // Validate attachment size (max 10MB for email)
    public boolean isValidSize() {
        return getSizeInMB() <= 10.0;
    }

    // Check if it's a valid content type for email
    public boolean isValidContentType() {
        if (contentType == null) return false;

        // Allow common document and image types
        return contentType.startsWith("image/") ||
                contentType.startsWith("application/pdf") ||
                contentType.startsWith("application/msword") ||
                contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
                contentType.startsWith("text/") ||
                contentType.equals("application/octet-stream");
    }
}