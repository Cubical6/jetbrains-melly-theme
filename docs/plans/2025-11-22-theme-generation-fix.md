# Theme Generation Fix Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Fix theme generation to match Lovelace-Theme demo quality, including proper rounded variants, correct color replacements, and complete UI sections.

**Architecture:** Fix placeholder naming mismatches in templates, add missing UI sections (Islands, MainToolbar, MainWindow), expand icon color palette, add parentTheme support, and verify rounded corners work in actual IntelliJ.

**Tech Stack:** Kotlin (theme generation), JSON (theme templates), IntelliJ Platform Theme API

---

## Analysis Summary

Investigation revealed **7 critical issues** causing differences between generated themes and the Lovelace-Theme demo:

1. **Placeholder Name Mismatches** - Template uses camelCase (`$wt_buttonBorderFocused$`) but ColorPalette defines snake_case (`wt_button_border_focused`)
2. **Missing parentTheme** - Generated themes don't inherit from `ExperimentalDark` base theme
3. **Algorithmic vs Hand-Tuned Colors** - Generated uses formulas, demo uses curated values
4. **Missing UI Sections** - Islands, MainToolbar, MainWindow not in templates
5. **Limited Icon Colors** - Only 9 checkbox icons vs demo's 14 comprehensive icon colors
6. **Arc Properties Present But Unsupported** - Rounded values exist but may need parentTheme to render
7. **EditorTabs Missing Properties** - Underlined tab styling incomplete

---

## Task 1: Fix Placeholder Naming Consistency (CRITICAL BUG)

**Files:**
- Read: `buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt:454-459`
- Modify: `buildSrc/templates/windows-terminal-rounded.template.theme.json`
- Verify: `buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt`

**Problem:** Template placeholders use camelCase but ColorPalette map keys use snake_case, causing unreplaced placeholders in generated themes.

**Step 1: Read ColorPalette definitions**

Examine `WindowsTerminalColorScheme.kt:454-459` to see current placeholder names:
```kotlin
"wt_button_border" to buttonBorder,
"wt_button_border_focused" to buttonBorderFocused,
"wt_popup_border" to popupBorder,
```

**Step 2: Identify all mismatched placeholders in rounded template**

Run: `grep -n '\$wt_.*\$' buildSrc/templates/windows-terminal-rounded.template.theme.json`

Expected issues:
- Line 100: `$wt_buttonBorderFocused$` should be `$wt_button_border_focused$`
- Line 101: `$wt_buttonBorder$` should be `$wt_button_border$`
- Line 316: `$wt_popupBorder$` should be `$wt_popup_border$`

**Step 3: Fix placeholder names in rounded template**

In `buildSrc/templates/windows-terminal-rounded.template.theme.json`:

Change line 100-101:
```json
"focusedBorderColor": "$wt_button_border_focused$",
"borderColor": "$wt_button_border$",
```

Change line 316:
```json
"borderColor": "$wt_popup_border$",
```

**Step 4: Verify all other placeholders follow snake_case convention**

Run: `grep -E '\$wt_[a-zA-Z]+\$' buildSrc/templates/windows-terminal-rounded.template.theme.json | grep -v '_'`

Expected: No output (all placeholders should have underscores)

**Step 5: Apply same fixes to standard template if needed**

Run: `grep -n '\$wt_buttonBorder' buildSrc/templates/windows-terminal.template.theme.json`

If found, apply same naming fixes.

**Step 6: Regenerate themes to test**

Run: `./gradlew generateThemesFromWindowsTerminal`

Expected: Success with no placeholder warnings

**Step 7: Verify placeholders were replaced**

Run: `grep -n '\$wt_' src/main/resources/themes/wt-lovelace-abd97252_rounded.theme.json`

Expected: No output (all placeholders should be replaced)

**Step 8: Commit the fix**

```bash
git add buildSrc/templates/windows-terminal-rounded.template.theme.json buildSrc/templates/windows-terminal.template.theme.json
git commit -m "fix: correct placeholder naming from camelCase to snake_case

- Change $wt_buttonBorderFocused$ to $wt_button_border_focused$
- Change $wt_buttonBorder$ to $wt_button_border$
- Change $wt_popupBorder$ to $wt_popup_border$
- Ensures all placeholders match ColorPalette map keys"
```

