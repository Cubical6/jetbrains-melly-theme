package colorschemes

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

class ColorSchemeParserTest {

    private val parser = ColorSchemeParser()

    // Get test resources directory
    private fun getTestResourcePath(fileName: String): Path {
        val resourceUrl = this::class.java.classLoader.getResource("test-schemes/$fileName")
            ?: error("Test resource not found: test-schemes/$fileName")
        return Path.of(resourceUrl.toURI())
    }

    @Test
    fun `parse valid scheme succeeds`() {
        val path = getTestResourcePath("valid-scheme.json")
        val result = parser.parse(path)

        result.isSuccess shouldBe true
        val scheme = result.getOrNull()
        scheme shouldNotBe null
        scheme!!.name shouldBe "Test Valid Scheme"
        scheme.background shouldBe "#282c34"
        scheme.foreground shouldBe "#abb2bf"
        scheme.cursorColor shouldBe "#528bff"
        scheme.selectionBackground shouldBe "#3e4451"
    }

    @Test
    fun `parse valid scheme without optional properties succeeds`() {
        val path = getTestResourcePath("valid-scheme-no-optionals.json")
        val result = parser.parse(path)

        result.isSuccess shouldBe true
        val scheme = result.getOrNull()
        scheme shouldNotBe null
        scheme!!.name shouldBe "Test Minimal Valid Scheme"
        scheme.cursorColor shouldBe null
        scheme.selectionBackground shouldBe null
    }

    @Test
    fun `parse validates all required ANSI colors`() {
        val path = getTestResourcePath("valid-scheme.json")
        val result = parser.parse(path)

        result.isSuccess shouldBe true
        val scheme = result.getOrThrow()

        // Verify all 16 ANSI colors are present
        scheme.black shouldBe "#1e2127"
        scheme.red shouldBe "#e06c75"
        scheme.green shouldBe "#98c379"
        scheme.yellow shouldBe "#e5c07b"
        scheme.blue shouldBe "#61afef"
        scheme.purple shouldBe "#c678dd"
        scheme.cyan shouldBe "#56b6c2"
        scheme.white shouldBe "#abb2bf"

        scheme.brightBlack shouldBe "#5c6370"
        scheme.brightRed shouldBe "#e06c75"
        scheme.brightGreen shouldBe "#98c379"
        scheme.brightYellow shouldBe "#e5c07b"
        scheme.brightBlue shouldBe "#61afef"
        scheme.brightPurple shouldBe "#c678dd"
        scheme.brightCyan shouldBe "#56b6c2"
        scheme.brightWhite shouldBe "#ffffff"
    }

    @Test
    fun `parse invalid JSON fails with appropriate error`() {
        val path = getTestResourcePath("invalid-json.json")
        val result = parser.parse(path)

        result.isFailure shouldBe true
        val exception = result.exceptionOrNull()
        exception shouldNotBe null
        exception shouldBe ParserException::class.java
        exception!!.message shouldContain "Invalid JSON syntax"
    }

    @Test
    fun `parse scheme with missing properties fails`() {
        val path = getTestResourcePath("missing-properties.json")
        val result = parser.parse(path)

        result.isFailure shouldBe true
        val exception = result.exceptionOrNull()
        exception shouldNotBe null
        exception!!.message shouldContain "Missing required properties"
        // Should mention several missing colors
        exception.message shouldContain "green"
        exception.message shouldContain "yellow"
    }

    @Test
    fun `parse scheme with invalid color format fails`() {
        val path = getTestResourcePath("invalid-colors.json")
        val result = parser.parse(path)

        result.isFailure shouldBe true
        val exception = result.exceptionOrNull()
        exception shouldNotBe null
        exception!!.message shouldContain "Invalid colors"
        // Should mention the invalid colors
        exception.message shouldContain "background"  // Missing #
        exception.message shouldContain "red"  // Invalid characters (gg)
    }

    @Test
    fun `parse non-existent file fails`(@TempDir tempDir: Path) {
        val nonExistentPath = tempDir.resolve("does-not-exist.json")
        val result = parser.parse(nonExistentPath)

        result.isFailure shouldBe true
        val exception = result.exceptionOrNull()
        exception shouldNotBe null
        exception!!.message shouldContain "File not found"
    }

