-- =============================================
-- Perfect8 Auth Database Schema
-- Service: admin-service (adminDB)
-- Version: 1.0
-- Date: 2025-11-19
-- =============================================

-- Drop existing tables (om du behöver starta om)
-- DROP TABLE IF EXISTS refresh_tokens;
-- DROP TABLE IF EXISTS user_roles;
-- DROP TABLE IF EXISTS users;

-- =============================================
-- Users Table (Central Auth)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Core identity
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    
    -- Account status
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Verification tokens
    email_verification_token VARCHAR(255),
    reset_password_token VARCHAR(255),
    reset_password_token_expiry DATETIME,
    
    -- Security
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked_until DATETIME,
    
    -- Audit
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME,
    last_login_date DATETIME,
    
    -- Indexes
    INDEX idx_users_email (email),
    INDEX idx_users_active (is_active),
    INDEX idx_users_created (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- User Roles (Many-to-Many via ElementCollection)
-- =============================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_user_roles_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Refresh Tokens
-- =============================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    token_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    
    -- Token data
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    
    -- Revocation
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_date DATETIME,
    replaced_by_token VARCHAR(500),
    
    -- Metadata
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    
    -- Audit
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_user (user_id),
    INDEX idx_refresh_tokens_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Initial Data: Default Admin User
-- Password: admin123 (BCrypt hash)
-- =============================================
INSERT INTO users (
    email, 
    password_hash, 
    first_name, 
    last_name, 
    is_active, 
    is_email_verified,
    created_date
) VALUES (
    'admin@perfect8.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGdgSyq.q/sNxYxvGpjyFq7Oe',
    'System',
    'Admin',
    TRUE,
    TRUE,
    NOW()
);

-- Assign SUPER_ADMIN role to default admin
INSERT INTO user_roles (user_id, role) 
SELECT user_id, 'SUPER_ADMIN' FROM users WHERE email = 'admin@perfect8.com';

INSERT INTO user_roles (user_id, role) 
SELECT user_id, 'ADMIN' FROM users WHERE email = 'admin@perfect8.com';