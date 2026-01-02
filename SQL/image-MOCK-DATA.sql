-- ================================================
-- image-MOCK-DATA.sql
-- Database: imageDB
-- Created: 2025-11-16
-- Purpose: Mock data for image-service testing
-- 
-- IMPORTANT NOTES:
-- - Images for blog posts (101-112)
-- - Images for products and categories
-- - Swedish alt-text and titles
-- - Image status: READY, PROCESSING, FAILED
-- - URLs point to /uploads/ directory
-- - Reference types: POST, PRODUCT, CATEGORY, NONE
-- ================================================

-- ================================================
-- TRUNCATE TABLE (Safe reload without duplicates)
-- ================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE images;
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- INSERT MOCK DATA: images
-- ================================================

INSERT INTO images (
    image_id,
    original_filename,
    stored_filename,
    mime_type,
    image_format,
    original_size_bytes,
    original_width,
    original_height,
    original_url,
    thumbnail_url,
    small_url,
    medium_url,
    large_url,
    image_status,
    processing_time_ms,
    error_message,
    alt_text,
    title,
    category,
    reference_type,
    reference_id,
    is_deleted,
    created_date,
    updated_date
) VALUES

-- ================================================
-- BLOG POST IMAGES (101-112)
-- Referenced from blog-MOCK-DATA.sql
-- ================================================

-- Spring Boot post images (101-102)
(
    101,
    'spring-boot-3.4-logo.png',
    '2025/10/28/spring-boot-3.4-logo-7a8b9c.png',
    'image/png',
    'PNG',
    245678,
    1200,
    630,
    '/uploads/2025/10/28/spring-boot-3.4-logo-7a8b9c.png',
    '/uploads/2025/10/28/spring-boot-3.4-logo-7a8b9c-thumb.png',
    '/uploads/2025/10/28/spring-boot-3.4-logo-7a8b9c-small.png',
    '/uploads/2025/10/28/spring-boot-3.4-logo-7a8b9c-medium.png',
    '/uploads/2025/10/28/spring-boot-3.4-logo-7a8b9c-large.png',
    'READY',
    342,
    NULL,
    'Spring Boot 3.4 officiell logotyp',
    'Spring Boot 3.4 Logo',
    'BLOG',
    'POST',
    1,
    FALSE,
    '2025-10-28 14:35:00.000000',
    '2025-10-28 14:35:23.000000'
),
(
    102,
    'spring-architecture-diagram.jpg',
    '2025/10/28/spring-architecture-diagram-4d5e6f.jpg',
    'image/jpeg',
    'JPEG',
    512345,
    1920,
    1080,
    '/uploads/2025/10/28/spring-architecture-diagram-4d5e6f.jpg',
    '/uploads/2025/10/28/spring-architecture-diagram-4d5e6f-thumb.jpg',
    '/uploads/2025/10/28/spring-architecture-diagram-4d5e6f-small.jpg',
    '/uploads/2025/10/28/spring-architecture-diagram-4d5e6f-medium.jpg',
    '/uploads/2025/10/28/spring-architecture-diagram-4d5e6f-large.jpg',
    'READY',
    578,
    NULL,
    'Arkitekturdiagram för Spring Boot applikation',
    'Architecture Diagram',
    'BLOG',
    'POST',
    1,
    FALSE,
    '2025-10-28 14:40:00.000000',
    '2025-10-28 14:40:34.000000'
),