---

## Task 2: Add parentTheme Support

**Files:**
- Modify: `buildSrc/templates/windows-terminal.template.theme.json:1-10`
- Modify: `buildSrc/templates/windows-terminal-rounded.template.theme.json:1-10`
- Read: `demo/Lovelace-Theme` (reference)

**Goal:** Enable generated themes to inherit from IntelliJ base themes like `ExperimentalDark`.

**Step 1: Read demo theme to understand parentTheme usage**

Run: `head -n 10 demo/Lovelace-Theme`

Expected to see:
```json
{
  "name": "Lovelace",
  "dark": true,
  "author": "...",
  "editorScheme": "...",
  "parentTheme": "ExperimentalDark",
```

**Step 2: Add parentTheme to standard template header**

In `buildSrc/templates/windows-terminal.template.theme.json`, after line 5, add:
```json
  "parentTheme": "ExperimentalDark",
```

Result should be:
```json
{
  "name": "$themeName$",
  "dark": true,
  "author": "Generated from Windows Terminal scheme",
  "editorScheme": "$editorSchemePath$",
  "parentTheme": "ExperimentalDark",
  "ui": {
```

**Step 3: Add parentTheme to rounded template header**

In `buildSrc/templates/windows-terminal-rounded.template.theme.json`, apply same change after line 5.

**Step 4: Regenerate themes**

Run: `./gradlew generateThemesFromWindowsTerminal`

Expected: Success

**Step 5: Verify parentTheme in generated files**

Run: `head -n 10 src/main/resources/themes/wt-lovelace-abd97252.theme.json | grep parentTheme`

Expected: `"parentTheme": "ExperimentalDark",`

**Step 6: Test in IntelliJ (manual step)**

Note for testing:
1. Build plugin with `./gradlew buildPlugin`
2. Install in IntelliJ
3. Switch to generated theme
4. Verify UI elements inherit proper base styling

**Step 7: Commit the change**

```bash
git add buildSrc/templates/windows-terminal.template.theme.json buildSrc/templates/windows-terminal-rounded.template.theme.json
git commit -m "feat: add parentTheme support to inherit from ExperimentalDark

- Enables generated themes to inherit base UI styling
- Provides proper foundation for rounded corners and modern UI features"
```

---

## Task 3: Add Islands UI Section

**Files:**
- Modify: `buildSrc/templates/windows-terminal.template.theme.json`
- Modify: `buildSrc/templates/windows-terminal-rounded.template.theme.json`
- Reference: `demo/Lovelace-Theme:482-489`
- Create placeholders in: `buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt`

**Goal:** Add Islands feature (floating tool windows with modern styling) to generated themes.

**Step 1: Read Islands section from demo**

Run: `sed -n '482,489p' demo/Lovelace-Theme`

Expected output:
```json
"Islands": 1,
"Island": {
    "arc": 20,
    "borderWidth": 5,
    "inactiveAlpha": 0.44,
    "inactiveAlphaInStatusBar": 0.2,
    "borderColor": "#1D1F28"
},
```

**Step 2: Define Island border color in ColorPalette**

In `buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt`, find the `toPaletteMap()` function around line 454.

Add after the popup_border entry:
```kotlin
"wt_island_border" to islandBorderColor,
```

Then add the calculation in the `init` block around line 150-200:
```kotlin
// Island styling (modern floating tool windows)
val islandBorderColor = ColorUtils.darken(background, 0.15)
```

**Step 3: Add Islands to standard template**

In `buildSrc/templates/windows-terminal.template.theme.json`, find the end of the `"ui"` object (before `"icons"`).

Add before the closing brace:
```json
    "Islands": 1,
    "Island": {
      "arc": 20,
      "borderWidth": 5,
      "inactiveAlpha": 0.44,
      "inactiveAlphaInStatusBar": 0.2,
      "borderColor": "$wt_island_border$"
    },
```

