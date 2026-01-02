-- =====================================================
-- 4. MOCK DATA
-- =====================================================

-- Lösenord för alla kunder: password123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi

-- 4.1 CUSTOMERS (Simplified - references admin-service via user_id)
INSERT INTO customers (
    customer_id, user_id, first_name, last_name, email, phone,
    is_active, is_email_verified, email_verified_date,
    created_date, updated_date, last_login_date,
    newsletter_subscribed, marketing_consent, preferred_language, preferred_currency
) VALUES
(1, 1, 'Magnus', 'Berglund', 'magnus@perfect8.com', '+46701234567',
 true, true, '2025-01-01 08:30:00',
 '2025-01-01 08:00:00', '2025-01-01 08:00:00', '2025-11-22 09:00:00',
 true, true, 'sv', 'SEK'),
 
(2, 2, 'Anna', 'Svensson', 'anna.svensson@email.com', '+46709876543',
 true, true, '2025-02-15 10:30:00',
 '2025-02-15 10:00:00', '2025-02-15 10:00:00', '2025-11-21 14:30:00',
 true, true, 'sv', 'SEK'),
 
(3, 3, 'Erik', 'Andersson', 'erik.andersson@email.com', '+46705551234',
 true, true, '2025-03-20 11:30:00',
 '2025-03-20 11:00:00', '2025-03-20 11:00:00', '2025-11-20 16:45:00',
 false, false, 'sv', 'SEK'),
 
(4, 4, 'Sofia', 'Karlsson', 'sofia.karlsson@email.com', '+46707778899',
 true, true, '2025-04-10 12:30:00',
 '2025-04-10 12:00:00', '2025-04-10 12:00:00', '2025-11-19 10:15:00',
 true, false, 'sv', 'SEK'),
 
(5, NULL, 'Guest', 'Customer', 'new.customer@email.com', '+46703334455',
 true, false, NULL,
 '2025-11-15 14:00:00', '2025-11-15 14:00:00', NULL,
 false, false, 'en', 'SEK');

-- 4.2 CATEGORIES
INSERT INTO categories (category_id, name, slug, description, parent_category_id, display_order, is_active) VALUES
(1, 'Electronics', 'electronics', 'Electronic devices and accessories', NULL, 1, true),
(2, 'Computers', 'computers', 'Desktop and laptop computers', NULL, 2, true),
(3, 'Audio', 'audio', 'Headphones, speakers, and audio equipment', NULL, 3, true),
(4, 'Accessories', 'accessories', 'Computer and phone accessories', NULL, 4, true),
(5, 'Smartphones', 'smartphones', 'Mobile phones and smartphones', 1, 1, true),
(6, 'Tablets', 'tablets', 'Tablet computers', 1, 2, true),
(7, 'Laptops', 'laptops', 'Portable computers', 2, 1, true),
(8, 'Desktops', 'desktops', 'Desktop computers', 2, 2, true),
(9, 'Headphones', 'headphones', 'Over-ear and in-ear headphones', 3, 1, true),
(10, 'Speakers', 'speakers', 'Bluetooth and wired speakers', 3, 2, true);

-- 4.3 PRODUCTS (with discount_price and image_url)
INSERT INTO products (
    product_id, sku, name, description, price, discount_price, stock_quantity,
    category_id, image_id, image_url, is_active, created_date, updated_date
) VALUES
(1, 'LAP-DELL-XPS15-001', 'Dell XPS 15',
 'High-performance laptop with 15.6" display, Intel i7, 16GB RAM, 512GB SSD',
 1599.99, NULL, 15, 7, 7, '/images/products/dell-xps-15.jpg',
 true, '2025-05-01 10:00:00', '2025-05-01 10:00:00'),

(2, 'LAP-APPLE-MBP14-001', 'Apple MacBook Pro 14"',
 'M3 Pro chip, 18GB RAM, 512GB SSD, Liquid Retina XDR display',
 2499.99, NULL, 8, 7, NULL, '/images/products/macbook-pro-14.jpg',
 true, '2025-05-05 11:00:00', '2025-05-05 11:00:00'),

(3, 'LAP-LENOVO-T14-001', 'Lenovo ThinkPad T14',
 'Business laptop with Intel i5, 8GB RAM, 256GB SSD',
 999.99, 899.99, 25, 7, NULL, '/images/products/lenovo-t14.jpg',
 true, '2025-05-10 09:00:00', '2025-05-10 09:00:00'),

