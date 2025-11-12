-- admin-MOCK-DATA.sql
-- Database: adminDB
-- Created: 2025-11-11
-- Purpose: Insert mock data for testing admin-service

-- ==============================================
-- Mock data for admin_users table
-- NOTE: All passwords are hashed using BCrypt
-- Plain password for all users: "Password123!"
-- ==============================================

-- Clear existing data (optional - uncomment if needed)
-- DELETE FROM admin_users;

-- Super Admin Users
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
(
    'superadmin',
    'superadmin@perfect8.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Super',
    'Administrator',
    'ROLE_SUPER_ADMIN',
    TRUE,
    '2025-01-01 10:00:00.000000',
    '2025-01-01 10:00:00.000000',
    '2025-11-11 08:30:00.000000'
),
(
    'magnus',
    'magnus@perfect8.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Magnus',
    'Berglund',
    'ROLE_SUPER_ADMIN',
    TRUE,
    '2025-01-01 10:00:00.000000',
    '2025-01-01 10:00:00.000000',
    '2025-11-11 09:15:00.000000'
);

-- Regular Admin Users
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
(
    'admin1',
    'admin1@perfect8.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'John',
    'Anderson',
    'ROLE_ADMIN',
    TRUE,
    '2025-02-01 10:00:00.000000',
    '2025-02-01 10:00:00.000000',
    '2025-11-10 14:22:00.000000'
),
(
    'admin2',
    'admin2@perfect8.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Sarah',
    'Johnson',
    'ROLE_ADMIN',
    TRUE,
    '2025-02-15 10:00:00.000000',
    '2025-02-15 10:00:00.000000',
    '2025-11-11 07:45:00.000000'
),
(
    'admin3',
    'admin3@perfect8.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Michael',
    'Chen',
    'ROLE_ADMIN',
    TRUE,
    '2025-03-01 10:00:00.000000',
    '2025-03-01 10:00:00.000000',
    '2025-11-09 16:30:00.000000'
);

-- Regular User (for testing user-level access)
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
(
    'user1',
    'user1@perfect8.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Emma',
    'Wilson',
    'ROLE_USER',
    TRUE,
    '2025-03-15 10:00:00.000000',
    '2025-03-15 10:00:00.000000',
    '2025-11-11 10:00:00.000000'
),
(
    'testuser',
    'testuser@perfect8.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Test',
    'User',
    'ROLE_USER',
    TRUE,
    '2025-04-01 10:00:00.000000',
    '2025-04-01 10:00:00.000000',
    '2025-11-10 12:00:00.000000'
);

-- Inactive User (for testing deactivation)
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
(
    'inactive_admin',
    'inactive@perfect8.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Inactive',
    'Admin',
    'ROLE_ADMIN',
    FALSE,
    '2025-01-15 10:00:00.000000',
    '2025-05-01 10:00:00.000000',
    '2025-04-30 15:00:00.000000'
);

-- ==============================================
-- Verification Query
-- Use this to verify the data was inserted:
-- SELECT username, email, role, active, last_login_date 
-- FROM admin_users 
-- ORDER BY created_date;
-- ==============================================

-- ==============================================
-- Login Test Information
-- ==============================================
-- All users have the same password: "Password123!"
-- 
-- Test Accounts:
-- SUPER_ADMIN: superadmin / Password123!
-- SUPER_ADMIN: magnus / Password123!
-- ADMIN:       admin1 / Password123!
-- ADMIN:       admin2 / Password123!
-- ADMIN:       admin3 / Password123!
-- USER:        user1 / Password123!
-- USER:        testuser / Password123!
-- INACTIVE:    inactive_admin / Password123! (should fail login)
-- ==============================================

-- End of admin-MOCK-DATA.sql
