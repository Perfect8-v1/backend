-- ============================================
-- Perfect8 User Migration v1.3 Unified (Säker)
-- Database: adminDB
-- ============================================
-- Detta script är idempotent - kan köras flera gånger
-- ============================================

USE adminDB;

-- Visa nuvarande struktur
SELECT 'FÖRE MIGRATION - Kolumner i users:' as info;
DESCRIBE users;

-- ============================================
-- SÄKER MIGRATION: Hantera password_salt
-- ============================================

-- Kolla om password_salt finns
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'adminDB' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'password_salt'
);

-- Om password_salt finns, gör den nullable först (säkerhetsåtgärd)
SET @sql = IF(@col_exists > 0,
    'ALTER TABLE users MODIFY COLUMN password_salt VARCHAR(29) NULL',
    'SELECT "password_salt finns inte, hoppar över MODIFY" as status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Om password_salt finns, ta bort den
SET @sql = IF(@col_exists > 0,
    'ALTER TABLE users DROP COLUMN password_salt',
    'SELECT "password_salt finns inte, hoppar över DROP" as status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- SÄKER MIGRATION: Hantera phone column
-- ============================================

-- Kolla om phone finns
SET @phone_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'adminDB' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'phone'
);

-- Om phone INTE finns, lägg till den
SET @sql = IF(@phone_exists = 0,
    'ALTER TABLE users ADD COLUMN phone VARCHAR(20) NULL AFTER last_name',
    'SELECT "phone finns redan, hoppar över ADD" as status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- Visa struktur efter migration
-- ============================================
SELECT 'EFTER MIGRATION - Kolumner i users:' as info;
DESCRIBE users;

-- ============================================
-- Verifiera testdata finns
-- ============================================
SELECT 'BEFINTLIGA ANVÄNDARE:' as info;
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
-- Test-credentials för login
-- ============================================
SELECT '
TEST CREDENTIALS:
-----------------
Admin:    cmb@p8.se / magnus123
Customer: customer@p8.se / customer123
' as credentials;

-- ============================================
-- KLART!
-- ============================================
-- Nästa steg:
-- 1. Ersätt User.java (utan passwordSalt)
-- 2. Rebuild: mvn clean package -DskipTests -pl :admin-service
-- 3. Deploy: bash copy-jars.sh && docker compose up -d --build admin-service
-- ============================================