(4, 'PHONE-APPLE-IP15P-001', 'Apple iPhone 15 Pro',
 'A17 Pro chip, 128GB, Titanium design, Pro camera system',
 1199.99, NULL, 20, 5, 9, '/images/products/iphone-15-pro.jpg',
 true, '2025-05-10 11:20:00', '2025-05-10 11:20:00'),

(5, 'PHONE-SAMSUNG-S24-001', 'Samsung Galaxy S24',
 'Snapdragon 8 Gen 3, 256GB, AI camera, 120Hz display',
 899.99, 849.99, 30, 5, NULL, '/images/products/galaxy-s24.jpg',
 true, '2025-05-15 10:00:00', '2025-05-15 10:00:00'),

(6, 'AUDIO-SONY-WH1K-001', 'Sony WH-1000XM5',
 'Premium noise-cancelling headphones, 30h battery, Hi-Res audio',
 399.99, 349.99, 40, 9, 10, '/images/products/sony-wh1000xm5.jpg',
 true, '2025-05-15 14:30:00', '2025-05-15 14:30:00'),

(7, 'AUDIO-APPLE-APMAX-001', 'Apple AirPods Max',
 'Over-ear headphones with spatial audio, active noise cancellation',
 549.99, NULL, 12, 9, NULL, '/images/products/airpods-max.jpg',
 true, '2025-05-18 10:00:00', '2025-05-18 10:00:00'),

(8, 'ACC-KEY-MECH-001', 'Mechanical Gaming Keyboard',
 'RGB backlit, mechanical switches, programmable keys',
 149.99, 129.99, 50, 4, 11, '/images/products/gaming-keyboard.jpg',
 true, '2025-05-20 09:45:00', '2025-05-20 09:45:00'),

(9, 'ACC-MOUSE-LG-MX3-001', 'Logitech MX Master 3',
 'Wireless mouse, ergonomic design, precision scroll wheel',
 99.99, NULL, 60, 4, 12, '/images/products/mx-master-3.jpg',
 true, '2025-05-25 10:15:00', '2025-05-25 10:15:00'),

(10, 'ACC-WEBCAM-LOG-001', 'Logitech C920 HD Webcam',
 '1080p video, stereo audio, autofocus',
 79.99, 69.99, 35, 4, NULL, '/images/products/c920-webcam.jpg',
 true, '2025-06-01 11:00:00', '2025-06-01 11:00:00'),

(11, 'LAP-ASUS-ROG-001', 'ASUS ROG Gaming Laptop',
 'Intel i9, RTX 4070, 32GB RAM, 1TB SSD, 17" 240Hz display',
 2799.99, NULL, 3, 7, NULL, '/images/products/asus-rog.jpg',
 true, '2025-06-05 10:00:00', '2025-06-05 10:00:00'),

(12, 'PHONE-GOOGLE-PIX8-001', 'Google Pixel 8 Pro',
 'Google Tensor G3, 256GB, AI photography',
 999.99, NULL, 2, 5, NULL, '/images/products/pixel-8-pro.jpg',
 true, '2025-06-10 12:00:00', '2025-06-10 12:00:00');

-- 4.4 ADDRESSES (default_address, state)
INSERT INTO addresses (
    address_id, customer_id, street, apartment, city, state, postal_code,
    country, default_address, address_type
) VALUES
(1, 1, 'Storgatan 12', NULL, 'Stockholm', NULL, '11420', 'Sweden', true, 'BILLING'),
(2, 1, 'Storgatan 12', NULL, 'Stockholm', NULL, '11420', 'Sweden', true, 'SHIPPING'),
(3, 2, 'Drottninggatan 45', 'Apt 3B', 'Göteborg', NULL, '41103', 'Sweden', true, 'BILLING'),
(4, 2, 'Drottninggatan 45', 'Apt 3B', 'Göteborg', NULL, '41103', 'Sweden', true, 'SHIPPING'),
(5, 3, 'Kungsgatan 88', NULL, 'Malmö', NULL, '21145', 'Sweden', true, 'BILLING'),
(6, 3, 'Arbetargatan 23', NULL, 'Malmö', NULL, '21240', 'Sweden', true, 'SHIPPING'),
(7, 4, 'Vasagatan 10', 'Lgh 12', 'Uppsala', NULL, '75320', 'Sweden', true, 'BILLING'),
(8, 4, 'Vasagatan 10', 'Lgh 12', 'Uppsala', NULL, '75320', 'Sweden', true, 'SHIPPING');

-- 4.5 CARTS (with all new columns)
INSERT INTO carts (
    cart_id, customer_id, total_amount, item_count, coupon_code, discount_amount,
    is_saved, saved_name, created_date, updated_date, expiration_date
) VALUES
(1, 1, 499.98, 2, NULL, 0.00, false, NULL,
 '2025-11-20 10:00:00', '2025-11-22 09:30:00', '2025-12-22 09:30:00'),
 
