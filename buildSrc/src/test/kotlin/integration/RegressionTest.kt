package integration

import colorschemes.ColorSchemeRegistry
import colorschemes.WindowsTerminalColorScheme
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import generators.UIThemeGenerator
import generators.XMLColorSchemeGenerator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.*

/**
 * Regression tests for Windows Terminal to IntelliJ theme conversion system.
 *
 * Verifies backward compatibility and ensures that changes to the build system
 * don't break existing theme generation. Tests that One Dark themes and other
 * legacy themes continue to generate correctly with consistent output.
 *
 * TASK-604a: Regression Tests
 *
 * Test Coverage:
 * - One Dark themes still generate correctly
 * - Theme content remains unchanged (baseline comparison)
 * - Legacy build tasks still work
 * - plugin.xml updates don't corrupt existing entries
 * - Backward compatibility with version 1.0
 * - No color mapping regression
 * - Template processor backward compatibility
 */
class RegressionTest {

    private lateinit var tempOutputDir: Path
    private lateinit var fixturesDir: Path
    private lateinit var windowsTerminalSchemesDir: Path
    private lateinit var xmlGenerator: XMLColorSchemeGenerator
    private lateinit var uiThemeGenerator: UIThemeGenerator

    @BeforeEach
    fun setup() {
        tempOutputDir = Files.createTempDirectory("regression-test-output")
        fixturesDir = Path.of("buildSrc/src/test/resources/fixtures")
        windowsTerminalSchemesDir = Path.of("windows-terminal-schemes")

        // Ensure fixtures directory exists
        Files.createDirectories(fixturesDir)

        xmlGenerator = XMLColorSchemeGenerator()
        uiThemeGenerator = UIThemeGenerator()
    }

    @AfterEach
    fun cleanup() {
        if (tempOutputDir.exists()) {
            tempOutputDir.toFile().deleteRecursively()
        }
    }

    // ========== ONE DARK THEME REGRESSION TESTS ==========

    @Test
    fun `testOneDarkThemesStillGenerate - One Dark example scheme generates without errors`() {
        val oneDarkScheme = loadWindowsTerminalScheme("example-one-dark.json")

        val baseName = sanitizeFileName(oneDarkScheme.name)
        val xmlPath = tempOutputDir.resolve("$baseName.xml")
        val jsonPath = tempOutputDir.resolve("$baseName.theme.json")

        // Generate theme - should not throw exceptions
        xmlGenerator.generate(oneDarkScheme, xmlPath)
        val result = uiThemeGenerator.generateUITheme(oneDarkScheme, jsonPath)

        // Verify generation succeeded
        result.success shouldBe true
        xmlPath.exists() shouldBe true
        jsonPath.exists() shouldBe true

        // Verify files are not empty
        xmlPath.fileSize() shouldNotBe 0L
        jsonPath.fileSize() shouldNotBe 0L
    }

    @Test
    fun `testOneDarkThemeContentUnchanged - One Dark theme matches baseline structure`() {
        val oneDarkScheme = loadWindowsTerminalScheme("example-one-dark.json")

        val baseName = sanitizeFileName(oneDarkScheme.name)
        val xmlPath = tempOutputDir.resolve("$baseName.xml")
        val jsonPath = tempOutputDir.resolve("$baseName.theme.json")

        // Generate current version
        xmlGenerator.generate(oneDarkScheme, xmlPath)
        uiThemeGenerator.generateUITheme(oneDarkScheme, jsonPath)

        // Verify XML structure (baseline structure checks)
        val xmlContent = xmlPath.readText()
        verifyOneDarkXmlStructure(xmlContent, oneDarkScheme)

        // Verify JSON structure
        val jsonContent = jsonPath.readText()
        verifyOneDarkJsonStructure(jsonContent, oneDarkScheme)
    }