    @Test
    fun `parse empty file fails`(@TempDir tempDir: Path) {
        val emptyFile = tempDir.resolve("empty.json")
        emptyFile.writeText("")

        val result = parser.parse(emptyFile)

        result.isFailure shouldBe true
        val exception = result.exceptionOrNull()
        exception shouldNotBe null
        exception!!.message shouldContain "File is empty"
    }

    @Test
    fun `parse directory fails when path is not a file`(@TempDir tempDir: Path) {
        val result = parser.parse(tempDir)

        result.isFailure shouldBe true
        val exception = result.exceptionOrNull()
        exception shouldNotBe null
        exception!!.message shouldContain "Not a regular file"
    }

    @Test
    fun `parseDirectory returns multiple schemes`() {
        val resourceUrl = this::class.java.classLoader.getResource("test-schemes")
            ?: error("Test resource directory not found: test-schemes")
        val dirPath = Path.of(resourceUrl.toURI())

        val results = parser.parseDirectory(dirPath)

        // Should find all JSON files in the directory
        results.size shouldBe 11  // Total test files in the directory

        val successful = results.filter { it.second.isSuccess }
        val failed = results.filter { it.second.isFailure }

        // We have 8 valid schemes and 3 invalid ones
        successful.size shouldBe 8
        failed.size shouldBe 3

        // Verify some of the successful schemes
        val schemes = successful.mapNotNull { it.second.getOrNull() }
        schemes.any { it.name == "Test Valid Scheme" } shouldBe true
        schemes.any { it.name == "Another Test Scheme" } shouldBe true
    }

    @Test
    fun `parseDirectory returns empty list for non-existent directory`(@TempDir tempDir: Path) {
        val nonExistent = tempDir.resolve("does-not-exist")
        val results = parser.parseDirectory(nonExistent)

        results.size shouldBe 0
    }

    @Test
    fun `parseDirectory returns error for file path instead of directory`(@TempDir tempDir: Path) {
        val file = tempDir.resolve("test.json")
        file.writeText("{}")

        val results = parser.parseDirectory(file)

        results.size shouldBe 1
        results[0].second.isFailure shouldBe true
        results[0].second.exceptionOrNull()?.message shouldContain "Not a directory"
    }

    @Test
    fun `parseDirectory only parses json files`(@TempDir tempDir: Path) {
        // Create various files
        tempDir.resolve("scheme1.json").writeText("""
            {
              "name": "Test",
              "background": "#000000",
              "foreground": "#ffffff",
              "black": "#000000",
              "red": "#ff0000",
              "green": "#00ff00",
              "yellow": "#ffff00",
              "blue": "#0000ff",
              "purple": "#ff00ff",
              "cyan": "#00ffff",
              "white": "#ffffff",
              "brightBlack": "#808080",
              "brightRed": "#ff8080",
              "brightGreen": "#80ff80",
              "brightYellow": "#ffff80",
              "brightBlue": "#8080ff",
              "brightPurple": "#ff80ff",
              "brightCyan": "#80ffff",
              "brightWhite": "#ffffff"
            }
        """.trimIndent())
        tempDir.resolve("readme.txt").writeText("This is not JSON")
        tempDir.resolve("data.xml").writeText("<xml/>")

        val results = parser.parseDirectory(tempDir)

        // Should only parse .json files
        results.size shouldBe 1
        results[0].first.fileName.toString() shouldBe "scheme1.json"
    }

    @Test
    fun `parseWithSummary generates correct statistics`() {
        val resourceUrl = this::class.java.classLoader.getResource("test-schemes")
            ?: error("Test resource directory not found: test-schemes")
        val dirPath = Path.of(resourceUrl.toURI())

        val summary = parser.parseWithSummary(dirPath)

        summary.totalFiles shouldBe 11
        summary.successCount shouldBe 8
        summary.failureCount shouldBe 3

        summary.successfulSchemes.size shouldBe 8
        summary.failures.size shouldBe 3

        // Verify report generation
        val report = summary.toReport()
        report shouldContain "Windows Terminal Color Scheme Parsing Summary"
        report shouldContain "Total files:      11"
        report shouldContain "Successful:       8"
        report shouldContain "Failed:           3"
        report shouldContain "Test Valid Scheme"
        report shouldContain "Another Test Scheme"
    }

