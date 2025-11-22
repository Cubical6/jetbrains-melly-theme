package colorschemes

import utils.ColorUtils

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
     *
     * In addition to the base Windows Terminal colors, this method generates derived
     * color placeholders for improved theme consistency:
     *
     * Surface colors (derived from background):
     * - wt_surface: Slightly lighter background for elevated surfaces
     * - wt_surface_light: Moderately lighter background for secondary UI elements
     * - wt_surface_lighter: Even lighter background for highlighted areas
     *
     * UI helper colors (derived from background and foreground):
     * - wt_line_numbers: Subdued color for line numbers
     * - wt_guide_color: Subtle color for indent guides
     * - wt_divider_color: Color for visual dividers and separators
     * - wt_muted_foreground: Dimmed foreground for less important text
     *
     * Semantic colors (derived from background and ANSI colors):
     * - wt_error_background: Background for error highlights
     * - wt_warning_background: Background for warning highlights
     * - wt_info_background: Background for info highlights
     *
     * All derived colors are calculated automatically using ColorUtils methods.
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
            put("wt_selectionBackground", selectionBackground ?: ColorUtils.blend(background, foreground, 0.2))

            // UI Surface colors (afgeleide kleuren voor betere theme consistency)
            put("wt_surface", ColorUtils.lighten(background, 0.05))
            put("wt_surface_light", ColorUtils.lighten(background, 0.10))
            put("wt_surface_lighter", ColorUtils.lighten(background, 0.15))

            // Text/UI helper colors
            put("wt_line_numbers", ColorUtils.blend(background, foreground, 0.30))
            put("wt_guide_color", ColorUtils.blend(background, foreground, 0.15))
            put("wt_divider_color", ColorUtils.blend(background, foreground, 0.25))
            put("wt_muted_foreground", ColorUtils.blend(background, foreground, 0.60))

            // Semantic colors voor errors/warnings/info
            put("wt_error_background", ColorUtils.blend(background, red, 0.20))
            put("wt_warning_background", ColorUtils.blend(background, yellow, 0.20))
            put("wt_info_background", ColorUtils.blend(background, blue, 0.20))
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
}
