package generators

import colorschemes.WindowsTerminalColorScheme
import colorschemes.toColorPaletteMap
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import themes.TemplateProcessor
import utils.ColorUtils
import variants.ThemeVariant
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Generates IntelliJ UI theme JSON files from Windows Terminal color schemes.
 *
 * This generator takes a WindowsTerminalColorScheme and produces a complete .theme.json file
 * suitable for IntelliJ IDEA and other JetBrains IDEs. It uses the Windows Terminal template
 * at buildSrc/templates/windows-terminal.template.theme.json as the base.
 *
 * Key features:
 * - Replaces all $wt_*$ placeholders with actual color values from the scheme
 * - Automatically detects if theme should be "dark" or "light" based on background luminance
 * - Maps terminal colors to IntelliJ UI elements (Editor, Tool Windows, etc.)
 * - Generates proper theme metadata (name, author info)
 * - Validates output JSON is well-formed
 *
 * Example usage:
 * ```kotlin
 * val generator = UIThemeGenerator()
 * val scheme = WindowsTerminalColorScheme(
 *     name = "My Theme",
 *     background = "#282c34",
 *     foreground = "#abb2bf",
 *     // ... other colors
 * )
 * generator.generateUITheme(scheme, Paths.get("output/mytheme.theme.json"))
 * ```
 *
 * @see WindowsTerminalColorScheme for color scheme structure
 * @see TemplateProcessor for template variable replacement
 */
class UIThemeGenerator {
    private val templateProcessor = TemplateProcessor()
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    // Template cache to avoid re-reading the file for each theme (thread-safe)
    private val templateCache = mutableMapOf<String, String>()
    private val cacheLock = Any()

    companion object {
        /**
         * Default template path relative to buildSrc directory
         */
        const val DEFAULT_TEMPLATE_PATH = "templates/windows-terminal.template.theme.json"

        /**
         * Luminance threshold for dark/light detection (0-255 scale)
         * Values below this are considered dark, values above are considered light
         */
        const val DARK_LIGHT_THRESHOLD = 100.0

        /**
         * Author string used in generated theme metadata
         */
        const val DEFAULT_AUTHOR = "Windows Terminal Color Scheme Converter"

        /**
         * Generate UI theme for specific variant
         */
        fun generateVariant(
            scheme: WindowsTerminalColorScheme,
            variant: ThemeVariant,
            outputDir: File
        ): File {
            // Validate the color scheme
            val validationErrors = scheme.validate()
            require(validationErrors.isEmpty()) {
                "Invalid color scheme: ${validationErrors.joinToString("; ")}"
            }

            val palette = scheme.toColorPaletteMap()

            // Choose template based on variant
            val templateName = when (variant) {
                ThemeVariant.Standard -> "windows-terminal.template.theme.json"
                ThemeVariant.Rounded -> "windows-terminal-rounded.template.theme.json"
            }

            val templateFile = File("buildSrc/templates/$templateName")
            require(templateFile.exists()) { "Template not found: $templateName" }

            val template = templateFile.readText()

            // Detect if theme is dark or light
            val isDark = ColorUtils.calculateLuminance(scheme.background) < DARK_LIGHT_THRESHOLD

            // Replace all placeholders (colors + arc values)
            var content = template

            // Replace color placeholders
            palette.forEach { (key, value) ->
                content = content.replace("\$$key$", value)
            }

            // Replace metadata placeholders
            val themeName = scheme.name + variant.suffix
            content = content.replace("\$wt_name$", themeName)
            content = content.replace("\$wt_scheme_name$", sanitizeFileNameStatic(scheme.name))
            content = content.replace("\$wt_dark$", isDark.toString())
            content = content.replace("\$wt_author$", DEFAULT_AUTHOR)

            // Replace arc value placeholders
            variant.arcValues.toPlaceholders().forEach { (placeholder, value) ->
                content = content.replace(placeholder, value)
            }

            // Validate the generated JSON
            try {
                JsonParser.parseString(content).asJsonObject
            } catch (e: Exception) {
                throw IllegalStateException("Generated content is not valid JSON: ${e.message}", e)
            }

            // Write output
            val fileName = "${sanitizeFileNameStatic(scheme.name)}${variant.suffix.replace(" ", "_").lowercase()}.theme.json"
            outputDir.mkdirs()
            val outputFile = File(outputDir, fileName)
            outputFile.writeText(content)

            return outputFile
        }

        /**
         * Generate UI themes for all variants
         */
        fun generate(scheme: WindowsTerminalColorScheme, outputDir: File): List<File> {
            val generatedFiles = mutableListOf<File>()

            // Generate all variants
            for (variant in ThemeVariant.all()) {
                val file = generateVariant(scheme, variant, outputDir)
                generatedFiles.add(file)
                val variantLabel = if (variant.displayName.isNotEmpty()) variant.displayName else "Standard"
                println("Generated $variantLabel variant: ${file.name}")
            }

            return generatedFiles
        }

        /**
         * Sanitizes a theme name for use as a file name (static version).
         */
        private fun sanitizeFileNameStatic(name: String): String {
            return name
                .trim()
                .replace(Regex("[^a-zA-Z0-9\\s-]"), "")  // Remove special chars
                .replace(Regex("\\s+"), "_")              // Spaces to underscores
                .replace(Regex("_+"), "_")                // Multiple underscores to single
                .lowercase()
        }
    }

