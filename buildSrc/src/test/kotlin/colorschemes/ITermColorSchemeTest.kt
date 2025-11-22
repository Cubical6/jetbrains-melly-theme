package colorschemes

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ITermColorSchemeTest {

    @Test
    fun `toHexString converts float RGB to hex correctly`() {
        val color = ITermColorScheme.ITermColor(
            red = 0.29f,    // ~74 = 0x4A
            green = 0.18f,  // ~46 = 0x2E
            blue = 0.44f    // ~112 = 0x70
        )

        val hex = color.toHexString()
        assertEquals("#4A2E70", hex)
    }

    @Test
    fun `toHexString handles edge cases`() {
        // Pure black
        assertEquals("#000000", ITermColorScheme.ITermColor(0f, 0f, 0f).toHexString())

        // Pure white
        assertEquals("#FFFFFF", ITermColorScheme.ITermColor(1f, 1f, 1f).toHexString())

        // Pure red
        assertEquals("#FF0000", ITermColorScheme.ITermColor(1f, 0f, 0f).toHexString())
    }

    @Test
    fun `fromHex converts hex to float RGB correctly`() {
        val color = ITermColorScheme.ITermColor.fromHex("#4A2E70")

        // Allow small floating point errors
        assertEquals(0.29f, color.red, 0.01f)
        assertEquals(0.18f, color.green, 0.01f)
        assertEquals(0.44f, color.blue, 0.01f)
    }

    @Test
    fun `fromHex handles with and without hash prefix`() {
        val color1 = ITermColorScheme.ITermColor.fromHex("#FF6B6B")
        val color2 = ITermColorScheme.ITermColor.fromHex("FF6B6B")

        assertEquals(color1.red, color2.red, 0.001f)
        assertEquals(color1.green, color2.green, 0.001f)
        assertEquals(color1.blue, color2.blue, 0.001f)
    }

    @Test
    fun `ITermColor validates range`() {
        assertThrows(IllegalArgumentException::class.java) {
            ITermColorScheme.ITermColor(-0.1f, 0.5f, 0.5f)
        }

        assertThrows(IllegalArgumentException::class.java) {
            ITermColorScheme.ITermColor(0.5f, 1.1f, 0.5f)
        }
    }

    @Test
    fun `validate detects missing ANSI colors`() {
        val scheme = ITermColorScheme(
            name = "Incomplete",
            ansiColors = mapOf(
                0 to ITermColorScheme.ITermColor(0f, 0f, 0f),
                1 to ITermColorScheme.ITermColor(1f, 0f, 0f)
                // Missing 2-15
            ),
            foreground = ITermColorScheme.ITermColor(1f, 1f, 1f),
            background = ITermColorScheme.ITermColor(0f, 0f, 0f),
            selection = ITermColorScheme.ITermColor(0.5f, 0.5f, 0.5f),
            cursor = ITermColorScheme.ITermColor(1f, 1f, 1f)
        )

        val errors = scheme.validate()
        assertEquals(14, errors.size) // Missing colors 2-15
        assertTrue(errors.any { it.contains("ANSI color 2") })
    }
}
