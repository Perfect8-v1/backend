-- email-CREATE-TABLE.sql
-- Database: emailDB
-- Created: 2025-11-09
-- Purpose: Create tables for email-service (email templates)

-- ==============================================
-- Table: email_templates
-- Purpose: HTML email templates with variables
-- ==============================================

CREATE TABLE IF NOT EXISTS email_templates (
    email_template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_date DATETIME(6),
    updated_date DATETIME(6),
    
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Note: EmailMessage is a DTO/Model, not an @Entity
-- Email messages are not persisted in database (sent via SMTP only)

-- End of email-CREATE-TABLE.sql
