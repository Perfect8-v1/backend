-- =====================================================
-- IMAGE-SERVICE KOMPLETT SETUP (FIXED)
-- Skapar: databas, tabeller, mock-data
-- Database: imageDB
-- FIX: All columns from Image.java
-- =====================================================

-- 1. Skapa databas
CREATE DATABASE IF NOT EXISTS imageDB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE imageDB;

-- 2. Ta bort gamla tabeller (om de finns)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS images;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 3. SKAPA TABELLER
-- =====================================================

-- 3.1 IMAGES (FIXED: all columns from Image.java)
CREATE TABLE images (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_filename VARCHAR(255),
    stored_filename VARCHAR(255) UNIQUE,
    mime_type VARCHAR(100),
    image_format VARCHAR(50),
    original_size_bytes BIGINT,
    original_width INT,
    original_height INT,
    original_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    small_url VARCHAR(500),
    medium_url VARCHAR(500),
    large_url VARCHAR(500),
    image_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    processing_time_ms BIGINT,
    error_message TEXT,
    alt_text VARCHAR(500),
    title VARCHAR(255),
    category VARCHAR(100),
    reference_type VARCHAR(50),
    reference_id BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    INDEX idx_stored_filename (stored_filename),
    INDEX idx_image_status (image_status),
    INDEX idx_reference (reference_type, reference_id),
    INDEX idx_category (category),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. MOCK DATA
-- =====================================================

INSERT INTO images (
    image_id, original_filename, stored_filename, mime_type, image_format,
    original_size_bytes, original_width, original_height,
    original_url, thumbnail_url, small_url, medium_url, large_url,
    image_status, processing_time_ms, error_message,
    alt_text, title, category, reference_type, reference_id,
    is_deleted, created_date, updated_date
) VALUES
-- Logo and branding
(1, 'perfect8-logo.png', 'img_2025_01_15_001_perfect8_logo.png',
'image/png', 'PNG', 245680, 800, 600,
'/images/original/img_2025_01_15_001_perfect8_logo.png',
'/images/thumbnail/img_2025_01_15_001_perfect8_logo.png',
'/images/small/img_2025_01_15_001_perfect8_logo.png',
'/images/medium/img_2025_01_15_001_perfect8_logo.png',
'/images/large/img_2025_01_15_001_perfect8_logo.png',
'ACTIVE', 1250, NULL,
'Perfect8 Logo', 'Perfect8 Company Logo', 'branding', 'BLOG_POST', 1,
false, '2025-01-15 10:00:00', '2025-01-15 10:00:00'),

(2, 'microservices-architecture.png', 'img_2025_01_15_002_microservices_arch.png',
'image/png', 'PNG', 512340, 1920, 1080,
'/images/original/img_2025_01_15_002_microservices_arch.png',
'/images/thumbnail/img_2025_01_15_002_microservices_arch.png',
'/images/small/img_2025_01_15_002_microservices_arch.png',
'/images/medium/img_2025_01_15_002_microservices_arch.png',
'/images/large/img_2025_01_15_002_microservices_arch.png',
'ACTIVE', 2100, NULL,
'Microservices Architecture Diagram', 'System Architecture Overview', 'technical', 'BLOG_POST', 1,
false, '2025-01-15 10:15:00', '2025-01-15 10:15:00'),

-- Blog post images
(3, 'spring-boot-logo.jpg', 'img_2025_02_20_001_spring_boot.jpg',
'image/jpeg', 'JPEG', 189450, 1200, 800,
'/images/original/img_2025_02_20_001_spring_boot.jpg',
'/images/thumbnail/img_2025_02_20_001_spring_boot.jpg',
'/images/small/img_2025_02_20_001_spring_boot.jpg',
'/images/medium/img_2025_02_20_001_spring_boot.jpg',
'/images/large/img_2025_02_20_001_spring_boot.jpg',
'ACTIVE', 1450, NULL,
'Spring Boot Logo', 'Spring Boot Framework Logo', 'technical', 'BLOG_POST', 2,
false, '2025-02-20 11:30:00', '2025-02-20 11:30:00'),

(4, 'jwt-flow-diagram.png', 'img_2025_03_10_001_jwt_flow.png',
'image/png', 'PNG', 367890, 1600, 900,
'/images/original/img_2025_03_10_001_jwt_flow.png',
'/images/thumbnail/img_2025_03_10_001_jwt_flow.png',
'/images/small/img_2025_03_10_001_jwt_flow.png',
'/images/medium/img_2025_03_10_001_jwt_flow.png',
'/images/large/img_2025_03_10_001_jwt_flow.png',
'ACTIVE', 1800, NULL,
'JWT Authentication Flow', 'JWT Token Flow Diagram', 'technical', 'BLOG_POST', 3,
false, '2025-03-10 13:45:00', '2025-03-10 13:45:00'),

(5, 'docker-compose-setup.jpg', 'img_2025_04_05_001_docker_setup.jpg',
'image/jpeg', 'JPEG', 423120, 1920, 1080,
'/images/original/img_2025_04_05_001_docker_setup.jpg',
'/images/thumbnail/img_2025_04_05_001_docker_setup.jpg',
'/images/small/img_2025_04_05_001_docker_setup.jpg',
'/images/medium/img_2025_04_05_001_docker_setup.jpg',
'/images/large/img_2025_04_05_001_docker_setup.jpg',
'ACTIVE', 2050, NULL,
'Docker Compose Configuration', 'Docker Compose Setup Example', 'technical', 'BLOG_POST', 4,
false, '2025-04-05 09:20:00', '2025-04-05 09:20:00'),

(6, 'container-network.png', 'img_2025_04_05_002_container_network.png',
'image/png', 'PNG', 298760, 1400, 900,
'/images/original/img_2025_04_05_002_container_network.png',
'/images/thumbnail/img_2025_04_05_002_container_network.png',
'/images/small/img_2025_04_05_002_container_network.png',
'/images/medium/img_2025_04_05_002_container_network.png',
'/images/large/img_2025_04_05_002_container_network.png',
'ACTIVE', 1650, NULL,
'Container Network Diagram', 'Docker Container Networking', 'technical', 'BLOG_POST', 4,
false, '2025-04-05 09:35:00', '2025-04-05 09:35:00'),

-- Product images
(7, 'laptop-dell-xps-15.jpg', 'img_2025_05_01_001_laptop_dell.jpg',
'image/jpeg', 'JPEG', 567890, 2000, 1500,
'/images/original/img_2025_05_01_001_laptop_dell.jpg',
'/images/thumbnail/img_2025_05_01_001_laptop_dell.jpg',
'/images/small/img_2025_05_01_001_laptop_dell.jpg',
'/images/medium/img_2025_05_01_001_laptop_dell.jpg',
'/images/large/img_2025_05_01_001_laptop_dell.jpg',
'ACTIVE', 2400, NULL,
'Dell XPS 15 Laptop', 'Dell XPS 15 Product Photo', 'product', 'PRODUCT', 1,
false, '2025-05-01 10:00:00', '2025-05-01 10:00:00'),

(8, 'laptop-dell-xps-15-side.jpg', 'img_2025_05_01_002_laptop_dell_side.jpg',
'image/jpeg', 'JPEG', 489320, 2000, 1500,
'/images/original/img_2025_05_01_002_laptop_dell_side.jpg',
'/images/thumbnail/img_2025_05_01_002_laptop_dell_side.jpg',
'/images/small/img_2025_05_01_002_laptop_dell_side.jpg',
'/images/medium/img_2025_05_01_002_laptop_dell_side.jpg',
'/images/large/img_2025_05_01_002_laptop_dell_side.jpg',
'ACTIVE', 2200, NULL,
'Dell XPS 15 Side View', 'Dell XPS 15 Side Angle', 'product', 'PRODUCT', 1,
false, '2025-05-01 10:05:00', '2025-05-01 10:05:00'),

(9, 'smartphone-iphone-15-pro.jpg', 'img_2025_05_10_001_iphone_15.jpg',
'image/jpeg', 'JPEG', 612450, 2400, 1800,
'/images/original/img_2025_05_10_001_iphone_15.jpg',
'/images/thumbnail/img_2025_05_10_001_iphone_15.jpg',
'/images/small/img_2025_05_10_001_iphone_15.jpg',
'/images/medium/img_2025_05_10_001_iphone_15.jpg',
'/images/large/img_2025_05_10_001_iphone_15.jpg',
'ACTIVE', 2800, NULL,
'Apple iPhone 15 Pro', 'iPhone 15 Pro Product Image', 'product', 'PRODUCT', 4,
false, '2025-05-10 11:20:00', '2025-05-10 11:20:00'),

(10, 'headphones-sony-wh1000xm5.jpg', 'img_2025_05_15_001_sony_headphones.jpg',
'image/jpeg', 'JPEG', 398120, 1800, 1200,
'/images/original/img_2025_05_15_001_sony_headphones.jpg',
'/images/thumbnail/img_2025_05_15_001_sony_headphones.jpg',
'/images/small/img_2025_05_15_001_sony_headphones.jpg',
'/images/medium/img_2025_05_15_001_sony_headphones.jpg',
'/images/large/img_2025_05_15_001_sony_headphones.jpg',
'ACTIVE', 2100, NULL,
'Sony WH-1000XM5 Headphones', 'Sony Premium Headphones', 'product', 'PRODUCT', 6,
false, '2025-05-15 14:30:00', '2025-05-15 14:30:00'),

(11, 'keyboard-mechanical-gaming.jpg', 'img_2025_05_20_001_gaming_keyboard.jpg',
'image/jpeg', 'JPEG', 445670, 2000, 1333,
'/images/original/img_2025_05_20_001_gaming_keyboard.jpg',
'/images/thumbnail/img_2025_05_20_001_gaming_keyboard.jpg',
'/images/small/img_2025_05_20_001_gaming_keyboard.jpg',
'/images/medium/img_2025_05_20_001_gaming_keyboard.jpg',
'/images/large/img_2025_05_20_001_gaming_keyboard.jpg',
'ACTIVE', 2350, NULL,
'Mechanical Gaming Keyboard', 'RGB Gaming Keyboard', 'product', 'PRODUCT', 8,
false, '2025-05-20 09:45:00', '2025-05-20 09:45:00'),

(12, 'mouse-logitech-mx-master.jpg', 'img_2025_05_25_001_logitech_mouse.jpg',
'image/jpeg', 'JPEG', 356780, 1600, 1200,
'/images/original/img_2025_05_25_001_logitech_mouse.jpg',
'/images/thumbnail/img_2025_05_25_001_logitech_mouse.jpg',
'/images/small/img_2025_05_25_001_logitech_mouse.jpg',
'/images/medium/img_2025_05_25_001_logitech_mouse.jpg',
'/images/large/img_2025_05_25_001_logitech_mouse.jpg',
'ACTIVE', 1950, NULL,
'Logitech MX Master 3', 'Logitech Wireless Mouse', 'product', 'PRODUCT', 9,
false, '2025-05-25 10:15:00', '2025-05-25 10:15:00'),

-- User avatars
(13, 'user-avatar-magnus.jpg', 'img_2025_06_01_001_avatar_magnus.jpg',
'image/jpeg', 'JPEG', 124560, 500, 500,
'/images/original/img_2025_06_01_001_avatar_magnus.jpg',
'/images/thumbnail/img_2025_06_01_001_avatar_magnus.jpg',
'/images/small/img_2025_06_01_001_avatar_magnus.jpg',
'/images/medium/img_2025_06_01_001_avatar_magnus.jpg',
'/images/large/img_2025_06_01_001_avatar_magnus.jpg',
'ACTIVE', 850, NULL,
'Magnus Profile Picture', 'User Avatar - Magnus', 'avatar', 'USER', 1,
false, '2025-06-01 08:30:00', '2025-06-01 08:30:00'),

(14, 'user-avatar-alice.jpg', 'img_2025_06_05_001_avatar_alice.jpg',
'image/jpeg', 'JPEG', 118920, 500, 500,
'/images/original/img_2025_06_05_001_avatar_alice.jpg',
'/images/thumbnail/img_2025_06_05_001_avatar_alice.jpg',
'/images/small/img_2025_06_05_001_avatar_alice.jpg',
'/images/medium/img_2025_06_05_001_avatar_alice.jpg',
'/images/large/img_2025_06_05_001_avatar_alice.jpg',
'ACTIVE', 820, NULL,
'Alice Profile Picture', 'User Avatar - Alice', 'avatar', 'USER', 2,
false, '2025-06-05 09:15:00', '2025-06-05 09:15:00'),

(15, 'user-avatar-bob.jpg', 'img_2025_06_10_001_avatar_bob.jpg',
'image/jpeg', 'JPEG', 132450, 500, 500,
'/images/original/img_2025_06_10_001_avatar_bob.jpg',
'/images/thumbnail/img_2025_06_10_001_avatar_bob.jpg',
'/images/small/img_2025_06_10_001_avatar_bob.jpg',
'/images/medium/img_2025_06_10_001_avatar_bob.jpg',
'/images/large/img_2025_06_10_001_avatar_bob.jpg',
'ACTIVE', 890, NULL,
'Bob Profile Picture', 'User Avatar - Bob', 'avatar', 'USER', 3,
false, '2025-06-10 10:00:00', '2025-06-10 10:00:00'),

-- Images in processing
(16, 'banner-summer-sale.jpg', 'img_2025_07_01_001_summer_banner.jpg',
'image/jpeg', 'JPEG', 789650, 3840, 2160,
'/images/original/img_2025_07_01_001_summer_banner.jpg',
NULL, NULL, NULL, NULL,
'PROCESSING', NULL, NULL,
'Summer Sale Banner', 'Promotional Summer Banner', 'marketing', 'BANNER', NULL,
false, '2025-07-01 11:00:00', '2025-07-01 11:00:00'),

-- Category images (webp)
(17, 'category-electronics.webp', 'img_2025_07_05_001_cat_electronics.webp',
'image/webp', 'WEBP', 234510, 1200, 800,
'/images/original/img_2025_07_05_001_cat_electronics.webp',
'/images/thumbnail/img_2025_07_05_001_cat_electronics.webp',
'/images/small/img_2025_07_05_001_cat_electronics.webp',
'/images/medium/img_2025_07_05_001_cat_electronics.webp',
'/images/large/img_2025_07_05_001_cat_electronics.webp',
'ACTIVE', 1350, NULL,
'Electronics Category', 'Electronics Category Image', 'category', 'CATEGORY', 1,
false, '2025-07-05 12:30:00', '2025-07-05 12:30:00'),

(18, 'category-clothing.webp', 'img_2025_07_05_002_cat_clothing.webp',
'image/webp', 'WEBP', 198760, 1200, 800,
'/images/original/img_2025_07_05_002_cat_clothing.webp',
'/images/thumbnail/img_2025_07_05_002_cat_clothing.webp',
'/images/small/img_2025_07_05_002_cat_clothing.webp',
'/images/medium/img_2025_07_05_002_cat_clothing.webp',
'/images/large/img_2025_07_05_002_cat_clothing.webp',
'ACTIVE', 1280, NULL,
'Clothing Category', 'Clothing Category Image', 'category', 'CATEGORY', 2,
false, '2025-07-05 12:35:00', '2025-07-05 12:35:00'),

(19, 'category-home-decor.webp', 'img_2025_07_05_003_cat_home_decor.webp',
'image/webp', 'WEBP', 212340, 1200, 800,
'/images/original/img_2025_07_05_003_cat_home_decor.webp',
'/images/thumbnail/img_2025_07_05_003_cat_home_decor.webp',
'/images/small/img_2025_07_05_003_cat_home_decor.webp',
'/images/medium/img_2025_07_05_003_cat_home_decor.webp',
'/images/large/img_2025_07_05_003_cat_home_decor.webp',
'ACTIVE', 1320, NULL,
'Home Decor Category', 'Home Decor Category Image', 'category', 'CATEGORY', 3,
false, '2025-07-05 12:40:00', '2025-07-05 12:40:00'),

-- Failed upload
(20, 'corrupted-file.jpg', 'img_2025_07_10_001_corrupted.jpg',
'image/jpeg', 'JPEG', 0, NULL, NULL,
NULL, NULL, NULL, NULL, NULL,
'FAILED', NULL, 'File corrupted or unreadable',
NULL, 'Corrupted Image File', NULL, NULL, NULL,
false, '2025-07-10 14:00:00', '2025-07-10 14:00:00');

-- Reset auto-increment
ALTER TABLE images AUTO_INCREMENT = 21;

-- =====================================================
-- 5. VERIFY DATA
-- =====================================================

SELECT '=== DATABASE CREATED ===' as '';
SELECT DATABASE() as current_database;

SELECT '=== TABLES ===' as '';
SHOW TABLES;

SELECT '=== IMAGES ===' as '';
SELECT 
    image_id,
    original_filename,
    stored_filename,
    mime_type,
    CONCAT(original_width, 'x', original_height) as dimensions,
    image_status,
    category,
    reference_type,
    is_deleted
FROM images 
ORDER BY image_id;

SELECT '=== IMAGES BY STATUS ===' as '';
SELECT 
    image_status,
    COUNT(*) as count,
    CONCAT(ROUND(SUM(original_size_bytes) / 1024 / 1024, 2), ' MB') as total_size
FROM images 
GROUP BY image_status
ORDER BY image_status;

SELECT '=== IMAGES BY CATEGORY ===' as '';
SELECT 
    category,
    COUNT(*) as count
FROM images 
WHERE category IS NOT NULL
GROUP BY category
ORDER BY count DESC;

SELECT '=== SETUP COMPLETE ===' as '';
SELECT 
    CONCAT('Total images: ', COUNT(*)) as total,
    CONCAT('Active: ', SUM(CASE WHEN image_status = 'ACTIVE' THEN 1 ELSE 0 END)) as active,
    CONCAT('Processing: ', SUM(CASE WHEN image_status = 'PROCESSING' THEN 1 ELSE 0 END)) as processing,
    CONCAT('Failed: ', SUM(CASE WHEN image_status = 'FAILED' THEN 1 ELSE 0 END)) as failed
FROM images;