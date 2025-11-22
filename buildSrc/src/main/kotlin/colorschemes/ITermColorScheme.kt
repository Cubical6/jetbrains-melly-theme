package colorschemes

import kotlin.math.roundToInt

/**
 * Data class representing an iTerm2 color scheme (.itermcolors format)
 *
 * iTerm2 uses XML plist format with RGB components as floating-point values (0.0-1.0).
 * This format is commonly used in macOS terminal applications.
 *
 * Format reference: https://iterm2.com/documentation-colors.html
 *
 * @property name The unique name of the color scheme (derived from filename, must not be blank)
 * @property ansiColors Map of ANSI colors indexed 0-15 (required for all 16 colors)
 *   - 0-7: Normal colors (black, red, green, yellow, blue, magenta, cyan, white)
 *   - 8-15: Bright variants of the same colors
 * @property foreground Default text color (required)
 * @property background Terminal background color (required)
 * @property selection Selection highlight background color (required)
 * @property cursor Cursor color (required)
 * @property cursorText Text color under cursor (optional, defaults to background)
 * @property bold Bold text color override (optional, uses foreground if not set)
 * @property link Hyperlink color (optional, uses foreground if not set)
 */
data class ITermColorScheme(
    val name: String,
    val ansiColors: Map<Int, ITermColor>,
    val foreground: ITermColor,
    val background: ITermColor,
    val selection: ITermColor,
    val cursor: ITermColor,
    val cursorText: ITermColor? = null,
    val bold: ITermColor? = null,
    val link: ITermColor? = null
) {
    data class ITermColor(
        val red: Float,   // 0.0 - 1.0
        val green: Float, // 0.0 - 1.0
        val blue: Float,  // 0.0 - 1.0
        val alpha: Float = 1.0f
    ) {
        init {
            require(red in 0.0f..1.0f) { "Red must be 0.0-1.0, got $red" }
            require(green in 0.0f..1.0f) { "Green must be 0.0-1.0, got $green" }
            require(blue in 0.0f..1.0f) { "Blue must be 0.0-1.0, got $blue" }
            require(alpha in 0.0f..1.0f) { "Alpha must be 0.0-1.0, got $alpha" }
        }

        /**
         * Convert iTerm float RGB (0.0-1.0) to hex string
         */
        fun toHexString(): String {
            val r = (red * 255).roundToInt().coerceIn(0, 255)
            val g = (green * 255).roundToInt().coerceIn(0, 255)
            val b = (blue * 255).roundToInt().coerceIn(0, 255)
            return "#%02X%02X%02X".format(r, g, b)
        }

        companion object {
            /**
             * Parse hex color to iTerm float format
             */
            fun fromHex(hex: String): ITermColor {
                val clean = hex.removePrefix("#")
                require(clean.length == 6) { "Invalid hex color: $hex" }

                val r = clean.substring(0, 2).toInt(16) / 255.0f
                val g = clean.substring(2, 4).toInt(16) / 255.0f
                val b = clean.substring(4, 6).toInt(16) / 255.0f

                return ITermColor(r, g, b)
            }
        }
    }

    /**
     * Validate that scheme has all required colors and properties
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        // Check name is not blank
        if (name.isBlank()) {
            errors.add("Color scheme name must not be blank")
        }

        // Check all 16 ANSI colors present
        for (i in 0..15) {
            if (!ansiColors.containsKey(i)) {
                errors.add("Missing ANSI color $i")
            }
        }

        return errors
    }
}
