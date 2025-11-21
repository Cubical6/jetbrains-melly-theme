# TemplateProcessor Documentation

## Overview

The `TemplateProcessor` is a powerful and flexible system for generating IntelliJ IDEA themes from Windows Terminal color schemes. It handles variable replacement in template files, converting placeholders like `$wt_background$` into actual color values.

## Key Features

- **Variable Replacement**: Replaces `$variable_name$` placeholders with actual values
- **Validation**: Ensures all required variables are provided and in correct format
- **Error Handling**: Provides detailed error messages and suggestions for missing variables
- **Flexible Processing**: Supports both strict and non-strict modes
- **Color Validation**: Validates hex color values
- **Multi-Format Support**: Works with XML and JSON templates

## Architecture

### Core Components

1. **TemplateProcessor** (`themes/TemplateProcessor.kt`)
   - Main processing engine
   - Variable replacement and validation logic
   - Utility functions for color normalization and variable filtering

2. **WindowsTerminalColorScheme** (`colorschemes/WindowsTerminalColorScheme.kt`)
   - Data model for Windows Terminal color schemes
   - `toColorPalette()` method converts scheme to variable map
   - Validation for color values

3. **Template Files** (`buildSrc/templates/`)
   - XML/JSON templates with variable placeholders
   - Example: `windows-terminal-sample.template.xml`

## Variable Naming Convention

### Windows Terminal Variables

All Windows Terminal color variables use the `wt_` prefix:

- **Core Colors**: `wt_background`, `wt_foreground`, `wt_cursorColor`, `wt_selectionBackground`
- **ANSI Colors (0-7)**: `wt_black`, `wt_red`, `wt_green`, `wt_yellow`, `wt_blue`, `wt_purple`, `wt_cyan`, `wt_white`
- **Bright Colors (8-15)**: `wt_brightBlack`, `wt_brightRed`, `wt_brightGreen`, `wt_brightYellow`, `wt_brightBlue`, `wt_brightPurple`, `wt_brightCyan`, `wt_brightWhite`

### Legacy Variables

Legacy One Dark theme variables (for backward compatibility):
- `green`, `coral`, `chalky`, `dark`, `error`, `fountainBlue`, `malibu`, `purple`, `whiskey`, `lightWhite`

## Usage Examples

### Basic Usage

```kotlin
import themes.TemplateProcessor
import colorschemes.WindowsTerminalColorScheme

// Create processor
val processor = TemplateProcessor()

// Create Windows Terminal scheme
val scheme = WindowsTerminalColorScheme(
    name = "One Dark",
    background = "#282c34",
    foreground = "#abb2bf",
    // ... other colors
)

// Convert to variable map
val variables = scheme.toColorPalette()

// Load template
val template = Files.readString(Paths.get("template.xml"))

// Process
val result = processor.processTemplate(template, variables, strict = true)

// Write output
Files.writeString(Paths.get("output.xml"), result.content)
```

### Validation Before Processing

```kotlin
val processor = TemplateProcessor()
val template = Files.readString(templatePath)
val variables = scheme.toColorPalette()

// Find required variables
val requiredVars = processor.findVariables(template)
println("Template requires: $requiredVars")

// Check for missing variables
val missingVars = processor.validateRequiredVariables(requiredVars, variables)
if (missingVars.isNotEmpty()) {
    println("Missing variables: $missingVars")
}

// Validate color values
val colorErrors = processor.validateColorValues(variables)
if (colorErrors.isNotEmpty()) {
    println("Color validation errors: $colorErrors")
}
```

### Non-Strict Mode (with Warnings)

```kotlin
val result = processor.processTemplate(template, variables, strict = false)

if (!result.isSuccess) {
    println("Warning: Some variables not replaced")
    println(result.getSummary())
    result.unreplacedVariables.forEach { varName ->
        println("  Unreplaced: $varName")
    }
}

// Still write output (with some variables unreplaced)
Files.writeString(outputPath, result.content)
```

### Merging Variables

```kotlin
// Default IDE colors
val defaults = mapOf(
    "ide_error_color" to "#f44747",
    "ide_warning_color" to "#cca700"
)

// Windows Terminal colors
val wtColors = scheme.toColorPalette()

// Custom overrides
val overrides = mapOf(
    "wt_red" to "#ff0000"  // Override red color
)

// Merge all (later maps override earlier ones)
val allVariables = processor.mergeVariables(defaults, wtColors, overrides)

val result = processor.processTemplate(template, allVariables)
```

### Color Normalization

