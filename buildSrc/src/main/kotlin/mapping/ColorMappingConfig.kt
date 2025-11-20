package mapping

/**
 * Configuration for mapping Windows Terminal color properties to IntelliJ attributes.
 *
 * Based on COLOR_MAPPING_SPEC.yaml and official IntelliJ theme documentation:
 * https://plugins.jetbrains.com/docs/intellij/theme-structure.html
 *
 * Provides:
 * - Direct ANSI color mappings (16 colors + 4 special)
 * - Syntax color inference rules
 * - Edge case detection thresholds
 * - Fallback strategies
 */
object ColorMappingConfig {

    // ========== CONSOLE COLOR MAPPINGS ==========

    /**
     * Maps Windows Terminal properties to IntelliJ console color attributes.
     * Each Windows Terminal property can map to one or more IntelliJ attributes.
     */
    val consoleColorMappings = mapOf(
        // Special colors
        "background" to listOf("CONSOLE_BACKGROUND_KEY"),
        "foreground" to listOf("CONSOLE_NORMAL_OUTPUT", "FOREGROUND"),
        "cursorColor" to listOf("CARET_COLOR", "CONSOLE_CURSOR"),
        "selectionBackground" to listOf("CONSOLE_SELECTION_BACKGROUND"),

        // ANSI colors (normal) - codes 30-37
        "black" to listOf("CONSOLE_BLACK_OUTPUT"),
        "red" to listOf("CONSOLE_RED_OUTPUT"),
        "green" to listOf("CONSOLE_GREEN_OUTPUT"),
        "yellow" to listOf("CONSOLE_YELLOW_OUTPUT"),
        "blue" to listOf("CONSOLE_BLUE_OUTPUT"),
        "purple" to listOf("CONSOLE_MAGENTA_OUTPUT"),  // Windows Terminal "purple" = ANSI magenta
        "cyan" to listOf("CONSOLE_CYAN_OUTPUT"),
        "white" to listOf("CONSOLE_GRAY_OUTPUT"),  // Windows Terminal "white" = ANSI gray

        // ANSI bright colors - codes 90-97
        "brightBlack" to listOf("CONSOLE_DARKGRAY_OUTPUT"),
        "brightRed" to listOf("CONSOLE_RED_BRIGHT_OUTPUT"),
        "brightGreen" to listOf("CONSOLE_GREEN_BRIGHT_OUTPUT"),
        "brightYellow" to listOf("CONSOLE_YELLOW_BRIGHT_OUTPUT"),
        "brightBlue" to listOf("CONSOLE_BLUE_BRIGHT_OUTPUT"),
        "brightPurple" to listOf("CONSOLE_MAGENTA_BRIGHT_OUTPUT"),
        "brightCyan" to listOf("CONSOLE_CYAN_BRIGHT_OUTPUT"),
        "brightWhite" to listOf("CONSOLE_WHITE_OUTPUT")
    )

    /**
     * ANSI color code mapping (for reference and validation)
     */
    val ansiColorCodes = mapOf(
        "black" to 30,
        "red" to 31,
        "green" to 32,
        "yellow" to 33,
        "blue" to 34,
        "purple" to 35,
        "cyan" to 36,
        "white" to 37,
        "brightBlack" to 90,
        "brightRed" to 91,
        "brightGreen" to 92,
        "brightYellow" to 93,
        "brightBlue" to 94,
        "brightPurple" to 95,
        "brightCyan" to 96,
        "brightWhite" to 97
    )

    // ========== SYNTAX COLOR INFERENCE RULES ==========

