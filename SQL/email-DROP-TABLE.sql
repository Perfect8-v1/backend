-- ⚠️ DANGER ZONE ⚠️
-- This file drops ALL tables in emailDB
-- ONLY use for clean slate / development reset
-- NEVER run in production without backup!
-- Created: 2025-11-09

-- Drop tables (no foreign keys)
DROP TABLE IF EXISTS email_templates;

-- Note: EmailMessage is a DTO/Model, not an @Entity
-- So it does NOT have a database table

-- End of email-DROP-TABLE.sql
