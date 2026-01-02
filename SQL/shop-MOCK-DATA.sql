-- ================================================
-- shop-MOCK-DATA.sql
-- Database: shopDB
-- Created: 2025-11-16
-- Purpose: Mock data for testing shop-service
-- 
-- USAGE:
--   mysql -u perfect8user -p shopDB < shop-MOCK-DATA.sql
--
-- IMPORTANT:
-- - Run AFTER shop-CREATE-TABLE.sql
-- - All data follows Magnum Opus principles
-- - Swedish addresses and product names
-- - SEK currency (Sverige)
-- - BCrypt password hashes
-- - Realistic e-commerce scenarios
-- ================================================

-- ================================================
-- 1. CATEGORIES (Hierarkiska kategorier)
-- ================================================

INSERT INTO categories (category_id, name, slug, description, image_url, parent_category_id, sort_order, active, meta_title, meta_description, created_date, updated_date) VALUES
-- Root categories
(1, 'Elektronik', 'elektronik', 'Datorer, mobiler, surfplattor och tillbehör', '/images/categories/electronics.jpg', NULL, 1, TRUE, 'Elektronik - Perfect8', 'Köp elektronik online hos Perfect8', NOW(), NOW()),
(2, 'Kläder', 'klader', 'Mode för dam, herr och barn', '/images/categories/fashion.jpg', NULL, 2, TRUE, 'Kläder - Perfect8', 'Senaste modet hos Perfect8', NOW(), NOW()),
(3, 'Böcker', 'bocker', 'Böcker, e-böcker och ljudböcker', '/images/categories/books.jpg', NULL, 3, TRUE, 'Böcker - Perfect8', 'Köp böcker online hos Perfect8', NOW(), NOW()),
(4, 'Hem & Trädgård', 'hem-tradgard', 'Möbler, inredning och trädgårdsprodukter', '/images/categories/home.jpg', NULL, 4, TRUE, 'Hem & Trädgård - Perfect8', 'Inred ditt hem med Perfect8', NOW(), NOW()),

-- Subcategories - Elektronik
(5, 'Datorer', 'datorer', 'Bärbara datorer och stationära datorer', '/images/categories/computers.jpg', 1, 1, TRUE, 'Datorer - Perfect8', 'Köp datorer online', NOW(), NOW()),
(6, 'Mobiler', 'mobiler', 'Smartphones och mobiltelefoner', '/images/categories/phones.jpg', 1, 2, TRUE, 'Mobiler - Perfect8', 'Senaste mobilerna', NOW(), NOW()),
(7, 'Surfplattor', 'surfplattor', 'Tablets och läsplattor', '/images/categories/tablets.jpg', 1, 3, TRUE, 'Surfplattor - Perfect8', 'Köp surfplattor online', NOW(), NOW()),
(8, 'Tillbehör', 'tillbehor', 'Kablar, fodral och laddare', '/images/categories/accessories.jpg', 1, 4, TRUE, 'Tillbehör - Perfect8', 'Tillbehör till din elektronik', NOW(), NOW()),

-- Subcategories - Kläder
(9, 'Herrkläder', 'herr', 'Kläder för herrar', '/images/categories/mens.jpg', 2, 1, TRUE, 'Herrkläder - Perfect8', 'Mode för män', NOW(), NOW()),
(10, 'Damkläder', 'dam', 'Kläder för damer', '/images/categories/womens.jpg', 2, 2, TRUE, 'Damkläder - Perfect8', 'Mode för kvinnor', NOW(), NOW()),
(11, 'Barnkläder', 'barn', 'Kläder för barn', '/images/categories/kids.jpg', 2, 3, TRUE, 'Barnkläder - Perfect8', 'Barnmode online', NOW(), NOW()),

-- Subcategories - Böcker
(12, 'Skönlitteratur', 'skonlitteratur', 'Romaner och noveller', '/images/categories/fiction.jpg', 3, 1, TRUE, 'Skönlitteratur - Perfect8', 'Läs skönlitteratur', NOW(), NOW()),
(13, 'Facklitteratur', 'facklitteratur', 'Faktaböcker och läroböcker', '/images/categories/nonfiction.jpg', 3, 2, TRUE, 'Facklitteratur - Perfect8', 'Lär dig något nytt', NOW(), NOW());

-- ================================================
-- 2. PRODUCTS (20 produkter med varierat lager)
-- ================================================