(2, 3, 1749.98, 2, 'WELCOME10', 50.00, false, NULL,
 '2025-11-21 14:00:00', '2025-11-22 08:00:00', '2025-12-22 08:00:00'),
 
(3, 2, 0.00, 0, NULL, 0.00, true, 'Wishlist for Later',
 '2025-10-15 10:00:00', '2025-10-15 10:00:00', NULL);

-- 4.6 CART_ITEMS (with ALL new columns from CartItem.java)
INSERT INTO cart_items (
    cart_item_id, cart_id, product_id, quantity, unit_price, discount_price, subtotal,
    product_name, product_sku, product_image_url, selected_options, custom_message,
    is_saved_for_later, is_gift, gift_message, stock_checked_date, stock_available,
    requested_quantity, notes, added_date, updated_date
) VALUES
-- Magnus cart (cart_id=1)
(1, 1, 6, 1, 399.99, 349.99, 349.99,
 'Sony WH-1000XM5', 'AUDIO-SONY-WH1K-001', '/images/products/sony-wh1000xm5.jpg',
 NULL, NULL, false, false, NULL, '2025-11-22 09:00:00', true, NULL, NULL,
 '2025-11-20 10:00:00', '2025-11-22 09:00:00'),

(2, 1, 9, 1, 99.99, NULL, 99.99,
 'Logitech MX Master 3', 'ACC-MOUSE-LG-MX3-001', '/images/products/mx-master-3.jpg',
 NULL, NULL, false, true, 'Happy Birthday!', '2025-11-22 09:00:00', true, NULL, NULL,
 '2025-11-21 15:00:00', '2025-11-21 15:00:00'),

-- Erik cart (cart_id=2)
(3, 2, 1, 1, 1599.99, NULL, 1599.99,
 'Dell XPS 15', 'LAP-DELL-XPS15-001', '/images/products/dell-xps-15.jpg',
 NULL, NULL, false, false, NULL, '2025-11-22 07:00:00', true, NULL, NULL,
 '2025-11-21 14:00:00', '2025-11-22 07:00:00'),

(4, 2, 8, 1, 149.99, 129.99, 129.99,
 'Mechanical Gaming Keyboard', 'ACC-KEY-MECH-001', '/images/products/gaming-keyboard.jpg',
 NULL, NULL, false, false, NULL, '2025-11-22 07:00:00', true, NULL, NULL,
 '2025-11-22 08:00:00', '2025-11-22 08:00:00');

-- 4.7 ORDERS (with ALL new columns from Order.java)
INSERT INTO orders (
    order_id, order_number, customer_id, order_status, order_date,
    subtotal, tax_amount, shipping_amount, total_amount, currency,
    shipping_first_name, shipping_last_name, shipping_email, shipping_phone,
    shipping_address_line1, shipping_address_line2, shipping_city, shipping_state,
    shipping_postal_code, shipping_country,
    billing_same_as_shipping, billing_address_line1, billing_address_line2,
    billing_city, billing_state, billing_postal_code, billing_country,
    customer_notes, internal_notes, created_date, updated_date
) VALUES
-- Delivered order
(1, 'ORD-2025-0001', 2, 'DELIVERED', '2025-09-15 10:30:00',
 1749.98, 349.99, 49.00, 2148.97, 'SEK',
 'Anna', 'Svensson', 'anna.svensson@email.com', '+46709876543',
 'Drottninggatan 45', 'Apt 3B', 'Göteborg', NULL, '41103', 'SE',
 true, NULL, NULL, NULL, NULL, NULL, NULL,
 'Please ring doorbell', 'Priority customer',
 '2025-09-15 10:30:00', '2025-09-18 11:45:00'),

-- Delivered order
(2, 'ORD-2025-0002', 3, 'DELIVERED', '2025-10-01 15:20:00',
 479.98, 95.99, 29.00, 604.97, 'SEK',
 'Erik', 'Andersson', 'erik.andersson@email.com', '+46705551234',
 'Arbetargatan 23', NULL, 'Malmö', NULL, '21240', 'SE',
 false, 'Kungsgatan 88', NULL, 'Malmö', NULL, '21145', 'SE',
 NULL, NULL,
 '2025-10-01 15:20:00', '2025-10-04 16:30:00'),

