#!/bin/bash
# FASE 5.1 - Theme Regeneration Validation Script
# Automated verification for theme generation process
# Run this after: ./gradlew createThemes

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0
WARNINGS=0

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}FASE 5.1 - Theme Regeneration Validator${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to print test result
pass() {
    echo -e "${GREEN}✓${NC} $1"
    ((PASSED++))
}

fail() {
    echo -e "${RED}✗${NC} $1"
    ((FAILED++))
}

warn() {
    echo -e "${YELLOW}⚠${NC} $1"
    ((WARNINGS++))
}

info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Test 1: Check theme directory exists
echo "1. Checking theme directory..."
if [ -d "src/main/resources/themes" ]; then
    pass "Theme directory exists"
else
    fail "Theme directory not found"
    exit 1
fi
echo ""

# Test 2: Count theme files
echo "2. Counting theme files..."
JSON_COUNT=$(find src/main/resources/themes -name "*.theme.json" | wc -l)
XML_COUNT=$(find src/main/resources/themes -name "*.xml" | wc -l)

echo "   Found: $JSON_COUNT .theme.json files"
echo "   Found: $XML_COUNT .xml files"

if [ "$JSON_COUNT" -eq "$XML_COUNT" ]; then
    pass "JSON and XML counts match ($JSON_COUNT themes)"
else
    fail "Mismatch: $JSON_COUNT JSON vs $XML_COUNT XML files"
fi

if [ "$JSON_COUNT" -ge 59 ]; then
    pass "Expected number of themes generated (≥59)"
else
    warn "Expected 59+ themes, found $JSON_COUNT"
fi
echo ""

# Test 3: Validate JSON syntax
echo "3. Validating JSON syntax..."
JSON_ERRORS=0
while IFS= read -r file; do
    if ! jq empty "$file" 2>/dev/null; then
        fail "Invalid JSON: $file"
        ((JSON_ERRORS++))
    fi
done < <(find src/main/resources/themes -name "*.theme.json")

if [ "$JSON_ERRORS" -eq 0 ]; then
    pass "All JSON files are valid ($JSON_COUNT files checked)"
else
    fail "Found $JSON_ERRORS invalid JSON file(s)"
fi
echo ""

# Test 4: Validate XML syntax (if xmllint available)
echo "4. Validating XML syntax..."
if command -v xmllint &> /dev/null; then
    XML_ERRORS=0
    while IFS= read -r file; do
        if ! xmllint --noout "$file" 2>/dev/null; then
            fail "Invalid XML: $file"
            ((XML_ERRORS++))
        fi
    done < <(find src/main/resources/themes -name "*.xml")

    if [ "$XML_ERRORS" -eq 0 ]; then
        pass "All XML files are valid ($XML_COUNT files checked)"
    else
        fail "Found $XML_ERRORS invalid XML file(s)"
    fi
else
    warn "xmllint not installed - skipping XML validation"
    info "Install with: sudo apt-get install libxml2-utils (Ubuntu) or brew install libxml2 (macOS)"
fi
echo ""

# Test 5: Check file sizes
echo "5. Checking file sizes..."
SMALL_JSON=0
SMALL_XML=0
LARGE_JSON=0
LARGE_XML=0

while IFS= read -r file; do
    size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null || echo 0)
    if [ "$size" -lt 5000 ]; then
        warn "Small JSON file: $(basename "$file") (${size} bytes)"
        ((SMALL_JSON++))
    elif [ "$size" -gt 50000 ]; then
        warn "Large JSON file: $(basename "$file") (${size} bytes)"
        ((LARGE_JSON++))
    fi
done < <(find src/main/resources/themes -name "*.theme.json")

while IFS= read -r file; do
    size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null || echo 0)
    if [ "$size" -lt 30000 ]; then
        warn "Small XML file: $(basename "$file") (${size} bytes)"
        ((SMALL_XML++))
    elif [ "$size" -gt 200000 ]; then
        warn "Large XML file: $(basename "$file") (${size} bytes)"
        ((LARGE_XML++))
    fi
done < <(find src/main/resources/themes -name "*.xml")

if [ "$SMALL_JSON" -eq 0 ] && [ "$LARGE_JSON" -eq 0 ] && [ "$SMALL_XML" -eq 0 ] && [ "$LARGE_XML" -eq 0 ]; then
    pass "All file sizes are reasonable"
else
    info "JSON files outside normal range: $SMALL_JSON too small, $LARGE_JSON too large"
    info "XML files outside normal range: $SMALL_XML too small, $LARGE_XML too large"
fi
echo ""

# Test 6: Verify popular themes exist
echo "6. Checking for popular themes..."
POPULAR_THEMES=("dracula" "solarized-dark" "nord" "gruvbox-dark" "tokyo-night")
FOUND_THEMES=0

for theme in "${POPULAR_THEMES[@]}"; do
    if ls src/main/resources/themes/wt-${theme}-*.theme.json 1> /dev/null 2>&1; then
        pass "Found: $theme"
        ((FOUND_THEMES++))
    else
        fail "Missing: $theme"
    fi
