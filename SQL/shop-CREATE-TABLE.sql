-- ================================================
-- shop-CREATE-TABLE.sql
-- Database: shopDB
-- Created: 2025-11-09
-- Purpose: Create tables for shop-service (e-commerce core)
-- 
-- IMPORTANT NOTES:
-- - Boolean fields: Java isActive → MySQL active (Hibernate removes "is" prefix)
-- - Boolean fields: Java defaultAddress → MySQL default_address (clean, no reserved words)
-- - ID fields: [entityName]Id → Hibernate maps to [entity_name]_id
-- - Date fields: *Date not *At (Magnum Opus compliance)
-- ================================================

-- ================================================
-- Table: categories
-- Purpose: Product categories with hierarchical structure
-- Notes: Self-referencing for parent/child relationships
-- ================================================

CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id BIGINT,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,  -- Java: isActive → MySQL: active
    
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id),
    INDEX idx_slug (slug),
    INDEX idx_parent_category_id (parent_category_id),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: products
-- Purpose: Product catalog with pricing and inventory
-- Notes: Links to categories and images (image_id is FK to image-service)
-- ================================================

CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category_id BIGINT,
    image_id BIGINT,  -- FK to image-service (not enforced cross-database)
    active BOOLEAN NOT NULL DEFAULT TRUE,  -- Java: isActive → MySQL: active
    created_date DATETIME(6),
    updated_date DATETIME(6),
    
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_sku (sku),
    INDEX idx_category_id (category_id),
    INDEX idx_active (active),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: customers
-- Purpose: Customer accounts with authentication and verification
-- Notes: Email verification and password reset tokens with expiry
-- ================================================

CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,  -- Java: isActive → MySQL: active
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,  -- Java: isEmailVerified → MySQL: email_verified
    email_verification_token VARCHAR(255),
    email_verified_date DATETIME(6),
    email_verification_sent_date DATETIME(6),
    reset_password_token VARCHAR(255),
    reset_password_token_expiry DATETIME(6),
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked_until DATETIME(6),
    created_date DATETIME(6),
    updated_date DATETIME(6),
    last_login_date DATETIME(6),
    
    INDEX idx_email (email),
    INDEX idx_email_verification_token (email_verification_token),
    INDEX idx_reset_password_token (reset_password_token),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: addresses
-- Purpose: Customer addresses for billing and shipping
-- Notes: Java defaultAddress → MySQL default_address (clean, no reserved words!)
-- ================================================

