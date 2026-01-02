INSERT INTO user_roles (user_id, role)
SELECT user_id, 'ADMIN' 
FROM users 
WHERE email = 'cmagnusb@yahoo.se';