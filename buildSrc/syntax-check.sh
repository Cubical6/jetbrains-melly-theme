#!/bin/bash

echo "Checking Kotlin Syntax..."
echo "============================================================"

SOURCE_FILE="src/main/kotlin/mapping/SyntaxColorInference.kt"
TEST_FILE="src/test/kotlin/mapping/SyntaxColorInferenceTest.kt"

echo ""
echo "Checking package declarations..."
grep -q "^package mapping$" "$SOURCE_FILE" && echo "  ✓ Source package: OK" || echo "  ✗ Source package: MISSING"
grep -q "^package mapping$" "$TEST_FILE" && echo "  ✓ Test package: OK" || echo "  ✗ Test package: MISSING"

echo ""
echo "Checking imports..."
grep -q "^import colorschemes.WindowsTerminalColorScheme$" "$SOURCE_FILE" && echo "  ✓ WindowsTerminalColorScheme import: OK" || echo "  ✗ WindowsTerminalColorScheme import: MISSING"
grep -q "^import utils.ColorUtils$" "$SOURCE_FILE" && echo "  ✓ ColorUtils import: OK" || echo "  ✗ ColorUtils import: MISSING"

echo ""
echo "Checking class/object declarations..."
grep -q "^object SyntaxColorInference {" "$SOURCE_FILE" && echo "  ✓ SyntaxColorInference object: OK" || echo "  ✗ SyntaxColorInference object: MALFORMED"

echo ""
echo "Checking public API..."
grep -q "fun inferSyntaxColors(scheme: WindowsTerminalColorScheme): Map<String, SyntaxColor>" "$SOURCE_FILE" && echo "  ✓ inferSyntaxColors signature: OK" || echo "  ✗ inferSyntaxColors signature: MALFORMED"

echo ""
echo "Checking for unclosed braces..."
OPEN_BRACES=$(grep -o "{" "$SOURCE_FILE" | wc -l)
CLOSE_BRACES=$(grep -o "}" "$SOURCE_FILE" | wc -l)
if [ "$OPEN_BRACES" -eq "$CLOSE_BRACES" ]; then
    echo "  ✓ Braces balanced: $OPEN_BRACES open, $CLOSE_BRACES close"
else
    echo "  ✗ Braces unbalanced: $OPEN_BRACES open, $CLOSE_BRACES close"
fi

echo ""
echo "Checking for unclosed parentheses..."
OPEN_PARENS=$(grep -o "(" "$SOURCE_FILE" | wc -l)
CLOSE_PARENS=$(grep -o ")" "$SOURCE_FILE" | wc -l)
if [ "$OPEN_PARENS" -eq "$CLOSE_PARENS" ]; then
    echo "  ✓ Parentheses balanced: $OPEN_PARENS open, $CLOSE_PARENS close"
else
    echo "  ✗ Parentheses unbalanced: $OPEN_PARENS open, $CLOSE_PARENS close"
fi

echo ""
echo "============================================================"
echo "Syntax Check Complete!"
