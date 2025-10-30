-- ============================================
-- Perfect8 Shop Service - Mock Data v1.0
-- Baserad på Feature_Map_v1.0.md
-- Använder *Date suffix (INTE *At)
-- ============================================

USE perfect8_shop;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data (optional - comment out if you want to keep existing data)
-- TRUNCATE TABLE inventory_transactions;
-- TRUNCATE TABLE order_items;
-- TRUNCATE TABLE payments;
-- TRUNCATE TABLE orders;
-- TRUNCATE TABLE cart_items;
-- TRUNCATE TABLE carts;
-- TRUNCATE TABLE addresses;
-- TRUNCATE TABLE customers;
-- TRUNCATE TABLE products;
-- TRUNCATE TABLE categories;

-- ============================================
-- 1. CATEGORIES
-- ============================================
INSERT INTO categories (category_id, name, slug, description, parent_category_id, display_order, is_active) VALUES
(1, 'Electronics', 'electronics', 'Electronic devices and accessories', NULL, 1, true),
(2, 'Laptops', 'laptops', 'Portable computers', 1, 1, true),
(3, 'Smartphones', 'smartphones', 'Mobile phones', 1, 2, true),
(4, 'Accessories', 'accessories', 'Electronic accessories', 1, 3, true),
(5, 'Clothing', 'clothing', 'Fashion and apparel', NULL, 2, true),
(6, 'Men', 'men', 'Men''s clothing', 5, 1, true),
(7, 'Women', 'women', 'Women''s clothing', 5, 2, true),
(8, 'Home & Garden', 'home-garden', 'Home improvement and garden', NULL, 3, true),
(9, 'Books', 'books', 'Physical and digital books', NULL, 4, true),
(10, 'Sports', 'sports', 'Sports equipment and gear', NULL, 5, true);

-- ============================================
-- 2. PRODUCTS
-- ============================================
INSERT INTO products (product_id, sku, name, description, price, stock_quantity, category_id, image_id, is_active, created_date, updated_date) VALUES
-- Electronics > Laptops
(1, 'LAPTOP-001', 'MacBook Pro 16"', 'Professional laptop with M3 chip, 16GB RAM, 512GB SSD', 2499.00, 15, 2, NULL, true, NOW(), NOW()),
(2, 'LAPTOP-002', 'Dell XPS 15', 'High-performance laptop, Intel i7, 16GB RAM, 1TB SSD', 1899.00, 20, 2, NULL, true, NOW(), NOW()),
(3, 'LAPTOP-003', 'ThinkPad X1 Carbon', 'Business ultrabook, Intel i5, 8GB RAM, 256GB SSD', 1299.00, 25, 2, NULL, true, NOW(), NOW()),

-- Electronics > Smartphones
(4, 'PHONE-001', 'iPhone 15 Pro', 'Latest iPhone with A17 chip, 256GB storage', 1199.00, 30, 3, NULL, true, NOW(), NOW()),
(5, 'PHONE-002', 'Samsung Galaxy S24', 'Flagship Android phone, 128GB storage', 899.00, 40, 3, NULL, true, NOW(), NOW()),
(6, 'PHONE-003', 'Google Pixel 8', 'Pure Android experience, 128GB storage', 699.00, 35, 3, NULL, true, NOW(), NOW()),

-- Electronics > Accessories
(7, 'ACC-001', 'Wireless Mouse', 'Logitech MX Master 3 wireless mouse', 99.00, 100, 4, NULL, true, NOW(), NOW()),
(8, 'ACC-002', 'Mechanical Keyboard', 'RGB mechanical gaming keyboard', 149.00, 75, 4, NULL, true, NOW(), NOW()),
(9, 'ACC-003', 'USB-C Hub', '7-in-1 USB-C hub with HDMI and card readers', 49.00, 150, 4, NULL, true, NOW(), NOW()),
(10, 'ACC-004', 'Wireless Headphones', 'Sony WH-1000XM5 noise cancelling', 399.00, 50, 4, NULL, true, NOW(), NOW()),

-- Clothing > Men
(11, 'CLOTH-M-001', 'Cotton T-Shirt', 'Premium cotton t-shirt, available in multiple colors', 29.00, 200, 6, NULL, true, NOW(), NOW()),
(12, 'CLOTH-M-002', 'Denim Jeans', 'Classic fit denim jeans', 79.00, 150, 6, NULL, true, NOW(), NOW()),
(13, 'CLOTH-M-003', 'Hoodie', 'Comfortable cotton blend hoodie', 59.00, 100, 6, NULL, true, NOW(), NOW()),

