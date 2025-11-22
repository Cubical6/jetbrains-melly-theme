package integration

import colorschemes.ColorSchemeRegistry
import colorschemes.WindowsTerminalColorScheme
import com.google.gson.JsonParser
import generators.UIThemeGenerator
import generators.XMLColorSchemeGenerator
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import mapping.ColorMappingConfig
import org.junit.jupiter.api.Assertions.assertTrue
import mapping.ConsoleColorMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.ColorUtils
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.*

/**
 * End-to-end integration tests for the Windows Terminal to IntelliJ theme conversion system.
 *
 * Tests the complete build process from Windows Terminal JSON to IntelliJ theme files,
 * verifying all generated files are created, valid, and follow the expected structure.
 *
 * TASK-604: E2E Integration Test
 *
 * Test Coverage:
 * - Complete theme generation from Windows Terminal JSON
 * - Multiple scheme types (dark, light, monochrome, high-contrast, normal)
 * - XML and JSON output format validation
 * - Generated themes can be loaded (syntax validation)
 * - Console colors match Windows Terminal ANSI colors exactly
 * - Theme IDs are unique
 * - File names follow conventions
 */
class BuildIntegrationTest {

    private lateinit var tempOutputDir: Path
    private lateinit var testSchemesDir: Path
    private lateinit var xmlGenerator: XMLColorSchemeGenerator
    private lateinit var uiThemeGenerator: UIThemeGenerator
    private lateinit var consoleColorMapper: ConsoleColorMapper

    @BeforeEach
    fun setup() {
        // Create temporary output directory
        tempOutputDir = Files.createTempDirectory("integration-test-output")

        // Set up test schemes directory
        testSchemesDir = Path.of("buildSrc/src/test/resources/test-schemes")

        // Initialize generators
        xmlGenerator = XMLColorSchemeGenerator()
        uiThemeGenerator = UIThemeGenerator()
        consoleColorMapper = ConsoleColorMapper(ColorMappingConfig)
    }

    @AfterEach
    fun cleanup() {
        // Clean up temporary directory
        if (tempOutputDir.exists()) {
            tempOutputDir.toFile().deleteRecursively()
        }
    }

    // ========== COMPLETE THEME GENERATION TESTS ==========

    @Test
    fun `testCompleteThemeGeneration - generates both XML and JSON files`() {
        // Load a test scheme
        val scheme = loadTestScheme("normal-test.json")

        // Generate theme files
        val baseName = sanitizeFileName(scheme.name)
        val xmlPath = tempOutputDir.resolve("$baseName.xml")
        val jsonPath = tempOutputDir.resolve("$baseName.theme.json")

        // Generate XML
        xmlGenerator.generate(scheme, xmlPath)

        // Generate JSON
        val result = uiThemeGenerator.generateUITheme(
            scheme = scheme,
            outputPath = jsonPath,
            overwriteExisting = true
        )

        // Verify both files exist
        xmlPath.exists() shouldBe true
        jsonPath.exists() shouldBe true

        // Verify files are not empty
        xmlPath.fileSize() shouldNotBe 0L
        jsonPath.fileSize() shouldNotBe 0L

        // Verify generation result
        result.success shouldBe true
        result.outputPath shouldBe jsonPath
        result.themeName shouldBe scheme.name
    }

