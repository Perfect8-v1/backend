-- ============================================
-- Fix customer@p8.se - BCrypt lösenord
-- Database: adminDB
-- Lösenord: customer123
-- ============================================

USE adminDB;

-- Ta bort befintlig customer@p8.se (om den finns)
DELETE FROM user_roles WHERE user_id = (SELECT user_id FROM users WHERE email = 'customer@p8.se');
DELETE FROM users WHERE email = 'customer@p8.se';

-- Skapa ny customer med BCrypt-hash för "customer123"
INSERT INTO users (
    email, 
    password_hash, 
    first_name, 
    last_name, 
    phone,
    is_active, 
    is_email_verified,
    failed_login_attempts,
    created_date
) VALUES (
    'customer@p8.se',
    '$2a$10$NXQVkYFcHDpYE.lYhN5a3OXN3T9gY3zYhqLFDQXYV5MZpE0HDXE2W',
    'Test',
    'Customer',
    '+46701234567',
    1,
    1,
    0,
    NOW()
);

-- Lägg till CUSTOMER role
INSERT INTO user_roles (user_id, role)
SELECT user_id, 'CUSTOMER' 
FROM users 
WHERE email = 'customer@p8.se';

-- Verifiera
SELECT 
    u.user_id,
    u.email,
    u.first_name,
    u.last_name,
    u.phone,
    u.is_active,
    GROUP_CONCAT(ur.role) as roles
FROM users u
LEFT JOIN user_roles ur ON u.user_id = ur.user_id
WHERE u.email = 'customer@p8.se'
GROUP BY u.user_id, u.email, u.first_name, u.last_name, u.phone, u.is_active;

-- ============================================
-- KLART!
-- Testa login:
-- curl -X POST https://p8.rantila.com/api/auth/login \
--   -H "Content-Type: application/json" \
--   -d '{"email":"customer@p8.se","password":"customer123"}'
-- ============================================
