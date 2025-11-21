package mapping

import colorschemes.WindowsTerminalColorScheme
import utils.ColorUtils

/**
 * Maps Windows Terminal color schemes to IntelliJ console color attributes.
 *
 * Handles:
 * - All 16 ANSI colors (black, red, green, yellow, blue, purple/magenta, cyan, white + bright variants)
 * - 4 special colors (background, foreground, cursorColor, selectionBackground)
 * - Fallback strategies for optional colors
 * - Color format normalization (#RRGGBB)
 *
 * Based on:
 * - COLOR_MAPPING_SPEC.yaml
 * - IntelliJ console color documentation
 * - Windows Terminal color scheme format
 *
 * @property config Color mapping configuration with console mappings and fallback strategies
 */
class ConsoleColorMapper(private val config: ColorMappingConfig) {

    /**
     * Maps a Windows Terminal color scheme to IntelliJ console color attributes.
     *
     * @param scheme Windows Terminal color scheme to map
     * @return Map of IntelliJ console attribute names to hex colors (#RRGGBB format)
     */
    fun mapToConsoleColors(scheme: WindowsTerminalColorScheme): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // Map all ANSI colors (16 colors)
        // Normal colors (ANSI 30-37)
        result["CONSOLE_BLACK_OUTPUT"] = normalizeColor(scheme.black)
        result["CONSOLE_RED_OUTPUT"] = normalizeColor(scheme.red)
        result["CONSOLE_GREEN_OUTPUT"] = normalizeColor(scheme.green)
        result["CONSOLE_YELLOW_OUTPUT"] = normalizeColor(scheme.yellow)
        result["CONSOLE_BLUE_OUTPUT"] = normalizeColor(scheme.blue)
        result["CONSOLE_MAGENTA_OUTPUT"] = normalizeColor(scheme.purple)  // Windows Terminal "purple" = ANSI magenta
        result["CONSOLE_CYAN_OUTPUT"] = normalizeColor(scheme.cyan)
        result["CONSOLE_GRAY_OUTPUT"] = normalizeColor(scheme.white)  // Windows Terminal "white" = ANSI gray

        // Bright colors (ANSI 90-97)
        result["CONSOLE_DARKGRAY_OUTPUT"] = normalizeColor(scheme.brightBlack)
        result["CONSOLE_RED_BRIGHT_OUTPUT"] = normalizeColor(scheme.brightRed)
        result["CONSOLE_GREEN_BRIGHT_OUTPUT"] = normalizeColor(scheme.brightGreen)
        result["CONSOLE_YELLOW_BRIGHT_OUTPUT"] = normalizeColor(scheme.brightYellow)
        result["CONSOLE_BLUE_BRIGHT_OUTPUT"] = normalizeColor(scheme.brightBlue)
        result["CONSOLE_MAGENTA_BRIGHT_OUTPUT"] = normalizeColor(scheme.brightPurple)
        result["CONSOLE_CYAN_BRIGHT_OUTPUT"] = normalizeColor(scheme.brightCyan)
        result["CONSOLE_WHITE_OUTPUT"] = normalizeColor(scheme.brightWhite)

        // Map special colors
        result["CONSOLE_BACKGROUND_KEY"] = normalizeColor(scheme.background)
        result["CONSOLE_NORMAL_OUTPUT"] = normalizeColor(scheme.foreground)
        result["FOREGROUND"] = normalizeColor(scheme.foreground)

        // Handle optional colors with fallback strategies
        result["CARET_COLOR"] = normalizeColor(
            scheme.cursorColor ?: config.getFallbackCursorColor(scheme.foreground)
        )
        result["CONSOLE_CURSOR"] = normalizeColor(
            scheme.cursorColor ?: config.getFallbackCursorColor(scheme.foreground)
        )

        result["CONSOLE_SELECTION_BACKGROUND"] = normalizeColor(
            scheme.selectionBackground ?: config.getFallbackSelectionBackground(
                scheme.background,
                scheme.foreground
            )
        )

        return result
    }

    /**
     * Normalizes a color to #RRGGBB format.
     * Handles various input formats and ensures consistent output.
     *
     * @param color Color string (e.g., "#RGB", "#RRGGBB", "RRGGBB")
     * @return Normalized color in #RRGGBB format (lowercase)
     */
    private fun normalizeColor(color: String): String {
        var normalized = color.trim()

        // Add # prefix if missing
        if (!normalized.startsWith("#")) {
            normalized = "#$normalized"
        }

        // Handle #RGB format (expand to #RRGGBB)
        if (normalized.length == 4) {
            val r = normalized[1]
            val g = normalized[2]
            val b = normalized[3]
            normalized = "#$r$r$g$g$b$b"
        }

        // Validate format
        require(normalized.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
            "Invalid color format: $color (expected #RRGGBB)"
        }

        // Return lowercase for consistency
        return normalized.lowercase()
    }
}
