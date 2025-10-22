-- Email Service - H2 Test Data
-- This file is loaded automatically when using the h2 profile

-- Email Templates
INSERT INTO email_templates (id, template_name, subject, body, created_at, updated_at) VALUES
(1, 'WELCOME', 'Welcome to Perfect8!',
 '<h1>Welcome!</h1><p>Thank you for joining Perfect8. We are excited to have you on board.</p>',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'ORDER_CONFIRMATION', 'Your Order Confirmation',
 '<h1>Order Confirmed!</h1><p>Your order has been successfully placed. Order ID: {{orderId}}</p>',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'ORDER_SHIPPED', 'Your Order Has Shipped',
 '<h1>Order Shipped!</h1><p>Your order {{orderId}} has been shipped. Tracking: {{trackingNumber}}</p>',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'PASSWORD_RESET', 'Reset Your Password',
 '<h1>Password Reset</h1><p>Click here to reset your password: {{resetLink}}</p>',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'NEWSLETTER', 'Perfect8 Newsletter',
 '<h1>Newsletter</h1><p>{{content}}</p>',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Email Log (sample sent emails)
INSERT INTO email_logs (id, recipient_email, subject, status, sent_at, template_id) VALUES
(1, 'john@example.com', 'Welcome to Perfect8!', 'SENT', CURRENT_TIMESTAMP, 1),
(2, 'jane@example.com', 'Your Order Confirmation', 'SENT', CURRENT_TIMESTAMP, 2),
(3, 'bob@example.com', 'Your Order Has Shipped', 'SENT', CURRENT_TIMESTAMP, 3),
(4, 'alice@example.com', 'Reset Your Password', 'PENDING', CURRENT_TIMESTAMP, 4);

-- Note: Email sending is MOCKED in H2 profile
-- Emails are logged but not actually sent