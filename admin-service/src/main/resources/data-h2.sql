-- Admin Service - H2 Test Data
-- This file is loaded automatically when using the h2 profile

-- Admin Users (password: admin123)
INSERT INTO admin_users (id, username, email, password, role, created_at, updated_at) VALUES
(1, 'admin', 'admin@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'manager', 'manager@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'support', 'support@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SUPPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Note: All passwords are 'admin123' hashed with BCrypt
-- Login with: username='admin', password='admin123'