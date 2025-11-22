# Placeholder Naming Conventions

## Overview

This document defines the strict naming conventions for placeholders used in theme templates. Following these conventions is **critical** to ensure placeholders are correctly replaced during theme generation.

## The Golden Rule

**ALL placeholder names MUST use `snake_case`.**

The template processor uses exact string matching. If a placeholder name doesn't match the key in the color palette map, it will not be replaced.

## Placeholder Format

All placeholders follow this format:

```
$<prefix>_<name>$
```

Components:
- `$` - Opening delimiter
- `<prefix>_<name>` - Variable name in snake_case
- `$` - Closing delimiter

## Naming Rules

### 1. Use snake_case for Multi-Word Names

**CORRECT:**
```
$wt_surface_light$
$wt_selection_inactive$
$wt_muted_foreground$
$wt_button_border_focused$
```

**INCORRECT:**
```
$wt_surfaceLight$      // camelCase - WRONG!
$wt_SelectionInactive$ // PascalCase - WRONG!
$wt_Muted-Foreground$  // kebab-case - WRONG!
$wt surface light$     // spaces - WRONG!
```

### 2. Exception: ANSI Color Names

ANSI colors use the exact Windows Terminal naming:

**CORRECT:**
```
$wt_brightBlue$    // Matches Windows Terminal "brightBlue"
$wt_brightRed$     // Matches Windows Terminal "brightRed"
$wt_brightWhite$   // Matches Windows Terminal "brightWhite"
```

This is because the WindowsTerminalColorScheme data class uses camelCase property names for ANSI colors (e.g., `brightBlue`, `brightRed`) to match the Windows Terminal standard.

### 3. Prefix Conventions

#### Color Placeholders
All color placeholders use the `wt_` prefix:

```
$wt_background$
$wt_foreground$
$wt_red$
$wt_surface$
```

#### Arc (Border Radius) Placeholders
All arc placeholders use the `arc_` prefix:

```
$arc_component$
$arc_button$
$arc_tabbedPane$
```

#### Special Icon Color Placeholders
Icon colors use dot notation (no prefix):

```
$Actions.Red$
$Actions.Blue$
$Objects.Green$
```

These are direct IntelliJ UI keys and preserve their original format.

## Complete Placeholder Reference

### Base Colors (from WindowsTerminalColorScheme)

```
$wt_background$           // Terminal background
$wt_foreground$           // Terminal foreground
$wt_cursorColor$          // Cursor color
$wt_selectionBackground$  // Selection background
```

### ANSI Colors (Normal)

```
$wt_black$
$wt_red$
$wt_green$
$wt_yellow$
$wt_blue$
$wt_purple$
$wt_cyan$
$wt_white$
```

### ANSI Colors (Bright) - Note camelCase

```
$wt_brightBlack$
$wt_brightRed$
$wt_brightGreen$
$wt_brightYellow$
$wt_brightBlue$
$wt_brightPurple$
$wt_brightCyan$
$wt_brightWhite$
```

### Surface Variations

```
$wt_surface$          // Lighten background 5%
$wt_surface_light$    // Lighten background 10%
$wt_surface_lighter$  // Lighten background 15%
$wt_surface_dark$     // Darken background 5%
$wt_surface_darker$   // Darken background 10%
$wt_surface_darkest$  // Darken background 15%
$wt_surface_subtle$   // Lighten background 3%
```

### Selection Colors

```
$wt_selection_inactive$  // Dimmed selection (40%)
$wt_selection_light$     // Lighter selection (20%)
$wt_selection_border$    // Selection border color
```

### Focus/Accent Colors

```
$wt_focus_color$       // Focus highlight color
$wt_focus_border$      // Focus border color
$wt_accent_primary$    // Primary accent (brightBlue)
$wt_accent_secondary$  // Secondary accent (brightPurple)
$wt_accent_tertiary$   // Tertiary accent (brightCyan)
```

### UI Component Colors

```
$wt_uiBorderColor$           // General UI borders
$wt_uiComponentBackground$   // Component backgrounds
$wt_button_border$           // Button border
$wt_button_border_focused$   // Focused button border
$wt_popup_background$        // Popup window background
$wt_popup_border$            // Popup window border
$wt_header_background$       // Header background
$wt_hover_background$        // Hover state background
```

### Text/Foreground Variations

```
$wt_muted_foreground$   // Dimmed foreground (60% blend)
$wt_line_numbers$       // Line number color (30% blend)
$wt_disabled_text$      // Disabled text color
$wt_guide_color$        // Indent guide color (15% blend)
$wt_divider_color$      // Divider line color (25% blend)
```

### Syntax-Specific Colors

```
$wt_instance_field$     // Instance field color (purple+red blend)
$wt_todo_color$         // TODO comment color (cyan+green blend)
$wt_deprecated_color$   // Deprecated item color
$wt_string_escape$      // String escape sequence color
$wt_number_alt$         // Alternative number color
$wt_constant_color$     // Constant value color
```

### Status/Semantic Colors

```
$wt_error_background$    // Error highlight background
$wt_warning_background$  // Warning highlight background
$wt_info_background$     // Info highlight background
$wt_passed_color$        // Test passed color
$wt_failed_color$        // Test failed color
```

