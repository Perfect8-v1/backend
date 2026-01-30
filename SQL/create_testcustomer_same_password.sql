-- ============================================
-- Skapa testcustomer med SAMMA lösenord som cmb@p8.se
-- Password: magnus123 (samma hash som cmb@p8.se)
-- ============================================

USE adminDB;

-- Ta bort gamla testcustomer@p8.se (om finns)
DELETE FROM user_roles WHERE user_id = (SELECT user_id FROM users WHERE email = 'testcustomer@p8.se');
DELETE FROM users WHERE email = 'testcustomer@p8.se';

-- Skapa ny customer med SAMMA password_hash som cmb@p8.se
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
)
SELECT 
    'testcustomer@p8.se',
    password_hash,  -- Kopierar hash från cmb@p8.se
    'Test',
    'Customer',
    '+46701234567',
    1,
    1,
    0,
    NOW()
FROM users 
WHERE email = 'cmb@p8.se';

-- Lägg till CUSTOMER role
INSERT INTO user_roles (user_id, role)
SELECT user_id, 'CUSTOMER' 
FROM users 
WHERE email = 'testcustomer@p8.se';

-- Verifiera
SELECT 
    u.user_id,
    u.email,
    u.first_name,
    u.last_name,
    u.is_active,
    GROUP_CONCAT(ur.role) as roles
FROM users u
LEFT JOIN user_roles ur ON u.user_id = ur.user_id
WHERE u.email IN ('cmb@p8.se', 'testcustomer@p8.se')
GROUP BY u.user_id, u.email, u.first_name, u.last_name, u.is_active
ORDER BY u.user_id;

-- ============================================
-- KLART!
-- Testa login (SAMMA lösenord som cmb@p8.se):
-- curl -X POST https://p8.rantila.com/api/auth/login \
--   -H "Content-Type: application/json" \
--   -d '{"email":"testcustomer@p8.se","password":"magnus123"}'
-- ============================================
