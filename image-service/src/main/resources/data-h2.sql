-- Image Service - H2 Test Data
-- This file is loaded automatically when using the h2 profile

-- Images (metadata only, actual files would need to exist)
INSERT INTO images (id, filename, original_filename, file_path, content_type, file_size, width, height, status, created_at, updated_at) VALUES
(1, 'product-001.jpg', 'laptop.jpg', '/uploads/product-001.jpg', 'image/jpeg', 524288, 1920, 1080, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'product-002.jpg', 'phone.jpg', '/uploads/product-002.jpg', 'image/jpeg', 412345, 1600, 900, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'product-003.png', 'headphones.png', '/uploads/product-003.png', 'image/png', 328192, 1200, 800, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'blog-001.jpg', 'hero-image.jpg', '/uploads/blog-001.jpg', 'image/jpeg', 615234, 2400, 1200, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'profile-001.jpg', 'avatar.jpg', '/uploads/profile-001.jpg', 'image/jpeg', 98304, 400, 400, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Note: In H2 profile, image upload will save to local ./uploads directory
-- Actual image processing will work but files are stored locally