INSERT INTO products (product_id, sku, name, description, price, discount_price, stock_quantity, reorder_point, reorder_quantity, category_id, image_url, featured, active, weight, dimensions, meta_title, meta_description, views, sales_count, rating, review_count, created_date, updated_date) VALUES
-- Elektronik - Datorer (category 5)
(1, 'LAPTOP-001', 'Dell XPS 13 Bärbar Dator', 'Kraftfull och kompakt bärbar dator med 13" skärm, Intel i7, 16GB RAM, 512GB SSD', 14995.00, 13495.00, 15, 5, 10, 5, '/images/products/laptop-dell-xps13.jpg', TRUE, TRUE, 1.27, '30x21x1.5', 'Dell XPS 13 - Perfect8', 'Köp Dell XPS 13 online', 1250, 43, 4.50, 28, NOW(), NOW()),
(2, 'LAPTOP-002', 'MacBook Air M2', 'Apple MacBook Air med M2-chip, 13" Retina, 8GB RAM, 256GB SSD', 13995.00, NULL, 8, 3, 5, 5, '/images/products/macbook-air-m2.jpg', TRUE, TRUE, 1.24, '30x21x1.6', 'MacBook Air M2 - Perfect8', 'Apple MacBook Air M2', 2100, 67, 4.80, 45, NOW(), NOW()),
(3, 'DESKTOP-001', 'Gaming PC RGB Master', 'Kraftfull gaming-dator med RTX 4070, i7-13700K, 32GB RAM, 1TB SSD', 24995.00, 22995.00, 5, 2, 5, 5, '/images/products/gaming-pc-rgb.jpg', TRUE, TRUE, 12.50, '45x20x45', 'Gaming PC - Perfect8', 'Köp gaming-dator online', 890, 12, 4.70, 8, NOW(), NOW()),

-- Elektronik - Mobiler (category 6)
(4, 'PHONE-001', 'iPhone 15 Pro 128GB', 'Apple iPhone 15 Pro med A17 Pro chip, 48MP kamera', 13995.00, NULL, 25, 10, 20, 6, '/images/products/iphone-15-pro.jpg', TRUE, TRUE, 0.187, '15x7x0.8', 'iPhone 15 Pro - Perfect8', 'Köp iPhone 15 Pro', 3400, 125, 4.60, 89, NOW(), NOW()),
(5, 'PHONE-002', 'Samsung Galaxy S24', 'Samsung Galaxy S24 med Snapdragon 8 Gen 3, 256GB', 11995.00, 10995.00, 30, 10, 20, 6, '/images/products/samsung-s24.jpg', TRUE, TRUE, 0.168, '15x7x0.8', 'Samsung Galaxy S24 - Perfect8', 'Köp Samsung Galaxy S24', 2800, 98, 4.40, 67, NOW(), NOW()),

-- Elektronik - Surfplattor (category 7)
(6, 'TABLET-001', 'iPad Air 10.9" 64GB', 'Apple iPad Air med M1-chip, 10.9" Liquid Retina', 7495.00, NULL, 20, 8, 15, 7, '/images/products/ipad-air.jpg', FALSE, TRUE, 0.461, '25x18x0.6', 'iPad Air - Perfect8', 'Köp iPad Air online', 1500, 54, 4.50, 38, NOW(), NOW()),
(7, 'TABLET-002', 'Samsung Galaxy Tab S9', 'Samsung Galaxy Tab S9 med S Pen, 11" AMOLED', 6995.00, 6495.00, 12, 5, 10, 7, '/images/products/samsung-tab-s9.jpg', FALSE, TRUE, 0.498, '25x16x0.6', 'Samsung Tab S9 - Perfect8', 'Köp Samsung Tab S9', 980, 32, 4.30, 21, NOW(), NOW()),

-- Elektronik - Tillbehör (category 8)
(8, 'ACC-001', 'USB-C Laddare 65W', 'Snabbladdare med USB-C Power Delivery', 299.00, NULL, 100, 20, 50, 8, '/images/products/usbc-charger.jpg', FALSE, TRUE, 0.120, '10x10x3', 'USB-C Laddare - Perfect8', 'Snabbladdare för laptop och mobil', 450, 156, 4.20, 42, NOW(), NOW()),
(9, 'ACC-002', 'Trådlösa Hörlurar ANC', 'Bluetooth hörlurar med aktiv brusreducering', 1495.00, 1295.00, 45, 15, 30, 8, '/images/products/wireless-earbuds.jpg', FALSE, TRUE, 0.050, '8x8x3', 'Trådlösa Hörlurar - Perfect8', 'Hörlurar med brusreducering', 780, 89, 4.10, 56, NOW(), NOW()),
(10, 'ACC-003', 'Mobil Fodral Premium', 'Skyddsfodral i läder för iPhone och Samsung', 249.00, NULL, 200, 50, 100, 8, '/images/products/phone-case.jpg', FALSE, TRUE, 0.030, '16x8x1', 'Mobil Fodral - Perfect8', 'Skydda din mobil med stil', 1200, 234, 3.90, 78, NOW(), NOW()),

