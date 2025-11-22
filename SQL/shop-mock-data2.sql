-- 4.9 PAYMENTS (with ALL columns from Payment.java)
INSERT INTO payments (
    payment_id, order_id, amount, currency, payment_method, payment_status,
    transaction_id, gateway_payment_id, payment_date, payer_email, payer_name,
    is_verified, verification_date, refund_amount, refund_reason, refund_date,
    is_partial_refund, retry_count, last_retry_date, failure_reason, notes,
    created_date, updated_date
) VALUES
-- Completed payments
(1, 1, 2148.97, 'SEK', 'PAYPAL', 'COMPLETED',
 'PAYPAL-TXN-001-2025-09-15', 'PAYID-ABC123XYZ', '2025-09-15 10:35:00',
 'anna.svensson@email.com', 'Anna Svensson',
 true, '2025-09-15 10:35:00', NULL, NULL, NULL,
 false, 0, NULL, NULL, 'Standard PayPal payment',
 '2025-09-15 10:30:00', '2025-09-15 10:35:00'),

(2, 2, 604.97, 'SEK', 'PAYPAL', 'COMPLETED',
 'PAYPAL-TXN-002-2025-10-01', 'PAYID-DEF456ABC', '2025-10-01 15:25:00',
 'erik.andersson@email.com', 'Erik Andersson',
 true, '2025-10-01 15:25:00', NULL, NULL, NULL,
 false, 0, NULL, NULL, 'Quick checkout',
 '2025-10-01 15:20:00', '2025-10-01 15:25:00'),

(3, 3, 2038.97, 'SEK', 'PAYPAL', 'COMPLETED',
 'PAYPAL-TXN-003-2025-11-18', 'PAYID-GHI789DEF', '2025-11-18 11:05:00',
 'magnus@perfect8.com', 'Magnus Berglund',
 true, '2025-11-18 11:05:00', NULL, NULL, NULL,
 false, 0, NULL, NULL, 'Express shipping order',
 '2025-11-18 11:00:00', '2025-11-18 11:05:00'),

(4, 4, 328.97, 'SEK', 'PAYPAL', 'COMPLETED',
 'PAYPAL-TXN-004-2025-11-20', 'PAYID-JKL012GHI', '2025-11-20 09:50:00',
 'sofia.karlsson@email.com', 'Sofia Karlsson',
 true, '2025-11-20 09:50:00', NULL, NULL, NULL,
 false, 0, NULL, NULL, NULL,
 '2025-11-20 09:45:00', '2025-11-20 09:50:00'),

-- Processing payment
(5, 5, 3059.97, 'SEK', 'PAYPAL', 'PROCESSING',
 'PAYPAL-TXN-005-2025-11-21', 'PAYID-MNO345JKL', NULL,
 'anna.svensson@email.com', 'Anna Svensson',
 false, NULL, NULL, NULL, NULL,
 false, 0, NULL, NULL, 'Large order - verification in progress',
 '2025-11-21 14:00:00', '2025-11-21 14:00:00'),

-- Pending payment
(6, 6, 1108.98, 'SEK', 'PAYPAL', 'PENDING',
 'PAYPAL-TXN-006-2025-11-22', NULL, NULL,
 'erik.andersson@email.com', 'Erik Andersson',
 false, NULL, NULL, NULL, NULL,
 false, 0, NULL, NULL, 'Awaiting PayPal confirmation',
 '2025-11-22 08:30:00', '2025-11-22 08:30:00'),

-- Refunded payment (cancelled order)
(7, 7, 4019.96, 'SEK', 'PAYPAL', 'REFUNDED',
 'PAYPAL-TXN-007-2025-10-15', 'PAYID-PQR678MNO', '2025-10-15 10:05:00',
 'sofia.karlsson@email.com', 'Sofia Karlsson',
 true, '2025-10-15 10:05:00', 4019.96, 'Customer changed mind', '2025-10-16 15:00:00',
 false, 0, NULL, NULL, 'Full refund processed',
 '2025-10-15 10:00:00', '2025-10-16 15:00:00');