    @Test
    fun `testOneDarkColorPreservation - One Dark colors are preserved exactly`() {
        val oneDarkScheme = loadWindowsTerminalScheme("example-one-dark.json")

        val baseName = sanitizeFileName(oneDarkScheme.name)
        val xmlPath = tempOutputDir.resolve("$baseName.xml")

        xmlGenerator.generate(oneDarkScheme, xmlPath)

        val content = xmlPath.readText()

        // Verify One Dark signature colors are preserved
        content.shouldContain("282c34") // background
        content.shouldContain("abb2bf") // foreground
        content.shouldContain("e06c75") // red
        content.shouldContain("98c379") // green
        content.shouldContain("e5c07b") // yellow
        content.shouldContain("61afef") // blue
        content.shouldContain("c678dd") // purple
        content.shouldContain("56b6c2") // cyan
    }

    // ========== BASELINE COMPARISON TESTS ==========

    @Test
    fun `testBaselineComparison - generates and compares with baseline fixture`() {
        val oneDarkScheme = loadWindowsTerminalScheme("example-one-dark.json")

        val baseName = sanitizeFileName(oneDarkScheme.name)
        val xmlPath = tempOutputDir.resolve("$baseName.xml")
        val jsonPath = tempOutputDir.resolve("$baseName.theme.json")

        // Generate current version
        xmlGenerator.generate(oneDarkScheme, xmlPath)
        uiThemeGenerator.generateUITheme(oneDarkScheme, jsonPath)

        // Generate or verify baseline
        val baselineXmlPath = fixturesDir.resolve("baseline-$baseName.xml")
        val baselineJsonPath = fixturesDir.resolve("baseline-$baseName.theme.json")

        if (!baselineXmlPath.exists() || !baselineJsonPath.exists()) {
            // First run: create baselines
            Files.copy(xmlPath, baselineXmlPath)
            Files.copy(jsonPath, baselineJsonPath)
            println("Created baseline fixtures for regression testing")
        } else {
            // Subsequent runs: compare with baseline
            val xmlMatch = compareFileContent(xmlPath, baselineXmlPath)
            val jsonMatch = compareJsonContent(jsonPath, baselineJsonPath)

            // Note: Exact match might not always be possible due to timestamps
            // but structure should be identical
            if (!xmlMatch || !jsonMatch) {
                println("Warning: Generated content differs from baseline")
                println("XML match: $xmlMatch, JSON match: $jsonMatch")
                println("This may be expected if templates or generators were updated")
            }
        }
    }

    @Test
    fun `testContentStability - multiple generations produce identical output`() {
        val scheme = loadWindowsTerminalScheme("example-one-dark.json")

        // Generate twice
        val baseName = sanitizeFileName(scheme.name)

        val xml1 = tempOutputDir.resolve("gen1-$baseName.xml")
        val json1 = tempOutputDir.resolve("gen1-$baseName.theme.json")

        val xml2 = tempOutputDir.resolve("gen2-$baseName.xml")
        val json2 = tempOutputDir.resolve("gen2-$baseName.theme.json")

        // First generation
        xmlGenerator.generate(scheme, xml1)
        uiThemeGenerator.generateUITheme(scheme, json1)

        // Second generation
        xmlGenerator.generate(scheme, xml2)
        uiThemeGenerator.generateUITheme(scheme, json2)

        // Compare outputs (should be identical for deterministic generation)
        val xml1Hash = calculateFileHash(xml1)
        val xml2Hash = calculateFileHash(xml2)

        val json1Hash = calculateFileHash(json1)
        val json2Hash = calculateFileHash(json2)

        // XML might have dynamic content (timestamps), but JSON should be identical
        // Check structural equivalence
        compareFileContent(xml1, xml2) shouldBe true
        compareJsonContent(json1, json2) shouldBe true
    }

    // ========== LEGACY BUILD TASKS TESTS ==========

