package generators

import colorschemes.WindowsTerminalColorScheme
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.collections.shouldContainAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.deleteIfExists

class XMLColorSchemeGeneratorTest {

    private lateinit var generator: XMLColorSchemeGenerator
    private lateinit var tempOutputPath: Path

    @BeforeEach
    fun setup() {
        generator = XMLColorSchemeGenerator()
        tempOutputPath = Files.createTempFile("test-color-scheme", ".xml")
    }

    @AfterEach
    fun cleanup() {
        tempOutputPath.deleteIfExists()
    }

    // ========== BASIC GENERATION TESTS ==========

    @Test
    fun `generate creates valid XML file`() {
        val scheme = createTestScheme()

        generator.generate(scheme, tempOutputPath)

        // File should exist
        tempOutputPath.exists() shouldBe true

        // File should contain valid XML
        val content = Files.readString(tempOutputPath)
        content.shouldContain("<?xml")
        content.shouldContain("<scheme")
        content.shouldContain("</scheme>")
    }

    @Test
    fun `generate replaces SCHEME_NAME placeholder`() {
        val scheme = createTestScheme()

        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)
        content.shouldContain("name=\"One Dark Pro\"")
        content.shouldNotContain("\$SCHEME_NAME$")
    }

    @Test
    fun `generate replaces all wt_ placeholders`() {
        val scheme = createTestScheme()

        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)

        // No placeholders should remain
        content.shouldNotContain("\$wt_background$")
        content.shouldNotContain("\$wt_foreground$")
        content.shouldNotContain("\$wt_black$")
        content.shouldNotContain("\$wt_red$")
        content.shouldNotContain("\$wt_green$")
        content.shouldNotContain("\$wt_yellow$")
        content.shouldNotContain("\$wt_blue$")
        content.shouldNotContain("\$wt_magenta$")
        content.shouldNotContain("\$wt_cyan$")
        content.shouldNotContain("\$wt_white$")
        content.shouldNotContain("\$wt_bright_black$")
        content.shouldNotContain("\$wt_bright_red$")
        content.shouldNotContain("\$wt_bright_green$")
        content.shouldNotContain("\$wt_bright_yellow$")
        content.shouldNotContain("\$wt_bright_blue$")
        content.shouldNotContain("\$wt_bright_magenta$")
        content.shouldNotContain("\$wt_bright_cyan$")
        content.shouldNotContain("\$wt_bright_white$")
    }

    @Test
    fun `generate normalizes colors to RRGGBB format without hash`() {
        val scheme = createTestScheme()

        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)

        // Colors should be normalized (without #)
        content.shouldContain("282c34")  // background
        content.shouldContain("abb2bf")  // foreground
        content.shouldContain("e06c75")  // red
        content.shouldContain("98c379")  // green

        // Should not contain colors with # prefix in value attributes
        // (checking specific examples from the scheme)
        content.shouldNotContain("value=\"#282c34\"")
        content.shouldNotContain("value=\"#e06c75\"")
    }

    // ========== COLOR MAPPING TESTS ==========

    @Test
    fun `generate correctly maps purple to magenta placeholder`() {
        val scheme = createTestScheme()

        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)

        // Windows Terminal "purple" should replace "wt_magenta"
        content.shouldContain("c678dd")  // purple color value
    }

    @Test
    fun `generate correctly maps brightPurple to bright_magenta placeholder`() {
        val scheme = createTestScheme()

        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)

        // Windows Terminal "brightPurple" should replace "wt_bright_magenta"
        content.shouldContain("c678dd")  // brightPurple color value
    }

    @Test
    fun `generate handles all ANSI colors`() {
        val scheme = createTestScheme()

        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)

        // All ANSI colors should be present
        content.shouldContain("000000")  // black
        content.shouldContain("e06c75")  // red
        content.shouldContain("98c379")  // green
        content.shouldContain("e5c07b")  // yellow
        content.shouldContain("61afef")  // blue
        content.shouldContain("56b6c2")  // cyan
        content.shouldContain("ffffff")  // brightWhite
    }

    // ========== PREVIEW GENERATION TESTS ==========

    @Test
    fun `generatePreview returns valid XML without writing file`() {
        val scheme = createTestScheme()

        val preview = generator.generatePreview(scheme)

        // Should be valid XML
        preview.shouldContain("<?xml")
        preview.shouldContain("<scheme")
        preview.shouldContain("</scheme>")

        // Should replace placeholders
        preview.shouldContain("name=\"One Dark Pro\"")
        preview.shouldNotContain("\$SCHEME_NAME$")
        preview.shouldNotContain("\$wt_background$")
    }

    @Test
    fun `generatePreview and generate produce identical output`() {
        val scheme = createTestScheme()

        val preview = generator.generatePreview(scheme)

        generator.generate(scheme, tempOutputPath)
        val fileContent = Files.readString(tempOutputPath)

        preview shouldBe fileContent
    }

    // ========== VALIDATION TESTS ==========

    @Test
    fun `generate validates XML structure`() {
        val scheme = createTestScheme()

        // Should not throw exception for valid scheme
        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)
        // Basic XML structure checks
        content.shouldContain("<scheme")
        content.shouldContain("</scheme>")
        content.shouldContain("<colors>")
        content.shouldContain("</colors>")
        content.shouldContain("<attributes>")
        content.shouldContain("</attributes>")
    }

    @Test
    fun `generate throws exception for invalid scheme`() {
        val invalidScheme = WindowsTerminalColorScheme(
            name = "",  // Invalid empty name
            background = "#282c34",
            foreground = "#abb2bf",
            black = "#000000",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#e5c07b",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            brightBlack = "#5c6370",
            brightRed = "#ff6c6b",
            brightGreen = "#b5cea8",
            brightYellow = "#ffd700",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#4ec9b0",
            brightWhite = "#ffffff"
        )

        assertThrows<IllegalArgumentException> {
            generator.generate(invalidScheme, tempOutputPath)
        }
    }

    @Test
    fun `generatePreview throws exception for invalid scheme`() {
        val invalidScheme = WindowsTerminalColorScheme(
            name = "Test",
            background = "invalid-color",  // Invalid color format
            foreground = "#abb2bf",
            black = "#000000",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#e5c07b",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            brightBlack = "#5c6370",
            brightRed = "#ff6c6b",
            brightGreen = "#b5cea8",
            brightYellow = "#ffd700",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#4ec9b0",
            brightWhite = "#ffffff"
        )

        assertThrows<IllegalArgumentException> {
            generator.generatePreview(invalidScheme)
        }
    }

    // ========== TEMPLATE TESTS ==========

    @Test
    fun `isTemplateAvailable returns true for existing template`() {
        generator.isTemplateAvailable() shouldBe true
    }

    @Test
    fun `getExpectedPlaceholders returns all required placeholders`() {
        val placeholders = generator.getExpectedPlaceholders()

        placeholders.shouldContainAll(
            "\$SCHEME_NAME$",
            "\$wt_background$",
            "\$wt_foreground$",
            "\$wt_black$",
            "\$wt_red$",
            "\$wt_green$",
            "\$wt_yellow$",
            "\$wt_blue$",
            "\$wt_magenta$",
            "\$wt_cyan$",
            "\$wt_white$",
            "\$wt_bright_black$",
            "\$wt_bright_red$",
            "\$wt_bright_green$",
            "\$wt_bright_yellow$",
            "\$wt_bright_blue$",
            "\$wt_bright_magenta$",
            "\$wt_bright_cyan$",
            "\$wt_bright_white$"
        )
    }

    // ========== MULTIPLE SCHEMES TESTS ==========

    @Test
    fun `generate works with different color schemes`() {
        val schemes = listOf(
            createTestScheme(),
            createDarkPlusScheme(),
            createSolarizedDarkScheme()
        )

        schemes.forEach { scheme ->
            val outputPath = Files.createTempFile("test-${scheme.name}", ".xml")
            try {
                generator.generate(scheme, outputPath)

                outputPath.exists() shouldBe true

                val content = Files.readString(outputPath)
                content.shouldContain("name=\"${scheme.name}\"")
                content.shouldNotContain("\$SCHEME_NAME$")
            } finally {
                outputPath.deleteIfExists()
            }
        }
    }

    @Test
    fun `generate works with light theme`() {
        val lightScheme = WindowsTerminalColorScheme(
            name = "Light+ (default light)",
            background = "#ffffff",
            foreground = "#000000",
            black = "#000000",
            red = "#cd3131",
            green = "#00bc00",
            yellow = "#949800",
            blue = "#0451a5",
            purple = "#bc05bc",
            cyan = "#0598bc",
            white = "#555555",
            brightBlack = "#666666",
            brightRed = "#cd3131",
            brightGreen = "#14ce14",
            brightYellow = "#b5ba00",
            brightBlue = "#0451a5",
            brightPurple = "#bc05bc",
            brightCyan = "#0598bc",
            brightWhite = "#a5a5a5",
            cursorColor = "#000000",
            selectionBackground = "#add6ff"
        )

        generator.generate(lightScheme, tempOutputPath)

        tempOutputPath.exists() shouldBe true

        val content = Files.readString(tempOutputPath)
        content.shouldContain("ffffff")  // background
        content.shouldContain("000000")  // foreground
    }

    // ========== OUTPUT PATH TESTS ==========

    @Test
    fun `generate creates parent directories if they don't exist`() {
        val nestedPath = Files.createTempDirectory("test-parent")
            .resolve("nested")
            .resolve("directories")
            .resolve("scheme.xml")

        generator.generate(createTestScheme(), nestedPath)

        nestedPath.exists() shouldBe true
        nestedPath.parent.exists() shouldBe true

        // Cleanup
        var current = nestedPath
        while (current != null && current.fileName.toString() != "test-parent") {
            val parent = current.parent
            Files.deleteIfExists(current)
            current = parent
        }
    }

    @Test
    fun `generate overwrites existing file`() {
        // Create initial file
        Files.writeString(tempOutputPath, "old content")

        val scheme = createTestScheme()
        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)
        content.shouldNotContain("old content")
        content.shouldContain("<scheme")
    }

    // ========== EDGE CASES ==========

    @Test
    fun `generate handles monochrome palette`() {
        val monochromeScheme = WindowsTerminalColorScheme(
            name = "Monochrome",
            background = "#000000",
            foreground = "#ffffff",
            black = "#000000",
            red = "#444444",
            green = "#555555",
            yellow = "#666666",
            blue = "#777777",
            purple = "#888888",
            cyan = "#999999",
            white = "#aaaaaa",
            brightBlack = "#bbbbbb",
            brightRed = "#cccccc",
            brightGreen = "#dddddd",
            brightYellow = "#eeeeee",
            brightBlue = "#f0f0f0",
            brightPurple = "#f5f5f5",
            brightCyan = "#fafafa",
            brightWhite = "#ffffff"
        )

        generator.generate(monochromeScheme, tempOutputPath)

        tempOutputPath.exists() shouldBe true

        val content = Files.readString(tempOutputPath)
        content.shouldContain("000000")
        content.shouldContain("ffffff")
    }

    @Test
    fun `generate handles scheme with special characters in name`() {
        val scheme = createTestScheme().copy(name = "Test & Special <Characters>")

        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)
        // XML should escape special characters
        content.shouldContain("Test &amp; Special &lt;Characters&gt;")
    }

    @Test
    fun `generate handles scheme with optional colors missing`() {
        val schemeWithoutOptionals = WindowsTerminalColorScheme(
            name = "Minimal Scheme",
            background = "#282c34",
            foreground = "#abb2bf",
            black = "#000000",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#e5c07b",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            brightBlack = "#5c6370",
            brightRed = "#ff6c6b",
            brightGreen = "#b5cea8",
            brightYellow = "#ffd700",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#4ec9b0",
            brightWhite = "#ffffff",
            cursorColor = null,
            selectionBackground = null
        )

        generator.generate(schemeWithoutOptionals, tempOutputPath)

        tempOutputPath.exists() shouldBe true

        val content = Files.readString(tempOutputPath)
        content.shouldNotContain("\$wt_")
    }

    // ========== COLOR FORMAT TESTS ==========

    @Test
    fun `generate normalizes uppercase colors`() {
        val scheme = WindowsTerminalColorScheme(
            name = "Uppercase Colors",
            background = "#282C34",  // Uppercase
            foreground = "#ABB2BF",
            black = "#000000",
            red = "#E06C75",
            green = "#98C379",
            yellow = "#E5C07B",
            blue = "#61AFEF",
            purple = "#C678DD",
            cyan = "#56B6C2",
            white = "#ABB2BF",
            brightBlack = "#5C6370",
            brightRed = "#FF6C6B",
            brightGreen = "#B5CEA8",
            brightYellow = "#FFD700",
            brightBlue = "#61AFEF",
            brightPurple = "#C678DD",
            brightCyan = "#4EC9B0",
            brightWhite = "#FFFFFF"
        )

        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)

        // Colors should be normalized to lowercase
        content.shouldContain("282c34")
        content.shouldContain("abb2bf")
        content.shouldContain("e06c75")
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    fun `generate produces well-formed XML parseable by standard parser`() {
        val scheme = createTestScheme()

        generator.generate(scheme, tempOutputPath)

        // If this doesn't throw, the XML is well-formed
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(tempOutputPath.toFile())

        document shouldNotBe null
        document.documentElement.nodeName shouldBe "scheme"
    }

    @Test
    fun `generated XML contains expected IntelliJ structure`() {
        val scheme = createTestScheme()

        generator.generate(scheme, tempOutputPath)

        val content = Files.readString(tempOutputPath)

        // Check for expected IntelliJ XML structure
        content.shouldContain("parent_scheme=\"Darcula\"")
        content.shouldContain("<metaInfo>")
        content.shouldContain("<colors>")
        content.shouldContain("<attributes>")
        content.shouldContain("windowsTerminalScheme")
    }

    // ========== HELPER METHODS ==========

    /**
     * Creates a standard test scheme (based on One Dark Pro)
     */
    private fun createTestScheme(): WindowsTerminalColorScheme {
        return WindowsTerminalColorScheme(
            name = "One Dark Pro",
            background = "#282c34",
            foreground = "#abb2bf",
            black = "#000000",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#e5c07b",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            brightBlack = "#5c6370",
            brightRed = "#ff6c6b",
            brightGreen = "#b5cea8",
            brightYellow = "#ffd700",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#4ec9b0",
            brightWhite = "#ffffff",
            cursorColor = "#abb2bf",
            selectionBackground = "#42464f"
        )
    }

    /**
     * Creates a Dark+ test scheme
     */
    private fun createDarkPlusScheme(): WindowsTerminalColorScheme {
        return WindowsTerminalColorScheme(
            name = "Dark+ (default dark)",
            background = "#1e1e1e",
            foreground = "#d4d4d4",
            black = "#000000",
            red = "#cd3131",
            green = "#0dbc79",
            yellow = "#e5e510",
            blue = "#2472c8",
            purple = "#bc3fbc",
            cyan = "#11a8cd",
            white = "#e5e5e5",
            brightBlack = "#666666",
            brightRed = "#f14c4c",
            brightGreen = "#23d18b",
            brightYellow = "#f5f543",
            brightBlue = "#3b8eea",
            brightPurple = "#d670d6",
            brightCyan = "#29b8db",
            brightWhite = "#ffffff",
            cursorColor = "#d4d4d4",
            selectionBackground = "#264f78"
        )
    }

    /**
     * Creates a Solarized Dark test scheme
     */
    private fun createSolarizedDarkScheme(): WindowsTerminalColorScheme {
        return WindowsTerminalColorScheme(
            name = "Solarized Dark",
            background = "#002b36",
            foreground = "#839496",
            black = "#002b36",
            red = "#dc322f",
            green = "#859900",
            yellow = "#b58900",
            blue = "#268bd2",
            purple = "#d33682",
            cyan = "#2aa198",
            white = "#eee8d5",
            brightBlack = "#073642",
            brightRed = "#cb4b16",
            brightGreen = "#586e75",
            brightYellow = "#657b83",
            brightBlue = "#839496",
            brightPurple = "#6c71c4",
            brightCyan = "#93a1a1",
            brightWhite = "#fdf6e3",
            cursorColor = "#839496",
            selectionBackground = "#073642"
        )
    }
}
