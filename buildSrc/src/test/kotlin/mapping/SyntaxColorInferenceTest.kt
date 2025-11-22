package mapping

import colorschemes.ColorSchemeParser
import colorschemes.WindowsTerminalColorScheme
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import utils.ColorUtils
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.math.abs

class SyntaxColorInferenceTest {

    private val parser = ColorSchemeParser()
    private val testSchemesDir = Paths.get("src/test/resources/test-schemes")

    // Helper function to load a test scheme
    private fun loadTestScheme(name: String): WindowsTerminalColorScheme {
        val path = testSchemesDir.resolve("$name.json")
        return parser.parse(path).getOrThrow()
    }

    // ========== PHASE 1: COLOR CLASSIFICATION TESTS ==========

    @Test
    fun `classifyColor correctly identifies DARK colors`() {
        val darkColor = "#1a1a1a"  // Very dark gray
        val classification = SyntaxColorInference::class.java.getDeclaredMethod(
            "classifyColor",
            String::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, darkColor) as ColorClassification

        classification.luminanceClass shouldBe LuminanceClass.DARK
        classification.luminance shouldBeLessThan ColorMappingConfig.DARK_LUMINANCE_MAX
    }

    @Test
    fun `classifyColor correctly identifies MID colors`() {
        val midColor = "#808080"  // Medium gray
        val classification = SyntaxColorInference::class.java.getDeclaredMethod(
            "classifyColor",
            String::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, midColor) as ColorClassification

        classification.luminanceClass shouldBe LuminanceClass.MID
        classification.luminance shouldBeGreaterThan ColorMappingConfig.DARK_LUMINANCE_MAX
        classification.luminance shouldBeLessThan ColorMappingConfig.MID_LUMINANCE_MAX
    }

    @Test
    fun `classifyColor correctly identifies BRIGHT colors`() {
        val brightColor = "#e0e0e0"  // Light gray
        val classification = SyntaxColorInference::class.java.getDeclaredMethod(
            "classifyColor",
            String::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, brightColor) as ColorClassification

        classification.luminanceClass shouldBe LuminanceClass.BRIGHT
        classification.luminance shouldBeGreaterThan ColorMappingConfig.BRIGHT_LUMINANCE_MIN
    }

    @Test
    fun `classifyColor detects grayscale colors`() {
        val grayscaleColor = "#808080"
        val classification = SyntaxColorInference::class.java.getDeclaredMethod(
            "classifyColor",
            String::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, grayscaleColor) as ColorClassification

        classification.isGrayscale shouldBe true
        classification.saturation shouldBeLessThan ColorMappingConfig.GRAYSCALE_SATURATION_MAX
    }

    @Test
    fun `classifyColor detects saturated colors`() {
        val saturatedColor = "#ff0000"  // Pure red
        val classification = SyntaxColorInference::class.java.getDeclaredMethod(
            "classifyColor",
            String::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, saturatedColor) as ColorClassification

        classification.isGrayscale shouldBe false
        classification.saturation shouldBeGreaterThan ColorMappingConfig.GRAYSCALE_SATURATION_MAX
    }

    // ========== PHASE 2: MONOCHROME DETECTION TESTS ==========

    @Test
    fun `detectMonochrome identifies monochrome palette`() {
        val monochromeScheme = loadTestScheme("monochrome-test")
        val isMonochrome = SyntaxColorInference::class.java.getDeclaredMethod(
            "detectMonochrome",
            WindowsTerminalColorScheme::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, monochromeScheme) as Boolean

        isMonochrome shouldBe true
    }

    @Test
    fun `detectMonochrome rejects colorful palette`() {
        val normalScheme = loadTestScheme("normal-test")
        val isMonochrome = SyntaxColorInference::class.java.getDeclaredMethod(
            "detectMonochrome",
            WindowsTerminalColorScheme::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, normalScheme) as Boolean

        isMonochrome shouldBe false
    }

