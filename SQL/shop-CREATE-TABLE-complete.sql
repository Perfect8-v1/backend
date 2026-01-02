-- ============================================================
-- Perfect8 Shop Service - CREATE TABLES
-- Version 1.0 - Core E-commerce Tables
-- Database: shopDB
-- Created: 2025-11-21
-- ============================================================

USE shopDB;

-- Disable foreign key checks for clean table creation
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. CATEGORIES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id BIGINT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_category_parent 
        FOREIGN KEY (parent_category_id) 
        REFERENCES categories(category_id)
        ON DELETE SET NULL,
    
    INDEX idx_category_slug (slug),
    INDEX idx_category_active (is_active),
    INDEX idx_category_parent (parent_category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. PRODUCTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category_id BIGINT,
    image_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_product_category 
        FOREIGN KEY (category_id) 
        REFERENCES categories(category_id)
        ON DELETE SET NULL,
    
    INDEX idx_product_sku (sku),
    INDEX idx_product_category (category_id),
    INDEX idx_product_active (is_active),
    INDEX idx_product_created (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. CARTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    session_id VARCHAR(255),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at DATETIME,
    
    CONSTRAINT fk_cart_customer 
        FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id)
        ON DELETE CASCADE,
    
    INDEX idx_cart_customer (customer_id),
    INDEX idx_cart_session (session_id),
    INDEX idx_cart_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. CART_ITEMS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price_at_add DECIMAL(10,2) NOT NULL,
    added_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_cart_item_cart 
        FOREIGN KEY (cart_id) 
        REFERENCES carts(cart_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_cart_item_product 
        FOREIGN KEY (product_id) 
        REFERENCES products(product_id)
        ON DELETE CASCADE,
    
    INDEX idx_cart_item_cart (cart_id),
    INDEX idx_cart_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. ORDERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    order_status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    shipping_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    billing_address_id BIGINT,
    shipping_address_id BIGINT,
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shipped_date DATETIME,
    delivered_date DATETIME,
    cancelled_date DATETIME,
    notes TEXT,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_order_customer 
        FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id)
        ON DELETE RESTRICT,
    
    CONSTRAINT fk_order_billing_address 
        FOREIGN KEY (billing_address_id) 
        REFERENCES addresses(address_id)
        ON DELETE SET NULL,
    
    CONSTRAINT fk_order_shipping_address 
        FOREIGN KEY (shipping_address_id) 
        REFERENCES addresses(address_id)
        ON DELETE SET NULL,
    
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_number (order_number),
    INDEX idx_order_status (order_status),
    INDEX idx_order_date (order_date),
    INDEX idx_order_created (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. ORDER_ITEMS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    tax_rate DECIMAL(5,2) DEFAULT 0.00,
    
    CONSTRAINT fk_order_item_order 
        FOREIGN KEY (order_id) 
        REFERENCES orders(order_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_order_item_product 
        FOREIGN KEY (product_id) 
        REFERENCES products(product_id)
        ON DELETE RESTRICT,
    
    INDEX idx_order_item_order (order_id),
    INDEX idx_order_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. PAYMENTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    transaction_id VARCHAR(255),
    payment_date DATETIME,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_date DATETIME,
    
    CONSTRAINT fk_payment_order 
        FOREIGN KEY (order_id) 
        REFERENCES orders(order_id)
        ON DELETE RESTRICT,
    
    INDEX idx_payment_order (order_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_payment_method (payment_method),
    INDEX idx_payment_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. SHIPMENTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS shipments (
    shipment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    carrier VARCHAR(100),
    tracking_number VARCHAR(255),
    shipment_status VARCHAR(50) NOT NULL,
    shipped_date DATETIME,
    estimated_delivery_date DATETIME,
    actual_delivery_date DATETIME,
    recipient_name VARCHAR(255),
    recipient_phone VARCHAR(20),
    street VARCHAR(255),
    apartment VARCHAR(100),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    
    CONSTRAINT fk_shipment_order 
        FOREIGN KEY (order_id) 
        REFERENCES orders(order_id)
        ON DELETE CASCADE,
    
    INDEX idx_shipment_order (order_id),
    INDEX idx_shipment_tracking (tracking_number),
    INDEX idx_shipment_status (shipment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. SHIPMENT_TRACKING TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS shipment_tracking (
    tracking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    location VARCHAR(255),
    status VARCHAR(100),
    notes TEXT,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_tracking_shipment 
        FOREIGN KEY (shipment_id) 
        REFERENCES shipments(shipment_id)
        ON DELETE CASCADE,
    
    INDEX idx_tracking_shipment (shipment_id),
    INDEX idx_tracking_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. INVENTORY_TRANSACTIONS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    notes TEXT,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_inventory_product 
        FOREIGN KEY (product_id) 
        REFERENCES products(product_id)
        ON DELETE CASCADE,
    
    INDEX idx_inventory_product (product_id),
    INDEX idx_inventory_type (transaction_type),
    INDEX idx_inventory_created (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- VERIFICATION QUERY
-- ============================================================
-- Show all tables in shopDB
SHOW TABLES;

SELECT 'Shop database tables created successfully!' AS status;