-- Kläder - Herr (category 9)
(11, 'MENS-001', 'Bomullsskjorta Vit Herr', 'Klassisk vit skjorta i 100% bomull', 599.00, NULL, 50, 15, 30, 9, '/images/products/mens-shirt-white.jpg', FALSE, TRUE, 0.300, '40x30x3', 'Bomullsskjorta Herr - Perfect8', 'Klassisk herr skjorta', 560, 67, 4.30, 34, NOW(), NOW()),
(12, 'MENS-002', 'Jeans Slim Fit Herr', 'Slim fit jeans i stretch denim', 799.00, 699.00, 40, 10, 25, 9, '/images/products/mens-jeans.jpg', FALSE, TRUE, 0.600, '50x30x5', 'Jeans Herr - Perfect8', 'Bekväma jeans för herr', 420, 45, 4.40, 28, NOW(), NOW()),

-- Kläder - Dam (category 10)
(13, 'WOMENS-001', 'Klänning Sommar Dam', 'Luftig sommarklänning i bomullslin', 899.00, 799.00, 35, 10, 20, 10, '/images/products/womens-dress.jpg', TRUE, TRUE, 0.250, '45x35x3', 'Sommarklänning - Perfect8', 'Vacker sommarklänning', 890, 56, 4.50, 42, NOW(), NOW()),
(14, 'WOMENS-002', 'Blazer Business Dam', 'Elegant blazer för kontoret', 1299.00, NULL, 25, 8, 15, 10, '/images/products/womens-blazer.jpg', FALSE, TRUE, 0.450, '50x40x5', 'Blazer Dam - Perfect8', 'Professionell blazer', 340, 23, 4.20, 15, NOW(), NOW()),

-- Kläder - Barn (category 11)
(15, 'KIDS-001', 'T-shirt Barn 6-8 år', 'Färgglad t-shirt för barn', 199.00, NULL, 80, 20, 40, 11, '/images/products/kids-tshirt.jpg', FALSE, TRUE, 0.100, '30x25x2', 'T-shirt Barn - Perfect8', 'Bekväm barnkläder', 280, 78, 4.10, 32, NOW(), NOW()),

-- Böcker - Skönlitteratur (category 12)
(16, 'BOOK-001', 'Millennium Trilogin Box', 'Stieg Larssons klassiska trilogi i box', 499.00, 449.00, 30, 10, 20, 12, '/images/products/millennium-box.jpg', FALSE, TRUE, 1.200, '25x18x8', 'Millennium Box - Perfect8', 'Stieg Larsson trilogi', 670, 89, 4.80, 156, NOW(), NOW()),
(17, 'BOOK-002', 'Beartown av Fredrik Backman', 'Bästsäljande roman om en hockeystad', 189.00, NULL, 50, 15, 30, 12, '/images/products/beartown.jpg', FALSE, TRUE, 0.400, '21x14x3', 'Beartown - Perfect8', 'Fredrik Backman roman', 450, 67, 4.70, 89, NOW(), NOW()),

-- Böcker - Facklitteratur (category 13)
(18, 'BOOK-003', 'Spring Boot in Action', 'Lär dig Spring Boot från grunden', 599.00, NULL, 15, 5, 10, 13, '/images/products/spring-boot-book.jpg', FALSE, TRUE, 0.800, '24x18x3', 'Spring Boot Book - Perfect8', 'Lär dig Spring Boot', 230, 34, 4.60, 23, NOW(), NOW()),

-- Hem & Trädgård (category 4)
(19, 'HOME-001', 'LED-lampa Smart 3-pack', 'Smarta LED-lampor med app-styrning', 599.00, 499.00, 60, 20, 40, 4, '/images/products/smart-bulbs.jpg', FALSE, TRUE, 0.200, '12x12x15', 'Smart LED - Perfect8', 'Smarta lampor för hemmet', 340, 78, 4.30, 45, NOW(), NOW()),
(20, 'HOME-002', 'Växtljus LED för inomhus', 'LED-växtlampa för inomhusodling', 899.00, NULL, 25, 8, 15, 4, '/images/products/plant-light.jpg', FALSE, TRUE, 1.500, '60x15x15', 'Växtljus - Perfect8', 'LED växtlampa', 180, 23, 4.40, 18, NOW(), NOW());

-- ================================================
-- 3. PRODUCT IMAGES (ElementCollection)
-- ================================================