**Step 4: Add Islands to rounded template**

In `buildSrc/templates/windows-terminal-rounded.template.theme.json`, add same section with same arc value (Islands always use arc: 20).

**Step 5: Regenerate and verify**

Run: `./gradlew generateThemesFromWindowsTerminal && grep -A 7 '"Islands"' src/main/resources/themes/wt-lovelace-abd97252.theme.json`

Expected: Islands section with replaced border color

**Step 6: Commit**

```bash
git add buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt buildSrc/templates/*.template.theme.json
git commit -m "feat: add Islands support for modern floating tool windows

- Add islandBorderColor to ColorPalette
- Add Islands section to both templates
- Enables modern UI with floating tool windows"
```

---

## Task 4: Add MainToolbar and MainWindow Sections

**Files:**
- Modify: `buildSrc/templates/windows-terminal.template.theme.json`
- Modify: `buildSrc/templates/windows-terminal-rounded.template.theme.json`
- Reference: `demo/Lovelace-Theme:502-514`

**Goal:** Add MainToolbar and MainWindow styling for proper window chrome appearance.

**Step 1: Read MainToolbar and MainWindow from demo**

Run: `sed -n '502,514p' demo/Lovelace-Theme`

Expected sections for MainToolbar and MainWindow with transparency effects.

**Step 2: Add MainToolbar to standard template**

In `buildSrc/templates/windows-terminal.template.theme.json`, after Islands section:

```json
    "MainToolbar": {
      "background": "$wt_background$",
      "separatorColor": "$wt_ui_border_color$",
      "borderColor": "$wt_ui_border_color$"
    },
```

**Step 3: Add MainWindow to standard template**

```json
    "MainWindow": {
      "background": "$wt_background$"
    },
```

**Step 4: Add same sections to rounded template**

Apply same additions to `windows-terminal-rounded.template.theme.json`.

**Step 5: Regenerate and verify**

Run: `./gradlew generateThemesFromWindowsTerminal && grep -A 3 '"MainToolbar"' src/main/resources/themes/wt-lovelace-abd97252.theme.json`

Expected: MainToolbar and MainWindow sections present

**Step 6: Commit**

```bash
git add buildSrc/templates/*.template.theme.json
git commit -m "feat: add MainToolbar and MainWindow sections

- Ensures proper window chrome styling
- Matches demo theme structure"
```

---

## Task 5: Enhance EditorTabs with Underline Styling

**Files:**
- Modify: `buildSrc/templates/windows-terminal.template.theme.json`
- Modify: `buildSrc/templates/windows-terminal-rounded.template.theme.json`
- Modify: `buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt`
- Reference: `demo/Lovelace-Theme:494-500`

**Goal:** Add underlined tab styling properties for better editor tab appearance.

**Step 1: Read EditorTabs underline properties from demo**

Run: `sed -n '494,500p' demo/Lovelace-Theme`

Expected properties:
- underlinedBorderColor
- underlinedTabBackground
- inactiveUnderlinedTabBorderColor
- inactiveUnderlinedTabBackground

**Step 2: Add color calculations to WindowsTerminalColorScheme**

In `buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt`, in the `init` block, add:

```kotlin
// Editor tab underline styling
val underlinedTabBorderColor = selectionBackground
val underlinedTabBackground = ColorUtils.mix(background, selectionBackground, 0.3)
val inactiveUnderlinedTabBorderColor = ColorUtils.desaturate(selectionBackground, 0.5)
val inactiveUnderlinedTabBackground = ColorUtils.mix(background, surface, 0.5)
```

Then add to `toPaletteMap()`:
```kotlin
"wt_underlined_tab_border_color" to underlinedTabBorderColor,
"wt_underlined_tab_background" to underlinedTabBackground,
"wt_inactive_underlined_tab_border_color" to inactiveUnderlinedTabBorderColor,
"wt_inactive_underlined_tab_background" to inactiveUnderlinedTabBackground,
```

**Step 3: Add properties to EditorTabs in standard template**

