-- =====================================================
-- 4. MOCK DATA
-- =====================================================

-- Lösenord för alla användare: password123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi

-- 4.1 USERS
INSERT INTO users (user_id, email, password_hash, first_name, last_name, is_active, is_email_verified, email_verification_token, reset_password_token, reset_password_token_expiry, failed_login_attempts, account_locked_until, created_date, updated_date, last_login_date) VALUES
(1, 'magnus@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Magnus', 'Berglund', true, true, NULL, NULL, NULL, 0, NULL, '2025-01-01 08:00:00', '2025-01-01 08:00:00', '2025-11-22 09:00:00'),
(2, 'admin@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Admin', 'User', true, true, NULL, NULL, NULL, 0, NULL, '2025-01-05 10:00:00', '2025-01-05 10:00:00', '2025-11-21 14:30:00'),
(3, 'writer@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Alice', 'Writer', true, true, NULL, NULL, NULL, 0, NULL, '2025-02-10 09:00:00', '2025-02-10 09:00:00', '2025-11-20 16:45:00'),
(4, 'staff@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Bob', 'Staff', true, true, NULL, NULL, NULL, 0, NULL, '2025-03-15 11:00:00', '2025-03-15 11:00:00', '2025-11-19 10:15:00'),
(5, 'john.doe@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'John', 'Doe', true, true, NULL, NULL, NULL, 0, NULL, '2025-04-01 12:00:00', '2025-04-01 12:00:00', '2025-11-18 08:20:00'),
(6, 'jane.smith@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Jane', 'Smith', true, true, NULL, NULL, NULL, 0, NULL, '2025-05-10 14:00:00', '2025-05-10 14:00:00', '2025-11-17 15:30:00'),
(7, 'unverified@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Charlie', 'Unverified', true, false, 'verify_token_12345', NULL, NULL, 0, NULL, '2025-06-15 10:00:00', '2025-06-15 10:00:00', NULL),
(8, 'inactive@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Inactive', 'User', false, true, NULL, NULL, NULL, 0, NULL, '2025-07-20 09:00:00', '2025-08-01 12:00:00', '2025-08-01 12:00:00'),
(9, 'locked@perfect8.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7JrRqX.Qq4YzFWbVYBHVCQ8.6Y8F4vi', 'Locked', 'User', true, true, NULL, NULL, NULL, 5, '2025-12-22 10:00:00', '2025-08-10 08:00:00', '2025-08-10 08:00:00', '2025-11-15 09:00:00');

-- 4.2 USER_ROLES
INSERT INTO user_roles (user_id, role) VALUES
-- Magnus: SUPER_ADMIN
(1, 'SUPER_ADMIN'),
(1, 'ADMIN'),
(1, 'STAFF'),
(1, 'WRITER'),
(1, 'USER'),
-- Admin User
(2, 'ADMIN'),
(2, 'STAFF'),
(2, 'USER'),
-- Writer
(3, 'WRITER'),
(3, 'USER'),
-- Staff
(4, 'STAFF'),
(4, 'USER'),
-- Regular users
(5, 'USER'),
(6, 'USER'),
(7, 'USER'),
(8, 'USER'),
(9, 'USER');

-- 4.3 REFRESH_TOKENS
INSERT INTO refresh_tokens (token_id, user_id, token, expires_at, created_date, revoked, revoked_date, replaced_by_token, device_info, ip_address) VALUES
(1, 1, 'refresh_token_magnus_active_chrome', '2025-12-22 09:00:00', '2025-11-22 09:00:00', false, NULL, NULL, 'Chrome 120/Windows 11', '192.168.1.100'),
(2, 2, 'refresh_token_admin_active_firefox', '2025-12-21 14:30:00', '2025-11-21 14:30:00', false, NULL, NULL, 'Firefox 121/Ubuntu', '192.168.1.101'),
(3, 3, 'refresh_token_writer_active_safari', '2025-12-20 16:45:00', '2025-11-20 16:45:00', false, NULL, NULL, 'Safari 17/MacOS', '192.168.1.102'),
(4, 5, 'refresh_token_john_revoked_old', '2025-12-18 08:20:00', '2025-11-18 08:20:00', true, '2025-11-19 10:00:00', 'refresh_token_john_active_new', 'Chrome 120/Windows 10', '192.168.1.103'),
(5, 5, 'refresh_token_john_active_new', '2025-12-19 10:00:00', '2025-11-19 10:00:00', false, NULL, NULL, 'Chrome 120/Windows 10', '192.168.1.103'),
(6, 6, 'refresh_token_jane_expired', '2025-11-16 15:30:00', '2025-10-17 15:30:00', false, NULL, NULL, 'Edge 120/Windows 11', '192.168.1.104');

-- Reset auto-increment
ALTER TABLE users AUTO_INCREMENT = 10;
ALTER TABLE refresh_tokens AUTO_INCREMENT = 7;

-- =====================================================
-- 5. VERIFY DATA
-- =====================================================

SELECT '=== DATABASE CREATED ===' as '';
SELECT DATABASE() as current_database;

SELECT '=== TABLES ===' as '';
SHOW TABLES;

SELECT '=== USERS ===' as '';
SELECT 
    user_id,
    email,
    CONCAT(first_name, ' ', last_name) as name,
    is_active,
    is_email_verified,
    failed_login_attempts,
    CASE WHEN account_locked_until IS NOT NULL AND account_locked_until > NOW() THEN 'LOCKED' ELSE 'OK' END as lock_status
FROM users 
ORDER BY user_id;

SELECT '=== USER_ROLES ===' as '';
SELECT user_id, GROUP_CONCAT(role ORDER BY role SEPARATOR ', ') as roles
FROM user_roles 
GROUP BY user_id
ORDER BY user_id;

SELECT '=== REFRESH_TOKENS ===' as '';
SELECT 
    token_id,
    user_id,
    LEFT(token, 35) as token_preview,
    expires_at,
    revoked,
    CASE 
        WHEN revoked THEN 'REVOKED'
        WHEN expires_at < NOW() THEN 'EXPIRED' 
        ELSE 'VALID' 
    END as status
FROM refresh_tokens 
ORDER BY token_id;

SELECT '=== SETUP COMPLETE ===' as '';
SELECT CONCAT('Total users: ', COUNT(*)) as summary FROM users;