INSERT INTO product_images (product_id, image_url) VALUES
-- Dell XPS 13 additional images
(1, '/images/products/laptop-dell-xps13-2.jpg'),
(1, '/images/products/laptop-dell-xps13-3.jpg'),
(1, '/images/products/laptop-dell-xps13-4.jpg'),

-- MacBook Air M2 additional images
(2, '/images/products/macbook-air-m2-2.jpg'),
(2, '/images/products/macbook-air-m2-3.jpg'),

-- iPhone 15 Pro additional images
(4, '/images/products/iphone-15-pro-2.jpg'),
(4, '/images/products/iphone-15-pro-3.jpg'),
(4, '/images/products/iphone-15-pro-4.jpg'),

-- Gaming PC additional images
(3, '/images/products/gaming-pc-rgb-2.jpg'),
(3, '/images/products/gaming-pc-rgb-3.jpg');

-- ================================================
-- 4. PRODUCT TAGS (ElementCollection)
-- ================================================

INSERT INTO product_tags (product_id, tag) VALUES
-- Laptops
(1, 'laptop'), (1, 'dell'), (1, 'xps'), (1, 'premium'),
(2, 'laptop'), (2, 'apple'), (2, 'macbook'), (2, 'premium'), (2, 'm2'),

-- Desktop
(3, 'desktop'), (3, 'gaming'), (3, 'rgb'), (3, 'high-performance'),

-- Phones
(4, 'smartphone'), (4, 'iphone'), (4, 'apple'), (4, '5g'),
(5, 'smartphone'), (5, 'samsung'), (5, 'android'), (5, '5g'),

-- Tablets
(6, 'tablet'), (6, 'ipad'), (6, 'apple'), (6, 'm1'),
(7, 'tablet'), (7, 'samsung'), (7, 'android'), (7, 's-pen'),

-- Accessories
(8, 'charger'), (8, 'usb-c'), (8, 'power-delivery'),
(9, 'headphones'), (9, 'wireless'), (9, 'anc'), (9, 'bluetooth'),
(10, 'case'), (10, 'protection'), (10, 'leather'),

-- Clothing
(11, 'shirt'), (11, 'mens'), (11, 'cotton'),
(12, 'jeans'), (12, 'mens'), (12, 'slim-fit'),
(13, 'dress'), (13, 'womens'), (13, 'summer'),
(14, 'blazer'), (14, 'womens'), (14, 'business'),
(15, 'tshirt'), (15, 'kids'),

-- Books
(16, 'book'), (16, 'fiction'), (16, 'thriller'), (16, 'swedish'),
(17, 'book'), (17, 'fiction'), (17, 'fredrik-backman'),
(18, 'book'), (18, 'programming'), (18, 'spring-boot'), (18, 'java'),

-- Home
(19, 'smart-home'), (19, 'led'), (19, 'lighting'),
(20, 'gardening'), (20, 'led'), (20, 'indoor-growing');

-- ================================================
-- 5. CUSTOMERS (5 kunder med BCrypt-hashade lösenord)
-- Password: "password123" för alla
-- BCrypt hash: $2a$10$XQy9z8WQHyQh9x3MvL5vp.EQG5K3gGWTZ0vP5YFmJfX4mZ5YJZvWG
-- ================================================

INSERT INTO customers (customer_id, email, password_hash, first_name, last_name, phone, active, email_verified, email_verified_date, role, newsletter_subscribed, marketing_consent, preferred_language, preferred_currency, created_date, updated_date, last_login_date) VALUES
(1, 'anna.andersson@example.com', '$2a$10$XQy9z8WQHyQh9x3MvL5vp.EQG5K3gGWTZ0vP5YFmJfX4mZ5YJZvWG', 'Anna', 'Andersson', '0701234567', TRUE, TRUE, NOW(), 'CUSTOMER', TRUE, TRUE, 'sv', 'SEK', NOW(), NOW(), NOW()),
(2, 'erik.berg@example.com', '$2a$10$XQy9z8WQHyQh9x3MvL5vp.EQG5K3gGWTZ0vP5YFmJfX4mZ5YJZvWG', 'Erik', 'Berg', '0709876543', TRUE, TRUE, NOW(), 'CUSTOMER', FALSE, FALSE, 'sv', 'SEK', NOW(), NOW(), NOW()),
(3, 'maria.carlsson@example.com', '$2a$10$XQy9z8WQHyQh9x3MvL5vp.EQG5K3gGWTZ0vP5YFmJfX4mZ5YJZvWG', 'Maria', 'Carlsson', '0731122334', TRUE, TRUE, NOW(), 'CUSTOMER', TRUE, TRUE, 'sv', 'SEK', NOW(), NOW(), NOW()),
(4, 'johan.danielsson@example.com', '$2a$10$XQy9z8WQHyQh9x3MvL5vp.EQG5K3gGWTZ0vP5YFmJfX4mZ5YJZvWG', 'Johan', 'Danielsson', '0765544332', TRUE, FALSE, NULL, 'CUSTOMER', FALSE, FALSE, 'sv', 'SEK', NOW(), NOW(), NULL),
(5, 'sara.eriksson@example.com', '$2a$10$XQy9z8WQHyQh9x3MvL5vp.EQG5K3gGWTZ0vP5YFmJfX4mZ5YJZvWG', 'Sara', 'Eriksson', '0708899776', TRUE, TRUE, NOW(), 'CUSTOMER', TRUE, FALSE, 'sv', 'SEK', NOW(), NOW(), NOW());

