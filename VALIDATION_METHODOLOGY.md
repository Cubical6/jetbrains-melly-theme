# Validation Methodology for FASE 5 - Task 5.2

## Overview

This document provides detailed methodology for validating theme file regeneration through git diff analysis, file size comparison, and syntax validation.

## 1. Git Status Analysis

### Purpose
Verify that theme regeneration has modified the expected files without introducing unintended changes.

### Commands

```bash
# Check overall repository status
git status

# Check status specifically for themes directory
git status src/main/resources/themes/
```

### Expected Results
- Modified files should be in `src/main/resources/themes/`
- Both `.theme.json` and `.xml` files should be modified for each theme
- No unexpected files should be modified outside the themes directory
- No untracked files should appear (unless intentionally added)

### Red Flags
- Modified files outside themes directory (unless expected)
- Deleted theme files
- Large number of untracked files
- Modified build configuration files (unless intentional)

---

## 2. File Size Comparison Methodology

### Purpose
Ensure that regenerated theme files contain the expected additional color definitions without unexpected bloat or data loss.

### Pre-Regeneration Baseline

Before regenerating themes, capture baseline file sizes:

```bash
# Create a baseline snapshot
ls -lh src/main/resources/themes/*.theme.json > theme-sizes-before.txt
ls -lh src/main/resources/themes/*.xml >> theme-sizes-before.txt
```

### Post-Regeneration Comparison

After regenerating themes:

```bash
# Capture new sizes
ls -lh src/main/resources/themes/*.theme.json > theme-sizes-after.txt
ls -lh src/main/resources/themes/*.xml >> theme-sizes-after.txt

# Side-by-side comparison
diff -y theme-sizes-before.txt theme-sizes-after.txt
```

### Quick Individual File Check

For spot-checking individual files:

```bash
# Check specific theme file size
ls -lh src/main/resources/themes/Dracula.theme.json

# Compare with git history
git show HEAD:src/main/resources/themes/Dracula.theme.json | wc -c
cat src/main/resources/themes/Dracula.theme.json | wc -c
```

### Size Change Expectations

