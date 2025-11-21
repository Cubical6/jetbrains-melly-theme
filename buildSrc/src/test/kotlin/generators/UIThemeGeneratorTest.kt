package generators

import colorschemes.WindowsTerminalColorScheme
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

class UIThemeGeneratorTest {

    private val generator = UIThemeGenerator()
    private val gson: Gson = GsonBuilder().create()

    /**
     * Creates a sample dark theme color scheme for testing
     */
    private fun createDarkColorScheme(): WindowsTerminalColorScheme {
        return WindowsTerminalColorScheme(
            name = "Test Dark Theme",
            background = "#282c34",
            foreground = "#abb2bf",
            black = "#282c34",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#e5c07b",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            brightBlack = "#5c6370",
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff",
            cursorColor = "#528bff",
            selectionBackground = "#3e4451"
        )
    }

    /**
     * Creates a sample light theme color scheme for testing
     */
    private fun createLightColorScheme(): WindowsTerminalColorScheme {
        return WindowsTerminalColorScheme(
            name = "Test Light Theme",
            background = "#fafafa",
            foreground = "#383a42",
            black = "#000000",
            red = "#e45649",
            green = "#50a14f",
            yellow = "#c18401",
            blue = "#0184bc",
            purple = "#a626a4",
            cyan = "#0997b3",
            white = "#fafafa",
            brightBlack = "#4f525e",
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff"
        )
    }

    @Test
    fun `generateUITheme creates valid theme file`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("test-theme.theme.json")

        val result = generator.generateUITheme(scheme, outputPath)

        result.success shouldBe true
        result.themeName shouldBe "Test Dark Theme"
        result.outputPath shouldBe outputPath
        result.error shouldBe null

        outputPath.exists() shouldBe true

        // Validate the file content is valid JSON
        val content = outputPath.readText()
        val jsonObj = JsonParser.parseString(content).asJsonObject

