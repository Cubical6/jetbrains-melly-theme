package colorschemes

import kotlin.math.roundToInt

/**
 * Represents an iTerm2 color scheme (.itermcolors format)
 * iTerm uses XML plist format with RGB components as floats (0.0-1.0)
 */
data class ITermColorScheme(
    val name: String,
    val ansiColors: Map<Int, ITermColor>, // 0-15 for ANSI colors
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
     * Validate that scheme has all required colors
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        // Check all 16 ANSI colors present
        for (i in 0..15) {
            if (!ansiColors.containsKey(i)) {
                errors.add("Missing ANSI color $i")
            }
        }

        return errors
    }
}