-- ================================================
-- 6. ADDRESSES (Svenska adresser)
-- ================================================

INSERT INTO addresses (address_id, customer_id, street, apartment, city, state, postal_code, country, default_address, address_type) VALUES
-- Anna Andersson (customer 1)
(1, 1, 'Drottninggatan 45', NULL, 'Stockholm', 'Stockholm', '11121', 'SE', TRUE, 'SHIPPING'),
(2, 1, 'Drottninggatan 45', NULL, 'Stockholm', 'Stockholm', '11121', 'SE', TRUE, 'BILLING'),

-- Erik Berg (customer 2)
(3, 2, 'Avenyn 10', 'Lägenhet 302', 'Göteborg', 'Västra Götaland', '41136', 'SE', TRUE, 'SHIPPING'),
(4, 2, 'Avenyn 10', 'Lägenhet 302', 'Göteborg', 'Västra Götaland', '41136', 'SE', TRUE, 'BILLING'),

-- Maria Carlsson (customer 3)
(5, 3, 'Stortorget 8', NULL, 'Malmö', 'Skåne', '21122', 'SE', TRUE, 'SHIPPING'),
(6, 3, 'Västra Hamngatan 22', NULL, 'Malmö', 'Skåne', '21115', 'SE', FALSE, 'BILLING'),

-- Johan Danielsson (customer 4)
(7, 4, 'Kungsgatan 15', 'Apt 12', 'Uppsala', 'Uppsala', '75320', 'SE', TRUE, 'SHIPPING'),
(8, 4, 'Kungsgatan 15', 'Apt 12', 'Uppsala', 'Uppsala', '75320', 'SE', TRUE, 'BILLING'),

-- Sara Eriksson (customer 5)
(9, 5, 'Storgatan 30', NULL, 'Lund', 'Skåne', '22234', 'SE', TRUE, 'SHIPPING'),
(10, 5, 'Storgatan 30', NULL, 'Lund', 'Skåne', '22234', 'SE', TRUE, 'BILLING');

-- ================================================
-- 7. CARTS (3 aktiva carts, 1 sparad)
-- ================================================

INSERT INTO carts (cart_id, customer_id, session_id, total_amount, item_count, coupon_code, discount_amount, is_saved, saved_name, created_date, updated_date, expiration_date) VALUES
-- Anna's active cart (med items)
(1, 1, NULL, 16289.00, 2, NULL, 0.00, FALSE, NULL, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),

-- Erik's active cart (med items och kupong)
(2, 2, NULL, 12690.00, 3, 'SUMMER10', 1410.00, FALSE, NULL, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),

-- Maria's saved cart
(3, 3, NULL, 7494.00, 1, NULL, 0.00, TRUE, 'Min önskelista', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), NULL),

-- Guest cart (session-based, ingen customer_id)
(4, NULL, 'guest-session-abc123', 1495.00, 1, NULL, 0.00, FALSE, NULL, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

-- ================================================
-- 8. CART ITEMS
-- ================================================

INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, unit_price, discount_price, subtotal, product_name, product_sku, product_image_url, is_saved_for_later, is_gift, stock_available, added_date, updated_date) VALUES
-- Anna's cart (cart_id 1)
(1, 1, 2, 1, 13995.00, NULL, 13995.00, 'MacBook Air M2', 'LAPTOP-002', '/images/products/macbook-air-m2.jpg', FALSE, FALSE, TRUE, NOW(), NOW()),
(2, 1, 9, 1, 1495.00, 1295.00, 1295.00, 'Trådlösa Hörlurar ANC', 'ACC-002', '/images/products/wireless-earbuds.jpg', FALSE, TRUE, TRUE, NOW(), NOW()),