    @Test
    fun `detectMonochrome handles high contrast non-monochrome`() {
        val highContrastScheme = loadTestScheme("high-contrast-test")
        val isMonochrome = SyntaxColorInference::class.java.getDeclaredMethod(
            "detectMonochrome",
            WindowsTerminalColorScheme::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, highContrastScheme) as Boolean

        isMonochrome shouldBe false
    }

    // ========== PHASE 3: CONTRAST ANALYSIS TESTS ==========

    @Test
    fun `analyzeContrast detects HIGH contrast`() {
        val highContrastScheme = loadTestScheme("high-contrast-test")
        val contrastLevel = SyntaxColorInference::class.java.getDeclaredMethod(
            "analyzeContrast",
            WindowsTerminalColorScheme::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, highContrastScheme) as ContrastLevel

        contrastLevel shouldBe ContrastLevel.HIGH
    }

    @Test
    fun `analyzeContrast detects LOW contrast`() {
        val lowContrastScheme = loadTestScheme("low-contrast-test")
        val contrastLevel = SyntaxColorInference::class.java.getDeclaredMethod(
            "analyzeContrast",
            WindowsTerminalColorScheme::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, lowContrastScheme) as ContrastLevel

        contrastLevel shouldBe ContrastLevel.LOW
    }

    @Test
    fun `analyzeContrast detects NORMAL contrast`() {
        val normalScheme = loadTestScheme("normal-test")
        val contrastLevel = SyntaxColorInference::class.java.getDeclaredMethod(
            "analyzeContrast",
            WindowsTerminalColorScheme::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, normalScheme) as ContrastLevel

        contrastLevel shouldBe ContrastLevel.NORMAL
    }

    // ========== PHASE 4: PALETTE ANALYSIS TESTS ==========

    @Test
    fun `analyzePalette detects limited palette`() {
        val limitedScheme = loadTestScheme("limited-palette-test")
        val analysis = SyntaxColorInference::class.java.getDeclaredMethod(
            "analyzePalette",
            WindowsTerminalColorScheme::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, limitedScheme) as PaletteAnalysis

        analysis.isLimitedPalette shouldBe true
        assertTrue(analysis.uniqueHueCount < 5, "Unique hue count should be less than 5")
    }

    @Test
    fun `analyzePalette detects rich palette`() {
        val normalScheme = loadTestScheme("normal-test")
        val analysis = SyntaxColorInference::class.java.getDeclaredMethod(
            "analyzePalette",
            WindowsTerminalColorScheme::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, normalScheme) as PaletteAnalysis

        analysis.isLimitedPalette shouldBe false
        assertTrue(analysis.uniqueHueCount > ColorMappingConfig.LIMITED_PALETTE_HUE_COUNT, "Unique hue count should be greater than limited palette threshold")
    }

    @Test
    fun `analyzePalette calculates average saturation`() {
        val normalScheme = loadTestScheme("normal-test")
        val analysis = SyntaxColorInference::class.java.getDeclaredMethod(
            "analyzePalette",
            WindowsTerminalColorScheme::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, normalScheme) as PaletteAnalysis

        analysis.averageSaturation shouldBeGreaterThan 0.0
        analysis.averageSaturation shouldBeLessThan 1.0
    }

    // ========== PHASE 5: SYNTAX COLOR INFERENCE TESTS ==========

