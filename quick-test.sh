#!/bin/bash

# Quick test script for theme generation

echo "🧪 Quick Theme Generation Test"
echo "==============================="
echo ""

# Test 1: Check placeholder replacement
echo "Test 1: Checking for unreplaced placeholders..."
UNREPLACED=$(grep -r '\$wt_' src/main/resources/themes/*.theme.json 2>/dev/null | wc -l)

if [ "$UNREPLACED" -eq 0 ]; then
  echo "✅ PASS: All placeholders replaced"
else
  echo "❌ FAIL: Found $UNREPLACED unreplaced placeholders"
  grep -r '\$wt_' src/main/resources/themes/*.theme.json | head -5
fi

# Test 2: Check theme count
echo ""
echo "Test 2: Checking theme file count..."
THEME_COUNT=$(ls src/main/resources/themes/*.theme.json 2>/dev/null | wc -l)

if [ "$THEME_COUNT" -eq 116 ]; then
  echo "✅ PASS: All 116 theme files present"
else
  echo "⚠️  WARNING: Expected 116 themes, found $THEME_COUNT"
fi

# Test 3: Check for parentTheme
echo ""
echo "Test 3: Checking parentTheme in generated files..."
PARENT_COUNT=$(grep -c '"parentTheme"' src/main/resources/themes/wt-lovelace-abd97252.theme.json 2>/dev/null)

if [ "$PARENT_COUNT" -eq 1 ]; then
  echo "✅ PASS: parentTheme present in generated themes"
else
  echo "❌ FAIL: parentTheme missing in generated themes"
fi

# Test 4: Check rounded arcs
echo ""
echo "Test 4: Checking arc values in rounded theme..."
ARC_COUNT=$(grep -c '"arc":' src/main/resources/themes/wt-lovelace-abd97252_rounded.theme.json 2>/dev/null)

if [ "$ARC_COUNT" -gt 0 ]; then
  echo "✅ PASS: Arc values found in rounded theme ($ARC_COUNT arcs)"
else
  echo "❌ FAIL: No arc values in rounded theme"
fi

# Test 5: Check plugin.xml
echo ""
echo "Test 5: Checking plugin.xml registration..."
THEME_PROVIDERS=$(grep -c '<themeProvider' src/main/resources/META-INF/plugin.xml 2>/dev/null)

if [ "$THEME_PROVIDERS" -eq 116 ]; then
  echo "✅ PASS: All themes registered in plugin.xml"
else
  echo "⚠️  WARNING: Expected 116 themeProvider entries, found $THEME_PROVIDERS"
fi

# Summary
echo ""
echo "==============================="
echo "Test Summary:"
echo "  - Placeholder replacement: $([ "$UNREPLACED" -eq 0 ] && echo "✅" || echo "❌")"
echo "  - Theme count: $([ "$THEME_COUNT" -eq 116 ] && echo "✅" || echo "⚠️")"
echo "  - ParentTheme: $([ "$PARENT_COUNT" -eq 1 ] && echo "✅" || echo "❌")"
echo "  - Arc values: $([ "$ARC_COUNT" -gt 0 ] && echo "✅" || echo "❌")"
echo "  - Plugin registration: $([ "$THEME_PROVIDERS" -eq 116 ] && echo "✅" || echo "⚠️")"
echo "==============================="
