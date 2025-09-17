-- =============================================
-- Blog Service Database Initialization
-- =============================================

-- Create blog database if not exists
CREATE DATABASE IF NOT EXISTS blogdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blogdb;

-- Create roles table med enum_name för Java mapping
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    enum_name VARCHAR(50) UNIQUE NOT NULL COMMENT 'Maps to Java Role enum',
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_roles junction table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create posts table
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    slug VARCHAR(255) UNIQUE,
    excerpt TEXT,
    published BOOLEAN DEFAULT FALSE,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    FOREIGN KEY (author_id) REFERENCES users(id),
    INDEX idx_slug (slug),
    INDEX idx_published (published),
    INDEX idx_author (author_id),
    INDEX idx_published_at (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create image_references table
CREATE TABLE IF NOT EXISTS image_references (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_id VARCHAR(255) NOT NULL,
    image_url VARCHAR(500),
    alt_text VARCHAR(255),
    caption TEXT,
    post_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_image_id (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create post_links table
CREATE TABLE IF NOT EXISTS post_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Insert initial data
-- =============================================

-- Insert default roles med enum mapping
-- VIKTIGT: enum_name måste matcha exakt med Java Role enum värden
INSERT INTO roles (name, enum_name, description) VALUES
    ('ADMIN', 'ROLE_ADMIN', 'Full system access'),
    ('WRITER', 'ROLE_WRITER', 'Can create and edit posts'),
    ('READER', 'ROLE_READER', 'Can read published posts')
ON DUPLICATE KEY UPDATE
    enum_name = VALUES(enum_name),
    description = VALUES(description);

-- Insert default admin user (password: admin123)
-- Note: Password is BCrypt encoded for 'admin123'
INSERT INTO users (username, email, password) VALUES
    ('admin', 'admin@perfect8.com', '$2a$10$N.Q.EqLBD8CZ8jXxPc3eNOYZ4JtUbC8Y8KXzCPBgEnSGgV0jLxXmC')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Insert sample users
INSERT INTO users (username, email, password) VALUES
    ('writer1', 'writer1@perfect8.com', '$2a$10$N.Q.EqLBD8CZ8jXxPc3eNOYZ4JtUbC8Y8KXzCPBgEnSGgV0jLxXmC'),
    ('writer2', 'writer2@perfect8.com', '$2a$10$N.Q.EqLBD8CZ8jXxPc3eNOYZ4JtUbC8Y8KXzCPBgEnSGgV0jLxXmC'),
    ('reader1', 'reader1@perfect8.com', '$2a$10$N.Q.EqLBD8CZ8jXxPc3eNOYZ4JtUbC8Y8KXzCPBgEnSGgV0jLxXmC'),
    ('reader2', 'reader2@perfect8.com', '$2a$10$N.Q.EqLBD8CZ8jXxPc3eNOYZ4JtUbC8Y8KXzCPBgEnSGgV0jLxXmC')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Assign roles to users
-- Admin gets all roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- Writers get WRITER and READER roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username IN ('writer1', 'writer2') AND r.name IN ('WRITER', 'READER')
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- Readers get only READER role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username IN ('reader1', 'reader2') AND r.name = 'READER'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- Insert sample posts
INSERT INTO posts (title, content, slug, excerpt, published, author_id, published_at) VALUES
(
    'Welcome to Perfect8 Blog',
    '# Welcome to Perfect8 Blog\n\nThis is our first blog post built with Spring Boot microservices architecture.\n\n## Features\n\n- **Microservice Architecture**: Separate services for blog and images\n- **JWT Authentication**: Secure API endpoints\n- **Role-based Access**: Admin, Writer, and Reader roles\n- **Markdown Support**: Write posts in Markdown\n- **Image Management**: Upload and manage images separately\n\n## Technology Stack\n\n- Spring Boot 3.2\n- MySQL 8.0\n- Docker & Docker Compose\n- JWT for authentication\n\nStay tuned for more posts!',
    'welcome-to-perfect8-blog',
    'Welcome to our new blog platform built with Spring Boot microservices.',
    TRUE,
    (SELECT id FROM users WHERE username = 'admin'),
    CURRENT_TIMESTAMP
),
(
    'Getting Started with Spring Boot',
    '# Getting Started with Spring Boot\n\nSpring Boot makes it easy to create stand-alone, production-grade Spring based Applications.\n\n## Why Spring Boot?\n\n1. **Auto-configuration**: Automatically configures your Spring application\n2. **Standalone**: Creates standalone applications\n3. **Production-ready**: Includes embedded server\n\n## Example Code\n\n```java\n@SpringBootApplication\npublic class Application {\n    public static void main(String[] args) {\n        SpringApplication.run(Application.class, args);\n    }\n}\n```\n\nHappy coding!',
    'getting-started-with-spring-boot',
    'Learn the basics of Spring Boot and why it\'s great for microservices.',
    TRUE,
    (SELECT id FROM users WHERE username = 'writer1'),
    CURRENT_TIMESTAMP
),
(
    'Understanding Microservices Architecture',
    '# Understanding Microservices Architecture\n\nMicroservices are a software development technique—a variant of the service-oriented architecture (SOA) architectural style.\n\n## Benefits\n\n- **Independence**: Services can be deployed independently\n- **Technology Diversity**: Use different technologies for different services\n- **Fault Isolation**: If one service fails, others continue to work\n- **Easy Scaling**: Scale individual services as needed\n\n## Our Architecture\n\nWe have separated our application into:\n- Blog Service: Handles posts, users, and authentication\n- Image Service: Manages image storage and retrieval\n\nThis separation allows us to scale and maintain each service independently.',
    'understanding-microservices-architecture',
    'Deep dive into microservices architecture and its benefits.',
    TRUE,
    (SELECT id FROM users WHERE username = 'writer2'),
    CURRENT_TIMESTAMP
),
(
    'Draft Post - Work in Progress',
    '# Draft Post\n\nThis is a draft post that is not yet published.\n\n## TODO\n- Add more content\n- Review and edit\n- Add images\n- Publish when ready',
    'draft-post-work-in-progress',
    'This post is still being written...',
    FALSE,
    (SELECT id FROM users WHERE username = 'writer1'),
    NULL
);

-- Insert sample links for posts
INSERT INTO post_links (post_id, url) VALUES
    ((SELECT id FROM posts WHERE slug = 'welcome-to-perfect8-blog'), 'https://spring.io/projects/spring-boot'),
    ((SELECT id FROM posts WHERE slug = 'welcome-to-perfect8-blog'), 'https://www.docker.com/'),
    ((SELECT id FROM posts WHERE slug = 'getting-started-with-spring-boot'), 'https://spring.io/guides/gs/spring-boot/'),
    ((SELECT id FROM posts WHERE slug = 'getting-started-with-spring-boot'), 'https://docs.spring.io/spring-boot/docs/current/reference/html/'),
    ((SELECT id FROM posts WHERE slug = 'understanding-microservices-architecture'), 'https://microservices.io/'),
    ((SELECT id FROM posts WHERE slug = 'understanding-microservices-architecture'), 'https://martinfowler.com/microservices/');

-- =============================================
-- Image Service Database Initialization
-- =============================================

-- Create image database if not exists
CREATE DATABASE IF NOT EXISTS imagedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE imagedb;

-- Create images table
CREATE TABLE IF NOT EXISTS images (
    id VARCHAR(36) PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    data LONGBLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Create stored procedures for maintenance
-- =============================================

USE blogdb;

DELIMITER //

-- Procedure to clean up old unpublished posts
CREATE PROCEDURE IF NOT EXISTS cleanup_old_drafts()
BEGIN
    DELETE FROM posts
    WHERE published = FALSE
    AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
END//

-- Procedure to get post statistics
CREATE PROCEDURE IF NOT EXISTS get_post_statistics()
BEGIN
    SELECT
        COUNT(*) as total_posts,
        SUM(CASE WHEN published = TRUE THEN 1 ELSE 0 END) as published_posts,
        SUM(CASE WHEN published = FALSE THEN 1 ELSE 0 END) as draft_posts,
        COUNT(DISTINCT author_id) as total_authors
    FROM posts;
END//

DELIMITER ;

-- =============================================
-- Create views for reporting
-- =============================================

-- View for post summary with author info
CREATE OR REPLACE VIEW post_summary AS
SELECT
    p.id,
    p.title,
    p.slug,
    p.excerpt,
    p.published,
    p.created_at,
    p.published_at,
    u.username as author_username,
    u.email as author_email,
    (SELECT COUNT(*) FROM image_references WHERE post_id = p.id) as image_count,
    (SELECT COUNT(*) FROM post_links WHERE post_id = p.id) as link_count
FROM posts p
JOIN users u ON p.author_id = u.id;

-- View for user activity
CREATE OR REPLACE VIEW user_activity AS
SELECT
    u.id,
    u.username,
    u.email,
    u.created_at as user_since,
    COUNT(DISTINCT p.id) as total_posts,
    SUM(CASE WHEN p.published = TRUE THEN 1 ELSE 0 END) as published_posts,
    MAX(p.created_at) as last_post_date
FROM users u
LEFT JOIN posts p ON u.id = p.author_id
GROUP BY u.id, u.username, u.email, u.created_at;

-- =============================================
-- Grant permissions (adjust as needed)
-- =============================================

-- Example: Grant permissions to blog application user
-- GRANT ALL PRIVILEGES ON blogdb.* TO 'bloguser'@'%';
-- GRANT ALL PRIVILEGES ON imagedb.* TO 'imageuser'@'%';
-- FLUSH PRIVILEGES;