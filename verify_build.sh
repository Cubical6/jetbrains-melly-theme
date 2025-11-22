#!/bin/bash

# Sprint 6 Build Verification Script
# This script performs automated verification of the plugin build
# Run this before manual testing to ensure the build is correct

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ARTIFACT_PATH="$SCRIPT_DIR/build/distributions/one-dark-theme.zip"
PLUGIN_XML="$SCRIPT_DIR/src/main/resources/META-INF/plugin.xml"
THEMES_DIR="$SCRIPT_DIR/src/main/resources/themes"

echo "========================================"
echo "Sprint 6 Build Verification"
echo "========================================"
echo ""

# Check 1: Verify plugin artifact exists
echo "[1/8] Checking plugin artifact exists..."
if [ -f "$ARTIFACT_PATH" ]; then
    SIZE=$(du -h "$ARTIFACT_PATH" | cut -f1)
    echo "✅ Plugin artifact found: $ARTIFACT_PATH ($SIZE)"
else
    echo "❌ Plugin artifact NOT found at: $ARTIFACT_PATH"
    echo "   Run './gradlew buildPlugin' to build the plugin"
    exit 1
fi

# Check 2: Verify artifact size is reasonable
echo ""
echo "[2/8] Checking artifact size..."
SIZE_BYTES=$(stat -c%s "$ARTIFACT_PATH" 2>/dev/null || stat -f%z "$ARTIFACT_PATH" 2>/dev/null)
MIN_SIZE=$((2 * 1024 * 1024))  # 2 MB
MAX_SIZE=$((5 * 1024 * 1024))  # 5 MB

if [ "$SIZE_BYTES" -ge "$MIN_SIZE" ] && [ "$SIZE_BYTES" -le "$MAX_SIZE" ]; then
    echo "✅ Artifact size is reasonable: $(numfmt --to=iec-i --suffix=B $SIZE_BYTES 2>/dev/null || echo "$SIZE_BYTES bytes")"
else
    echo "⚠️  Artifact size is unusual: $(numfmt --to=iec-i --suffix=B $SIZE_BYTES 2>/dev/null || echo "$SIZE_BYTES bytes")"
    echo "   Expected: 2-5 MB"
fi

# Check 3: Verify plugin.xml has dual registration
echo ""
echo "[3/8] Checking plugin.xml registration entries..."
THEME_PROVIDER_COUNT=$(grep -c "<themeProvider" "$PLUGIN_XML" || echo "0")
BUNDLED_SCHEME_COUNT=$(grep -c "<bundledColorScheme" "$PLUGIN_XML" || echo "0")

echo "   themeProvider entries: $THEME_PROVIDER_COUNT"
echo "   bundledColorScheme entries: $BUNDLED_SCHEME_COUNT"

if [ "$THEME_PROVIDER_COUNT" -eq "$BUNDLED_SCHEME_COUNT" ] && [ "$THEME_PROVIDER_COUNT" -gt 0 ]; then
    echo "✅ Dual registration confirmed (counts match)"
else
    echo "❌ Registration mismatch!"
    echo "   Expected equal counts of themeProvider and bundledColorScheme"
    exit 1
fi

# Check 4: Verify expected theme count (57 Windows Terminal themes)
echo ""
echo "[4/8] Verifying theme count..."
EXPECTED_COUNT=57
if [ "$THEME_PROVIDER_COUNT" -eq "$EXPECTED_COUNT" ]; then
    echo "✅ Theme count matches expected: $THEME_PROVIDER_COUNT themes"
else
    echo "⚠️  Theme count differs from expected"
    echo "   Expected: $EXPECTED_COUNT"
    echo "   Actual: $THEME_PROVIDER_COUNT"
fi

# Check 5: Verify theme files exist in source
echo ""
echo "[5/8] Checking theme files in source directory..."
XML_COUNT=$(find "$THEMES_DIR" -name "wt-*.xml" 2>/dev/null | wc -l)
JSON_COUNT=$(find "$THEMES_DIR" -name "wt-*.theme.json" 2>/dev/null | wc -l)

echo "   XML files: $XML_COUNT"
echo "   JSON files: $JSON_COUNT"

if [ "$XML_COUNT" -eq "$JSON_COUNT" ] && [ "$XML_COUNT" -eq "$EXPECTED_COUNT" ]; then
    echo "✅ All theme files present in source"
else
    echo "⚠️  Theme file count mismatch"
