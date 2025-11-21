package generators

import colorschemes.WindowsTerminalColorScheme
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

class ThemeMetadataGeneratorTest {

    private val generator = ThemeMetadataGenerator(
        generatorVersion = "1.0.0",
        defaultAuthor = "Test Author",
        intellijVersion = "2020.3+"
    )

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

    /**
     * Creates a sample light theme color scheme for testing
     */
    private fun createLightScheme(name: String = "One Light"): WindowsTerminalColorScheme {
        return WindowsTerminalColorScheme(
            name = name,
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

    // ========================================
    // Theme ID Generation Tests
    // ========================================

    @Test
    fun `generateThemeId creates valid ID format`() {
        val scheme = createDarkScheme("One Dark")
        val id = generator.generateThemeId(scheme)

        // Should start with "wt-"
        id shouldStartWith "wt-"

        // Should match format: wt-{name}-{hash}
        id shouldMatch Regex("wt-[a-z0-9-]+-[a-f0-9]{8}")
    }

    @Test
    fun `generateThemeId is deterministic for same scheme`() {
        val scheme = createDarkScheme("One Dark")
        val id1 = generator.generateThemeId(scheme)
        val id2 = generator.generateThemeId(scheme)

        id1 shouldBe id2
    }

    @Test
    fun `generateThemeId generates different IDs for different schemes`() {
        val scheme1 = createDarkScheme("One Dark")
        val scheme2 = createLightScheme("One Light")

        val id1 = generator.generateThemeId(scheme1)
        val id2 = generator.generateThemeId(scheme2)

        id1 shouldNotBe id2
    }

    @Test
    fun `generateThemeId generates different IDs for same name but different colors`() {
        val scheme1 = createDarkScheme("One Dark")
        val scheme2 = scheme1.copy(background = "#000000") // Different color

        val id1 = generator.generateThemeId(scheme1)
        val id2 = generator.generateThemeId(scheme2)

        id1 shouldNotBe id2
    }

    @Test
    fun `generateThemeId sanitizes special characters in name`() {
        val scheme = createDarkScheme("My@Special#Theme!")
        val id = generator.generateThemeId(scheme)

        // Should not contain special characters
        id shouldNotContain "@"
        id shouldNotContain "#"
        id shouldNotContain "!"

        // Should still be valid format
        id shouldMatch Regex("wt-[a-z0-9-]+-[a-f0-9]{8}")
    }

    @Test
    fun `generateThemeId handles long names by truncating`() {
        val longName = "A".repeat(50)
        val scheme = createDarkScheme(longName)
        val id = generator.generateThemeId(scheme)

        // ID should not be excessively long
        id.length shouldBe (3 + 20 + 1 + 8) // "wt-" + max 20 chars + "-" + 8 char hash
    }

    @Test
    fun `generateThemeId handles underscores and hyphens`() {
        val scheme = createDarkScheme("Gruvbox_Dark-Hard")
        val id = generator.generateThemeId(scheme)

        // Should convert to lowercase with hyphens
        id shouldStartWith "wt-gruvbox-dark-hard"
    }

    @Test
    fun `generateThemeId handles spaces`() {
        val scheme = createDarkScheme("Nord Polar Night")
        val id = generator.generateThemeId(scheme)

        // Spaces should be converted to hyphens
        id shouldContain "nord-polar-night"
    }

    // ========================================
    // Theme Name Generation Tests
    // ========================================

    @Test
    fun `generateThemeName formats name as Title Case`() {
        val scheme = createDarkScheme("one dark")
        val name = generator.generateThemeName(scheme)

        name shouldBe "WT One Dark"
    }

    @Test
    fun `generateThemeName adds WT prefix`() {
        val scheme = createDarkScheme("Solarized")
        val name = generator.generateThemeName(scheme)

        name shouldStartWith "WT "
    }

    @Test
    fun `generateThemeName removes special characters`() {
        val scheme = createDarkScheme("My@Theme#Name!")
        val name = generator.generateThemeName(scheme)

        name shouldNotContain "@"
        name shouldNotContain "#"
        name shouldNotContain "!"
        name shouldBe "WT My Theme Name"
    }

    @Test
    fun `generateThemeName converts underscores to spaces`() {
        val scheme = createDarkScheme("Gruvbox_Dark_Hard")
        val name = generator.generateThemeName(scheme)

        name shouldBe "WT Gruvbox Dark Hard"
    }

    @Test
    fun `generateThemeName converts hyphens to spaces`() {
        val scheme = createDarkScheme("One-Dark-Pro")
        val name = generator.generateThemeName(scheme)

        name shouldBe "WT One Dark Pro"
    }

    @Test
    fun `generateThemeName handles multiple spaces`() {
        val scheme = createDarkScheme("One    Dark")
        val name = generator.generateThemeName(scheme)

        name shouldBe "WT One Dark"
    }

    @Test
    fun `generateThemeName handles mixed case`() {
        val scheme = createDarkScheme("SolarizedDark")
        val name = generator.generateThemeName(scheme)

        name shouldBe "WT Solarizeddark"
    }

    // ========================================
    // Display Name Generation Tests
    // ========================================

    @Test
    fun `generateDisplayName removes WT prefix`() {
        val scheme = createDarkScheme("One Dark")
        val displayName = generator.generateDisplayName(scheme)

        displayName shouldBe "One Dark"
        displayName shouldNotContain "WT"
    }

    @Test
    fun `generateDisplayName formats name properly`() {
        val scheme = createDarkScheme("gruvbox-dark")
        val displayName = generator.generateDisplayName(scheme)

        displayName shouldBe "Gruvbox Dark"
    }

    // ========================================
    // Dark Theme Detection Tests
    // ========================================

    @Test
    fun `isDarkTheme returns true for dark backgrounds`() {
        val darkScheme = createDarkScheme()
        generator.isDarkTheme(darkScheme) shouldBe true
    }

    @Test
    fun `isDarkTheme returns false for light backgrounds`() {
        val lightScheme = createLightScheme()
        generator.isDarkTheme(lightScheme) shouldBe false
    }

    @Test
    fun `isDarkTheme handles edge case near threshold`() {
        val scheme = createDarkScheme().copy(background = "#666666") // Gray
        val isDark = generator.isDarkTheme(scheme)

        // Should be consistent with threshold
        isDark shouldBe false // luminance ~100, at threshold boundary
    }

    @Test
    fun `isDarkTheme correctly identifies very dark theme`() {
        val scheme = createDarkScheme().copy(background = "#000000")
        generator.isDarkTheme(scheme) shouldBe true
    }

    @Test
    fun `isDarkTheme correctly identifies very light theme`() {
        val scheme = createLightScheme().copy(background = "#ffffff")
        generator.isDarkTheme(scheme) shouldBe false
    }

    // ========================================
    // Metadata Generation Tests
    // ========================================

    @Test
    fun `generateMetadata creates complete metadata`() {
        val scheme = createDarkScheme("One Dark")
        val metadata = generator.generateMetadata(scheme)

        metadata.id shouldNotBe ""
        metadata.name shouldBe "WT One Dark"
        metadata.displayName shouldBe "One Dark"
        metadata.author shouldBe "Test Author"
        metadata.sourceScheme shouldBe "One Dark"
        metadata.generatorVersion shouldBe "1.0.0"
        metadata.intellijVersion shouldBe "2020.3+"
        metadata.isDark shouldBe true
        metadata.themeVersion shouldBe "1.0.0"
        metadata.minIntellijVersion shouldBe ThemeMetadataGenerator.MIN_INTELLIJ_VERSION
        metadata.maxIntellijVersion shouldBe ThemeMetadataGenerator.MAX_INTELLIJ_VERSION
        metadata.fingerprint.length shouldBe 32 // MD5 hash
    }

    @Test
    fun `generateMetadata uses default author when not specified`() {
        val scheme = createDarkScheme()
        val metadata = generator.generateMetadata(scheme)

        metadata.author shouldBe "Test Author"
    }

    @Test
    fun `generateMetadata allows custom author`() {
        val scheme = createDarkScheme()
        val metadata = generator.generateMetadata(scheme, author = "Custom Author")

        metadata.author shouldBe "Custom Author"
    }

    @Test
    fun `generateMetadata includes valid timestamp`() {
        val scheme = createDarkScheme()
        val metadata = generator.generateMetadata(scheme)

        // Should match ISO 8601 format: YYYY-MM-DDTHH:mm:ss
        metadata.createdDate shouldMatch Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")
    }

    @Test
    fun `generateMetadata correctly identifies dark theme`() {
        val darkScheme = createDarkScheme()
        val metadata = generator.generateMetadata(darkScheme)

        metadata.isDark shouldBe true
    }

    @Test
    fun `generateMetadata correctly identifies light theme`() {
        val lightScheme = createLightScheme()
        val metadata = generator.generateMetadata(lightScheme)

        metadata.isDark shouldBe false
    }

    // ========================================
    // Fingerprinting Tests
    // ========================================

    @Test
    fun `generateFingerprint creates 32 character hash`() {
        val scheme = createDarkScheme()
        val fingerprint = generator.generateFingerprint(scheme)

        fingerprint.length shouldBe 32
        fingerprint shouldMatch Regex("[a-f0-9]{32}")
    }

    @Test
    fun `generateFingerprint is deterministic`() {
        val scheme = createDarkScheme()
        val fp1 = generator.generateFingerprint(scheme)
        val fp2 = generator.generateFingerprint(scheme)

        fp1 shouldBe fp2
    }

    @Test
    fun `generateFingerprint differs for different colors`() {
        val scheme1 = createDarkScheme()
        val scheme2 = scheme1.copy(background = "#000000")

        val fp1 = generator.generateFingerprint(scheme1)
        val fp2 = generator.generateFingerprint(scheme2)

        fp1 shouldNotBe fp2
    }

    @Test
    fun `generateFingerprint same for different names with same colors`() {
        val scheme1 = createDarkScheme("Theme A")
        val scheme2 = scheme1.copy(name = "Theme B")

        val fp1 = generator.generateFingerprint(scheme1)
        val fp2 = generator.generateFingerprint(scheme2)

        fp1 shouldBe fp2
    }

    // ========================================
    // Duplicate Detection Tests
    // ========================================

    @Test
    fun `detectDuplicates finds no duplicates in unique schemes`() {
        val schemes = listOf(
            createDarkScheme("One Dark"),
            createLightScheme("One Light"),
            createDarkScheme("Gruvbox").copy(background = "#1d2021")
        )

        val duplicates = generator.detectDuplicates(schemes)

        duplicates.shouldBeEmpty()
    }

    @Test
    fun `detectDuplicates finds duplicates with different names`() {
        val scheme1 = createDarkScheme("One Dark Original")
        val scheme2 = scheme1.copy(name = "One Dark Copy")

        val duplicates = generator.detectDuplicates(listOf(scheme1, scheme2))

        duplicates shouldHaveSize 1
        duplicates[0].first shouldBe "One Dark Original"
        duplicates[0].second shouldBe "One Dark Copy"
    }

    @Test
    fun `detectDuplicates finds all pairs in multiple duplicates`() {
        val base = createDarkScheme("Base")
        val schemes = listOf(
            base.copy(name = "Theme A"),
            base.copy(name = "Theme B"),
            base.copy(name = "Theme C")
        )

        val duplicates = generator.detectDuplicates(schemes)

        // Should find 3 pairs: (A,B), (A,C), (B,C)
        duplicates shouldHaveSize 3
    }

    @Test
    fun `detectDuplicates handles empty list`() {
        val duplicates = generator.detectDuplicates(emptyList())
        duplicates.shouldBeEmpty()
    }

    @Test
    fun `detectDuplicates handles single scheme`() {
        val duplicates = generator.detectDuplicates(listOf(createDarkScheme()))
        duplicates.shouldBeEmpty()
    }

    // ========================================
    // Version Compatibility Tests
    // ========================================

    @Test
    fun `checkCompatibility accepts minimum version`() {
        generator.checkCompatibility("203.7148.57") shouldBe true
    }

    @Test
    fun `checkCompatibility accepts higher version`() {
        generator.checkCompatibility("231.8109.175") shouldBe true
    }

    @Test
    fun `checkCompatibility rejects lower version`() {
        generator.checkCompatibility("202.0.0") shouldBe false
    }

    @Test
    fun `checkCompatibility handles year dot major format`() {
        generator.checkCompatibility("2023.1") shouldBe true
        generator.checkCompatibility("2020.3") shouldBe true
        generator.checkCompatibility("2019.1") shouldBe false
    }

    @Test
    fun `checkCompatibility handles invalid version format`() {
        generator.checkCompatibility("invalid") shouldBe false
        generator.checkCompatibility("") shouldBe false
        generator.checkCompatibility("1.2") shouldBe false
    }

    @Test
    fun `checkCompatibility handles edge cases`() {
        // Exactly at minimum
        generator.checkCompatibility("203.7148.57") shouldBe true

        // Just above minimum
        generator.checkCompatibility("203.7148.58") shouldBe true

        // Just below minimum
        generator.checkCompatibility("203.7148.56") shouldBe false
    }

    // ========================================
    // Metadata Validation Tests
    // ========================================

    @Test
    fun `validateMetadata accepts valid metadata`() {
        val scheme = createDarkScheme()
        val metadata = generator.generateMetadata(scheme)

        val errors = generator.validateMetadata(metadata)

        errors.shouldBeEmpty()
    }

    @Test
    fun `validateMetadata detects blank ID`() {
        val metadata = ThemeMetadata(
            id = "",
            name = "Test Theme",
            displayName = "Test",
            author = "Author",
            createdDate = "2023-01-01T00:00:00",
            sourceScheme = "Test",
            generatorVersion = "1.0.0",
            intellijVersion = "2020.3+",
            isDark = true,
            fingerprint = "abc123"
        )

        val errors = generator.validateMetadata(metadata)

        errors shouldContain "Theme ID cannot be blank"
    }

    @Test
    fun `validateMetadata detects blank name`() {
        val metadata = ThemeMetadata(
            id = "test-id",
            name = "",
            displayName = "Test",
            author = "Author",
            createdDate = "2023-01-01T00:00:00",
            sourceScheme = "Test",
            generatorVersion = "1.0.0",
            intellijVersion = "2020.3+",
            isDark = true,
            fingerprint = "abc123"
        )

        val errors = generator.validateMetadata(metadata)

        errors shouldContain "Theme name cannot be blank"
    }

    @Test
    fun `validateMetadata detects invalid theme version format`() {
        val metadata = ThemeMetadata(
            id = "test-id",
            name = "Test",
            displayName = "Test",
            author = "Author",
            createdDate = "2023-01-01T00:00:00",
            sourceScheme = "Test",
            generatorVersion = "1.0.0",
            intellijVersion = "2020.3+",
            isDark = true,
            themeVersion = "1.0", // Invalid: should be x.y.z
            fingerprint = "abc123"
        )

        val errors = generator.validateMetadata(metadata)

        errors shouldContain "Theme version must be in semantic versioning format (e.g., 1.0.0)"
    }

    @Test
    fun `validateMetadata detects invalid IntelliJ version format`() {
        val metadata = ThemeMetadata(
            id = "test-id",
            name = "Test",
            displayName = "Test",
            author = "Author",
            createdDate = "2023-01-01T00:00:00",
            sourceScheme = "Test",
            generatorVersion = "1.0.0",
            intellijVersion = "2020.3+",
            isDark = true,
            minIntellijVersion = "invalid", // Invalid format
            fingerprint = "abc123"
        )

        val errors = generator.validateMetadata(metadata)

        errors shouldContain "Min IntelliJ version must be in build number format (e.g., 203.7148.57)"
    }

    @Test
    fun `validateMetadata detects multiple errors`() {
        val metadata = ThemeMetadata(
            id = "",
            name = "",
            displayName = "Test",
            author = "",
            createdDate = "2023-01-01T00:00:00",
            sourceScheme = "Test",
            generatorVersion = "1.0.0",
            intellijVersion = "2020.3+",
            isDark = true,
            themeVersion = "1.0",
            minIntellijVersion = "invalid",
            fingerprint = "abc123"
        )

        val errors = generator.validateMetadata(metadata)

        errors shouldHaveSize 5
    }

    // ========================================
    // ThemeMetadata toMap() Tests
    // ========================================

    @Test
    fun `ThemeMetadata toMap includes all properties`() {
        val scheme = createDarkScheme()
        val metadata = generator.generateMetadata(scheme)

        val map = metadata.toMap()

        map["id"] shouldBe metadata.id
        map["name"] shouldBe metadata.name
        map["displayName"] shouldBe metadata.displayName
        map["author"] shouldBe metadata.author
        map["createdDate"] shouldBe metadata.createdDate
        map["sourceScheme"] shouldBe metadata.sourceScheme
        map["generatorVersion"] shouldBe metadata.generatorVersion
        map["intellijVersion"] shouldBe metadata.intellijVersion
        map["isDark"] shouldBe metadata.isDark
        map["themeVersion"] shouldBe metadata.themeVersion
        map["minIntellijVersion"] shouldBe metadata.minIntellijVersion
        map["maxIntellijVersion"] shouldBe metadata.maxIntellijVersion
        map["fingerprint"] shouldBe metadata.fingerprint
    }

    // ========================================
    // ThemeMetadata toSummaryString() Tests
    // ========================================

    @Test
    fun `ThemeMetadata toSummaryString includes key information`() {
        val scheme = createDarkScheme("Test Theme")
        val metadata = generator.generateMetadata(scheme)

        val summary = metadata.toSummaryString()

        summary shouldContain metadata.name
        summary shouldContain metadata.id
        summary shouldContain metadata.author
        summary shouldContain metadata.sourceScheme
        summary shouldContain "Dark" // Since it's a dark theme
    }
}
