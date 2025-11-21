# Manual Testing Checklist for Windows Terminal Theme Generator

Version: 1.0
Last Updated: 2025-11-21

This checklist provides structured guidance for manual QA verification of the Windows Terminal to IntelliJ theme conversion system. Follow each section systematically to ensure comprehensive testing coverage.

---

## Table of Contents

1. [Pre-Testing Setup](#pre-testing-setup)
2. [Build System Tests](#build-system-tests)
3. [Theme Loading Tests](#theme-loading-tests)
4. [Console Color Verification](#console-color-verification)
5. [Syntax Highlighting Tests](#syntax-highlighting-tests)
6. [UI Theme Tests](#ui-theme-tests)
7. [Accessibility Tests](#accessibility-tests)
8. [Edge Cases](#edge-cases)
9. [Multi-IDE Testing](#multi-ide-testing)
10. [Performance Tests](#performance-tests)
11. [Documentation Verification](#documentation-verification)
12. [Test Results Summary](#test-results-summary)

---

## Pre-Testing Setup

### Environment Preparation

- [ ] **Clean IntelliJ installation** (or create test sandbox)
  - Version: _________
  - Build: _________
  - Location: _________

- [ ] **Build the plugin**
  ```bash
  ./gradlew clean buildPlugin
  ```
  - Build successful: Yes / No
  - Build time: _________
  - Output location: `build/distributions/jetbrains-melly-theme-*.zip`

- [ ] **Verify build artifacts**
  - [ ] Plugin ZIP file exists
  - [ ] File size is reasonable (> 10KB)
  - [ ] Archive can be extracted without errors

- [ ] **Install plugin in test IDE**
  - Method: Install from disk
  - Path to ZIP: _________
  - Installation successful: Yes / No
  - IDE restart required: Yes / No

### Windows Terminal Schemes Preparation

- [ ] **Verify schemes directory**
  ```bash
  ls windows-terminal-schemes/
  ```
  - Number of schemes: _________
  - Minimum expected: 15 schemes

- [ ] **Generate themes**
  ```bash
  ./gradlew generateThemesFromWindowsTerminal
  ```
  - Generation successful: Yes / No
  - Number of themes generated: _________
  - Any failures: Yes / No (list below)
  - Failures: _________

---

## Build System Tests

### Theme Generation

- [ ] **Run theme generation task**
  ```bash
  ./gradlew generateThemesFromWindowsTerminal
  ```
  - Task completes without errors: Yes / No
  - Duration: _________

- [ ] **Verify output files**
  - Location: `src/main/resources/themes/`
  - [ ] All .xml files created
  - [ ] All .theme.json files created
  - [ ] File pairs match (same base name)

- [ ] **Check file naming conventions**
  - [ ] Names are lowercase
  - [ ] Spaces converted to underscores
  - [ ] Special characters removed
  - [ ] No duplicate names

- [ ] **Validate file content**
  - [ ] XML files are well-formed (can be parsed)
  - [ ] JSON files are valid JSON
  - [ ] No placeholder variables remain (`$wt_*$`, `$SCHEME_NAME$`)

### Build Tasks

- [ ] **Test clean build**
  ```bash
  ./gradlew clean build
  ```
  - Build successful: Yes / No
  - All tests pass: Yes / No

- [ ] **Test plugin build**
  ```bash
  ./gradlew buildPlugin
  ```
  - Build successful: Yes / No
  - Plugin ZIP created: Yes / No

- [ ] **Test with multiple schemes**
  - Add a new scheme to `windows-terminal-schemes/`
  - Run generation task
  - [ ] New theme appears in output
  - [ ] Existing themes unchanged

---

## Theme Loading Tests

### Theme Availability

- [ ] **Open IDE theme selector**
  - Path: Settings → Appearance & Behavior → Appearance → Theme
  - [ ] All generated themes appear in dropdown
  - [ ] Theme names are readable and formatted correctly
  - [ ] No duplicate theme names

- [ ] **Verify theme metadata**
  For each generated theme:
  - Theme: _________
    - [ ] Has proper name
    - [ ] Shows as "Dark" or "Light" correctly
    - [ ] Author information present

### Theme Activation

- [ ] **Activate a dark theme**
  - Theme: _________
  - [ ] Theme applies without errors
  - [ ] No error popups or exceptions
  - [ ] IDE log shows no errors
  - [ ] UI updates immediately

- [ ] **Activate a light theme**
  - Theme: _________
  - [ ] Theme applies without errors
  - [ ] No error popups or exceptions
  - [ ] IDE log shows no errors
  - [ ] UI updates immediately

### Theme Switching

- [ ] **Switch between themes rapidly**
  - [ ] No lag or freezing
  - [ ] No memory leaks visible
  - [ ] UI updates consistently
  - [ ] No visual artifacts

- [ ] **Theme persistence**
  - [ ] Activate a theme
  - [ ] Restart IDE
  - [ ] Theme is still active after restart
  - [ ] Settings are preserved

---

## Console Color Verification

### Setup Console Test Environment

Create a test script that outputs all ANSI colors:

```bash
# Save as test-colors.sh
#!/bin/bash
echo -e "\033[30mBlack\033[0m"
echo -e "\033[31mRed\033[0m"
echo -e "\033[32mGreen\033[0m"
echo -e "\033[33mYellow\033[0m"
echo -e "\033[34mBlue\033[0m"
echo -e "\033[35mMagenta\033[0m"
echo -e "\033[36mCyan\033[0m"
echo -e "\033[37mWhite\033[0m"
echo -e "\033[90mBright Black\033[0m"
echo -e "\033[91mBright Red\033[0m"
echo -e "\033[92mBright Green\033[0m"
echo -e "\033[93mBright Yellow\033[0m"
echo -e "\033[94mBright Bright Blue\033[0m"
echo -e "\033[95mBright Magenta\033[0m"
echo -e "\033[96mBright Cyan\033[0m"
echo -e "\033[97mBright White\033[0m"
```

### ANSI Color Testing

Test with each Windows Terminal scheme:

#### One Dark Example

- [ ] **Run test script in IDE terminal**
  ```bash
  bash test-colors.sh
  ```

- [ ] **Verify colors match Windows Terminal**
  - [ ] Black (ANSI 0) - Expected: `#1e2127`
  - [ ] Red (ANSI 1) - Expected: `#e06c75`
  - [ ] Green (ANSI 2) - Expected: `#98c379`
  - [ ] Yellow (ANSI 3) - Expected: `#e5c07b`
  - [ ] Blue (ANSI 4) - Expected: `#61afef`
  - [ ] Magenta (ANSI 5) - Expected: `#c678dd`
  - [ ] Cyan (ANSI 6) - Expected: `#56b6c2`
  - [ ] White (ANSI 7) - Expected: `#abb2bf`
  - [ ] Bright Black (ANSI 8) - Expected: `#5c6370`
  - [ ] Bright Red (ANSI 9) - Expected: `#e06c75`
  - [ ] Bright Green (ANSI 10) - Expected: `#98c379`
  - [ ] Bright Yellow (ANSI 11) - Expected: `#e5c07b`
  - [ ] Bright Blue (ANSI 12) - Expected: `#61afef`
  - [ ] Bright Magenta (ANSI 13) - Expected: `#c678dd`
  - [ ] Bright Cyan (ANSI 14) - Expected: `#56b6c2`
  - [ ] Bright White (ANSI 15) - Expected: `#ffffff`

#### Additional Schemes

Repeat the above tests for:

- [ ] **Dracula**
- [ ] **Nord**
- [ ] **Solarized Dark**
- [ ] **Solarized Light**
- [ ] **Gruvbox Dark**

### RGB Exact Match Verification

- [ ] **Use color picker tool** (e.g., Digital Color Meter on macOS, ColorPicker on Windows)
  - Theme tested: _________
  - [ ] Sample 5 colors from terminal
  - [ ] Compare RGB values with Windows Terminal
  - [ ] Document any discrepancies: _________

### Console Background/Foreground

- [ ] **Verify console background color**
  - Matches theme background: Yes / No
  - Measured RGB: _________
  - Expected RGB: _________

- [ ] **Verify console foreground color**
  - Matches theme foreground: Yes / No
  - Measured RGB: _________
  - Expected RGB: _________

---

## Syntax Highlighting Tests

Test syntax highlighting in multiple languages to verify editor color scheme generation.

### Java Syntax Highlighting

Create test file `Test.java`:
```java
package com.example;

import java.util.*;

/**
 * Test class for syntax highlighting
 * TODO: Test comment highlighting
 * FIXME: Check error highlighting
 */
public class Test {
    private static final String CONSTANT = "string literal";
    private int number = 42;

    public void method() {
        // Line comment
        if (true) {
            System.out.println("Hello World");
        }
    }
}
```

- [ ] **Keywords** (public, class, static, final, if, etc.)
  - Visible: Yes / No
  - Color appropriate: Yes / No
  - Contrast sufficient: Yes / No

- [ ] **Strings** ("Hello World", "string literal")
  - Visible: Yes / No
  - Color appropriate: Yes / No
  - Distinct from comments: Yes / No

- [ ] **Comments** (// and /* */)
  - Visible: Yes / No
  - Color appropriate: Yes / No
  - Distinct from code: Yes / No

- [ ] **Numbers** (42)
  - Visible: Yes / No
  - Color appropriate: Yes / No

- [ ] **TODO/FIXME** highlighting
  - Highlighted: Yes / No
  - Color appropriate: Yes / No

### Kotlin Syntax Highlighting

Create test file `Test.kt`:
```kotlin
package com.example

import kotlin.math.*

/**
 * Test data class
 * TODO: Add more tests
 */
data class Person(
    val name: String,
    val age: Int = 0
) {
    fun greet() {
        println("Hello, $name!")
    }

    companion object {
        const val MAX_AGE = 150
    }
}
```

- [ ] **Keywords** (package, import, data, class, val, fun, etc.)
  - Visible: Yes / No
  - Color appropriate: Yes / No

- [ ] **String templates** ($name)
  - Highlighted: Yes / No
  - Distinct from plain strings: Yes / No

- [ ] **Annotations** (@)
  - Visible: Yes / No
  - Color appropriate: Yes / No

### Python Syntax Highlighting

Create test file `test.py`:
```python
#!/usr/bin/env python3
"""
Module docstring
TODO: Test highlighting
"""

import sys
from typing import List

class TestClass:
    """Class docstring"""

    def __init__(self, name: str):
        self.name = name
        self.number = 42

    def method(self) -> None:
        # Line comment
        print(f"Hello {self.name}")

if __name__ == "__main__":
    test = TestClass("World")
    test.method()
```

- [ ] **Keywords** (class, def, if, import, etc.)
  - Visible: Yes / No
  - Color appropriate: Yes / No

- [ ] **Decorators** (@)
  - Visible: Yes / No
  - Color appropriate: Yes / No

- [ ] **Docstrings** (""" """)
  - Distinct from comments: Yes / No
  - Visible: Yes / No

### JavaScript/TypeScript Syntax Highlighting

Create test file `test.ts`:
```typescript
/**
 * Test interface
 * TODO: Add more fields
 */
interface User {
    name: string;
    age: number;
    active: boolean;
}

const greeting = "Hello World";
const number = 42;

function greet(user: User): void {
    // Line comment
    console.log(`Hello ${user.name}!`);
}

export default greet;
```

- [ ] **Keywords** (interface, const, function, export, etc.)
  - Visible: Yes / No
  - Color appropriate: Yes / No

- [ ] **Template literals** (`Hello ${user.name}`)
  - Highlighted: Yes / No
  - Placeholders distinct: Yes / No

- [ ] **Type annotations** (: string, : number)
  - Visible: Yes / No
  - Color appropriate: Yes / No

### XML/HTML Syntax Highlighting

Create test file `test.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- XML comment -->
<root xmlns="http://example.com">
    <element attribute="value">
        <nested>Text content</nested>
    </element>
    <!-- TODO: Add more elements -->
</root>
```

- [ ] **Tags** (<root>, <element>)
  - Visible: Yes / No
  - Color appropriate: Yes / No

- [ ] **Attributes** (attribute="value")
  - Distinct from tags: Yes / No
  - Visible: Yes / No

- [ ] **Comments** (<!-- -->)
  - Visible: Yes / No
  - Distinct from tags: Yes / No

### Additional Languages

Test at least 2 more languages:

- [ ] Language: _________
  - Keywords visible: Yes / No
  - Strings highlighted: Yes / No
  - Comments visible: Yes / No

- [ ] Language: _________
  - Keywords visible: Yes / No
  - Strings highlighted: Yes / No
  - Comments visible: Yes / No

### Error Highlighting

- [ ] **Syntax errors** are visible
  - Create intentional syntax error
  - Error underline appears: Yes / No
  - Error color distinct: Yes / No
  - Readable: Yes / No

- [ ] **TODO highlighting**
  - TODO comments highlighted: Yes / No
  - FIXME comments highlighted: Yes / No
  - Color distinct: Yes / No

### Diff Highlighting

- [ ] **Version control diff**
  - Open file with uncommitted changes
  - [ ] Added lines visible (green/positive color)
  - [ ] Removed lines visible (red/negative color)
  - [ ] Modified lines visible
  - [ ] Colors match theme

---

## UI Theme Tests

### Main Window Elements

- [ ] **Menu bar**
  - Background color appropriate: Yes / No
  - Text readable: Yes / No
  - Hover state visible: Yes / No

- [ ] **Toolbar**
  - Icons visible: Yes / No
  - Background appropriate: Yes / No
  - Button states clear: Yes / No

- [ ] **Status bar**
  - Background color appropriate: Yes / No
  - Text readable: Yes / No
  - Icons visible: Yes / No

### Editor Area

- [ ] **Editor background**
  - Matches theme background: Yes / No
  - Comfortable for long viewing: Yes / No

- [ ] **Editor foreground**
  - Matches theme foreground: Yes / No
  - Readable: Yes / No
  - Good contrast: Yes / No

- [ ] **Line numbers**
  - Visible: Yes / No
  - Color appropriate: Yes / No
  - Not distracting: Yes / No

- [ ] **Current line highlight**
  - Visible: Yes / No
  - Subtle enough: Yes / No
  - Distinct from selection: Yes / No

- [ ] **Selection background**
  - Visible: Yes / No
  - Text readable over selection: Yes / No
  - Distinct from other highlights: Yes / No

### Tool Windows

- [ ] **Project panel**
  - Background color appropriate: Yes / No
  - Text readable: Yes / No
  - Tree structure clear: Yes / No
  - Icons visible: Yes / No

- [ ] **Terminal**
  - Background matches theme: Yes / No
  - Foreground matches theme: Yes / No
  - ANSI colors correct: Yes / No

- [ ] **Git/VCS window**
  - Diff colors appropriate: Yes / No
  - Text readable: Yes / No

- [ ] **Debug window**
  - Text readable: Yes / No
  - Stack trace clear: Yes / No
  - Variables visible: Yes / No

### Dialogs and Popups

- [ ] **Settings dialog**
  - Background appropriate: Yes / No
  - Text readable: Yes / No
  - Form elements visible: Yes / No

- [ ] **Code completion popup**
  - Background appropriate: Yes / No
  - Selected item clear: Yes / No
  - Text readable: Yes / No
  - Documentation visible: Yes / No

- [ ] **Parameter hints**
  - Visible: Yes / No
  - Readable: Yes / No

---

## Accessibility Tests

### Contrast Ratios

Use a contrast checker tool (e.g., WebAIM Contrast Checker):

- [ ] **Editor text contrast**
  - Foreground color: _________
  - Background color: _________
  - Ratio: _________
  - Meets WCAG AA (4.5:1): Yes / No
  - Meets WCAG AAA (7:1): Yes / No

- [ ] **Console output contrast**
  - Sample color: _________
  - Background: _________
  - Ratio: _________
  - Meets WCAG AA: Yes / No

- [ ] **Syntax highlighting contrast**
  - Keyword color: _________
  - Ratio: _________
  - Meets WCAG AA: Yes / No

### Low Light Conditions

- [ ] **Test in dimmed room**
  - Theme: _________
  - [ ] Dark theme comfortable
  - [ ] No eye strain
  - [ ] All elements visible

- [ ] **Test in bright conditions**
  - Theme: _________
  - [ ] Light theme usable
  - [ ] No glare issues
  - [ ] All elements visible

### Color Blindness Compatibility

Use color blindness simulators (e.g., Color Oracle):

- [ ] **Protanopia (red-blind) simulation**
  - Error highlights distinguishable: Yes / No
  - Syntax colors distinct: Yes / No
  - Console colors usable: Yes / No

- [ ] **Deuteranopia (green-blind) simulation**
  - Error highlights distinguishable: Yes / No
  - Syntax colors distinct: Yes / No
  - Console colors usable: Yes / No

- [ ] **Tritanopia (blue-blind) simulation**
  - Error highlights distinguishable: Yes / No
  - Syntax colors distinct: Yes / No
  - Console colors usable: Yes / No

---

## Edge Cases

### Monochrome Themes

- [ ] **Load monochrome test theme**
  - Theme loads: Yes / No
  - All UI elements visible: Yes / No
  - Grayscale colors distinct enough: Yes / No
  - Still usable: Yes / No

### High Contrast Themes

- [ ] **Load high contrast theme**
  - Theme loads: Yes / No
  - Colors very distinct: Yes / No
  - No visual artifacts: Yes / No
  - Comfortable to use: Yes / No

### Light Themes on Light Backgrounds

- [ ] **Test light theme visibility**
  - Theme: _________
  - [ ] All text readable
  - [ ] Selection visible
  - [ ] Cursor visible
  - [ ] No white-on-white issues

### Dark Themes on Dark Backgrounds

- [ ] **Test dark theme visibility**
  - Theme: _________
  - [ ] All text readable
  - [ ] Selection visible
  - [ ] Cursor visible
  - [ ] No black-on-black issues

### Special Characters in Theme Names

- [ ] **Create scheme with special characters**
  - Name: "Test & <Special> Characters!"
  - [ ] File names sanitized correctly
  - [ ] Theme loads without errors
  - [ ] Display name preserved

### Very Long Theme Names

- [ ] **Create scheme with long name**
  - Name: (50+ characters)
  - [ ] Name handled correctly
  - [ ] No truncation issues in UI
  - [ ] File name reasonable

---

## Multi-IDE Testing

### IntelliJ IDEA Community Edition

- [ ] **Version**: _________
- [ ] Plugin installs: Yes / No
- [ ] Themes load: Yes / No
- [ ] Console colors correct: Yes / No
- [ ] Syntax highlighting works: Yes / No
- [ ] No errors: Yes / No

### IntelliJ IDEA Ultimate Edition

- [ ] **Version**: _________
- [ ] Plugin installs: Yes / No
- [ ] Themes load: Yes / No
- [ ] Console colors correct: Yes / No
- [ ] Syntax highlighting works: Yes / No
- [ ] No errors: Yes / No

### PhpStorm

- [ ] **Version**: _________
- [ ] Plugin installs: Yes / No
- [ ] Themes load: Yes / No
- [ ] PHP syntax highlighting: Yes / No
- [ ] Console colors correct: Yes / No
- [ ] No errors: Yes / No

### PyCharm

- [ ] **Version**: _________
- [ ] Plugin installs: Yes / No
- [ ] Themes load: Yes / No
- [ ] Python syntax highlighting: Yes / No
- [ ] Console colors correct: Yes / No
- [ ] No errors: Yes / No

### WebStorm (Optional)

- [ ] **Version**: _________
- [ ] Plugin installs: Yes / No
- [ ] Themes load: Yes / No
- [ ] JavaScript/TypeScript syntax highlighting: Yes / No
- [ ] Console colors correct: Yes / No
- [ ] No errors: Yes / No

---

## Performance Tests

### Theme Switching Performance

- [ ] **Measure theme switch time**
  - Switch from default to test theme
  - Time measured: _________ ms
  - Acceptable (< 1 second): Yes / No

- [ ] **Rapid theme switching**
  - Switch between 5 themes rapidly
  - [ ] No lag or freezing
  - [ ] UI remains responsive
  - [ ] No visual artifacts

### Memory Usage

- [ ] **Monitor memory before theme activation**
  - IDE memory usage: _________ MB

- [ ] **Monitor memory after theme activation**
  - IDE memory usage: _________ MB
  - Memory increase: _________ MB
  - Acceptable (< 50MB increase): Yes / No

- [ ] **Check for memory leaks**
  - Switch themes 20 times
  - Final memory usage: _________ MB
  - Memory leak suspected: Yes / No

### IDE Startup Performance

- [ ] **Measure startup time with custom theme**
  - Theme active: _________
  - Startup time: _________ seconds
  - Baseline (no custom theme): _________ seconds
  - Impact acceptable (< 10% increase): Yes / No

---

## Documentation Verification

### README Instructions

- [ ] **Follow README build instructions**
  - Instructions clear: Yes / No
  - All steps work: Yes / No
  - No missing dependencies: Yes / No
  - Any issues: _________

### Build Instructions

- [ ] **Test all build commands**
  ```bash
  ./gradlew clean
  ./gradlew build
  ./gradlew test
  ./gradlew buildPlugin
  ./gradlew generateThemesFromWindowsTerminal
  ```
  - All commands work: Yes / No
  - Any failures: _________

### Code Documentation

- [ ] **Review inline code documentation**
  - Classes documented: Yes / No
  - Methods documented: Yes / No
  - Complex logic explained: Yes / No

### Examples and Tutorials

- [ ] **Test any provided examples**
  - Examples work: Yes / No
  - Examples up-to-date: Yes / No

---

## Test Results Summary

### Overall Results

**Test Date**: _________
**Tester Name**: _________
**Environment**: _________

### Summary Statistics

- Total test items: _________
- Passed: _________
- Failed: _________
- Skipped: _________
- Pass rate: _________ %

### Critical Issues Found

1. _________
2. _________
3. _________

### Minor Issues Found

1. _________
2. _________
3. _________

### Recommendations

1. _________
2. _________
3. _________

### Sign-Off

- [ ] **All critical tests passed**
- [ ] **No blocking issues found**
- [ ] **Theme generation works correctly**
- [ ] **Console colors accurate**
- [ ] **Accessibility requirements met**
- [ ] **Performance acceptable**
- [ ] **Documentation accurate**

**Approved for release**: Yes / No
**Signature**: _________
**Date**: _________

---

## Appendix A: Test Script for Console Colors

```bash
#!/bin/bash
# test-ansi-colors.sh
# Outputs all 16 ANSI colors for visual verification

echo "=== Normal Colors ==="
echo -e "\033[30m█ Black (30)\033[0m"
echo -e "\033[31m█ Red (31)\033[0m"
echo -e "\033[32m█ Green (32)\033[0m"
echo -e "\033[33m█ Yellow (33)\033[0m"
echo -e "\033[34m█ Blue (34)\033[0m"
echo -e "\033[35m█ Magenta (35)\033[0m"
echo -e "\033[36m█ Cyan (36)\033[0m"
echo -e "\033[37m█ White (37)\033[0m"

echo ""
echo "=== Bright Colors ==="
echo -e "\033[90m█ Bright Black (90)\033[0m"
echo -e "\033[91m█ Bright Red (91)\033[0m"
echo -e "\033[92m█ Bright Green (92)\033[0m"
echo -e "\033[93m█ Bright Yellow (93)\033[0m"
echo -e "\033[94m█ Bright Blue (94)\033[0m"
echo -e "\033[95m█ Bright Magenta (95)\033[0m"
echo -e "\033[96m█ Bright Cyan (96)\033[0m"
echo -e "\033[97m█ Bright White (97)\033[0m"
```

## Appendix B: Color Contrast Checker Commands

For command-line contrast checking:

```bash
# Example using ImageMagick to extract colors from screenshot
convert screenshot.png -crop 1x1+X+Y txt:- | grep -o '#[0-9A-Fa-f]\{6\}'
```

## Appendix C: Expected Color Values

### One Dark Example Color Reference

| Color | Hex Value | RGB |
|-------|-----------|-----|
| Background | #282c34 | 40, 44, 52 |
| Foreground | #abb2bf | 171, 178, 191 |
| Black | #1e2127 | 30, 33, 39 |
| Red | #e06c75 | 224, 108, 117 |
| Green | #98c379 | 152, 195, 121 |
| Yellow | #e5c07b | 229, 192, 123 |
| Blue | #61afef | 97, 175, 239 |
| Magenta | #c678dd | 198, 120, 221 |
| Cyan | #56b6c2 | 86, 182, 194 |
| White | #abb2bf | 171, 178, 191 |
| Bright Black | #5c6370 | 92, 99, 112 |
| Bright Red | #e06c75 | 224, 108, 117 |
| Bright Green | #98c379 | 152, 195, 121 |
| Bright Yellow | #e5c07b | 229, 192, 123 |
| Bright Blue | #61afef | 97, 175, 239 |
| Bright Magenta | #c678dd | 198, 120, 221 |
| Bright Cyan | #56b6c2 | 86, 182, 194 |
| Bright White | #ffffff | 255, 255, 255 |

---

**End of Manual Testing Checklist**
