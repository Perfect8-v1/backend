-- email-CREATE-TABLE.sql
-- Database: emailDB
-- Created: 2025-11-09
-- Updated: 2025-11-13 - Fixed schema validation errors
-- Purpose: Create tables for email-service (email templates)

-- ==============================================
-- Table: email_templates
-- Purpose: HTML email templates with variables
-- Matches: com.perfect8.email.model.EmailTemplate
-- ==============================================

CREATE TABLE IF NOT EXISTS email_templates (
    -- Primary Key
    email_template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Core template fields
    name VARCHAR(100) NOT NULL UNIQUE,
    subject VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    html_content TEXT,
    description VARCHAR(500),
    
    -- Template classification
    template_type VARCHAR(50),
    category VARCHAR(50),
    
    -- Status & versioning
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    
    -- Audit fields
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_date DATETIME(6) NOT NULL,
    updated_date DATETIME(6),
    
    -- Template variables
    required_variables VARCHAR(1000),
    optional_variables VARCHAR(1000),
    
    -- Usage tracking
    usage_count BIGINT DEFAULT 0,
    last_used_date DATETIME(6),
    
    -- Indexes (matching @Index annotations in Entity)
    INDEX idx_template_name (name),
    INDEX idx_template_active (active),
    INDEX idx_template_type (template_type),
    INDEX idx_category (category)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- Note about EmailMessage.java
-- ==============================================
-- EmailMessage is a DTO/Model, NOT an @Entity
-- Email messages are not persisted in database
-- They are sent via SMTP only (transient data)

-- End of email-CREATE-TABLE.sql
