#!/bin/bash

# Git Diff Analysis Script
# Task 5.2.2: Analyze git diffs for theme regeneration validation
# This script provides comprehensive diff analysis for theme files

# Note: set -e removed - we use explicit error handling with || fallbacks

THEMES_DIR="src/main/resources/themes"

echo "======================================"
echo "Git Diff Analysis Report"
echo "======================================"
echo ""

# Check git status first
echo "Git Status:"
echo "----------------------------------------"
git status --short "$THEMES_DIR"
echo ""

# Check if there are any changes
if ! git diff --quiet "$THEMES_DIR"; then
    echo "Changes detected in themes directory"
    echo ""

    # Overall diff statistics
    echo "Diff Statistics:"
    echo "----------------------------------------"
    git diff --stat "$THEMES_DIR"
    echo ""

    # Analyze JSON files
    echo "JSON Files Analysis:"
    echo "=========================================="
    echo ""

    find "$THEMES_DIR" -name "*.theme.json" | sort | while IFS= read -r file; do
        if ! git diff --quiet "$file" 2>/dev/null; then
            FILENAME=$(basename "$file")
            echo "File: $FILENAME"
            echo "----------------------------------------"

            # Count additions and deletions (filter before counting)
            ADDITIONS=$(git diff "$file" | grep "^+" | grep -v "^+++" | wc -l || echo "0")
            DELETIONS=$(git diff "$file" | grep "^-" | grep -v "^---" | wc -l || echo "0")

            echo "Lines added: $ADDITIONS"
            echo "Lines deleted: $DELETIONS"

            # Check for Terminal.Ansi colors
            TERMINAL_ADDITIONS=$(git diff "$file" | grep "^+" | grep -c "Terminal.Ansi" || echo "0")
            echo "Terminal.Ansi* additions: $TERMINAL_ADDITIONS"

            # Show sample of changes
            echo ""
            echo "Sample changes (first 50 lines):"
            git diff "$file" | head -50
            echo ""
            echo "... (use 'git diff $file' to see full diff)"
            echo ""

            # Check for potential issues
            if [ "$DELETIONS" -gt "$ADDITIONS" ]; then
                echo "⚠️  WARNING: More deletions than additions!"
            fi

            if [ "$TERMINAL_ADDITIONS" -lt 16 ]; then
                echo "⚠️  WARNING: Expected 16 Terminal.Ansi* colors, found $TERMINAL_ADDITIONS"
            fi

            echo ""
        fi
    done

    # Analyze XML files
    echo ""
    echo "XML Files Analysis:"
    echo "=========================================="
    echo ""

    find "$THEMES_DIR" -name "*.xml" | sort | while IFS= read -r file; do
        if ! git diff --quiet "$file" 2>/dev/null; then
            FILENAME=$(basename "$file")
            echo "File: $FILENAME"
            echo "----------------------------------------"

            # Count additions and deletions (filter before counting)
            ADDITIONS=$(git diff "$file" | grep "^+" | grep -v "^+++" | wc -l || echo "0")
            DELETIONS=$(git diff "$file" | grep "^-" | grep -v "^---" | wc -l || echo "0")

            echo "Lines added: $ADDITIONS"
            echo "Lines deleted: $DELETIONS"

            # Check for Terminal.Ansi colors
            TERMINAL_ADDITIONS=$(git diff "$file" | grep "^+" | grep -c "Terminal.Ansi" || echo "0")
            echo "Terminal.Ansi* additions: $TERMINAL_ADDITIONS"

            # Show sample of changes
            echo ""
            echo "Sample changes (first 50 lines):"
            git diff "$file" | head -50
            echo ""
            echo "... (use 'git diff $file' to see full diff)"
            echo ""

            # Check for potential issues
            if [ "$DELETIONS" -gt "$ADDITIONS" ]; then
                echo "⚠️  WARNING: More deletions than additions!"
            fi

            if [ "$TERMINAL_ADDITIONS" -lt 16 ]; then
                echo "⚠️  WARNING: Expected 16 Terminal.Ansi* colors, found $TERMINAL_ADDITIONS"
            fi

            echo ""
        fi
    done

    # Summary of Terminal.Ansi additions across all files
    echo ""
    echo "Summary:"
    echo "=========================================="
    TOTAL_TERMINAL_ADDITIONS=$(git diff "$THEMES_DIR" | grep "^+" | grep -c "Terminal.Ansi" || echo "0")
    echo "Total Terminal.Ansi* additions: $TOTAL_TERMINAL_ADDITIONS"

    # Count unique Terminal color keys added
    echo ""
    echo "Unique Terminal colors added:"
    git diff "$THEMES_DIR" | grep "^+" | grep "Terminal\." | sed 's/.*"\(Terminal\.[^"]*\)".*/\1/' | sort -u

else
    echo "No changes detected in themes directory."
    echo ""
    echo "This is expected if:"
    echo "  - Theme regeneration hasn't been run yet"
    echo "  - Changes were already committed"
    echo "  - Working in a clean repository"
fi

echo ""
echo "======================================"
echo "Quick Commands for Manual Review:"
echo "======================================"
echo ""
echo "View all changes:"
echo "  git diff $THEMES_DIR"
echo ""
echo "View specific file:"
echo "  git diff $THEMES_DIR/FILENAME.theme.json"
echo ""
echo "View only additions:"
echo "  git diff $THEMES_DIR | grep '^+'"
echo ""
echo "View only Terminal.Ansi changes:"
echo "  git diff $THEMES_DIR | grep Terminal.Ansi"
echo ""
echo "Interactive review:"
echo "  git diff --word-diff $THEMES_DIR/FILENAME.theme.json"