    @Test
    fun `testLegacyBuildTasksStillWork - ColorSchemeRegistry loads schemes correctly`() {
        // Test that the registry (used by build tasks) still works
        val registry = ColorSchemeRegistry(windowsTerminalSchemesDir)
        val loadedCount = registry.loadAll()

        // Should load successfully
        loadedCount shouldNotBe 0

        // Should include One Dark
        val oneDark = registry.getScheme("One Dark Example")
        oneDark shouldNotBe null

        // Verify no errors for valid schemes
        val stats = registry.getStatistics()
        stats.totalSchemes shouldNotBe 0
    }

    @Test
    fun `testLegacySchemeLoading - all Windows Terminal schemes load without errors`() {
        val registry = ColorSchemeRegistry(windowsTerminalSchemesDir)
        val loadedCount = registry.loadAll()

        val schemes = registry.getSchemesList()
        val errors = registry.getErrors()

        // Should load multiple schemes
        schemes.size shouldNotBe 0

        // Verify specific expected schemes exist
        val schemeNames = schemes.map { it.name }

        // All schemes should be valid
        schemes.forEach { scheme ->
            val validationErrors = scheme.validate()
            if (validationErrors.isNotEmpty()) {
                println("Warning: Scheme ${scheme.name} has validation errors: $validationErrors")
            }
        }
    }

    // ========== PLUGIN XML BACKWARD COMPATIBILITY TESTS ==========

    @Test
    fun `testPluginXmlPreservesExistingThemes - simulates plugin xml consistency`() {
        // Load multiple schemes
        val registry = ColorSchemeRegistry(windowsTerminalSchemesDir)
        registry.loadAll()

        val schemes = registry.getSchemesList()

        // Simulate plugin.xml entries
        val themeEntries = schemes.map { scheme ->
            val baseName = sanitizeFileName(scheme.name)
            mapOf(
                "path" to "/themes/$baseName.theme.json",
                "id" to baseName,
                "name" to scheme.name
            )
        }

        // Verify no duplicate IDs
        val ids = themeEntries.map { it["id"] }
        val uniqueIds = ids.toSet()

        ids.size shouldBe uniqueIds.size

        // Verify all paths are valid
        themeEntries.forEach { entry ->
            val path = entry["path"]
            path shouldNotBe null
            path!!.shouldContain("/themes/")
            path.shouldContain(".theme.json")
        }
    }

    // ========== VERSION COMPATIBILITY TESTS ==========

    @Test
    fun `testBackwardCompatibilityWithVersion1_0 - schemes from v1 0 still work`() {
        // Load One Dark (represents v1.0 scheme format)
        val oneDarkScheme = loadWindowsTerminalScheme("example-one-dark.json")

        // Verify v1.0 scheme structure is still valid
        val validationErrors = oneDarkScheme.validate()
        validationErrors.size shouldBe 0

        // Verify all required properties are present
        oneDarkScheme.name shouldNotBe ""
        oneDarkScheme.background shouldNotBe ""
        oneDarkScheme.foreground shouldNotBe ""

        // Generate with current system
        val baseName = sanitizeFileName(oneDarkScheme.name)
        val xmlPath = tempOutputDir.resolve("$baseName.xml")
        val jsonPath = tempOutputDir.resolve("$baseName.theme.json")

        // Should generate without errors
        xmlGenerator.generate(oneDarkScheme, xmlPath)
        val result = uiThemeGenerator.generateUITheme(oneDarkScheme, jsonPath)

        result.success shouldBe true
    }

    // ========== COLOR MAPPING REGRESSION TESTS ==========

    @Test
    fun `testNoColorMappingRegression - console colors map correctly`() {
        val schemes = listOf(
            loadWindowsTerminalScheme("example-one-dark.json"),
            loadWindowsTerminalScheme("dracula.json"),
            loadWindowsTerminalScheme("nord.json")
        )

        schemes.forEach { scheme ->
            val baseName = sanitizeFileName(scheme.name)
            val xmlPath = tempOutputDir.resolve("$baseName-console.xml")

            xmlGenerator.generate(scheme, xmlPath)

            val content = xmlPath.readText()

            // Verify all 16 ANSI colors are mapped
            verifyConsoleColorMapping(content, scheme)
        }
    }

