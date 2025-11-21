#!/bin/bash

echo "Checking XMLColorSchemeGenerator Syntax..."
echo "============================================================"

SOURCE_FILE="src/main/kotlin/generators/XMLColorSchemeGenerator.kt"
TEST_FILE="src/test/kotlin/generators/XMLColorSchemeGeneratorTest.kt"

# Check braces in source file
OPEN_BRACES=$(grep -o '{' "$SOURCE_FILE" | wc -l)
CLOSE_BRACES=$(grep -o '}' "$SOURCE_FILE" | wc -l)

echo "Source File Syntax:"
if [ "$OPEN_BRACES" -eq "$CLOSE_BRACES" ]; then
    echo "  ✓ Braces balanced: $OPEN_BRACES open, $CLOSE_BRACES close"
else
    echo "  ✗ Braces imbalanced: $OPEN_BRACES open, $CLOSE_BRACES close"
fi

# Check parentheses in source file
OPEN_PARENS=$(grep -o '(' "$SOURCE_FILE" | wc -l)
CLOSE_PARENS=$(grep -o ')' "$SOURCE_FILE" | wc -l)

if [ "$OPEN_PARENS" -eq "$CLOSE_PARENS" ]; then
    echo "  ✓ Parentheses balanced: $OPEN_PARENS open, $CLOSE_PARENS close"
else
    echo "  ✗ Parentheses imbalanced: $OPEN_PARENS open, $CLOSE_PARENS close"
fi

# Check for package declaration
PACKAGE_COUNT=$(grep -c "^package generators" "$SOURCE_FILE")
if [ "$PACKAGE_COUNT" -eq 1 ]; then
    echo "  ✓ Package declaration: OK"
else
    echo "  ✗ Package declaration: MISSING or MULTIPLE"
fi

echo ""
echo "Test File Syntax:"

# Check braces in test file
TEST_OPEN_BRACES=$(grep -o '{' "$TEST_FILE" | wc -l)
TEST_CLOSE_BRACES=$(grep -o '}' "$TEST_FILE" | wc -l)

if [ "$TEST_OPEN_BRACES" -eq "$TEST_CLOSE_BRACES" ]; then
    echo "  ✓ Braces balanced: $TEST_OPEN_BRACES open, $TEST_CLOSE_BRACES close"
else
    echo "  ✗ Braces imbalanced: $TEST_OPEN_BRACES open, $TEST_CLOSE_BRACES close"
fi

# Check parentheses in test file
TEST_OPEN_PARENS=$(grep -o '(' "$TEST_FILE" | wc -l)
TEST_CLOSE_PARENS=$(grep -o ')' "$TEST_FILE" | wc -l)

if [ "$TEST_OPEN_PARENS" -eq "$TEST_CLOSE_PARENS" ]; then
    echo "  ✓ Parentheses balanced: $TEST_OPEN_PARENS open, $TEST_CLOSE_PARENS close"
else
    echo "  ✗ Parentheses imbalanced: $TEST_OPEN_PARENS open, $TEST_CLOSE_PARENS close"
fi

# Check for package declaration
TEST_PACKAGE_COUNT=$(grep -c "^package generators" "$TEST_FILE")
if [ "$TEST_PACKAGE_COUNT" -eq 1 ]; then
    echo "  ✓ Package declaration: OK"
else
    echo "  ✗ Package declaration: MISSING or MULTIPLE"
fi

echo ""
echo "============================================================"
echo "Syntax Check Complete!"