-- Docker post images (103-104)
(
    103,
    'docker-compose-example.png',
    '2025/11/04/docker-compose-example-1a2b3c.png',
    'image/png',
    'PNG',
    187234,
    1600,
    900,
    '/uploads/2025/11/04/docker-compose-example-1a2b3c.png',
    '/uploads/2025/11/04/docker-compose-example-1a2b3c-thumb.png',
    '/uploads/2025/11/04/docker-compose-example-1a2b3c-small.png',
    '/uploads/2025/11/04/docker-compose-example-1a2b3c-medium.png',
    '/uploads/2025/11/04/docker-compose-example-1a2b3c-large.png',
    'READY',
    423,
    NULL,
    'Exempel på docker-compose.yml fil',
    'Docker Compose Example',
    'BLOG',
    'POST',
    2,
    FALSE,
    '2025-11-04 16:25:00.000000',
    '2025-11-04 16:25:28.000000'
),
(
    104,
    'microservices-architecture.svg',
    '2025/11/04/microservices-architecture-7d8e9f.svg',
    'image/svg+xml',
    'SVG',
    98456,
    2000,
    1200,
    '/uploads/2025/11/04/microservices-architecture-7d8e9f.svg',
    '/uploads/2025/11/04/microservices-architecture-7d8e9f-thumb.png',
    '/uploads/2025/11/04/microservices-architecture-7d8e9f-small.png',
    '/uploads/2025/11/04/microservices-architecture-7d8e9f-medium.png',
    '/uploads/2025/11/04/microservices-architecture-7d8e9f-large.png',
    'READY',
    267,
    NULL,
    'Microservices arkitektur med 5 services',
    'Microservices Architecture',
    'BLOG',
    'POST',
    2,
    FALSE,
    '2025-11-04 16:30:00.000000',
    '2025-11-04 16:30:15.000000'
),

-- JWT post images (105-107)
(
    105,
    'jwt-token-flow.png',
    '2025/11/07/jwt-token-flow-2b3c4d.png',
    'image/png',
    'PNG',
    312567,
    1800,
    1000,
    '/uploads/2025/11/07/jwt-token-flow-2b3c4d.png',
    '/uploads/2025/11/07/jwt-token-flow-2b3c4d-thumb.png',
    '/uploads/2025/11/07/jwt-token-flow-2b3c4d-small.png',
    '/uploads/2025/11/07/jwt-token-flow-2b3c4d-medium.png',
    '/uploads/2025/11/07/jwt-token-flow-2b3c4d-large.png',
    'READY',
    489,
    NULL,
    'Flödesschema för JWT token autentisering',
    'JWT Token Flow',
    'BLOG',
    'POST',
    3,
    FALSE,
    '2025-11-07 09:50:00.000000',
    '2025-11-07 09:50:32.000000'
),
(
    106,
    'security-filter-chain.jpg',
    '2025/11/07/security-filter-chain-5e6f7a.jpg',
    'image/jpeg',
    'JPEG',
    423890,
    1920,
    1080,
    '/uploads/2025/11/07/security-filter-chain-5e6f7a.jpg',
    '/uploads/2025/11/07/security-filter-chain-5e6f7a-thumb.jpg',
    '/uploads/2025/11/07/security-filter-chain-5e6f7a-small.jpg',
    '/uploads/2025/11/07/security-filter-chain-5e6f7a-medium.jpg',
    '/uploads/2025/11/07/security-filter-chain-5e6f7a-large.jpg',
    'READY',
    534,
    NULL,
    'Spring Security filter chain diagram',
    'Security Filter Chain',
    'BLOG',
    'POST',
    3,
    FALSE,
    '2025-11-07 09:55:00.000000',
    '2025-11-07 09:55:38.000000'
),
(
    107,
    'authentication-sequence.png',
    '2025/11/07/authentication-sequence-8b9c0d.png',
    'image/png',
    'PNG',
    267345,
    1600,
    900,
    '/uploads/2025/11/07/authentication-sequence-8b9c0d.png',
    '/uploads/2025/11/07/authentication-sequence-8b9c0d-thumb.png',
    '/uploads/2025/11/07/authentication-sequence-8b9c0d-small.png',
    '/uploads/2025/11/07/authentication-sequence-8b9c0d-medium.png',
    '/uploads/2025/11/07/authentication-sequence-8b9c0d-large.png',
    'READY',
    401,
    NULL,
    'Sekvensdiagram för autentiseringsflöde',
    'Authentication Sequence',
    'BLOG',
    'POST',
    3,
    FALSE,
    '2025-11-07 10:00:00.000000',
    '2025-11-07 10:00:27.000000'
),

