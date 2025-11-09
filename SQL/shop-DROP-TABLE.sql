-- ⚠️ DANGER ZONE ⚠️
-- This file drops ALL tables in shopDB
-- ONLY use for clean slate / development reset
-- NEVER run in production without backup!
-- Created: 2025-11-09

-- Drop tables in reverse order (to respect foreign keys)
DROP TABLE IF EXISTS shipment_tracking;
DROP TABLE IF EXISTS shipments;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS inventory_transactions;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;

-- End of shop-DROP-TABLE.sql
