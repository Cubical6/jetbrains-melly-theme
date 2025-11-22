package generators

import colorschemes.WindowsTerminalColorScheme
import mapping.ColorMappingConfig
import mapping.ConsoleColorMapper
import mapping.SyntaxColorInference
import utils.ColorUtils
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import java.io.ByteArrayInputStream

/**
 * Generates IntelliJ IDEA XML color scheme files from Windows Terminal color schemes.
 *
 * This generator:
 * - Reads the Windows Terminal template XML
 * - Replaces all template placeholders with actual color values
 * - Maps console colors using ConsoleColorMapper
 * - Infers syntax colors using SyntaxColorInference
 * - Validates the generated XML is well-formed
 * - Writes the result to the specified output file
 *
 * Template placeholders:
 * - `$SCHEME_NAME$` - Replaced with the scheme name
 * - `$wt_background$`, `$wt_foreground$` - Special colors
 * - `$wt_<color>$` - ANSI colors (black, red, green, yellow, blue, magenta, cyan, white)
 * - `$wt_bright_<color>$` - Bright ANSI colors
 *
 * @property templatePath Path to the Windows Terminal template XML file
 * @property consoleColorMapper Mapper for console color attributes
 */
class XMLColorSchemeGenerator(
    private val templatePath: Path = Path.of("buildSrc/templates/windows-terminal.template.xml"),
    private val consoleColorMapper: ConsoleColorMapper = ConsoleColorMapper(ColorMappingConfig)
) {

    // Template cache to avoid re-reading the file for each theme
    @Volatile
    private var cachedTemplate: String? = null
    private val cacheLock = Any()

    companion object {
        /**
         * Expected template placeholders and their meanings
         */
        private val EXPECTED_PLACEHOLDERS = setOf(
            "\$SCHEME_NAME$",
            "\$wt_background$",
            "\$wt_foreground$",
            "\$wt_black$",
            "\$wt_red$",
            "\$wt_green$",
            "\$wt_yellow$",
            "\$wt_blue$",
            "\$wt_magenta$",
            "\$wt_cyan$",
            "\$wt_white$",
            "\$wt_bright_black$",
            "\$wt_bright_red$",
            "\$wt_bright_green$",
            "\$wt_bright_yellow$",
            "\$wt_bright_blue$",
            "\$wt_bright_magenta$",
            "\$wt_bright_cyan$",
            "\$wt_bright_white$"
        )
    }

    /**
     * Validates that all required color attributes are present and non-empty.
     * @throws IllegalArgumentException if required colors are missing
     */
    private fun validateRequiredAttributes(colorPalette: Map<String, String>) {
        val required = listOf(
            "wt_background", "wt_foreground",
            "wt_black", "wt_red", "wt_green", "wt_yellow",
            "wt_blue", "wt_purple", "wt_cyan", "wt_white"
        )

        required.forEach { key ->
            val value = colorPalette[key]
            require(!value.isNullOrBlank()) {
                "Required color placeholder '$key' is missing or empty in color palette"
            }
        }
    }

    /**
     * Validates contrast ratio between background and foreground colors.
     * Warns if contrast is too low for readability.
     */
    private fun validateContrast(background: String, foreground: String) {
        val ratio = ColorUtils.calculateContrastRatio(background, foreground)
        if (ratio < 3.0) {
            println("⚠️  WARNING: Low contrast ratio (${"%.2f".format(ratio)}) between background ($background) and foreground ($foreground)")
            println("   WCAG recommends minimum 4.5:1 for normal text, 3:1 for large text")
        }
    }

    /**
     * Generates an IntelliJ XML color scheme file from a Windows Terminal color scheme.
     *
     * @param scheme Windows Terminal color scheme to convert
     * @param outputPath Path where the XML file should be written
     * @throws IllegalArgumentException if the scheme is invalid
     * @throws IllegalStateException if the template is missing or invalid
     * @throws java.io.IOException if file operations fail
     */
    fun generate(scheme: WindowsTerminalColorScheme, outputPath: Path) {
        // Validate the scheme
        val validationErrors = scheme.validate()
        require(validationErrors.isEmpty()) {
            "Invalid color scheme: ${validationErrors.joinToString(", ")}"
        }

        // Read the template
        val template = readTemplate()

        // Build replacement map
        val replacements = buildReplacementMap(scheme)

        // Build color palette for validation
        val colorPalette = mapOf(
            "wt_background" to scheme.background,
            "wt_foreground" to scheme.foreground,
            "wt_black" to scheme.black,
            "wt_red" to scheme.red,
            "wt_green" to scheme.green,
            "wt_yellow" to scheme.yellow,
            "wt_blue" to scheme.blue,
            "wt_purple" to scheme.purple,
            "wt_cyan" to scheme.cyan,
            "wt_white" to scheme.white
        )

        // Validate color palette
        validateRequiredAttributes(colorPalette)
        validateContrast(
            colorPalette["wt_background"] ?: "",
            colorPalette["wt_foreground"] ?: ""
        )

        // Replace all placeholders
        var result = template
        replacements.forEach { (placeholder, value) ->
            result = result.replace(placeholder, value)
        }

        // Validate the generated XML
        validateXml(result)

        // Ensure output directory exists
        outputPath.parent?.let { Files.createDirectories(it) }

        // Write the result
        Files.writeString(outputPath, result)
    }

    /**
     * Reads the template file with caching for performance.
     * Uses double-checked locking to ensure thread-safe lazy initialization.
     *
     * @return Template content as a string
     * @throws IllegalStateException if the template file is missing or cannot be read
     */
    private fun readTemplate(): String {
        // Fast path: return cached template if available
        cachedTemplate?.let { return it }

        // Slow path: read template with synchronization
        return synchronized(cacheLock) {
            // Double-check after acquiring lock
            cachedTemplate?.let { return it }

            require(Files.exists(templatePath)) {
                "Template file not found: $templatePath"
            }

            try {
                val template = Files.readString(templatePath)
                cachedTemplate = template
                template
            } catch (e: Exception) {
                throw IllegalStateException("Failed to read template file: $templatePath", e)
            }
        }
    }

    /**
     * Builds a complete replacement map for all template placeholders.
     *
     * @param scheme Windows Terminal color scheme
     * @return Map of placeholder strings to their replacement values
     */
    private fun buildReplacementMap(
        scheme: WindowsTerminalColorScheme
    ): Map<String, String> {
        val replacements = mutableMapOf<String, String>()

        // Scheme name
        replacements["\$SCHEME_NAME$"] = scheme.name

        // Map Windows Terminal colors to template placeholders
        // Note: Windows Terminal uses "purple" but template uses "magenta"
        replacements["\$wt_background$"] = normalizeColor(scheme.background)
        replacements["\$wt_foreground$"] = normalizeColor(scheme.foreground)
        replacements["\$wt_black$"] = normalizeColor(scheme.black)
        replacements["\$wt_red$"] = normalizeColor(scheme.red)
        replacements["\$wt_green$"] = normalizeColor(scheme.green)
        replacements["\$wt_yellow$"] = normalizeColor(scheme.yellow)
        replacements["\$wt_blue$"] = normalizeColor(scheme.blue)
        replacements["\$wt_magenta$"] = normalizeColor(scheme.purple)  // purple → magenta
        replacements["\$wt_cyan$"] = normalizeColor(scheme.cyan)
        replacements["\$wt_white$"] = normalizeColor(scheme.white)
        replacements["\$wt_bright_black$"] = normalizeColor(scheme.brightBlack)
        replacements["\$wt_bright_red$"] = normalizeColor(scheme.brightRed)
        replacements["\$wt_bright_green$"] = normalizeColor(scheme.brightGreen)
        replacements["\$wt_bright_yellow$"] = normalizeColor(scheme.brightYellow)
        replacements["\$wt_bright_blue$"] = normalizeColor(scheme.brightBlue)
        replacements["\$wt_bright_magenta$"] = normalizeColor(scheme.brightPurple)  // brightPurple → bright_magenta
        replacements["\$wt_bright_cyan$"] = normalizeColor(scheme.brightCyan)
        replacements["\$wt_bright_white$"] = normalizeColor(scheme.brightWhite)

        return replacements
    }

    /**
     * Normalizes a color value for XML output.
     * Removes the '#' prefix and converts to lowercase as IntelliJ XML format expects.
     *
     * @param color Color in #RRGGBB format
     * @return Color in lowercase rrggbb format (without #)
     */
    private fun normalizeColor(color: String): String {
        return color.removePrefix("#").lowercase()
    }

    /**
     * Validates that the generated XML is well-formed.
     *
     * @param xml XML content to validate
     * @throws IllegalStateException if the XML is not well-formed
     */
    private fun validateXml(xml: String) {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()

            // Parse the XML to ensure it's well-formed
            ByteArrayInputStream(xml.toByteArray()).use { inputStream ->
                builder.parse(inputStream)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Generated XML is not well-formed: ${e.message}", e)
        }
    }

    /**
     * Generates a preview of the color scheme without writing to file.
     * Useful for testing and validation.
     *
     * @param scheme Windows Terminal color scheme to convert
     * @return Generated XML content as a string
     */
    fun generatePreview(scheme: WindowsTerminalColorScheme): String {
        // Validate the scheme
        val validationErrors = scheme.validate()
        require(validationErrors.isEmpty()) {
            "Invalid color scheme: ${validationErrors.joinToString(", ")}"
        }

        // Read the template
        val template = readTemplate()

        // Build replacement map
        val replacements = buildReplacementMap(scheme)

        // Build color palette for validation
        val colorPalette = mapOf(
            "wt_background" to scheme.background,
            "wt_foreground" to scheme.foreground,
            "wt_black" to scheme.black,
            "wt_red" to scheme.red,
            "wt_green" to scheme.green,
            "wt_yellow" to scheme.yellow,
            "wt_blue" to scheme.blue,
            "wt_purple" to scheme.purple,
            "wt_cyan" to scheme.cyan,
            "wt_white" to scheme.white
        )

        // Validate color palette
        validateRequiredAttributes(colorPalette)
        validateContrast(
            colorPalette["wt_background"] ?: "",
            colorPalette["wt_foreground"] ?: ""
        )

        // Replace all placeholders
        var result = template
        replacements.forEach { (placeholder, value) ->
            result = result.replace(placeholder, value)
        }

        // Validate the generated XML
        validateXml(result)

        return result
    }

    /**
     * Checks if the template file exists and is readable.
     *
     * @return true if the template is available, false otherwise
     */
    fun isTemplateAvailable(): Boolean {
        return Files.exists(templatePath) && Files.isReadable(templatePath)
    }

    /**
     * Gets the expected template placeholders.
     * Useful for testing and validation.
     *
     * @return Set of expected placeholder strings
     */
    fun getExpectedPlaceholders(): Set<String> {
        return EXPECTED_PLACEHOLDERS
    }
}
