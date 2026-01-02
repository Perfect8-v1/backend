-- =====================================================
-- SHOP-SERVICE KOMPLETT SETUP
-- Skapar: databas, tabeller, mock-data
-- Database: shopDB
-- =====================================================

-- 1. Skapa databas
CREATE DATABASE IF NOT EXISTS shopDB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE shopDB;

-- 2. Ta bort gamla tabeller (om de finns)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS inventory_transactions;
DROP TABLE IF EXISTS shipment_tracking;
DROP TABLE IF EXISTS shipments;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS customers;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 3. SKAPA TABELLER
-- =====================================================

-- 3.1 CUSTOMERS
CREATE TABLE customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    reset_password_token VARCHAR(255),
    reset_password_token_expiry DATETIME,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked_until DATETIME,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    last_login_date DATETIME,
    INDEX idx_email (email),
    INDEX idx_email_verification_token (email_verification_token),
    INDEX idx_reset_password_token (reset_password_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2 CATEGORIES
CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id BIGINT,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_category_id) 
        REFERENCES categories(category_id) ON DELETE SET NULL,
    INDEX idx_slug (slug),
    INDEX idx_parent_category_id (parent_category_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.3 PRODUCTS
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category_id BIGINT,
    image_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) 
        REFERENCES categories(category_id) ON DELETE SET NULL,
    INDEX idx_sku (sku),
    INDEX idx_category_id (category_id),
    INDEX idx_is_active (is_active),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.4 ADDRESSES
