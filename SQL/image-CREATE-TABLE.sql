-- image-CREATE-TABLE.sql
-- Database: imageDB
-- Created: 2025-11-09
-- Updated: 2025-11-13 - Fixed schema validation errors
-- Purpose: Create tables for image-service (image storage & processing)

-- ==============================================
-- Table: images
-- Purpose: Image metadata and processing status
-- Matches: com.perfect8.image.model.Image
-- ==============================================

CREATE TABLE IF NOT EXISTS images (
    -- Primary Key
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- File metadata
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL UNIQUE,
    mime_type VARCHAR(100),
    image_format VARCHAR(50),
    original_size_bytes BIGINT,
    original_width INT,
    original_height INT,
    
    -- Image URLs (different sizes)
    original_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    small_url VARCHAR(500),
    medium_url VARCHAR(500),
    large_url VARCHAR(500),
    
    -- Processing status
    image_status VARCHAR(50) NOT NULL,
    processing_time_ms BIGINT,
    error_message TEXT,
    
    -- Image metadata
    alt_text VARCHAR(500),
    title VARCHAR(255),
    category VARCHAR(100),
    
    -- Reference to other entities (post, product, etc.)
    reference_type VARCHAR(50),
    reference_id BIGINT,
    
    -- Soft delete
    is_deleted BOOLEAN DEFAULT FALSE,
    
    -- Audit fields
    created_date DATETIME(6) NOT NULL,
    updated_date DATETIME(6),
    
    -- Indexes
    INDEX idx_stored_filename (stored_filename),
    INDEX idx_image_status (image_status),
    INDEX idx_created_date (created_date),
    INDEX idx_reference (reference_type, reference_id),
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_category (category)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- End of image-CREATE-TABLE.sql