-- Clothing > Women
(14, 'CLOTH-W-001', 'Summer Dress', 'Floral pattern summer dress', 69.00, 80, 7, NULL, true, NOW(), NOW()),
(15, 'CLOTH-W-002', 'Yoga Pants', 'High-waist athletic yoga pants', 49.00, 120, 7, NULL, true, NOW(), NOW()),

-- Books
(16, 'BOOK-001', 'Clean Code', 'Robert C. Martin - Software engineering classic', 45.00, 50, 9, NULL, true, NOW(), NOW()),
(17, 'BOOK-002', 'Design Patterns', 'Gang of Four design patterns book', 55.00, 40, 9, NULL, true, NOW(), NOW()),

-- Sports
(18, 'SPORT-001', 'Yoga Mat', 'Non-slip exercise yoga mat', 39.00, 80, 10, NULL, true, NOW(), NOW()),
(19, 'SPORT-002', 'Dumbbell Set', '20kg adjustable dumbbell set', 89.00, 30, 10, NULL, true, NOW(), NOW()),
(20, 'SPORT-003', 'Running Shoes', 'Professional running shoes for athletes', 129.00, 60, 10, NULL, true, NOW(), NOW());

-- ============================================
-- 3. CUSTOMERS
-- Password: "password123" (BCrypt hash)
-- ============================================
INSERT INTO customers (customer_id, email, password_hash, first_name, last_name, phone, is_active, is_email_verified, email_verification_token, reset_password_token, reset_password_token_expiry, failed_login_attempts, account_locked_until, created_date, updated_date, last_login_date) VALUES
(1, 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', '+46701234567', true, true, NULL, NULL, NULL, 0, NULL, NOW(), NOW(), NOW()),
(2, 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', '+46702345678', true, true, NULL, NULL, NULL, 0, NULL, NOW(), NOW(), NOW()),
(3, 'bob.johnson@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'Johnson', '+46703456789', true, true, NULL, NULL, NULL, 0, NULL, NOW(), NOW(), NULL),
(4, 'alice.williams@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice', 'Williams', '+46704567890', true, false, 'verify_token_123', NULL, NULL, 0, NULL, NOW(), NOW(), NULL),
(5, 'test.user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test', 'User', '+46705678901', true, true, NULL, NULL, NULL, 0, NULL, NOW(), NOW(), NOW());

-- ============================================
-- 4. ADDRESSES
-- ============================================
INSERT INTO addresses (address_id, customer_id, street, apartment, city, postal_code, country, is_default, address_type) VALUES
-- John Doe addresses
(1, 1, 'Kungsgatan 10', NULL, 'Stockholm', '11143', 'Sweden', true, 'BILLING'),
(2, 1, 'Drottninggatan 25', 'Apt 3B', 'Stockholm', '11151', 'Sweden', true, 'SHIPPING'),

-- Jane Smith addresses
(3, 2, 'Vasagatan 15', NULL, 'Gothenburg', '41124', 'Sweden', true, 'BILLING'),
(4, 2, 'Vasagatan 15', NULL, 'Gothenburg', '41124', 'Sweden', true, 'SHIPPING'),

-- Bob Johnson addresses
(5, 3, 'Storgatan 5', 'Floor 2', 'Malmö', '21122', 'Sweden', true, 'BILLING'),
(6, 3, 'Södergatan 12', NULL, 'Malmö', '21134', 'Sweden', true, 'SHIPPING'),

-- Alice Williams addresses
(7, 4, 'Strandvägen 7', NULL, 'Stockholm', '11456', 'Sweden', true, 'BILLING'),
(8, 4, 'Strandvägen 7', NULL, 'Stockholm', '11456', 'Sweden', true, 'SHIPPING'),

-- Test User addresses
(9, 5, 'Testgatan 1', NULL, 'Uppsala', '75320', 'Sweden', true, 'BILLING'),
(10, 5, 'Testgatan 1', NULL, 'Uppsala', '75320', 'Sweden', true, 'SHIPPING');

-- ============================================
-- 5. CARTS (Some active shopping carts)
-- ============================================
INSERT INTO carts (cart_id, customer_id, session_id, created_date, updated_date, expires_at) VALUES
(1, 1, NULL, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(2, 3, NULL, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(3, NULL, 'guest_session_abc123', NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

-- ============================================
-- 6. CART ITEMS
-- VIKTIG: addedDate (INTE addedAt)
-- ============================================
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, price_at_add, added_date) VALUES
-- John's cart
(1, 1, 4, 1, 1199.00, NOW()),  -- iPhone 15 Pro
(2, 1, 7, 1, 99.00, NOW()),    -- Wireless Mouse

-- Bob's cart
(3, 2, 2, 1, 1899.00, NOW()),  -- Dell XPS 15
(4, 2, 8, 1, 149.00, NOW()),   -- Mechanical Keyboard
(5, 2, 10, 1, 399.00, NOW()),  -- Wireless Headphones

-- Guest cart
(6, 3, 11, 3, 29.00, NOW()),   -- Cotton T-Shirts (3x)
(7, 3, 18, 1, 39.00, NOW());   -- Yoga Mat

-- ============================================
-- 7. ORDERS
-- ============================================
INSERT INTO orders (order_id, customer_id, order_number, order_status, total_amount, tax_amount, shipping_amount, discount_amount, billing_address_id, shipping_address_id, order_date, shipped_date, delivered_date, cancelled_date, notes) VALUES
-- Completed orders
(1, 1, 'ORD-2025-001', 'DELIVERED', 1348.00, 270.00, 50.00, 0.00, 1, 2, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), NULL, 'First order - MacBook Pro'),
(2, 2, 'ORD-2025-002', 'DELIVERED', 954.00, 180.00, 50.00, 0.00, 3, 4, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), NULL, NULL),

-- Shipped orders
(3, 1, 'ORD-2025-003', 'SHIPPED', 248.00, 50.00, 50.00, 0.00, 1, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, NULL, 'Accessories order'),

-- Processing orders
(4, 3, 'ORD-2025-004', 'PROCESSING', 2447.00, 490.00, 50.00, 0.00, 5, 6, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, NULL, NULL, 'Dell XPS + peripherals'),

-- Pending orders
(5, 4, 'ORD-2025-005', 'PENDING', 148.00, 30.00, 50.00, 0.00, 7, 8, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL, NULL, NULL),

-- Cancelled order
(6, 2, 'ORD-2025-006', 'CANCELLED', 129.00, 26.00, 50.00, 0.00, 3, 4, DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, NULL, DATE_SUB(NOW(), INTERVAL 9 DAY), 'Customer requested cancellation');

-- ============================================
-- 8. ORDER ITEMS
-- ============================================
INSERT INTO order_items (order_item_id, order_id, product_id, sku, product_name, quantity, unit_price, total_price, tax_rate) VALUES
-- Order 1 (John - Delivered)
(1, 1, 1, 'LAPTOP-001', 'MacBook Pro 16"', 1, 2499.00, 2499.00, 0.25),

-- Order 2 (Jane - Delivered)
(2, 2, 5, 'PHONE-002', 'Samsung Galaxy S24', 1, 899.00, 899.00, 0.25),

-- Order 3 (John - Shipped)
(3, 3, 7, 'ACC-001', 'Wireless Mouse', 1, 99.00, 99.00, 0.25),
(4, 3, 9, 'ACC-003', 'USB-C Hub', 1, 49.00, 49.00, 0.25),

-- Order 4 (Bob - Processing)
(5, 4, 2, 'LAPTOP-002', 'Dell XPS 15', 1, 1899.00, 1899.00, 0.25),
(6, 4, 8, 'ACC-002', 'Mechanical Keyboard', 1, 149.00, 149.00, 0.25),
(7, 4, 10, 'ACC-004', 'Wireless Headphones', 1, 399.00, 399.00, 0.25),

-- Order 5 (Alice - Pending)
(8, 5, 11, 'CLOTH-M-001', 'Cotton T-Shirt', 2, 29.00, 58.00, 0.25),
(9, 5, 18, 'SPORT-001', 'Yoga Mat', 1, 39.00, 39.00, 0.25),

-- Order 6 (Jane - Cancelled)
(10, 6, 20, 'SPORT-003', 'Running Shoes', 1, 129.00, 129.00, 0.25);

-- ============================================
-- 9. PAYMENTS
-- ============================================
INSERT INTO payments (payment_id, order_id, payment_method, payment_status, amount, transaction_id, payment_date, created_date, processed_date) VALUES
(1, 1, 'PAYPAL', 'COMPLETED', 1348.00, 'PAYPAL-TXN-001', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2, 2, 'PAYPAL', 'COMPLETED', 954.00, 'PAYPAL-TXN-002', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
(3, 3, 'PAYPAL', 'COMPLETED', 248.00, 'PAYPAL-TXN-003', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 4, 'PAYPAL', 'COMPLETED', 2447.00, 'PAYPAL-TXN-004', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 5, 'PAYPAL', 'PENDING', 148.00, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),
(6, 6, 'PAYPAL', 'REFUNDED', 129.00, 'PAYPAL-TXN-006', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY));

-- ============================================
-- 10. SHIPMENTS
-- ============================================
INSERT INTO shipments (shipment_id, order_id, carrier, tracking_number, shipment_status, shipped_date, estimated_delivery_date, actual_delivery_date, recipient_name, recipient_phone, street, apartment, city, postal_code, country) VALUES
(1, 1, 'PostNord', 'PN123456789SE', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), 'John Doe', '+46701234567', 'Drottninggatan 25', 'Apt 3B', 'Stockholm', '11151', 'Sweden'),
(2, 2, 'DHL', 'DHL987654321SE', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), 'Jane Smith', '+46702345678', 'Vasagatan 15', NULL, 'Gothenburg', '41124', 'Sweden'),
(3, 3, 'PostNord', 'PN111222333SE', 'SHIPPED', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), NULL, 'John Doe', '+46701234567', 'Drottninggatan 25', 'Apt 3B', 'Stockholm', '11151', 'Sweden');

