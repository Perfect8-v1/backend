-- ============================================
-- Perfect8 User Migration v1.3 Unified
-- Database: adminDB
-- ============================================
-- Detta script:
-- 1. Gör password_salt nullable (säkerhetsåtgärd)
-- 2. Tar bort password_salt column
-- 3. Lägger till phone column
-- 4. Skapar test-användare med CUSTOMER role
-- ============================================

USE adminDB;

-- Steg 1: Säkerhetsåtgärd - Gör password_salt nullable först
-- Detta förhindrar "Data truncated" fel
ALTER TABLE users 
MODIFY COLUMN password_salt VARCHAR(29) NULL;

-- Steg 2: Ta bort password_salt helt (v1.2 legacy)
ALTER TABLE users 
DROP COLUMN password_salt;

-- Steg 3: Lägg till phone column för customers
ALTER TABLE users 
ADD COLUMN phone VARCHAR(20) NULL AFTER last_name;

-- Steg 4: Verifiera struktur
DESCRIBE users;

-- ============================================
-- TESTDATA: Skapa en CUSTOMER användare
-- ============================================

-- Password: "customer123" (BCrypt hash)
-- Salt: $2a$10$abcdefghijklmnopqrstuvwxyz...
-- OBS: Detta är ett EXEMPEL-hash, använd riktig BCrypt i produktion
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
    '$2a$10$NXQVkYFcHDpYE.lYhN5a3OXN3T9gY3zYhqLFDQXYV5MZpE0HDXE2W', -- customer123
    'Test',
    'Customer',
    '+46701234567',
    1,
    1,
    0,
    NOW()
) ON DUPLICATE KEY UPDATE email = email;

-- Lägg till CUSTOMER role
INSERT INTO user_roles (user_id, role)
SELECT user_id, 'CUSTOMER'
FROM users
WHERE email = 'customer@p8.se'
ON DUPLICATE KEY UPDATE role = role;

-- ============================================
-- Uppdatera befintlig cmb@p8.se med phone
-- ============================================
UPDATE users 
SET phone = '+46701111111'
WHERE email = 'cmb@p8.se';

-- ============================================
-- Verifiera migration
-- ============================================
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
GROUP BY u.user_id, u.email, u.first_name, u.last_name, u.phone, u.is_active
ORDER BY u.user_id;

-- ============================================
-- KLART!
-- ============================================
-- Nästa steg:
-- 1. Ersätt User.java på servern (utan passwordSalt)
-- 2. Rebuild admin-service: mvn clean package
-- 3. Starta om: docker compose up -d --build admin-service
-- ============================================
