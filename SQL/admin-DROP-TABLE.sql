-- ⚠️ DANGER ZONE ⚠️
-- This file drops ALL tables in adminDB
-- ONLY use for clean slate / development reset
-- NEVER run in production without backup!
-- Created: 2025-11-09

-- Drop tables in reverse order (to respect foreign keys)
-- Currently admin-service only has AdminUser (no foreign keys)

DROP TABLE IF EXISTS admin_users;

-- End of admin-DROP-TABLE.sql
