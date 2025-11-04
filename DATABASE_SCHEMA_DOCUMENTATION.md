# Database Schema Documentation
**Project:** Spring Boot Microservices E-commerce Platform  
**Version:** 1.0  
**Date:** 2025-10-23  
**License:** MIT  

---

## 📊 Overview

The system uses **5 separate databases** following the microservices pattern:

| Database | Service | Purpose |
|----------|---------|---------|
| `adminDB` | admin-service | Admin users, authentication, authorization |
| `blogDB` | blog-service | Blog posts, users, roles |
| `emailDB` | email-service | Email templates, logs |
| `imageDB` | image-service | Image metadata, processing |
| `shopDB` | shop-service | Products, orders, customers, payments |

**Database Engine:** MariaDB 10.11.14 (MySQL 8.0 compatible)  
**Character Set:** utf8mb4  
**Collation:** utf8mb4_unicode_ci  

---

## 🔐 Database: `adminDB`

**Service:** admin-service  
**Port:** 8081  

### Tables:

#### `admin_users`
Admin user accounts with role-based access control.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `admin_user_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique admin user ID |
| `username` | VARCHAR(255) | UNIQUE, NOT NULL | Admin username |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Admin email address |
| `password` | VARCHAR(255) | NOT NULL | Encrypted password (BCrypt) |
| `first_name` | VARCHAR(255) | | Admin first name |
| `last_name` | VARCHAR(255) | | Admin last name |
| `role` | VARCHAR(50) | NOT NULL | Enum: SUPER_ADMIN, SHOP_ADMIN, CONTENT_ADMIN |
| `active` | BOOLEAN | NOT NULL, DEFAULT TRUE | Account active status |
| `created_date` | DATETIME | NOT NULL | Account creation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |
| `last_login` | DATETIME | | Last login timestamp |

**Indexes:**
- PRIMARY KEY on `admin_user_id`
- UNIQUE on `username`
- UNIQUE on `email`

---

## 📝 Database: `blogDB`

**Service:** blog-service  
**Port:** 8082  

### Tables:

#### `users`
Blog authors and editors.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique user ID |
| `username` | VARCHAR(255) | UNIQUE, NOT NULL | Username |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Email address |
| `password` | VARCHAR(255) | NOT NULL | Encrypted password |
| `created_date` | DATETIME | NOT NULL | Registration date |
| `updated_date` | DATETIME | | Last update date |

**Indexes:**
- PRIMARY KEY on `user_id`
- UNIQUE on `username`
- UNIQUE on `email`

#### `roles`
User role definitions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `role_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique role ID |
| `name` | VARCHAR(255) | UNIQUE, NOT NULL | Role name (e.g., AUTHOR, EDITOR) |

**Indexes:**
- PRIMARY KEY on `role_id`
- UNIQUE on `name`

#### `user_roles`
Many-to-many relationship between users and roles.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | BIGINT | FOREIGN KEY → users(user_id) | User reference |
| `role_id` | BIGINT | FOREIGN KEY → roles(role_id) | Role reference |

**Indexes:**
- COMPOSITE PRIMARY KEY on (`user_id`, `role_id`)
- FOREIGN KEY on `user_id` → `users.user_id`
- FOREIGN KEY on `role_id` → `roles.role_id`

#### `posts`
Blog post content.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `post_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique post ID |
| `title` | VARCHAR(255) | NOT NULL | Post title |
| `content` | TEXT | NOT NULL | Post content (markdown/HTML) |
| `slug` | VARCHAR(255) | UNIQUE | URL-friendly post identifier |
| `excerpt` | VARCHAR(500) | | Short description |
| `published` | BOOLEAN | DEFAULT FALSE | Publication status |
| `created_date` | DATETIME | NOT NULL | Creation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |
| `published_at` | DATETIME | | Publication timestamp |
| `author_id` | BIGINT | FOREIGN KEY → users(user_id), NOT NULL | Author reference |

**Indexes:**
- PRIMARY KEY on `post_id`
- UNIQUE on `slug`
- INDEX on `author_id`
- INDEX on `published`
- FOREIGN KEY on `author_id` → `users.user_id`

#### `image_references`
Images associated with blog posts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `image_reference_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique reference ID |
| `image_id` | VARCHAR(255) | NOT NULL | External image ID (from image-service) |
| `image_url` | VARCHAR(500) | | Image URL |
| `alt_text` | VARCHAR(255) | | Alternative text for accessibility |
| `caption` | VARCHAR(500) | | Image caption |
| `post_id` | BIGINT | FOREIGN KEY → posts(post_id) | Post reference |
| `created_date` | DATETIME | NOT NULL | Creation timestamp |