-- Hibernate post images (108-109)
(
    108,
    'n-plus-one-problem.jpg',
    '2025/11/09/n-plus-one-problem-3c4d5e.jpg',
    'image/jpeg',
    'JPEG',
    389012,
    1800,
    1000,
    '/uploads/2025/11/09/n-plus-one-problem-3c4d5e.jpg',
    '/uploads/2025/11/09/n-plus-one-problem-3c4d5e-thumb.jpg',
    '/uploads/2025/11/09/n-plus-one-problem-3c4d5e-small.jpg',
    '/uploads/2025/11/09/n-plus-one-problem-3c4d5e-medium.jpg',
    '/uploads/2025/11/09/n-plus-one-problem-3c4d5e-large.jpg',
    'READY',
    512,
    NULL,
    'Illustration av N+1 query problem i Hibernate',
    'N+1 Problem Diagram',
    'BLOG',
    'POST',
    4,
    FALSE,
    '2025-11-09 13:20:00.000000',
    '2025-11-09 13:20:36.000000'
),
(
    109,
    'query-performance-graph.png',
    '2025/11/09/query-performance-graph-6f7a8b.png',
    'image/png',
    'PNG',
    198765,
    1600,
    900,
    '/uploads/2025/11/09/query-performance-graph-6f7a8b.png',
    '/uploads/2025/11/09/query-performance-graph-6f7a8b-thumb.png',
    '/uploads/2025/11/09/query-performance-graph-6f7a8b-small.png',
    '/uploads/2025/11/09/query-performance-graph-6f7a8b-medium.png',
    '/uploads/2025/11/09/query-performance-graph-6f7a8b-large.png',
    'READY',
    378,
    NULL,
    'Graf som visar query performance-förbättring',
    'Query Performance Graph',
    'BLOG',
    'POST',
    4,
    FALSE,
    '2025-11-09 13:25:00.000000',
    '2025-11-09 13:25:26.000000'
),

-- Flutter post images (110-111)
(
    110,
    'flutter-logo.png',
    '2025/11/11/flutter-logo-9c0d1e.png',
    'image/png',
    'PNG',
    156789,
    1200,
    1200,
    '/uploads/2025/11/11/flutter-logo-9c0d1e.png',
    '/uploads/2025/11/11/flutter-logo-9c0d1e-thumb.png',
    '/uploads/2025/11/11/flutter-logo-9c0d1e-small.png',
    '/uploads/2025/11/11/flutter-logo-9c0d1e-medium.png',
    '/uploads/2025/11/11/flutter-logo-9c0d1e-large.png',
    'READY',
    298,
    NULL,
    'Flutter officiell logotyp',
    'Flutter Logo',
    'BLOG',
    'POST',
    5,
    FALSE,
    '2025-11-11 15:35:00.000000',
    '2025-11-11 15:35:19.000000'
),
(
    111,
    'rest-api-integration.jpg',
    '2025/11/11/rest-api-integration-2e3f4a.jpg',
    'image/jpeg',
    'JPEG',
    445678,
    1920,
    1080,
    '/uploads/2025/11/11/rest-api-integration-2e3f4a.jpg',
    '/uploads/2025/11/11/rest-api-integration-2e3f4a-thumb.jpg',
    '/uploads/2025/11/11/rest-api-integration-2e3f4a-small.jpg',
    '/uploads/2025/11/11/rest-api-integration-2e3f4a-medium.jpg',
    '/uploads/2025/11/11/rest-api-integration-2e3f4a-large.jpg',
    'READY',
    567,
    NULL,
    'Flutter REST API integration diagram',
    'REST API Integration',
    'BLOG',
    'POST',
    5,
    FALSE,
    '2025-11-11 15:40:00.000000',
    '2025-11-11 15:40:40.000000'
),

-- ADHD post image (112)
(
    112,
    'productivity-tips-infographic.png',
    '2025/11/12/productivity-tips-infographic-5a6b7c.png',
    'image/png',
    'PNG',
    534890,
    1200,
    2400,
    '/uploads/2025/11/12/productivity-tips-infographic-5a6b7c.png',
    '/uploads/2025/11/12/productivity-tips-infographic-5a6b7c-thumb.png',
    '/uploads/2025/11/12/productivity-tips-infographic-5a6b7c-small.png',
    '/uploads/2025/11/12/productivity-tips-infographic-5a6b7c-medium.png',
    '/uploads/2025/11/12/productivity-tips-infographic-5a6b7c-large.png',
    'READY',
    623,
    NULL,
    'Infografik med produktivitetstips för ADHD',
    'Productivity Tips Infographic',
    'BLOG',
    'POST',
    8,
    FALSE,
    '2025-11-12 12:05:00.000000',
    '2025-11-12 12:05:43.000000'
),

-- ================================================
-- PRODUCT CATEGORY IMAGES (201-210)
-- For shop-service categories
-- ================================================