-- 4.10 SHIPMENTS (with ALL v1.0 columns from Shipment.java)
INSERT INTO shipments (
    shipment_id, order_id, tracking_number, carrier, shipment_status,
    shipped_date, estimated_delivery_date, actual_delivery_date, delivered_date,
    shipping_cost, recipient_name, recipient_phone, recipient_email,
    shipping_address, shipping_street, shipping_city, shipping_state,
    shipping_postal_code, shipping_country, shipping_method,
    weight, dimensions, delivery_instructions, signature_required,
    insurance_amount, label_url, current_location, notes,
    created_date, last_updated
) VALUES
-- Delivered shipment 1
(1, 1, 'PN-SE-2025-001234', 'PostNord', 'DELIVERED',
 '2025-09-16 14:00:00', '2025-09-18', '2025-09-18', '2025-09-18 11:45:00',
 49.00, 'Anna Svensson', '+46709876543', 'anna.svensson@email.com',
 'Drottninggatan 45, Apt 3B, 41103 Göteborg, Sweden',
 'Drottninggatan 45', 'Göteborg', NULL, '41103', 'SE', 'STANDARD',
 2.500, '40x30x15', 'Please ring doorbell', false,
 NULL, 'https://postnord.se/labels/PN-SE-2025-001234.pdf',
 'Göteborg - Delivered', 'Package delivered successfully',
 '2025-09-16 14:00:00', '2025-09-18 11:45:00'),

-- Delivered shipment 2
(2, 2, 'DHL-SE-2025-567890', 'DHL', 'DELIVERED',
 '2025-10-02 09:00:00', '2025-10-04', '2025-10-04', '2025-10-04 16:30:00',
 29.00, 'Erik Andersson', '+46705551234', 'erik.andersson@email.com',
 'Arbetargatan 23, 21240 Malmö, Sweden',
 'Arbetargatan 23', 'Malmö', NULL, '21240', 'SE', 'STANDARD',
 1.200, '30x20x10', NULL, false,
 NULL, 'https://dhl.com/labels/DHL-SE-2025-567890.pdf',
 'Malmö - Delivered', NULL,
 '2025-10-02 09:00:00', '2025-10-04 16:30:00'),

-- Shipped (in transit) shipment 3
(3, 3, 'PN-SE-2025-002345', 'PostNord', 'SHIPPED',
 '2025-11-19 13:30:00', '2025-11-23', NULL, NULL,
 49.00, 'Magnus Berglund', '+46701234567', 'magnus@perfect8.com',
 'Storgatan 12, 11420 Stockholm, Sweden',
 'Storgatan 12', 'Stockholm', NULL, '11420', 'SE', 'EXPRESS',
 1.800, '35x25x12', 'Express delivery', true,
 1000.00, 'https://postnord.se/labels/PN-SE-2025-002345.pdf',
 'Stockholm Local Depot - Out for delivery', 'Priority express shipping',
 '2025-11-19 13:30:00', '2025-11-22 06:30:00'),

-- Shipped (in transit) shipment 4
(4, 4, 'DHL-SE-2025-678901', 'DHL', 'SHIPPED',
 '2025-11-21 10:15:00', '2025-11-24', NULL, NULL,
 29.00, 'Sofia Karlsson', '+46707778899', 'sofia.karlsson@email.com',
 'Vasagatan 10, Lgh 12, 75320 Uppsala, Sweden',
 'Vasagatan 10', 'Uppsala', NULL, '75320', 'SE', 'STANDARD',
 0.800, '25x20x8', NULL, false,
 NULL, 'https://dhl.com/labels/DHL-SE-2025-678901.pdf',
 'Uppsala Hub - Processing', NULL,
 '2025-11-21 10:15:00', '2025-11-21 18:00:00');

-- 4.11 SHIPMENT_TRACKING (with ALL columns from ShipmentTracking.java)
INSERT INTO shipment_tracking (
    shipment_tracking_id, shipment_id, status, location, description, timestamp,
    event_code, event_details, delivery_confirmation, exception_type,
    next_scheduled_delivery
) VALUES
-- Tracking for shipment 1 (delivered)
(1, 1, 'PICKED_UP', 'Stockholm Distribution Center',
 'Package picked up from sender', '2025-09-16 14:00:00',
 'PU', 'Package accepted at distribution center', NULL, NULL, NULL),

(2, 1, 'IN_TRANSIT', 'Göteborg Distribution Center',
 'Package in transit to destination city', '2025-09-17 08:30:00',
 'IT', 'Arrived at destination city sorting facility', NULL, NULL, NULL),

