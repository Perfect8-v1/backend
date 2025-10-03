-- Migration to update email_logs table with object-oriented support
-- File: src/main/resources/db/migration/V2__update_email_logs_with_object_support.sql

-- Add new columns if they don't exist
ALTER TABLE email_logs
ADD COLUMN IF NOT EXISTS cc_recipients VARCHAR(1000),
ADD COLUMN IF NOT EXISTS bcc_recipients VARCHAR(1000),
ADD COLUMN IF NOT EXISTS message_type VARCHAR(50),
ADD COLUMN IF NOT EXISTS tracking_id VARCHAR(50),
ADD COLUMN IF NOT EXISTS campaign_id VARCHAR(50),
ADD COLUMN IF NOT EXISTS priority INT DEFAULT 3,
ADD COLUMN IF NOT EXISTS language_code VARCHAR(10) DEFAULT 'sv-SE',
ADD COLUMN IF NOT EXISTS template_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS reference_id VARCHAR(100),
ADD COLUMN IF NOT EXISTS reference_type VARCHAR(50),
ADD COLUMN IF NOT EXISTS opened_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS clicked_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS bounced_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS next_retry_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS error_code VARCHAR(50),
ADD COLUMN IF NOT EXISTS smtp_message_id VARCHAR(255),
ADD COLUMN IF NOT EXISTS from_email VARCHAR(255),
ADD COLUMN IF NOT EXISTS reply_to VARCHAR(255),
ADD COLUMN IF NOT EXISTS attachment_count INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS total_size_bytes BIGINT,
ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45),
ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500),
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) DEFAULT 'system',
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Version 2.0 fields (for future use)
ALTER TABLE email_logs
ADD COLUMN IF NOT EXISTS unsubscribed_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS spam_score DECIMAL(5,2),
ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMP;

-- Update existing rows with default values
UPDATE email_logs
SET
    message_type = CASE
        WHEN subject LIKE '%Order%' THEN 'ORDER_CONFIRMATION'
        WHEN subject LIKE '%Shipping%' OR subject LIKE '%Skickat%' THEN 'SHIPPING_NOTIFICATION'
        WHEN subject LIKE '%Welcome%' OR subject LIKE '%Välkommen%' THEN 'WELCOME_EMAIL'
        ELSE 'SIMPLE_EMAIL'
    END,
    priority = 3,
    language_code = 'sv-SE',
    tracking_id = CONCAT('TRACK-', SUBSTRING(MD5(CONCAT(email_id, recipient)), 1, 8)),
    created_by = 'migration',
    updated_at = CURRENT_TIMESTAMP
WHERE message_type IS NULL;

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_email_log_tracking_id ON email_logs(tracking_id);
CREATE INDEX IF NOT EXISTS idx_email_log_message_type ON email_logs(message_type);
CREATE INDEX IF NOT EXISTS idx_email_log_priority ON email_logs(priority);
CREATE INDEX IF NOT EXISTS idx_email_log_reference_id ON email_logs(reference_id);
CREATE INDEX IF NOT EXISTS idx_email_log_campaign_id ON email_logs(campaign_id);
CREATE INDEX IF NOT EXISTS idx_email_log_sent_at ON email_logs(sent_at);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_email_log_status_priority ON email_logs(status, priority);
CREATE INDEX IF NOT EXISTS idx_email_log_recipient_created ON email_logs(recipient, created_at DESC);

-- Add check constraint for priority values
ALTER TABLE email_logs
ADD CONSTRAINT chk_email_priority
CHECK (priority IS NULL OR priority BETWEEN 1 AND 3);

-- Add check constraint for status enum
ALTER TABLE email_logs
ADD CONSTRAINT chk_email_status
CHECK (status IN ('PENDING', 'QUEUED', 'SENDING', 'SENT', 'DELIVERED',
                  'OPENED', 'CLICKED', 'BOUNCED', 'FAILED', 'CANCELLED', 'SPAM'));

-- Create a view for email statistics (useful for dashboard)
CREATE OR REPLACE VIEW email_statistics AS
SELECT
    DATE(created_at) as email_date,
    message_type,
    status,
    COUNT(*) as email_count,
    AVG(retry_count) as avg_retries,
    SUM(CASE WHEN status = 'SENT' THEN 1 ELSE 0 END) as sent_count,
    SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed_count,
    SUM(CASE WHEN status = 'BOUNCED' THEN 1 ELSE 0 END) as bounced_count
FROM email_logs
GROUP BY DATE(created_at), message_type, status;

-- Create a view for recent failed emails (for monitoring)
CREATE OR REPLACE VIEW recent_failed_emails AS
SELECT
    email_id,
    recipient,
    subject,
    message_type,
    tracking_id,
    error_message,
    error_code,
    retry_count,
    created_at,
    next_retry_at
FROM email_logs
WHERE status IN ('FAILED', 'BOUNCED')
    AND created_at > DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY created_at DESC;

-- Add comment to table for documentation
ALTER TABLE email_logs COMMENT = 'Stores all email communications with full tracking and retry capabilities';

-- Log migration completion
INSERT INTO email_logs (
    recipient,
    subject,
    body,
    is_html,
    status,
    message_type,
    created_at,
    created_by
) VALUES (
    'admin@perfect8.com',
    'Database Migration Completed - Email Logs Updated',
    'Email logs table has been successfully updated with object-oriented support.',
    false,
    'SENT',
    'SYSTEM_NOTIFICATION',
    NOW(),
    'migration_v2'
);