In `buildSrc/templates/windows-terminal.template.theme.json`, find the EditorTabs section and add:

```json
      "underlinedBorderColor": "$wt_underlined_tab_border_color$",
      "underlinedTabBackground": "$wt_underlined_tab_background$",
      "inactiveUnderlinedTabBorderColor": "$wt_inactive_underlined_tab_border_color$",
      "inactiveUnderlinedTabBackground": "$wt_inactive_underlined_tab_background$",
```

**Step 4: Add same properties to rounded template**

Apply same additions to EditorTabs in `windows-terminal-rounded.template.theme.json`.

**Step 5: Regenerate and verify**

Run: `./gradlew generateThemesFromWindowsTerminal && grep 'underlined' src/main/resources/themes/wt-lovelace-abd97252.theme.json`

Expected: 4 underlined properties with color values

**Step 6: Commit**

```bash
git add buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt buildSrc/templates/*.template.theme.json
git commit -m "feat: add underlined tab styling to EditorTabs

- Add 4 underline-related color calculations
- Improves editor tab visual refinement"
```

---

## Task 6: Expand Icon Color Palette

**Files:**
- Modify: `buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt`
- Modify: `buildSrc/templates/windows-terminal.template.theme.json`
- Modify: `buildSrc/templates/windows-terminal-rounded.template.theme.json`
- Reference: `demo/Lovelace-Theme` (icon ColorPalette section)

**Goal:** Add comprehensive icon colors (Actions and Objects categories) instead of just checkbox icons.

**Step 1: Read demo icon ColorPalette**

Run: `grep -A 20 '"ColorPalette"' demo/Lovelace-Theme | grep '#'`

Expected: ~14 icon color definitions including Actions.* and Objects.*

**Step 2: Add icon color calculations to WindowsTerminalColorScheme**

In `buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt`, in the `init` block:

```kotlin
// Icon colors - Actions category
val actionsRed = ColorUtils.adjustColorForTheme(Color(0xDB5860), background)
val actionsYellow = ColorUtils.adjustColorForTheme(Color(0xEDA200), background)
val actionsGreen = ColorUtils.adjustColorForTheme(Color(0x59A869), background)
val actionsBlue = ColorUtils.adjustColorForTheme(selectionBackground, background)
val actionsGrey = ColorUtils.desaturate(foreground, 0.3)

// Icon colors - Objects category
val objectsGreen = actionsGreen
val objectsYellow = actionsYellow
val objectsBlue = actionsBlue
val objectsGrey = actionsGrey
val objectsRed = actionsRed
```

**Step 3: Add to toPaletteMap()**

```kotlin
// Icon colors
"Actions.Red" to actionsRed,
"Actions.Yellow" to actionsYellow,
"Actions.Green" to actionsGreen,
"Actions.Blue" to actionsBlue,
"Actions.Grey" to actionsGrey,
"Objects.Green" to objectsGreen,
"Objects.Yellow" to objectsYellow,
"Objects.Blue" to objectsBlue,
"Objects.Grey" to objectsGrey,
"Objects.Red" to objectsRed,
```

**Step 4: Add ColorPalette section to templates**

In both template files, add after the `"icons"` object and before closing:

```json
  "icons": {
    "ColorPalette": {
      "Actions.Red": "$Actions.Red$",
      "Actions.Yellow": "$Actions.Yellow$",
      "Actions.Green": "$Actions.Green$",
      "Actions.Blue": "$Actions.Blue$",
      "Actions.Grey": "$Actions.Grey$",
      "Objects.Green": "$Objects.Green$",
      "Objects.Yellow": "$Objects.Yellow$",
      "Objects.Blue": "$Objects.Blue$",
      "Objects.Grey": "$Objects.Grey$",
      "Objects.Red": "$Objects.Red$"
    }
  }
```

**Step 5: Regenerate and verify**

Run: `./gradlew generateThemesFromWindowsTerminal && grep -A 12 '"ColorPalette"' src/main/resources/themes/wt-lovelace-abd97252.theme.json`

Expected: 10 icon color entries with hex values

**Step 6: Commit**