**Indexes:**
- PRIMARY KEY on `image_reference_id`
- INDEX on `post_id`
- FOREIGN KEY on `post_id` → `posts.post_id`

#### `post_links`
External links in blog posts (ElementCollection).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `post_id` | BIGINT | FOREIGN KEY → posts(post_id) | Post reference |
| `url` | VARCHAR(500) | | External link URL |

**Indexes:**
- INDEX on `post_id`
- FOREIGN KEY on `post_id` → `posts.post_id`

---

## 📧 Database: `emailDB`

**Service:** email-service  
**Port:** 8083  

### Tables:

#### `email_templates`
Reusable email templates.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `email_template_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique template ID |
| `name` | VARCHAR(100) | UNIQUE, NOT NULL | Template name |
| `subject` | VARCHAR(500) | NOT NULL | Email subject line |
| `content` | TEXT | NOT NULL | Plain text content |
| `html_content` | TEXT | | HTML content |
| `description` | VARCHAR(500) | | Template description |
| `template_type` | VARCHAR(50) | | ORDER, MARKETING, SYSTEM, NEWSLETTER |
| `category` | VARCHAR(50) | | TRANSACTIONAL, PROMOTIONAL, INFORMATIONAL |
| `active` | BOOLEAN | DEFAULT TRUE | Template active status |
| `version` | BIGINT | | Optimistic locking version |
| `created_by` | VARCHAR(100) | | Creator username |
| `updated_by` | VARCHAR(100) | | Last updater username |
| `created_date` | DATETIME | NOT NULL | Creation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |
| `required_variables` | VARCHAR(1000) | | Comma-separated required variable names |
| `optional_variables` | VARCHAR(1000) | | Comma-separated optional variable names |
| `usage_count` | BIGINT | DEFAULT 0 | Number of times template used |
| `last_used_at` | DATETIME | | Last usage timestamp |

**Indexes:**
- PRIMARY KEY on `email_template_id`
- UNIQUE on `name`
- INDEX on `name`
- INDEX on `active`

---

## 🖼️ Database: `imageDB`

**Service:** image-service  
**Port:** 8084  

### Tables:

#### `images`
Image metadata and processing information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `image_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique image ID |
| `original_filename` | VARCHAR(255) | | Original uploaded filename |
| `stored_filename` | VARCHAR(255) | UNIQUE | Unique stored filename |
| `mime_type` | VARCHAR(100) | | MIME type (image/jpeg, image/png) |
| `image_format` | VARCHAR(50) | | Image format (JPEG, PNG, WEBP) |
| `original_size_bytes` | BIGINT | | Original file size in bytes |
| `original_width` | INT | | Original image width in pixels |
| `original_height` | INT | | Original image height in pixels |
| `original_url` | VARCHAR(500) | | URL to original image |
| `thumbnail_url` | VARCHAR(500) | | URL to thumbnail (150x150) |
| `small_url` | VARCHAR(500) | | URL to small version (300px) |
| `medium_url` | VARCHAR(500) | | URL to medium version (800px) |
| `large_url` | VARCHAR(500) | | URL to large version (1920px) |
| `image_status` | VARCHAR(50) | DEFAULT 'PENDING' | Enum: PENDING, PROCESSING, ACTIVE, FAILED, DELETED |
| `processing_time_ms` | BIGINT | | Processing time in milliseconds |
| `error_message` | TEXT | | Error message if processing failed |
| `alt_text` | VARCHAR(255) | | Alternative text for accessibility |
| `title` | VARCHAR(255) | | Image title |
| `category` | VARCHAR(100) | | Image category |
| `reference_type` | VARCHAR(100) | | Type of entity referencing this image |
| `reference_id` | BIGINT | | ID of referencing entity |
| `is_deleted` | BOOLEAN | DEFAULT FALSE | Soft delete flag |
| `created_date` | DATETIME | NOT NULL | Upload timestamp |
| `updated_date` | DATETIME | | Last update timestamp |

**Indexes:**
- PRIMARY KEY on `image_id`
- UNIQUE on `stored_filename`
- INDEX on `image_status`
- INDEX on `reference_type`, `reference_id`

---

## 🛒 Database: `shopDB`

**Service:** shop-service  
**Port:** 8085  

### Tables:

