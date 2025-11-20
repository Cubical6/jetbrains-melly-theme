# TemplateProcessor Quick Reference

## Quick Start

```kotlin
val processor = TemplateProcessor()
val variables = windowsTerminalScheme.toColorPalette()
val result = processor.processTemplate(template, variables, strict = true)
Files.writeString(outputPath, result.content)
```

## Common Operations

### 1. Basic Processing
```kotlin
val result = processor.processTemplate(template, variables)
```

### 2. Find Variables in Template
```kotlin
val vars = processor.findVariables(template)
// Returns: ["wt_background", "wt_foreground", "wt_red"]
```

### 3. Validate Required Variables
```kotlin
val missing = processor.validateRequiredVariables(required, provided)
// Returns list of missing variable names
```

### 4. Check All Variables Replaced
```kotlin
val allReplaced = processor.validateAllVariablesReplaced(content)
// Returns true/false
```

### 5. Merge Variable Maps
```kotlin
val merged = processor.mergeVariables(defaults, wtColors, overrides)
```

### 6. Normalize Color Values
```kotlin
val normalized = processor.normalizeColorValues(variables)
// Adds # prefix to colors missing it
```

### 7. Filter Variables by Prefix
```kotlin
val wtVars = processor.filterVariablesByPrefix(allVars, "wt_")
```

### 8. Validate Color Values
```kotlin
val errors = processor.validateColorValues(variables)
// Returns map of variable name to error message
```

### 9. Generate Analysis Report
```kotlin
val report = processor.generateVariableReport(template, variables)
println(report)
```

## Variable Naming

### Windows Terminal Variables (use these in templates)
- Core: `$wt_background$`, `$wt_foreground$`, `$wt_cursorColor$`, `$wt_selectionBackground$`
- Normal: `$wt_black$`, `$wt_red$`, `$wt_green$`, `$wt_yellow$`, `$wt_blue$`, `$wt_purple$`, `$wt_cyan$`, `$wt_white$`
- Bright: `$wt_brightBlack$`, `$wt_brightRed$`, `$wt_brightGreen$`, `$wt_brightYellow$`, `$wt_brightBlue$`, `$wt_brightPurple$`, `$wt_brightCyan$`, `$wt_brightWhite$`

### Get Variables from WindowsTerminalColorScheme
```kotlin
val variables = windowsTerminalScheme.toColorPalette()
// Returns Map<String, String> with all wt_* variables
```

## ProcessingResult

```kotlin
val result = processor.processTemplate(template, variables)

result.content                  // Processed template content
result.replacedVariables        // Map of replaced variables
result.unreplacedVariables      // List of unreplaced variables
result.warnings                 // List of warning messages
result.isSuccess                // true if all variables replaced
result.getSummary()             // Human-readable summary
```

## Error Handling Patterns

### Pattern 1: Strict Mode (Fail Fast)
```kotlin
try {
    val result = processor.processTemplate(template, variables, strict = true)
    Files.writeString(output, result.content)
} catch (e: IllegalArgumentException) {
    println("Error: ${e.message}")
}
```

### Pattern 2: Non-Strict Mode (Collect Warnings)
```kotlin
val result = processor.processTemplate(template, variables, strict = false)
if (!result.isSuccess) {
    println("Warnings: ${result.warnings.joinToString()}")
}
Files.writeString(output, result.content)
```

### Pattern 3: Pre-Validation
```kotlin
val requiredVars = processor.findVariables(template)
val missingVars = processor.validateRequiredVariables(requiredVars, variables)

if (missingVars.isNotEmpty()) {
    println("Cannot process: Missing variables $missingVars")
    return
}

val result = processor.processTemplate(template, variables)
```

## Template Examples

### Minimal XML Template
```xml
<scheme name="$name$">
  <option name="BG" value="$wt_background$"/>
  <option name="FG" value="$wt_foreground$"/>
</scheme>
```

### Minimal JSON Template
```json
{
  "name": "$name$",
  "background": "$wt_background$",
  "foreground": "$wt_foreground$"
}
```

