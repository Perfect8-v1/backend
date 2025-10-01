-- Create all databases for microservices
CREATE DATABASE IF NOT EXISTS perfect8_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS perfect8_blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS perfect8_email CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS perfect8_images CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS perfect8_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant permissions
GRANT ALL PRIVILEGES ON perfect8_admin.* TO 'perfect8user'@'%';
GRANT ALL PRIVILEGES ON perfect8_blog.* TO 'perfect8user'@'%';
GRANT ALL PRIVILEGES ON perfect8_email.* TO 'perfect8user'@'%';
GRANT ALL PRIVILEGES ON perfect8_images.* TO 'perfect8user'@'%';
GRANT ALL PRIVILEGES ON perfect8_shop.* TO 'perfect8user'@'%';

FLUSH PRIVILEGES;