(3, 1, 'OUT_FOR_DELIVERY', 'Göteborg Local Depot',
 'Out for delivery', '2025-09-18 07:00:00',
 'OFD', 'Package loaded on delivery vehicle', NULL, NULL, NULL),

(4, 1, 'DELIVERED', 'Göteborg - Drottninggatan 45',
 'Package delivered successfully', '2025-09-18 11:45:00',
 'DEL', 'Delivered to recipient', 'Signed by recipient', NULL, NULL),

-- Tracking for shipment 2 (delivered)
(5, 2, 'PICKED_UP', 'Stockholm Hub',
 'Package received at DHL facility', '2025-10-02 09:00:00',
 'RCV', 'Package accepted', NULL, NULL, NULL),

(6, 2, 'IN_TRANSIT', 'Malmö Hub',
 'Package in transit', '2025-10-03 14:20:00',
 'IT', 'In transit to destination', NULL, NULL, NULL),

(7, 2, 'DELIVERED', 'Malmö - Arbetargatan 23',
 'Package delivered', '2025-10-04 16:30:00',
 'DEL', 'Successfully delivered', 'Left at door', NULL, NULL),

-- Tracking for shipment 3 (in transit - express)
(8, 3, 'PICKED_UP', 'Stockholm Distribution Center',
 'Express package received', '2025-11-19 13:30:00',
 'PU-EXP', 'Priority express handling initiated', NULL, NULL, NULL),

(9, 3, 'IN_TRANSIT', 'Stockholm Hub',
 'Package sorted and ready for express delivery', '2025-11-20 08:00:00',
 'IT-EXP', 'Express lane processing', NULL, NULL, NULL),

(10, 3, 'OUT_FOR_DELIVERY', 'Stockholm Local Depot',
 'Out for express delivery', '2025-11-22 06:30:00',
 'OFD-EXP', 'Loaded on priority delivery vehicle', NULL, NULL,
 '2025-11-22 12:00:00'),

-- Tracking for shipment 4 (in transit - standard)
(11, 4, 'PICKED_UP', 'Uppsala Distribution Center',
 'Package received', '2025-11-21 10:15:00',
 'RCV', 'Package accepted at distribution center', NULL, NULL, NULL),

(12, 4, 'IN_TRANSIT', 'Uppsala Hub',
 'Package processing', '2025-11-21 18:00:00',
 'IT', 'Sorting in progress', NULL, NULL, NULL);

-- 4.12 INVENTORY_TRANSACTIONS (with ALL columns from InventoryTransaction.java)
INSERT INTO inventory_transactions (
    inventory_transaction_id, product_id, transaction_type, transaction_date,
    quantity_before, quantity_after, quantity_change, reason, reference_id,
    user_id, batch_number, expiry_date, cost_per_unit, total_cost, notes
) VALUES
-- Initial stock setup
(1, 1, 'STOCK_IN', '2025-05-01 10:00:00',
 0, 20, 20, 'Initial inventory', NULL, 'SYSTEM', 'BATCH-2025-001', NULL,
 1200.00, 24000.00, 'Initial stock for Dell XPS 15'),

(2, 4, 'STOCK_IN', '2025-05-10 11:20:00',
 0, 25, 25, 'Initial inventory', NULL, 'SYSTEM', 'BATCH-2025-002', NULL,
 900.00, 22500.00, 'Initial stock for iPhone 15 Pro'),

(3, 6, 'STOCK_IN', '2025-05-15 14:30:00',
 0, 50, 50, 'Initial inventory', NULL, 'SYSTEM', 'BATCH-2025-003', NULL,
 250.00, 12500.00, 'Initial stock for Sony headphones'),

(4, 8, 'STOCK_IN', '2025-05-20 09:45:00',
 0, 60, 60, 'Initial inventory', NULL, 'SYSTEM', 'BATCH-2025-004', NULL,
 90.00, 5400.00, 'Initial stock for gaming keyboard'),

(5, 9, 'STOCK_IN', '2025-05-25 10:15:00',
 0, 70, 70, 'Initial inventory', NULL, 'SYSTEM', 'BATCH-2025-005', NULL,
 60.00, 4200.00, 'Initial stock for MX Master 3'),