    @Test
    fun `parsed scheme validates color formats correctly`() {
        val path = getTestResourcePath("valid-scheme.json")
        val result = parser.parse(path)

        result.isSuccess shouldBe true
        val scheme = result.getOrThrow()

        // All colors should be valid hex format
        val errors = scheme.validate()
        errors.isEmpty() shouldBe true
    }

    @Test
    fun `parsed scheme with optional properties includes them in color palette`() {
        val path = getTestResourcePath("valid-scheme.json")
        val result = parser.parse(path)

        result.isSuccess shouldBe true
        val scheme = result.getOrThrow()
        val palette = scheme.toColorPalette()

        // Optional properties should be in the palette
        palette["wt_cursorColor"] shouldBe "#528bff"
        palette["wt_selectionBackground"] shouldBe "#3e4451"
    }

    @Test
    fun `parsed scheme without optional properties uses defaults in color palette`() {
        val path = getTestResourcePath("valid-scheme-no-optionals.json")
        val result = parser.parse(path)

        result.isSuccess shouldBe true
        val scheme = result.getOrThrow()
        val palette = scheme.toColorPalette()

        // CursorColor should default to foreground
        palette["wt_cursorColor"] shouldBe scheme.foreground
        // SelectionBackground should be a blend
        palette["wt_selectionBackground"] shouldNotBe null
    }

    @Test
    fun `ParserException can be thrown and caught`() {
        var exceptionCaught = false
        try {
            throw ParserException("Test error")
        } catch (e: ParserException) {
            exceptionCaught = true
            e.message shouldBe "Test error"
        }
        exceptionCaught shouldBe true
    }

    @Test
    fun `ParserException can include cause`() {
        val cause = RuntimeException("Original error")
        val exception = ParserException("Wrapped error", cause)

        exception.message shouldBe "Wrapped error"
        exception.cause shouldBe cause
    }

    @Test
    fun `validate detects blank name`(@TempDir tempDir: Path) {
        val schemeFile = tempDir.resolve("blank-name.json")
        schemeFile.writeText("""
            {
              "name": "",
              "background": "#000000",
              "foreground": "#ffffff",
              "black": "#000000",
              "red": "#ff0000",
              "green": "#00ff00",
              "yellow": "#ffff00",
              "blue": "#0000ff",
              "purple": "#ff00ff",
              "cyan": "#00ffff",
              "white": "#ffffff",
              "brightBlack": "#808080",
              "brightRed": "#ff8080",
              "brightGreen": "#80ff80",
              "brightYellow": "#ffff80",
              "brightBlue": "#8080ff",
              "brightPurple": "#ff80ff",
              "brightCyan": "#80ffff",
              "brightWhite": "#ffffff"
            }
        """.trimIndent())

        val result = parser.parse(schemeFile)

        result.isFailure shouldBe true
        result.exceptionOrNull()?.message shouldContain "Missing required properties"
        result.exceptionOrNull()?.message shouldContain "name"
    }

    @Test
    fun `validate detects invalid optional color`(@TempDir tempDir: Path) {
        val schemeFile = tempDir.resolve("invalid-optional.json")
        schemeFile.writeText("""
            {
              "name": "Test",
              "background": "#000000",
              "foreground": "#ffffff",
              "cursorColor": "invalid",
              "black": "#000000",
              "red": "#ff0000",
              "green": "#00ff00",
              "yellow": "#ffff00",
              "blue": "#0000ff",
              "purple": "#ff00ff",
              "cyan": "#00ffff",
              "white": "#ffffff",
              "brightBlack": "#808080",
              "brightRed": "#ff8080",
              "brightGreen": "#80ff80",
              "brightYellow": "#ffff80",
              "brightBlue": "#8080ff",
              "brightPurple": "#ff80ff",
              "brightCyan": "#80ffff",
              "brightWhite": "#ffffff"
            }
        """.trimIndent())

        val result = parser.parse(schemeFile)

        result.isFailure shouldBe true
        result.exceptionOrNull()?.message shouldContain "Invalid colors"
        result.exceptionOrNull()?.message shouldContain "cursorColor"
    }
}