#### `categories`
Product categories (hierarchical tree structure).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `category_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique category ID |
| `name` | VARCHAR(100) | NOT NULL | Category name |
| `slug` | VARCHAR(150) | UNIQUE, NOT NULL | URL-friendly identifier |
| `description` | TEXT | | Category description |
| `parent_id` | BIGINT | FOREIGN KEY → categories(category_id) | Parent category (NULL for root) |
| `display_order` | INT | DEFAULT 0 | Sort order for display |
| `is_active` | BOOLEAN | DEFAULT TRUE | Category active status |
| `created_date` | DATETIME | NOT NULL | Creation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |

**Indexes:**
- PRIMARY KEY on `category_id`
- UNIQUE on `slug`
- INDEX on `parent_id`
- INDEX on `is_active`
- FOREIGN KEY on `parent_id` → `categories.category_id`

#### `products`
Product catalog.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `product_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique product ID |
| `name` | VARCHAR(255) | NOT NULL | Product name |
| `description` | TEXT | | Product description |
| `sku` | VARCHAR(100) | UNIQUE, NOT NULL | Stock Keeping Unit |
| `price` | DECIMAL(10,2) | NOT NULL | Current selling price |
| `cost` | DECIMAL(10,2) | | Cost price (for profit calculation) |
| `stock_quantity` | INT | NOT NULL, DEFAULT 0 | Available stock quantity |
| `reorder_level` | INT | DEFAULT 10 | Minimum stock before reorder |
| `weight` | DECIMAL(10,2) | | Product weight (kg) |
| `length` | DECIMAL(10,2) | | Length (cm) |
| `width` | DECIMAL(10,2) | | Width (cm) |
| `height` | DECIMAL(10,2) | | Height (cm) |
| `is_active` | BOOLEAN | DEFAULT TRUE | Product active status |
| `is_featured` | BOOLEAN | DEFAULT FALSE | Featured product flag |
| `image_url` | VARCHAR(500) | | Primary product image URL |
| `category_id` | BIGINT | FOREIGN KEY → categories(category_id) | Category reference |
| `created_date` | DATETIME | NOT NULL | Creation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |

**Indexes:**
- PRIMARY KEY on `product_id`
- UNIQUE on `sku`
- INDEX on `category_id`
- INDEX on `is_active`
- INDEX on `is_featured`
- FOREIGN KEY on `category_id` → `categories.category_id`

#### `customers`
Customer accounts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `customer_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique customer ID |
| `first_name` | VARCHAR(100) | NOT NULL | First name |
| `last_name` | VARCHAR(100) | NOT NULL | Last name |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Email address |
| `phone` | VARCHAR(20) | | Phone number |
| `password` | VARCHAR(255) | NOT NULL | Encrypted password |
| `is_active` | BOOLEAN | DEFAULT TRUE | Account active status |
| `email_verified` | BOOLEAN | DEFAULT FALSE | Email verification status |
| `created_date` | DATETIME | NOT NULL | Registration timestamp |
| `updated_date` | DATETIME | | Last update timestamp |
| `last_login` | DATETIME | | Last login timestamp |

**Indexes:**
- PRIMARY KEY on `customer_id`
- UNIQUE on `email`
- INDEX on `is_active`

#### `addresses`
Customer shipping/billing addresses.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `address_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique address ID |
| `street_address` | VARCHAR(255) | NOT NULL | Street address |
| `city` | VARCHAR(100) | NOT NULL | City |
| `state_province` | VARCHAR(100) | | State/Province |
| `postal_code` | VARCHAR(20) | NOT NULL | Postal/ZIP code |
| `country` | VARCHAR(100) | NOT NULL | Country |
| `address_type` | VARCHAR(50) | | SHIPPING or BILLING |
| `is_default` | BOOLEAN | DEFAULT FALSE | Default address flag |
| `customer_id` | BIGINT | FOREIGN KEY → customers(customer_id), NOT NULL | Customer reference |
| `created_date` | DATETIME | NOT NULL | Creation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |

**Indexes:**
- PRIMARY KEY on `address_id`
- INDEX on `customer_id`
- FOREIGN KEY on `customer_id` → `customers.customer_id`

#### `carts`
Shopping cart sessions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `cart_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique cart ID |
| `customer_id` | BIGINT | FOREIGN KEY → customers(customer_id) | Customer reference (NULL for guest) |
| `session_id` | VARCHAR(255) | | Session identifier for guest carts |
| `created_date` | DATETIME | NOT NULL | Cart creation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |
| `expires_at` | DATETIME | | Cart expiration time |

**Indexes:**
- PRIMARY KEY on `cart_id`
- INDEX on `customer_id`
- INDEX on `session_id`
- FOREIGN KEY on `customer_id` → `customers.customer_id`

#### `cart_items`
Items in shopping carts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `cart_item_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique cart item ID |
| `cart_id` | BIGINT | FOREIGN KEY → carts(cart_id), NOT NULL | Cart reference |
| `product_id` | BIGINT | FOREIGN KEY → products(product_id), NOT NULL | Product reference |
| `quantity` | INT | NOT NULL, DEFAULT 1 | Quantity in cart |
| `price_at_addition` | DECIMAL(10,2) | NOT NULL | Product price when added |
| `added_date` | DATETIME | NOT NULL | Item addition timestamp |