-- Sales transactions (order 1)
(6, 1, 'STOCK_OUT', '2025-09-15 10:35:00',
 20, 19, -1, 'Sold via order', 'ORD-2025-0001', 'ORDER-SYSTEM', NULL, NULL,
 1200.00, 1200.00, 'Order ORD-2025-0001 - customer purchase'),

(7, 8, 'STOCK_OUT', '2025-09-15 10:35:00',
 60, 59, -1, 'Sold via order', 'ORD-2025-0001', 'ORDER-SYSTEM', NULL, NULL,
 90.00, 90.00, 'Order ORD-2025-0001 - customer purchase'),

-- Sales transactions (order 2)
(8, 6, 'STOCK_OUT', '2025-10-01 15:25:00',
 50, 49, -1, 'Sold via order', 'ORD-2025-0002', 'ORDER-SYSTEM', NULL, NULL,
 250.00, 250.00, 'Order ORD-2025-0002'),

(9, 10, 'STOCK_OUT', '2025-10-01 15:25:00',
 40, 39, -1, 'Sold via order', 'ORD-2025-0002', 'ORDER-SYSTEM', NULL, NULL,
 50.00, 50.00, 'Order ORD-2025-0002'),

-- Sales transactions (order 3)
(10, 4, 'STOCK_OUT', '2025-11-18 11:05:00',
 25, 24, -1, 'Sold via order', 'ORD-2025-0003', 'ORDER-SYSTEM', NULL, NULL,
 900.00, 900.00, 'Order ORD-2025-0003'),

(11, 6, 'STOCK_OUT', '2025-11-18 11:05:00',
 49, 48, -1, 'Sold via order', 'ORD-2025-0003', 'ORDER-SYSTEM', NULL, NULL,
 250.00, 250.00, 'Order ORD-2025-0003'),

(12, 9, 'STOCK_OUT', '2025-11-18 11:05:00',
 70, 69, -1, 'Sold via order', 'ORD-2025-0003', 'ORDER-SYSTEM', NULL, NULL,
 60.00, 60.00, 'Order ORD-2025-0003'),

-- Sales transactions (order 4)
(13, 8, 'STOCK_OUT', '2025-11-20 09:50:00',
 59, 58, -1, 'Sold via order', 'ORD-2025-0004', 'ORDER-SYSTEM', NULL, NULL,
 90.00, 90.00, 'Order ORD-2025-0004'),

(14, 9, 'STOCK_OUT', '2025-11-20 09:50:00',
 69, 68, -1, 'Sold via order', 'ORD-2025-0004', 'ORDER-SYSTEM', NULL, NULL,
 60.00, 60.00, 'Order ORD-2025-0004'),

-- Sales transactions (order 5)
(15, 2, 'STOCK_OUT', '2025-11-21 14:00:00',
 10, 9, -1, 'Sold via order', 'ORD-2025-0005', 'ORDER-SYSTEM', NULL, NULL,
 1800.00, 1800.00, 'Order ORD-2025-0005'),

(16, 10, 'STOCK_OUT', '2025-11-21 14:00:00',
 39, 38, -1, 'Sold via order', 'ORD-2025-0005', 'ORDER-SYSTEM', NULL, NULL,
 50.00, 50.00, 'Order ORD-2025-0005'),

-- Reserved stock (order 6 - pending)
(17, 5, 'RESERVED', '2025-11-22 08:30:00',
 30, 29, -1, 'Reserved for pending order', 'ORD-2025-0006', 'ORDER-SYSTEM', NULL, NULL,
 NULL, NULL, 'Stock reserved while payment pending'),

-- Return transaction (cancelled order 7)
(18, 11, 'STOCK_IN', '2025-10-16 15:00:00',
 2, 3, 1, 'Order cancellation return', 'ORD-2025-0007', 'REFUND-SYSTEM', NULL, NULL,
 NULL, NULL, 'Stock returned from cancelled order'),

(19, 6, 'STOCK_IN', '2025-10-16 15:00:00',
 48, 49, 1, 'Order cancellation return', 'ORD-2025-0007', 'REFUND-SYSTEM', NULL, NULL,
 NULL, NULL, 'Stock returned from cancelled order'),

(20, 8, 'STOCK_IN', '2025-10-16 15:00:00',
 58, 59, 1, 'Order cancellation return', 'ORD-2025-0007', 'REFUND-SYSTEM', NULL, NULL,
 NULL, NULL, 'Stock returned from cancelled order'),

