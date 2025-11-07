#!/bin/bash

# ============================================
# Fix Entity Annotations - Safe Version
# Magnum Opus Compliant - Let JPA Do Its Job!
# Windows Git Bash & Ubuntu Compatible
# Uses AWK instead of sed for safety
# ============================================

echo "=========================================="
echo "Perfect8 Entity Annotation Fixer v1.1"
echo "=========================================="
echo "MODE: SAFE FIX (AWK-based)"
echo ""

# Check if we're in the right directory
if [ ! -d "backend" ]; then
    echo "❌ ERROR: Run this script from project root (where 'backend' folder exists)"
    echo "Current directory: $(pwd)"
    exit 1
fi

cd backend

# Check for required tools
if ! command -v awk &> /dev/null; then
    echo "❌ ERROR: awk not found. This script requires awk."
    exit 1
fi

# Counters
total_files=0
files_fixed=0

echo "Scanning and fixing problematic @Column annotations..."
echo ""

# Function to fix a single file
fix_file() {
    local file=$1
    local temp_file="${file}.tmp"
    local fixed=0
    
    # Check if file has issues
    if grep -E '@Column\(name = ".*_at"\)' "$file" > /dev/null; then
        echo "Fixing: $file"
        
        # Use AWK to fix the file (safer than sed in Git Bash)
        awk '{
            # Remove @Column(name = "created_at")
            if ($0 ~ /@Column\(name = "created_at"\)/) {
                print "    // FIXED: JPA handles createdDate automatically"
                next
            }
            # Remove @Column(name = "updated_at")
            if ($0 ~ /@Column\(name = "updated_at"\)/) {
                print "    // FIXED: JPA handles updatedDate automatically"
                next
            }
            # Remove @Column(name = "published_at")
            if ($0 ~ /@Column\(name = "published_at"\)/) {
                print "    // FIXED: JPA handles publishedDate automatically"
                next
            }
            # Remove @Column(name = "expires_at")
            if ($0 ~ /@Column\(name = "expires_at"\)/) {
                print "    // FIXED: JPA handles expiresAt automatically"
                next
            }
            # Keep all other lines unchanged
            print $0
        }' "$file" > "$temp_file"
        
        # Replace original file with fixed version
        if [ -f "$temp_file" ]; then
            mv "$temp_file" "$file"
            echo "  ✅ Fixed!"
            ((files_fixed++))
        else
            echo "  ❌ Failed to create temp file"
        fi
    fi
    
    ((total_files++))
}

# Scan and fix shop-service
if [ -d "shop-service/src/main/java/com/perfect8/shop/entity" ]; then
    echo "=== SHOP SERVICE ==="
    for file in shop-service/src/main/java/com/perfect8/shop/entity/*.java; do
        if [ -f "$file" ]; then
            fix_file "$file"
        fi
    done
    echo ""
fi

# Scan and fix blog-service
if [ -d "blog-service/src/main/java/com/perfect8/blog/model" ]; then
    echo "=== BLOG SERVICE ==="
    for file in blog-service/src/main/java/com/perfect8/blog/model/*.java; do
        if [ -f "$file" ]; then
            fix_file "$file"
        fi
    done
    echo ""
fi

# Scan and fix admin-service
if [ -d "admin-service/src/main/java/com/perfect8/admin/model" ]; then
    echo "=== ADMIN SERVICE ==="
    for file in admin-service/src/main/java/com/perfect8/admin/model/*.java; do
        if [ -f "$file" ]; then
            fix_file "$file"
        fi
    done
    echo ""
fi

# Scan and fix email-service
if [ -d "email-service/src/main/java/com/perfect8/email/model" ]; then
    echo "=== EMAIL SERVICE ==="
    for file in email-service/src/main/java/com/perfect8/email/model/*.java; do
        if [ -f "$file" ]; then
            fix_file "$file"
        fi
    done
    echo ""
fi

# Scan and fix image-service
if [ -d "image-service/src/main/java/com/perfect8/image/model" ]; then
    echo "=== IMAGE SERVICE ==="
    for file in image-service/src/main/java/com/perfect8/image/model/*.java; do
        if [ -f "$file" ]; then
            fix_file "$file"
        fi
    done
    echo ""
fi

# Summary
echo "=========================================="
echo "SUMMARY"
echo "=========================================="
echo "Total files scanned: $total_files"
echo "Files fixed: $files_fixed"
echo ""

if [ $files_fixed -gt 0 ]; then
    echo "✅ SUCCESS! Fixed $files_fixed file(s)"
    echo ""
    echo "NEXT STEPS:"
    echo "1. Review changes in IntelliJ"
    echo "2. Run Maven compile: mvn clean compile -q"
    echo "3. Commit to Git: git add . && git commit -m 'fix: Remove unnecessary @Column annotations'"
    echo "4. Push to GitHub: git push"
else
    echo "ℹ️  No files needed fixing."
fi

echo ""
echo "=========================================="