    @Test
    fun `testMultipleSchemeGeneration - generates themes for multiple schemes`() {
        // Load all valid test schemes
        val registry = ColorSchemeRegistry(testSchemesDir)
        val loadedCount = registry.loadAll()

        loadedCount shouldNotBe 0

        val schemes = registry.getSchemesList()
        assertTrue(schemes.isNotEmpty(), "Schemes list should not be empty")

        val generatedFiles = mutableListOf<Pair<Path, Path>>()

        // Generate themes for each scheme
        schemes.forEach { scheme ->
            val baseName = sanitizeFileName(scheme.name)
            val xmlPath = tempOutputDir.resolve("$baseName.xml")
            val jsonPath = tempOutputDir.resolve("$baseName.theme.json")

            try {
                xmlGenerator.generate(scheme, xmlPath)
                uiThemeGenerator.generateUITheme(scheme, jsonPath)

                if (xmlPath.exists() && jsonPath.exists()) {
                    generatedFiles.add(xmlPath to jsonPath)
                }
            } catch (e: Exception) {
                // Some test schemes might be intentionally invalid
                println("Skipping invalid scheme: ${scheme.name}")
            }
        }

        // Verify we generated at least some themes
        generatedFiles.shouldHaveAtLeastSize(5)

        // Verify each generated theme has both files
        generatedFiles.forEach { (xml, json) ->
            xml.exists() shouldBe true
            json.exists() shouldBe true
        }
    }

    // ========== SCHEME TYPE TESTS ==========

    @Test
    fun `testDarkThemeGeneration - dark theme is correctly detected and generated`() {
        val scheme = loadTestScheme("normal-test.json")

        // Normal test should be a dark theme (background: #282c34)
        val isDark = ColorUtils.calculateLuminance(scheme.background) < UIThemeGenerator.DARK_LIGHT_THRESHOLD
        isDark shouldBe true

        val jsonPath = tempOutputDir.resolve("dark-theme.theme.json")
        val result = uiThemeGenerator.generateUITheme(scheme, jsonPath)

        result.success shouldBe true
        result.isDark shouldBe true

        // Verify JSON contains dark flag
        val jsonContent = jsonPath.readText()
        val jsonObject = JsonParser.parseString(jsonContent).asJsonObject
        jsonObject.get("dark").asBoolean shouldBe true
    }

    @Test
    fun `testLightThemeGeneration - light theme is correctly detected and generated`() {
        val scheme = loadTestScheme("light-theme-test.json")

        // Light theme test should be a light theme (background: #ffffff)
        val isDark = ColorUtils.calculateLuminance(scheme.background) < UIThemeGenerator.DARK_LIGHT_THRESHOLD
        isDark shouldBe false

        val jsonPath = tempOutputDir.resolve("light-theme.theme.json")
        val result = uiThemeGenerator.generateUITheme(scheme, jsonPath)

        result.success shouldBe true
        result.isDark shouldBe false

        // Verify JSON contains light flag
        val jsonContent = jsonPath.readText()
        val jsonObject = JsonParser.parseString(jsonContent).asJsonObject
        jsonObject.get("dark").asBoolean shouldBe false
    }

    @Test
    fun `testMonochromeThemeGeneration - monochrome theme generates valid output`() {
        val scheme = loadTestScheme("monochrome-test.json")

        val xmlPath = tempOutputDir.resolve("monochrome.xml")
        val jsonPath = tempOutputDir.resolve("monochrome.theme.json")

        // Generate theme
        xmlGenerator.generate(scheme, xmlPath)
        uiThemeGenerator.generateUITheme(scheme, jsonPath)

        // Verify files exist and are valid
        xmlPath.exists() shouldBe true
        jsonPath.exists() shouldBe true

        // Verify XML is well-formed
        validateXmlFile(xmlPath)

        // Verify JSON is well-formed
        validateJsonFile(jsonPath)

        // Monochrome themes should still have all ANSI colors mapped
        val xmlContent = xmlPath.readText()
        xmlContent.shouldContain("CONSOLE_BLACK_OUTPUT")
        xmlContent.shouldContain("CONSOLE_WHITE_OUTPUT")
    }

