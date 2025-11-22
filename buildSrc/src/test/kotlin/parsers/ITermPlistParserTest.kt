package parsers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class ITermPlistParserTest {

    @Test
    fun `parse valid itermcolors file`() {
        val file = File("src/test/resources/test-scheme.itermcolors")
        val scheme = ITermPlistParser.parse(file)

        assertEquals("test-scheme", scheme.name)

        // Verify ANSI colors parsed
        assertEquals(16, scheme.ansiColors.size)

        // Verify Ansi 0 Color (background equivalent)
        val ansi0 = scheme.ansiColors[0]!!
        assertEquals("#1D1F28", ansi0.toHexString())

        // Verify Ansi 1 Color (red)
        val ansi1 = scheme.ansiColors[1]!!
        assertEquals("#FF6B6B", ansi1.toHexString())

        // Verify foreground
        assertEquals("#D0D0D9", scheme.foreground.toHexString())

        // Verify background
        assertEquals("#1D1F28", scheme.background.toHexString())

        // Verify selection
        assertEquals("#70618D", scheme.selection.toHexString())

        // Verify cursor
        assertEquals("#FFFFFF", scheme.cursor.toHexString())
    }

    @Test
    fun `parse fails for missing file`() {
        assertThrows(IllegalArgumentException::class.java) {
            ITermPlistParser.parse(File("nonexistent.itermcolors"))
        }
    }

    @Test
    fun `parse fails for wrong extension`() {
        val file = File.createTempFile("test", ".txt")
        assertThrows(IllegalArgumentException::class.java) {
            ITermPlistParser.parse(file)
        }
        file.delete()
    }
}
