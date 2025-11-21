package themes

/**
 * TemplateProcessor handles variable replacement in template files for theme generation.
 *
 * This processor supports replacing placeholders in the format `$variable_name$` with actual values.
 * It's designed to work with both XML and JSON templates for Windows Terminal and IntelliJ theme generation.
 *
 * Key features:
 * - Replaces `$wt_*$` placeholders for Windows Terminal color schemes
 * - Replaces legacy placeholders from One Dark theme (e.g., `$green$`, `$coral$`)
 * - Validates that all required variables are replaced
 * - Provides detailed error messages for missing variables
 *
 * Example usage:
 * ```
 * val processor = TemplateProcessor()
 * val variables = mapOf(
 *     "wt_background" to "#282c34",
 *     "wt_foreground" to "#abb2bf"
 * )
 * val result = processor.processTemplate(template, variables)
 * ```
 *
 * @see WindowsTerminalColorScheme.toColorPalette for generating Windows Terminal variable maps
 */
class TemplateProcessor {
    companion object {
        /**
         * Regular expression to match template variables in the format $variable_name$
         * Matches: $name$, $wt_background$, $brightGreen$, etc.
         */
        private val VARIABLE_PATTERN = Regex("""\$([a-zA-Z_][a-zA-Z0-9_]*)\$""")

        /**
         * Delimiter character used for template variables
         */
        private const val DELIMITER = '$'

        /**
         * List of known Windows Terminal variable names for documentation purposes
         */
        val WINDOWS_TERMINAL_VARIABLES = listOf(
            "wt_background", "wt_foreground", "wt_cursorColor", "wt_selectionBackground",
            "wt_black", "wt_red", "wt_green", "wt_yellow", "wt_blue", "wt_purple", "wt_cyan", "wt_white",
            "wt_brightBlack", "wt_brightRed", "wt_brightGreen", "wt_brightYellow",
            "wt_brightBlue", "wt_brightPurple", "wt_brightCyan", "wt_brightWhite"
        )
    }

    /**
     * Result of template processing operation
     *
     * @property content The processed template content with variables replaced
     * @property replacedVariables Map of variable names to their replacement values
     * @property unreplacedVariables List of variable names that were found but not replaced
     * @property warnings List of warning messages generated during processing
     */
    data class ProcessingResult(
        val content: String,
        val replacedVariables: Map<String, String>,
        val unreplacedVariables: List<String>,
        val warnings: List<String>
    ) {
        /**
         * Returns true if processing was successful (no unreplaced variables)
         */
        val isSuccess: Boolean
            get() = unreplacedVariables.isEmpty()

        /**
         * Returns a human-readable summary of the processing result
         */
        fun getSummary(): String = buildString {
            appendLine("Template Processing Summary:")
            appendLine("  Replaced: ${replacedVariables.size} variables")
            if (unreplacedVariables.isNotEmpty()) {
                appendLine("  Unreplaced: ${unreplacedVariables.size} variables")
                unreplacedVariables.forEach { varName ->
                    appendLine("    - $varName")
                }
            }
            if (warnings.isNotEmpty()) {
                appendLine("  Warnings: ${warnings.size}")
                warnings.forEach { warning ->
                    appendLine("    - $warning")
                }
            }
        }
    }

    /**
     * Processes a template by replacing all variable placeholders with their corresponding values.
     *
     * This is the main entry point for template processing. It performs the following steps:
     * 1. Finds all variables in the template
     * 2. Replaces each variable with its value from the provided map
     * 3. Tracks which variables were replaced and which are missing
     * 4. Generates warnings for missing variables
     *
     * @param template The template content containing $variable$ placeholders
     * @param variables Map of variable names to their replacement values
     * @param strict If true, throws exception on unreplaced variables; if false, returns result with warnings
     * @return ProcessingResult containing the processed content and metadata
     * @throws IllegalArgumentException if strict=true and required variables are missing
     */
    fun processTemplate(
        template: String,
        variables: Map<String, String>,
        strict: Boolean = false
    ): ProcessingResult {
        val replacedVars = mutableMapOf<String, String>()
        val warnings = mutableListOf<String>()

        // Find all variables in the template
        val foundVariables = findVariables(template)

        // Replace variables
        var processedContent = template
        foundVariables.forEach { varName ->
            val value = variables[varName]
            if (value != null) {
                processedContent = processedContent.replace("$DELIMITER$varName$DELIMITER", value)
                replacedVars[varName] = value
            } else {
                warnings.add("Variable '\$$varName\$' not found in provided variables map")
            }
        }

        // Check for any remaining unreplaced variables
        val unreplacedVars = findVariables(processedContent)

        val result = ProcessingResult(
            content = processedContent,
            replacedVariables = replacedVars,
            unreplacedVariables = unreplacedVars,
            warnings = warnings
        )

        // In strict mode, throw exception if there are unreplaced variables
        if (strict && unreplacedVars.isNotEmpty()) {
            throw IllegalArgumentException(
                "Template processing failed. Missing required variables: ${unreplacedVars.joinToString(", ")}\n" +
                result.getSummary()
            )
        }

        return result
    }

    /**
     * Replaces all variables in the content with their corresponding values.
     *
     * This is a simpler version of processTemplate that only returns the processed content string.
     * It does not track replaced/unreplaced variables or generate warnings.
     *
     * @param content The content containing $variable$ placeholders
     * @param variables Map of variable names to their replacement values
     * @return The content with all found variables replaced
     */
    fun replaceVariables(content: String, variables: Map<String, String>): String {
        var result = content
        variables.forEach { (varName, value) ->
            result = result.replace("$DELIMITER$varName$DELIMITER", value)
        }
        return result
    }

