package converters

import colorschemes.ITermColorScheme
import colorschemes.WindowsTerminalColorScheme

/**
 * Converter to transform iTerm2 color schemes to Windows Terminal format
 *
 * This converter handles:
 * - Converting iTerm's floating-point RGB (0.0-1.0) to hex strings (#RRGGBB)
 * - Mapping iTerm ANSI colors to Windows Terminal property names
 * - Converting special colors (foreground, background, cursor, selection)
 */
object ITermToWindowsTerminalConverter {

    /**
     * Convert an ITermColorScheme to WindowsTerminalColorScheme
     *
     * @param iTermScheme The iTerm2 color scheme to convert
     * @return A Windows Terminal compatible color scheme
     */
    fun convert(iTermScheme: ITermColorScheme): WindowsTerminalColorScheme {
        // Helper function to safely get ANSI color by index
        fun getAnsiColor(index: Int): String {
            val color = iTermScheme.ansiColors[index]
                ?: throw IllegalArgumentException("Missing ANSI color $index in scheme '${iTermScheme.name}'")
            return color.toHexString()
        }

        return WindowsTerminalColorScheme(
            name = iTermScheme.name,
            background = iTermScheme.background.toHexString(),
            foreground = iTermScheme.foreground.toHexString(),

            // ANSI colors 0-7 (normal colors)
            black = getAnsiColor(0),
            red = getAnsiColor(1),
            green = getAnsiColor(2),
            yellow = getAnsiColor(3),
            blue = getAnsiColor(4),
            purple = getAnsiColor(5),
            cyan = getAnsiColor(6),
            white = getAnsiColor(7),

            // ANSI colors 8-15 (bright colors)
            brightBlack = getAnsiColor(8),
            brightRed = getAnsiColor(9),
            brightGreen = getAnsiColor(10),
            brightYellow = getAnsiColor(11),
            brightBlue = getAnsiColor(12),
            brightPurple = getAnsiColor(13),
            brightCyan = getAnsiColor(14),
            brightWhite = getAnsiColor(15),

            // Optional colors
            cursorColor = iTermScheme.cursor.toHexString(),
            selectionBackground = iTermScheme.selection.toHexString()
        )
    }
}