CREATE TABLE addresses (
    address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    apartment VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    address_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_addresses_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_address_type (address_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.5 CARTS
CREATE TABLE carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    session_id VARCHAR(255),
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    expires_at DATETIME,
    CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_session_id (session_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.6 CART_ITEMS
CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_at_add DECIMAL(10, 2) NOT NULL,
    added_date DATETIME NOT NULL,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) 
        REFERENCES carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) 
        REFERENCES products(product_id) ON DELETE CASCADE,
    INDEX idx_cart_id (cart_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.7 ORDERS
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    order_status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    billing_address_id BIGINT,
    shipping_address_id BIGINT,
    order_date DATETIME NOT NULL,
    shipped_date DATETIME,
    delivered_date DATETIME,
    cancelled_date DATETIME,
    notes TEXT,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_billing_address FOREIGN KEY (billing_address_id) 
        REFERENCES addresses(address_id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_shipping_address FOREIGN KEY (shipping_address_id) 
        REFERENCES addresses(address_id) ON DELETE SET NULL,
    INDEX idx_customer_id (customer_id),
    INDEX idx_order_number (order_number),
    INDEX idx_order_status (order_status),
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.8 ORDER_ITEMS
CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) 
        REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) 
        REFERENCES products(product_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.9 PAYMENTS
CREATE TABLE payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transaction_id VARCHAR(255),
    payment_date DATETIME,
    created_date DATETIME NOT NULL,
    processed_date DATETIME,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) 
        REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.10 SHIPMENTS
CREATE TABLE shipments (
    shipment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    carrier VARCHAR(100),
    tracking_number VARCHAR(255),
    shipment_status VARCHAR(50) NOT NULL,
    shipped_date DATETIME,
    estimated_delivery_date DATETIME,
    actual_delivery_date DATETIME,
    recipient_name VARCHAR(255) NOT NULL,
    recipient_phone VARCHAR(20),
    street VARCHAR(255) NOT NULL,
    apartment VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) 
        REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_tracking_number (tracking_number),
    INDEX idx_shipment_status (shipment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.11 SHIPMENT_TRACKING
CREATE TABLE shipment_tracking (
    tracking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    location VARCHAR(255),
    status VARCHAR(100) NOT NULL,
    notes TEXT,
    timestamp DATETIME NOT NULL,
    CONSTRAINT fk_shipment_tracking_shipment FOREIGN KEY (shipment_id) 
        REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    INDEX idx_shipment_id (shipment_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.12 INVENTORY_TRANSACTIONS
CREATE TABLE inventory_transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    notes TEXT,
    created_date DATETIME NOT NULL,
    CONSTRAINT fk_inventory_transactions_product FOREIGN KEY (product_id) 
        REFERENCES products(product_id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_created_date (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. MOCK DATA
-- =====================================================

-- Lösenord för alla kunder: password123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi

-- 4.1 CUSTOMERS
INSERT INTO customers (customer_id, email, password_hash, first_name, last_name, phone, is_active, is_email_verified, email_verification_token, reset_password_token, reset_password_token_expiry, failed_login_attempts, account_locked_until, created_date, updated_date, last_login_date) VALUES
(1, 'magnus@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Magnus', 'Berglund', '+46701234567', true, true, NULL, NULL, NULL, 0, NULL, '2025-01-01 08:00:00', '2025-01-01 08:00:00', '2025-11-22 09:00:00'),
(2, 'anna.svensson@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Anna', 'Svensson', '+46709876543', true, true, NULL, NULL, NULL, 0, NULL, '2025-02-15 10:00:00', '2025-02-15 10:00:00', '2025-11-21 14:30:00'),
(3, 'erik.andersson@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Erik', 'Andersson', '+46705551234', true, true, NULL, NULL, NULL, 0, NULL, '2025-03-20 11:00:00', '2025-03-20 11:00:00', '2025-11-20 16:45:00'),
(4, 'sofia.karlsson@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Sofia', 'Karlsson', '+46707778899', true, true, NULL, NULL, NULL, 0, NULL, '2025-04-10 12:00:00', '2025-04-10 12:00:00', '2025-11-19 10:15:00'),
(5, 'new.customer@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'New', 'Customer', '+46703334455', true, false, 'verify_token_abc123', NULL, NULL, 0, NULL, '2025-11-15 14:00:00', '2025-11-15 14:00:00', NULL);

-- 4.2 CATEGORIES
INSERT INTO categories (category_id, name, slug, description, parent_category_id, display_order, is_active) VALUES
-- Top level categories
(1, 'Electronics', 'electronics', 'Electronic devices and accessories', NULL, 1, true),
(2, 'Computers', 'computers', 'Desktop and laptop computers', NULL, 2, true),
(3, 'Audio', 'audio', 'Headphones, speakers, and audio equipment', NULL, 3, true),
(4, 'Accessories', 'accessories', 'Computer and phone accessories', NULL, 4, true),
-- Sub-categories under Electronics
(5, 'Smartphones', 'smartphones', 'Mobile phones and smartphones', 1, 1, true),
(6, 'Tablets', 'tablets', 'Tablet computers', 1, 2, true),
-- Sub-categories under Computers
(7, 'Laptops', 'laptops', 'Portable computers', 2, 1, true),
(8, 'Desktops', 'desktops', 'Desktop computers', 2, 2, true),
-- Sub-categories under Audio
(9, 'Headphones', 'headphones', 'Over-ear and in-ear headphones', 3, 1, true),
(10, 'Speakers', 'speakers', 'Bluetooth and wired speakers', 3, 2, true);

-- 4.3 PRODUCTS
INSERT INTO products (product_id, sku, name, description, price, stock_quantity, category_id, image_id, is_active, created_date, updated_date) VALUES
-- Laptops
(1, 'LAP-DELL-XPS15-001', 'Dell XPS 15', 'High-performance laptop with 15.6" display, Intel i7, 16GB RAM, 512GB SSD', 1599.99, 15, 7, 7, true, '2025-05-01 10:00:00', '2025-05-01 10:00:00'),
(2, 'LAP-APPLE-MBP14-001', 'Apple MacBook Pro 14"', 'M3 Pro chip, 18GB RAM, 512GB SSD, Liquid Retina XDR display', 2499.99, 8, 7, NULL, true, '2025-05-05 11:00:00', '2025-05-05 11:00:00'),
(3, 'LAP-LENOVO-T14-001', 'Lenovo ThinkPad T14', 'Business laptop with Intel i5, 8GB RAM, 256GB SSD', 999.99, 25, 7, NULL, true, '2025-05-10 09:00:00', '2025-05-10 09:00:00'),

-- Smartphones
(4, 'PHONE-APPLE-IP15P-001', 'Apple iPhone 15 Pro', 'A17 Pro chip, 128GB, Titanium design, Pro camera system', 1199.99, 20, 5, 9, true, '2025-05-10 11:20:00', '2025-05-10 11:20:00'),
(5, 'PHONE-SAMSUNG-S24-001', 'Samsung Galaxy S24', 'Snapdragon 8 Gen 3, 256GB, AI camera, 120Hz display', 899.99, 30, 5, NULL, true, '2025-05-15 10:00:00', '2025-05-15 10:00:00'),

-- Headphones
(6, 'AUDIO-SONY-WH1K-001', 'Sony WH-1000XM5', 'Premium noise-cancelling headphones, 30h battery, Hi-Res audio', 399.99, 40, 9, 10, true, '2025-05-15 14:30:00', '2025-05-15 14:30:00'),
(7, 'AUDIO-APPLE-APMAX-001', 'Apple AirPods Max', 'Over-ear headphones with spatial audio, active noise cancellation', 549.99, 12, 9, NULL, true, '2025-05-18 10:00:00', '2025-05-18 10:00:00'),

-- Accessories
(8, 'ACC-KEY-MECH-001', 'Mechanical Gaming Keyboard', 'RGB backlit, mechanical switches, programmable keys', 149.99, 50, 4, 11, true, '2025-05-20 09:45:00', '2025-05-20 09:45:00'),
(9, 'ACC-MOUSE-LG-MX3-001', 'Logitech MX Master 3', 'Wireless mouse, ergonomic design, precision scroll wheel', 99.99, 60, 4, 12, true, '2025-05-25 10:15:00', '2025-05-25 10:15:00'),
(10, 'ACC-WEBCAM-LOG-001', 'Logitech C920 HD Webcam', '1080p video, stereo audio, autofocus', 79.99, 35, 4, NULL, true, '2025-06-01 11:00:00', '2025-06-01 11:00:00'),

-- Low stock products
(11, 'LAP-ASUS-ROG-001', 'ASUS ROG Gaming Laptop', 'Intel i9, RTX 4070, 32GB RAM, 1TB SSD, 17" 240Hz display', 2799.99, 3, 7, NULL, true, '2025-06-05 10:00:00', '2025-06-05 10:00:00'),
(12, 'PHONE-GOOGLE-PIX8-001', 'Google Pixel 8 Pro', 'Google Tensor G3, 256GB, AI photography', 999.99, 2, 5, NULL, true, '2025-06-10 12:00:00', '2025-06-10 12:00:00');

-- 4.4 ADDRESSES
INSERT INTO addresses (address_id, customer_id, street, apartment, city, postal_code, country, is_default, address_type) VALUES
-- Magnus addresses
(1, 1, 'Storgatan 12', NULL, 'Stockholm', '11420', 'Sweden', true, 'BILLING'),
(2, 1, 'Storgatan 12', NULL, 'Stockholm', '11420', 'Sweden', true, 'SHIPPING'),
-- Anna addresses
(3, 2, 'Drottninggatan 45', 'Apt 3B', 'Göteborg', '41103', 'Sweden', true, 'BILLING'),
(4, 2, 'Drottninggatan 45', 'Apt 3B', 'Göteborg', '41103', 'Sweden', true, 'SHIPPING'),
-- Erik addresses
(5, 3, 'Kungsgatan 88', NULL, 'Malmö', '21145', 'Sweden', true, 'BILLING'),
(6, 3, 'Arbetargatan 23', NULL, 'Malmö', '21240', 'Sweden', true, 'SHIPPING'),
-- Sofia addresses
(7, 4, 'Vasagatan 10', 'Lgh 12', 'Uppsala', '75320', 'Sweden', true, 'BILLING'),
(8, 4, 'Vasagatan 10', 'Lgh 12', 'Uppsala', '75320', 'Sweden', true, 'SHIPPING');

-- 4.5 CARTS
INSERT INTO carts (cart_id, customer_id, session_id, created_date, updated_date, expires_at) VALUES
-- Active carts
(1, 1, NULL, '2025-11-20 10:00:00', '2025-11-22 09:30:00', '2025-12-22 09:30:00'),
(2, 3, NULL, '2025-11-21 14:00:00', '2025-11-22 08:00:00', '2025-12-22 08:00:00'),
-- Guest cart
(3, NULL, 'guest_session_abc123xyz', '2025-11-22 08:00:00', '2025-11-22 08:00:00', '2025-11-23 08:00:00');

-- 4.6 CART_ITEMS
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, price_at_add, added_date) VALUES
-- Magnus cart
(1, 1, 6, 1, 399.99, '2025-11-20 10:00:00'),
(2, 1, 9, 1, 99.99, '2025-11-21 15:00:00'),
-- Erik cart
(3, 2, 1, 1, 1599.99, '2025-11-21 14:00:00'),
(4, 2, 8, 1, 149.99, '2025-11-22 08:00:00'),
-- Guest cart
(5, 3, 4, 1, 1199.99, '2025-11-22 08:00:00');

-- 4.7 ORDERS
INSERT INTO orders (order_id, customer_id, order_number, order_status, total_amount, tax_amount, shipping_amount, discount_amount, billing_address_id, shipping_address_id, order_date, shipped_date, delivered_date, cancelled_date, notes) VALUES
-- Delivered orders
(1, 2, 'ORD-2025-0001', 'DELIVERED', 1749.98, 349.99, 49.00, 0.00, 3, 4, '2025-09-15 10:30:00', '2025-09-16 14:00:00', '2025-09-18 11:45:00', NULL, NULL),
(2, 3, 'ORD-2025-0002', 'DELIVERED', 479.98, 95.99, 29.00, 0.00, 5, 6, '2025-10-01 15:20:00', '2025-10-02 09:00:00', '2025-10-04 16:30:00', NULL, NULL),

-- Shipped orders
(3, 1, 'ORD-2025-0003', 'SHIPPED', 1699.98, 339.99, 49.00, 50.00, 1, 2, '2025-11-18 11:00:00', '2025-11-19 13:30:00', NULL, NULL, 'Customer requested express shipping'),
(4, 4, 'ORD-2025-0004', 'SHIPPED', 249.98, 49.99, 29.00, 0.00, 7, 8, '2025-11-20 09:45:00', '2025-11-21 10:15:00', NULL, NULL, NULL),

-- Processing orders
(5, 2, 'ORD-2025-0005', 'PROCESSING', 2549.98, 509.99, 0.00, 0.00, 3, 4, '2025-11-21 14:00:00', NULL, NULL, NULL, 'Free shipping promotion'),

-- Pending orders
(6, 3, 'ORD-2025-0006', 'PENDING', 899.99, 179.99, 29.00, 0.00, 5, 6, '2025-11-22 08:30:00', NULL, NULL, NULL, NULL),

-- Cancelled order
(7, 4, 'ORD-2025-0007', 'CANCELLED', 3349.97, 669.99, 0.00, 0.00, 7, 8, '2025-10-15 10:00:00', NULL, NULL, '2025-10-16 14:30:00', 'Customer changed mind');

-- 4.8 ORDER_ITEMS
INSERT INTO order_items (order_item_id, order_id, product_id, sku, product_name, quantity, unit_price, total_price, tax_rate) VALUES
-- Order 1 items
(1, 1, 1, 'LAP-DELL-XPS15-001', 'Dell XPS 15', 1, 1599.99, 1599.99, 0.25),
(2, 1, 8, 'ACC-KEY-MECH-001', 'Mechanical Gaming Keyboard', 1, 149.99, 149.99, 0.25),
-- Order 2 items
(3, 2, 6, 'AUDIO-SONY-WH1K-001', 'Sony WH-1000XM5', 1, 399.99, 399.99, 0.25),
(4, 2, 10, 'ACC-WEBCAM-LOG-001', 'Logitech C920 HD Webcam', 1, 79.99, 79.99, 0.25),
-- Order 3 items
(5, 3, 4, 'PHONE-APPLE-IP15P-001', 'Apple iPhone 15 Pro', 1, 1199.99, 1199.99, 0.25),
(6, 3, 6, 'AUDIO-SONY-WH1K-001', 'Sony WH-1000XM5', 1, 399.99, 399.99, 0.25),
(7, 3, 9, 'ACC-MOUSE-LG-MX3-001', 'Logitech MX Master 3', 1, 99.99, 99.99, 0.25),
-- Order 4 items
(8, 4, 8, 'ACC-KEY-MECH-001', 'Mechanical Gaming Keyboard', 1, 149.99, 149.99, 0.25),
(9, 4, 9, 'ACC-MOUSE-LG-MX3-001', 'Logitech MX Master 3', 1, 99.99, 99.99, 0.25),
-- Order 5 items
(10, 5, 2, 'LAP-APPLE-MBP14-001', 'Apple MacBook Pro 14"', 1, 2499.99, 2499.99, 0.25),
(11, 5, 10, 'ACC-WEBCAM-LOG-001', 'Logitech C920 HD Webcam', 1, 79.99, 79.99, 0.25),
-- Order 6 items
(12, 6, 5, 'PHONE-SAMSUNG-S24-001', 'Samsung Galaxy S24', 1, 899.99, 899.99, 0.25),
-- Order 7 items (cancelled)
(13, 7, 11, 'LAP-ASUS-ROG-001', 'ASUS ROG Gaming Laptop', 1, 2799.99, 2799.99, 0.25),
(14, 7, 6, 'AUDIO-SONY-WH1K-001', 'Sony WH-1000XM5', 1, 399.99, 399.99, 0.25),
(15, 7, 8, 'ACC-KEY-MECH-001', 'Mechanical Gaming Keyboard', 1, 149.99, 149.99, 0.25);

-- 4.9 PAYMENTS
INSERT INTO payments (payment_id, order_id, payment_method, payment_status, amount, transaction_id, payment_date, created_date, processed_date) VALUES
-- Completed payments
(1, 1, 'PAYPAL', 'COMPLETED', 1749.98, 'PAYPAL-TXN-001-2025', '2025-09-15 10:35:00', '2025-09-15 10:30:00', '2025-09-15 10:35:00'),
(2, 2, 'PAYPAL', 'COMPLETED', 479.98, 'PAYPAL-TXN-002-2025', '2025-10-01 15:25:00', '2025-10-01 15:20:00', '2025-10-01 15:25:00'),
(3, 3, 'PAYPAL', 'COMPLETED', 1699.98, 'PAYPAL-TXN-003-2025', '2025-11-18 11:05:00', '2025-11-18 11:00:00', '2025-11-18 11:05:00'),
(4, 4, 'PAYPAL', 'COMPLETED', 249.98, 'PAYPAL-TXN-004-2025', '2025-11-20 09:50:00', '2025-11-20 09:45:00', '2025-11-20 09:50:00'),
-- Processing payment
(5, 5, 'PAYPAL', 'PROCESSING', 2549.98, 'PAYPAL-TXN-005-2025', NULL, '2025-11-21 14:00:00', NULL),
-- Pending payment
(6, 6, 'PAYPAL', 'PENDING', 899.99, NULL, NULL, '2025-11-22 08:30:00', NULL),
-- Refunded payment (cancelled order)
(7, 7, 'PAYPAL', 'REFUNDED', 3349.97, 'PAYPAL-TXN-007-2025-REFUND', '2025-10-16 15:00:00', '2025-10-15 10:00:00', '2025-10-16 15:00:00');

-- 4.10 SHIPMENTS
INSERT INTO shipments (shipment_id, order_id, carrier, tracking_number, shipment_status, shipped_date, estimated_delivery_date, actual_delivery_date, recipient_name, recipient_phone, street, apartment, city, postal_code, country) VALUES
-- Delivered shipments
(1, 1, 'PostNord', 'PN-SE-2025-001234', 'DELIVERED', '2025-09-16 14:00:00', '2025-09-18 17:00:00', '2025-09-18 11:45:00', 'Anna Svensson', '+46709876543', 'Drottninggatan 45', 'Apt 3B', 'Göteborg', '41103', 'Sweden'),
(2, 2, 'DHL', 'DHL-SE-2025-567890', 'DELIVERED', '2025-10-02 09:00:00', '2025-10-04 17:00:00', '2025-10-04 16:30:00', 'Erik Andersson', '+46705551234', 'Arbetargatan 23', NULL, 'Malmö', '21240', 'Sweden'),

-- Shipped (in transit)
(3, 3, 'PostNord', 'PN-SE-2025-002345', 'SHIPPED', '2025-11-19 13:30:00', '2025-11-23 17:00:00', NULL, 'Magnus Berglund', '+46701234567', 'Storgatan 12', NULL, 'Stockholm', '11420', 'Sweden'),
(4, 4, 'DHL', 'DHL-SE-2025-678901', 'SHIPPED', '2025-11-21 10:15:00', '2025-11-24 17:00:00', NULL, 'Sofia Karlsson', '+46707778899', 'Vasagatan 10', 'Lgh 12', 'Uppsala', '75320', 'Sweden');

-- 4.11 SHIPMENT_TRACKING
INSERT INTO shipment_tracking (tracking_id, shipment_id, location, status, notes, timestamp) VALUES
-- Tracking for shipment 1 (delivered)
(1, 1, 'Stockholm Distribution Center', 'Package received', 'Package accepted at distribution center', '2025-09-16 14:00:00'),
(2, 1, 'Göteborg Distribution Center', 'In transit', 'Package arrived at destination city', '2025-09-17 08:30:00'),
(3, 1, 'Göteborg Local Depot', 'Out for delivery', 'Package loaded on delivery vehicle', '2025-09-18 07:00:00'),
(4, 1, 'Delivered', 'Delivered', 'Package delivered to recipient', '2025-09-18 11:45:00'),

-- Tracking for shipment 2 (delivered)
(5, 2, 'Stockholm Hub', 'Package received', 'Package accepted', '2025-10-02 09:00:00'),
(6, 2, 'Malmö Hub', 'In transit', 'Package in transit', '2025-10-03 14:20:00'),
(7, 2, 'Delivered', 'Delivered', 'Successfully delivered', '2025-10-04 16:30:00'),

-- Tracking for shipment 3 (in transit)
(8, 3, 'Stockholm Distribution Center', 'Package received', 'Package accepted for shipping', '2025-11-19 13:30:00'),
(9, 3, 'Stockholm Hub', 'In transit', 'Package sorted and ready for delivery route', '2025-11-20 08:00:00'),
(10, 3, 'Stockholm Local Depot', 'Out for delivery', 'Package on delivery vehicle', '2025-11-22 06:30:00'),

-- Tracking for shipment 4 (in transit)
(11, 4, 'Uppsala Distribution Center', 'Package received', 'Package received at distribution center', '2025-11-21 10:15:00'),
(12, 4, 'Uppsala Hub', 'In transit', 'Package processing', '2025-11-21 18:00:00');

-- 4.12 INVENTORY_TRANSACTIONS
INSERT INTO inventory_transactions (transaction_id, product_id, transaction_type, quantity, quantity_before, quantity_after, reference_type, reference_id, notes, created_date) VALUES
-- Initial stock
(1, 1, 'ADJUSTMENT', 20, 0, 20, 'INITIAL_STOCK', NULL, 'Initial inventory', '2025-05-01 10:00:00'),
(2, 4, 'ADJUSTMENT', 25, 0, 25, 'INITIAL_STOCK', NULL, 'Initial inventory', '2025-05-10 11:20:00'),
(3, 6, 'ADJUSTMENT', 50, 0, 50, 'INITIAL_STOCK', NULL, 'Initial inventory', '2025-05-15 14:30:00'),
(4, 8, 'ADJUSTMENT', 60, 0, 60, 'INITIAL_STOCK', NULL, 'Initial inventory', '2025-05-20 09:45:00'),
(5, 9, 'ADJUSTMENT', 70, 0, 70, 'INITIAL_STOCK', NULL, 'Initial inventory', '2025-05-25 10:15:00'),

-- Sales transactions (order 1)
(6, 1, 'SALE', -1, 20, 19, 'ORDER', 1, 'Sold via order ORD-2025-0001', '2025-09-15 10:35:00'),
(7, 8, 'SALE', -1, 60, 59, 'ORDER', 1, 'Sold via order ORD-2025-0001', '2025-09-15 10:35:00'),

-- Sales transactions (order 2)
(8, 6, 'SALE', -1, 50, 49, 'ORDER', 2, 'Sold via order ORD-2025-0002', '2025-10-01 15:25:00'),
(9, 10, 'SALE', -1, 40, 39, 'ORDER', 2, 'Sold via order ORD-2025-0002', '2025-10-01 15:25:00'),

-- Sales transactions (order 3)
(10, 4, 'SALE', -1, 25, 24, 'ORDER', 3, 'Sold via order ORD-2025-0003', '2025-11-18 11:05:00'),
(11, 6, 'SALE', -1, 49, 48, 'ORDER', 3, 'Sold via order ORD-2025-0003', '2025-11-18 11:05:00'),
(12, 9, 'SALE', -1, 70, 69, 'ORDER', 3, 'Sold via order ORD-2025-0003', '2025-11-18 11:05:00'),

-- Sales transactions (order 4)
(13, 8, 'SALE', -1, 59, 58, 'ORDER', 4, 'Sold via order ORD-2025-0004', '2025-11-20 09:50:00'),
(14, 9, 'SALE', -1, 69, 68, 'ORDER', 4, 'Sold via order ORD-2025-0004', '2025-11-20 09:50:00'),

-- Sales transactions (order 5)
(15, 2, 'SALE', -1, 10, 9, 'ORDER', 5, 'Sold via order ORD-2025-0005', '2025-11-21 14:00:00'),
(16, 10, 'SALE', -1, 39, 38, 'ORDER', 5, 'Sold via order ORD-2025-0005', '2025-11-21 14:00:00'),

-- Return transaction (cancelled order 7)
(17, 11, 'RETURN', 1, 2, 3, 'ORDER', 7, 'Return from cancelled order ORD-2025-0007', '2025-10-16 15:00:00'),
(18, 6, 'RETURN', 1, 48, 49, 'ORDER', 7, 'Return from cancelled order ORD-2025-0007', '2025-10-16 15:00:00'),
(19, 8, 'RETURN', 1, 58, 59, 'ORDER', 7, 'Return from cancelled order ORD-2025-0007', '2025-10-16 15:00:00'),

-- Stock adjustment
(20, 1, 'ADJUSTMENT', -4, 19, 15, 'ADJUSTMENT', NULL, 'Inventory correction', '2025-11-10 10:00:00'),
(21, 4, 'ADJUSTMENT', -4, 24, 20, 'ADJUSTMENT', NULL, 'Inventory correction', '2025-11-15 11:00:00');

-- Reset auto-increment
ALTER TABLE customers AUTO_INCREMENT = 6;
ALTER TABLE categories AUTO_INCREMENT = 11;
ALTER TABLE products AUTO_INCREMENT = 13;
ALTER TABLE addresses AUTO_INCREMENT = 9;
ALTER TABLE carts AUTO_INCREMENT = 4;
ALTER TABLE cart_items AUTO_INCREMENT = 6;
ALTER TABLE orders AUTO_INCREMENT = 8;
ALTER TABLE order_items AUTO_INCREMENT = 16;
ALTER TABLE payments AUTO_INCREMENT = 8;
ALTER TABLE shipments AUTO_INCREMENT = 5;
ALTER TABLE shipment_tracking AUTO_INCREMENT = 13;
ALTER TABLE inventory_transactions AUTO_INCREMENT = 22;

-- =====================================================
-- 5. VERIFY DATA
-- =====================================================

SELECT '=== DATABASE CREATED ===' as '';
SELECT DATABASE() as current_database;

SELECT '=== TABLES ===' as '';
SHOW TABLES;

SELECT '=== CUSTOMERS ===' as '';
SELECT customer_id, email, CONCAT(first_name, ' ', last_name) as name, phone, is_active, is_email_verified
FROM customers ORDER BY customer_id;

SELECT '=== CATEGORIES ===' as '';
SELECT category_id, name, slug, parent_category_id, display_order, is_active
FROM categories ORDER BY display_order, category_id;

SELECT '=== PRODUCTS ===' as '';
SELECT p.product_id, p.sku, p.name, p.price, p.stock_quantity, c.name as category, p.is_active
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
ORDER BY p.product_id;

SELECT '=== ORDERS ===' as '';
SELECT o.order_id, o.order_number, CONCAT(c.first_name, ' ', c.last_name) as customer, 
       o.order_status, o.total_amount, o.order_date
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
ORDER BY o.order_id;

SELECT '=== ACTIVE CARTS ===' as '';
SELECT c.cart_id, COALESCE(CONCAT(cu.first_name, ' ', cu.last_name), 'Guest') as customer,
       COUNT(ci.cart_item_id) as items, SUM(ci.price_at_add * ci.quantity) as total
FROM carts c
LEFT JOIN customers cu ON c.customer_id = cu.customer_id
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
GROUP BY c.cart_id, cu.first_name, cu.last_name
ORDER BY c.cart_id;

SELECT '=== SHIPMENTS ===' as '';
SELECT s.shipment_id, o.order_number, s.carrier, s.tracking_number, 
       s.shipment_status, s.shipped_date, s.estimated_delivery_date
FROM shipments s
JOIN orders o ON s.order_id = o.order_id
ORDER BY s.shipment_id;

SELECT '=== INVENTORY SUMMARY ===' as '';
SELECT p.product_id, p.sku, p.name, p.stock_quantity,
       CASE 
         WHEN p.stock_quantity = 0 THEN 'OUT_OF_STOCK'
         WHEN p.stock_quantity <= 5 THEN 'LOW_STOCK'
         ELSE 'IN_STOCK'
       END as stock_status
FROM products p
ORDER BY p.stock_quantity ASC, p.product_id;

SELECT '=== SETUP COMPLETE ===' as '';
SELECT 
    CONCAT('Customers: ', (SELECT COUNT(*) FROM customers)) as customers,
    CONCAT('Products: ', (SELECT COUNT(*) FROM products)) as products,
    CONCAT('Orders: ', (SELECT COUNT(*) FROM orders)) as orders,
    CONCAT('Active Carts: ', (SELECT COUNT(*) FROM carts WHERE expires_at > NOW())) as active_carts,
    CONCAT('Total Sales: ', ROUND((SELECT SUM(total_amount) FROM orders WHERE order_status IN ('DELIVERED', 'SHIPPED', 'PROCESSING')), 2)) as total_sales;