-- ============================================
-- Perfect8 Blog Service - Mock Data v1.0
-- Baserad på Feature_Map_v1.0.md
-- Använder *Date suffix (INTE *At)
-- ============================================

USE perfect8_blog;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data (optional - comment out if you want to keep existing data)
-- TRUNCATE TABLE image_references;
-- TRUNCATE TABLE posts;
-- TRUNCATE TABLE user_roles;
-- TRUNCATE TABLE users;
-- TRUNCATE TABLE roles;

-- ============================================
-- 1. ROLES
-- ============================================
INSERT INTO roles (role_id, name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN'),
(3, 'ROLE_AUTHOR');

-- ============================================
-- 2. USERS
-- Password: "password123" (BCrypt hash)
-- ============================================
INSERT INTO users (user_id, username, email, password_hash, first_name, last_name, created_date, updated_date) VALUES
(1, 'admin', 'admin@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', NOW(), NOW()),
(2, 'magnus', 'magnus@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Magnus', 'Berglund', NOW(), NOW()),
(3, 'jonatan', 'jonatan@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jonatan', 'Developer', NOW(), NOW()),
(4, 'jane_author', 'jane@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Writer', NOW(), NOW()),
(5, 'john_reader', 'john@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Reader', NOW(), NOW());

-- ============================================
-- 3. USER_ROLES (Many-to-Many relationship)
-- ============================================
INSERT INTO user_roles (user_id, role_id) VALUES
-- Admin has all roles
(1, 1),  -- ROLE_USER
(1, 2),  -- ROLE_ADMIN
(1, 3),  -- ROLE_AUTHOR

-- Magnus: Admin + Author
(2, 1),  -- ROLE_USER
(2, 2),  -- ROLE_ADMIN
(2, 3),  -- ROLE_AUTHOR

-- Jonatan: Author
(3, 1),  -- ROLE_USER
(3, 3),  -- ROLE_AUTHOR

-- Jane: Author
(4, 1),  -- ROLE_USER
(4, 3),  -- ROLE_AUTHOR

-- John: Regular user (reader only)
(5, 1);  -- ROLE_USER

-- ============================================
-- 4. POSTS
-- ============================================
INSERT INTO posts (post_id, title, content, slug, author_id, published_date, created_date, updated_date, is_published, view_count) VALUES
-- Published posts
(1, 'Welcome to Perfect8 Blog', 
'<h1>Welcome!</h1><p>This is our first blog post. We are excited to share our journey in building Perfect8, a modern e-commerce platform with microservices architecture.</p><p>Perfect8 consists of 5 microservices: Admin, Blog, Email, Image, and Shop services. Each service is designed to be independent and scalable.</p><p>Stay tuned for more technical articles about Spring Boot, microservices, and software development!</p>', 
'welcome-to-perfect8-blog', 
1, 
DATE_SUB(NOW(), INTERVAL 30 DAY), 
DATE_SUB(NOW(), INTERVAL 35 DAY), 
DATE_SUB(NOW(), INTERVAL 30 DAY), 
true, 
150),

(2, 'Building Microservices with Spring Boot', 
'<h1>Microservices Architecture</h1><p>In this post, we explore the benefits of microservices architecture and how Spring Boot makes it easy to build distributed systems.</p><h2>Key Benefits:</h2><ul><li>Independent deployment</li><li>Technology flexibility</li><li>Fault isolation</li><li>Scalability</li></ul><p>We use Docker and Podman for containerization, and each service has its own database to ensure data independence.</p>', 
'building-microservices-with-spring-boot', 
2, 
DATE_SUB(NOW(), INTERVAL 25 DAY), 
DATE_SUB(NOW(), INTERVAL 28 DAY), 
DATE_SUB(NOW(), INTERVAL 25 DAY), 
true, 
230),

(3, 'ADHD-Friendly Development Practices', 
'<h1>Coding with ADHD</h1><p>As developers with ADHD, we have learned valuable lessons about creating sustainable development practices.</p><h2>Our Principles:</h2><ul><li>One file at a time - complete files, no partial edits</li><li>Max 2 choices to avoid decision paralysis</li><li>Clear naming conventions - orderId not id</li><li>Comprehensive documentation</li></ul><p>These practices are documented in our Magnum Opus, which serves as our development bible.</p>', 
'adhd-friendly-development-practices', 
2, 
DATE_SUB(NOW(), INTERVAL 20 DAY), 
DATE_SUB(NOW(), INTERVAL 22 DAY), 
DATE_SUB(NOW(), INTERVAL 20 DAY), 
true, 
180),

(4, 'JWT Authentication in Spring Boot', 
'<h1>Securing APIs with JWT</h1><p>JSON Web Tokens (JWT) provide a stateless authentication mechanism perfect for microservices.</p><h2>Implementation Steps:</h2><ol><li>Add jjwt dependencies (version 0.12.3)</li><li>Create JwtTokenProvider class</li><li>Implement JwtAuthenticationFilter</li><li>Configure SecurityConfig</li></ol><p>Our admin-service handles all authentication and issues JWT tokens that other services can validate.</p>', 
'jwt-authentication-in-spring-boot', 
3, 
DATE_SUB(NOW(), INTERVAL 15 DAY), 
DATE_SUB(NOW(), INTERVAL 18 DAY), 
DATE_SUB(NOW(), INTERVAL 15 DAY), 
true, 
310),

(5, 'Image Processing with Thumbnailator', 
'<h1>Efficient Image Handling</h1><p>Our image-service uses Thumbnailator 0.4.20 for fast and efficient image processing.</p><p>We generate 4 thumbnail sizes automatically: SMALL (150px), MEDIUM (300px), LARGE (600px), and XLARGE (1200px).</p><p>All images are stored on the filesystem with metadata in MariaDB. This hybrid approach gives us the best of both worlds.</p>', 
'image-processing-with-thumbnailator', 
4, 
DATE_SUB(NOW(), INTERVAL 10 DAY), 
DATE_SUB(NOW(), INTERVAL 12 DAY), 
DATE_SUB(NOW(), INTERVAL 10 DAY), 
true, 
95),

(6, 'Email Templates with Spring Boot', 
'<h1>HTML Email Templates</h1><p>Our email-service uses HTML templates for beautiful transactional emails.</p><h2>Templates we have:</h2><ul><li>order-confirmation.html</li><li>order-shipped.html</li><li>order-cancelled.html</li><li>welcome.html</li><li>password-reset.html</li><li>newsletter.html</li></ul><p>All templates use modern HTML/CSS and are mobile-responsive.</p>', 
'email-templates-with-spring-boot', 
4, 
DATE_SUB(NOW(), INTERVAL 7 DAY), 
DATE_SUB(NOW(), INTERVAL 9 DAY), 
DATE_SUB(NOW(), INTERVAL 7 DAY), 
true, 
125),

(7, 'PayPal Integration Guide', 
'<h1>Payment Processing</h1><p>Version 1.0 of Perfect8 supports PayPal payments. Here is how we integrated it.</p><p>PayPal SDK makes it straightforward to create orders and capture payments. We store transaction IDs for reference and refund purposes.</p><p>Version 2.0 will add Stripe support for more payment options.</p>', 
'paypal-integration-guide', 
3, 
DATE_SUB(NOW(), INTERVAL 5 DAY), 
DATE_SUB(NOW(), INTERVAL 6 DAY), 
DATE_SUB(NOW(), INTERVAL 5 DAY), 
true, 
165),

(8, 'Database Schema Design Principles', 
'<h1>Designing Scalable Schemas</h1><p>Each microservice has its own database in Perfect8. This ensures independence and prevents cascading failures.</p><h2>Our naming conventions:</h2><ul><li>Entity IDs: customerId, orderId, productId (not just "id")</li><li>Dates: createdDate, updatedDate (not "createdAt")</li><li>Booleans: isActive, isPublished (clear intent)</li></ul><p>Consistency in naming makes code more maintainable.</p>', 
'database-schema-design-principles', 
2, 
DATE_SUB(NOW(), INTERVAL 3 DAY), 
DATE_SUB(NOW(), INTERVAL 4 DAY), 
DATE_SUB(NOW(), INTERVAL 3 DAY), 
true, 
210),

-- Draft posts (not published)
(9, 'Advanced Caching Strategies', 
'<h1>Redis and Caching</h1><p>This is a draft post about implementing Redis caching in Spring Boot microservices.</p><p>TODO: Add code examples and performance metrics.</p>', 
'advanced-caching-strategies', 
2, 
NULL, 
DATE_SUB(NOW(), INTERVAL 2 DAY), 
NOW(), 
false, 
0),

(10, 'Monitoring with Prometheus', 
'<h1>Metrics and Monitoring</h1><p>Draft: How to set up Prometheus and Grafana for monitoring microservices.</p>', 
'monitoring-with-prometheus', 
3, 
NULL, 
DATE_SUB(NOW(), INTERVAL 1 DAY), 
NOW(), 
false, 
0),

(11, 'Version 2.0 Roadmap', 
'<h1>What is Coming in v2.0</h1><p>We are planning exciting features for version 2.0:</p><ul><li>Analytics dashboard</li><li>Coupon system</li><li>Product reviews</li><li>Recommendation engine</li><li>Multiple payment methods</li></ul><p>Stay tuned for updates!</p>', 
'version-2-roadmap', 
1, 
NULL, 
NOW(), 
NOW(), 
false, 
0),

-- Recent published post
(12, 'Frontend Development with Flutter', 
'<h1>Building Multi-Platform Apps</h1><p>Jonatan is building our frontend using Flutter/Dart, which allows us to create web, iOS, Android, and Windows clients from a single codebase.</p><p>The frontend connects to our backend APIs via HTTP REST calls, using JWT tokens for authentication.</p><p>This approach gives us maximum reach with minimal effort.</p>', 
'frontend-development-with-flutter', 
3, 
NOW(), 
DATE_SUB(NOW(), INTERVAL 1 DAY), 
NOW(), 
true, 
42);

-- ============================================
-- 5. IMAGE REFERENCES (Optional - for posts with images)
-- Note: imageId refers to images in image-service
-- These are examples - you will need actual imageId values from image-service
-- ============================================
INSERT INTO image_references (image_reference_id, post_id, image_id, caption, display_order) VALUES
(1, 1, 1, 'Perfect8 Logo', 1),
(2, 2, 2, 'Microservices Architecture Diagram', 1),
(3, 4, 3, 'JWT Token Flow', 1),
(4, 5, 4, 'Image Processing Pipeline', 1),
(5, 8, 5, 'Database Schema', 1);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Run these to verify data was inserted correctly:

-- SELECT COUNT(*) as role_count FROM roles;
-- SELECT COUNT(*) as user_count FROM users;
-- SELECT COUNT(*) as user_role_count FROM user_roles;
-- SELECT COUNT(*) as post_count FROM posts;
-- SELECT COUNT(*) as published_post_count FROM posts WHERE is_published = true;
-- SELECT COUNT(*) as draft_post_count FROM posts WHERE is_published = false;
-- SELECT COUNT(*) as image_ref_count FROM image_references;

-- Show all published posts with author names:
-- SELECT 
--     p.post_id, 
--     p.title, 
--     p.slug, 
--     u.username as author,
--     p.published_date,
--     p.view_count
-- FROM posts p
-- JOIN users u ON p.author_id = u.user_id
-- WHERE p.is_published = true
-- ORDER BY p.published_date DESC;

-- ============================================
-- TEST CREDENTIALS FOR JONATAN
-- ============================================
-- Admin Account:
-- Username: admin
-- Email: admin@perfect8.com
-- Password: password123
--
-- Author Accounts:
-- Username: magnus / Email: magnus@perfect8.com / Password: password123
-- Username: jonatan / Email: jonatan@perfect8.com / Password: password123
-- Username: jane_author / Email: jane@perfect8.com / Password: password123
--
-- Reader Account:
-- Username: john_reader / Email: john@perfect8.com / Password: password123
--
-- POSTS AVAILABLE:
-- - 9 published posts (visible to everyone)
-- - 3 draft posts (visible to authors/admins only)
-- ============================================
