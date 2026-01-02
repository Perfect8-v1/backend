#!/bin/bash

# ============================================
# Perfect8 - CREATE Tables Script
# ============================================
# Purpose: Run all CREATE-TABLE.sql files
# Created: 2025-11-09
# Usage: bash run-create-tables.sh
# ============================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# MySQL connection settings
# You can override these with environment variables
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-your_password_here}"

# SQL files directory
SQL_DIR="."

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}Perfect8 - CREATE Tables Script${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Function to run SQL file
run_sql_file() {
    local sql_file=$1
    local database=$2
    local service_name=$3
    
    echo -e "${YELLOW}[${service_name}]${NC} Creating tables in ${database}..."
    
    if [ ! -f "$SQL_DIR/$sql_file" ]; then
        echo -e "${RED}ERROR: File not found: $sql_file${NC}"
        return 1
    fi
    
    mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${database}" < "$SQL_DIR/$sql_file" 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ ${service_name} tables created successfully${NC}"
        echo ""
        return 0
    else
        echo -e "${RED}✗ ${service_name} failed${NC}"
        echo ""
        return 1
    fi
}

# Safety check
echo -e "${YELLOW}⚠️  WARNING: This will create all tables in your databases${NC}"
echo -e "${YELLOW}   Make sure you have run DROP-TABLE scripts if needed${NC}"
echo ""
echo "Database Host: ${DB_HOST}:${DB_PORT}"
echo "Database User: ${DB_USER}"
echo ""
read -p "Continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo -e "${RED}Aborted by user${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Starting table creation...${NC}"
echo ""

# Counter for success/failure
SUCCESS_COUNT=0
FAIL_COUNT=0

# Run CREATE scripts in order
# (order doesn't matter since we use IF NOT EXISTS)

# 1. Admin Service
if run_sql_file "admin-CREATE-TABLE.sql" "adminDB" "admin-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# 2. Blog Service
if run_sql_file "blog-CREATE-TABLE.sql" "blogDB" "blog-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# 3. Email Service
if run_sql_file "email-CREATE-TABLE.sql" "emailDB" "email-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# 4. Image Service
if run_sql_file "image-CREATE-TABLE.sql" "imageDB" "image-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# 5. Shop Service
if run_sql_file "shop-CREATE-TABLE.sql" "shopDB" "shop-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# Summary
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}SUMMARY${NC}"
echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}Successful: ${SUCCESS_COUNT}/5${NC}"
if [ $FAIL_COUNT -gt 0 ]; then
    echo -e "${RED}Failed: ${FAIL_COUNT}/5${NC}"
fi
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}✓ All tables created successfully!${NC}"
    echo ""
    echo -e "${YELLOW}Next steps:${NC}"
    echo "1. Verify tables: mysql -u${DB_USER} -p -e 'SHOW TABLES' adminDB"
    echo "2. Start services with spring.jpa.hibernate.ddl-auto=validate"
    echo "3. Check for validation errors in logs"
    exit 0
else
    echo -e "${RED}✗ Some tables failed to create${NC}"
    echo "Check error messages above for details"
    exit 1
fi
