package converters

import colorschemes.ITermColorScheme
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import parsers.ITermPlistParser
import java.io.File

class ITermToWindowsTerminalConverterTest {

    @Test
    fun `convert iTerm scheme to Windows Terminal scheme`() {
        // Parse test iTerm scheme
        val iTermFile = File("src/test/resources/test-scheme.itermcolors")
        val iTermScheme = ITermPlistParser.parse(iTermFile)

        // Convert to Windows Terminal
        val wtScheme = ITermToWindowsTerminalConverter.convert(iTermScheme)

        // Verify basic properties
        assertEquals("test-scheme", wtScheme.name)
        assertEquals("#1D1F28", wtScheme.background)
        assertEquals("#D0D0D9", wtScheme.foreground)

        // Verify ANSI colors mapping
        assertEquals("#1D1F28", wtScheme.black)       // ANSI 0
        assertEquals("#FF6B6B", wtScheme.red)         // ANSI 1
        assertEquals(16, getAllAnsiColors(wtScheme).size) // All 16 colors present

        // Verify optional colors
        assertEquals("#FFFFFF", wtScheme.cursorColor)
        assertEquals("#70618D", wtScheme.selectionBackground)
    }

    @Test
    fun `convert preserves all ANSI colors`() {
        val iTermFile = File("src/test/resources/test-scheme.itermcolors")
        val iTermScheme = ITermPlistParser.parse(iTermFile)
        val wtScheme = ITermToWindowsTerminalConverter.convert(iTermScheme)

        // Verify all 16 ANSI colors are present and valid hex colors
        val ansiColors = getAllAnsiColors(wtScheme)
        assertEquals(16, ansiColors.size)

        ansiColors.forEach { color ->
            assertTrue(color.matches(Regex("^#[0-9A-F]{6}$")),
                "Color $color should be valid hex format")
        }
    }

    @Test
    fun `converter validates color hex format`() {
        val iTermFile = File("src/test/resources/test-scheme.itermcolors")
        val iTermScheme = ITermPlistParser.parse(iTermFile)
        val wtScheme = ITermToWindowsTerminalConverter.convert(iTermScheme)

        // Validate that all colors are in uppercase hex format
        val errors = wtScheme.validate()
        assertTrue(errors.isEmpty(), "Converted scheme should be valid: $errors")
    }

    private fun getAllAnsiColors(scheme: colorschemes.WindowsTerminalColorScheme): List<String> {
        return listOf(
            scheme.black, scheme.red, scheme.green, scheme.yellow,
            scheme.blue, scheme.purple, scheme.cyan, scheme.white,
            scheme.brightBlack, scheme.brightRed, scheme.brightGreen, scheme.brightYellow,
            scheme.brightBlue, scheme.brightPurple, scheme.brightCyan, scheme.brightWhite
        )
    }
}
