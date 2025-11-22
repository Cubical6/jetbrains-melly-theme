#!/bin/bash

# JSON Validation Script for Theme Files
# Task 5.2.4: Validate JSON syntax
# This script validates all .theme.json files in the themes directory

# Note: set -e removed - we use explicit error handling with error counters

THEMES_DIR="src/main/resources/themes"
ERRORS=0
VALIDATED=0

echo "======================================"
echo "JSON Theme File Validation"
echo "======================================"
echo ""
echo "Validating JSON files in: $THEMES_DIR"
echo ""

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "ERROR: jq is not installed. Please install jq to validate JSON files."
    echo "Install with: sudo apt-get install jq (Debian/Ubuntu)"
    echo "            or: brew install jq (macOS)"
    exit 1
fi

# Find and validate all .theme.json files
while IFS= read -r file; do
    VALIDATED=$((VALIDATED + 1))
    echo -n "Validating: $(basename "$file")... "

    if jq empty "$file" 2>/dev/null; then
        echo "OK"
    else
        echo "FAILED"
        echo "  Error details:"
        jq empty "$file" 2>&1 | sed 's/^/    /'
        ERRORS=$((ERRORS + 1))
    fi
done < <(find "$THEMES_DIR" -name "*.theme.json" 2>/dev/null)

echo ""
echo "======================================"
echo "Validation Summary"
echo "======================================"
echo "Files validated: $VALIDATED"
echo "Errors found: $ERRORS"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo "SUCCESS: All JSON files are valid!"
    exit 0
else
    echo "FAILURE: $ERRORS file(s) have JSON syntax errors."
    exit 1
fi
