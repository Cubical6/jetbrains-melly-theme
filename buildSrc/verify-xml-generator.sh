#!/bin/bash

echo "Verifying XMLColorSchemeGenerator Implementation..."
echo "============================================================"

SOURCE_FILE="src/main/kotlin/generators/XMLColorSchemeGenerator.kt"
TEST_FILE="src/test/kotlin/generators/XMLColorSchemeGeneratorTest.kt"

if [ ! -f "$SOURCE_FILE" ]; then
    echo "ERROR: XMLColorSchemeGenerator.kt not found!"
    exit 1
fi

echo ""
echo "Core Functionality"
grep -q "class XMLColorSchemeGenerator" "$SOURCE_FILE" && echo "  ✓ XMLColorSchemeGenerator class: FOUND" || echo "  ✗ XMLColorSchemeGenerator class: MISSING"
grep -q "fun generate" "$SOURCE_FILE" && echo "  ✓ generate method: FOUND" || echo "  ✗ generate method: MISSING"
grep -q "fun generatePreview" "$SOURCE_FILE" && echo "  ✓ generatePreview method: FOUND" || echo "  ✗ generatePreview method: MISSING"

echo ""
echo "Template Handling"
grep -q "readTemplate" "$SOURCE_FILE" && echo "  ✓ readTemplate method: FOUND" || echo "  ✗ readTemplate method: MISSING"
grep -q "isTemplateAvailable" "$SOURCE_FILE" && echo "  ✓ isTemplateAvailable method: FOUND" || echo "  ✗ isTemplateAvailable method: MISSING"
grep -q "getExpectedPlaceholders" "$SOURCE_FILE" && echo "  ✓ getExpectedPlaceholders method: FOUND" || echo "  ✗ getExpectedPlaceholders method: MISSING"

echo ""
echo "Placeholder Replacement"
grep -q "buildReplacementMap" "$SOURCE_FILE" && echo "  ✓ buildReplacementMap method: FOUND" || echo "  ✗ buildReplacementMap method: MISSING"
grep -q '\$SCHEME_NAME\$' "$SOURCE_FILE" && echo "  ✓ SCHEME_NAME placeholder: FOUND" || echo "  ✗ SCHEME_NAME placeholder: MISSING"
grep -q '\$wt_background\$' "$SOURCE_FILE" && echo "  ✓ wt_background placeholder: FOUND" || echo "  ✗ wt_background placeholder: MISSING"
grep -q '\$wt_magenta\$' "$SOURCE_FILE" && echo "  ✓ wt_magenta placeholder: FOUND" || echo "  ✗ wt_magenta placeholder: MISSING"

echo ""
echo "Color Normalization"
grep -q "normalizeColor" "$SOURCE_FILE" && echo "  ✓ normalizeColor method: FOUND" || echo "  ✗ normalizeColor method: MISSING"
grep -q "removePrefix" "$SOURCE_FILE" && echo "  ✓ Color hash removal: FOUND" || echo "  ✗ Color hash removal: MISSING"

echo ""
echo "XML Validation"
grep -q "validateXml" "$SOURCE_FILE" && echo "  ✓ validateXml method: FOUND" || echo "  ✗ validateXml method: MISSING"
grep -q "DocumentBuilderFactory" "$SOURCE_FILE" && echo "  ✓ XML parser usage: FOUND" || echo "  ✗ XML parser usage: MISSING"

echo ""
echo "Dependencies"
grep -q "import colorschemes.WindowsTerminalColorScheme" "$SOURCE_FILE" && echo "  ✓ WindowsTerminalColorScheme import: FOUND" || echo "  ✗ WindowsTerminalColorScheme import: MISSING"
grep -q "import mapping.ConsoleColorMapper" "$SOURCE_FILE" && echo "  ✓ ConsoleColorMapper import: FOUND" || echo "  ✗ ConsoleColorMapper import: MISSING"
grep -q "import mapping.SyntaxColorInference" "$SOURCE_FILE" && echo "  ✓ SyntaxColorInference import: FOUND" || echo "  ✗ SyntaxColorInference import: MISSING"

echo ""
echo "Test File"
if [ -f "$TEST_FILE" ]; then
    TEST_COUNT=$(grep -c "@Test" "$TEST_FILE")
    echo "  ✓ XMLColorSchemeGeneratorTest.kt: FOUND"
    echo "  ✓ Number of tests: $TEST_COUNT"
else
    echo "  ✗ XMLColorSchemeGeneratorTest.kt: NOT FOUND"
fi

echo ""
echo "Code Statistics"
echo "  - Source file lines: $(wc -l < "$SOURCE_FILE")"
if [ -f "$TEST_FILE" ]; then
    echo "  - Test file lines: $(wc -l < "$TEST_FILE")"
fi

echo ""
echo "============================================================"
echo "Verification Complete!"
