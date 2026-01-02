-- =====================================================
-- EMAIL-SERVICE KOMPLETT SETUP (FIXED)
-- Skapar: databas, tabeller, mock-data
-- Database: emailDB
-- FIX: Corrected all columns to match EmailTemplate.java
-- =====================================================

-- 1. Skapa databas
CREATE DATABASE IF NOT EXISTS emailDB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE emailDB;

-- 2. Ta bort gamla tabeller (om de finns)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS email_templates;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 3. SKAPA TABELLER
-- =====================================================

-- 3.1 EMAIL_TEMPLATES (FIXED: all columns from EmailTemplate.java)
CREATE TABLE email_templates (
    email_template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    subject VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    html_content TEXT,
    description VARCHAR(500),
    template_type VARCHAR(50),
    category VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    required_variables VARCHAR(1000),
    optional_variables VARCHAR(1000),
    usage_count BIGINT NOT NULL DEFAULT 0,
    last_used_date DATETIME,
    INDEX idx_template_name (name),
    INDEX idx_template_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. MOCK DATA
-- =====================================================

-- 4.1 EMAIL_TEMPLATES
INSERT INTO email_templates (
    email_template_id, name, subject, content, html_content, description, 
    template_type, category, active, version, created_by, updated_by,
    created_date, updated_date, required_variables, optional_variables, 
    usage_count, last_used_date
) VALUES
-- Order Confirmation
(1, 'order-confirmation', 
'Order Confirmation - Order #{{orderNumber}}',
'Hi {{customerName}},

Your order has been confirmed and will be shipped soon.

Order Details:
Order Number: {{orderNumber}}
Order Date: {{orderDate}}
Total Amount: {{totalAmount}}

Items Ordered:
{{#items}}
- {{productName}} - Quantity: {{quantity}} - {{itemPrice}}
{{/items}}

You can track your order status in your account.

Thank you for shopping with Perfect8!',
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
        .content { background-color: #f9f9f9; padding: 20px; }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
        .order-details { background-color: white; padding: 15px; margin: 15px 0; }
        .item { border-bottom: 1px solid #ddd; padding: 10px 0; }
        .total { font-size: 18px; font-weight: bold; color: #4CAF50; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Thank You for Your Order!</h1>
        </div>
        <div class="content">
            <p>Hi {{customerName}},</p>
            <p>Your order has been confirmed and will be shipped soon.</p>
            <div class="order-details">
                <h3>Order Details</h3>
                <p><strong>Order Number:</strong> {{orderNumber}}</p>
                <p><strong>Order Date:</strong> {{orderDate}}</p>
                <p><strong>Total Amount:</strong> <span class="total">{{totalAmount}}</span></p>
            </div>
            <h3>Items Ordered:</h3>
            {{#items}}
            <div class="item">
                <strong>{{productName}}</strong> - Quantity: {{quantity}} - {{itemPrice}}
            </div>
            {{/items}}
            <p>You can track your order status in your account.</p>
        </div>
        <div class="footer">
            <p>Perfect8 - Your trusted e-commerce partner</p>
            <p>© 2025 Perfect8. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
'Email sent to customers when their order is confirmed',
'ORDER',
'TRANSACTIONAL',
true,
1,
'system',
'system',
'2025-01-01 08:00:00',
'2025-01-01 08:00:00',
'customerName,orderNumber,orderDate,totalAmount,items',
'',
0,
NULL),

-- Order Shipped
(2, 'order-shipped',
'Your Order Has Been Shipped - Order #{{orderNumber}}',
'Hi {{customerName}},

Great news! Your order #{{orderNumber}} has been shipped.

Tracking Information:
Carrier: {{carrier}}
Tracking Number: {{trackingNumber}}
Estimated Delivery: {{estimatedDelivery}}

You can track your package using the tracking number above.

Thank you for shopping with Perfect8!',
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
        .content { background-color: #f9f9f9; padding: 20px; }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
        .tracking { background-color: white; padding: 15px; margin: 15px 0; text-align: center; }
        .tracking-number { font-size: 20px; font-weight: bold; color: #2196F3; padding: 10px; background-color: #e3f2fd; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📦 Your Order is On Its Way!</h1>
        </div>
        <div class="content">
            <p>Hi {{customerName}},</p>
            <p>Great news! Your order #{{orderNumber}} has been shipped.</p>
            <div class="tracking">
                <h3>Tracking Information</h3>
                <p><strong>Carrier:</strong> {{carrier}}</p>
                <p><strong>Tracking Number:</strong></p>
                <div class="tracking-number">{{trackingNumber}}</div>
                <p><strong>Estimated Delivery:</strong> {{estimatedDelivery}}</p>
            </div>
            <p>You can track your package using the tracking number above.</p>
        </div>
        <div class="footer">
            <p>Perfect8 - Your trusted e-commerce partner</p>
            <p>© 2025 Perfect8. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
'Email sent to customers when their order is shipped',
'ORDER',
'TRANSACTIONAL',
true,
1,
'system',
'system',
'2025-01-01 08:00:00',
'2025-01-01 08:00:00',
'customerName,orderNumber,carrier,trackingNumber,estimatedDelivery',
'',
0,
NULL),

-- Order Cancelled
(3, 'order-cancelled',
'Order Cancelled - Order #{{orderNumber}}',
'Hi {{customerName}},

Your order #{{orderNumber}} has been cancelled as requested.

Refund Information:
Order Number: {{orderNumber}}
Cancellation Date: {{cancellationDate}}
Refund Amount: {{refundAmount}}
Reason: {{cancellationReason}}

Your refund will be processed within 5-7 business days.

We hope to serve you again soon!',
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
        .content { background-color: #f9f9f9; padding: 20px; }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
        .refund-info { background-color: white; padding: 15px; margin: 15px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Order Cancelled</h1>
        </div>
        <div class="content">
            <p>Hi {{customerName}},</p>
            <p>Your order #{{orderNumber}} has been cancelled as requested.</p>
            <div class="refund-info">
                <h3>Refund Information</h3>
                <p><strong>Order Number:</strong> {{orderNumber}}</p>
                <p><strong>Cancellation Date:</strong> {{cancellationDate}}</p>
                <p><strong>Refund Amount:</strong> {{refundAmount}}</p>
                <p><strong>Reason:</strong> {{cancellationReason}}</p>
            </div>
            <p>Your refund will be processed within 5-7 business days.</p>
            <p>We hope to serve you again soon!</p>
        </div>
        <div class="footer">
            <p>Perfect8 - Your trusted e-commerce partner</p>
            <p>© 2025 Perfect8. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
'Email sent to customers when their order is cancelled',
'ORDER',
'TRANSACTIONAL',
true,
1,
'system',
'system',
'2025-01-01 08:00:00',
'2025-01-01 08:00:00',
'customerName,orderNumber,cancellationDate,refundAmount,cancellationReason',
'',
0,
NULL),

-- Welcome Email
(4, 'welcome',
'Welcome to Perfect8!',
'Hi {{customerName}},

Welcome to Perfect8! We are excited to have you as part of our community.

Your account has been successfully created.
Email: {{email}}

Please verify your email by clicking the link below:
{{verificationLink}}

Start shopping and discover amazing products!',
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #9C27B0; color: white; padding: 20px; text-align: center; }
        .content { background-color: #f9f9f9; padding: 20px; }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
        .welcome-box { background-color: white; padding: 20px; margin: 15px 0; text-align: center; }
        .button { display: inline-block; padding: 10px 30px; background-color: #9C27B0; color: white; text-decoration: none; border-radius: 5px; margin: 10px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎉 Welcome to Perfect8!</h1>
        </div>
        <div class="content">
            <p>Hi {{customerName}},</p>
            <p>Welcome to Perfect8! We are excited to have you as part of our community.</p>
            <div class="welcome-box">
                <h3>Get Started</h3>
                <p>Your account has been successfully created.</p>
                <p><strong>Email:</strong> {{email}}</p>
                <a href="{{verificationLink}}" class="button">Verify Your Email</a>
            </div>
            <p>Start shopping and discover amazing products!</p>
        </div>
        <div class="footer">
            <p>Perfect8 - Your trusted e-commerce partner</p>
            <p>© 2025 Perfect8. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
'Welcome email sent to new customers',
'SYSTEM',
'TRANSACTIONAL',
true,
1,
'system',
'system',
'2025-01-01 08:00:00',
'2025-01-01 08:00:00',
'customerName,email,verificationLink',
'',
0,
NULL),

-- Password Reset
(5, 'password-reset',
'Reset Your Password - Perfect8',
'Hi {{customerName}},

We received a request to reset your password for your Perfect8 account.

Click the link below to reset your password:
{{resetLink}}

This link will expire in 1 hour.

If you did not request a password reset, please ignore this email or contact support if you have concerns.',
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #FF9800; color: white; padding: 20px; text-align: center; }
        .content { background-color: #f9f9f9; padding: 20px; }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
        .reset-box { background-color: white; padding: 20px; margin: 15px 0; text-align: center; }
        .button { display: inline-block; padding: 10px 30px; background-color: #FF9800; color: white; text-decoration: none; border-radius: 5px; margin: 10px 0; }
        .warning { color: #f44336; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔐 Reset Your Password</h1>
        </div>
        <div class="content">
            <p>Hi {{customerName}},</p>
            <p>We received a request to reset your password for your Perfect8 account.</p>
            <div class="reset-box">
                <h3>Reset Your Password</h3>
                <p>Click the button below to reset your password:</p>
                <a href="{{resetLink}}" class="button">Reset Password</a>
                <p class="warning">This link will expire in 1 hour.</p>
            </div>
            <p>If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>
        </div>
        <div class="footer">
            <p>Perfect8 - Your trusted e-commerce partner</p>
            <p>© 2025 Perfect8. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
'Password reset email sent to customers',
'SYSTEM',
'TRANSACTIONAL',
true,
1,
'system',
'system',
'2025-01-01 08:00:00',
'2025-01-01 08:00:00',
'customerName,resetLink',
'',
0,
NULL),

-- Newsletter
(6, 'newsletter',
'{{subject}}',
'Hi {{subscriberName}},

{{content}}

Thank you for being a valued subscriber!

To unsubscribe: {{unsubscribeLink}}',
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #00BCD4; color: white; padding: 20px; text-align: center; }
        .content { background-color: #f9f9f9; padding: 20px; }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
        .article { background-color: white; padding: 15px; margin: 15px 0; }
        .unsubscribe { font-size: 11px; color: #999; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Perfect8 Newsletter</h1>
        </div>
        <div class="content">
            <p>Hi {{subscriberName}},</p>
            <div class="article">
                {{content}}
            </div>
            <p>Thank you for being a valued subscriber!</p>
        </div>
        <div class="footer">
            <p>Perfect8 - Your trusted e-commerce partner</p>
            <p class="unsubscribe">
                <a href="{{unsubscribeLink}}">Unsubscribe from newsletter</a>
            </p>
            <p>© 2025 Perfect8. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
'Newsletter template for marketing campaigns',
'NEWSLETTER',
'PROMOTIONAL',
true,
1,
'system',
'system',
'2025-01-01 08:00:00',
'2025-01-01 08:00:00',
'subscriberName,content,subject',
'unsubscribeLink',
0,
NULL),

-- New Post Notification
(7, 'new-post-notification',
'New Blog Post: {{postTitle}}',
'Hi there,

We just published a new blog post that you might find interesting!

{{postTitle}}
By {{authorName}} on {{publishDate}}

{{postExcerpt}}

Read the full post: {{postLink}}

Stay tuned for more updates!',
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #673AB7; color: white; padding: 20px; text-align: center; }
        .content { background-color: #f9f9f9; padding: 20px; }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
        .post-preview { background-color: white; padding: 20px; margin: 15px 0; }
        .button { display: inline-block; padding: 10px 30px; background-color: #673AB7; color: white; text-decoration: none; border-radius: 5px; margin: 10px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📝 New Blog Post!</h1>
        </div>
        <div class="content">
            <p>Hi there,</p>
            <p>We just published a new blog post that you might find interesting!</p>
            <div class="post-preview">
                <h2>{{postTitle}}</h2>
                <p><em>By {{authorName}} on {{publishDate}}</em></p>
                <p>{{postExcerpt}}</p>
                <a href="{{postLink}}" class="button">Read Full Post</a>
            </div>
            <p>Stay tuned for more updates!</p>
        </div>
        <div class="footer">
            <p>Perfect8 - Your trusted e-commerce partner</p>
            <p>© 2025 Perfect8. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
'Notification email for new blog posts',
'SYSTEM',
'INFORMATIONAL',
true,
1,
'system',
'system',
'2025-01-01 08:00:00',
'2025-01-01 08:00:00',
'postTitle,authorName,publishDate,postExcerpt,postLink',
'',
0,
NULL);

-- Reset auto-increment
ALTER TABLE email_templates AUTO_INCREMENT = 8;

-- =====================================================
-- 5. VERIFY DATA
-- =====================================================

SELECT '=== DATABASE CREATED ===' as '';
SELECT DATABASE() as current_database;

SELECT '=== TABLES ===' as '';
SHOW TABLES;

SELECT '=== EMAIL_TEMPLATES ===' as '';
SELECT 
    email_template_id,
    name,
    subject,
    template_type,
    category,
    active,
    usage_count,
    created_date
FROM email_templates 
ORDER BY email_template_id;

SELECT '=== TEMPLATES BY TYPE ===' as '';
SELECT 
    template_type,
    COUNT(*) as count
FROM email_templates 
GROUP BY template_type
ORDER BY template_type;

SELECT '=== TEMPLATES BY CATEGORY ===' as '';
SELECT 
    category,
    COUNT(*) as count
FROM email_templates 
GROUP BY category
ORDER BY category;

SELECT '=== SETUP COMPLETE ===' as '';
SELECT CONCAT('Total templates: ', COUNT(*)) as summary FROM email_templates;