    @Test
    fun `testHighContrastThemeGeneration - high contrast theme generates valid output`() {
        val scheme = loadTestScheme("high-contrast-test.json")

        val xmlPath = tempOutputDir.resolve("high-contrast.xml")
        val jsonPath = tempOutputDir.resolve("high-contrast.theme.json")

        // Generate theme
        xmlGenerator.generate(scheme, xmlPath)
        uiThemeGenerator.generateUITheme(scheme, jsonPath)

        // Verify files exist
        xmlPath.exists() shouldBe true
        jsonPath.exists() shouldBe true

        // Verify high contrast colors are preserved
        val xmlContent = xmlPath.readText()

        // High contrast test has pure colors (#ff0000, #00ff00, etc.)
        xmlContent.shouldContain("ff0000") // red
        xmlContent.shouldContain("00ff00") // green
        xmlContent.shouldContain("0000ff") // blue
        xmlContent.shouldContain("ffff00") // yellow
    }

    @Test
    fun `testNormalThemeGeneration - normal contrast theme generates correctly`() {
        val scheme = loadTestScheme("normal-test.json")

        val xmlPath = tempOutputDir.resolve("normal.xml")
        val jsonPath = tempOutputDir.resolve("normal.theme.json")

        // Generate theme
        xmlGenerator.generate(scheme, xmlPath)
        val result = uiThemeGenerator.generateUITheme(scheme, jsonPath)

        result.success shouldBe true

        // Verify files exist
        xmlPath.exists() shouldBe true
        jsonPath.exists() shouldBe true

        // Verify normal One Dark colors are present
        val xmlContent = xmlPath.readText()
        xmlContent.shouldContain("282c34") // background
        xmlContent.shouldContain("abb2bf") // foreground
        xmlContent.shouldContain("e06c75") // red
        xmlContent.shouldContain("98c379") // green
    }

    // ========== FILE STRUCTURE TESTS ==========

    @Test
    fun `testOutputFileStructure - verifies correct file naming and structure`() {
        val scheme = loadTestScheme("normal-test.json")
        val baseName = sanitizeFileName(scheme.name)

        val xmlPath = tempOutputDir.resolve("$baseName.xml")
        val jsonPath = tempOutputDir.resolve("$baseName.theme.json")

        xmlGenerator.generate(scheme, xmlPath)
        uiThemeGenerator.generateUITheme(scheme, jsonPath)

        // Verify file names follow convention
        xmlPath.fileName.toString() shouldBe "${baseName}.xml"
        jsonPath.fileName.toString() shouldBe "${baseName}.theme.json"

        // Verify files are in the output directory
        xmlPath.parent shouldBe tempOutputDir
        jsonPath.parent shouldBe tempOutputDir

        // Verify file extensions
        xmlPath.extension shouldBe "xml"
        jsonPath.extension shouldBe "json"
    }

    // ========== XML VALIDATION TESTS ==========

    @Test
    fun `testXmlValidation - XML output is well-formed and parseable`() {
        val schemes = listOf(
            loadTestScheme("normal-test.json"),
            loadTestScheme("light-theme-test.json"),
            loadTestScheme("monochrome-test.json")
        )

        schemes.forEachIndexed { index, scheme ->
            val xmlPath = tempOutputDir.resolve("scheme-$index.xml")
            xmlGenerator.generate(scheme, xmlPath)

            // Validate XML structure
            validateXmlFile(xmlPath)

            val content = xmlPath.readText()

            // Check for XML declaration
            content.shouldContain("<?xml")

            // Check for scheme element
            content.shouldContain("<scheme")
            content.shouldContain("</scheme>")

            // Check for required sections
            content.shouldContain("<metaInfo>")
            content.shouldContain("<colors>")
            content.shouldContain("<attributes>")

            // Verify no placeholders remain
            content.shouldNotContain("\$SCHEME_NAME$")
            content.shouldNotContain("\$wt_")
        }
    }

