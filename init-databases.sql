-- Init script for microservices - Creates all databases
-- This runs automatically when MariaDB container starts for the first time

CREATE DATABASE IF NOT EXISTS adminDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS blogDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS emailDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS imageDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS shopDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Note: User privileges are automatically granted by MariaDB environment variables
-- The user specified in MARIADB_USER will have access to all databases