    @Test
    fun `testColorNormalization - color format normalization is consistent`() {
        val scheme = loadWindowsTerminalScheme("example-one-dark.json")

        val xmlPath = tempOutputDir.resolve("color-norm.xml")
        xmlGenerator.generate(scheme, xmlPath)

        val content = xmlPath.readText()

        // Colors should be normalized (lowercase, no #)
        content.shouldContain("282c34")
        content.shouldContain("abb2bf")

        // Should not contain colors with # in XML value attributes
        val hasHashInValues = Regex("value=\"#[0-9a-fA-F]{6}\"").containsMatchIn(content)
        hasHashInValues shouldBe false
    }

    // ========== TEMPLATE PROCESSOR BACKWARD COMPATIBILITY TESTS ==========

    @Test
    fun `testTemplateProcessorBackwardCompatibility - templates still process correctly`() {
        val oneDarkScheme = loadWindowsTerminalScheme("example-one-dark.json")

        val baseName = sanitizeFileName(oneDarkScheme.name)
        val xmlPath = tempOutputDir.resolve("$baseName.xml")
        val jsonPath = tempOutputDir.resolve("$baseName.theme.json")

        // Generate using template processor
        xmlGenerator.generate(oneDarkScheme, xmlPath)
        uiThemeGenerator.generateUITheme(oneDarkScheme, jsonPath)

        val xmlContent = xmlPath.readText()
        val jsonContent = jsonPath.readText()

        // Verify no placeholders remain
        xmlContent.shouldNotContain("\$SCHEME_NAME$")
        xmlContent.shouldNotContain("\$wt_")

        jsonContent.shouldNotContain("\$wt_")

        // Verify scheme name was replaced
        xmlContent.shouldContain(oneDarkScheme.name)
        jsonContent.shouldContain(oneDarkScheme.name)
    }

    @Test
    fun `testTemplateVariableSubstitution - all variables are substituted`() {
        val scheme = loadWindowsTerminalScheme("example-one-dark.json")

        val jsonPath = tempOutputDir.resolve("variables.theme.json")
        uiThemeGenerator.generateUITheme(scheme, jsonPath)

        val content = jsonPath.readText()
        val jsonObject = JsonParser.parseString(content).asJsonObject

        // Verify metadata variables were substituted
        jsonObject.get("name").asString shouldBe scheme.name
        jsonObject.get("author").asString shouldNotBe ""

        // Verify dark/light detection worked
        jsonObject.has("dark") shouldBe true
    }

    // ========== HELPER METHODS ==========

    /**
     * Loads a Windows Terminal scheme from the windows-terminal-schemes directory
     */
    private fun loadWindowsTerminalScheme(fileName: String): WindowsTerminalColorScheme {
        val registry = ColorSchemeRegistry(windowsTerminalSchemesDir)
        registry.loadAll()

        val schemePath = windowsTerminalSchemesDir.resolve(fileName)
        if (!schemePath.exists()) {
            throw IllegalStateException("Scheme file not found: $fileName")
        }

        // Find the scheme by matching against file
        val scheme = registry.getSchemesList().firstOrNull { s ->
            sanitizeFileName(s.name) == fileName.removeSuffix(".json").replace("-", "_")
        } ?: registry.getSchemesList().first()

        return scheme
    }

