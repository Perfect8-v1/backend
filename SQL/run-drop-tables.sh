#!/bin/bash

# ============================================
# Perfect8 - DROP Tables Script
# ============================================
# ⚠️⚠️⚠️ DANGER ZONE ⚠️⚠️⚠️
# This script DELETES ALL TABLES
# Only use for clean slate / development reset
# NEVER run in production without backup!
# ============================================
# Created: 2025-11-09
# Usage: bash run-drop-tables.sh
# ============================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# MySQL connection settings
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-your_password_here}"

# SQL files directory
SQL_DIR="."

echo -e "${RED}============================================${NC}"
echo -e "${RED}⚠️⚠️⚠️  DANGER ZONE  ⚠️⚠️⚠️${NC}"
echo -e "${RED}============================================${NC}"
echo -e "${RED}Perfect8 - DROP ALL TABLES${NC}"
echo -e "${RED}============================================${NC}"
echo ""

# Function to run SQL file
run_sql_file() {
    local sql_file=$1
    local database=$2
    local service_name=$3
    
    echo -e "${MAGENTA}[${service_name}]${NC} Dropping tables in ${database}..."
    
    if [ ! -f "$SQL_DIR/$sql_file" ]; then
        echo -e "${RED}ERROR: File not found: $sql_file${NC}"
        return 1
    fi
    
    mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${database}" < "$SQL_DIR/$sql_file" 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${YELLOW}✓ ${service_name} tables dropped${NC}"
        echo ""
        return 0
    else
        echo -e "${RED}✗ ${service_name} failed${NC}"
        echo ""
        return 1
    fi
}

# TRIPLE SAFETY CHECK
echo -e "${RED}⚠️  THIS WILL DELETE ALL DATA IN ALL TABLES!${NC}"
echo -e "${RED}⚠️  ALL CUSTOMER DATA WILL BE LOST!${NC}"
echo -e "${RED}⚠️  ALL ORDERS, PRODUCTS, IMAGES WILL BE DELETED!${NC}"
echo ""
echo "Database Host: ${DB_HOST}:${DB_PORT}"
echo "Database User: ${DB_USER}"
echo ""
echo -e "${YELLOW}Type 'DELETE_ALL_DATA' to confirm (case-sensitive):${NC}"
read -p "> " confirm

if [ "$confirm" != "DELETE_ALL_DATA" ]; then
    echo -e "${GREEN}Aborted - No tables were dropped${NC}"
    exit 0
fi

echo ""
echo -e "${RED}Final confirmation:${NC}"
read -p "Are you ABSOLUTELY SURE? (yes/no): " final_confirm

if [ "$final_confirm" != "yes" ]; then
    echo -e "${GREEN}Aborted - No tables were dropped${NC}"
    exit 0
fi

echo ""
echo -e "${RED}Starting table deletion in 3 seconds...${NC}"
sleep 1
echo -e "${RED}2...${NC}"
sleep 1
echo -e "${RED}1...${NC}"
sleep 1
echo ""

# Counter for success/failure
SUCCESS_COUNT=0
FAIL_COUNT=0

# Run DROP scripts in order
# (must drop in reverse order of foreign keys)

# 5. Shop Service (most foreign keys, drop first)
if run_sql_file "shop-DROP-TABLE.sql" "shopDB" "shop-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# 4. Blog Service
if run_sql_file "blog-DROP-TABLE.sql" "blogDB" "blog-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# 3. Image Service
if run_sql_file "image-DROP-TABLE.sql" "imageDB" "image-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# 2. Email Service
if run_sql_file "email-DROP-TABLE.sql" "emailDB" "email-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# 1. Admin Service
if run_sql_file "admin-DROP-TABLE.sql" "adminDB" "admin-service"; then
    ((SUCCESS_COUNT++))
else
    ((FAIL_COUNT++))
fi

# Summary
echo -e "${RED}============================================${NC}"
echo -e "${RED}SUMMARY${NC}"
echo -e "${RED}============================================${NC}"
echo -e "${YELLOW}Tables dropped: ${SUCCESS_COUNT}/5${NC}"
if [ $FAIL_COUNT -gt 0 ]; then
    echo -e "${RED}Failed: ${FAIL_COUNT}/5${NC}"
fi
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${YELLOW}All tables have been dropped${NC}"
    echo ""
    echo -e "${GREEN}Next steps:${NC}"
    echo "1. Run: bash run-create-tables.sh"
    echo "2. Verify tables exist: mysql -u${DB_USER} -p -e 'SHOW TABLES' adminDB"
    exit 0
else
    echo -e "${RED}Some tables failed to drop${NC}"
    echo "Check error messages above for details"
    exit 1
fi
