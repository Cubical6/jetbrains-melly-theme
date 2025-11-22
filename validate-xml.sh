#!/bin/bash

# XML Validation Script for Theme Files
# Task 5.2.5: Validate XML syntax
# This script validates all .xml theme files in the themes directory

set -e

THEMES_DIR="src/main/resources/themes"
ERRORS=0
VALIDATED=0

echo "======================================"
echo "XML Theme File Validation"
echo "======================================"
echo ""
echo "Validating XML files in: $THEMES_DIR"
echo ""

# Check if xmllint is installed
if ! command -v xmllint &> /dev/null; then
    echo "WARNING: xmllint is not installed."
    echo "Install with: sudo apt-get install libxml2-utils (Debian/Ubuntu)"
    echo "            or: brew install libxml2 (macOS)"
    echo ""
    echo "Attempting basic well-formedness check without xmllint..."
    echo ""

    # Fallback: basic XML check
    while IFS= read -r file; do
        VALIDATED=$((VALIDATED + 1))
        echo -n "Basic check: $(basename "$file")... "

        # Check if file starts with <?xml and has closing tags
        if grep -q '<?xml' "$file" && grep -q '</scheme>' "$file"; then
            echo "OK (basic check)"
        else
            echo "FAILED (basic check)"
            ERRORS=$((ERRORS + 1))
        fi
    done < <(find "$THEMES_DIR" -name "*.xml" 2>/dev/null)
else
    # Use xmllint for proper validation
    while IFS= read -r file; do
        VALIDATED=$((VALIDATED + 1))
        echo -n "Validating: $(basename "$file")... "

        if xmllint --noout "$file" 2>/dev/null; then
            echo "OK"
        else
            echo "FAILED"
            echo "  Error details:"
            xmllint --noout "$file" 2>&1 | sed 's/^/    /'
            ERRORS=$((ERRORS + 1))
        fi
    done < <(find "$THEMES_DIR" -name "*.xml" 2>/dev/null)
fi

echo ""
echo "======================================"
echo "Validation Summary"
echo "======================================"
echo "Files validated: $VALIDATED"
echo "Errors found: $ERRORS"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo "SUCCESS: All XML files are valid!"
    exit 0
else
    echo "FAILURE: $ERRORS file(s) have XML syntax errors."
    exit 1
fi
