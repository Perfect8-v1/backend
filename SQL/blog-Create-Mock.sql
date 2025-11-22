-- =====================================================
-- BLOG-SERVICE KOMPLETT SETUP (FIXED)
-- Skapar: databas, tabeller, mock-data
-- Database: blogDB
-- FIX: Added created_date to image_references
-- =====================================================

-- 1. Skapa databas
CREATE DATABASE IF NOT EXISTS blogDB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE blogDB;

-- 2. Ta bort gamla tabeller (om de finns)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS image_references;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 3. SKAPA TABELLER
-- =====================================================

-- 3.1 USERS
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2 ROLES
CREATE TABLE roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.3 USER_ROLES (Many-to-Many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) 
        REFERENCES roles(role_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.4 POSTS
CREATE TABLE posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    author_id BIGINT NOT NULL,
    published_date DATETIME,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    view_count INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_slug (slug),
    INDEX idx_author_id (author_id),
    INDEX idx_is_published (is_published),
    INDEX idx_published_date (published_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.5 IMAGE_REFERENCES (FIXED: added created_date)
CREATE TABLE image_references (
    image_reference_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_id BIGINT NOT NULL,
    caption VARCHAR(500),
    display_order INT NOT NULL DEFAULT 0,
    created_date DATETIME NOT NULL,
    CONSTRAINT fk_image_references_post FOREIGN KEY (post_id) 
        REFERENCES posts(post_id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_image_id (image_id),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. MOCK DATA
-- =====================================================

-- Lösenord för alla användare: password123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi

-- 4.1 ROLES
INSERT INTO roles (role_id, name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN');

-- 4.2 USERS
INSERT INTO users (user_id, username, email, password_hash, first_name, last_name, created_date, updated_date) VALUES
(1, 'magnus', 'magnus@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Magnus', 'Berglund', '2025-01-01 08:00:00', '2025-01-01 08:00:00'),
(2, 'alice_writer', 'alice@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Alice', 'Writer', '2025-02-10 09:00:00', '2025-02-10 09:00:00'),
(3, 'bob_blogger', 'bob@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Bob', 'Blogger', '2025-03-15 10:00:00', '2025-03-15 10:00:00'),
(4, 'john_reader', 'john@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'John', 'Reader', '2025-04-20 11:00:00', '2025-04-20 11:00:00');

-- 4.3 USER_ROLES
INSERT INTO user_roles (user_id, role_id) VALUES
-- Magnus: ADMIN + USER
(1, 1),
(1, 2),
-- Alice: ADMIN + USER
(2, 1),
(2, 2),
-- Bob: USER only
(3, 1),
-- John: USER only
(4, 1);

-- 4.4 POSTS
INSERT INTO posts (post_id, title, content, slug, author_id, published_date, created_date, updated_date, is_published, view_count) VALUES
-- Published posts
(1, 'Welcome to Perfect8 Blog', 
'# Welcome to Perfect8!\n\nThis is our first blog post. We are excited to share our journey with you.\n\n## What is Perfect8?\n\nPerfect8 is a modern e-commerce platform built with Spring Boot and microservices architecture.\n\n### Features\n- Secure authentication\n- Fast performance\n- Scalable design\n\nStay tuned for more updates!', 
'welcome-to-perfect8-blog', 
1, 
'2025-01-15 10:00:00', 
'2025-01-10 09:00:00', 
'2025-01-10 09:00:00', 
true, 
245),

(2, 'Getting Started with Spring Boot Microservices', 
'# Spring Boot Microservices\n\nIn this post, we will explore how to build microservices using Spring Boot.\n\n## Architecture Overview\n\nOur system consists of 5 main services:\n1. Admin Service\n2. Blog Service\n3. Email Service\n4. Image Service\n5. Shop Service\n\nEach service has its own database and runs independently.\n\n## Benefits\n- Independent scaling\n- Technology flexibility\n- Fault isolation', 
'getting-started-with-spring-boot-microservices', 
2, 
'2025-02-20 12:00:00', 
'2025-02-18 10:00:00', 
'2025-02-19 14:00:00', 
true, 
189),

(3, 'Building Secure APIs with JWT', 
'# JWT Authentication\n\nSecurity is crucial for any web application. In this post, we discuss how we implement JWT authentication.\n\n## What is JWT?\n\nJSON Web Token (JWT) is a compact, URL-safe means of representing claims to be transferred between two parties.\n\n## Implementation\n\n```java\npublic String generateToken(User user) {\n    return Jwts.builder()\n        .setSubject(user.getEmail())\n        .signWith(key)\n        .compact();\n}\n```\n\n## Best Practices\n- Use strong secret keys\n- Set appropriate expiration times\n- Implement refresh tokens', 
'building-secure-apis-with-jwt', 
2, 
'2025-03-10 14:00:00', 
'2025-03-08 11:00:00', 
'2025-03-09 16:00:00', 
true, 
312),

(4, 'Docker and Microservices Deployment', 
'# Deploying with Docker\n\nDocker makes it easy to deploy microservices. Here is how we do it.\n\n## Docker Compose\n\nWe use Docker Compose to orchestrate our services:\n\n```yaml\nservices:\n  admin-service:\n    build: ./admin-service\n    ports:\n      - "8081:8081"\n```\n\n## Benefits\n- Consistent environments\n- Easy scaling\n- Simple deployment', 
'docker-and-microservices-deployment', 
1, 
'2025-04-05 10:00:00', 
'2025-04-02 09:00:00', 
'2025-04-04 15:00:00', 
true, 
178),

-- Draft posts (unpublished)
(5, 'DRAFT: Advanced Database Optimization', 
'# Database Optimization\n\nThis is a draft post about database optimization techniques.\n\n## Indexing Strategies\n\n(TODO: Add more content)\n\n## Query Optimization\n\n(TODO: Complete this section)', 
'draft-advanced-database-optimization', 
2, 
NULL, 
'2025-05-15 10:00:00', 
'2025-05-20 14:00:00', 
false, 
0),

(6, 'DRAFT: React Frontend Integration', 
'# Frontend Integration\n\nPlanning to write about integrating React with our Spring Boot backend.\n\n(This is a work in progress)', 
'draft-react-frontend-integration', 
3, 
NULL, 
'2025-06-10 11:00:00', 
'2025-06-10 11:00:00', 
false, 
0);

-- 4.5 IMAGE_REFERENCES (FIXED: added created_date column)
INSERT INTO image_references (image_reference_id, post_id, image_id, caption, display_order, created_date) VALUES
-- Post 1: Welcome post - 2 images
(1, 1, 1, 'Perfect8 Logo', 0, '2025-01-10 09:00:00'),
(2, 1, 2, 'Microservices Architecture Diagram', 1, '2025-01-10 09:00:00'),

-- Post 2: Spring Boot post - 1 image
(3, 2, 3, 'Spring Boot Architecture', 0, '2025-02-18 10:00:00'),

-- Post 3: JWT post - 1 image
(4, 3, 4, 'JWT Flow Diagram', 0, '2025-03-08 11:00:00'),

-- Post 4: Docker post - 2 images
(5, 4, 5, 'Docker Compose Setup', 0, '2025-04-02 09:00:00'),
(6, 4, 6, 'Container Network Diagram', 1, '2025-04-02 09:00:00');

-- Reset auto-increment
ALTER TABLE users AUTO_INCREMENT = 5;
ALTER TABLE roles AUTO_INCREMENT = 3;
ALTER TABLE posts AUTO_INCREMENT = 7;
ALTER TABLE image_references AUTO_INCREMENT = 7;

-- =====================================================
-- 5. VERIFY DATA
-- =====================================================

SELECT '=== DATABASE CREATED ===' as '';
SELECT DATABASE() as current_database;

SELECT '=== TABLES ===' as '';
SHOW TABLES;

SELECT '=== USERS ===' as '';
SELECT 
    user_id,
    username,
    email,
    CONCAT(first_name, ' ', last_name) as name,
    created_date
FROM users 
ORDER BY user_id;

SELECT '=== ROLES ===' as '';
SELECT role_id, name FROM roles ORDER BY role_id;

SELECT '=== USER_ROLES ===' as '';
SELECT 
    u.username,
    GROUP_CONCAT(r.name ORDER BY r.name SEPARATOR ', ') as roles
FROM users u
JOIN user_roles ur ON u.user_id = ur.user_id
JOIN roles r ON ur.role_id = r.role_id
GROUP BY u.user_id, u.username
ORDER BY u.user_id;

SELECT '=== POSTS ===' as '';
SELECT 
    p.post_id,
    p.title,
    p.slug,
    u.username as author,
    p.is_published,
    p.view_count,
    p.published_date
FROM posts p
JOIN users u ON p.author_id = u.user_id
ORDER BY p.post_id;

SELECT '=== IMAGE_REFERENCES ===' as '';
SELECT 
    ir.image_reference_id,
    p.title as post_title,
    ir.image_id,
    ir.caption,
    ir.display_order,
    ir.created_date
FROM image_references ir
JOIN posts p ON ir.post_id = p.post_id
ORDER BY ir.post_id, ir.display_order;

SELECT '=== SETUP COMPLETE ===' as '';
SELECT 
    CONCAT('Total users: ', (SELECT COUNT(*) FROM users)) as users,
    CONCAT('Total posts: ', (SELECT COUNT(*) FROM posts)) as posts,
    CONCAT('Published: ', (SELECT COUNT(*) FROM posts WHERE is_published = true)) as published,
    CONCAT('Drafts: ', (SELECT COUNT(*) FROM posts WHERE is_published = false)) as drafts;