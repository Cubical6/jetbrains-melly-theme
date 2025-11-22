# Theme Generation System

## Overview

The theme generation system converts Windows Terminal color schemes into complete IntelliJ IDEA theme files. This process involves parsing color schemes, deriving additional colors, and replacing placeholders in JSON templates to produce fully-functional `.theme.json` files.

## Architecture

### Key Components

#### 1. WindowsTerminalColorScheme.kt
**Location:** `/buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

This data class represents a Windows Terminal color scheme with:
- **Required properties**: name, background, foreground, 16 ANSI colors (black through brightWhite)
- **Optional properties**: cursorColor, selectionBackground
- **Validation**: Ensures all colors are valid hex format (#RRGGBB)
- **Color palette generation**: Derives 50+ additional colors from base scheme

The `toColorPaletteMap()` extension function converts a scheme into a complete map of placeholder keys to color values, including:
- Base colors (e.g., `wt_background`, `wt_foreground`)
- ANSI colors (e.g., `wt_red`, `wt_brightBlue`)
- Derived colors (e.g., `wt_surface`, `wt_selection_inactive`)

#### 2. UIThemeGenerator.kt
**Location:** `/buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt`

The main theme generation engine that:
- Reads template files from `buildSrc/templates/`
- Replaces all placeholders with actual color values
- Detects theme type (dark/light) based on background luminance
- Validates generated JSON is well-formed
- Writes output files to the specified directory

Key methods:
- `generateVariant()`: Generates a single theme variant (Standard or Rounded)
- `generate()`: Generates all variants for a color scheme
- `detectDarkTheme()`: Determines if theme is dark (luminance < 100)

#### 3. TemplateProcessor.kt
**Location:** `/buildSrc/src/main/kotlin/themes/TemplateProcessor.kt`

Handles variable replacement in template files:
- Finds all `$variable_name$` placeholders
- Replaces them with values from the variable map
- Validates all required variables are replaced
- Provides detailed error reporting for missing placeholders

#### 4. Template Files
**Location:** `/buildSrc/templates/`

JSON templates with placeholders:
- `windows-terminal.template.theme.json` - Standard variant (sharp corners)
- `windows-terminal-rounded.template.theme.json` - Rounded variant (rounded corners)

Templates contain:
- `$wt_*$` placeholders for colors
- `$arc_*$` placeholders for border radius values
- Metadata placeholders (`$wt_name$`, `$wt_dark$`, etc.)

## Process Flow

### From Windows Terminal Scheme to Generated Theme

```
1. Parse Color Scheme
   └─> WindowsTerminalColorScheme object created
       └─> Validate all colors are valid hex format

2. Generate Color Palette
   └─> toColorPaletteMap() called
       ├─> Base colors added (background, foreground, ANSI colors)
       └─> 50+ derived colors calculated (surfaces, selections, focus, etc.)

3. Select Template
   └─> Choose based on variant (Standard or Rounded)

4. Replace Placeholders
   └─> TemplateProcessor.processTemplate()
       ├─> Replace $wt_*$ color placeholders
       ├─> Replace $arc_*$ border radius placeholders
       └─> Replace metadata placeholders ($wt_name$, $wt_dark$, etc.)

5. Validate Output
   └─> Ensure generated JSON is well-formed

6. Write Theme File
   └─> Save to output directory as <scheme_name>_<variant>.theme.json