        // Verify structure
        jsonObj.has("name") shouldBe true
        jsonObj.has("dark") shouldBe true
        jsonObj.has("colors") shouldBe true
        jsonObj.has("ui") shouldBe true
    }

    @Test
    fun `generateUITheme detects dark theme correctly`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("dark-theme.theme.json")

        val result = generator.generateUITheme(scheme, outputPath)

        result.isDark shouldBe true

        // Verify the dark flag in the JSON
        val content = outputPath.readText()
        val jsonObj = JsonParser.parseString(content).asJsonObject
        jsonObj.get("dark").asString shouldBe "true"
    }

    @Test
    fun `generateUITheme detects light theme correctly`(@TempDir tempDir: Path) {
        val scheme = createLightColorScheme()
        val outputPath = tempDir.resolve("light-theme.theme.json")

        val result = generator.generateUITheme(scheme, outputPath)

        result.isDark shouldBe false

        // Verify the dark flag in the JSON
        val content = outputPath.readText()
        val jsonObj = JsonParser.parseString(content).asJsonObject
        jsonObj.get("dark").asString shouldBe "false"
    }

    @Test
    fun `generateUITheme replaces all template variables`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        generator.generateUITheme(scheme, outputPath)

        val content = outputPath.readText()

        // Verify no unreplaced variables remain
        content shouldNotContain "\$wt_background\$"
        content shouldNotContain "\$wt_foreground\$"
        content shouldNotContain "\$wt_name\$"
        content shouldNotContain "\$wt_scheme_name\$"

        // Verify actual colors are present
        content shouldContain scheme.background
        content shouldContain scheme.foreground
        content shouldContain scheme.red
        content shouldContain scheme.green
        content shouldContain scheme.blue
    }

    @Test
    fun `generateUITheme includes theme name and metadata`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        generator.generateUITheme(scheme, outputPath)

        val content = outputPath.readText()
        val jsonObj = JsonParser.parseString(content).asJsonObject

        jsonObj.get("name").asString shouldBe "Test Dark Theme"
        jsonObj.get("author").asString shouldBe UIThemeGenerator.DEFAULT_AUTHOR
    }

    @Test
    fun `generateUITheme creates parent directories if needed`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("nested/path/theme.theme.json")

        val result = generator.generateUITheme(scheme, outputPath)

        result.success shouldBe true
        outputPath.exists() shouldBe true
        outputPath.parent.exists() shouldBe true
    }

    @Test
    fun `generateUITheme overwrites existing file by default`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        // Create initial file
        Files.writeString(outputPath, "initial content")

        val result = generator.generateUITheme(scheme, outputPath)

        result.success shouldBe true

        // Verify file was overwritten
        val content = outputPath.readText()
        content shouldNotContain "initial content"
        content shouldContain scheme.background
    }

    @Test
    fun `generateUITheme fails if file exists and overwrite is false`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        // Create initial file
        Files.writeString(outputPath, "initial content")

        val result = generator.generateUITheme(scheme, outputPath, overwriteExisting = false)

        result.success shouldBe false
        result.error shouldNotBe null
        result.error shouldContain "already exists"
    }

    @Test
    fun `generateUITheme fails for invalid color scheme`(@TempDir tempDir: Path) {
        val invalidScheme = WindowsTerminalColorScheme(
            name = "Invalid",
            background = "invalid-color",  // Invalid hex color
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
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff"
        )

        val outputPath = tempDir.resolve("theme.theme.json")
        val result = generator.generateUITheme(invalidScheme, outputPath)

        result.success shouldBe false
        result.error shouldNotBe null
        result.error shouldContain "Invalid color scheme"
    }

    @Test
    fun `generateUIThemeContent returns valid JSON string`() {
        val scheme = createDarkColorScheme()

        val content = generator.generateUIThemeContent(scheme)

        content shouldNotBe ""

        // Validate JSON structure
        val jsonObj = JsonParser.parseString(content).asJsonObject
        jsonObj.has("name") shouldBe true
        jsonObj.has("dark") shouldBe true
        jsonObj.has("colors") shouldBe true
        jsonObj.has("ui") shouldBe true
    }

    @Test
    fun `generateUIThemeContent replaces all variables correctly`() {
        val scheme = createDarkColorScheme()

        val content = generator.generateUIThemeContent(scheme)

        // Verify no unreplaced variables remain
        content shouldNotContain "\$wt_"
        content shouldNotContain "\$"

        // Verify actual colors are present
        content shouldContain scheme.background
        content shouldContain scheme.foreground
    }

    @Test
    fun `generateUIThemeContent throws for invalid scheme`() {
        val invalidScheme = WindowsTerminalColorScheme(
            name = "",  // Invalid: blank name
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
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff"
        )

        shouldThrow<IllegalArgumentException> {
            generator.generateUIThemeContent(invalidScheme)
        }
    }

    @Test
    fun `analyzeColorScheme returns correct dark theme information`() {
        val scheme = createDarkColorScheme()

        val analysis = generator.analyzeColorScheme(scheme)

        analysis["name"] shouldBe "Test Dark Theme"
        analysis["backgroundColor"] shouldBe "#282c34"
        analysis["foregroundColor"] shouldBe "#abb2bf"
        analysis["isDark"] shouldBe true
        analysis["themeType"] shouldBe "dark"
        analysis["sanitizedName"] shouldBe "test_dark_theme"
    }

    @Test
    fun `analyzeColorScheme returns correct light theme information`() {
        val scheme = createLightColorScheme()

        val analysis = generator.analyzeColorScheme(scheme)

        analysis["name"] shouldBe "Test Light Theme"
        analysis["isDark"] shouldBe false
        analysis["themeType"] shouldBe "light"
    }

    @Test
    fun `analyzeColorScheme calculates luminance correctly`() {
        val darkScheme = createDarkColorScheme()
        val lightScheme = createLightColorScheme()

        val darkAnalysis = generator.analyzeColorScheme(darkScheme)
        val lightAnalysis = generator.analyzeColorScheme(lightScheme)

        val darkLuminance = darkAnalysis["luminance"] as Double
        val lightLuminance = lightAnalysis["luminance"] as Double

        (darkLuminance < UIThemeGenerator.DARK_LIGHT_THRESHOLD) shouldBe true
        (lightLuminance > UIThemeGenerator.DARK_LIGHT_THRESHOLD) shouldBe true
    }

    @Test
    fun `sanitized name removes special characters`() {
        val scheme = WindowsTerminalColorScheme(
            name = "My Cool Theme! (2024) #1",
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
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff"
        )

        val analysis = generator.analyzeColorScheme(scheme)
        val sanitized = analysis["sanitizedName"] as String

        sanitized shouldBe "my_cool_theme_2024_1"
        sanitized shouldNotContain "!"
        sanitized shouldNotContain "("
        sanitized shouldNotContain ")"
        sanitized shouldNotContain "#"
    }

    @Test
    fun `sanitized name handles multiple spaces`() {
        val scheme = WindowsTerminalColorScheme(
            name = "Theme   With    Multiple     Spaces",
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
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff"
        )

        val analysis = generator.analyzeColorScheme(scheme)
        val sanitized = analysis["sanitizedName"] as String

        sanitized shouldBe "theme_with_multiple_spaces"
        sanitized shouldNotContain "  "  // No double spaces
    }

    @Test
    fun `generated theme includes Editor section with correct colors`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        generator.generateUITheme(scheme, outputPath)

        val content = outputPath.readText()

        // Verify Editor section has the right colors
        content shouldContain "\"Editor\""
        content shouldContain scheme.background  // Editor background
        content shouldContain scheme.foreground  // Editor foreground
    }

    @Test
    fun `generated theme includes UI sections for tool windows`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        generator.generateUITheme(scheme, outputPath)

        val content = outputPath.readText()

        // Verify key UI sections exist
        content shouldContain "\"ToolWindow\""
        content shouldContain "\"Button\""
        content shouldContain "\"List\""
        content shouldContain "\"Table\""
    }

    @Test
    fun `generated theme includes all ANSI colors`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        generator.generateUITheme(scheme, outputPath)

        val content = outputPath.readText()

        // Verify all ANSI colors are present
        content shouldContain scheme.black
        content shouldContain scheme.red
        content shouldContain scheme.green
        content shouldContain scheme.yellow
        content shouldContain scheme.blue
        content shouldContain scheme.purple
        content shouldContain scheme.cyan
        content shouldContain scheme.white
        content shouldContain scheme.brightBlack
        content shouldContain scheme.brightRed
        content shouldContain scheme.brightGreen
        content shouldContain scheme.brightYellow
        content shouldContain scheme.brightBlue
        content shouldContain scheme.brightPurple
        content shouldContain scheme.brightCyan
        content shouldContain scheme.brightWhite
    }

    @Test
    fun `generation result provides helpful summary`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        val result = generator.generateUITheme(scheme, outputPath)
        val summary = result.getSummary()

        summary shouldContain "✓"
        summary shouldContain "successfully"
        summary shouldContain "Test Dark Theme"
        summary shouldContain "Dark"
        summary shouldContain outputPath.toString()
    }

    @Test
    fun `failed generation provides error summary`(@TempDir tempDir: Path) {
        val invalidScheme = WindowsTerminalColorScheme(
            name = "Invalid",
            background = "bad-color",
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
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff"
        )

        val outputPath = tempDir.resolve("theme.theme.json")
        val result = generator.generateUITheme(invalidScheme, outputPath)
        val summary = result.getSummary()

        summary shouldContain "✗"
        summary shouldContain "failed"
        summary shouldContain "Error:"
    }

    @Test
    fun `generated theme has valid editorScheme reference`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        generator.generateUITheme(scheme, outputPath)

        val content = outputPath.readText()
        val jsonObj = JsonParser.parseString(content).asJsonObject

        val editorScheme = jsonObj.get("editorScheme")?.asString
        editorScheme shouldNotBe null
        editorScheme shouldContain "/themes/"
        editorScheme shouldContain ".xml"
        editorScheme shouldContain "test_dark_theme"
    }

    @Test
    fun `generated theme has proper color palette section`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        generator.generateUITheme(scheme, outputPath)

        val content = outputPath.readText()
        val jsonObj = JsonParser.parseString(content).asJsonObject

        val colors = jsonObj.getAsJsonObject("colors")
        colors.has("accentColor") shouldBe true
        colors.has("backgroundColor") shouldBe true
        colors.has("foregroundColor") shouldBe true
        colors.has("selectionBackground") shouldBe true
    }

    @Test
    fun `scheme with optional colors uses them correctly`(@TempDir tempDir: Path) {
        val scheme = createDarkColorScheme()
        val outputPath = tempDir.resolve("theme.theme.json")

        generator.generateUITheme(scheme, outputPath)

        val content = outputPath.readText()

        // Verify optional colors are present
        scheme.cursorColor?.let { content shouldContain it }
        scheme.selectionBackground?.let { content shouldContain it }
    }

    @Test
    fun `scheme without optional colors uses fallbacks`(@TempDir tempDir: Path) {
        val schemeWithoutOptionals = WindowsTerminalColorScheme(
            name = "Minimal Theme",
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
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff"
            // No cursorColor or selectionBackground
        )

        val outputPath = tempDir.resolve("theme.theme.json")
        val result = generator.generateUITheme(schemeWithoutOptionals, outputPath)

        result.success shouldBe true
        outputPath.exists() shouldBe true

        // Should still contain valid JSON with fallback values
        val content = outputPath.readText()
        val jsonObj = JsonParser.parseString(content).asJsonObject
        jsonObj shouldNotBe null
    }
}
