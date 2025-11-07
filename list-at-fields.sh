#!/bin/bash

# ============================================
# List All *At Fields That Need Renaming
# Magnum Opus Compliant - Use *Date suffix!
# ============================================

echo "=========================================="
echo "Perfect8 Field Name Analyzer"
echo "=========================================="
echo "Finding all *At fields that should be *Date"
echo ""

cd backend 2>/dev/null || {
    echo "❌ Run from project root"
    exit 1
}

total_issues=0

# Function to scan a file
scan_file() {
    local file=$1
    local service=$2
    
    # Find all LocalDateTime fields ending with At
    if grep -q "private LocalDateTime.*At;" "$file"; then
        echo "=== $service: $(basename $file) ==="
        grep -n "private LocalDateTime.*At;" "$file" | while read line; do
            echo "  Line: $line"
            ((total_issues++))
        done
        echo ""
    fi
}

# Scan all services
echo "🔍 SCANNING ENTITIES/MODELS..."
echo ""

# Shop service entities
if [ -d "shop-service/src/main/java/com/perfect8/shop/entity" ]; then
    echo "--- SHOP SERVICE ---"
    for file in shop-service/src/main/java/com/perfect8/shop/entity/*.java; do
        [ -f "$file" ] && scan_file "$file" "shop-service"
    done
fi

# Admin service model
if [ -d "admin-service/src/main/java/com/perfect8/admin/model" ]; then
    echo "--- ADMIN SERVICE ---"
    for file in admin-service/src/main/java/com/perfect8/admin/model/*.java; do
        [ -f "$file" ] && scan_file "$file" "admin-service"
    done
fi

# Blog service model
if [ -d "blog-service/src/main/java/com/perfect8/blog/model" ]; then
    echo "--- BLOG SERVICE ---"
    for file in blog-service/src/main/java/com/perfect8/blog/model/*.java; do
        [ -f "$file" ] && scan_file "$file" "blog-service"
    done
fi

# Email service model
if [ -d "email-service/src/main/java/com/perfect8/email/model" ]; then
    echo "--- EMAIL SERVICE ---"
    for file in email-service/src/main/java/com/perfect8/email/model/*.java; do
        [ -f "$file" ] && scan_file "$file" "email-service"
    done
fi

# Image service model
if [ -d "image-service/src/main/java/com/perfect8/image/model" ]; then
    echo "--- IMAGE SERVICE ---"
    for file in image-service/src/main/java/com/perfect8/image/model/*.java; do
        [ -f "$file" ] && scan_file "$file" "image-service"
    done
fi

echo "=========================================="
echo "SUMMARY"
echo "=========================================="
echo ""
echo "Common fields that need renaming:"
echo "  createdAt       → createdDate"
echo "  updatedAt       → updatedDate"
echo "  publishedAt     → publishedDate"
echo "  expiresAt       → expiresAt (keep - not a date field)"
echo "  lastLoginAt     → lastLoginDate"
echo "  stockCheckedAt  → stockCheckedDate"
echo "  shippedAt       → shippedDate"
echo "  deliveredAt     → deliveredDate"
echo "  cancelledAt     → cancelledDate"
echo "  processedAt     → processedDate"
echo ""
echo "=========================================="
echo "REFACTORING GUIDE (IntelliJ)"
echo "=========================================="
echo ""
echo "For EACH field above:"
echo "  1. Open the entity file in IntelliJ"
echo "  2. Right-click on field name (e.g., 'createdAt')"
echo "  3. Refactor → Rename (or Shift+F6)"
echo "  4. Enter new name (e.g., 'createdDate')"
echo "  5. Check 'Search in comments and strings'"
echo "  6. Click 'Refactor'"
echo "  7. IntelliJ updates ALL references automatically!"
echo ""
echo "After ALL fields renamed:"
echo "  mvn clean compile -q"
echo ""
