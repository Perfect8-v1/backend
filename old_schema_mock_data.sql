-- ============================================
-- Perfect8 Shop/Blog Mock Data - GAMLA SCHEMAT
-- Matchar befintlig databas-struktur exakt
-- ============================================

USE perfect8_shop;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. CATEGORIES (gamla schemat)
-- ============================================
INSERT INTO categories (category_id, name, slug, description, parent_id, sort_order, is_active, image_url, meta_title, meta_description, meta_keywords, created_at, updated_at, created_date, updated_date) VALUES
(1, 'Electronics', 'electronics', 'Electronic devices and accessories', NULL, 1, 1, NULL, 'Electronics', 'Shop electronics', 'electronics, gadgets', NOW(), NOW(), NOW(), NOW()),
(2, 'Laptops', 'laptops', 'Portable computers', 1, 1, 1, NULL, 'Laptops', 'Buy laptops', 'laptops, computers', NOW(), NOW(), NOW(), NOW()),
(3, 'Smartphones', 'smartphones', 'Mobile phones', 1, 2, 1, NULL, 'Smartphones', 'Buy smartphones', 'phones, mobile', NOW(), NOW(), NOW(), NOW()),
(4, 'Accessories', 'accessories', 'Electronic accessories', 1, 3, 1, NULL, 'Accessories', 'Tech accessories', 'mouse, keyboard', NOW(), NOW(), NOW(), NOW()),
(5, 'Clothing', 'clothing', 'Fashion and apparel', NULL, 2, 1, NULL, 'Clothing', 'Fashion clothes', 'clothing, fashion', NOW(), NOW(), NOW(), NOW()),
(6, 'Men', 'men', 'Mens clothing', 5, 1, 1, NULL, 'Mens Clothing', 'Shop mens wear', 'mens, fashion', NOW(), NOW(), NOW(), NOW()),
(7, 'Women', 'women', 'Womens clothing', 5, 2, 1, NULL, 'Womens Clothing', 'Shop womens wear', 'womens, fashion', NOW(), NOW(), NOW(), NOW()),
(8, 'Home & Garden', 'home-garden', 'Home and garden', NULL, 3, 1, NULL, 'Home Garden', 'Home products', 'home, garden', NOW(), NOW(), NOW(), NOW()),
(9, 'Books', 'books', 'Books and literature', NULL, 4, 1, NULL, 'Books', 'Buy books', 'books, reading', NOW(), NOW(), NOW(), NOW()),
(10, 'Sports', 'sports', 'Sports equipment', NULL, 5, 1, NULL, 'Sports', 'Sports gear', 'sports, fitness', NOW(), NOW(), NOW(), NOW());

-- ============================================
-- 2. PRODUCTS (gamla schemat)
-- ============================================
INSERT INTO products (product_id, sku, name, description, price, discount_price, stock_quantity, category_id, image_url, is_active, is_featured, dimensions, weight, rating, review_count, sales_count, views, reorder_point, reorder_quantity, meta_title, meta_description, meta_keywords, created_at, updated_at, created_date, updated_date) VALUES
-- Electronics > Laptops
(1, 'LAPTOP-001', 'MacBook Pro 16"', 'Professional laptop with M3 chip, 16GB RAM, 512GB SSD', 2499.00, NULL, 15, 2, NULL, 1, 1, '35.7 x 24.8 x 1.6 cm', 2.15, 4.8, 124, 45, 1250, 5, 10, 'MacBook Pro 16"', 'Buy MacBook Pro', 'macbook, laptop, apple', NOW(), NOW(), NOW(), NOW()),
(2, 'LAPTOP-002', 'Dell XPS 15', 'High-performance laptop, Intel i7, 16GB RAM, 1TB SSD', 1899.00, 1799.00, 20, 2, NULL, 1, 1, '34.4 x 23.0 x 1.8 cm', 1.96, 4.6, 89, 67, 980, 5, 10, 'Dell XPS 15', 'Buy Dell XPS', 'dell, laptop, xps', NOW(), NOW(), NOW(), NOW()),
(3, 'LAPTOP-003', 'ThinkPad X1 Carbon', 'Business ultrabook, Intel i5, 8GB RAM, 256GB SSD', 1299.00, NULL, 25, 2, NULL, 1, 0, '32.3 x 21.7 x 1.5 cm', 1.13, 4.5, 67, 123, 750, 5, 10, 'ThinkPad X1', 'Buy ThinkPad', 'lenovo, thinkpad, laptop', NOW(), NOW(), NOW(), NOW()),