    @Test
    fun `testXmlValidation - XML contains correct IntelliJ structure`() {
        val scheme = loadTestScheme("normal-test.json")
        val xmlPath = tempOutputDir.resolve("intellij-structure-test.xml")

        xmlGenerator.generate(scheme, xmlPath)

        val content = xmlPath.readText()

        // Verify IntelliJ-specific attributes
        content.shouldContain("parent_scheme=\"Darcula\"")
        content.shouldContain("version=\"")

        // Verify metadata section
        content.shouldContain("<property name=\"created\">")
        content.shouldContain("<property name=\"ide\">")
        content.shouldContain("<property name=\"modified\">")
        content.shouldContain("<property name=\"originalScheme\">")
        content.shouldContain("windowsTerminalScheme")

        // Verify color options
        content.shouldContain("<option name=\"BACKGROUND\"")
        content.shouldContain("<option name=\"FOREGROUND\"")

        // Verify console colors
        content.shouldContain("CONSOLE_BLACK_OUTPUT")
        content.shouldContain("CONSOLE_RED_OUTPUT")
        content.shouldContain("CONSOLE_GREEN_OUTPUT")
    }

    // ========== JSON VALIDATION TESTS ==========

    @Test
    fun `testJsonValidation - JSON output is well-formed and parseable`() {
        val schemes = listOf(
            loadTestScheme("normal-test.json"),
            loadTestScheme("light-theme-test.json"),
            loadTestScheme("high-contrast-test.json")
        )

        schemes.forEachIndexed { index, scheme ->
            val jsonPath = tempOutputDir.resolve("scheme-$index.theme.json")
            uiThemeGenerator.generateUITheme(scheme, jsonPath)

            // Validate JSON structure
            validateJsonFile(jsonPath)

            val content = jsonPath.readText()
            val jsonObject = JsonParser.parseString(content).asJsonObject

            // Verify required properties exist
            jsonObject.has("name") shouldBe true
            jsonObject.has("dark") shouldBe true
            jsonObject.has("author") shouldBe true
            jsonObject.has("editorScheme") shouldBe true
            jsonObject.has("ui") shouldBe true
        }
    }

    @Test
    fun `testJsonValidation - JSON has all required properties`() {
        val scheme = loadTestScheme("normal-test.json")
        val jsonPath = tempOutputDir.resolve("complete-json-test.theme.json")

        uiThemeGenerator.generateUITheme(scheme, jsonPath)

        val content = jsonPath.readText()
        val jsonObject = JsonParser.parseString(content).asJsonObject

        // Verify top-level properties
        jsonObject.get("name").asString shouldNotBe ""
        jsonObject.has("dark") shouldBe true
        jsonObject.get("author").asString shouldContain "Windows Terminal"

        // Verify editorScheme points to correct XML file
        val editorScheme = jsonObject.get("editorScheme").asString
        editorScheme.shouldContain(".xml")

        // Verify ui object exists and has colors
        val ui = jsonObject.getAsJsonObject("ui")
        ui shouldNotBe null
        ui.has("*") shouldBe true
    }

    // ========== THEME METADATA TESTS ==========

    @Test
    fun `testThemeMetadataGeneration - metadata is correctly generated`() {
        val scheme = loadTestScheme("normal-test.json")
        val jsonPath = tempOutputDir.resolve("metadata-test.theme.json")

        val result = uiThemeGenerator.generateUITheme(scheme, jsonPath)

        result.success shouldBe true
        result.themeName shouldBe scheme.name

        val content = jsonPath.readText()
        val jsonObject = JsonParser.parseString(content).asJsonObject

        // Verify metadata
        jsonObject.get("name").asString shouldBe scheme.name
        jsonObject.get("author").asString shouldBe UIThemeGenerator.DEFAULT_AUTHOR

        // Verify theme ID is based on scheme name
        val editorScheme = jsonObject.get("editorScheme").asString
        val sanitizedName = sanitizeFileName(scheme.name)
        editorScheme.shouldContain(sanitizedName)
    }

    // ========== CONSOLE COLOR MAPPING TESTS ==========