    /**
     * Validates that all variables in the content have been replaced.
     *
     * This method checks if there are any remaining $variable$ placeholders in the content.
     * It's useful for ensuring that template processing is complete.
     *
     * @param content The content to validate
     * @return true if no unreplaced variables remain, false otherwise
     */
    fun validateAllVariablesReplaced(content: String): Boolean {
        return findVariables(content).isEmpty()
    }

    /**
     * Finds all variable names in the given content.
     *
     * Scans the content for all occurrences of the pattern $variable_name$ and returns
     * a list of the variable names (without the delimiters).
     *
     * @param content The content to scan for variables
     * @return List of unique variable names found in the content
     */
    fun findVariables(content: String): List<String> {
        return VARIABLE_PATTERN.findAll(content)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }

    /**
     * Extracts a subset of variables from a map that match a specific prefix.
     *
     * This is useful for filtering Windows Terminal variables (wt_*) from a larger variable set.
     *
     * @param variables The full variable map
     * @param prefix The prefix to filter by (e.g., "wt_")
     * @return Map containing only variables with the specified prefix
     */
    fun filterVariablesByPrefix(variables: Map<String, String>, prefix: String): Map<String, String> {
        return variables.filterKeys { it.startsWith(prefix) }
    }

    /**
     * Validates that all required variables are present in the provided map.
     *
     * @param requiredVariables List of variable names that must be present
     * @param providedVariables The map of variables to validate
     * @return List of missing variable names (empty if all required variables are present)
     */
    fun validateRequiredVariables(
        requiredVariables: List<String>,
        providedVariables: Map<String, String>
    ): List<String> {
        return requiredVariables.filter { it !in providedVariables }
    }

    /**
     * Merges multiple variable maps, with later maps taking precedence.
     *
     * This is useful for combining default variables with user-provided overrides.
     *
     * @param variableMaps Variable maps to merge, in order of precedence (first = lowest, last = highest)
     * @return Merged map with later values overriding earlier ones
     */
    fun mergeVariables(vararg variableMaps: Map<String, String>): Map<String, String> {
        return variableMaps.fold(emptyMap()) { acc, map -> acc + map }
    }

    /**
     * Validates that all color values in a variable map are valid hex colors.
     *
     * @param variables Map of variables to validate
     * @return Map of variable names to validation error messages (empty if all valid)
     */
    fun validateColorValues(variables: Map<String, String>): Map<String, String> {
        val hexColorPattern = Regex("^#?[0-9A-Fa-f]{6}$")
        val errors = mutableMapOf<String, String>()

        variables.forEach { (varName, value) ->
            // Check if this looks like a color variable (contains color names or wt_ prefix)
            val isColorVariable = varName.startsWith("wt_") ||
                listOf("color", "background", "foreground", "black", "red", "green", "blue",
                       "yellow", "purple", "cyan", "white", "coral", "chalky", "malibu").any {
                    varName.contains(it, ignoreCase = true)
                }

            if (isColorVariable && !hexColorPattern.matches(value)) {
                errors[varName] = "Invalid color value: '$value' (expected #RRGGBB or RRGGBB format)"
            }
        }

        return errors
    }

    /**
     * Normalizes color values to ensure they have the # prefix.
     *
     * @param variables Map of variables, some of which may be colors
     * @return New map with color values normalized to include # prefix
     */
    fun normalizeColorValues(variables: Map<String, String>): Map<String, String> {
        val hexColorPattern = Regex("^[0-9A-Fa-f]{6}$")

        return variables.mapValues { (_, value) ->
            if (hexColorPattern.matches(value)) {
                "#$value"
            } else {
                value
            }
        }
    }

    /**
     * Generates a report of template variable usage.
     *
     * This method analyzes a template and generates a detailed report showing:
     * - All variables found in the template
     * - Which variables have values provided
     * - Which variables are missing
     * - Suggestions for missing variables
     *
     * @param template The template content to analyze
     * @param variables The available variables
     * @return Human-readable report string
     */
    fun generateVariableReport(template: String, variables: Map<String, String>): String = buildString {
        val templateVars = findVariables(template)
        val providedVars = templateVars.filter { it in variables }
        val missingVars = templateVars.filter { it !in variables }

        appendLine("Template Variable Analysis")
        appendLine("=" .repeat(50))
        appendLine("Total variables in template: ${templateVars.size}")
        appendLine("Variables with values: ${providedVars.size}")
        appendLine("Missing variables: ${missingVars.size}")
        appendLine()

        if (providedVars.isNotEmpty()) {
            appendLine("Provided Variables:")
            providedVars.sorted().forEach { varName ->
                appendLine("  \$$varName\$ = ${variables[varName]}")
            }
            appendLine()
        }

        if (missingVars.isNotEmpty()) {
            appendLine("Missing Variables:")
            missingVars.sorted().forEach { varName ->
                appendLine("  \$$varName\$")

                // Provide suggestions for similar variable names
                val suggestions = variables.keys.filter {
                    it.contains(varName, ignoreCase = true) ||
                    varName.contains(it, ignoreCase = true)
                }
                if (suggestions.isNotEmpty()) {
                    appendLine("    Similar available: ${suggestions.joinToString(", ")}")
                }
            }
        }
    }
}