-- ============================================
-- 11. INVENTORY TRANSACTIONS
-- ============================================
INSERT INTO inventory_transactions (transaction_id, product_id, transaction_type, quantity, quantity_before, quantity_after, reference_type, reference_id, notes, created_date) VALUES
-- Initial stock setup
(1, 1, 'ADJUSTMENT', 15, 0, 15, 'INITIAL_STOCK', NULL, 'Initial inventory setup', DATE_SUB(NOW(), INTERVAL 60 DAY)),
(2, 2, 'ADJUSTMENT', 20, 0, 20, 'INITIAL_STOCK', NULL, 'Initial inventory setup', DATE_SUB(NOW(), INTERVAL 60 DAY)),
(3, 4, 'ADJUSTMENT', 30, 0, 30, 'INITIAL_STOCK', NULL, 'Initial inventory setup', DATE_SUB(NOW(), INTERVAL 60 DAY)),

-- Sales (reducing stock)
(4, 1, 'SALE', -1, 15, 14, 'ORDER', 1, 'Sold via order ORD-2025-001', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(5, 5, 'SALE', -1, 40, 39, 'ORDER', 2, 'Sold via order ORD-2025-002', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(6, 7, 'SALE', -1, 100, 99, 'ORDER', 3, 'Sold via order ORD-2025-003', DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Return (increasing stock)
(7, 20, 'RETURN', 1, 60, 61, 'ORDER', 6, 'Returned from cancelled order', DATE_SUB(NOW(), INTERVAL 9 DAY));

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Run these to verify data was inserted correctly:

-- SELECT COUNT(*) as category_count FROM categories;
-- SELECT COUNT(*) as product_count FROM products;
-- SELECT COUNT(*) as customer_count FROM customers;
-- SELECT COUNT(*) as address_count FROM addresses;
-- SELECT COUNT(*) as cart_count FROM carts;
-- SELECT COUNT(*) as cart_item_count FROM cart_items;
-- SELECT COUNT(*) as order_count FROM orders;
-- SELECT COUNT(*) as order_item_count FROM order_items;
-- SELECT COUNT(*) as payment_count FROM payments;
-- SELECT COUNT(*) as shipment_count FROM shipments;
-- SELECT COUNT(*) as transaction_count FROM inventory_transactions;

-- ============================================
-- TEST CREDENTIALS FOR JONATAN
-- ============================================
-- Email: john.doe@example.com
-- Password: password123
--
-- Email: jane.smith@example.com
-- Password: password123
--
-- Email: test.user@example.com
-- Password: password123
--
-- All users can login with password: password123
-- ============================================