#### JSON Files (.theme.json)
- **Expected increase**: 10-30% larger
- **Reasoning**: Adding Terminal.* color definitions (16-24 new colors)
- **Typical before**: 8-15 KB
- **Typical after**: 10-18 KB
- **Red flags**:
  - Files smaller than before (data loss)
  - Files 2x or more larger (possible duplication)
  - Files unchanged (regeneration didn't run)

#### XML Files (.xml)
- **Expected increase**: 5-20% larger
- **Reasoning**: Adding Terminal.* color keys with descriptions
- **Typical before**: 50-80 KB
- **Typical after**: 55-90 KB
- **Red flags**:
  - Files smaller than before (data loss)
  - Files dramatically larger (duplication or formatting issues)
  - Files unchanged (regeneration didn't run)

### Automated Size Analysis Script

```bash
#!/bin/bash
# compare-file-sizes.sh

echo "File Size Comparison Report"
echo "=============================="
echo ""

for file in src/main/resources/themes/*.theme.json; do
    FILENAME=$(basename "$file")
    OLD_SIZE=$(git show HEAD:"$file" 2>/dev/null | wc -c)
    NEW_SIZE=$(wc -c < "$file")

    if [ $OLD_SIZE -gt 0 ]; then
        PERCENT=$(echo "scale=1; ($NEW_SIZE - $OLD_SIZE) * 100 / $OLD_SIZE" | bc)
        echo "$FILENAME"
        echo "  Before: $OLD_SIZE bytes"
        echo "  After:  $NEW_SIZE bytes"
        echo "  Change: ${PERCENT}%"
        echo ""
    fi
done
```

---

## 3. Git Diff Analysis Instructions

### Purpose
Examine actual content changes to verify correct color additions and preservation of existing definitions.

### Basic Diff Commands

```bash
# View all changes in themes directory
git diff src/main/resources/themes/

# View changes for specific theme (JSON)
git diff src/main/resources/themes/Dracula.theme.json

# View changes for specific theme (XML)
git diff src/main/resources/themes/Dracula.xml

# View first 100 lines of diff
git diff src/main/resources/themes/Dracula.theme.json | head -100

# View diff with word-level changes (better for JSON)
git diff --word-diff src/main/resources/themes/Dracula.theme.json
```

### Focused Diff Analysis

```bash
# Show only added lines
git diff src/main/resources/themes/Dracula.theme.json | grep '^+'

# Show only removed lines
git diff src/main/resources/themes/Dracula.theme.json | grep '^-'

# Count additions vs deletions
git diff --stat src/main/resources/themes/
```

### What to Look For in Diffs

#### JSON Files (.theme.json)

**Expected Additions:**
- New `Terminal.AnsiBrightBlack` through `Terminal.AnsiBrightWhite` definitions
- New `Terminal.AnsiBlack` through `Terminal.AnsiWhite` definitions
- Properly formatted hex color values (e.g., `"#1E1E1E"`)
- Correct JSON syntax with commas and quotes

**Expected Preservation:**
- All existing color definitions remain unchanged
- Existing `ui`, `colors`, and other sections intact
- Original formatting and structure maintained

**Red Flags:**
- Removed color definitions (lines with `-` not matched by `+`)
- Malformed JSON (missing commas, quotes, brackets)
- Duplicate color definitions
- Changed existing color values (unless intentional)

**Example Good Diff Pattern:**
```diff
   "Console.Blue": "#4080D0",
+  "Terminal.AnsiBlack": "#000000",
+  "Terminal.AnsiRed": "#E06C75",
+  "Terminal.AnsiGreen": "#98C379",
   "Editor.Background": "#282C34"
```

**Example Bad Diff Pattern:**
```diff
-  "Console.Blue": "#4080D0",
+  "Console.Blue": "#4080D0"
+  "Terminal.AnsiBlack": "#000000",
```
*(Missing comma after "Console.Blue" line will cause JSON parse error)*

#### XML Files (.xml)

**Expected Additions:**
- New `<option name="Terminal.AnsiBlack">` through `Terminal.AnsiBrightWhite`
- Each with nested `<value>` element containing hex color
- Properly formatted XML structure

**Expected Preservation:**
- All existing `<option>` elements remain
- Original color values unchanged
- XML structure and formatting maintained

**Red Flags:**
- Removed `<option>` elements
- Malformed XML (unclosed tags, missing attributes)
- Duplicate color key definitions
- Changed existing color values

**Example Good Diff Pattern:**
```diff
     <option name="Console.Blue">
       <value>
         <option name="BACKGROUND" value="4080D0" />
       </value>
     </option>
+    <option name="Terminal.AnsiBlack">
+      <value>
+        <option name="BACKGROUND" value="000000" />
+      </value>
+    </option>
```

### Diff Analysis Checklist

Use this checklist when reviewing diffs:

- [ ] All Terminal.Ansi* colors are added (16 total: Black through White, Bright versions)
- [ ] No existing color definitions are removed
- [ ] No existing color values are changed (unless intentional update)
- [ ] JSON syntax is valid (proper commas, quotes, brackets)
- [ ] XML syntax is valid (proper tags, nesting, attributes)
- [ ] File structure is preserved (no reformatting unless intended)
- [ ] New colors follow the theme's color scheme appropriately
- [ ] Both .theme.json and .xml files are updated consistently

---

## 4. Syntax Validation

### JSON Validation

Use the provided validation script:

```bash
./validate-json.sh
```

**Expected Output:**
```
======================================
JSON Theme File Validation
======================================

Validating JSON files in: src/main/resources/themes

Validating: Dracula.theme.json... OK
Validating: Lovelace.theme.json... OK
...

======================================
Validation Summary
======================================
Files validated: X
Errors found: 0

SUCCESS: All JSON files are valid!
```

**Manual Validation:**
```bash
# Validate single file
jq empty src/main/resources/themes/Dracula.theme.json

# Validate with error details
jq . src/main/resources/themes/Dracula.theme.json > /dev/null
```

### XML Validation

Use the provided validation script:

```bash
./validate-xml.sh
```

**Expected Output:**
```
======================================
XML Theme File Validation
======================================

Validating XML files in: src/main/resources/themes

Validating: Dracula.xml... OK
Validating: Lovelace.xml... OK
...

======================================
Validation Summary
======================================
Files validated: X
Errors found: 0

SUCCESS: All XML files are valid!
```

**Manual Validation:**
```bash
# Validate single file
xmllint --noout src/main/resources/themes/Dracula.xml

# Validate with detailed output
xmllint src/main/resources/themes/Dracula.xml > /dev/null
```

---

## 5. Complete Validation Workflow

### Step-by-Step Process

1. **Capture Baseline (Before Regeneration)**
   ```bash
   # Save file sizes
   ls -lh src/main/resources/themes/*.theme.json > theme-sizes-before.txt
   ls -lh src/main/resources/themes/*.xml >> theme-sizes-before.txt

   # Ensure working directory is clean
   git status
   ```

2. **Regenerate Themes**
   ```bash
   # Run theme regeneration process
   # (This will be defined in Task 5.1)
   ```

3. **Check Git Status**
   ```bash
   git status
   # Verify expected files are modified
   ```

4. **Analyze File Sizes**
   ```bash
   # Check individual file
   ls -lh src/main/resources/themes/Dracula.theme.json

   # Compare before/after
   diff -y theme-sizes-before.txt <(ls -lh src/main/resources/themes/*.theme.json; ls -lh src/main/resources/themes/*.xml)
   ```

5. **Review Git Diffs**
   ```bash
   # Quick overview
   git diff --stat src/main/resources/themes/

   # Detailed review of sample file
   git diff src/main/resources/themes/Dracula.theme.json | head -100

   # Check for Terminal.* additions
   git diff src/main/resources/themes/Dracula.theme.json | grep Terminal
   ```

6. **Validate Syntax**
   ```bash
   # JSON validation
   ./validate-json.sh

   # XML validation
   ./validate-xml.sh
   ```

7. **Verify Specific Color Additions**
   ```bash
   # Check that all 16 ANSI colors are present in JSON
   grep -c "Terminal.Ansi" src/main/resources/themes/Dracula.theme.json
   # Should be 16 or more

   # Check that all 16 ANSI colors are present in XML
   grep -c "Terminal.Ansi" src/main/resources/themes/Dracula.xml
   # Should be 16 or more
   ```

8. **Review Checklist**
   - [ ] Git status shows only theme files modified
   - [ ] File sizes increased by expected amount (10-30%)
   - [ ] Diffs show Terminal.* color additions
   - [ ] Diffs show no unexpected deletions
   - [ ] JSON syntax validation passes
   - [ ] XML syntax validation passes
   - [ ] All 16 ANSI colors present in each theme
   - [ ] No duplicate color definitions

---

## 6. Common Issues and Troubleshooting

### Issue: Files Not Modified

**Symptoms:**
- `git status` shows no changes
- File sizes unchanged

**Possible Causes:**
- Regeneration script didn't run
- Script ran but didn't save changes
- Wrong directory

**Solution:**
- Verify regeneration script execution
- Check for error messages in script output
- Verify correct paths in regeneration code

### Issue: Invalid JSON Syntax

**Symptoms:**
- `validate-json.sh` reports errors
- `jq` fails to parse file

**Common Causes:**
- Missing comma between color definitions
- Trailing comma after last element
- Unescaped quotes in strings
- Mismatched brackets

**Solution:**
```bash
# Find the exact error
jq . src/main/resources/themes/PROBLEM_FILE.theme.json

# Common fixes:
# - Add missing commas
# - Remove trailing commas
# - Balance brackets
```

### Issue: Invalid XML Syntax

**Symptoms:**
- `validate-xml.sh` reports errors
- `xmllint` fails to parse file

**Common Causes:**
- Unclosed tags
- Missing attributes
- Invalid character encoding
- Improper nesting

**Solution:**
```bash
# Find the exact error
xmllint --noout src/main/resources/themes/PROBLEM_FILE.xml

# Review the specific line number reported
# Verify tag pairs match
```

### Issue: File Size Too Large

**Symptoms:**
- Files 2x or more larger than expected
- Diff shows many duplicate entries

**Possible Causes:**
- Color definitions duplicated
- Script ran multiple times without reset
- Concatenation instead of merge

**Solution:**
- Review diff for duplicates: `git diff FILE | grep Terminal | sort | uniq -c`
- Restore from backup and re-run regeneration
- Fix regeneration script logic

### Issue: File Size Unchanged or Smaller

**Symptoms:**
- Files same size or smaller after regeneration
- Diff shows deletions

**Possible Causes:**
- Data loss during regeneration
- Wrong file overwritten
- Parsing error in regeneration script

**Solution:**
- Immediately restore from git: `git checkout FILE`
- Review regeneration script for bugs
- Add error handling to regeneration script

---

## 7. Success Criteria

A successful theme regeneration must meet ALL of these criteria:

1. **Git Status**: Only theme files in `src/main/resources/themes/` are modified
2. **File Count**: All existing themes have both .theme.json and .xml updated
3. **File Sizes**: Each file is 10-30% larger (JSON) or 5-20% larger (XML)
4. **Additions**: Each theme has 16 new Terminal.Ansi* color definitions
5. **Preservation**: All existing color definitions remain unchanged
6. **JSON Syntax**: All .theme.json files pass `jq` validation
7. **XML Syntax**: All .xml files pass `xmllint` validation
8. **Consistency**: .theme.json and .xml files have matching color values
9. **No Duplicates**: Each color key appears exactly once per file
10. **Build Success**: Plugin builds successfully with new theme files

---

## 8. Quick Reference Commands

```bash
# Status check
git status

# Diff overview
git diff --stat src/main/resources/themes/

# Sample diff review
git diff src/main/resources/themes/Dracula.theme.json | head -100

# File size check
ls -lh src/main/resources/themes/Dracula.theme.json

# Syntax validation
./validate-json.sh
./validate-xml.sh

# Count color additions
git diff src/main/resources/themes/ | grep "^+" | grep Terminal | wc -l

# Verify no deletions
git diff src/main/resources/themes/ | grep "^-" | grep -v "^---" | wc -l

# Check specific theme has all ANSI colors
grep "Terminal.Ansi" src/main/resources/themes/Dracula.theme.json | wc -l
```

---

## Document Version

- **Version**: 1.0
- **Date**: 2025-11-22
- **Task**: FASE 5 - Task 5.2 (Git Diff Analysis)
- **Purpose**: Validation methodology for theme regeneration verification