CREATE TABLE IF NOT EXISTS addresses (
    address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    apartment VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    default_address BOOLEAN NOT NULL DEFAULT FALSE,  -- Java: defaultAddress → MySQL: default_address
    address_type VARCHAR(50) NOT NULL,  -- BILLING or SHIPPING
    
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_default_address (default_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: carts
-- Purpose: Shopping carts (both logged-in customers and guests)
-- Notes: customer_id nullable for guest carts (identified by session_id)
-- ================================================

CREATE TABLE IF NOT EXISTS carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,  -- Nullable for guest carts
    session_id VARCHAR(255),  -- For guest cart identification
    created_date DATETIME(6),
    updated_date DATETIME(6),
    expires_at DATETIME(6),  -- Auto-cleanup of abandoned carts
    
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_session_id (session_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: cart_items
-- Purpose: Items in shopping cart
-- Notes: price_at_add is snapshot to handle price changes
-- ================================================

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price_at_add DECIMAL(10, 2) NOT NULL,  -- Price snapshot when added
    added_date DATETIME(6),
    updated_date DATETIME(6),
    stock_checked_date DATETIME(6),  -- Last time stock was verified
    
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_cart_id (cart_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: orders
-- Purpose: Customer orders (converted from carts)
-- Notes: Amounts are snapshots, addresses by reference to maintain history
-- ================================================

CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,  -- Human-readable order number
    order_status VARCHAR(50) NOT NULL,  -- Enum: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    total_amount DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    billing_address_id BIGINT NOT NULL,
    shipping_address_id BIGINT NOT NULL,
    order_date DATETIME(6),
    shipped_date DATETIME(6),
    delivered_date DATETIME(6),
    cancelled_date DATETIME(6),
    notes TEXT,
    
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (billing_address_id) REFERENCES addresses(address_id),
    FOREIGN KEY (shipping_address_id) REFERENCES addresses(address_id),
    INDEX idx_order_number (order_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_order_status (order_status),
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: order_items
-- Purpose: Items in an order (snapshot of product data at purchase)
-- Notes: All product details are snapshots to handle product changes
-- ================================================

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,  -- Snapshot
    product_name VARCHAR(255) NOT NULL,  -- Snapshot
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,  -- Snapshot
    total_price DECIMAL(10, 2) NOT NULL,  -- Calculated: quantity * unit_price
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: payments
-- Purpose: Payment transactions (PayPal, Stripe, etc.)
-- Notes: transaction_id from payment provider for tracking
-- ================================================

CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,  -- Enum: PAYPAL, STRIPE, CARD
    payment_status VARCHAR(50) NOT NULL,  -- Enum: PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED
    amount DECIMAL(10, 2) NOT NULL,
    transaction_id VARCHAR(255),  -- From payment provider (PayPal/Stripe)
    payment_date DATETIME(6),
    created_date DATETIME(6),
    processed_date DATETIME(6),
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX idx_order_id (order_id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: shipments
-- Purpose: Shipment information with carrier tracking
-- Notes: Address fields duplicated for historical record (address might be deleted)
-- ================================================

CREATE TABLE IF NOT EXISTS shipments (
    shipment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    carrier VARCHAR(100),  -- DHL, PostNord, UPS, etc.
    tracking_number VARCHAR(100),
    shipment_status VARCHAR(50) NOT NULL,  -- PENDING, SHIPPED, DELIVERED
    shipped_date DATETIME(6),
    estimated_delivery_date DATETIME(6),
    actual_delivery_date DATETIME(6),
    recipient_name VARCHAR(200) NOT NULL,
    recipient_phone VARCHAR(20),
    street VARCHAR(255) NOT NULL,
    apartment VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX idx_order_id (order_id),
    INDEX idx_tracking_number (tracking_number),
    INDEX idx_shipment_status (shipment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: shipment_tracking
-- Purpose: Shipment tracking events (location updates)
-- Notes: Timeline of shipment progress
-- ================================================

CREATE TABLE IF NOT EXISTS shipment_tracking (
    tracking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    location VARCHAR(255),
    status VARCHAR(100) NOT NULL,
    notes TEXT,
    timestamp DATETIME(6) NOT NULL,
    
    FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    INDEX idx_shipment_id (shipment_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Table: inventory_transactions
-- Purpose: Audit trail of inventory movements
-- Notes: Tracks all stock changes (sales, returns, adjustments)
-- ================================================

CREATE TABLE IF NOT EXISTS inventory_transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,  -- Enum: SALE, RETURN, ADJUSTMENT, DAMAGED
    quantity INT NOT NULL,  -- Positive or negative
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reference_type VARCHAR(50),  -- ORDER, RETURN, ADJUSTMENT
    reference_id BIGINT,  -- ID of related entity (order_id, return_id, etc.)
    notes TEXT,
    created_date DATETIME(6),
    
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_product_id (product_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_created_date (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- End of shop-CREATE-TABLE.sql
-- 
-- QUICK REFERENCE:
-- - 12 tables total
-- - Foreign keys respect deletion cascades where appropriate
-- - All indexes optimized for common queries
-- - Boolean fields follow Hibernate naming (remove "is" prefix)
-- - Date fields use *Date suffix (Magnum Opus compliance)
-- - NO reserved words used - clean and portable SQL
-- ================================================
