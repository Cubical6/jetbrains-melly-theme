package themes

import colorschemes.WindowsTerminalColorScheme
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Example usage of TemplateProcessor for generating IntelliJ themes
 * from Windows Terminal color schemes.
 *
 * This class demonstrates the complete workflow:
 * 1. Load a Windows Terminal color scheme (from JSON)
 * 2. Load a template file (XML or JSON)
 * 3. Process the template with color variables
 * 4. Write the output to a file
 */
object TemplateProcessorExample {

    /**
     * Example: Process a single Windows Terminal scheme into an IntelliJ theme
     */
    fun processWindowsTerminalScheme(
        wtScheme: WindowsTerminalColorScheme,
        templatePath: Path,
        outputPath: Path
    ) {
        val processor = TemplateProcessor()

        // Step 1: Validate the Windows Terminal scheme
        val validationErrors = wtScheme.validate()
        if (validationErrors.isNotEmpty()) {
            throw IllegalArgumentException(
                "Invalid Windows Terminal scheme:\n${validationErrors.joinToString("\n")}"
            )
        }

        // Step 2: Convert to color palette
        val colorVariables = wtScheme.toColorPalette()

        // Step 3: Add metadata variables
        val allVariables = colorVariables + mapOf(
            "name" to wtScheme.name,
            "scheme_name" to wtScheme.name
        )

        // Step 4: Load template
        val template = Files.readString(templatePath)

        // Step 5: Analyze template requirements (optional, for debugging)
        val report = processor.generateVariableReport(template, allVariables)
        println(report)

        // Step 6: Process template
        val result = processor.processTemplate(template, allVariables, strict = true)

        // Step 7: Write output
        Files.writeString(outputPath, result.content)

        println("Successfully generated theme: ${outputPath}")
        println("Replaced ${result.replacedVariables.size} variables")
    }

    /**
     * Example: Process multiple Windows Terminal schemes with the same template
     */
    fun batchProcessSchemes(
        schemes: List<WindowsTerminalColorScheme>,
        templatePath: Path,
        outputDirectory: Path
    ) {
        val processor = TemplateProcessor()
        val template = Files.readString(templatePath)

        schemes.forEach { scheme ->
            println("Processing scheme: ${scheme.name}")

            // Convert to variables
            val variables = scheme.toColorPalette() + mapOf("name" to scheme.name)

            // Process template
            val result = processor.processTemplate(template, variables, strict = false)

            if (!result.isSuccess) {
                println("Warning: Some variables not replaced in ${scheme.name}:")
                result.unreplacedVariables.forEach { println("  - $it") }
            }

            // Write output
            val outputFile = outputDirectory.resolve("${scheme.name.toLowerCase().replace(' ', '_')}.xml")
            Files.writeString(outputFile, result.content)

            println("  -> ${outputFile}")
        }
    }

    /**
     * Example: Create a theme with custom color overrides
     */
    fun processWithOverrides(
        wtScheme: WindowsTerminalColorScheme,
        templatePath: Path,
        outputPath: Path,
        customOverrides: Map<String, String>
    ) {
        val processor = TemplateProcessor()

        // Merge Windows Terminal colors with custom overrides
        val baseVariables = wtScheme.toColorPalette()
        val allVariables = processor.mergeVariables(baseVariables, customOverrides)

        val template = Files.readString(templatePath)
        val result = processor.processTemplate(template, allVariables, strict = true)

        Files.writeString(outputPath, result.content)
    }

    /**
     * Example: Validate template before processing
     */
    fun validateTemplateRequirements(
        templatePath: Path,
        availableVariables: Map<String, String>
    ): ValidationReport {
        val processor = TemplateProcessor()
        val template = Files.readString(templatePath)

        val requiredVars = processor.findVariables(template)
        val missingVars = processor.validateRequiredVariables(requiredVars, availableVariables)
        val colorErrors = processor.validateColorValues(availableVariables)

        return ValidationReport(
            templatePath = templatePath,
            requiredVariables = requiredVars,
            providedVariables = availableVariables.keys.toList(),
            missingVariables = missingVars,
            colorValidationErrors = colorErrors,
            isValid = missingVars.isEmpty() && colorErrors.isEmpty()
        )
    }

    data class ValidationReport(
        val templatePath: Path,
        val requiredVariables: List<String>,
        val providedVariables: List<String>,
        val missingVariables: List<String>,
        val colorValidationErrors: Map<String, String>,
        val isValid: Boolean
    ) {
        override fun toString(): String = buildString {
            appendLine("Template Validation Report")
            appendLine("=" .repeat(50))
            appendLine("Template: $templatePath")
            appendLine("Required variables: ${requiredVariables.size}")
            appendLine("Provided variables: ${providedVariables.size}")
            appendLine("Missing variables: ${missingVariables.size}")

            if (missingVariables.isNotEmpty()) {
                appendLine("\nMissing Variables:")
                missingVariables.forEach { appendLine("  - $it") }
            }

            if (colorValidationErrors.isNotEmpty()) {
                appendLine("\nColor Validation Errors:")
                colorValidationErrors.forEach { (varName, error) ->
                    appendLine("  - $varName: $error")
                }
            }

            appendLine("\nStatus: ${if (isValid) "VALID" else "INVALID"}")
        }
    }

    /**
     * Example: Filter and process only Windows Terminal variables
     */
    fun processOnlyWindowsTerminalVariables(
        template: String,
        allVariables: Map<String, String>
    ): String {
        val processor = TemplateProcessor()

        // Filter only wt_* variables
        val wtVariables = processor.filterVariablesByPrefix(allVariables, "wt_")

        // Process template
        val result = processor.processTemplate(template, wtVariables, strict = false)

        return result.content
    }

    /**
     * Demonstration of the complete workflow
     */
    @JvmStatic
    fun main(args: Array<String>) {
        // Example Windows Terminal scheme
        val oneDarkScheme = WindowsTerminalColorScheme(
            name = "One Dark",
            background = "#282c34",
            foreground = "#abb2bf",
            black = "#000000",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#d19a66",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            brightBlack = "#5c6370",
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#d19a66",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff",
            cursorColor = "#528bff",
            selectionBackground = "#3e4451"
        )

        // Example template
        val simpleTemplate = """
            <?xml version="1.0" encoding="UTF-8"?>
            <scheme name="${"$"}name${"$"}" parent_scheme="Darcula" version="142">
              <colors>
                <option name="CONSOLE_BACKGROUND" value="${"$"}wt_background${"$"}"/>
                <option name="CONSOLE_FOREGROUND" value="${"$"}wt_foreground${"$"}"/>
                <option name="CONSOLE_RED" value="${"$"}wt_red${"$"}"/>
                <option name="CONSOLE_GREEN" value="${"$"}wt_green${"$"}"/>
              </colors>
            </scheme>
        """.trimIndent()

        // Process it
        val processor = TemplateProcessor()
        val variables = oneDarkScheme.toColorPalette() + mapOf("name" to oneDarkScheme.name)
        val result = processor.processTemplate(simpleTemplate, variables, strict = true)

        println("Processing Result:")
        println("=" .repeat(50))
        println(result.getSummary())
        println()
        println("Generated XML:")
        println(result.content)

        // Example validation
        println("\n" + "=".repeat(50))
        println("Variable Analysis:")
        println("=" .repeat(50))
        println(processor.generateVariableReport(simpleTemplate, variables))
    }
}