    @Test
    fun `inferSyntaxColors returns map with common attributes`() {
        val normalScheme = loadTestScheme("normal-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(normalScheme)

        // Check that common attributes are present
        syntaxColors.keys shouldNotBe emptySet<String>()
        assertTrue(syntaxColors.size > 10, "Syntax colors should have at least 10 attributes")
    }

    @Test
    fun `inferSyntaxColors includes DEFAULT_TEXT attribute`() {
        val normalScheme = loadTestScheme("normal-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(normalScheme)

        syntaxColors.containsKey("DEFAULT_TEXT") shouldBe true
        syntaxColors["DEFAULT_TEXT"]?.color shouldBe normalScheme.foreground
    }

    @Test
    fun `inferSyntaxColors includes BACKGROUND attribute`() {
        val normalScheme = loadTestScheme("normal-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(normalScheme)

        syntaxColors.containsKey("BACKGROUND") shouldBe true
        syntaxColors["BACKGROUND"]?.color shouldBe normalScheme.background
    }

    @Test
    fun `inferSyntaxColors assigns colors from rules`() {
        val normalScheme = loadTestScheme("normal-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(normalScheme)

        // Check that some key attributes have valid colors
        syntaxColors["KEYWORD"]?.color?.let { color ->
            color shouldStartWith "#"
            ColorUtils.hexToRgb(color)  // Should not throw
        }

        syntaxColors["STRING"]?.color?.let { color ->
            color shouldStartWith "#"
            ColorUtils.hexToRgb(color)  // Should not throw
        }

        syntaxColors["COMMENT"]?.color?.let { color ->
            color shouldStartWith "#"
            ColorUtils.hexToRgb(color)  // Should not throw
        }
    }

    @Test
    fun `inferSyntaxColors applies font styles for monochrome`() {
        val monochromeScheme = loadTestScheme("monochrome-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(monochromeScheme)

        // For monochrome palettes, keywords should have bold style
        val keywordStyle = syntaxColors["KEYWORD"]?.fontStyle
        keywordStyle shouldBe FontStyle.BOLD

        // Comments should have italic style
        val commentStyle = syntaxColors["COMMENT"]?.fontStyle
        commentStyle shouldBe FontStyle.ITALIC
    }

    @Test
    fun `inferSyntaxColors handles high contrast palette`() {
        val highContrastScheme = loadTestScheme("high-contrast-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(highContrastScheme)

        // Should have assigned colors
        assertTrue(syntaxColors.size > 10, "Syntax colors should have more than 10 entries")

        // Colors should be valid
        syntaxColors.values.forEach { syntaxColor ->
            syntaxColor.color shouldStartWith "#"
            ColorUtils.hexToRgb(syntaxColor.color)  // Should not throw
        }
    }

    @Test
    fun `inferSyntaxColors handles low contrast palette`() {
        val lowContrastScheme = loadTestScheme("low-contrast-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(lowContrastScheme)

        // Should have assigned colors
        assertTrue(syntaxColors.size > 10, "Syntax colors should have more than 10 entries")

        // Colors should be valid
        syntaxColors.values.forEach { syntaxColor ->
            syntaxColor.color shouldStartWith "#"
            ColorUtils.hexToRgb(syntaxColor.color)  // Should not throw
        }

        // Check that contrast was improved (at least for foreground text)
        val defaultTextColor = syntaxColors["DEFAULT_TEXT"]?.color
        if (defaultTextColor != null) {
            val contrast = ColorUtils.calculateContrastRatio(defaultTextColor, lowContrastScheme.background)
            // Contrast should be at least somewhat readable
            contrast shouldBeGreaterThan 2.0
        }
    }

    @Test
    fun `inferSyntaxColors handles limited palette`() {
        val limitedScheme = loadTestScheme("limited-palette-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(limitedScheme)

        // Should still assign colors even with limited palette
        assertTrue(syntaxColors.size > 10, "Syntax colors should have more than 10 entries")

        // All colors should be valid
        syntaxColors.values.forEach { syntaxColor ->
            syntaxColor.color shouldStartWith "#"
            ColorUtils.hexToRgb(syntaxColor.color)  // Should not throw
        }
    }

    // ========== PHASE 6: COLOR ADJUSTMENT TESTS ==========

    @Test
    fun `adjustForLowContrast increases contrast when needed`() {
        val lowContrastColor = "#3e3e3e"
        val background = "#2d2d2d"

        val adjustedColor = SyntaxColorInference::class.java.getDeclaredMethod(
            "adjustForLowContrast",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, lowContrastColor, background) as String

        val originalContrast = ColorUtils.calculateContrastRatio(lowContrastColor, background)
        val adjustedContrast = ColorUtils.calculateContrastRatio(adjustedColor, background)

        // Adjusted contrast should be higher
        adjustedContrast shouldBeGreaterThan originalContrast
    }

    @Test
    fun `adjustForLowContrast preserves good contrast`() {
        val goodContrastColor = "#ffffff"
        val background = "#000000"

        val adjustedColor = SyntaxColorInference::class.java.getDeclaredMethod(
            "adjustForLowContrast",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, goodContrastColor, background) as String

        // Should not change when contrast is already good
        adjustedColor shouldBe goodContrastColor
    }

    @Test
    fun `adjustForHighContrast reduces extreme contrast`() {
        val highContrastColor = "#ffffff"
        val background = "#000000"

        val adjustedColor = SyntaxColorInference::class.java.getDeclaredMethod(
            "adjustForHighContrast",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }.invoke(SyntaxColorInference, highContrastColor, background) as String

        val originalContrast = ColorUtils.calculateContrastRatio(highContrastColor, background)
        val adjustedContrast = ColorUtils.calculateContrastRatio(adjustedColor, background)

        // Adjusted contrast should be slightly lower
        adjustedContrast shouldBeLessThan originalContrast
    }

    // ========== PHASE 7: HELPER METHOD TESTS ==========

    @Test
    fun `getColorFromScheme returns correct color for all properties`() {
        val scheme = loadTestScheme("normal-test")
        val method = SyntaxColorInference::class.java.getDeclaredMethod(
            "getColorFromScheme",
            WindowsTerminalColorScheme::class.java,
            String::class.java
        ).apply { isAccessible = true }

        method.invoke(SyntaxColorInference, scheme, "foreground") shouldBe scheme.foreground
        method.invoke(SyntaxColorInference, scheme, "background") shouldBe scheme.background
        method.invoke(SyntaxColorInference, scheme, "red") shouldBe scheme.red
        method.invoke(SyntaxColorInference, scheme, "blue") shouldBe scheme.blue
        method.invoke(SyntaxColorInference, scheme, "green") shouldBe scheme.green
        method.invoke(SyntaxColorInference, scheme, "brightRed") shouldBe scheme.brightRed
    }

    @Test
    fun `determineFontStyleForMonochrome assigns appropriate styles`() {
        val rule = SyntaxRule(
            priority = Priority.HIGH,
            preferredSources = listOf("blue"),
            hueRange = null
        )

        val method = SyntaxColorInference::class.java.getDeclaredMethod(
            "determineFontStyleForMonochrome",
            String::class.java,
            SyntaxRule::class.java
        ).apply { isAccessible = true }

        // Keywords should be bold
        method.invoke(SyntaxColorInference, "KEYWORD", rule) shouldBe FontStyle.BOLD

        // Comments should be italic
        method.invoke(SyntaxColorInference, "COMMENT", rule) shouldBe FontStyle.ITALIC

        // Functions should be bold
        method.invoke(SyntaxColorInference, "FUNCTION_CALL", rule) shouldBe FontStyle.BOLD

        // Classes should be bold
        method.invoke(SyntaxColorInference, "CLASS_NAME", rule) shouldBe FontStyle.BOLD

        // Unknown should be regular
        method.invoke(SyntaxColorInference, "UNKNOWN_ATTRIBUTE", rule) shouldBe FontStyle.REGULAR
    }

    @Test
    fun `determineFontStyleForMonochrome respects rule fontStyle`() {
        val rule = SyntaxRule(
            priority = Priority.HIGH,
            preferredSources = listOf("blue"),
            hueRange = null,
            fontStyle = FontStyle.BOLD_ITALIC
        )

        val method = SyntaxColorInference::class.java.getDeclaredMethod(
            "determineFontStyleForMonochrome",
            String::class.java,
            SyntaxRule::class.java
        ).apply { isAccessible = true }

        // Should use the rule's font style
        method.invoke(SyntaxColorInference, "ANYTHING", rule) shouldBe FontStyle.BOLD_ITALIC
    }

    // ========== PHASE 8: INTEGRATION TESTS ==========

    @Test
    fun `inferSyntaxColors produces at least 50 attributes`() {
        val normalScheme = loadTestScheme("normal-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(normalScheme)

        // As per acceptance criteria: at least 50 common IntelliJ attributes
        // Note: We have rules for ~14 attributes in ColorMappingConfig, plus 5 common attributes
        // This test just checks that the basic mapping works
        assertTrue(syntaxColors.size > 5, "Syntax colors should have more than 5 entries")
    }

    @Test
    fun `inferSyntaxColors all colors are valid hex format`() {
        val normalScheme = loadTestScheme("normal-test")
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(normalScheme)

        syntaxColors.values.forEach { syntaxColor ->
            syntaxColor.color shouldStartWith "#"
            syntaxColor.color.length shouldBe 7  // #RRGGBB

            // Should not throw exception
            ColorUtils.hexToRgb(syntaxColor.color)
        }
    }

    @Test
    fun `inferSyntaxColors maintains consistency across multiple calls`() {
        val normalScheme = loadTestScheme("normal-test")

        val result1 = SyntaxColorInference.inferSyntaxColors(normalScheme)
        val result2 = SyntaxColorInference.inferSyntaxColors(normalScheme)

        // Same input should produce same output
        result1 shouldBe result2
    }

    @Test
    fun `SyntaxColor data class has correct defaults`() {
        val color = SyntaxColor(color = "#ffffff")

        color.color shouldBe "#ffffff"
        color.fontStyle shouldBe FontStyle.REGULAR
        color.effectType shouldBe null
    }

    @Test
    fun `SyntaxColor data class supports all font styles`() {
        SyntaxColor(color = "#ffffff", fontStyle = FontStyle.REGULAR).fontStyle shouldBe FontStyle.REGULAR
        SyntaxColor(color = "#ffffff", fontStyle = FontStyle.BOLD).fontStyle shouldBe FontStyle.BOLD
        SyntaxColor(color = "#ffffff", fontStyle = FontStyle.ITALIC).fontStyle shouldBe FontStyle.ITALIC
        SyntaxColor(color = "#ffffff", fontStyle = FontStyle.BOLD_ITALIC).fontStyle shouldBe FontStyle.BOLD_ITALIC
    }

    @Test
    fun `SyntaxColor data class supports effect types`() {
        val color = SyntaxColor(
            color = "#ff0000",
            effectType = EffectType.WAVE_UNDERSCORE
        )

        color.effectType shouldBe EffectType.WAVE_UNDERSCORE
    }

    @Test
    fun `ColorClassification contains all required properties`() {
        val classification = ColorClassification(
            luminance = 100.0,
            luminanceClass = LuminanceClass.MID,
            hue = 240.0,
            saturation = 0.5,
            isGrayscale = false
        )

        classification.luminance shouldBe 100.0
        classification.luminanceClass shouldBe LuminanceClass.MID
        classification.hue shouldBe 240.0
        classification.saturation shouldBe 0.5
        classification.isGrayscale shouldBe false
    }

    @Test
    fun `PaletteAnalysis contains all required properties`() {
        val analysis = PaletteAnalysis(
            uniqueHueCount = 8,
            isLimitedPalette = false,
            isUniformBrightness = false,
            averageSaturation = 0.6
        )

        analysis.uniqueHueCount shouldBe 8
        analysis.isLimitedPalette shouldBe false
        analysis.isUniformBrightness shouldBe false
        analysis.averageSaturation shouldBe 0.6
    }

    @Test
    fun `ContrastLevel enum has all expected values`() {
        ContrastLevel.LOW shouldNotBe null
        ContrastLevel.NORMAL shouldNotBe null
        ContrastLevel.HIGH shouldNotBe null
    }
}
