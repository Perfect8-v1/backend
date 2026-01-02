-- =====================================================
-- SHOP-SERVICE KOMPLETT SETUP (FIXED)
-- Skapar: databas, tabeller, mock-data
-- Database: shopDB
-- FIX: default_address (not is_default), added state column
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

-- 3.4 ADDRESSES (FIXED: default_address, added state)
CREATE TABLE addresses (
    address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    apartment VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    default_address BOOLEAN NOT NULL DEFAULT FALSE,
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