    @Test
    fun `testConsoleColorMapping - ANSI colors match Windows Terminal exactly`() {
        val scheme = loadTestScheme("normal-test.json")
        val xmlPath = tempOutputDir.resolve("console-colors.xml")

        xmlGenerator.generate(scheme, xmlPath)

        val content = xmlPath.readText()

        // Verify black (normal and bright)
        content.shouldContain(ColorUtils.normalizeColor(scheme.black))
        content.shouldContain(ColorUtils.normalizeColor(scheme.brightBlack))

        // Verify red (normal and bright)
        content.shouldContain(ColorUtils.normalizeColor(scheme.red))
        content.shouldContain(ColorUtils.normalizeColor(scheme.brightRed))

        // Verify green (normal and bright)
        content.shouldContain(ColorUtils.normalizeColor(scheme.green))
        content.shouldContain(ColorUtils.normalizeColor(scheme.brightGreen))

        // Verify yellow (normal and bright)
        content.shouldContain(ColorUtils.normalizeColor(scheme.yellow))
        content.shouldContain(ColorUtils.normalizeColor(scheme.brightYellow))

        // Verify blue (normal and bright)
        content.shouldContain(ColorUtils.normalizeColor(scheme.blue))
        content.shouldContain(ColorUtils.normalizeColor(scheme.brightBlue))

        // Verify purple/magenta (normal and bright)
        content.shouldContain(ColorUtils.normalizeColor(scheme.purple))
        content.shouldContain(ColorUtils.normalizeColor(scheme.brightPurple))

        // Verify cyan (normal and bright)
        content.shouldContain(ColorUtils.normalizeColor(scheme.cyan))
        content.shouldContain(ColorUtils.normalizeColor(scheme.brightCyan))

        // Verify white (normal and bright)
        content.shouldContain(ColorUtils.normalizeColor(scheme.white))
        content.shouldContain(ColorUtils.normalizeColor(scheme.brightWhite))
    }

    @Test
    fun `testConsoleColorMapping - console color mapper preserves exact RGB values`() {
        val scheme = loadTestScheme("high-contrast-test.json")

        // Map console colors
        val consoleColors = consoleColorMapper.mapToConsoleColors(scheme)

        // Verify exact color preservation (high contrast has distinct values)
        assertTrue(consoleColors.isNotEmpty(), "Console colors should not be empty")

        // Generate XML and verify colors
        val xmlPath = tempOutputDir.resolve("rgb-exact.xml")
        xmlGenerator.generate(scheme, xmlPath)

        val content = xmlPath.readText()

        // High contrast test has pure colors that should be preserved exactly
        content.shouldContain("ff0000") // red
        content.shouldContain("00ff00") // green
        content.shouldContain("0000ff") // blue
        content.shouldContain("ffff00") // yellow
        content.shouldContain("ff00ff") // magenta/purple
        content.shouldContain("00ffff") // cyan
    }

    // ========== THEME ID UNIQUENESS TESTS ==========

    @Test
    fun `testThemeIdUniqueness - different schemes generate unique IDs`() {
        val schemes = listOf(
            loadTestScheme("normal-test.json"),
            loadTestScheme("light-theme-test.json"),
            loadTestScheme("monochrome-test.json")
        )

        val themeNames = mutableSetOf<String>()
        val editorSchemeNames = mutableSetOf<String>()

        schemes.forEachIndexed { index, scheme ->
            val jsonPath = tempOutputDir.resolve("unique-$index.theme.json")
            uiThemeGenerator.generateUITheme(scheme, jsonPath)

            val content = jsonPath.readText()
            val jsonObject = JsonParser.parseString(content).asJsonObject

            val themeName = jsonObject.get("name").asString
            val editorScheme = jsonObject.get("editorScheme").asString

            // Verify names are unique
            themeNames.add(themeName) shouldBe true
            editorSchemeNames.add(editorScheme) shouldBe true
        }

        // Verify we have unique names for all schemes
        themeNames.size shouldBe schemes.size
        editorSchemeNames.size shouldBe schemes.size
    }

