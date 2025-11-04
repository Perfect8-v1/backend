-- Blog Service - H2 Test Data
-- This file is loaded automatically when using the h2 profile

-- Roles
INSERT INTO roles (id, name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN'),
(3, 'ROLE_AUTHOR');

-- Users (password: password123)
INSERT INTO users (id, username, email, password, first_name, last_name, created_at, updated_at) VALUES
(1, 'john_author', 'john@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'jane_admin', 'jane@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- john is USER
(1, 3), -- john is AUTHOR
(2, 1), -- jane is USER
(2, 2), -- jane is ADMIN
(2, 3); -- jane is AUTHOR

-- Blog Posts
INSERT INTO posts (id, title, slug, content, excerpt, author_id, published, featured, created_at, updated_at) VALUES
(1, 'Welcome to Perfect8 Blog', 'welcome-to-perfect8-blog',
 'This is our first blog post! We are excited to share our journey with you.',
 'Our first blog post welcoming you to Perfect8',
 1, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Getting Started with Flutter', 'getting-started-with-flutter',
 'Flutter is an amazing framework for building cross-platform applications. Here is how to get started...',
 'Learn the basics of Flutter development',
 1, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Spring Boot Best Practices', 'spring-boot-best-practices',
 'After years of working with Spring Boot, here are the best practices we have learned...',
 'Essential Spring Boot tips and tricks',
 2, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Draft Post - Work in Progress', 'draft-post-wip',
 'This is a draft post that is not yet published...',
 'A draft post for testing',
 1, false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Note: All passwords are 'password123' hashed with BCrypt
-- Login with: username='john_author', password='password123'
-- Or: username='jane_admin', password='password123'