-- Stock adjustments
(21, 1, 'ADJUSTMENT', '2025-11-10 10:00:00',
 19, 15, -4, 'Inventory correction', NULL, 'ADMIN-001', NULL, NULL,
 NULL, NULL, 'Physical inventory count adjustment'),

(22, 4, 'ADJUSTMENT', '2025-11-15 11:00:00',
 24, 20, -4, 'Damaged units removed', NULL, 'ADMIN-001', NULL, NULL,
 NULL, NULL, 'Damaged during handling - removed from sellable inventory'),

-- Restock
(23, 6, 'STOCK_IN', '2025-11-16 09:00:00',
 49, 89, 40, 'Restocking', NULL, 'SUPPLIER-SONY', 'BATCH-2025-020', NULL,
 250.00, 10000.00, 'Restock from supplier - Sony headphones'),

(24, 10, 'STOCK_IN', '2025-11-17 10:00:00',
 38, 73, 35, 'Restocking', NULL, 'SUPPLIER-LOGI', 'BATCH-2025-021', NULL,
 50.00, 1750.00, 'Restock from supplier - Logitech webcam');

-- Reset auto-increment
ALTER TABLE customers AUTO_INCREMENT = 6;
ALTER TABLE categories AUTO_INCREMENT = 11;
ALTER TABLE products AUTO_INCREMENT = 13;
ALTER TABLE addresses AUTO_INCREMENT = 9;
ALTER TABLE carts AUTO_INCREMENT = 4;
ALTER TABLE cart_items AUTO_INCREMENT = 5;
ALTER TABLE orders AUTO_INCREMENT = 8;
ALTER TABLE order_items AUTO_INCREMENT = 16;
ALTER TABLE payments AUTO_INCREMENT = 8;
ALTER TABLE shipments AUTO_INCREMENT = 5;
ALTER TABLE shipment_tracking AUTO_INCREMENT = 13;
ALTER TABLE inventory_transactions AUTO_INCREMENT = 25;

-- =====================================================
-- 5. VERIFY DATA
-- =====================================================

SELECT '=== DATABASE CREATED ===' as '';
SELECT DATABASE() as current_database;

SELECT '=== TABLES ===' as '';
SHOW TABLES;

SELECT '=== CUSTOMERS ===' as '';
SELECT customer_id, email, CONCAT(first_name, ' ', last_name) as name, 
       is_active, is_email_verified, newsletter_subscribed
FROM customers ORDER BY customer_id;

SELECT '=== CATEGORIES ===' as '';
SELECT category_id, name, slug, parent_category_id, display_order, is_active
FROM categories ORDER BY display_order, category_id;

SELECT '=== PRODUCTS ===' as '';
SELECT p.product_id, p.sku, p.name, p.price, p.discount_price, p.stock_quantity, 
       c.name as category, p.is_active
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
SELECT c.cart_id, CONCAT(cu.first_name, ' ', cu.last_name) as customer,
       c.item_count, c.total_amount, c.discount_amount
FROM carts c
JOIN customers cu ON c.customer_id = cu.customer_id
ORDER BY c.cart_id;

SELECT '=== SHIPMENTS ===' as '';
SELECT s.shipment_id, o.order_number, s.carrier, s.tracking_number, 
       s.shipment_status, s.shipped_date, s.estimated_delivery_date
FROM shipments s
JOIN orders o ON s.order_id = o.order_id
ORDER BY s.shipment_id;

SELECT '=== PAYMENTS ===' as '';
SELECT p.payment_id, o.order_number, p.amount, p.currency, 
       p.payment_method, p.payment_status, p.payment_date
FROM payments p
JOIN orders o ON p.order_id = o.order_id
ORDER BY p.payment_id;

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
    CONCAT('Carts: ', (SELECT COUNT(*) FROM carts)) as carts,
    CONCAT('Payments: ', (SELECT COUNT(*) FROM payments WHERE payment_status = 'COMPLETED')) as completed_payments,
    CONCAT('Total Sales: ', ROUND((SELECT SUM(total_amount) FROM orders WHERE order_status IN ('DELIVERED', 'SHIPPED', 'PROCESSING')), 2), ' SEK') as total_sales;