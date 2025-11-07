#!/bin/bash

# ============================================
# List Entity Annotation Issues (DRY-RUN)
# Magnum Opus Compliant - Let JPA Do Its Job!
# Windows Git Bash & Ubuntu Compatible
# ============================================

echo "=========================================="
echo "Perfect8 Entity Annotation Checker v1.1"
echo "=========================================="
echo "MODE: DRY-RUN (read-only scan)"
echo ""

# Check if we're in the right directory
if [ ! -d "backend" ]; then
    echo "❌ ERROR: Run this script from project root (where 'backend' folder exists)"
    echo "Current directory: $(pwd)"
    exit 1
fi

cd backend

# Counters
total_files=0
files_with_issues=0

echo "Scanning for problematic @Column annotations..."
echo ""

# Function to check if file has problematic @Column
check_file() {
    local file=$1
    local has_issue=0
    
    # Check for @Column(name = "..._at") - problem med Date-fält
    if grep -E '@Column\(name = ".*_at"\)' "$file" > /dev/null; then
        echo "FOUND in $file:"
        grep -n -E '@Column\(name = ".*_at"\)' "$file" | head -10
        has_issue=1
    fi
    
    ((total_files++))
    
    if [ $has_issue -eq 1 ]; then
        ((files_with_issues++))
        echo ""
    fi
}

# Scan shop-service
if [ -d "shop-service/src/main/java/com/perfect8/shop/entity" ]; then
    echo "=== SHOP SERVICE ==="
    for file in shop-service/src/main/java/com/perfect8/shop/entity/*.java; do
        if [ -f "$file" ]; then
            check_file "$file"
        fi
    done
    echo ""
fi

# Scan blog-service
if [ -d "blog-service/src/main/java/com/perfect8/blog/model" ]; then
    echo "=== BLOG SERVICE ==="
    for file in blog-service/src/main/java/com/perfect8/blog/model/*.java; do
        if [ -f "$file" ]; then
            check_file "$file"
        fi
    done
    echo ""
fi

# Scan admin-service
if [ -d "admin-service/src/main/java/com/perfect8/admin/model" ]; then
    echo "=== ADMIN SERVICE ==="
    for file in admin-service/src/main/java/com/perfect8/admin/model/*.java; do
        if [ -f "$file" ]; then
            check_file "$file"
        fi
    done
    echo ""
fi

# Scan email-service
if [ -d "email-service/src/main/java/com/perfect8/email/model" ]; then
    echo "=== EMAIL SERVICE ==="
    for file in email-service/src/main/java/com/perfect8/email/model/*.java; do
        if [ -f "$file" ]; then
            check_file "$file"
        fi
    done
    echo ""
fi

# Scan image-service
if [ -d "image-service/src/main/java/com/perfect8/image/model" ]; then
    echo "=== IMAGE SERVICE ==="
    for file in image-service/src/main/java/com/perfect8/image/model/*.java; do
        if [ -f "$file" ]; then
            check_file "$file"
        fi
    done
    echo ""
fi

# Summary
echo "=========================================="
echo "SUMMARY"
echo "=========================================="
echo "Total files scanned: $total_files"
echo "Files with issues: $files_with_issues"
echo ""

if [ $files_with_issues -gt 0 ]; then
    echo "RECOMMENDATION:"
    echo "Fix these files manually in IntelliJ by:"
    echo ""
    echo "STEP 1: IntelliJ Find & Replace in Path"
    echo "  Find:    @Column\\(name = \"created_at\"\\)"
    echo "  Replace: // FIXED: JPA handles createdDate automatically"
    echo "  Scope:   Project Files"
    echo ""
    echo "STEP 2: Repeat for other _at fields:"
    echo "  - updated_at    -> updatedDate"
    echo "  - published_at  -> publishedDate"
    echo "  - expires_at    -> expiresAt"
    echo ""
    echo "OR run the fix-entity-annotations-safe.sh script"
else
    echo "✅ No issues found! All entities look good."
fi

echo ""
echo "=========================================="
