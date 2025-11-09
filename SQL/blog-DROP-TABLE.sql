-- ⚠️ DANGER ZONE ⚠️
-- This file drops ALL tables in blogDB
-- ONLY use for clean slate / development reset
-- NEVER run in production without backup!
-- Created: 2025-11-09

-- Drop tables in reverse order (to respect foreign keys)
DROP TABLE IF EXISTS image_references;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- End of blog-DROP-TABLE.sql