-- Erik's cart (cart_id 2) - med kupong applicerad
(3, 2, 4, 1, 13995.00, NULL, 13995.00, 'iPhone 15 Pro 128GB', 'PHONE-001', '/images/products/iphone-15-pro.jpg', FALSE, FALSE, TRUE, NOW(), NOW()),
(4, 2, 8, 2, 299.00, NULL, 598.00, 'USB-C Laddare 65W', 'ACC-001', '/images/products/usbc-charger.jpg', FALSE, FALSE, TRUE, NOW(), NOW()),
(5, 2, 10, 1, 249.00, NULL, 249.00, 'Mobil Fodral Premium', 'ACC-003', '/images/products/phone-case.jpg', FALSE, FALSE, TRUE, NOW(), NOW()),

-- Maria's saved cart (cart_id 3)
(6, 3, 6, 1, 7495.00, NULL, 7495.00, 'iPad Air 10.9" 64GB', 'TABLET-001', '/images/products/ipad-air.jpg', TRUE, FALSE, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),

-- Guest cart (cart_id 4)
(7, 4, 9, 1, 1495.00, 1295.00, 1295.00, 'Trådlösa Hörlurar ANC', 'ACC-002', '/images/products/wireless-earbuds.jpg', FALSE, FALSE, TRUE, NOW(), NOW());

-- ================================================
-- 9. ORDERS (3 orders i olika status)
-- ================================================

INSERT INTO orders (order_id, customer_id, order_number, order_status, order_date, subtotal, tax_amount, shipping_amount, total_amount, currency, shipping_first_name, shipping_last_name, shipping_email, shipping_phone, shipping_address_line1, shipping_address_line2, shipping_city, shipping_state, shipping_postal_code, shipping_country, billing_same_as_shipping, customer_notes, created_date, updated_date) VALUES
-- Order 1: DELIVERED (completed)
(1, 1, 'ORD-2025-001', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 10 DAY), 14995.00, 3748.75, 99.00, 18842.75, 'SEK', 'Anna', 'Andersson', 'anna.andersson@example.com', '0701234567', 'Drottninggatan 45', NULL, 'Stockholm', 'Stockholm', '11121', 'SE', TRUE, 'Lämna utanför dörren om jag inte är hemma', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Order 2: SHIPPED (on the way)
(2, 2, 'ORD-2025-002', 'SHIPPED', DATE_SUB(NOW(), INTERVAL 3 DAY), 25489.00, 6372.25, 0.00, 31861.25, 'SEK', 'Erik', 'Berg', 'erik.berg@example.com', '0709876543', 'Avenyn 10', 'Lägenhet 302', 'Göteborg', 'Västra Götaland', '41136', 'SE', TRUE, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Order 3: PROCESSING (payment confirmed, preparing shipment)
(3, 3, 'ORD-2025-003', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 1 DAY), 1495.00, 373.75, 49.00, 1917.75, 'SEK', 'Maria', 'Carlsson', 'maria.carlsson@example.com', '0731122334', 'Stortorget 8', NULL, 'Malmö', 'Skåne', '21122', 'SE', TRUE, 'Ring mig när ni är framme', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());

-- ================================================
-- 10. ORDER ITEMS (snapshots of products at purchase)
-- ================================================