    /**
     * Result of UI theme generation
     *
     * @property success True if generation completed successfully
     * @property outputPath Path where the theme file was written
     * @property themeName Name of the generated theme
     * @property isDark True if the theme was detected as dark
     * @property warnings List of non-fatal warnings during generation
     * @property error Error message if generation failed, null otherwise
     */
    data class GenerationResult(
        val success: Boolean,
        val outputPath: Path?,
        val themeName: String,
        val isDark: Boolean,
        val warnings: List<String> = emptyList(),
        val error: String? = null
    ) {
        /**
         * Returns a human-readable summary of the generation result
         */
        fun getSummary(): String = buildString {
            if (success) {
                appendLine("✓ UI Theme generated successfully")
                appendLine("  Theme: $themeName")
                appendLine("  Type: ${if (isDark) "Dark" else "Light"}")
                appendLine("  Output: $outputPath")
                if (warnings.isNotEmpty()) {
                    appendLine("  Warnings: ${warnings.size}")
                    warnings.forEach { appendLine("    - $it") }
                }
            } else {
                appendLine("✗ UI Theme generation failed")
                appendLine("  Theme: $themeName")
                appendLine("  Error: $error")
            }
        }
    }

    /**
     * Generates a complete IntelliJ UI theme JSON file from a Windows Terminal color scheme.
     *
     * This method:
     * 1. Reads the Windows Terminal UI theme template
     * 2. Replaces all $wt_*$ placeholders with actual colors from the scheme
     * 3. Detects if theme should be "dark" or "light" based on background luminance
     * 4. Generates theme metadata (name, author)
     * 5. Validates the output JSON is well-formed
     * 6. Writes to the specified output path
     *
     * @param scheme The Windows Terminal color scheme to convert
     * @param outputPath Path where the .theme.json file should be written
     * @param templatePath Optional custom template path (relative to buildSrc). Uses DEFAULT_TEMPLATE_PATH if null.
     * @param overwriteExisting If true, overwrites existing file; if false, throws exception if file exists
     * @return GenerationResult containing status and metadata
     * @throws IllegalArgumentException if the color scheme is invalid
     * @throws IllegalStateException if the template file is missing or invalid
     * @throws java.io.IOException if file operations fail
     */
    fun generateUITheme(
        scheme: WindowsTerminalColorScheme,
        outputPath: Path,
        templatePath: String? = null,
        overwriteExisting: Boolean = true
    ): GenerationResult {
        val warnings = mutableListOf<String>()

        try {
            // Validate the color scheme
            val validationErrors = scheme.validate()
            if (validationErrors.isNotEmpty()) {
                throw IllegalArgumentException(
                    "Invalid color scheme: ${validationErrors.joinToString("; ")}"
                )
            }

            // Check if output file exists
            if (!overwriteExisting && outputPath.exists()) {
                throw IllegalStateException("Output file already exists: $outputPath")
            }

            // Read the template
            val template = readTemplate(templatePath ?: DEFAULT_TEMPLATE_PATH)

            // Detect if theme is dark or light
            val isDark = detectDarkTheme(scheme.background)

            // Prepare variables for template replacement
            val variables = buildVariableMap(scheme, isDark)

            // Process the template
            val processingResult = templateProcessor.processTemplate(template, variables, strict = true)

            if (!processingResult.isSuccess) {
                warnings.addAll(processingResult.warnings)
            }

            // Validate the generated JSON
            validateJson(processingResult.content)

            // Ensure output directory exists
            outputPath.parent?.let { parent ->
                Files.createDirectories(parent)
            }

            // Write the output file
            Files.writeString(
                outputPath,
                processingResult.content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )

            return GenerationResult(
                success = true,
                outputPath = outputPath,
                themeName = scheme.name,
                isDark = isDark,
                warnings = warnings
            )

        } catch (e: Exception) {
            return GenerationResult(
                success = false,
                outputPath = null,
                themeName = scheme.name,
                isDark = false,
                error = "${e::class.simpleName}: ${e.message}"
            )
        }
    }

    /**
     * Reads the UI theme template file with caching for performance.
     * Uses synchronized access to ensure thread-safe caching.
     *
     * @param templatePath Path to template file relative to buildSrc directory
     * @return Template content as string
     * @throws IllegalStateException if template file is missing or cannot be read
     */
    private fun readTemplate(templatePath: String): String {
        // Check cache first (synchronized for thread safety)
        synchronized(cacheLock) {
            templateCache[templatePath]?.let { return it }
        }

        // Template not cached, read from disk
        val buildSrcPath = Path.of("buildSrc", templatePath)

        val template = if (buildSrcPath.exists()) {
            buildSrcPath.readText()
        } else {
            // Try as absolute path
            val absolutePath = Path.of(templatePath)
            if (absolutePath.exists()) {
                absolutePath.readText()
            } else {
                throw IllegalStateException(
                    "Template file not found: $templatePath (tried: $buildSrcPath, $absolutePath)"
                )
            }
        }

        // Cache the template (synchronized for thread safety)
        synchronized(cacheLock) {
            templateCache[templatePath] = template
        }

        return template
    }