```kotlin
// Some colors might be missing # prefix
val unnormalizedVars = mapOf(
    "wt_background" to "282c34",     // Missing #
    "wt_foreground" to "#abb2bf"     // Has #
)

// Normalize before processing
val normalized = processor.normalizeColorValues(unnormalizedVars)
// Result: "wt_background" -> "#282c34", "wt_foreground" -> "#abb2bf"

val result = processor.processTemplate(template, normalized)
```

### Generate Analysis Report

```kotlin
val report = processor.generateVariableReport(template, variables)
println(report)

// Output:
// Template Variable Analysis
// ==================================================
// Total variables in template: 20
// Variables with values: 18
// Missing variables: 2
//
// Provided Variables:
//   $wt_background$ = #282c34
//   $wt_foreground$ = #abb2bf
//   ...
//
// Missing Variables:
//   $wt_custom_color$
//     Similar available: wt_cursorColor
```

### Batch Processing

```kotlin
val schemes = listOf(oneDark, dracula, monokai)
val template = Files.readString(templatePath)

schemes.forEach { scheme ->
    val variables = scheme.toColorPalette() + mapOf("name" to scheme.name)
    val result = processor.processTemplate(template, variables)

    val outputFile = outputDir.resolve("${scheme.name.toLowerCase()}.xml")
    Files.writeString(outputFile, result.content)
}
```

## Template Format

### XML Template Example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<scheme name="$name$" parent_scheme="Darcula" version="142">
  <colors>
    <option name="CONSOLE_BACKGROUND" value="$wt_background$"/>
    <option name="CONSOLE_FOREGROUND" value="$wt_foreground$"/>
    <option name="CONSOLE_RED" value="$wt_red$"/>
    <option name="CONSOLE_GREEN" value="$wt_green$"/>
    <option name="CONSOLE_BRIGHT_GREEN" value="$wt_brightGreen$"/>
  </colors>
</scheme>
```

### JSON Template Example

```json
{
  "name": "$name$",
  "dark": true,
  "colors": {
    "background": "$wt_background$",
    "foreground": "$wt_foreground$"
  }
}
```

## API Reference

### TemplateProcessor Class

#### Main Methods

##### `processTemplate(template: String, variables: Map<String, String>, strict: Boolean = false): ProcessingResult`

Processes a template by replacing all variable placeholders.

- **Parameters**:
  - `template`: The template content containing `$variable$` placeholders
  - `variables`: Map of variable names to replacement values
  - `strict`: If true, throws exception on unreplaced variables
- **Returns**: `ProcessingResult` with processed content and metadata
- **Throws**: `IllegalArgumentException` if strict=true and variables are missing

##### `replaceVariables(content: String, variables: Map<String, String>): String`

Simple variable replacement without tracking or validation.

- **Parameters**:
  - `content`: Content with `$variable$` placeholders
  - `variables`: Map of variable names to replacement values
- **Returns**: Content with variables replaced

##### `validateAllVariablesReplaced(content: String): Boolean`

Checks if any unreplaced variables remain in the content.

- **Parameters**:
  - `content`: The content to validate
- **Returns**: `true` if no placeholders remain, `false` otherwise

##### `findVariables(content: String): List<String>`

Finds all variable names in the content.

- **Parameters**:
  - `content`: The content to scan
- **Returns**: List of unique variable names (without delimiters)

#### Utility Methods

##### `filterVariablesByPrefix(variables: Map<String, String>, prefix: String): Map<String, String>`

Filters variables by prefix (e.g., "wt_").

##### `validateRequiredVariables(requiredVariables: List<String>, providedVariables: Map<String, String>): List<String>`

Returns list of missing required variables.

##### `mergeVariables(vararg variableMaps: Map<String, String>): Map<String, String>`

Merges multiple variable maps (later maps override earlier ones).

##### `validateColorValues(variables: Map<String, String>): Map<String, String>`

Validates that color values are valid hex colors.

##### `normalizeColorValues(variables: Map<String, String>): Map<String, String>`

Adds # prefix to color values that are missing it.

##### `generateVariableReport(template: String, variables: Map<String, String>): String`

Generates a detailed analysis report of template variables.

### ProcessingResult Class

#### Properties

- `content: String` - The processed template content
- `replacedVariables: Map<String, String>` - Variables that were replaced
- `unreplacedVariables: List<String>` - Variables that were not replaced
- `warnings: List<String>` - Warning messages
- `isSuccess: Boolean` - True if all variables were replaced

#### Methods

- `getSummary(): String` - Returns human-readable summary

## Integration with ThemeConstructor

The TemplateProcessor is designed to integrate seamlessly with the existing ThemeConstructor:

```kotlin
// In ThemeConstructor.kt
private fun buildWindowsTerminalScheme(
    wtScheme: WindowsTerminalColorScheme,
    outputPath: Path
) {
    val processor = TemplateProcessor()
    val template = getWindowsTerminalTemplate()
    val variables = wtScheme.toColorPalette()

    val result = processor.processTemplate(template, variables, strict = true)

    Files.writeString(outputPath, result.content)
}
```

## Testing

### Unit Tests

Located in `buildSrc/src/test/kotlin/themes/TemplateProcessorTest.kt`:

- Basic variable replacement
- Windows Terminal variables
- Legacy One Dark variables
- Error handling and validation
- Variable discovery and analysis
- Utility functions
- Real-world scenarios
- Edge cases

### Integration Tests

Located in `buildSrc/src/test/kotlin/themes/TemplateProcessorIntegrationTest.kt`:

- Complete workflow from WindowsTerminalColorScheme to IntelliJ theme
- Loading and processing template files
- Batch processing multiple schemes
- Variable validation before processing
- Merging default and custom variables
- Error handling with helpful messages

### Running Tests

```bash
./gradlew :buildSrc:test --tests "TemplateProcessorTest"
./gradlew :buildSrc:test --tests "TemplateProcessorIntegrationTest"
```

## Error Handling

### Strict Mode

In strict mode, the processor throws an exception if any required variables are missing:

```kotlin
try {
    val result = processor.processTemplate(template, variables, strict = true)
    Files.writeString(outputPath, result.content)
} catch (e: IllegalArgumentException) {
    println("Template processing failed: ${e.message}")
    // Handle error
}
```

### Non-Strict Mode

In non-strict mode, the processor returns a result with warnings:

```kotlin
val result = processor.processTemplate(template, variables, strict = false)

