-- Shop Service - H2 Test Data
-- COMPLETE VERSION - Generated from actual Hibernate schema output
-- All column names match EXACTLY what Hibernate creates

-- ============================================================================
-- CATEGORIES
-- ============================================================================
INSERT INTO categories (category_id, name, slug, description, parent_id, is_active, created_at, updated_at) VALUES
(1, 'Electronics', 'electronics', 'Electronic devices and accessories', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Laptops', 'laptops', 'Laptop computers', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Phones', 'phones', 'Smartphones and mobile devices', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Accessories', 'accessories', 'Electronic accessories', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- PRODUCTS
-- ============================================================================
INSERT INTO products (product_id, name, slug, description, sku, price, discount_price, stock_quantity, weight, dimensions, category_id, is_active, is_featured, created_at, updated_at) VALUES
(1, 'MacBook Pro 16"', 'macbook-pro-16', 'Powerful laptop with M3 chip', 'LAPTOP-001', 2499.00, NULL, 15, 2.1, '35.79 x 24.81 x 1.68 cm', 2, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'iPhone 15 Pro', 'iphone-15-pro', 'Latest iPhone with titanium design', 'PHONE-001', 1199.00, NULL, 50, 0.221, '14.67 x 7.08 x 0.83 cm', 3, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'AirPods Pro', 'airpods-pro', 'Wireless earbuds with noise cancellation', 'ACC-001', 249.00, NULL, 100, 0.056, '4.5 x 6.0 x 2.1 cm', 4, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Dell XPS 15', 'dell-xps-15', 'Premium Windows laptop', 'LAPTOP-002', 1799.00, NULL, 20, 1.8, '34.46 x 23.05 x 1.80 cm', 2, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Samsung Galaxy S24', 'samsung-galaxy-s24', 'Android flagship phone', 'PHONE-002', 999.00, NULL, 35, 0.196, '14.7 x 7.06 x 0.76 cm', 3, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- CUSTOMERS (password: password123)
-- ============================================================================
INSERT INTO customers (customer_id, email, password_hash, first_name, last_name, phone, role, active, email_verified, newsletter_subscribed, marketing_consent, failed_login_attempts, created_date, updated_date) VALUES
(1, 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', '+46701234567', 'CUSTOMER', true, true, true, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', '+46709876543', 'CUSTOMER', true, true, false, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'bob@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'Johnson', '+46705551234', 'CUSTOMER', true, true, true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- ADDRESSES
-- ============================================================================
INSERT INTO addresses (address_id, customer_id, street, apartment, city, state, postal_code, country, address_type, is_default) VALUES
(1, 1, 'Storgatan 1', NULL, 'Stockholm', 'Stockholm', '11420', 'Sweden', 'BILLING', true),
(2, 1, 'Storgatan 1', NULL, 'Stockholm', 'Stockholm', '11420', 'Sweden', 'SHIPPING', true),
(3, 2, 'Drottninggatan 50', NULL, 'Göteborg', 'Västra Götaland', '41107', 'Sweden', 'BILLING', true),
(4, 2, 'Drottninggatan 50', NULL, 'Göteborg', 'Västra Götaland', '41107', 'Sweden', 'SHIPPING', true);

-- ============================================================================
-- ORDERS
-- ============================================================================
INSERT INTO orders (order_id, customer_id, order_number, order_date, order_status, subtotal, tax_amount, shipping_amount, discount_amount, total_amount, currency, payment_status, billing_address_line1, billing_city, billing_state, billing_postal_code, billing_country, shipping_first_name, shipping_last_name, shipping_email, shipping_phone, shipping_address_line1, shipping_city, shipping_state, shipping_postal_code, shipping_country, created_date, updated_date) VALUES
(1, 1, 'ORD-2025-001', CURRENT_TIMESTAMP, 'DELIVERED', 2499.00, 200.00, 49.00, 0.00, 2748.00, 'SEK', 'COMPLETED', 'Storgatan 1', 'Stockholm', 'Stockholm', '11420', 'Sweden', 'John', 'Doe', 'john@example.com', '+46701234567', 'Storgatan 1', 'Stockholm', 'Stockholm', '11420', 'SE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 'ORD-2025-002', CURRENT_TIMESTAMP, 'PROCESSING', 1199.00, 200.00, 49.00, 0.00, 1448.00, 'SEK', 'PENDING', 'Drottninggatan 50', 'Göteborg', 'Västra Götaland', '41107', 'Sweden', 'Jane', 'Smith', 'jane@example.com', '+46709876543', 'Drottninggatan 50', 'Göteborg', 'Västra Götaland', '41107', 'SE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'ORD-2025-003', CURRENT_TIMESTAMP, 'PENDING', 249.00, 49.00, 0.00, 0.00, 298.00, 'SEK', 'PENDING', 'Storgatan 1', 'Stockholm', 'Stockholm', '11420', 'Sweden', 'John', 'Doe', 'john@example.com', '+46701234567', 'Storgatan 1', 'Stockholm', 'Stockholm', '11420', 'SE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- ORDER ITEMS
-- ============================================================================
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_sku, quantity, unit_price, discount_amount, subtotal, item_status, created_at, updated_at) VALUES
(1, 1, 1, 'MacBook Pro 16"', 'LAPTOP-001', 1, 2499.00, 0.00, 2499.00, 'DELIVERED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 2, 'iPhone 15 Pro', 'PHONE-001', 1, 1199.00, 0.00, 1199.00, 'PROCESSING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 3, 'AirPods Pro', 'ACC-001', 1, 249.00, 0.00, 249.00, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- PAYMENTS
-- ============================================================================
INSERT INTO payments (payment_id, order_id, payment_method, payment_status, amount, currency, transaction_id, gateway_payment_id, payment_date, created_at, updated_at) VALUES
(1, 1, 'CREDIT_CARD', 'COMPLETED', 2748.00, 'SEK', 'TXN-001-TEST', 'STRIPE-TEST-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 'PAYPAL', 'PENDING', 1448.00, 'SEK', 'TXN-002-TEST', 'PAYPAL-TEST-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 'CREDIT_CARD', 'PENDING', 298.00, 'SEK', 'TXN-003-TEST', 'STRIPE-TEST-003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- CARTS
-- ============================================================================
INSERT INTO carts (cart_id, customer_id, item_count, total_amount, discount_amount, is_saved, created_at, updated_at) VALUES
(1, 3, 3, 3597.00, 0.00, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- CART ITEMS
-- ============================================================================
INSERT INTO cart_items (cart_item_id, cart_id, product_id, product_name, product_sku, quantity, unit_price, subtotal, added_at, updated_at) VALUES
(1, 1, 4, 'Dell XPS 15', 'LAPTOP-002', 1, 1799.00, 1799.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 5, 'Samsung Galaxy S24', 'PHONE-002', 2, 999.00, 1998.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- SHIPMENTS
-- ============================================================================
INSERT INTO shipments (shipment_id, order_id, tracking_number, carrier, shipping_method, shipment_status, shipping_street, shipping_city, shipping_state, shipping_postal_code, shipping_country, recipient_name, recipient_email, recipient_phone, shipped_date, estimated_delivery_date, delivered_date, created_at, last_updated) VALUES
(1, 1, 'TRACK-001-TEST', 'DHL', 'STANDARD', 'DELIVERED', 'Storgatan 1', 'Stockholm', 'Stockholm', '11420', 'Sweden', 'John Doe', 'john@example.com', '+46701234567', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- INVENTORY TRANSACTIONS
-- ============================================================================
INSERT INTO inventory_transactions (inventory_transaction_id, product_id, transaction_type, quantity_change, quantity_before, quantity_after, reference_id, transaction_date) VALUES
(1, 1, 'SALE', -1, 16, 15, '1', CURRENT_TIMESTAMP),
(2, 2, 'SALE', -1, 51, 50, '2', CURRENT_TIMESTAMP),
(3, 3, 'SALE', -1, 101, 100, '3', CURRENT_TIMESTAMP);

-- ============================================================================
-- NOTES
-- ============================================================================
-- All customer passwords are 'password123' hashed with BCrypt
-- Login with: email='john@example.com', password='password123'
-- Or: email='jane@example.com', password='password123'
-- Or: email='bob@example.com', password='password123'
