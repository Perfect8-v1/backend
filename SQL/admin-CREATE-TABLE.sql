-- admin-CREATE-TABLE.sql
-- Database: adminDB
-- Created: 2025-11-09
-- Purpose: Create tables for admin-service (authentication & authorization)

-- ==============================================
-- Table: admin_users
-- Purpose: Admin user accounts with roles
-- ==============================================

CREATE TABLE IF NOT EXISTS admin_users (
    admin_user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATETIME(6),
    updated_date DATETIME(6),
    last_login_date DATETIME(6),
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- End of admin-CREATE-TABLE.sql