## Complete Workflow Example

```kotlin
// 1. Create/load Windows Terminal scheme
val scheme = WindowsTerminalColorScheme(
    name = "One Dark",
    background = "#282c34",
    foreground = "#abb2bf",
    // ... other colors
)

// 2. Validate scheme
val errors = scheme.validate()
require(errors.isEmpty()) { "Invalid scheme: $errors" }

// 3. Convert to variables
val variables = scheme.toColorPalette()

// 4. Add metadata
val allVars = variables + mapOf("name" to scheme.name)

// 5. Normalize colors
val normalized = processor.normalizeColorValues(allVars)

// 6. Load template
val template = Files.readString(templatePath)

// 7. Validate template requirements
val report = processor.generateVariableReport(template, normalized)
println(report)

// 8. Process
val result = processor.processTemplate(template, normalized, strict = true)

// 9. Verify success
require(result.isSuccess) { "Processing failed: ${result.getSummary()}" }

// 10. Write output
Files.writeString(outputPath, result.content)
```

## Integration with Existing Code

### Use with ThemeConstructor
```kotlin
// In ThemeConstructor.kt
private fun buildWindowsTerminalTheme(
    wtScheme: WindowsTerminalColorScheme,
    outputFile: Path
) {
    val processor = TemplateProcessor()
    val template = getWindowsTerminalTemplate()
    val variables = wtScheme.toColorPalette() + mapOf("name" to wtScheme.name)

    val result = processor.processTemplate(template, variables, strict = true)
    Files.writeString(outputFile, result.content)
}
```

### Batch Processing
```kotlin
fun processMultipleSchemes(schemes: List<WindowsTerminalColorScheme>) {
    val processor = TemplateProcessor()
    val template = Files.readString(templatePath)

    schemes.forEach { scheme ->
        val variables = scheme.toColorPalette() + mapOf("name" to scheme.name)
        val result = processor.processTemplate(template, variables)

        val outputFile = outputDir.resolve("${scheme.name.toLowerCase().replace(' ', '_')}.xml")
        Files.writeString(outputFile, result.content)

        println("Generated: $outputFile (${result.replacedVariables.size} variables)")
    }
}
```

## Testing

```kotlin
// Unit test example
@Test
fun `should replace all Windows Terminal variables`() {
    val template = """
        <option name="BG" value="${"$"}wt_background${"$"}"/>
        <option name="FG" value="${"$"}wt_foreground${"$"}"/>
    """.trimIndent()

    val variables = mapOf(
        "wt_background" to "#282c34",
        "wt_foreground" to "#abb2bf"
    )

    val result = processor.processTemplate(template, variables)

    assertTrue(result.isSuccess)
    assertTrue(result.content.contains("#282c34"))
    assertTrue(result.content.contains("#abb2bf"))
    assertFalse(result.content.contains("$"))
}
```

## Troubleshooting Quick Fixes

| Problem | Quick Fix |
|---------|-----------|
| Variables not replaced | Check variable names are exact match (case-sensitive) |
| Color validation errors | Use `processor.normalizeColorValues()` |
| Missing variables | Use `processor.generateVariableReport()` to see what's missing |
| Template won't process | Use non-strict mode first to see warnings |
| Typo in variable name | Use `generateVariableReport()` - it shows similar available names |

## Constants

```kotlin
TemplateProcessor.WINDOWS_TERMINAL_VARIABLES  // List of standard WT variable names
TemplateProcessor.VARIABLE_PATTERN            // Regex pattern for variables
```

## See Also

- Full documentation: `docs/TEMPLATE_PROCESSOR.md`
- Unit tests: `buildSrc/src/test/kotlin/themes/TemplateProcessorTest.kt`
- Integration tests: `buildSrc/src/test/kotlin/themes/TemplateProcessorIntegrationTest.kt`
- Example usage: `buildSrc/src/main/kotlin/themes/TemplateProcessorExample.kt`