done

if [ "$FOUND_THEMES" -eq ${#POPULAR_THEMES[@]} ]; then
    pass "All popular themes found (${FOUND_THEMES}/${#POPULAR_THEMES[@]})"
fi
echo ""

# Test 7: Check for duplicate theme IDs
echo "7. Checking for duplicate theme IDs..."
DUPLICATES=$(find src/main/resources/themes -name "*.theme.json" -exec basename {} \; | sed 's/.theme.json$//' | sort | uniq -d | wc -l)

if [ "$DUPLICATES" -eq 0 ]; then
    pass "No duplicate theme IDs found"
else
    fail "Found $DUPLICATES duplicate theme ID(s)"
    info "Duplicate IDs:"
    find src/main/resources/themes -name "*.theme.json" -exec basename {} \; | sed 's/.theme.json$//' | sort | uniq -d
fi
echo ""

# Test 8: Verify theme-xml pairs exist
echo "8. Verifying theme-XML pairs..."
UNPAIRED=0

while IFS= read -r jsonfile; do
    basename=$(basename "$jsonfile" .theme.json)
    xmlfile="src/main/resources/themes/${basename}.xml"
    if [ ! -f "$xmlfile" ]; then
        fail "Missing XML for: $basename"
        ((UNPAIRED++))
    fi
done < <(find src/main/resources/themes -name "*.theme.json")

if [ "$UNPAIRED" -eq 0 ]; then
    pass "All themes have matching JSON+XML pairs"
else
    fail "Found $UNPAIRED unpaired theme file(s)"
fi
echo ""

# Test 9: Check plugin.xml updates (if exists)
echo "9. Checking plugin.xml..."
if [ -f "src/main/resources/META-INF/plugin.xml" ]; then
    THEME_PROVIDERS=$(grep -c "<themeProvider" src/main/resources/META-INF/plugin.xml || echo 0)
    BUNDLED_SCHEMES=$(grep -c "<bundledColorScheme" src/main/resources/META-INF/plugin.xml || echo 0)

    info "Found $THEME_PROVIDERS themeProvider entries"
    info "Found $BUNDLED_SCHEMES bundledColorScheme entries"

    if [ "$THEME_PROVIDERS" -eq "$JSON_COUNT" ] && [ "$BUNDLED_SCHEMES" -eq "$JSON_COUNT" ]; then
        pass "plugin.xml has correct number of entries"
    else
        warn "plugin.xml entries don't match theme count"
        info "Expected: $JSON_COUNT themes, found: $THEME_PROVIDERS providers, $BUNDLED_SCHEMES schemes"
    fi
else
    warn "plugin.xml not found"
fi
echo ""

# Test 10: Sample theme content validation
echo "10. Validating sample theme content..."
SAMPLE_THEME=$(find src/main/resources/themes -name "wt-dracula-*.theme.json" | head -1)

if [ -n "$SAMPLE_THEME" ]; then
    # Check for required fields
    HAS_NAME=$(jq -e '.name' "$SAMPLE_THEME" > /dev/null 2>&1 && echo "yes" || echo "no")
    HAS_DARK=$(jq -e '.dark' "$SAMPLE_THEME" > /dev/null 2>&1 && echo "yes" || echo "no")
    HAS_AUTHOR=$(jq -e '.author' "$SAMPLE_THEME" > /dev/null 2>&1 && echo "yes" || echo "no")
    HAS_UI=$(jq -e '.ui' "$SAMPLE_THEME" > /dev/null 2>&1 && echo "yes" || echo "no")

    if [ "$HAS_NAME" == "yes" ] && [ "$HAS_DARK" == "yes" ] && [ "$HAS_AUTHOR" == "yes" ] && [ "$HAS_UI" == "yes" ]; then
        pass "Sample theme has required fields (name, dark, author, ui)"
    else
        fail "Sample theme missing required fields"
        info "Has name: $HAS_NAME, dark: $HAS_DARK, author: $HAS_AUTHOR, ui: $HAS_UI"
    fi
else
    warn "Could not find Dracula theme for validation"
fi
echo ""

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Validation Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Passed:${NC}   $PASSED"
echo -e "${RED}Failed:${NC}   $FAILED"
echo -e "${YELLOW}Warnings:${NC} $WARNINGS"
echo ""

if [ "$FAILED" -eq 0 ]; then
    echo -e "${GREEN}✓ All critical validations passed!${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Review warnings (if any)"
    echo "  2. Spot-check popular themes manually"
    echo "  3. Run: ./gradlew buildPlugin"
    echo "  4. Proceed to Task 5.2 (Git Diff Analysis)"
    echo ""
    exit 0
else
    echo -e "${RED}✗ Validation failed with $FAILED error(s)${NC}"
    echo ""
    echo "Please review the errors above and:"
    echo "  1. Check Gradle output for generation errors"
    echo "  2. Re-run: ./gradlew createThemes"
    echo "  3. Run this script again"
    echo ""
    exit 1
fi
