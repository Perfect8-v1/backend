-- ================================================
-- blog-CREATE-TABLE.sql
-- Database: blogDB
-- Created: 2025-11-09
-- Updated: 2025-11-12 (FIXED: 100% Entity Match)
-- Purpose: Create tables for blog-service (CMS)
-- 
-- FIXES APPLIED (2025-11-12):
-- - Removed unnecessary fields (v1.0 focus)
-- - 100% match with Java entities
-- - Magnum Opus compliance
-- ================================================

-- ================================================
-- Table: roles
-- Purpose: Role definitions (ROLE_USER, ROLE_ADMIN)
-- ================================================

CREATE TABLE IF NOT EXISTS roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: users
-- Purpose: Blog user accounts
-- ================================================

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_date DATETIME(6),
    updated_date DATETIME(6),
    
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: user_roles
-- Purpose: Join table for User-Role many-to-many
-- ================================================

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: posts
-- Purpose: Blog posts
-- Notes: Removed excerpt (v1.0 - not needed)
-- ================================================

CREATE TABLE IF NOT EXISTS posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    published_date DATETIME(6),
    created_date DATETIME(6),
    updated_date DATETIME(6),
    published BOOLEAN NOT NULL DEFAULT FALSE,
    view_count INT NOT NULL DEFAULT 0,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_slug (slug),
    INDEX idx_user_id (user_id),
    INDEX idx_published (published),
    INDEX idx_published_date (published_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: image_references
-- Purpose: Links between posts and images
-- Notes: Simplified for v1.0 (no url, alt fields)
-- ================================================

CREATE TABLE IF NOT EXISTS image_references (
    image_reference_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_id BIGINT NOT NULL,
    caption VARCHAR(500),
    display_order INT NOT NULL DEFAULT 0,
    created_date DATETIME(6),
    
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_image_id (image_id),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- End of blog-CREATE-TABLE.sql
-- 
-- KEY PRINCIPLES:
-- - Version 1.0 focus: Core functionality only
-- - 100% match with Java entities
-- - Magnum Opus compliance: [entityName]Id, no "is" prefix
-- ================================================
