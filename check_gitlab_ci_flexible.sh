#!/bin/bash

# Updated function to check gitlab-ci.yml with both formats
check_gitlab_ci_flexible() {
    local file=".gitlab-ci.yml"

    if [ ! -f "$file" ]; then
        echo -e "${RED}✗${NC} $file - FILE MISSING"
        ((ISSUES_COUNT++))
        return 1
    fi

    # Check if all required stages are present (either format)
    local all_stages_found=true

    # Check for stages: line
    if ! grep -q "stages:" "$file"; then
        echo -e "${RED}✗${NC} $file - Missing 'stages:' definition"
        ((ISSUES_COUNT++))
        all_stages_found=false
    fi

    # Check for each stage in either format
    for stage in "build" "test" "package" "deploy"; do
        # Check if stage exists either as "- stage" or in inline format
        if ! grep -qE "(- $stage|[[:space:]]$stage[[:space:],\]])" "$file"; then
            echo -e "${RED}✗${NC} $file - Missing stage: $stage"
            ((ISSUES_COUNT++))
            all_stages_found=false
        fi
    done

    if [ "$all_stages_found" = true ]; then
        echo -e "${GREEN}✓${NC} $file"
    fi
}

