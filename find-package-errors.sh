#!/bin/bash

# ============================================
# Find Package Declaration Errors
# Magnum Opus Compliant
# ============================================

echo "=========================================="
echo "Package Declaration Error Finder"
echo "=========================================="
echo ""

cd backend/email-service/src/main/java/com/perfect8/email 2>/dev/null || {
    echo "❌ ERROR: Cannot find email-service directory"
    echo "Current dir: $(pwd)"
    exit 1
}

echo "Checking email-service package declarations..."
echo ""

# Check model/ directory
if [ -d "model" ]; then
    echo "=== FILES IN model/ DIRECTORY ==="
    for file in model/*.java; do
        if [ -f "$file" ]; then
            package_line=$(grep -n "^package" "$file" | head -1)
            echo "File: $file"
            echo "  $package_line"
            
            # Check if package matches directory
            if echo "$package_line" | grep -q "package com.perfect8.email.entity"; then
                echo "  ❌ ERROR: File in model/ but package says 'entity'"
            elif echo "$package_line" | grep -q "package com.perfect8.email.model"; then
                echo "  ✅ OK"
            else
                echo "  ⚠️  UNKNOWN package"
            fi
            echo ""
        fi
    done
fi

# Check entity/ directory
if [ -d "entity" ]; then
    echo "=== FILES IN entity/ DIRECTORY ==="
    for file in entity/*.java; do
        if [ -f "$file" ]; then
            package_line=$(grep -n "^package" "$file" | head -1)
            echo "File: $file"
            echo "  $package_line"
            
            # Check if package matches directory
            if echo "$package_line" | grep -q "package com.perfect8.email.model"; then
                echo "  ❌ ERROR: File in entity/ but package says 'model'"
            elif echo "$package_line" | grep -q "package com.perfect8.email.entity"; then
                echo "  ✅ OK"
            else
                echo "  ⚠️  UNKNOWN package"
            fi
            echo ""
        fi
    done
else
    echo "ℹ️  No entity/ directory (this is fine)"
fi

echo "=========================================="
