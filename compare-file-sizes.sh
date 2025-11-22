#!/bin/bash

# File Size Comparison Script
# Task 5.2.3: Compare file sizes before and after regeneration
# This script compares current theme file sizes with their git HEAD versions

# Note: set -e removed - we use explicit error handling with || fallbacks

THEMES_DIR="src/main/resources/themes"

echo "======================================"
echo "File Size Comparison Report"
echo "======================================"
echo ""
echo "Comparing current files with git HEAD"
echo ""

# Check if bc is installed for calculations
if ! command -v bc &> /dev/null; then
    echo "WARNING: bc is not installed. Percentage calculations will be skipped."
    echo "Install with: sudo apt-get install bc (Debian/Ubuntu)"
    echo ""
    USE_BC=false
else
    USE_BC=true
fi

echo "JSON Files (.theme.json):"
echo "----------------------------------------"

# Process JSON files
find "$THEMES_DIR" -name "*.theme.json" 2>/dev/null | sort | while IFS= read -r file; do
    FILENAME=$(basename "$file")

    # Get old size from git (if exists)
    OLD_SIZE=$(git show HEAD:"$file" 2>/dev/null | wc -c || echo "0")
    NEW_SIZE=$(wc -c < "$file")

    if [ "$OLD_SIZE" -gt 0 ]; then
        DIFF=$((NEW_SIZE - OLD_SIZE))

        echo "File: $FILENAME"
        printf "  Before: %d bytes\n" "$OLD_SIZE"
        printf "  After:  %d bytes\n" "$NEW_SIZE"
        printf "  Change: %d bytes" "$DIFF"

        if [ "$USE_BC" = true ]; then
            PERCENT=$(echo "scale=1; $DIFF * 100 / $OLD_SIZE" | bc)
            echo " (${PERCENT}%)"
        else
            echo ""
        fi

        # Flag unusual changes
        if [ $DIFF -lt 0 ]; then
            echo "  ⚠️  WARNING: File decreased in size!"
        elif [ "$USE_BC" = true ]; then
            # Check if increase is more than 50%
            PERCENT_INT=$(echo "scale=0; $DIFF * 100 / $OLD_SIZE" | bc)
            if [ "$PERCENT_INT" -gt 50 ]; then
                echo "  ⚠️  WARNING: File increased by more than 50%!"
            fi
        fi
        echo ""
    else
        echo "File: $FILENAME"
        printf "  Current: %d bytes\n" "$NEW_SIZE"
        echo "  Status: New file (no git history)"
        echo ""
    fi
done

echo ""
echo "XML Files (.xml):"
echo "----------------------------------------"

# Process XML files
find "$THEMES_DIR" -name "*.xml" 2>/dev/null | sort | while IFS= read -r file; do
    FILENAME=$(basename "$file")

    # Get old size from git (if exists)
    OLD_SIZE=$(git show HEAD:"$file" 2>/dev/null | wc -c || echo "0")
    NEW_SIZE=$(wc -c < "$file")

    if [ "$OLD_SIZE" -gt 0 ]; then
        DIFF=$((NEW_SIZE - OLD_SIZE))

        echo "File: $FILENAME"
        printf "  Before: %d bytes\n" "$OLD_SIZE"
        printf "  After:  %d bytes\n" "$NEW_SIZE"
        printf "  Change: %d bytes" "$DIFF"

        if [ "$USE_BC" = true ]; then
            PERCENT=$(echo "scale=1; $DIFF * 100 / $OLD_SIZE" | bc)
            echo " (${PERCENT}%)"
        else
            echo ""
        fi

        # Flag unusual changes
        if [ $DIFF -lt 0 ]; then
            echo "  ⚠️  WARNING: File decreased in size!"
        elif [ "$USE_BC" = true ]; then
            # Check if increase is more than 50%
            PERCENT_INT=$(echo "scale=0; $DIFF * 100 / $OLD_SIZE" | bc)
            if [ "$PERCENT_INT" -gt 50 ]; then
                echo "  ⚠️  WARNING: File increased by more than 50%!"
            fi
        fi
        echo ""
    else
        echo "File: $FILENAME"
        printf "  Current: %d bytes\n" "$NEW_SIZE"
        echo "  Status: New file (no git history)"
        echo ""
    fi
done

echo "======================================"
echo "Summary"
echo "======================================"
echo ""
echo "Expected changes after theme regeneration:"
echo "  - JSON files: 10-30% increase"
echo "  - XML files: 5-20% increase"
echo ""
echo "Red flags:"
echo "  - Files smaller than before (data loss)"
echo "  - Files 50%+ larger (possible duplication)"
echo "  - Files unchanged (regeneration may not have run)"