-- Electronics > Smartphones
(4, 'PHONE-001', 'iPhone 15 Pro', 'Latest iPhone with A17 chip, 256GB storage', 1199.00, NULL, 30, 3, NULL, 1, 1, '14.6 x 7.1 x 0.8 cm', 0.187, 4.7, 234, 189, 2340, 10, 20, 'iPhone 15 Pro', 'Buy iPhone 15', 'iphone, apple, smartphone', NOW(), NOW(), NOW(), NOW()),
(5, 'PHONE-002', 'Samsung Galaxy S24', 'Flagship Android phone, 128GB storage', 899.00, NULL, 40, 3, NULL, 1, 1, '14.7 x 7.1 x 0.8 cm', 0.168, 4.5, 178, 234, 1890, 10, 20, 'Galaxy S24', 'Buy Samsung Galaxy', 'samsung, android, phone', NOW(), NOW(), NOW(), NOW()),
(6, 'PHONE-003', 'Google Pixel 8', 'Pure Android experience, 128GB storage', 699.00, 649.00, 35, 3, NULL, 1, 0, '15.0 x 7.1 x 0.9 cm', 0.197, 4.4, 145, 156, 1230, 10, 20, 'Pixel 8', 'Buy Google Pixel', 'pixel, google, android', NOW(), NOW(), NOW(), NOW()),

-- Electronics > Accessories
(7, 'ACC-001', 'Wireless Mouse', 'Logitech MX Master 3 wireless mouse', 99.00, NULL, 100, 4, NULL, 1, 0, '12.4 x 8.4 x 5.1 cm', 0.141, 4.6, 456, 789, 3450, 20, 50, 'Logitech Mouse', 'Buy wireless mouse', 'mouse, logitech, wireless', NOW(), NOW(), NOW(), NOW()),
(8, 'ACC-002', 'Mechanical Keyboard', 'RGB mechanical gaming keyboard', 149.00, NULL, 75, 4, NULL, 1, 0, '43.5 x 13.5 x 3.6 cm', 1.02, 4.5, 289, 456, 2340, 15, 30, 'Gaming Keyboard', 'Buy mechanical keyboard', 'keyboard, gaming, rgb', NOW(), NOW(), NOW(), NOW()),
(9, 'ACC-003', 'USB-C Hub', '7-in-1 USB-C hub with HDMI and card readers', 49.00, NULL, 150, 4, NULL, 1, 0, '11.2 x 4.5 x 1.3 cm', 0.084, 4.3, 234, 567, 1890, 30, 50, 'USB-C Hub', 'Buy USB hub', 'usb, hub, adapter', NOW(), NOW(), NOW(), NOW()),
(10, 'ACC-004', 'Wireless Headphones', 'Sony WH-1000XM5 noise cancelling', 399.00, 379.00, 50, 4, NULL, 1, 1, '22.0 x 18.5 x 8.0 cm', 0.250, 4.8, 678, 345, 4560, 10, 20, 'Sony Headphones', 'Buy Sony headphones', 'headphones, sony, wireless', NOW(), NOW(), NOW(), NOW()),