**Indexes:**
- PRIMARY KEY on `cart_item_id`
- INDEX on `cart_id`
- INDEX on `product_id`
- FOREIGN KEY on `cart_id` → `carts.cart_id`
- FOREIGN KEY on `product_id` → `products.product_id`

#### `orders`
Customer orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `order_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique order ID |
| `order_number` | VARCHAR(50) | UNIQUE, NOT NULL | Human-readable order number |
| `customer_id` | BIGINT | FOREIGN KEY → customers(customer_id), NOT NULL | Customer reference |
| `order_status` | VARCHAR(50) | NOT NULL | Enum: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED |
| `order_date` | DATETIME | NOT NULL | Order placement timestamp |
| `subtotal` | DECIMAL(10,2) | NOT NULL | Subtotal before tax/shipping |
| `tax_amount` | DECIMAL(10,2) | DEFAULT 0 | Tax amount |
| `shipping_cost` | DECIMAL(10,2) | DEFAULT 0 | Shipping cost |
| `total_amount` | DECIMAL(10,2) | NOT NULL | Final total amount |
| `currency` | VARCHAR(3) | DEFAULT 'USD' | Currency code (ISO 4217) |
| `shipping_address_id` | BIGINT | FOREIGN KEY → addresses(address_id) | Shipping address reference |
| `billing_address_id` | BIGINT | FOREIGN KEY → addresses(address_id) | Billing address reference |
| `notes` | TEXT | | Order notes |
| `created_date` | DATETIME | NOT NULL | Creation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |

**Indexes:**
- PRIMARY KEY on `order_id`
- UNIQUE on `order_number`
- INDEX on `customer_id`
- INDEX on `order_status`
- INDEX on `order_date`
- FOREIGN KEY on `customer_id` → `customers.customer_id`
- FOREIGN KEY on `shipping_address_id` → `addresses.address_id`
- FOREIGN KEY on `billing_address_id` → `addresses.address_id`

#### `order_items`
Line items in orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `order_item_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique order item ID |
| `order_id` | BIGINT | FOREIGN KEY → orders(order_id), NOT NULL | Order reference |
| `product_id` | BIGINT | FOREIGN KEY → products(product_id), NOT NULL | Product reference |
| `quantity` | INT | NOT NULL | Quantity ordered |
| `unit_price` | DECIMAL(10,2) | NOT NULL | Price per unit at time of order |
| `subtotal` | DECIMAL(10,2) | NOT NULL | Line item subtotal (quantity × unit_price) |
| `item_status` | VARCHAR(50) | DEFAULT 'PENDING' | Enum: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED |

**Indexes:**
- PRIMARY KEY on `order_item_id`
- INDEX on `order_id`
- INDEX on `product_id`
- FOREIGN KEY on `order_id` → `orders.order_id`
- FOREIGN KEY on `product_id` → `products.product_id`

#### `payments`
Payment transactions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `payment_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique payment ID |
| `order_id` | BIGINT | FOREIGN KEY → orders(order_id), NOT NULL | Order reference |
| `amount` | DECIMAL(10,2) | NOT NULL | Payment amount |
| `currency` | VARCHAR(3) | DEFAULT 'USD' | Currency code |
| `payment_method` | VARCHAR(50) | NOT NULL | Enum: CREDIT_CARD, PAYPAL, STRIPE, CASH_ON_DELIVERY |
| `payment_status` | VARCHAR(50) | NOT NULL | Enum: PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED |
| `transaction_id` | VARCHAR(255) | UNIQUE | External payment gateway transaction ID |
| `payment_date` | DATETIME | | Payment completion timestamp |
| `created_date` | DATETIME | NOT NULL | Payment initiation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |

**Indexes:**
- PRIMARY KEY on `payment_id`
- UNIQUE on `transaction_id`
- INDEX on `order_id`
- INDEX on `payment_status`
- FOREIGN KEY on `order_id` → `orders.order_id`

#### `shipments`
Order shipment tracking.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `shipment_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique shipment ID |
| `order_id` | BIGINT | FOREIGN KEY → orders(order_id), NOT NULL | Order reference |
| `tracking_number` | VARCHAR(100) | UNIQUE | Carrier tracking number |
| `carrier` | VARCHAR(100) | | Shipping carrier name |
| `shipment_status` | VARCHAR(50) | NOT NULL | Enum: PENDING, IN_TRANSIT, DELIVERED, RETURNED, LOST |
| `shipped_date` | DATETIME | | Shipment date |
| `estimated_delivery` | DATE | | Estimated delivery date |
| `actual_delivery` | DATETIME | | Actual delivery timestamp |
| `created_date` | DATETIME | NOT NULL | Creation timestamp |
| `updated_date` | DATETIME | | Last update timestamp |

