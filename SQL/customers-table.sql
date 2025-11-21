-- =============================================
-- Perfect8 Shop Database - Customers Table
-- UPDATED: 2025-11-20
-- Removed auth fields (handled by admin-service)
-- =============================================

-- Drop existing table if needed (WARNING: destroys data!)
-- DROP TABLE IF EXISTS customers;

-- =============================================
-- Customers Table
-- =============================================
CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Reference to User in admin-service (null for guest checkout)
    user_id BIGINT,
    
    -- Personal info
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    
    -- Account status
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified_date DATETIME,
    
    -- Preferences
    newsletter_subscribed BOOLEAN NOT NULL DEFAULT FALSE,
    marketing_consent BOOLEAN NOT NULL DEFAULT FALSE,
    preferred_language VARCHAR(10),
    preferred_currency VARCHAR(3),
    
    -- Audit
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME,
    last_login_date DATETIME,
    
    -- Indexes
    INDEX idx_customers_email (email),
    INDEX idx_customers_user_id (user_id),
    INDEX idx_customers_active (is_active),
    INDEX idx_customers_created (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