    /**
     * Maps Windows Terminal colors to IntelliJ syntax highlighting attributes.
     * These are inference rules - the actual color selection depends on hue/saturation/luminance.
     */
    val syntaxInferenceRules = mapOf(
        // Comments (typically low-brightness, desaturated)
        "COMMENT" to SyntaxRule(
            priority = Priority.HIGH,
            preferredSources = listOf("brightBlack", "white"),
            hueRange = null,
            luminanceClass = LuminanceClass.DARK,
            applyDimming = 0.7
        ),

        "DOC_COMMENT" to SyntaxRule(
            priority = Priority.MEDIUM,
            preferredSources = listOf("brightBlack", "white"),
            hueRange = null,
            luminanceClass = LuminanceClass.DARK,
            applyDimming = 0.85,
            inheritFrom = "COMMENT"
        ),

        // Keywords (typically blue/purple)
        "KEYWORD" to SyntaxRule(
            priority = Priority.HIGH,
            preferredSources = listOf("blue", "brightBlue", "purple"),
            hueRange = 200.0..280.0,  // Blue to purple spectrum
            luminanceClass = null
        ),

        "RESERVED_WORD" to SyntaxRule(
            priority = Priority.HIGH,
            preferredSources = listOf("blue", "brightBlue"),
            hueRange = 200.0..260.0,
            inheritFrom = "KEYWORD"
        ),

        // Strings (typically green)
        "STRING" to SyntaxRule(
            priority = Priority.HIGH,
            preferredSources = listOf("green", "brightGreen"),
            hueRange = 80.0..160.0,  // Green spectrum
            luminanceClass = null
        ),

        "VALID_STRING_ESCAPE" to SyntaxRule(
            priority = Priority.MEDIUM,
            preferredSources = listOf("brightGreen", "cyan"),
            hueRange = 80.0..200.0,
            applyLightening = 0.2,
            inheritFrom = "STRING"
        ),

        // Numbers (typically yellow/cyan)
        "NUMBER" to SyntaxRule(
            priority = Priority.MEDIUM,
            preferredSources = listOf("yellow", "brightYellow", "cyan"),
            hueRange = 40.0..200.0,
            luminanceClass = null
        ),

        // Functions (typically cyan/blue)
        "FUNCTION_CALL" to SyntaxRule(
            priority = Priority.MEDIUM,
            preferredSources = listOf("cyan", "brightCyan", "blue"),
            hueRange = 170.0..220.0,
            luminanceClass = null
        ),

        "FUNCTION_DECLARATION" to SyntaxRule(
            priority = Priority.MEDIUM,
            preferredSources = listOf("cyan", "brightCyan"),
            hueRange = 170.0..220.0,
            fontStyle = FontStyle.BOLD,
            inheritFrom = "FUNCTION_CALL"
        ),

        // Classes/Types (typically yellow/orange)
        "CLASS_NAME" to SyntaxRule(
            priority = Priority.MEDIUM,
            preferredSources = listOf("yellow", "brightYellow"),
            hueRange = 30.0..70.0,
            fontStyle = FontStyle.BOLD
        ),

        // Errors (red)
        "ERRORS_ATTRIBUTES" to SyntaxRule(
            priority = Priority.HIGH,
            preferredSources = listOf("red", "brightRed"),
            hueRange = 350.0..20.0,  // Red spectrum (wraps around)
            luminanceClass = null
        ),

        "WRONG_REFERENCES_ATTRIBUTES" to SyntaxRule(
            priority = Priority.HIGH,
            preferredSources = listOf("red", "brightRed"),
            hueRange = 350.0..20.0,
            effectType = EffectType.WAVE_UNDERSCORE,
            inheritFrom = "ERRORS_ATTRIBUTES"
        ),

        // Constants/Enums (purple/magenta)
        "CONSTANT" to SyntaxRule(
            priority = Priority.MEDIUM,
            preferredSources = listOf("purple", "brightPurple"),
            hueRange = 280.0..330.0,
            luminanceClass = null
        ),

        // Warnings (yellow/orange)
        "WARNING_ATTRIBUTES" to SyntaxRule(
            priority = Priority.MEDIUM,
            preferredSources = listOf("yellow", "brightYellow"),
            hueRange = 30.0..60.0,
            luminanceClass = null
        ),

        // Identifiers (typically foreground or subtle variation)
        "IDENTIFIER" to SyntaxRule(
            priority = Priority.LOW,
            preferredSources = listOf("foreground", "white"),
            hueRange = null,
            luminanceClass = null
        )
    )

    // ========== COLOR CLASSIFICATION ==========

    /**
     * Luminance thresholds for color classification
     */
    const val DARK_LUMINANCE_MAX = 100.0
    const val MID_LUMINANCE_MAX = 155.0
    const val BRIGHT_LUMINANCE_MIN = 155.0

    /**
     * Saturation thresholds for grayscale detection
     */
    const val GRAYSCALE_SATURATION_MAX = 0.15  // 15%
    const val DESATURATED_SATURATION_MAX = 0.40  // 40%

    // ========== EDGE CASE THRESHOLDS ==========

    /**
     * Threshold for detecting monochrome palettes (5% luminance variation)
     */
    const val MONOCHROME_THRESHOLD = 0.05

    /**
     * WCAG contrast ratio thresholds
     */
    const val HIGH_CONTRAST_THRESHOLD = 7.0  // WCAG AAA
    const val LOW_CONTRAST_THRESHOLD = 3.0   // Below WCAG AA