(
    201,
    'category-electronics.jpg',
    '2025/01/15/category-electronics-1a2b3c.jpg',
    'image/jpeg',
    'JPEG',
    678901,
    1600,
    900,
    '/uploads/2025/01/15/category-electronics-1a2b3c.jpg',
    '/uploads/2025/01/15/category-electronics-1a2b3c-thumb.jpg',
    '/uploads/2025/01/15/category-electronics-1a2b3c-small.jpg',
    '/uploads/2025/01/15/category-electronics-1a2b3c-medium.jpg',
    '/uploads/2025/01/15/category-electronics-1a2b3c-large.jpg',
    'READY',
    445,
    NULL,
    'Elektronikprodukter och prylar',
    'Elektronik Kategori',
    'SHOP',
    'CATEGORY',
    1,
    FALSE,
    '2025-01-15 10:00:00.000000',
    '2025-01-15 10:00:31.000000'
),
(
    202,
    'category-computers.jpg',
    '2025/01/15/category-computers-4d5e6f.jpg',
    'image/jpeg',
    'JPEG',
    723456,
    1600,
    900,
    '/uploads/2025/01/15/category-computers-4d5e6f.jpg',
    '/uploads/2025/01/15/category-computers-4d5e6f-thumb.jpg',
    '/uploads/2025/01/15/category-computers-4d5e6f-small.jpg',
    '/uploads/2025/01/15/category-computers-4d5e6f-medium.jpg',
    '/uploads/2025/01/15/category-computers-4d5e6f-large.jpg',
    'READY',
    478,
    NULL,
    'Datorer och tillbehör',
    'Datorer Kategori',
    'SHOP',
    'CATEGORY',
    2,
    FALSE,
    '2025-01-15 10:05:00.000000',
    '2025-01-15 10:05:34.000000'
),
(
    203,
    'category-smartphones.jpg',
    '2025/01/15/category-smartphones-7a8b9c.jpg',
    'image/jpeg',
    'JPEG',
    589234,
    1600,
    900,
    '/uploads/2025/01/15/category-smartphones-7a8b9c.jpg',
    '/uploads/2025/01/15/category-smartphones-7a8b9c-thumb.jpg',
    '/uploads/2025/01/15/category-smartphones-7a8b9c-small.jpg',
    '/uploads/2025/01/15/category-smartphones-7a8b9c-medium.jpg',
    '/uploads/2025/01/15/category-smartphones-7a8b9c-large.jpg',
    'READY',
    412,
    NULL,
    'Smartphones och tillbehör',
    'Smartphones Kategori',
    'SHOP',
    'CATEGORY',
    3,
    FALSE,
    '2025-01-15 10:10:00.000000',
    '2025-01-15 10:10:29.000000'
),

-- ================================================
-- PRODUCT IMAGES (301-320)
-- For shop-service products
-- ================================================