**Indexes:**
- PRIMARY KEY on `shipment_id`
- UNIQUE on `tracking_number`
- INDEX on `order_id`
- INDEX on `shipment_status`
- FOREIGN KEY on `order_id` → `orders.order_id`

#### `shipment_tracking`
Detailed shipment tracking history.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `tracking_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique tracking event ID |
| `shipment_id` | BIGINT | FOREIGN KEY → shipments(shipment_id), NOT NULL | Shipment reference |
| `tracking_status` | VARCHAR(100) | NOT NULL | Status description |
| `location` | VARCHAR(255) | | Current/last known location |
| `tracking_date` | DATETIME | NOT NULL | Event timestamp |
| `notes` | TEXT | | Additional tracking notes |

**Indexes:**
- PRIMARY KEY on `tracking_id`
- INDEX on `shipment_id`
- INDEX on `tracking_date`
- FOREIGN KEY on `shipment_id` → `shipments.shipment_id`

#### `inventory_transactions`
Stock movement history.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `transaction_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique transaction ID |
| `product_id` | BIGINT | FOREIGN KEY → products(product_id), NOT NULL | Product reference |
| `transaction_type` | VARCHAR(50) | NOT NULL | Enum: PURCHASE, SALE, ADJUSTMENT, RETURN, DAMAGE |
| `quantity_change` | INT | NOT NULL | Quantity change (positive or negative) |
| `quantity_after` | INT | NOT NULL | Stock quantity after transaction |
| `reference_id` | BIGINT | | Reference to order/purchase/etc |
| `reference_type` | VARCHAR(50) | | Type of reference (ORDER, PURCHASE, etc) |
| `notes` | TEXT | | Transaction notes |
| `transaction_date` | DATETIME | NOT NULL | Transaction timestamp |
| `created_by` | VARCHAR(100) | | User who created transaction |

**Indexes:**
- PRIMARY KEY on `transaction_id`
- INDEX on `product_id`
- INDEX on `transaction_type`
- INDEX on `transaction_date`
- FOREIGN KEY on `product_id` → `products.product_id`

---

## 🔗 Inter-Service Relationships

While databases are separate, services communicate via REST APIs:

- **blog-service** → **image-service**: References images by `image_id`
- **shop-service** → **email-service**: Triggers order confirmation emails
- **shop-service** → **image-service**: References product images
- **admin-service** → **image-service**: Manages uploaded images
- **admin-service** → **email-service**: Manages email templates

---

## 📈 Database Statistics (Estimated)

| Database | Tables | Columns | Relationships |
|----------|--------|---------|---------------|
| `adminDB` | 1 | 11 | 0 |
| `blogDB` | 5 | 32 | 4 |
| `emailDB` | 1 | 17 | 0 |
| `imageDB` | 1 | 23 | 0 |
| `shopDB` | 13 | 127 | 15 |
| **TOTAL** | **21** | **210** | **19** |

---

## 🔐 Security Considerations

- All passwords stored using **BCrypt** hashing
- Sensitive data (payment info) should be PCI-DSS compliant
- Soft deletes used where appropriate (`is_deleted` flag)
- Optimistic locking with `@Version` on critical tables

---

## 🚀 Automatic Schema Management

**Hibernate DDL Auto:** `update`

- Tables/columns created automatically on first run
- Schema updates applied automatically on entity changes
- No manual SQL migrations needed for v1.0

**Production Best Practice (v2.0):**
- Use Flyway/Liquibase for versioned migrations
- Set `ddl-auto=validate` in production

---

## 📝 Notes

- All timestamps use `DATETIME` (not `TIMESTAMP`) for timezone flexibility
- Foreign keys use `ON DELETE CASCADE` or `ON DELETE SET NULL` where appropriate
- Indexes optimized for common query patterns
- Character set utf8mb4 supports emoji and international characters

---

**Generated:** 2025-10-23  
**Version:** 1.0  
**Maintained by:** Development Team  
**License:** MIT