-- Clothing
(11, 'CLOTH-M-001', 'Cotton T-Shirt', 'Premium cotton t-shirt, available in multiple colors', 29.00, NULL, 200, 6, NULL, 1, 0, 'M: 72 x 52 cm', 0.15, 4.2, 345, 890, 5670, 50, 100, 'Cotton T-Shirt', 'Buy t-shirt', 'tshirt, cotton, mens', NOW(), NOW(), NOW(), NOW()),
(12, 'CLOTH-M-002', 'Denim Jeans', 'Classic fit denim jeans', 79.00, 69.00, 150, 6, NULL, 1, 0, 'W32 L32', 0.65, 4.3, 234, 456, 3450, 30, 60, 'Denim Jeans', 'Buy jeans', 'jeans, denim, mens', NOW(), NOW(), NOW(), NOW()),
(13, 'CLOTH-M-003', 'Hoodie', 'Comfortable cotton blend hoodie', 59.00, NULL, 100, 6, NULL, 1, 0, 'L: 74 x 60 cm', 0.48, 4.4, 189, 345, 2340, 20, 40, 'Cotton Hoodie', 'Buy hoodie', 'hoodie, cotton, mens', NOW(), NOW(), NOW(), NOW()),
(14, 'CLOTH-W-001', 'Summer Dress', 'Floral pattern summer dress', 69.00, NULL, 80, 7, NULL, 1, 0, 'M: 95 cm length', 0.25, 4.5, 156, 234, 1890, 15, 30, 'Summer Dress', 'Buy dress', 'dress, summer, womens', NOW(), NOW(), NOW(), NOW()),
(15, 'CLOTH-W-002', 'Yoga Pants', 'High-waist athletic yoga pants', 49.00, NULL, 120, 7, NULL, 1, 0, 'M: W28 L32', 0.22, 4.6, 278, 456, 3210, 25, 50, 'Yoga Pants', 'Buy yoga pants', 'yoga, pants, womens', NOW(), NOW(), NOW(), NOW()),

-- Books
(16, 'BOOK-001', 'Clean Code', 'Robert C. Martin - Software engineering classic', 45.00, NULL, 50, 9, NULL, 1, 0, '23.4 x 15.6 x 2.8 cm', 0.68, 4.7, 456, 234, 2340, 10, 20, 'Clean Code Book', 'Buy Clean Code', 'book, programming, clean code', NOW(), NOW(), NOW(), NOW()),
(17, 'BOOK-002', 'Design Patterns', 'Gang of Four design patterns book', 55.00, NULL, 40, 9, NULL, 1, 0, '23.5 x 19.1 x 3.3 cm', 0.79, 4.6, 345, 189, 1890, 10, 20, 'Design Patterns', 'Buy Design Patterns', 'book, programming, patterns', NOW(), NOW(), NOW(), NOW()),

-- Sports
(18, 'SPORT-001', 'Yoga Mat', 'Non-slip exercise yoga mat', 39.00, NULL, 80, 10, NULL, 1, 0, '183 x 61 x 0.6 cm', 1.2, 4.4, 234, 345, 2340, 15, 30, 'Yoga Mat', 'Buy yoga mat', 'yoga, mat, exercise', NOW(), NOW(), NOW(), NOW()),
(19, 'SPORT-002', 'Dumbbell Set', '20kg adjustable dumbbell set', 89.00, NULL, 30, 10, NULL, 1, 0, '40 x 20 x 20 cm', 20.0, 4.5, 123, 156, 1230, 5, 10, 'Dumbbell Set', 'Buy dumbbells', 'dumbbell, weights, fitness', NOW(), NOW(), NOW(), NOW()),
(20, 'SPORT-003', 'Running Shoes', 'Professional running shoes for athletes', 129.00, 119.00, 60, 10, NULL, 1, 1, 'US 10', 0.35, 4.6, 345, 267, 3450, 15, 30, 'Running Shoes', 'Buy running shoes', 'shoes, running, sports', NOW(), NOW(), NOW(), NOW());