(
    301,
    'product-macbook-pro-m4.jpg',
    '2025/02/01/product-macbook-pro-m4-1b2c3d.jpg',
    'image/jpeg',
    'JPEG',
    845678,
    2000,
    1500,
    '/uploads/2025/02/01/product-macbook-pro-m4-1b2c3d.jpg',
    '/uploads/2025/02/01/product-macbook-pro-m4-1b2c3d-thumb.jpg',
    '/uploads/2025/02/01/product-macbook-pro-m4-1b2c3d-small.jpg',
    '/uploads/2025/02/01/product-macbook-pro-m4-1b2c3d-medium.jpg',
    '/uploads/2025/02/01/product-macbook-pro-m4-1b2c3d-large.jpg',
    'READY',
    689,
    NULL,
    'MacBook Pro 16" med M4 chip',
    'MacBook Pro M4',
    'SHOP',
    'PRODUCT',
    1001,
    FALSE,
    '2025-02-01 14:00:00.000000',
    '2025-02-01 14:00:48.000000'
),
(
    302,
    'product-iphone-15-pro.jpg',
    '2025/02/05/product-iphone-15-pro-4e5f6a.jpg',
    'image/jpeg',
    'JPEG',
    756234,
    2000,
    1500,
    '/uploads/2025/02/05/product-iphone-15-pro-4e5f6a.jpg',
    '/uploads/2025/02/05/product-iphone-15-pro-4e5f6a-thumb.jpg',
    '/uploads/2025/02/05/product-iphone-15-pro-4e5f6a-small.jpg',
    '/uploads/2025/02/05/product-iphone-15-pro-4e5f6a-medium.jpg',
    '/uploads/2025/02/05/product-iphone-15-pro-4e5f6a-large.jpg',
    'READY',
    612,
    NULL,
    'iPhone 15 Pro 256GB Titan Blå',
    'iPhone 15 Pro',
    'SHOP',
    'PRODUCT',
    1002,
    FALSE,
    '2025-02-05 11:30:00.000000',
    '2025-02-05 11:30:42.000000'
),
(
    303,
    'product-airpods-pro-2.jpg',
    '2025/02/10/product-airpods-pro-2-7b8c9d.jpg',
    'image/jpeg',
    'JPEG',
    534890,
    1800,
    1200,
    '/uploads/2025/02/10/product-airpods-pro-2-7b8c9d.jpg',
    '/uploads/2025/02/10/product-airpods-pro-2-7b8c9d-thumb.jpg',
    '/uploads/2025/02/10/product-airpods-pro-2-7b8c9d-small.jpg',
    '/uploads/2025/02/10/product-airpods-pro-2-7b8c9d-medium.jpg',
    '/uploads/2025/02/10/product-airpods-pro-2-7b8c9d-large.jpg',
    'READY',
    456,
    NULL,
    'AirPods Pro 2:a generationen med MagSafe',
    'AirPods Pro 2',
    'SHOP',
    'PRODUCT',
    1003,
    FALSE,
    '2025-02-10 09:15:00.000000',
    '2025-02-10 09:15:32.000000'
),
(
    304,
    'product-samsung-galaxy-s24.jpg',
    '2025/02/15/product-samsung-galaxy-s24-0d1e2f.jpg',
    'image/jpeg',
    'JPEG',
    698745,
    2000,
    1500,
    '/uploads/2025/02/15/product-samsung-galaxy-s24-0d1e2f.jpg',
    '/uploads/2025/02/15/product-samsung-galaxy-s24-0d1e2f-thumb.jpg',
    '/uploads/2025/02/15/product-samsung-galaxy-s24-0d1e2f-small.jpg',
    '/uploads/2025/02/15/product-samsung-galaxy-s24-0d1e2f-medium.jpg',
    '/uploads/2025/02/15/product-samsung-galaxy-s24-0d1e2f-large.jpg',
    'READY',
    578,
    NULL,
    'Samsung Galaxy S24 Ultra 512GB Svart',
    'Samsung Galaxy S24 Ultra',
    'SHOP',
    'PRODUCT',
    1004,
    FALSE,
    '2025-02-15 13:45:00.000000',
    '2025-02-15 13:45:41.000000'
),
(
    305,
    'product-logitech-mx-master-3s.jpg',
    '2025/03/01/product-logitech-mx-master-3s-3f4a5b.jpg',
    'image/jpeg',
    'JPEG',
    467823,
    1600,
    1200,
    '/uploads/2025/03/01/product-logitech-mx-master-3s-3f4a5b.jpg',
    '/uploads/2025/03/01/product-logitech-mx-master-3s-3f4a5b-thumb.jpg',
    '/uploads/2025/03/01/product-logitech-mx-master-3s-3f4a5b-small.jpg',
    '/uploads/2025/03/01/product-logitech-mx-master-3s-3f4a5b-medium.jpg',
    '/uploads/2025/03/01/product-logitech-mx-master-3s-3f4a5b-large.jpg',
    'READY',
    389,
    NULL,
    'Logitech MX Master 3S trådlös mus',
    'Logitech MX Master 3S',
    'SHOP',
    'PRODUCT',
    1005,
    FALSE,
    '2025-03-01 10:20:00.000000',
    '2025-03-01 10:20:27.000000'
),

-- ================================================
-- TEST IMAGES (different statuses)
-- ================================================

-- Processing image (not ready yet)
(
    901,
    'test-processing-image.jpg',
    '2025/11/16/test-processing-image-9a0b1c.jpg',
    'image/jpeg',
    'JPEG',
    1234567,
    4000,
    3000,
    NULL, -- No URLs yet
    NULL,
    NULL,
    NULL,
    NULL,
    'PROCESSING',
    NULL, -- Still processing
    NULL,
    'Test image i processningsstadiet',
    'Processing Test Image',
    'TEST',
    'NONE',
    NULL,
    FALSE,
    '2025-11-16 09:00:00.000000',
    '2025-11-16 09:00:00.000000'
),