if (!result.isSuccess) {
    println("Warnings during processing:")
    result.warnings.forEach { println("  - $it") }

    println("\nUnreplaced variables:")
    result.unreplacedVariables.forEach { println("  - $it") }
}

// You can still use the partially processed content
Files.writeString(outputPath, result.content)
```

## Best Practices

1. **Always validate color schemes** before processing:
   ```kotlin
   val errors = wtScheme.validate()
   if (errors.isNotEmpty()) {
       throw IllegalArgumentException("Invalid scheme: ${errors.joinToString()}")
   }
   ```

2. **Use strict mode** for production builds:
   ```kotlin
   val result = processor.processTemplate(template, variables, strict = true)
   ```

3. **Normalize color values** to ensure consistency:
   ```kotlin
   val normalized = processor.normalizeColorValues(variables)
   ```

4. **Generate reports** during development for debugging:
   ```kotlin
   println(processor.generateVariableReport(template, variables))
   ```

5. **Validate templates** before batch processing:
   ```kotlin
   val requiredVars = processor.findVariables(template)
   val missing = processor.validateRequiredVariables(requiredVars, variables)
   if (missing.isNotEmpty()) {
       println("Warning: Template requires variables not in scheme: $missing")
   }
   ```

## Performance Considerations

- Template processing is fast and can handle large templates efficiently
- Use `replaceVariables()` for simple replacements without validation overhead
- For batch processing, reuse the same `TemplateProcessor` instance
- Template loading from disk is the main bottleneck, not processing

## Future Enhancements

Potential improvements for future versions:

1. **Template caching** - Cache parsed templates for repeated use
2. **Variable type system** - Type-safe variables (color, number, string, etc.)
3. **Conditional variables** - Support for optional sections in templates
4. **Variable transformations** - Built-in functions like `darken($wt_red$, 0.2)`
5. **Template inheritance** - Base templates with overrides
6. **Better error recovery** - Suggestions for typos in variable names
7. **Performance metrics** - Track processing time and variable usage

## Troubleshooting

### Common Issues

**Problem**: Variables not being replaced

**Solution**: Check variable names match exactly (case-sensitive), ensure delimiters are `$` not other characters

**Problem**: Color validation errors

**Solution**: Ensure colors are in `#RRGGBB` format, use `normalizeColorValues()` to fix missing # prefix

**Problem**: Template requires variables not in WindowsTerminalColorScheme

**Solution**: Add custom variables or use `mergeVariables()` to include additional colors

**Problem**: "Similar available" suggestions not helpful

**Solution**: Use `generateVariableReport()` to see all available variables and their values

## See Also

- [Windows Terminal Color Schemes Documentation](https://learn.microsoft.com/en-us/windows/terminal/customize-settings/color-schemes)
- [IntelliJ Platform SDK - Color Schemes](https://plugins.jetbrains.com/docs/intellij/color-scheme-management.html)
- `WindowsTerminalColorScheme.kt` - Color scheme data model
- `ThemeConstructor.kt` - Theme building task
- `TASKS.md` - Project task tracking