```

## Placeholder Naming Convention

**CRITICAL:** All placeholder names MUST use `snake_case`.

### Color Placeholders

Format: `$wt_<color_name>$`

Examples:
- `$wt_background$` - Terminal background color
- `$wt_foreground$` - Terminal foreground color
- `$wt_red$` - ANSI red color
- `$wt_brightBlue$` - ANSI bright blue color (uses camelCase for compound ANSI names)
- `$wt_surface_light$` - Derived surface color (lighter variant)
- `$wt_selection_inactive$` - Derived inactive selection color

### Arc Placeholders

Format: `$arc_<component>$`

Examples:
- `$arc_component$` - General component border radius
- `$arc_button$` - Button border radius
- `$arc_tabbedPane$` - Tab border radius

### Metadata Placeholders

- `$wt_name$` - Theme display name
- `$wt_scheme_name$` - Sanitized scheme name for file references
- `$wt_dark$` - Boolean indicating if theme is dark
- `$wt_author$` - Theme author string

### Complete Color Palette Reference

See `ColorPalette.toMap()` in WindowsTerminalColorScheme.kt (lines 486-577) for the complete list of available placeholders.

## How to Add New UI Sections

### 1. Add Placeholder to Template

Edit the template file (`buildSrc/templates/windows-terminal.template.theme.json`):

```json
"ui": {
  "MyNewComponent.background": "$wt_my_new_color$",
  "MyNewComponent.foreground": "$wt_foreground$"
}
```

### 2. Add Derived Color (if needed)

If the color doesn't exist in the base palette, add it to `WindowsTerminalColorScheme.toColorPalette()`:

```kotlin
// In the toColorPalette() method
val myNewColor = ColorUtils.blend(background, blue, 0.30)

// Add to ColorPalette data class constructor
ColorPalette(
    // ... existing colors
    myNewColor = myNewColor
)
```

### 3. Add to ColorPalette.toMap()

```kotlin
fun toMap(): Map<String, String> {
    return mapOf(
        // ... existing mappings
        "wt_my_new_color" to myNewColor
    )
}
```

### 4. Test the Change

Run theme generation and verify the placeholder is replaced:
```bash
./gradlew generateThemesFromWindowsTerminal
```

## Testing Instructions

### 1. Unit Tests

Run template processor tests:
```bash
./gradlew test --tests "themes.TemplateProcessorTest"
```

Run UI theme generator tests:
```bash
./gradlew test --tests "generators.UIThemeGeneratorTest"
```

### 2. Integration Tests

Generate themes from a sample scheme:
```bash
./gradlew generateThemesFromWindowsTerminal
```

Check the output in `themes/ui/` directory.

### 3. Manual Verification

1. Open a generated `.theme.json` file
2. Search for any remaining `$` symbols
3. If found, a placeholder was not replaced (see Troubleshooting)

### 4. Visual Testing

1. Build the plugin: `./gradlew buildPlugin`
2. Install in IntelliJ IDEA
3. Go to Settings > Appearance & Behavior > Appearance
4. Select one of the generated themes
5. Verify colors appear correctly and rounded corners (if applicable) are visible

## Requirements

### parentTheme

All generated themes require a `parentTheme` setting in the template:

```json
"parentTheme": "ExperimentalDark"
```

This provides:
- Base UI styling
- Modern IntelliJ features (Islands, etc.)
- Fallback values for unspecified properties

**Available parent themes:**
- `ExperimentalDark` - For dark themes (background luminance < 100)
- `ExperimentalLight` - For light themes (background luminance ≥ 100)

### IntelliJ Version

Minimum supported version: **2024.1**

Features used:
- Modern UI architecture (New UI)
- Island-style tool windows
- Underlined tab styling
- Arc (border radius) properties

## File Locations

### Source Files
- Color scheme parsing: `/buildSrc/src/main/kotlin/colorschemes/`
- Theme generation: `/buildSrc/src/main/kotlin/generators/`
- Template processing: `/buildSrc/src/main/kotlin/themes/`
- Utilities: `/buildSrc/src/main/kotlin/utils/`
- Theme variants: `/buildSrc/src/main/kotlin/variants/`

### Templates
- `/buildSrc/templates/windows-terminal.template.theme.json`
- `/buildSrc/templates/windows-terminal-rounded.template.theme.json`

### Output
- Generated UI themes: `/themes/ui/`
- Generated XML color schemes: `/themes/schemes/`

### Tests
- Unit tests: `/buildSrc/src/test/kotlin/`

## Related Documentation

- [Placeholder Conventions](placeholder-conventions.md) - Detailed placeholder naming rules
- [Troubleshooting](troubleshooting.md) - Common issues and solutions