-- Failed image (processing error)
(
    902,
    'test-failed-image.corrupted',
    '2025/11/16/test-failed-image-2c3d4e.jpg',
    'image/jpeg',
    'JPEG',
    98765,
    0,
    0,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    'FAILED',
    1234,
    'Image file corrupted: Invalid JPEG header',
    'Test image som misslyckades med processning',
    'Failed Test Image',
    'TEST',
    'NONE',
    NULL,
    FALSE,
    '2025-11-16 09:05:00.000000',
    '2025-11-16 09:05:12.000000'
),

-- Deleted image (soft delete)
(
    903,
    'test-deleted-image.png',
    '2025/11/10/test-deleted-image-5e6f7a.png',
    'image/png',
    'PNG',
    345678,
    1200,
    800,
    '/uploads/2025/11/10/test-deleted-image-5e6f7a.png',
    '/uploads/2025/11/10/test-deleted-image-5e6f7a-thumb.png',
    '/uploads/2025/11/10/test-deleted-image-5e6f7a-small.png',
    '/uploads/2025/11/10/test-deleted-image-5e6f7a-medium.png',
    '/uploads/2025/11/10/test-deleted-image-5e6f7a-large.png',
    'READY',
    412,
    NULL,
    'Test image som har raderats (soft delete)',
    'Deleted Test Image',
    'TEST',
    'NONE',
    NULL,
    TRUE, -- SOFT DELETED
    '2025-11-10 15:00:00.000000',
    '2025-11-15 12:30:00.000000'
);

-- ================================================
-- VERIFICATION QUERIES (uncomment to test)
-- ================================================

-- -- Check images by category and status
-- SELECT 
--     image_id,
--     original_filename,
--     category,
--     reference_type,
--     reference_id,
--     image_status,
--     is_deleted,
--     CONCAT(original_width, 'x', original_height) AS dimensions,
--     ROUND(original_size_bytes / 1024, 2) AS size_kb,
--     processing_time_ms,
--     DATE_FORMAT(created_date, '%Y-%m-%d %H:%i') AS uploaded
-- FROM images
-- WHERE is_deleted = FALSE
-- ORDER BY category, image_id;

-- -- Check blog post images
-- SELECT 
--     i.image_id,
--     i.title,
--     i.reference_id AS post_id,
--     i.image_status,
--     i.alt_text,
--     DATE_FORMAT(i.created_date, '%Y-%m-%d') AS uploaded
-- FROM images i
-- WHERE i.reference_type = 'POST'
--   AND i.is_deleted = FALSE
-- ORDER BY i.image_id;

-- -- Check product images
-- SELECT 
--     i.image_id,
--     i.title,
--     i.reference_id AS product_id,
--     ROUND(i.original_size_bytes / 1024, 2) AS size_kb,
--     CONCAT(i.original_width, 'x', i.original_height) AS dimensions,
--     i.processing_time_ms AS process_ms
-- FROM images i
-- WHERE i.reference_type = 'PRODUCT'
--   AND i.is_deleted = FALSE
-- ORDER BY i.image_id;

-- -- Check image status distribution
-- SELECT 
--     image_status,
--     COUNT(*) AS count,
--     ROUND(AVG(processing_time_ms), 0) AS avg_process_ms,
--     ROUND(SUM(original_size_bytes) / 1024 / 1024, 2) AS total_mb
-- FROM images
-- WHERE is_deleted = FALSE
-- GROUP BY image_status
-- ORDER BY count DESC;

-- ================================================
-- End of image-MOCK-DATA.sql
-- 
-- SUMMARY:
-- - 12 blog post images (101-112) - READY
-- - 3 category images (201-203) - READY
-- - 5 product images (301-305) - READY
-- - 3 test images (901-903) - PROCESSING, FAILED, DELETED
-- - Total: 23 images
-- - Image formats: JPEG, PNG, SVG
-- - Swedish alt-text and titles
-- - Realistic file sizes and dimensions
-- - Multiple thumbnail sizes per image
-- - Ready for frontend image display testing
-- ================================================
