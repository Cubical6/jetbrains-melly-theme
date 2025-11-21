package generators

import colorschemes.WindowsTerminalColorScheme
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class PluginXmlUpdaterTest {

    /**
     * Creates a minimal valid plugin.xml file for testing
     */
    private fun createBasicPluginXml(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <idea-plugin>
              <id>com.example.theme</id>
              <name>Example Theme</name>
              <version>1.0.0</version>
              <vendor>Test Vendor</vendor>
              <depends>com.intellij.modules.platform</depends>
              <extensions defaultExtensionNs="com.intellij">
              </extensions>
            </idea-plugin>
        """.trimIndent()
    }

    /**
     * Creates a plugin.xml file with existing theme providers
     */
    private fun createPluginXmlWithThemes(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <idea-plugin>
              <id>com.example.theme</id>
              <name>Example Theme</name>
              <version>1.0.0</version>
              <vendor>Test Vendor</vendor>
              <depends>com.intellij.modules.platform</depends>
              <extensions defaultExtensionNs="com.intellij">
                <themeProvider id="one-dark-original" path="/themes/one_dark.theme.json"/>
                <themeProvider id="one-dark-italic" path="/themes/one_dark_italic.theme.json"/>
              </extensions>
            </idea-plugin>
        """.trimIndent()
    }

    /**
     * Creates a plugin.xml file with mixed WT and non-WT themes
     */
    private fun createPluginXmlWithMixedThemes(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <idea-plugin>
              <id>com.example.theme</id>
              <name>Example Theme</name>
              <version>1.0.0</version>
              <vendor>Test Vendor</vendor>
              <depends>com.intellij.modules.platform</depends>
              <extensions defaultExtensionNs="com.intellij">
                <themeProvider id="one-dark-original" path="/themes/one_dark.theme.json"/>
                <themeProvider id="wt-gruvbox-dark-abc123" path="/themes/wt-gruvbox-dark.theme.json"/>
                <themeProvider id="wt-nord-xyz789" path="/themes/wt-nord.theme.json"/>
                <themeProvider id="one-dark-italic" path="/themes/one_dark_italic.theme.json"/>
              </extensions>
            </idea-plugin>
        """.trimIndent()
    }

    /**
     * Creates a temporary plugin.xml file
     */
    private fun createTempPluginXml(tempDir: Path, content: String): Path {
        val pluginXmlPath = tempDir.resolve("plugin.xml")
        pluginXmlPath.writeText(content)
        return pluginXmlPath
    }

    /**
     * Creates a sample dark theme color scheme for testing
     */
    private fun createDarkScheme(name: String = "One Dark"): WindowsTerminalColorScheme {
        return WindowsTerminalColorScheme(
            name = name,
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
            brightWhite = "#ffffff"
        )
    }

    // ========================================
    // Initialization Tests
    // ========================================

    @Test
    fun `constructor throws exception if file does not exist`(@TempDir tempDir: Path) {
        val nonExistentPath = tempDir.resolve("non-existent.xml")

        shouldThrow<IllegalArgumentException> {
            PluginXmlUpdater(nonExistentPath)
        }
    }

    @Test
    fun `constructor succeeds if file exists`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())

        // Should not throw
        PluginXmlUpdater(pluginXmlPath)
    }

    // ========================================
    // Backup Tests
    // ========================================

    @Test
    fun `backupPluginXml creates backup file with timestamp`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val backupPath = updater.backupPluginXml()

        backupPath.exists() shouldBe true
        backupPath.fileName.toString() shouldContain "plugin.xml.backup-"
        backupPath.fileName.toString() shouldContain "-" // Timestamp separator
    }

    @Test
    fun `backupPluginXml preserves original content`(@TempDir tempDir: Path) {
        val originalContent = createBasicPluginXml()
        val pluginXmlPath = createTempPluginXml(tempDir, originalContent)
        val updater = PluginXmlUpdater(pluginXmlPath)

        val backupPath = updater.backupPluginXml()
        val backupContent = backupPath.readText()

        backupContent shouldContain "<id>com.example.theme</id>"
        backupContent shouldContain "<name>Example Theme</name>"
    }

    @Test
    fun `backupPluginXml creates new backup each time`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val backup1 = updater.backupPluginXml()
        Thread.sleep(1000) // Ensure different timestamp
        val backup2 = updater.backupPluginXml()

        backup1 shouldNotBe backup2
        backup1.exists() shouldBe true
        backup2.exists() shouldBe true
    }

    // ========================================
    // Add Theme Provider Tests
    // ========================================

    @Test
    fun `addThemeProvider adds new theme to empty extensions`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        updater.addThemeProvider("test-theme-id", "/themes/test.theme.json")

        val content = pluginXmlPath.readText()
        content shouldContain "themeProvider"
        content shouldContain "id=\"test-theme-id\""
        content shouldContain "path=\"/themes/test.theme.json\""
    }

    @Test
    fun `addThemeProvider adds theme to existing themes`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        updater.addThemeProvider("new-theme-id", "/themes/new.theme.json")

        val content = pluginXmlPath.readText()
        content shouldContain "id=\"new-theme-id\""
        content shouldContain "id=\"one-dark-original\"" // Preserves existing
    }

    @Test
    fun `addThemeProvider updates existing theme with same ID`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        updater.addThemeProvider("one-dark-original", "/themes/updated.theme.json")

        val content = pluginXmlPath.readText()
        content shouldContain "path=\"/themes/updated.theme.json\""

        // Should only appear once
        val idCount = content.split("id=\"one-dark-original\"").size - 1
        idCount shouldBe 1
    }

    @Test
    fun `addThemeProvider creates well-formed XML`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        updater.addThemeProvider("test-id", "/themes/test.theme.json")

        // Should be able to parse the XML again without errors
        PluginXmlUpdater(pluginXmlPath)
    }

    // ========================================
    // Remove Theme Provider Tests
    // ========================================

    @Test
    fun `removeThemeProvider removes existing theme`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        updater.removeThemeProvider("one-dark-original")

        val content = pluginXmlPath.readText()
        content shouldNotContain "id=\"one-dark-original\""
        content shouldContain "id=\"one-dark-italic\"" // Preserves other themes
    }

    @Test
    fun `removeThemeProvider does nothing if theme not found`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val originalContent = pluginXmlPath.readText()
        val updater = PluginXmlUpdater(pluginXmlPath)

        updater.removeThemeProvider("non-existent-id")

        // Content should be essentially the same (may have formatting differences)
        val newContent = pluginXmlPath.readText()
        newContent shouldContain "id=\"one-dark-original\""
        newContent shouldContain "id=\"one-dark-italic\""
    }

    // ========================================
    // Get Existing Theme Providers Tests
    // ========================================

    @Test
    fun `getExistingThemeProviders returns empty list for no themes`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val themes = updater.getExistingThemeProviders()

        themes.shouldBeEmpty()
    }

    @Test
    fun `getExistingThemeProviders returns all themes`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val themes = updater.getExistingThemeProviders()

        themes shouldHaveSize 2
        themes.any { it.id == "one-dark-original" } shouldBe true
        themes.any { it.id == "one-dark-italic" } shouldBe true
    }

    @Test
    fun `getExistingThemeProviders includes path information`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val themes = updater.getExistingThemeProviders()
        val theme = themes.find { it.id == "one-dark-original" }

        theme.shouldNotBeNull()
        theme.path shouldBe "/themes/one_dark.theme.json"
    }

    // ========================================
    // Has Theme Provider Tests
    // ========================================

    @Test
    fun `hasThemeProvider returns true for existing theme`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        updater.hasThemeProvider("one-dark-original") shouldBe true
    }

    @Test
    fun `hasThemeProvider returns false for non-existent theme`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        updater.hasThemeProvider("non-existent") shouldBe false
    }

    // ========================================
    // Remove All WT Theme Providers Tests
    // ========================================

    @Test
    fun `removeAllWtThemeProviders removes only WT themes`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithMixedThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val removed = updater.removeAllWtThemeProviders()

        removed shouldBe 2 // Two WT themes

        val content = pluginXmlPath.readText()
        content shouldNotContain "wt-gruvbox-dark-abc123"
        content shouldNotContain "wt-nord-xyz789"
        content shouldContain "one-dark-original" // Preserves non-WT
        content shouldContain "one-dark-italic" // Preserves non-WT
    }

    @Test
    fun `removeAllWtThemeProviders returns 0 if no WT themes`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val removed = updater.removeAllWtThemeProviders()

        removed shouldBe 0
    }

    @Test
    fun `removeAllWtThemeProviders preserves well-formed XML`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithMixedThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        updater.removeAllWtThemeProviders()

        // Should be able to parse the XML again without errors
        PluginXmlUpdater(pluginXmlPath)
    }

    // ========================================
    // Update Plugin XML (Bulk) Tests
    // ========================================

    @Test
    fun `updatePluginXml adds multiple themes`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val generator = ThemeMetadataGenerator()
        val themes = listOf(
            generator.generateMetadata(createDarkScheme("One Dark")),
            generator.generateMetadata(createDarkScheme("Gruvbox Dark")),
            generator.generateMetadata(createDarkScheme("Nord"))
        )

        val result = updater.updatePluginXml(themes)

        result.success shouldBe true
        result.themesAdded shouldBe 3
        result.backupPath.shouldNotBeNull()
        result.error.shouldBeNull()

        val content = pluginXmlPath.readText()
        themes.forEach { theme ->
            content shouldContain "id=\"${theme.id}\""
        }
    }

    @Test
    fun `updatePluginXml creates backup before update`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val generator = ThemeMetadataGenerator()
        val themes = listOf(generator.generateMetadata(createDarkScheme("Test")))

        val result = updater.updatePluginXml(themes)

        result.backupPath.shouldNotBeNull()
        result.backupPath!!.exists() shouldBe true
    }

    @Test
    fun `updatePluginXml removes old WT themes and adds new ones`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithMixedThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val generator = ThemeMetadataGenerator()
        val themes = listOf(
            generator.generateMetadata(createDarkScheme("Solarized Dark"))
        )

        val result = updater.updatePluginXml(themes)

        result.success shouldBe true
        result.themesRemoved shouldBe 2 // Old WT themes
        result.themesAdded shouldBe 1 // New theme

        val content = pluginXmlPath.readText()
        // Old WT themes should be gone
        content shouldNotContain "wt-gruvbox-dark-abc123"
        content shouldNotContain "wt-nord-xyz789"

        // New theme should be present
        content shouldContain themes[0].id

        // Original non-WT themes should be preserved
        content shouldContain "one-dark-original"
        content shouldContain "one-dark-italic"
    }

    @Test
    fun `updatePluginXml preserves non-WT themes`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithMixedThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val generator = ThemeMetadataGenerator()
        val themes = listOf(
            generator.generateMetadata(createDarkScheme("New Theme"))
        )

        updater.updatePluginXml(themes)

        val content = pluginXmlPath.readText()
        content shouldContain "one-dark-original"
        content shouldContain "one-dark-italic"
    }

    @Test
    fun `updatePluginXml handles empty theme list`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithMixedThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val result = updater.updatePluginXml(emptyList())

        result.success shouldBe true
        result.themesAdded shouldBe 0
        result.themesRemoved shouldBe 2 // Removes existing WT themes

        val content = pluginXmlPath.readText()
        // WT themes should be removed
        content shouldNotContain "wt-gruvbox-dark-abc123"
        content shouldNotContain "wt-nord-xyz789"

        // Non-WT themes should remain
        content shouldContain "one-dark-original"
    }

    @Test
    fun `updatePluginXml uses custom themes directory`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val generator = ThemeMetadataGenerator()
        val themes = listOf(generator.generateMetadata(createDarkScheme("Test")))

        updater.updatePluginXml(themes, themesDir = "/custom/themes")

        val content = pluginXmlPath.readText()
        content shouldContain "path=\"/custom/themes/${themes[0].id}.theme.json\""
    }

    @Test
    fun `updatePluginXml creates well-formed XML`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val generator = ThemeMetadataGenerator()
        val themes = listOf(
            generator.generateMetadata(createDarkScheme("Theme 1")),
            generator.generateMetadata(createDarkScheme("Theme 2")),
            generator.generateMetadata(createDarkScheme("Theme 3"))
        )

        updater.updatePluginXml(themes)

        // Should be able to create a new updater without errors
        PluginXmlUpdater(pluginXmlPath)
    }

    // ========================================
    // UpdateResult Tests
    // ========================================

    @Test
    fun `UpdateResult toSummaryString shows success info`(@TempDir tempDir: Path) {
        val backupPath = tempDir.resolve("backup.xml")
        val result = UpdateResult(
            success = true,
            themesAdded = 5,
            themesRemoved = 2,
            backupPath = backupPath,
            error = null
        )

        val summary = result.toSummaryString()

        summary shouldContain "successfully"
        summary shouldContain "5"
        summary shouldContain "2"
        summary shouldContain backupPath.toString()
    }

    @Test
    fun `UpdateResult toSummaryString shows failure info`() {
        val result = UpdateResult(
            success = false,
            themesAdded = 0,
            themesRemoved = 0,
            backupPath = null,
            error = "Test error message"
        )

        val summary = result.toSummaryString()

        summary shouldContain "failed"
        summary shouldContain "Test error message"
    }

    // ========================================
    // Error Handling Tests
    // ========================================

    @Test
    fun `updatePluginXml handles invalid XML gracefully`(@TempDir tempDir: Path) {
        // Create invalid XML
        val pluginXmlPath = tempDir.resolve("plugin.xml")
        pluginXmlPath.writeText("<invalid><xml>")

        val updater = PluginXmlUpdater(pluginXmlPath)
        val generator = ThemeMetadataGenerator()
        val themes = listOf(generator.generateMetadata(createDarkScheme("Test")))

        val result = updater.updatePluginXml(themes)

        result.success shouldBe false
        result.error.shouldNotBeNull()
    }

    @Test
    fun `updatePluginXml preserves XML declaration`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)

        val generator = ThemeMetadataGenerator()
        val themes = listOf(generator.generateMetadata(createDarkScheme("Test")))

        updater.updatePluginXml(themes)

        val content = pluginXmlPath.readText()
        content shouldContain "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    }

    // ========================================
    // Integration Tests
    // ========================================

    @Test
    fun `full workflow - add, update, remove themes`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createBasicPluginXml())
        val updater = PluginXmlUpdater(pluginXmlPath)
        val generator = ThemeMetadataGenerator()

        // Step 1: Add initial themes
        val initialThemes = listOf(
            generator.generateMetadata(createDarkScheme("Theme A")),
            generator.generateMetadata(createDarkScheme("Theme B"))
        )
        var result = updater.updatePluginXml(initialThemes)
        result.success shouldBe true
        result.themesAdded shouldBe 2

        // Step 2: Update with new themes (remove old, add new)
        val updatedThemes = listOf(
            generator.generateMetadata(createDarkScheme("Theme C")),
            generator.generateMetadata(createDarkScheme("Theme D")),
            generator.generateMetadata(createDarkScheme("Theme E"))
        )
        result = updater.updatePluginXml(updatedThemes)
        result.success shouldBe true
        result.themesAdded shouldBe 3
        result.themesRemoved shouldBe 2

        // Step 3: Verify final state
        val themes = updater.getExistingThemeProviders()
        themes shouldHaveSize 3
    }

    @Test
    fun `preserves One Dark themes during updates`(@TempDir tempDir: Path) {
        val pluginXmlPath = createTempPluginXml(tempDir, createPluginXmlWithThemes())
        val updater = PluginXmlUpdater(pluginXmlPath)
        val generator = ThemeMetadataGenerator()

        // Add WT themes
        val wtThemes = listOf(
            generator.generateMetadata(createDarkScheme("Gruvbox")),
            generator.generateMetadata(createDarkScheme("Nord"))
        )
        updater.updatePluginXml(wtThemes)

        // Verify One Dark themes are still present
        val themes = updater.getExistingThemeProviders()
        themes.any { it.id == "one-dark-original" } shouldBe true
        themes.any { it.id == "one-dark-italic" } shouldBe true
    }
}