-- ============================================
-- 3. CUSTOMERS (gamla schemat)
-- Password: "password123" (BCrypt hash)
-- ============================================
INSERT INTO customers (customer_id, email, password_hash, first_name, last_name, phone, active, email_verified, email_verification_token, email_verification_sent_at, email_verified_at, reset_password_token, reset_password_token_expiry, failed_login_attempts, account_locked_until, role, marketing_consent, newsletter_subscribed, preferred_language, preferred_currency, created_date, updated_date, last_login_date) VALUES
(1, 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', '+46701234567', 1, 1, NULL, NULL, NOW(), NULL, NULL, 0, NULL, 'CUSTOMER', 1, 1, 'en', 'SEK', NOW(), NOW(), NOW()),
(2, 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', '+46702345678', 1, 1, NULL, NULL, NOW(), NULL, NULL, 0, NULL, 'CUSTOMER', 1, 1, 'sv', 'SEK', NOW(), NOW(), NOW()),
(3, 'bob.johnson@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'Johnson', '+46703456789', 1, 1, NULL, NULL, NOW(), NULL, NULL, 0, NULL, 'CUSTOMER', 0, 0, 'en', 'SEK', NOW(), NOW(), NULL),
(4, 'alice.williams@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice', 'Williams', '+46704567890', 1, 0, 'verify_token_123', NOW(), NULL, NULL, NULL, 0, NULL, 'CUSTOMER', 1, 0, 'en', 'SEK', NOW(), NOW(), NULL),
(5, 'test.user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test', 'User', '+46705678901', 1, 1, NULL, NULL, NOW(), NULL, NULL, 0, NULL, 'CUSTOMER', 1, 1, 'en', 'SEK', NOW(), NOW(), NOW());

-- ============================================
-- 4. USERS (för blog - gamla schemat)
-- Password: "password123" (BCrypt hash)
-- ============================================
INSERT INTO users (user_id, username, email, password, first_name, last_name, bio, avatar_url, email_verified, created_at, updated_at) VALUES
(1, 'admin', 'admin@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', 'System administrator', NULL, 1, NOW(), NOW()),
(2, 'magnus', 'magnus@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Magnus', 'Berglund', 'Lead developer with ADHD-friendly practices', NULL, 1, NOW(), NOW()),
(3, 'jonatan', 'jonatan@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jonatan', 'Developer', 'Frontend developer specializing in Flutter', NULL, 1, NOW(), NOW()),
(4, 'jane_author', 'jane@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Writer', 'Technical writer and content creator', NULL, 1, NOW(), NOW()),
(5, 'john_reader', 'john@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Reader', 'Interested in software development', NULL, 1, NOW(), NOW());

-- ============================================
-- 5. ROLES (för blog)
-- ============================================
INSERT INTO roles (role_id, name, description) VALUES
(1, 'ROLE_USER', 'Regular user with read access'),
(2, 'ROLE_ADMIN', 'Administrator with full access'),
(3, 'ROLE_AUTHOR', 'Can create and edit posts');

-- ============================================
-- 6. USER_ROLES (Many-to-Many)
-- ============================================
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), (1, 2), (1, 3),  -- Admin: all roles
(2, 1), (2, 2), (2, 3),  -- Magnus: all roles
(3, 1), (3, 3),          -- Jonatan: user + author
(4, 1), (4, 3),          -- Jane: user + author
(5, 1);                  -- John: user only

-- ============================================
-- 7. POSTS (gamla schemat med 'id' och 'published')
-- ============================================
INSERT INTO posts (id, title, content, excerpt, slug, author_id, published, published_at, created_at, updated_at) VALUES
(1, 'Welcome to Perfect8 Blog', 
'<h1>Welcome!</h1><p>This is our first blog post. We are excited to share our journey in building Perfect8, a modern e-commerce platform with microservices architecture.</p><p>Perfect8 consists of 5 microservices: Admin, Blog, Email, Image, and Shop services. Each service is designed to be independent and scalable.</p>', 
'Our first blog post about building Perfect8',
'welcome-to-perfect8-blog', 
1, 
1,
DATE_SUB(NOW(), INTERVAL 30 DAY),
DATE_SUB(NOW(), INTERVAL 35 DAY),
DATE_SUB(NOW(), INTERVAL 30 DAY)),

(2, 'Building Microservices with Spring Boot', 
'<h1>Microservices Architecture</h1><p>In this post, we explore the benefits of microservices architecture and how Spring Boot makes it easy to build distributed systems.</p><h2>Key Benefits:</h2><ul><li>Independent deployment</li><li>Technology flexibility</li><li>Fault isolation</li><li>Scalability</li></ul>', 
'Exploring microservices benefits',
'building-microservices-with-spring-boot', 
2, 
1,
DATE_SUB(NOW(), INTERVAL 25 DAY),
DATE_SUB(NOW(), INTERVAL 28 DAY),
DATE_SUB(NOW(), INTERVAL 25 DAY)),

(3, 'ADHD-Friendly Development', 
'<h1>Coding with ADHD</h1><p>As developers with ADHD, we have learned valuable lessons about creating sustainable development practices.</p><h2>Our Principles:</h2><ul><li>One file at a time</li><li>Max 2 choices</li><li>Clear naming</li><li>Documentation</li></ul>', 
'Development practices for ADHD',
'adhd-friendly-development', 
2, 
1,
DATE_SUB(NOW(), INTERVAL 20 DAY),
DATE_SUB(NOW(), INTERVAL 22 DAY),
DATE_SUB(NOW(), INTERVAL 20 DAY)),

(4, 'JWT Authentication', 
'<h1>Securing APIs with JWT</h1><p>JSON Web Tokens provide stateless authentication perfect for microservices.</p>', 
'JWT authentication guide',
'jwt-authentication', 
3, 
1,
DATE_SUB(NOW(), INTERVAL 15 DAY),
DATE_SUB(NOW(), INTERVAL 18 DAY),
DATE_SUB(NOW(), INTERVAL 15 DAY)),

(5, 'Image Processing', 
'<h1>Efficient Image Handling</h1><p>Our image-service uses Thumbnailator for fast image processing.</p>', 
'Image processing with Thumbnailator',
'image-processing', 
4, 
1,
DATE_SUB(NOW(), INTERVAL 10 DAY),
DATE_SUB(NOW(), INTERVAL 12 DAY),
DATE_SUB(NOW(), INTERVAL 10 DAY)),

(6, 'Email Templates', 
'<h1>HTML Email Templates</h1><p>Beautiful transactional emails with Spring Boot.</p>', 
'Email template guide',
'email-templates', 
4, 
1,
DATE_SUB(NOW(), INTERVAL 7 DAY),
DATE_SUB(NOW(), INTERVAL 9 DAY),
DATE_SUB(NOW(), INTERVAL 7 DAY)),

(7, 'PayPal Integration', 
'<h1>Payment Processing</h1><p>Version 1.0 supports PayPal payments.</p>', 
'PayPal integration guide',
'paypal-integration', 
3, 
1,
DATE_SUB(NOW(), INTERVAL 5 DAY),
DATE_SUB(NOW(), INTERVAL 6 DAY),
DATE_SUB(NOW(), INTERVAL 5 DAY)),

(8, 'Database Design', 
'<h1>Designing Scalable Schemas</h1><p>Each microservice has its own database.</p>', 
'Database schema principles',
'database-design', 
2, 
1,
DATE_SUB(NOW(), INTERVAL 3 DAY),
DATE_SUB(NOW(), INTERVAL 4 DAY),
DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Draft posts
(9, 'Redis Caching', 
'<h1>Advanced Caching</h1><p>Draft about Redis caching.</p>', 
'Caching strategies',
'redis-caching', 
2, 
0,
NULL,
DATE_SUB(NOW(), INTERVAL 2 DAY),
NOW()),

(10, 'Monitoring', 
'<h1>Prometheus Monitoring</h1><p>Draft about monitoring.</p>', 
'System monitoring',
'monitoring', 
3, 
0,
NULL,
DATE_SUB(NOW(), INTERVAL 1 DAY),
NOW()),

(11, 'Version 2.0 Roadmap', 
'<h1>v2.0 Features</h1><p>Exciting features coming in v2.0.</p>', 
'What is coming in v2.0',
'version-2-roadmap', 
1, 
0,
NULL,
NOW(),
NOW()),

(12, 'Flutter Frontend', 
'<h1>Multi-Platform Apps</h1><p>Building with Flutter/Dart.</p>', 
'Frontend with Flutter',
'flutter-frontend', 
3, 
1,
NOW(),
DATE_SUB(NOW(), INTERVAL 1 DAY),
NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- VERIFICATION
-- ============================================
-- SELECT COUNT(*) FROM categories;
-- SELECT COUNT(*) FROM products;
-- SELECT COUNT(*) FROM customers;
-- SELECT COUNT(*) FROM users;
-- SELECT COUNT(*) FROM posts WHERE published = 1;
-- SELECT COUNT(*) FROM posts WHERE published = 0;

-- ============================================
-- TEST CREDENTIALS
-- ============================================
-- Customers (shop):
-- john.doe@example.com / password123
-- test.user@example.com / password123
--
-- Blog authors:
-- admin@perfect8.com / password123
-- magnus@perfect8.com / password123
-- jonatan@perfect8.com / password123
-- ============================================
