#!/bin/bash

# WCAG AA Accessibility Audit Runner
# Compiles and runs the accessibility audit tool for Windows Terminal themes

set -e

echo "========================================"
echo "WCAG AA Accessibility Audit"
echo "========================================"
echo ""

# Check if we're in the project root
if [ ! -d "windows-terminal-schemes" ]; then
    echo "ERROR: windows-terminal-schemes directory not found"
    echo "Please run this script from the project root"
    exit 1
fi

# Compile the Kotlin code
echo "Compiling audit tool..."
cd buildSrc

# Use Gradle to compile
../gradlew classes

echo ""
echo "Running accessibility audit..."
echo ""

# Run the audit script using kotlinc
cd ..
kotlin -classpath "buildSrc/build/classes/kotlin/main:buildSrc/build/libs/*" \
       -classpath "buildSrc/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.8.9/*/*/*.jar" \
       RunAccessibilityAuditKt

echo ""
echo "Audit complete!"
