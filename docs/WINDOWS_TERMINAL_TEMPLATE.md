# Windows Terminal Template Documentation

## Overview

This document describes the Windows Terminal theme template located at `buildSrc/templates/windows-terminal.template.xml`. This template is designed to generate JetBrains IDE color schemes that are compatible with Windows Terminal color schemes, using a consistent placeholder naming convention.

## Template Location

```
buildSrc/templates/windows-terminal.template.xml
```

## Placeholder Naming Convention

All placeholders in the Windows Terminal template use the `$wt_*$` naming convention to clearly distinguish them from other template systems and to align with Windows Terminal's ANSI color scheme structure.

## Placeholder Reference

### Template Variables

| Placeholder | Description | Usage |
|------------|-------------|-------|
| `$SCHEME_NAME$` | The name of the color scheme | Used in the `<scheme name="...">` attribute |

### ANSI Color Placeholders (Normal)

These placeholders correspond to the 8 standard ANSI colors (indices 0-7):

| Placeholder | ANSI Color | Description | Usage Count |
|------------|-----------|-------------|-------------|
| `$wt_black$` | Black (0) | Standard black color, also used for dark UI elements | 20 |
| `$wt_red$` | Red (1) | Standard red color, used for errors and deletions | 37 |
| `$wt_green$` | Green (2) | Standard green color, used for success and additions | 39 |
| `$wt_yellow$` | Yellow (3) | Standard yellow color, used for warnings and highlights | 54 |
| `$wt_blue$` | Blue (4) | Standard blue color, used for information and functions | 55 |
| `$wt_magenta$` | Magenta (5) | Standard magenta color, used for keywords and special syntax | 50 |
| `$wt_cyan$` | Cyan (6) | Standard cyan color, used for constants and identifiers | 45 |
| `$wt_white$` | White (7) | Standard white color, used for normal text | 21 |

### ANSI Color Placeholders (Bright)

These placeholders correspond to the 8 bright ANSI colors (indices 8-15):

| Placeholder | ANSI Color | Description | Usage Count |
|------------|-----------|-------------|-------------|
| `$wt_bright_black$` | Bright Black (8) | Bright/bold black, typically rendered as gray | 1 |
| `$wt_bright_red$` | Bright Red (9) | Bright/bold red, used for critical errors | 14 |
| `$wt_bright_green$` | Bright Green (10) | Bright/bold green | 1 |
| `$wt_bright_yellow$` | Bright Yellow (11) | Bright/bold yellow, often used for orangish tones | 64 |
| `$wt_bright_blue$` | Bright Blue (12) | Bright/bold blue | 1 |
| `$wt_bright_magenta$` | Bright Magenta (13) | Bright/bold magenta | 1 |
| `$wt_bright_cyan$` | Bright Cyan (14) | Bright/bold cyan | 1 |
| `$wt_bright_white$` | Bright White (15) | Bright/bold white, used for emphasized text | 1 |

### Special Color Placeholders

These placeholders are used for Windows Terminal-specific features:

| Placeholder | Description | Usage Count |
|------------|-------------|-------------|
| `$wt_foreground$` | Default text foreground color | 1 |
| `$wt_background$` | Default background color | 1 |

**Note:** Windows Terminal also supports cursor color and selection background, but these are not directly mapped in the JetBrains template as the IDE has its own cursor and selection handling.

## Console Color Mappings

The template includes comprehensive console color mappings that align with Windows Terminal's ANSI color scheme:

### Standard Console Colors

| JetBrains Console Attribute | Placeholder |
|----------------------------|-------------|
| `CONSOLE_BLACK_OUTPUT` | `$wt_black$` |
| `CONSOLE_RED_OUTPUT` | `$wt_red$` |
| `CONSOLE_GREEN_OUTPUT` | `$wt_green$` |
| `CONSOLE_YELLOW_OUTPUT` | `$wt_yellow$` |
| `CONSOLE_BLUE_OUTPUT` | `$wt_blue$` |
| `CONSOLE_MAGENTA_OUTPUT` | `$wt_magenta$` |
| `CONSOLE_CYAN_OUTPUT` | `$wt_cyan$` |
| `CONSOLE_WHITE_OUTPUT` | `$wt_white$` |
| `CONSOLE_DARKGRAY_OUTPUT` | `$wt_bright_black$` |

### Bright Console Colors

