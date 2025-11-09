-- image-CREATE-TABLE.sql
-- Database: imageDB
-- Created: 2025-11-09
-- Purpose: Create tables for image-service (image storage & processing)

-- ==============================================
-- Table: images
-- Purpose: Image metadata and processing status
-- ==============================================

CREATE TABLE IF NOT EXISTS images (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    image_status VARCHAR(50) NOT NULL,
    uploaded_date DATETIME(6),
    created_date DATETIME(6),
    updated_date DATETIME(6),
    
    INDEX idx_stored_filename (stored_filename),
    INDEX idx_image_status (image_status),
    INDEX idx_uploaded_date (uploaded_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- End of image-CREATE-TABLE.sql