-- Shipped order
(3, 'ORD-2025-0003', 1, 'SHIPPED', '2025-11-18 11:00:00',
 1699.98, 339.99, 49.00, 2038.97, 'SEK',
 'Magnus', 'Berglund', 'magnus@perfect8.com', '+46701234567',
 'Storgatan 12', NULL, 'Stockholm', NULL, '11420', 'SE',
 true, NULL, NULL, NULL, NULL, NULL, NULL,
 'Express shipping requested', 'Applied 50 SEK discount',
 '2025-11-18 11:00:00', '2025-11-19 13:30:00'),

-- Shipped order
(4, 'ORD-2025-0004', 4, 'SHIPPED', '2025-11-20 09:45:00',
 249.98, 49.99, 29.00, 328.97, 'SEK',
 'Sofia', 'Karlsson', 'sofia.karlsson@email.com', '+46707778899',
 'Vasagatan 10', 'Lgh 12', 'Uppsala', NULL, '75320', 'SE',
 true, NULL, NULL, NULL, NULL, NULL, NULL,
 NULL, NULL,
 '2025-11-20 09:45:00', '2025-11-21 10:15:00'),

-- Processing order
(5, 'ORD-2025-0005', 2, 'PROCESSING', '2025-11-21 14:00:00',
 2549.98, 509.99, 0.00, 3059.97, 'SEK',
 'Anna', 'Svensson', 'anna.svensson@email.com', '+46709876543',
 'Drottninggatan 45', 'Apt 3B', 'Göteborg', NULL, '41103', 'SE',
 true, NULL, NULL, NULL, NULL, NULL, NULL,
 'Free shipping promotion', 'VIP customer',
 '2025-11-21 14:00:00', '2025-11-21 14:00:00'),

-- Pending order
(6, 'ORD-2025-0006', 3, 'PENDING', '2025-11-22 08:30:00',
 899.99, 179.99, 29.00, 1108.98, 'SEK',
 'Erik', 'Andersson', 'erik.andersson@email.com', '+46705551234',
 'Arbetargatan 23', NULL, 'Malmö', NULL, '21240', 'SE',
 true, NULL, NULL, NULL, NULL, NULL, NULL,
 NULL, 'Payment pending',
 '2025-11-22 08:30:00', '2025-11-22 08:30:00'),

-- Cancelled order
(7, 'ORD-2025-0007', 4, 'CANCELLED', '2025-10-15 10:00:00',
 3349.97, 669.99, 0.00, 4019.96, 'SEK',
 'Sofia', 'Karlsson', 'sofia.karlsson@email.com', '+46707778899',
 'Vasagatan 10', 'Lgh 12', 'Uppsala', NULL, '75320', 'SE',
 true, NULL, NULL, NULL, NULL, NULL, NULL,
 'Changed mind', 'Cancelled at customer request - refund issued',
 '2025-10-15 10:00:00', '2025-10-16 14:30:00');

-- 4.8 ORDER_ITEMS (with ALL new columns from OrderItem.java)
INSERT INTO order_items (
    order_item_id, order_id, product_id, product_name, product_sku, quantity,
    unit_price, discount_amount, tax_amount, price, item_status, notes,
    product_image_url, product_description, shipped_quantity, returned_quantity,
    refunded_quantity, created_date, updated_date
) VALUES
-- Order 1 items
(1, 1, 1, 'Dell XPS 15', 'LAP-DELL-XPS15-001', 1,
 1599.99, 0.00, 319.99, 1919.98, 'DELIVERED', NULL,
 '/images/products/dell-xps-15.jpg', 'High-performance laptop', 1, 0, 0,
 '2025-09-15 10:30:00', '2025-09-18 11:45:00'),

(2, 1, 8, 'Mechanical Gaming Keyboard', 'ACC-KEY-MECH-001', 1,
 149.99, 20.00, 30.00, 159.99, 'DELIVERED', NULL,
 '/images/products/gaming-keyboard.jpg', 'RGB gaming keyboard', 1, 0, 0,
 '2025-09-15 10:30:00', '2025-09-18 11:45:00'),

-- Order 2 items
(3, 2, 6, 'Sony WH-1000XM5', 'AUDIO-SONY-WH1K-001', 1,
 399.99, 50.00, 79.99, 429.98, 'DELIVERED', 'On sale',
 '/images/products/sony-wh1000xm5.jpg', 'Premium headphones', 1, 0, 0,
 '2025-10-01 15:20:00', '2025-10-04 16:30:00'),

(4, 2, 10, 'Logitech C920 HD Webcam', 'ACC-WEBCAM-LOG-001', 1,
 79.99, 10.00, 16.00, 85.99, 'DELIVERED', NULL,
 '/images/products/c920-webcam.jpg', '1080p webcam', 1, 0, 0,
 '2025-10-01 15:20:00', '2025-10-04 16:30:00'),