    // ========== FILE NAME CONVENTION TESTS ==========

    @Test
    fun `testFileNameConventions - file names follow sanitization rules`() {
        // Create a scheme with special characters in the name
        val scheme = WindowsTerminalColorScheme(
            name = "Test & Special <Characters> Theme!",
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

        val baseName = sanitizeFileName(scheme.name)

        // Verify sanitized name follows rules
        baseName.shouldNotContain("&")
        baseName.shouldNotContain("<")
        baseName.shouldNotContain(">")
        baseName.shouldNotContain("!")
        baseName.shouldContain("_") // spaces converted to underscores

        // Verify name is lowercase
        baseName shouldBe baseName.lowercase()

        // Generate files with sanitized name
        val xmlPath = tempOutputDir.resolve("$baseName.xml")
        val jsonPath = tempOutputDir.resolve("$baseName.theme.json")

        xmlGenerator.generate(scheme, xmlPath)
        uiThemeGenerator.generateUITheme(scheme, jsonPath)

        xmlPath.exists() shouldBe true
        jsonPath.exists() shouldBe true
    }

    // ========== PLUGIN XML UPDATING TESTS ==========

    @Test
    fun `testPluginXmlUpdating - simulates plugin xml theme reference generation`() {
        val schemes = listOf(
            loadTestScheme("normal-test.json"),
            loadTestScheme("light-theme-test.json")
        )

        val themeReferences = mutableListOf<String>()

        schemes.forEach { scheme ->
            val baseName = sanitizeFileName(scheme.name)

            // Simulate plugin.xml entry generation
            val themeRef = "/themes/$baseName.theme.json"
            themeReferences.add(themeRef)
        }

        // Verify unique references
        themeReferences.size shouldBe schemes.size
        themeReferences.forEach { ref ->
            ref.shouldContain("/themes/")
            ref.shouldContain(".theme.json")
        }
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    fun `testInvalidScheme - throws exception for invalid color format`() {
        val invalidScheme = WindowsTerminalColorScheme(
            name = "Invalid Colors",
            background = "not-a-color", // Invalid format
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

        val xmlPath = tempOutputDir.resolve("invalid.xml")

        assertThrows<IllegalArgumentException> {
            xmlGenerator.generate(invalidScheme, xmlPath)
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Loads a test scheme from the test resources directory
     */
    private fun loadTestScheme(fileName: String): WindowsTerminalColorScheme {
        val registry = ColorSchemeRegistry(testSchemesDir)
        registry.loadAll()

        val scheme = registry.getSchemesList().find {
            testSchemesDir.resolve(fileName).exists()
        } ?: run {
            // If not found by name match, try to load directly
            val schemes = registry.getSchemesList()
            schemes.find { sanitizeFileName(it.name) == fileName.removeSuffix(".json") }
                ?: schemes.firstOrNull { testSchemesDir.resolve(fileName).exists() }
                ?: throw IllegalStateException("Test scheme not found: $fileName")
        }

        return scheme
    }

    /**
     * Validates that an XML file is well-formed
     */
    private fun validateXmlFile(xmlPath: Path) {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(xmlPath.toFile())

        document shouldNotBe null
        document.documentElement.nodeName shouldBe "scheme"
    }

    /**
     * Validates that a JSON file is well-formed
     */
    private fun validateJsonFile(jsonPath: Path) {
        val content = jsonPath.readText()
        val jsonObject = JsonParser.parseString(content).asJsonObject

        jsonObject shouldNotBe null
    }

    /**
     * Sanitizes a scheme name for use as a file name
     */
    private fun sanitizeFileName(name: String): String {
        return name
            .trim()
            .replace(Regex("[^a-zA-Z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "_")
            .replace(Regex("_+"), "_")
            .lowercase()
    }
}