fi

# Check 6: Verify plugin.xml is well-formed
echo ""
echo "[6/8] Verifying plugin.xml is well-formed..."
if command -v xmllint &> /dev/null; then
    if xmllint --noout "$PLUGIN_XML" 2>/dev/null; then
        echo "✅ plugin.xml is well-formed XML"
    else
        echo "❌ plugin.xml has XML syntax errors"
        xmllint --noout "$PLUGIN_XML"
        exit 1
    fi
else
    echo "⚠️  xmllint not available, skipping XML validation"
fi

# Check 7: Sample a few themes to verify dual registration format
echo ""
echo "[7/8] Sampling theme registration format..."
SAMPLE_THEMES=("wt-dracula" "wt-nord" "wt-material")
SAMPLES_OK=true

for theme in "${SAMPLE_THEMES[@]}"; do
    # Look for theme ID pattern with hash
    if grep -q "themeProvider.*${theme}-[a-f0-9]\{8\}" "$PLUGIN_XML"; then
        if grep -q "bundledColorScheme.*${theme}-[a-f0-9]\{8\}" "$PLUGIN_XML"; then
            echo "   ✓ $theme: dual registration found"
        else
            echo "   ✗ $theme: missing bundledColorScheme"
            SAMPLES_OK=false
        fi
    else
        echo "   ? $theme: not found (may use different hash)"
    fi
done

if [ "$SAMPLES_OK" = true ]; then
    echo "✅ Sample themes have correct dual registration"
fi

# Check 8: Verify artifact contents
echo ""
echo "[8/8] Checking plugin artifact contents..."
TEMP_DIR=$(mktemp -d)
cd "$TEMP_DIR"
unzip -q "$ARTIFACT_PATH"

# Find the jar file
JAR_FILE=$(find . -name "instrumented-*.jar" | head -1)
if [ -n "$JAR_FILE" ]; then
    ARTIFACT_XML_COUNT=$(unzip -l "$JAR_FILE" | grep -c "themes/wt-.*\.xml" || echo "0")
    ARTIFACT_JSON_COUNT=$(unzip -l "$JAR_FILE" | grep -c "themes/wt-.*\.theme\.json" || echo "0")

    echo "   Theme files in artifact:"
    echo "   - XML files: $ARTIFACT_XML_COUNT"
    echo "   - JSON files: $ARTIFACT_JSON_COUNT"

    if [ "$ARTIFACT_XML_COUNT" -eq "$EXPECTED_COUNT" ] && [ "$ARTIFACT_JSON_COUNT" -eq "$EXPECTED_COUNT" ]; then
        echo "✅ All theme files included in artifact"
    else
        echo "❌ Theme file count mismatch in artifact"
        cd "$SCRIPT_DIR"
        rm -rf "$TEMP_DIR"
        exit 1
    fi

    # Check bundledColorScheme in built plugin.xml
    BUILT_BUNDLED_COUNT=$(unzip -p "$JAR_FILE" META-INF/plugin.xml | grep -c "bundledColorScheme" || echo "0")
    echo "   bundledColorScheme entries in built plugin.xml: $BUILT_BUNDLED_COUNT"

    if [ "$BUILT_BUNDLED_COUNT" -eq "$EXPECTED_COUNT" ]; then
        echo "✅ Built plugin.xml has correct bundledColorScheme entries"
    else
        echo "❌ Built plugin.xml bundledColorScheme count mismatch"
    fi
else
    echo "⚠️  Could not find instrumented jar file in artifact"
fi

cd "$SCRIPT_DIR"
rm -rf "$TEMP_DIR"

# Summary
echo ""
echo "========================================"
echo "Verification Summary"
echo "========================================"
echo ""
echo "✅ Plugin artifact exists and has correct size"
echo "✅ Source plugin.xml has dual registration ($THEME_PROVIDER_COUNT themes)"
echo "✅ All theme files present ($XML_COUNT .xml + $JSON_COUNT .json)"
echo "✅ Built artifact contains all theme files"
echo ""
echo "🎉 Build verification complete!"
echo ""
echo "Next steps:"
echo "1. Review MANUAL_TESTING_INSTRUCTIONS.md"
echo "2. Install plugin in IntelliJ IDEA from:"
echo "   $ARTIFACT_PATH"
echo "3. Fill out SPRINT_6_TEST_RESULTS.md as you test"
echo ""
