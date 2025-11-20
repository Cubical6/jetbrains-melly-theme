package colorschemes

/**
 * Data class representing a Windows Terminal color scheme.
 *
 * Conforms to the Windows Terminal color scheme JSON format:
 * https://learn.microsoft.com/en-us/windows/terminal/customize-settings/color-schemes
 *
 * @property name The unique name of the color scheme
 * @property background Terminal background color (#RRGGBB)
 * @property foreground Default text color (#RRGGBB)
 * @property cursorColor Cursor color (optional, defaults to foreground)
 * @property selectionBackground Selection highlight color (optional, defaults to blend)
 * @property black ANSI color 0 (black)
 * @property red ANSI color 1 (red)
 * @property green ANSI color 2 (green)
 * @property yellow ANSI color 3 (yellow)
 * @property blue ANSI color 4 (blue)
 * @property purple ANSI color 5 (magenta/purple)
 * @property cyan ANSI color 6 (cyan)
 * @property white ANSI color 7 (white/gray)
 * @property brightBlack ANSI color 8 (bright black/dark gray)
 * @property brightRed ANSI color 9 (bright red)
 * @property brightGreen ANSI color 10 (bright green)
 * @property brightYellow ANSI color 11 (bright yellow)
 * @property brightBlue ANSI color 12 (bright blue)
 * @property brightPurple ANSI color 13 (bright magenta/purple)
 * @property brightCyan ANSI color 14 (bright cyan)
 * @property brightWhite ANSI color 15 (bright white)
 */
data class WindowsTerminalColorScheme(
    // Required properties
    val name: String,
    val background: String,
    val foreground: String,

    // ANSI colors (8 normal colors) - required
    val black: String,
    val red: String,
    val green: String,
    val yellow: String,
    val blue: String,
    val purple: String,
    val cyan: String,
    val white: String,

    // ANSI bright colors (8 bright colors) - required
    val brightBlack: String,
    val brightRed: String,
    val brightGreen: String,
    val brightYellow: String,
    val brightBlue: String,
    val brightPurple: String,
    val brightCyan: String,
    val brightWhite: String,

    // Optional properties
    val cursorColor: String? = null,
    val selectionBackground: String? = null
) {
    companion object {
        private val HEX_COLOR_REGEX = Regex("^#[0-9A-Fa-f]{6}$")

        /**
         * List of all required property names for validation
         */
        val REQUIRED_PROPERTIES = listOf(
            "name", "background", "foreground",
            "black", "red", "green", "yellow", "blue", "purple", "cyan", "white",
            "brightBlack", "brightRed", "brightGreen", "brightYellow",
            "brightBlue", "brightPurple", "brightCyan", "brightWhite"
        )

        /**
         * List of optional property names
         */
        val OPTIONAL_PROPERTIES = listOf("cursorColor", "selectionBackground")
    }

    /**
     * Validates that a color string is in valid hex format (#RRGGBB)
     */
    fun isValidHexColor(color: String): Boolean {
        return HEX_COLOR_REGEX.matches(color)
    }

    /**
     * Validates the entire color scheme and returns a list of validation errors.
     * Empty list means the scheme is valid.
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        // Validate name
        if (name.isBlank()) {
            errors.add("Name cannot be blank")
        }

        // Validate all required colors
        val colorProperties = mapOf(
            "background" to background,
            "foreground" to foreground,
            "black" to black,
            "red" to red,
            "green" to green,
            "yellow" to yellow,
            "blue" to blue,
            "purple" to purple,
            "cyan" to cyan,
            "white" to white,
            "brightBlack" to brightBlack,
            "brightRed" to brightRed,
            "brightGreen" to brightGreen,
            "brightYellow" to brightYellow,
            "brightBlue" to brightBlue,
            "brightPurple" to brightPurple,
            "brightCyan" to brightCyan,
            "brightWhite" to brightWhite
        )

        colorProperties.forEach { (propertyName, colorValue) ->
            if (!isValidHexColor(colorValue)) {
                errors.add("Invalid hex color for $propertyName: $colorValue (expected #RRGGBB format)")
            }
        }

        // Validate optional colors if present
        cursorColor?.let { color ->
            if (!isValidHexColor(color)) {
                errors.add("Invalid hex color for cursorColor: $color (expected #RRGGBB format)")
            }
        }

        selectionBackground?.let { color ->
            if (!isValidHexColor(color)) {
                errors.add("Invalid hex color for selectionBackground: $color (expected #RRGGBB format)")
            }
        }

        return errors
    }

    /**
     * Converts the color scheme to a color palette map compatible with ThemeConstructor.
     * Maps Windows Terminal property names to their hex color values.
     */
    fun toColorPalette(): Map<String, String> {
        return buildMap {
            // Required colors
            put("wt_background", background)
            put("wt_foreground", foreground)
            put("wt_black", black)
            put("wt_red", red)
            put("wt_green", green)
            put("wt_yellow", yellow)
            put("wt_blue", blue)
            put("wt_purple", purple)
            put("wt_cyan", cyan)
            put("wt_white", white)
            put("wt_brightBlack", brightBlack)
            put("wt_brightRed", brightRed)
            put("wt_brightGreen", brightGreen)
            put("wt_brightYellow", brightYellow)
            put("wt_brightBlue", brightBlue)
            put("wt_brightPurple", brightPurple)
            put("wt_brightCyan", brightCyan)
            put("wt_brightWhite", brightWhite)

            // Optional colors with fallbacks
            put("wt_cursorColor", cursorColor ?: foreground)
            put("wt_selectionBackground", selectionBackground ?: blendColors(background, foreground, 0.2))
        }
    }

    /**
     * Returns all colors (required and optional) as a list of hex strings.
     * Useful for color analysis and classification.
     */
    fun getAllColors(): List<String> {
        return listOf(
            background, foreground,
            black, red, green, yellow, blue, purple, cyan, white,
            brightBlack, brightRed, brightGreen, brightYellow,
            brightBlue, brightPurple, brightCyan, brightWhite
        )
    }

    /**
     * Returns ANSI colors only (16 colors) as a map of ANSI code to hex color.
     */
    fun getAnsiColors(): Map<Int, String> {
        return mapOf(
            30 to black,
            31 to red,
            32 to green,
            33 to yellow,
            34 to blue,
            35 to purple,
            36 to cyan,
            37 to white,
            90 to brightBlack,
            91 to brightRed,
            92 to brightGreen,
            93 to brightYellow,
            94 to brightBlue,
            95 to brightPurple,
            96 to brightCyan,
            97 to brightWhite
        )
    }

    /**
     * Simple color blending for fallback selection background.
     * Blends color1 and color2 with the given ratio (0.0 = all color1, 1.0 = all color2).
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
}
