#!/bin/bash

echo "Verifying SyntaxColorInference Implementation..."
echo "============================================================"

SOURCE_FILE="src/main/kotlin/mapping/SyntaxColorInference.kt"
TEST_FILE="src/test/kotlin/mapping/SyntaxColorInferenceTest.kt"

if [ ! -f "$SOURCE_FILE" ]; then
    echo "ERROR: SyntaxColorInference.kt not found!"
    exit 1
fi

echo ""
echo "Phase 1: Color Classification"
grep -q "fun classifyColor\|private fun classifyColor" "$SOURCE_FILE" && echo "  ✓ classifyColor method: FOUND" || echo "  ✗ classifyColor method: MISSING"
grep -q "calculateLuminance" "$SOURCE_FILE" && echo "  ✓ Luminance calculation: FOUND" || echo "  ✗ Luminance calculation: MISSING"
grep -q "LuminanceClass" "$SOURCE_FILE" && echo "  ✓ LuminanceClass enum: FOUND" || echo "  ✗ LuminanceClass enum: MISSING"

echo ""
echo "Phase 2: Semantic Mapping"
grep -q "fun inferSyntaxColors" "$SOURCE_FILE" && echo "  ✓ inferSyntaxColors method: FOUND" || echo "  ✗ inferSyntaxColors method: MISSING"
grep -q "ColorMappingConfig" "$SOURCE_FILE" && echo "  ✓ ColorMappingConfig usage: FOUND" || echo "  ✗ ColorMappingConfig usage: MISSING"
grep -q "SyntaxRule" "$SOURCE_FILE" && echo "  ✓ SyntaxRule handling: FOUND" || echo "  ✗ SyntaxRule handling: MISSING"

echo ""
echo "Phase 3: Edge Case Handling"
grep -q "detectMonochrome" "$SOURCE_FILE" && echo "  ✓ Monochrome detection: FOUND" || echo "  ✗ Monochrome detection: MISSING"
grep -q "analyzeContrast" "$SOURCE_FILE" && echo "  ✓ Contrast analysis: FOUND" || echo "  ✗ Contrast analysis: MISSING"
grep -q "determineFontStyleForMonochrome" "$SOURCE_FILE" && echo "  ✓ Font style fallback: FOUND" || echo "  ✗ Font style fallback: MISSING"
grep -q "adjustForLowContrast" "$SOURCE_FILE" && grep -q "adjustForHighContrast" "$SOURCE_FILE" && echo "  ✓ Contrast adjustment: FOUND" || echo "  ✗ Contrast adjustment: MISSING"

echo ""
echo "Data Classes"
grep -q "data class ColorClassification" "$SOURCE_FILE" && echo "  ✓ ColorClassification: FOUND" || echo "  ✗ ColorClassification: MISSING"
grep -q "data class SyntaxColor" "$SOURCE_FILE" && echo "  ✓ SyntaxColor: FOUND" || echo "  ✗ SyntaxColor: MISSING"
grep -q "enum class ContrastLevel" "$SOURCE_FILE" && echo "  ✓ ContrastLevel: FOUND" || echo "  ✗ ContrastLevel: MISSING"
grep -q "data class PaletteAnalysis" "$SOURCE_FILE" && echo "  ✓ PaletteAnalysis: FOUND" || echo "  ✗ PaletteAnalysis: MISSING"

echo ""
echo "Test Resources"
TEST_SCHEMES_DIR="src/test/resources/test-schemes"
if [ -d "$TEST_SCHEMES_DIR" ]; then
    echo "  ✓ Test schemes directory: FOUND"
    SCHEME_COUNT=$(ls -1 "$TEST_SCHEMES_DIR"/*.json 2>/dev/null | wc -l)
    echo "  ✓ Test scheme count: $SCHEME_COUNT"
    ls -1 "$TEST_SCHEMES_DIR"/*.json 2>/dev/null | sed 's/.*\//    - /'
else
    echo "  ✗ Test schemes directory: NOT FOUND"
fi

echo ""
echo "Test File"
if [ -f "$TEST_FILE" ]; then
    TEST_COUNT=$(grep -c "@Test" "$TEST_FILE")
    echo "  ✓ SyntaxColorInferenceTest.kt: FOUND"
    echo "  ✓ Number of tests: $TEST_COUNT"
else
    echo "  ✗ SyntaxColorInferenceTest.kt: NOT FOUND"
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