-- Order 3 items
(5, 3, 4, 'Apple iPhone 15 Pro', 'PHONE-APPLE-IP15P-001', 1,
 1199.99, 0.00, 239.99, 1439.98, 'SHIPPED', NULL,
 '/images/products/iphone-15-pro.jpg', 'Latest iPhone', 1, 0, 0,
 '2025-11-18 11:00:00', '2025-11-19 13:30:00'),

(6, 3, 6, 'Sony WH-1000XM5', 'AUDIO-SONY-WH1K-001', 1,
 399.99, 50.00, 79.99, 429.98, 'SHIPPED', NULL,
 '/images/products/sony-wh1000xm5.jpg', 'Premium headphones', 1, 0, 0,
 '2025-11-18 11:00:00', '2025-11-19 13:30:00'),

(7, 3, 9, 'Logitech MX Master 3', 'ACC-MOUSE-LG-MX3-001', 1,
 99.99, 0.00, 20.01, 120.00, 'SHIPPED', NULL,
 '/images/products/mx-master-3.jpg', 'Wireless mouse', 1, 0, 0,
 '2025-11-18 11:00:00', '2025-11-19 13:30:00'),

-- Order 4 items
(8, 4, 8, 'Mechanical Gaming Keyboard', 'ACC-KEY-MECH-001', 1,
 149.99, 20.00, 29.99, 159.98, 'SHIPPED', NULL,
 '/images/products/gaming-keyboard.jpg', 'RGB gaming keyboard', 1, 0, 0,
 '2025-11-20 09:45:00', '2025-11-21 10:15:00'),

(9, 4, 9, 'Logitech MX Master 3', 'ACC-MOUSE-LG-MX3-001', 1,
 99.99, 0.00, 20.00, 119.99, 'SHIPPED', NULL,
 '/images/products/mx-master-3.jpg', 'Wireless mouse', 1, 0, 0,
 '2025-11-20 09:45:00', '2025-11-21 10:15:00'),

-- Order 5 items
(10, 5, 2, 'Apple MacBook Pro 14"', 'LAP-APPLE-MBP14-001', 1,
 2499.99, 0.00, 499.99, 2999.98, 'PROCESSING', NULL,
 '/images/products/macbook-pro-14.jpg', 'M3 MacBook Pro', 0, 0, 0,
 '2025-11-21 14:00:00', '2025-11-21 14:00:00'),

(11, 5, 10, 'Logitech C920 HD Webcam', 'ACC-WEBCAM-LOG-001', 1,
 79.99, 10.00, 10.00, 79.99, 'PROCESSING', NULL,
 '/images/products/c920-webcam.jpg', '1080p webcam', 0, 0, 0,
 '2025-11-21 14:00:00', '2025-11-21 14:00:00'),

-- Order 6 items
(12, 6, 5, 'Samsung Galaxy S24', 'PHONE-SAMSUNG-S24-001', 1,
 899.99, 50.00, 179.99, 1029.98, 'PENDING', NULL,
 '/images/products/galaxy-s24.jpg', 'Latest Samsung', 0, 0, 0,
 '2025-11-22 08:30:00', '2025-11-22 08:30:00'),

-- Order 7 items (cancelled)
(13, 7, 11, 'ASUS ROG Gaming Laptop', 'LAP-ASUS-ROG-001', 1,
 2799.99, 0.00, 559.99, 3359.98, 'CANCELLED', 'Refunded',
 '/images/products/asus-rog.jpg', 'Gaming laptop', 0, 0, 1,
 '2025-10-15 10:00:00', '2025-10-16 14:30:00'),

(14, 7, 6, 'Sony WH-1000XM5', 'AUDIO-SONY-WH1K-001', 1,
 399.99, 50.00, 79.99, 429.98, 'CANCELLED', 'Refunded',
 '/images/products/sony-wh1000xm5.jpg', 'Premium headphones', 0, 0, 1,
 '2025-10-15 10:00:00', '2025-10-16 14:30:00'),

(15, 7, 8, 'Mechanical Gaming Keyboard', 'ACC-KEY-MECH-001', 1,
 149.99, 20.00, 30.00, 159.99, 'CANCELLED', 'Refunded',
 '/images/products/gaming-keyboard.jpg', 'RGB gaming keyboard', 0, 0, 1,
 '2025-10-15 10:00:00', '2025-10-16 14:30:00');

-- Fortsätter med Payments, Shipments, etc... Vill du att jag fortsätter? 📦