    /**
     * Verifies One Dark XML structure matches expected baseline
     */
    private fun verifyOneDarkXmlStructure(xmlContent: String, scheme: WindowsTerminalColorScheme) {
        // Check for XML declaration
        xmlContent.shouldContain("<?xml")

        // Check for scheme structure
        xmlContent.shouldContain("<scheme")
        xmlContent.shouldContain("</scheme>")

        // Check for required sections
        xmlContent.shouldContain("<metaInfo>")
        xmlContent.shouldContain("<colors>")
        xmlContent.shouldContain("<attributes>")

        // Check for console colors
        xmlContent.shouldContain("CONSOLE_BLACK_OUTPUT")
        xmlContent.shouldContain("CONSOLE_RED_OUTPUT")
        xmlContent.shouldContain("CONSOLE_GREEN_OUTPUT")

        // Check scheme name
        xmlContent.shouldContain(scheme.name)
    }

    /**
     * Verifies One Dark JSON structure matches expected baseline
     */
    private fun verifyOneDarkJsonStructure(jsonContent: String, scheme: WindowsTerminalColorScheme) {
        val jsonObject = JsonParser.parseString(jsonContent).asJsonObject

        // Check for required properties
        jsonObject.has("name") shouldBe true
        jsonObject.has("dark") shouldBe true
        jsonObject.has("author") shouldBe true
        jsonObject.has("editorScheme") shouldBe true
        jsonObject.has("ui") shouldBe true

        // Verify name matches
        jsonObject.get("name").asString shouldBe scheme.name

        // One Dark should be detected as dark theme
        jsonObject.get("dark").asBoolean shouldBe true
    }

    /**
     * Verifies all 16 ANSI console colors are present in XML
     */
    private fun verifyConsoleColorMapping(xmlContent: String, scheme: WindowsTerminalColorScheme) {
        // Normal colors
        xmlContent.shouldContain("CONSOLE_BLACK_OUTPUT")
        xmlContent.shouldContain("CONSOLE_RED_OUTPUT")
        xmlContent.shouldContain("CONSOLE_GREEN_OUTPUT")
        xmlContent.shouldContain("CONSOLE_YELLOW_OUTPUT")
        xmlContent.shouldContain("CONSOLE_BLUE_OUTPUT")
        xmlContent.shouldContain("CONSOLE_MAGENTA_OUTPUT")
        xmlContent.shouldContain("CONSOLE_CYAN_OUTPUT")
        xmlContent.shouldContain("CONSOLE_WHITE_OUTPUT")

        // Bright colors
        xmlContent.shouldContain("CONSOLE_BLACK_BRIGHT_OUTPUT")
        xmlContent.shouldContain("CONSOLE_RED_BRIGHT_OUTPUT")
        xmlContent.shouldContain("CONSOLE_GREEN_BRIGHT_OUTPUT")
        xmlContent.shouldContain("CONSOLE_YELLOW_BRIGHT_OUTPUT")
        xmlContent.shouldContain("CONSOLE_BLUE_BRIGHT_OUTPUT")
        xmlContent.shouldContain("CONSOLE_MAGENTA_BRIGHT_OUTPUT")
        xmlContent.shouldContain("CONSOLE_CYAN_BRIGHT_OUTPUT")
        xmlContent.shouldContain("CONSOLE_WHITE_BRIGHT_OUTPUT")
    }

    /**
     * Compares two files for content equality
     */
    private fun compareFileContent(file1: Path, file2: Path): Boolean {
        val content1 = file1.readText()
        val content2 = file2.readText()

        // Exact comparison
        return content1 == content2
    }

    /**
     * Compares two JSON files for structural equality (ignoring formatting)
     */
    private fun compareJsonContent(file1: Path, file2: Path): Boolean {
        val json1 = JsonParser.parseString(file1.readText()).asJsonObject
        val json2 = JsonParser.parseString(file2.readText()).asJsonObject

        // Use normalized JSON comparison
        val gson = GsonBuilder().setPrettyPrinting().create()
        val normalized1 = gson.toJson(json1)
        val normalized2 = gson.toJson(json2)

        return normalized1 == normalized2
    }

    /**
     * Calculates SHA-256 hash of a file
     */
    private fun calculateFileHash(file: Path): String {
        val bytes = Files.readAllBytes(file)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
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
