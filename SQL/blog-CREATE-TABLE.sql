-- ================================================
-- blog-CREATE-TABLE.sql
-- Database: blogDB
-- Created: 2025-11-09 (FIXED: 2025-11-10)
-- Purpose: Create tables for blog-service (CMS)
-- 
-- FIXES APPLIED:
-- - author_id → user_id (User är User, inte Author)
-- - is_published → published (Hibernate boolean naming)
-- - Boolean fields: Java isPublished → MySQL published
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
-- Notes: user_id references User (not "author_id")
--        published field (Java: isPublished → MySQL: published)
-- ================================================

CREATE TABLE IF NOT EXISTS posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,  -- FIXED: author_id → user_id
    published_date DATETIME(6),
    created_date DATETIME(6),
    updated_date DATETIME(6),
    published BOOLEAN NOT NULL DEFAULT FALSE,  -- FIXED: Java isPublished → MySQL published
    view_count INT NOT NULL DEFAULT 0,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id),  -- FIXED: author_id → user_id
    INDEX idx_slug (slug),
    INDEX idx_user_id (user_id),  -- FIXED: idx_author_id → idx_user_id
    INDEX idx_published (published),  -- FIXED: idx_is_published → idx_published
    INDEX idx_published_date (published_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: image_references
-- Purpose: Links between posts and images
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
-- - User är User (inte Author) - renare och mer logiskt
-- - Boolean fields: remove "is" prefix (Hibernate standard)
-- - ID fields: [entityName]Id (Magnum Opus compliance)
-- ================================================