    /**
     * Detects if a theme should be classified as "dark" based on background luminance.
     *
     * Uses ColorUtils.calculateLuminance() to determine perceived brightness.
     * Values below DARK_LIGHT_THRESHOLD are considered dark.
     *
     * @param backgroundColor Background color in #RRGGBB format
     * @return True if theme should be dark, false if light
     */
    private fun detectDarkTheme(backgroundColor: String): Boolean {
        val luminance = ColorUtils.calculateLuminance(backgroundColor)
        return luminance < DARK_LIGHT_THRESHOLD
    }

    /**
     * Builds the complete variable map for template replacement.
     *
     * This includes:
     * - All Windows Terminal colors with wt_* prefix
     * - Theme metadata (name, author)
     * - Dark/light flag
     * - Scheme name (sanitized for file system)
     *
     * @param scheme The Windows Terminal color scheme
     * @param isDark Whether the theme is dark
     * @return Map of variable names to their values
     */
    private fun buildVariableMap(
        scheme: WindowsTerminalColorScheme,
        isDark: Boolean
    ): Map<String, String> {
        return buildMap {
            // Add all color palette variables (base colors + derived colors)
            putAll(scheme.toColorPaletteMap())

            // Add metadata variables
            put("wt_name", scheme.name)
            put("wt_scheme_name", sanitizeFileName(scheme.name))
            put("wt_dark", isDark.toString())
            put("wt_author", DEFAULT_AUTHOR)
        }
    }

    /**
     * Sanitizes a theme name for use as a file name.
     *
     * Removes or replaces characters that are invalid in file names:
     * - Spaces → underscores
     * - Special characters → removed
     * - Multiple underscores → single underscore
     *
     * @param name Original theme name
     * @return Sanitized name suitable for file names
     */
    private fun sanitizeFileName(name: String): String {
        return name
            .trim()
            .replace(Regex("[^a-zA-Z0-9\\s-]"), "")  // Remove special chars
            .replace(Regex("\\s+"), "_")              // Spaces to underscores
            .replace(Regex("_+"), "_")                // Multiple underscores to single
            .lowercase()
    }

    /**
     * Validates that a string contains well-formed JSON.
     *
     * @param jsonContent JSON string to validate
     * @throws IllegalStateException if JSON is malformed
     */
    private fun validateJson(jsonContent: String) {
        try {
            JsonParser.parseString(jsonContent).asJsonObject
        } catch (e: JsonSyntaxException) {
            throw IllegalStateException("Generated content is not valid JSON: ${e.message}", e)
        } catch (e: Exception) {
            throw IllegalStateException("Generated content is not valid JSON: ${e.message}", e)
        }
    }

    /**
     * Generates a UI theme and returns the JSON content as a string without writing to disk.
     * Useful for testing or in-memory operations.
     *
     * @param scheme The Windows Terminal color scheme to convert
     * @param templatePath Optional custom template path (relative to buildSrc)
     * @return Generated theme JSON content
     * @throws IllegalArgumentException if the color scheme is invalid
     * @throws IllegalStateException if the template file is missing or invalid
     */
    fun generateUIThemeContent(
        scheme: WindowsTerminalColorScheme,
        templatePath: String? = null
    ): String {
        // Validate the color scheme
        val validationErrors = scheme.validate()
        if (validationErrors.isNotEmpty()) {
            throw IllegalArgumentException(
                "Invalid color scheme: ${validationErrors.joinToString("; ")}"
            )
        }

        // Read the template
        val template = readTemplate(templatePath ?: DEFAULT_TEMPLATE_PATH)

        // Detect if theme is dark or light
        val isDark = detectDarkTheme(scheme.background)

        // Prepare variables for template replacement
        val variables = buildVariableMap(scheme, isDark)

        // Process the template
        val processingResult = templateProcessor.processTemplate(template, variables, strict = true)

        // Validate the generated JSON
        validateJson(processingResult.content)

        return processingResult.content
    }

    /**
     * Analyzes a color scheme and returns theme detection information without generating files.
     * Useful for preview and debugging purposes.
     *
     * @param scheme The Windows Terminal color scheme to analyze
     * @return Map containing detection results (isDark, luminance, etc.)
     */
    fun analyzeColorScheme(scheme: WindowsTerminalColorScheme): Map<String, Any> {
        val luminance = ColorUtils.calculateLuminance(scheme.background)
        val isDark = luminance < DARK_LIGHT_THRESHOLD

        return mapOf(
            "name" to scheme.name,
            "backgroundColor" to scheme.background,
            "foregroundColor" to scheme.foreground,
            "luminance" to luminance,
            "isDark" to isDark,
            "themeType" to if (isDark) "dark" else "light",
            "sanitizedName" to sanitizeFileName(scheme.name)
        )
    }
}
