-- ================================================
-- admin-MOCK-DATA.sql
-- Database: adminDB
-- Created: 2025-11-16
-- Purpose: Mock data for admin-service testing
-- 
-- IMPORTANT NOTES:
-- - Password hash: BCrypt hash for "password123"
-- - Roles: ROLE_USER, ROLE_ADMIN, ROLE_SUPER_ADMIN (from common/enums/Role.java)
-- - Swedish/European names and data
-- ================================================

-- ================================================
-- TRUNCATE TABLE (Safe reload without duplicates)
-- ================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE admin_users;
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- INSERT MOCK DATA: admin_users
-- ================================================

INSERT INTO admin_users (
    username,
    email,
    password_hash,
    first_name,
    last_name,
    role,
    active,
    created_date,
    updated_date,
    last_login_date
) VALUES
-- Super Admin Account (full access)
(
    'superadmin',
    'super@perfect8.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Magnus',
    'Berglund',
    'ROLE_SUPER_ADMIN',
    TRUE,
    '2025-01-01 08:00:00.000000',
    '2025-11-16 09:30:00.000000',
    '2025-11-16 09:30:00.000000'
),

-- Admin Accounts (management access)
(
    'admin.erik',
    'erik.andersson@perfect8.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Erik',
    'Andersson',
    'ROLE_ADMIN',
    TRUE,
    '2025-02-15 10:00:00.000000',
    '2025-11-15 14:20:00.000000',
    '2025-11-15 16:45:00.000000'
),
(
    'admin.anna',
    'anna.karlsson@perfect8.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Anna',
    'Karlsson',
    'ROLE_ADMIN',
    TRUE,
    '2025-03-10 11:30:00.000000',
    '2025-11-16 08:15:00.000000',
    '2025-11-16 08:15:00.000000'
),

-- Regular User Accounts (limited access)
(
    'user.lars',
    'lars.johansson@perfect8.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Lars',
    'Johansson',
    'ROLE_USER',
    TRUE,
    '2025-05-20 09:00:00.000000',
    '2025-11-10 12:30:00.000000',
    '2025-11-14 10:00:00.000000'
),
(
    'user.maria',
    'maria.nilsson@perfect8.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Maria',
    'Nilsson',
    'ROLE_USER',
    TRUE,
    '2025-06-12 14:00:00.000000',
    '2025-11-12 15:45:00.000000',
    '2025-11-13 09:20:00.000000'
),

-- Inactive Account (for testing deactivation)
(
    'inactive.test',
    'inactive@perfect8.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Test',
    'Inaktiv',
    'ROLE_USER',
    FALSE, -- INACTIVE
    '2025-04-01 10:00:00.000000',
    '2025-08-15 16:00:00.000000',
    '2025-08-14 12:00:00.000000'
);

-- ================================================
-- VERIFICATION QUERY (uncomment to test)
-- ================================================

 SELECT 
     admin_user_id,
     username,
     email,
     CONCAT(first_name, ' ', last_name) AS full_name,
     role,
     active,admin_users
     DATE_FORMAT(created_date, '%Y-%m-%d') AS created,
     DATE_FORMAT(last_login_date, '%Y-%m-%d %H:%i') AS last_login
 FROM admin_users
 ORDER BY role DESC, created_date ASC;

-- ================================================
-- End of admin-MOCK-DATA.sql
-- 
-- SUMMARY:
-- - 6 admin users (1 SUPER_ADMIN, 2 ADMIN, 3 USER)
-- - 1 inactive account for testing
-- - All passwords: "password123" (BCrypt hashed)
-- - Swedish names and domains
-- - Ready for frontend testing
-- ================================================