| JetBrains Console Attribute | Placeholder |
|----------------------------|-------------|
| `CONSOLE_RED_BRIGHT_OUTPUT` | `$wt_bright_red$` |
| `CONSOLE_GREEN_BRIGHT_OUTPUT` | `$wt_bright_green$` |
| `CONSOLE_YELLOW_BRIGHT_OUTPUT` | `$wt_bright_yellow$` |
| `CONSOLE_BLUE_BRIGHT_OUTPUT` | `$wt_bright_blue$` |
| `CONSOLE_MAGENTA_BRIGHT_OUTPUT` | `$wt_bright_magenta$` |
| `CONSOLE_CYAN_BRIGHT_OUTPUT` | `$wt_bright_cyan$` |
| `CONSOLE_GRAY_OUTPUT` | `$wt_bright_white$` |

### Special Console Colors

| JetBrains Console Attribute | Placeholder | Description |
|----------------------------|-------------|-------------|
| `CONSOLE_NORMAL_OUTPUT` | `$wt_white$` | Normal console output |
| `CONSOLE_ERROR_OUTPUT` | `$wt_red$` | Error messages |
| `CONSOLE_SYSTEM_OUTPUT` | `$wt_foreground$` | System messages |
| `CONSOLE_USER_INPUT` | `$wt_green$` | User input echo |
| `CONSOLE_RANGE_TO_EXECUTE` | `$wt_black$` (effect color) | Code execution highlight |

## Placeholder Mapping Strategy

The Windows Terminal template uses the following mapping strategy from the original One Dark template:

| Original Placeholder | Windows Terminal Placeholder | Rationale |
|---------------------|----------------------------|-----------|
| `$green$` | `$wt_green$` | Direct ANSI green mapping |
| `$coral$` | `$wt_red$` | Coral is a reddish color, maps to ANSI red |
| `$chalky$` | `$wt_yellow$` | Chalky yellow maps to ANSI yellow |
| `$malibu$` | `$wt_blue$` | Malibu blue maps to ANSI blue |
| `$fountainBlue$` | `$wt_cyan$` | Fountain blue (cyan-like) maps to ANSI cyan |
| `$purple$` | `$wt_magenta$` | Purple maps to ANSI magenta |
| `$whiskey$` | `$wt_bright_yellow$` | Whiskey (orange/amber) uses bright yellow |
| `$error$` | `$wt_bright_red$` | Error color uses bright red for emphasis |
| `$dark$` | `$wt_black$` | Dark colors map to ANSI black |
| `$lightWhite$` | `$wt_white$` | Light white maps to ANSI white |

## Usage

### For Theme Developers

When creating a Windows Terminal-compatible theme:

1. **Define color values**: Create a color definition file that maps each `$wt_*$` placeholder to an actual hex color value
2. **Process template**: Use the build system to replace all placeholders with the actual values
3. **Validate output**: Ensure the generated `.icls` file is valid XML and renders correctly in JetBrains IDEs

### For Build System Integration

The template is designed to work with a build system that:

1. Reads Windows Terminal color scheme files (JSON format)
2. Extracts the ANSI color values
3. Maps them to the corresponding `$wt_*$` placeholders
4. Performs find-and-replace to generate the final `.icls` file

## Validation

The template has been validated to ensure:

- **Valid XML**: The template passes `xmllint` validation
- **No orphaned placeholders**: All placeholders follow the `$wt_*$` convention
- **Complete ANSI coverage**: All 16 ANSI colors plus special colors are represented
- **Consistent usage**: Placeholders are used consistently throughout the template

## Additional Notes

### Color Brightness

Note that Windows Terminal's "bright" colors (indices 8-15) are typically rendered with the bold font attribute in the template. This matches the traditional terminal behavior where bright colors were achieved through bold text.

### Background Colors

The template uses `$wt_background$` for error stripe and other background elements where appropriate. However, most background colors in the JetBrains IDE UI remain as hardcoded values since they represent IDE chrome rather than terminal content.

### Non-Console Colors

Many UI elements in the template (such as gutter colors, caret colors, bookmark colors) retain their hardcoded hex values. These are IDE-specific UI elements that don't have direct Windows Terminal equivalents.

## See Also

- [Windows Terminal Color Schemes Documentation](https://learn.microsoft.com/en-us/windows/terminal/customize-settings/color-schemes)
- [ANSI Escape Codes](https://en.wikipedia.org/wiki/ANSI_escape_code#Colors)
- [JetBrains Editor Color Scheme Documentation](https://www.jetbrains.com/help/idea/configuring-colors-and-fonts.html)

## Version Information

- **Template Version**: 142 (matching Darcula parent scheme)
- **Parent Scheme**: Darcula
- **Meta Property**: `windowsTerminalScheme: runtimeGenerated`