    /**
     * Threshold for limited palette detection (number of unique hues)
     */
    const val LIMITED_PALETTE_HUE_COUNT = 3

    /**
     * Brightness uniformity threshold (80% of colors in same luminance class)
     */
    const val BRIGHTNESS_UNIFORMITY_THRESHOLD = 0.8

    // ========== FALLBACK STRATEGIES ==========

    /**
     * Default colors to use when Windows Terminal colors are invalid or missing
     */
    val defaultColors = mapOf(
        "background" to "#282c34",
        "foreground" to "#abb2bf",
        "black" to "#000000",
        "red" to "#e06c75",
        "green" to "#98c379",
        "yellow" to "#e5c07b",
        "blue" to "#61afef",
        "purple" to "#c678dd",
        "cyan" to "#56b6c2",
        "white" to "#abb2bf",
        "brightBlack" to "#5c6370",
        "brightRed" to "#ff6c6b",
        "brightGreen" to "#b5cea8",
        "brightYellow" to "#ffd700",
        "brightBlue" to "#61afef",
        "brightPurple" to "#c678dd",
        "brightCyan" to "#4ec9b0",
        "brightWhite" to "#ffffff"
    )

    /**
     * Fallback strategy for missing cursor color: use foreground
     */
    fun getFallbackCursorColor(foreground: String): String = foreground

    /**
     * Fallback strategy for missing selection background: blend background and foreground
     */
    fun getFallbackSelectionBackground(background: String, foreground: String): String {
        // Simple blend: 80% background + 20% foreground
        return blendColors(background, foreground, 0.2)
    }

    /**
     * Simple color blending for fallbacks
     */
    private fun blendColors(color1: String, color2: String, ratio: Double): String {
        fun hexToRgb(hex: String): Triple<Int, Int, Int> {
            val cleanHex = hex.removePrefix("#")
            return Triple(
                cleanHex.substring(0, 2).toInt(16),
                cleanHex.substring(2, 4).toInt(16),
                cleanHex.substring(4, 6).toInt(16)
            )
        }

        fun rgbToHex(r: Int, g: Int, b: Int): String {
            return "#%02x%02x%02x".format(r, g, b)
        }

        val (r1, g1, b1) = hexToRgb(color1)
        val (r2, g2, b2) = hexToRgb(color2)

        val r = (r1 * (1 - ratio) + r2 * ratio).toInt().coerceIn(0, 255)
        val g = (g1 * (1 - ratio) + g2 * ratio).toInt().coerceIn(0, 255)
        val b = (b1 * (1 - ratio) + b2 * ratio).toInt().coerceIn(0, 255)

        return rgbToHex(r, g, b)
    }

    /**
     * Gets the IntelliJ attributes for a Windows Terminal property
     */
    fun getIntelliJAttributes(wtProperty: String): List<String> {
        return consoleColorMappings[wtProperty] ?: emptyList()
    }

    /**
     * Gets the syntax rule for an IntelliJ attribute
     */
    fun getSyntaxRule(attribute: String): SyntaxRule? {
        return syntaxInferenceRules[attribute]
    }
}

// ========== DATA CLASSES ==========

/**
 * Rule for inferring syntax colors from Windows Terminal colors
 */
data class SyntaxRule(
    val priority: Priority,
    val preferredSources: List<String>,
    val hueRange: ClosedFloatingPointRange<Double>?,
    val luminanceClass: LuminanceClass? = null,
    val fontStyle: FontStyle? = null,
    val effectType: EffectType? = null,
    val applyDimming: Double? = null,
    val applyLightening: Double? = null,
    val inheritFrom: String? = null
)

/**
 * Priority levels for syntax color inference
 */
enum class Priority {
    HIGH,    // Must have accurate color mapping
    MEDIUM,  // Important but can have fallbacks
    LOW      // Can use default/inherited colors
}

/**
 * Luminance classification for color selection
 */
enum class LuminanceClass {
    DARK,    // 0-100
    MID,     // 100-155
    BRIGHT   // 155-255
}

/**
 * Font styles for syntax elements (when colors are insufficient)
 */
enum class FontStyle {
    REGULAR,
    BOLD,
    ITALIC,
    BOLD_ITALIC
}

/**
 * Text effect types for special highlighting
 */
enum class EffectType {
    WAVE_UNDERSCORE,
    BOLD_LINE_UNDERSCORE,
    STRIKEOUT,
    BOXED,
    ROUNDED_BOX
}