```bash
git add buildSrc/src/main/kotlin/generators/WindowsTerminalColorScheme.kt buildSrc/templates/*.template.theme.json
git commit -m "feat: expand icon ColorPalette with Actions and Objects colors

- Add 10 icon color definitions (up from 9 checkbox-only)
- Icons will use theme-appropriate colors instead of defaults"
```

---

## Task 7: Build and Test in IntelliJ

**Files:**
- Build: Plugin JAR
- Test: Manual installation in IntelliJ IDEA

**Goal:** Verify all fixes work in actual IntelliJ instance, especially rounded corners.

**Step 1: Clean build**

Run: `./gradlew clean buildPlugin`

Expected: BUILD SUCCESSFUL

**Step 2: Locate plugin JAR**

Run: `ls -lh build/distributions/*.zip`

Expected: Plugin ZIP file present

**Step 3: Manual test in IntelliJ**

Manual steps:
1. Open IntelliJ IDEA
2. Go to Settings → Plugins → Gear icon → Install Plugin from Disk
3. Select the generated ZIP file
4. Restart IntelliJ
5. Go to Settings → Appearance & Behavior → Appearance
6. Select "wt-lovelace-abd97252" (standard variant)
7. Note appearance
8. Switch to "wt-lovelace-abd97252_rounded" (rounded variant)
9. Verify rounded corners are visible on:
   - Buttons (arc: 6)
   - ComboBoxes (arc: 4)
   - Popups (arc: 12)
   - Tabs (tabArc: 8)

**Step 4: Take screenshots for documentation**

Create: `docs/screenshots/before-fix.png` and `docs/screenshots/after-fix.png`

**Step 5: Document test results**

Create: `docs/testing/2025-11-22-visual-verification.md` with:
- IntelliJ version tested
- Which UI elements show proper rounding
- Any remaining issues
- Comparison with demo theme

**Step 6: Commit documentation**

```bash
git add docs/screenshots/ docs/testing/
git commit -m "docs: add visual verification test results

- Document IntelliJ version compatibility
- Confirm rounded corners rendering
- Add before/after screenshots"
```

---

## Task 8: Create Comprehensive Documentation

**Files:**
- Create: `docs/theme-generation.md`
- Create: `docs/placeholder-conventions.md`
- Create: `docs/troubleshooting.md`

**Goal:** Document theme generation system for future maintenance.

**Step 1: Create theme-generation.md**

Create `docs/theme-generation.md`:

```markdown
# Theme Generation System

## Overview

This plugin generates IntelliJ IDEA themes from Windows Terminal color schemes.

## Architecture

### Key Components

1. **WindowsTerminalColorScheme.kt** - Color palette calculation from WT schemes
2. **UIThemeGenerator.kt** - Template processing and file generation
3. **ThemeVariant.kt** - Standard/Rounded variant system
4. **Templates** - JSON templates with placeholder syntax

### Process Flow

1. Read WT color scheme from `windows-terminal-schemes/*.json`
2. Calculate derived colors (surface, borders, components)
3. Create two variants per scheme:
   - Standard (no arc)
   - Rounded (arc values 3-12)
4. Process templates with placeholder replacement
5. Write to `src/main/resources/themes/*.theme.json`

## Placeholder Naming Convention

**CRITICAL:** All placeholders must use snake_case.

✅ Correct: `$wt_button_border_focused$`
❌ Wrong: `$wt_buttonBorderFocused$`

## Adding New UI Sections

1. Add color calculations to `WindowsTerminalColorScheme.kt` init block
2. Add to `toPaletteMap()` with snake_case key
3. Add section to templates with placeholder references
4. Regenerate with `./gradlew generateThemesFromWindowsTerminal`

## Testing

Build and install: `./gradlew buildPlugin`
Test both standard and rounded variants in IntelliJ.

## Requirements

- Rounded corners require `parentTheme: "ExperimentalDark"`
- Minimum IntelliJ version: 2023.1+
```

**Step 2: Create placeholder-conventions.md**

Create `docs/placeholder-conventions.md` with detailed placeholder naming rules.

**Step 3: Create troubleshooting.md**

Create `docs/troubleshooting.md`:

```markdown
# Troubleshooting Theme Generation

## Placeholders Not Replaced

**Symptom:** Generated theme contains literal `$wt_something$` strings

**Cause:** Placeholder name mismatch

**Solution:**
1. Check `WindowsTerminalColorScheme.kt` toPaletteMap() for exact key name
2. Ensure template uses snake_case matching the map key
3. Verify no typos

## Rounded Corners Not Showing

**Symptom:** Rounded variant looks identical to standard

**Possible causes:**
1. Missing `parentTheme: "ExperimentalDark"`
2. IntelliJ version < 2023.1
3. Arc values not in generated JSON (check file)

**Solution:**
1. Verify parentTheme in generated theme.json
2. Update IntelliJ to 2023.1+
3. Regenerate themes if arc values missing

## Colors Look Wrong

**Symptom:** Colors don't match Windows Terminal

**Cause:** Algorithmic color derivation vs. hand-tuned

**Note:** This is expected. Generated themes use formulas for consistency.
For specific color overrides, modify WindowsTerminalColorScheme calculation.
```

**Step 4: Commit documentation**

```bash
git add docs/*.md
git commit -m "docs: add comprehensive theme generation documentation

- Document architecture and process flow
- Establish placeholder naming conventions
- Add troubleshooting guide for common issues"
```

---

## Task 9: Final Verification and Comparison

**Goal:** Comprehensive comparison between generated themes and Lovelace demo.

**Step 1: Generate final themes**

Run: `./gradlew clean generateThemesFromWindowsTerminal`

**Step 2: Compare file sizes**

Run:
```bash
wc -l demo/Lovelace-Theme
wc -l src/main/resources/themes/wt-lovelace-abd97252.theme.json
wc -l src/main/resources/themes/wt-lovelace-abd97252_rounded.theme.json
```

**Step 3: Compare structure**

Run: `diff -u <(jq -S 'keys' demo/Lovelace-Theme) <(jq -S 'keys' src/main/resources/themes/wt-lovelace-abd97252.theme.json)`

Expected: Minimal differences (only expected variations)

**Step 4: Verify no unreplaced placeholders**

Run: `grep '\$wt_' src/main/resources/themes/*.theme.json`

Expected: No output

**Step 5: Verify rounded arc values**

Run: `grep '"arc"' src/main/resources/themes/wt-lovelace-abd97252_rounded.theme.json`

Expected: Multiple arc properties with numeric values

**Step 6: Create comparison report**

Create `docs/comparison-report.md` documenting:
- Remaining intentional differences
- Achieved parity items
- Known limitations

**Step 7: Final commit**

```bash
git add docs/comparison-report.md
git commit -m "docs: add generated vs demo comparison report

- Document achieved feature parity
- List intentional differences
- Note remaining limitations"
```

---

## Expected Outcomes

✅ **Placeholder replacement works correctly** - All `$wt_*$` placeholders replaced with actual colors
✅ **Rounded themes have proper arc values** - Corners are actually rounded in IntelliJ
✅ **Generated themes inherit from ExperimentalDark** - Proper base UI styling
✅ **Islands support added** - Modern floating tool windows enabled
✅ **Complete EditorTabs styling** - Underlined tab properties present
✅ **Comprehensive icon colors** - 10+ icon colors instead of 9 checkbox-only
✅ **MainToolbar and MainWindow sections** - Proper window chrome styling
✅ **Full documentation** - Architecture, conventions, troubleshooting guides

## Remaining Limitations

- Colors are still algorithmically derived, not hand-tuned like demo
- Some demo-specific customizations may not be replicated
- Arc rendering depends on IntelliJ version (2023.1+ required)

## Execution Options

**Option 1: Subagent-Driven Development (this session)**
Use `superpowers:subagent-driven-development` to dispatch fresh subagent per task with code review between tasks.

**Option 2: Parallel Session (separate worktree)**
Open new session in dedicated worktree, use `superpowers:executing-plans` for batch execution with review checkpoints.