### Progress Indicators

```
$wt_progress_start$      // Progress bar gradient start
$wt_progress_mid$        // Progress bar gradient middle
$wt_progress_end$        // Progress bar gradient end
$wt_memory_indicator$    // Memory usage indicator
```

### Additional UI Elements

```
$wt_breadcrumb_current$   // Current breadcrumb item
$wt_breadcrumb_hover$     // Breadcrumb hover state
$wt_separator_color$      // Separator line color
$wt_counter_background$   // Counter badge background
$wt_tooltip_background$   // Tooltip background
$wt_link_hover$           // Link hover state
$wt_icon_color$           // Default icon color
$wt_island_border$        // Island border color
```

### Tab Underline Styling

```
$wt_underlined_tab_border_color$            // Active tab underline
$wt_underlined_tab_background$              // Active tab background
$wt_inactive_underlined_tab_border_color$   // Inactive tab underline
$wt_inactive_underlined_tab_background$     // Inactive tab background
```

### Arc (Border Radius) Values

```
$arc_component$      // General component arc (0 or 8)
$arc_button$         // Button arc (0 or 6)
$arc_tabbedPane$     // Tab arc (0 or 8)
$arc_progressBar$    // Progress bar arc (0 or 4)
$arc_comboBox$       // Combo box arc (0 or 4)
$arc_textField$      // Text field arc (0 or 4)
$arc_checkBox$       // Checkbox arc (0 or 3)
$arc_tree$           // Tree row arc (0 or 4)
$arc_table$          // Table cell arc (0)
$arc_popup$          // Popup window arc (0 or 12)
```

### Metadata Placeholders

```
$wt_name$          // Theme display name
$wt_scheme_name$   // Sanitized scheme name (for file references)
$wt_dark$          // "true" or "false" (theme type)
$wt_author$        // Author string
```

### Icon Colors (Special Format)

```
$Actions.Red$      // Action icon red
$Actions.Yellow$   // Action icon yellow
$Actions.Green$    // Action icon green
$Actions.Blue$     // Action icon blue
$Actions.Grey$     // Action icon grey

$Objects.Green$    // Object icon green
$Objects.Yellow$   // Object icon yellow
$Objects.Blue$     // Object icon blue
$Objects.Grey$     // Object icon grey
$Objects.Red$      // Object icon red
```

## How to Add New Placeholders

### Step 1: Add to ColorPalette Data Class

Edit `WindowsTerminalColorScheme.kt`:

```kotlin
data class ColorPalette(
    // ... existing properties
    val myNewColor: String  // Add new property in snake_case
)
```

### Step 2: Calculate in toColorPalette()

```kotlin
fun toColorPalette(): ColorPalette {
    // ... existing calculations
    val myNewColor = ColorUtils.blend(background, blue, 0.30)

    return ColorPalette(
        // ... existing parameters
        myNewColor = myNewColor
    )
}
```

### Step 3: Add to ColorPalette.toMap()

```kotlin
fun toMap(): Map<String, String> {
    return mapOf(
        // ... existing mappings
        "wt_my_new_color" to myNewColor  // Use snake_case for key
    )
}
```

### Step 4: Use in Template

```json
{
  "ui": {
    "Component.background": "$wt_my_new_color$"
  }
}
```

## Common Mistakes to Avoid

### Mistake 1: Using camelCase for Derived Colors

**WRONG:**
```
$wt_surfaceLight$
$wt_selectionInactive$
```

**RIGHT:**
```
$wt_surface_light$
$wt_selection_inactive$
```

### Mistake 2: Inconsistent Naming

**WRONG:**
```kotlin
// In ColorPalette
val surfaceLight: String

// In toMap()
"wt_surface_light" to surfaceLight  // ✓ Correct key
"wt_surfaceLight" to surfaceLight   // ✗ Wrong - won't match property name
```

**RIGHT:**
```kotlin
// Keep property names and map keys consistent (both snake_case)
val surfaceLight: String
"wt_surface_light" to surfaceLight
```

### Mistake 3: Missing the wt_ Prefix

**WRONG:**
```
$surface_light$    // Missing prefix
$background$       // Missing prefix
```

**RIGHT:**
```
$wt_surface_light$
$wt_background$
```

### Mistake 4: Typos in Placeholder Names

**WRONG:**
```
$wt_backgrund$        // Typo: "backgrund"
$wt_selction_border$  // Typo: "selction"
```

**RIGHT:**
```
$wt_background$
$wt_selection_border$
```

The template processor does **exact string matching**. A single typo means the placeholder won't be replaced.

## Verification

To verify placeholder naming:

1. **Check ColorPalette.toMap()**: All keys should use snake_case
2. **Check template files**: All placeholders should match map keys exactly
3. **Run tests**: Template processor tests will catch mismatches
4. **Search for `$` in generated files**: Any remaining `$` indicates unreplaced placeholder

## Reference Implementation

See the complete implementation in:
- `/buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt` (lines 486-577)
- `/buildSrc/templates/windows-terminal.template.theme.json`

## Related Documentation

- [Theme Generation](theme-generation.md) - Overview of the theme generation system
- [Troubleshooting](troubleshooting.md) - Common issues with placeholders