INSERT INTO order_items (order_item_id, order_id, product_id, product_sku, product_name, quantity, unit_price, discount_amount, tax_amount, price, item_status, product_image_url, product_description, shipped_quantity, created_date, updated_date) VALUES
-- Order 1 items (DELIVERED)
(1, 1, 1, 'LAPTOP-001', 'Dell XPS 13 Bärbar Dator', 1, 14995.00, 1500.00, 3373.75, 16868.75, 'DELIVERED', '/images/products/laptop-dell-xps13.jpg', 'Kraftfull och kompakt bärbar dator med 13" skärm', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Order 2 items (SHIPPED)
(2, 2, 3, 'DESKTOP-001', 'Gaming PC RGB Master', 1, 24995.00, 2000.00, 5748.75, 28743.75, 'SHIPPED', '/images/products/gaming-pc-rgb.jpg', 'Kraftfull gaming-dator med RTX 4070', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 2, 8, 'ACC-001', 'USB-C Laddare 65W', 2, 299.00, 0.00, 149.50, 747.50, 'SHIPPED', '/images/products/usbc-charger.jpg', 'Snabbladdare med USB-C', 2, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 2, 10, 'ACC-003', 'Mobil Fodral Premium', 1, 249.00, 0.00, 62.25, 311.25, 'SHIPPED', '/images/products/phone-case.jpg', 'Skyddsfodral i läder', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Order 3 items (PROCESSING)
(5, 3, 9, 'ACC-002', 'Trådlösa Hörlurar ANC', 1, 1495.00, 200.00, 323.75, 1618.75, 'PROCESSING', '/images/products/wireless-earbuds.jpg', 'Bluetooth hörlurar med ANC', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());

-- ================================================
-- 11. PAYMENTS (PayPal transactions)
-- ================================================

INSERT INTO payments (payment_id, order_id, amount, currency, payment_method, payment_status, transaction_id, gateway_payment_id, payment_date, payer_email, payer_name, is_verified, verification_date, retry_count, created_date, updated_date) VALUES
-- Payment for Order 1 (COMPLETED)
(1, 1, 18842.75, 'SEK', 'PAYPAL', 'COMPLETED', 'PAY-1731753600000-123456', 'PAYID-ABC123XYZ', DATE_SUB(NOW(), INTERVAL 10 DAY), 'anna.andersson@example.com', 'Anna Andersson', TRUE, DATE_SUB(NOW(), INTERVAL 10 DAY), 0, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),

-- Payment for Order 2 (COMPLETED)
(2, 2, 31861.25, 'SEK', 'PAYPAL', 'COMPLETED', 'PAY-1732012800000-234567', 'PAYID-DEF456ABC', DATE_SUB(NOW(), INTERVAL 3 DAY), 'erik.berg@example.com', 'Erik Berg', TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY), 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Payment for Order 3 (COMPLETED)
(3, 3, 1917.75, 'SEK', 'PAYPAL', 'COMPLETED', 'PAY-1732185600000-345678', 'PAYID-GHI789DEF', DATE_SUB(NOW(), INTERVAL 1 DAY), 'maria.carlsson@example.com', 'Maria Carlsson', TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ================================================
-- 12. SHIPMENTS (PostNord tracking)
-- ================================================

INSERT INTO shipments (shipment_id, order_id, carrier, tracking_number, shipment_status, shipped_date, estimated_delivery_date, actual_delivery_date, delivered_date, shipping_cost, recipient_name, recipient_phone, recipient_email, shipping_street, shipping_city, shipping_state, shipping_postal_code, shipping_country, shipping_method, weight, dimensions, signature_required, created_date, last_updated) VALUES
-- Shipment for Order 1 (DELIVERED)
(1, 1, 'PostNord', 'PN123456789SE', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 99.00, 'Anna Andersson', '0701234567', 'anna.andersson@example.com', 'Drottninggatan 45', 'Stockholm', 'Stockholm', '11121', 'SE', 'STANDARD', 1.27, '30x21x1.5', FALSE, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Shipment for Order 2 (IN_TRANSIT)
(2, 2, 'PostNord', 'PN234567890SE', 'IN_TRANSIT', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), NULL, NULL, 0.00, 'Erik Berg', '0709876543', 'erik.berg@example.com', 'Avenyn 10, Lägenhet 302', 'Göteborg', 'Västra Götaland', '41136', 'SE', 'EXPRESS', 12.60, '45x20x45', TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),

-- Shipment for Order 3 (PENDING - not yet shipped)
(3, 3, 'PostNord', 'PN345678901SE', 'PENDING', NULL, DATE_ADD(NOW(), INTERVAL 5 DAY), NULL, NULL, 49.00, 'Maria Carlsson', '0731122334', 'maria.carlsson@example.com', 'Stortorget 8', 'Malmö', 'Skåne', '21122', 'SE', 'STANDARD', 0.05, '8x8x3', FALSE, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());

-- ================================================
-- 13. SHIPMENT TRACKING (delivery updates)
-- ================================================

INSERT INTO shipment_tracking (shipment_tracking_id, shipment_id, location, status, description, timestamp, event_code, delivery_confirmation) VALUES
-- Tracking for Shipment 1 (DELIVERED)
(1, 1, 'Stockholm Terminal', 'PICKED_UP', 'Paket upphämtat från avsändare', DATE_SUB(NOW(), INTERVAL 9 DAY), 'PU001', NULL),
(2, 1, 'Stockholm Sortering', 'IN_TRANSIT', 'Paket sorteras på terminal', DATE_SUB(NOW(), INTERVAL 8 DAY), 'IT001', NULL),
(3, 1, 'Stockholm Distribution', 'OUT_FOR_DELIVERY', 'Paket är ute för leverans', DATE_SUB(NOW(), INTERVAL 3 DAY), 'OFD001', NULL),
(4, 1, 'Drottninggatan 45, Stockholm', 'DELIVERED', 'Paket levererat till mottagare', DATE_SUB(NOW(), INTERVAL 3 DAY), 'DL001', 'Lämnat utanför dörren'),

-- Tracking for Shipment 2 (IN_TRANSIT)
(5, 2, 'Stockholm Terminal', 'PICKED_UP', 'Paket upphämtat från avsändare', DATE_SUB(NOW(), INTERVAL 2 DAY), 'PU001', NULL),
(6, 2, 'Hallsberg Logistikcentrum', 'IN_TRANSIT', 'Paket passerar Hallsberg', DATE_SUB(NOW(), INTERVAL 1 DAY), 'IT002', NULL),
(7, 2, 'Göteborg Terminal', 'IN_TRANSIT', 'Anlänt till Göteborg', NOW(), 'IT003', NULL),

-- Tracking for Shipment 3 (PENDING - no tracking yet)
(8, 3, 'Lager', 'PENDING', 'Väntar på upphämtning', DATE_SUB(NOW(), INTERVAL 1 DAY), 'PE001', NULL);

-- ================================================
-- 14. INVENTORY TRANSACTIONS (lagerhistorik)
-- ================================================

INSERT INTO inventory_transactions (inventory_transaction_id, product_id, transaction_type, transaction_date, quantity_before, quantity_after, quantity_change, reason, reference_id, user_id, cost_per_unit, total_cost) VALUES
-- Initial stock for popular products
(1, 1, 'STOCK_IN', DATE_SUB(NOW(), INTERVAL 30 DAY), 0, 20, 20, 'Initial inventory', 'PO-001', 'admin', 11000.00, 220000.00),
(2, 2, 'STOCK_IN', DATE_SUB(NOW(), INTERVAL 30 DAY), 0, 15, 15, 'Initial inventory', 'PO-001', 'admin', 10500.00, 157500.00),
(3, 4, 'STOCK_IN', DATE_SUB(NOW(), INTERVAL 30 DAY), 0, 50, 50, 'Initial inventory', 'PO-002', 'admin', 10000.00, 500000.00),

-- Sales transactions (when orders were placed)
(4, 1, 'STOCK_OUT', DATE_SUB(NOW(), INTERVAL 10 DAY), 20, 19, -1, 'Sold via order', 'ORD-2025-001', 'system', NULL, NULL),
(5, 3, 'STOCK_OUT', DATE_SUB(NOW(), INTERVAL 3 DAY), 6, 5, -1, 'Sold via order', 'ORD-2025-002', 'system', NULL, NULL),
(6, 8, 'STOCK_OUT', DATE_SUB(NOW(), INTERVAL 3 DAY), 102, 100, -2, 'Sold via order', 'ORD-2025-002', 'system', NULL, NULL),
(7, 10, 'STOCK_OUT', DATE_SUB(NOW(), INTERVAL 3 DAY), 201, 200, -1, 'Sold via order', 'ORD-2025-002', 'system', NULL, NULL),
(8, 9, 'STOCK_OUT', DATE_SUB(NOW(), INTERVAL 1 DAY), 46, 45, -1, 'Sold via order', 'ORD-2025-003', 'system', NULL, NULL),

-- Restock transactions
(9, 4, 'STOCK_IN', DATE_SUB(NOW(), INTERVAL 5 DAY), 0, 25, 25, 'Restock from supplier', 'PO-003', 'admin', 10000.00, 250000.00),
(10, 9, 'STOCK_IN', DATE_SUB(NOW(), INTERVAL 2 DAY), 25, 50, 25, 'Restock from supplier', 'PO-004', 'admin', 1100.00, 27500.00),

-- Adjustment (inventory count correction)
(11, 8, 'ADJUSTMENT', NOW(), 100, 100, 0, 'Inventory count verification', 'ADJ-001', 'warehouse', NULL, NULL);

-- ================================================
-- SUMMARY
-- ================================================
-- Categories: 13 (hierarkiska)
-- Products: 20 (olika kategorier och lagerstatusar)
-- Product Images: 10 (additional images)
-- Product Tags: 60+ (SEO tags)
-- Customers: 5 (verified and unverified)
-- Addresses: 10 (shipping and billing)
-- Carts: 4 (3 active, 1 saved, 1 guest)
-- Cart Items: 7
-- Orders: 3 (DELIVERED, SHIPPED, PROCESSING)
-- Order Items: 5
-- Payments: 3 (all COMPLETED via PayPal)
-- Shipments: 3 (different statuses)
-- Shipment Tracking: 8 events
-- Inventory Transactions: 11 (stock movements)
--
-- Total Records: 140+
-- ================================================
