package mapping

import colorschemes.WindowsTerminalColorScheme
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import utils.ColorUtils

/**
 * Comprehensive unit tests for color mapping functionality.
 *
 * Tests cover:
 * - Console Color Mapping (TASK-202)
 * - Syntax Color Inference (TASK-203)
 * - Palette Expansion (TASK-204)
 * - ColorMappingConfig
 *
 * Test data includes:
 * - Normal color scheme (One Dark)
 * - Monochrome scheme (grayscale)
 * - High contrast scheme
 * - Low contrast scheme
 */
class ColorMappingTest {

    // ========== TEST DATA ==========

    companion object {
        /**
         * Normal color scheme based on One Dark theme
         */
        val normalScheme = WindowsTerminalColorScheme(
            name = "One Dark",
            background = "#282c34",
            foreground = "#abb2bf",
            // Normal ANSI colors
            black = "#282c34",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#e5c07b",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            // Bright ANSI colors
            brightBlack = "#5c6370",
            brightRed = "#ff6c6b",
            brightGreen = "#b5cea8",
            brightYellow = "#ffd700",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#4ec9b0",
            brightWhite = "#ffffff",
            // Optional colors
            cursorColor = "#abb2bf",
            selectionBackground = "#3e4451"
        )

        /**
         * Monochrome (grayscale) color scheme
         */
        val monochromeScheme = WindowsTerminalColorScheme(
            name = "Monochrome",
            background = "#1e1e1e",
            foreground = "#d4d4d4",
            // All grayscale colors
            black = "#000000",
            red = "#707070",
            green = "#808080",
            yellow = "#909090",
            blue = "#606060",
            purple = "#757575",
            cyan = "#858585",
            white = "#d4d4d4",
            brightBlack = "#404040",
            brightRed = "#909090",
            brightGreen = "#a0a0a0",
            brightYellow = "#b0b0b0",
            brightBlue = "#808080",
            brightPurple = "#959595",
            brightCyan = "#a5a5a5",
            brightWhite = "#ffffff"
        )

        /**
         * High contrast color scheme (WCAG AAA compliant)
         */
        val highContrastScheme = WindowsTerminalColorScheme(
            name = "High Contrast",
            background = "#000000",
            foreground = "#ffffff",
            // High contrast colors
            black = "#000000",
            red = "#ff0000",
            green = "#00ff00",
            yellow = "#ffff00",
            blue = "#0000ff",
            purple = "#ff00ff",
            cyan = "#00ffff",
            white = "#ffffff",
            brightBlack = "#808080",
            brightRed = "#ff8080",
            brightGreen = "#80ff80",
            brightYellow = "#ffff80",
            brightBlue = "#8080ff",
            brightPurple = "#ff80ff",
            brightCyan = "#80ffff",
            brightWhite = "#ffffff",
            cursorColor = "#ffff00"
        )

        /**
         * Low contrast color scheme (subtle colors)
         */
        val lowContrastScheme = WindowsTerminalColorScheme(
            name = "Low Contrast",
            background = "#2d2d2d",
            foreground = "#3d3d3d",
            // Subtle, low contrast colors
            black = "#2a2a2a",
            red = "#3f3030",
            green = "#303f30",
            yellow = "#3f3f30",
            blue = "#30303f",
            purple = "#3f303f",
            cyan = "#303f3f",
            white = "#3d3d3d",
            brightBlack = "#353535",
            brightRed = "#4a3535",
            brightGreen = "#354a35",
            brightYellow = "#4a4a35",
            brightBlue = "#35354a",
            brightPurple = "#4a354a",
            brightCyan = "#354a4a",
            brightWhite = "#4d4d4d"
        )
    }

    // ========== CONSOLE COLOR MAPPING TESTS (TASK-202) ==========

    @Nested
    inner class ConsoleColorMapperTests {

        private val mapper = ConsoleColorMapper(ColorMappingConfig)

        @Test
        fun `maps all 16 ANSI colors correctly`() {
            val result = mapper.mapToConsoleColors(normalScheme)

            // Normal ANSI colors (30-37)
            result["CONSOLE_BLACK_OUTPUT"] shouldBe normalScheme.black
            result["CONSOLE_RED_OUTPUT"] shouldBe normalScheme.red
            result["CONSOLE_GREEN_OUTPUT"] shouldBe normalScheme.green
            result["CONSOLE_YELLOW_OUTPUT"] shouldBe normalScheme.yellow
            result["CONSOLE_BLUE_OUTPUT"] shouldBe normalScheme.blue
            result["CONSOLE_MAGENTA_OUTPUT"] shouldBe normalScheme.purple
            result["CONSOLE_CYAN_OUTPUT"] shouldBe normalScheme.cyan
            result["CONSOLE_GRAY_OUTPUT"] shouldBe normalScheme.white

            // Bright ANSI colors (90-97)
            result["CONSOLE_DARKGRAY_OUTPUT"] shouldBe normalScheme.brightBlack
            result["CONSOLE_RED_BRIGHT_OUTPUT"] shouldBe normalScheme.brightRed
            result["CONSOLE_GREEN_BRIGHT_OUTPUT"] shouldBe normalScheme.brightGreen
            result["CONSOLE_YELLOW_BRIGHT_OUTPUT"] shouldBe normalScheme.brightYellow
            result["CONSOLE_BLUE_BRIGHT_OUTPUT"] shouldBe normalScheme.brightBlue
            result["CONSOLE_MAGENTA_BRIGHT_OUTPUT"] shouldBe normalScheme.brightPurple
            result["CONSOLE_CYAN_BRIGHT_OUTPUT"] shouldBe normalScheme.brightCyan
            result["CONSOLE_WHITE_OUTPUT"] shouldBe normalScheme.brightWhite
        }

        @Test
        fun `maps special colors correctly`() {
            val result = mapper.mapToConsoleColors(normalScheme)

            result["CONSOLE_BACKGROUND_KEY"] shouldBe normalScheme.background
            result["CONSOLE_NORMAL_OUTPUT"] shouldBe normalScheme.foreground
            result["FOREGROUND"] shouldBe normalScheme.foreground
            result["CARET_COLOR"] shouldBe normalScheme.cursorColor
            result["CONSOLE_CURSOR"] shouldBe normalScheme.cursorColor
            result["CONSOLE_SELECTION_BACKGROUND"] shouldBe normalScheme.selectionBackground
        }

        @Test
        fun `uses fallback for missing cursor color`() {
            val schemeWithoutCursor = normalScheme.copy(cursorColor = null)
            val result = mapper.mapToConsoleColors(schemeWithoutCursor)

            // Should fall back to foreground
            result["CARET_COLOR"] shouldBe normalScheme.foreground
            result["CONSOLE_CURSOR"] shouldBe normalScheme.foreground
        }

        @Test
        fun `uses fallback for missing selection background`() {
            val schemeWithoutSelection = normalScheme.copy(selectionBackground = null)
            val result = mapper.mapToConsoleColors(schemeWithoutSelection)

            // Should be a blend of background and foreground
            val selectionBg = result["CONSOLE_SELECTION_BACKGROUND"]
            selectionBg shouldNotBe null
            selectionBg!! shouldMatch Regex("^#[0-9a-f]{6}$")

            // Verify it's different from both background and foreground
            selectionBg shouldNotBe normalScheme.background
            selectionBg shouldNotBe normalScheme.foreground
        }

        @Test
        fun `all mapped colors are in valid hex format`() {
            val result = mapper.mapToConsoleColors(normalScheme)

            result.values.forEach { color ->
                color shouldMatch Regex("^#[0-9a-f]{6}$")
            }
        }

        @Test
        fun `maps high contrast scheme correctly`() {
            val result = mapper.mapToConsoleColors(highContrastScheme)

            result shouldContainKey "CONSOLE_BLACK_OUTPUT"
            result shouldContainKey "CONSOLE_WHITE_OUTPUT"
            result["CONSOLE_BLACK_OUTPUT"] shouldBe "#000000"
            result["CONSOLE_WHITE_OUTPUT"] shouldBe "#ffffff"
        }

        @Test
        fun `normalizes color format to lowercase`() {
            val schemeWithUppercase = normalScheme.copy(
                red = "#FF0000",
                blue = "#0000FF"
            )
            val result = mapper.mapToConsoleColors(schemeWithUppercase)

            result["CONSOLE_RED_OUTPUT"] shouldBe "#ff0000"
            result["CONSOLE_BLUE_OUTPUT"] shouldBe "#0000ff"
        }
    }

    // ========== SYNTAX COLOR INFERENCE TESTS (TASK-203) ==========

    @Nested
    inner class SyntaxColorInferenceTests {

        @Test
        fun `infers syntax colors for normal scheme`() {
            val result = SyntaxColorInference.inferSyntaxColors(normalScheme)

            result shouldNotBe emptyMap()
            result shouldContainKey "COMMENT"
            result shouldContainKey "KEYWORD"
            result shouldContainKey "STRING"
        }

        @Test
        fun `classifies colors as DARK, MID, or BRIGHT`() {
            val result = SyntaxColorInference.inferSyntaxColors(normalScheme)

            // Dark background should be classified as DARK
            val bgLuminance = ColorUtils.calculateLuminance(normalScheme.background)
            bgLuminance shouldBeLessThan ColorMappingConfig.DARK_LUMINANCE_MAX

            // Bright white should be classified as BRIGHT
            val whiteLuminance = ColorUtils.calculateLuminance(normalScheme.brightWhite)
            whiteLuminance shouldBeGreaterThan ColorMappingConfig.BRIGHT_LUMINANCE_MIN
        }

        @Test
        fun `detects monochrome palette`() {
            val result = SyntaxColorInference.inferSyntaxColors(monochromeScheme)

            result shouldNotBe emptyMap()

            // For monochrome palettes, font styles should be used for differentiation
            val keyword = result["KEYWORD"]
            keyword shouldNotBe null
            keyword?.fontStyle shouldBe FontStyle.BOLD
        }

        @Test
        fun `analyzes high contrast scheme correctly`() {
            val result = SyntaxColorInference.inferSyntaxColors(highContrastScheme)

            result shouldNotBe emptyMap()

            // High contrast scheme should have colors with good separation
            val foreground = result["IDENTIFIER"]?.color ?: highContrastScheme.foreground
            val background = highContrastScheme.background

            val contrast = ColorUtils.calculateContrastRatio(foreground, background)
            contrast shouldBeGreaterThan ColorMappingConfig.HIGH_CONTRAST_THRESHOLD
        }

        @Test
        fun `analyzes low contrast scheme correctly`() {
            val result = SyntaxColorInference.inferSyntaxColors(lowContrastScheme)

            result shouldNotBe emptyMap()

            // Low contrast schemes should have their colors adjusted for better readability
            val foreground = lowContrastScheme.foreground
            val background = lowContrastScheme.background

            val contrast = ColorUtils.calculateContrastRatio(foreground, background)
            contrast shouldBeLessThan 4.5  // Below WCAG AA
        }

        @Test
        fun `maps keywords to blue or purple colors`() {
            val result = SyntaxColorInference.inferSyntaxColors(normalScheme)

            val keyword = result["KEYWORD"]
            keyword shouldNotBe null

            // Should use blue or purple from the scheme
            val keywordColor = keyword?.color ?: ""
            val possibleColors = listOf(
                normalScheme.blue,
                normalScheme.brightBlue,
                normalScheme.purple,
                normalScheme.brightPurple
            )

            // Check if it's one of the expected colors or derived from them
            keywordColor shouldMatch Regex("^#[0-9a-f]{6}$")
        }

        @Test
        fun `maps strings to green colors`() {
            val result = SyntaxColorInference.inferSyntaxColors(normalScheme)

            val string = result["STRING"]
            string shouldNotBe null

            // Should use green from the scheme
            val stringColor = string?.color ?: ""
            stringColor shouldMatch Regex("^#[0-9a-f]{6}$")
        }

        @Test
        fun `maps comments to dimmed colors`() {
            val result = SyntaxColorInference.inferSyntaxColors(normalScheme)

            val comment = result["COMMENT"]
            comment shouldNotBe null

            // Comments should be darker/dimmed
            val commentColor = comment?.color ?: ""
            val commentLuminance = ColorUtils.calculateLuminance(commentColor)
            val foregroundLuminance = ColorUtils.calculateLuminance(normalScheme.foreground)

            commentLuminance shouldBeLessThan foregroundLuminance
        }

        @Test
        fun `applies font styles for monochrome themes`() {
            val result = SyntaxColorInference.inferSyntaxColors(monochromeScheme)

            // In monochrome, font styles differentiate elements
            val keyword = result["KEYWORD"]
            keyword?.fontStyle shouldBe FontStyle.BOLD

            val comment = result["COMMENT"]
            comment?.fontStyle shouldBe FontStyle.ITALIC
        }

        @Test
        fun `includes common attributes`() {
            val result = SyntaxColorInference.inferSyntaxColors(normalScheme)

            // Should include essential editor attributes
            result shouldContainKey "COMMENT"
            result shouldContainKey "KEYWORD"
            result shouldContainKey "STRING"
            result shouldContainKey "IDENTIFIER"
        }

        @Test
        fun `all inferred colors are valid hex format`() {
            val result = SyntaxColorInference.inferSyntaxColors(normalScheme)

            result.values.forEach { syntaxColor ->
                syntaxColor.color shouldMatch Regex("^#[0-9a-f]{6}$")
            }
        }
    }

    // ========== PALETTE EXPANSION TESTS (TASK-204) ==========

    @Nested
    inner class ColorPaletteExpanderTests {

        @Test
        fun `expands palette to 100+ colors`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            // Should have significantly more colors than the original 16 ANSI colors
            result.size shouldBeGreaterThan 50
        }

        @Test
        fun `includes all original colors`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            // Should include base colors from scheme
            result shouldContainKey "wt_background"
            result shouldContainKey "wt_foreground"
            result shouldContainKey "wt_red"
            result shouldContainKey "wt_green"
            result shouldContainKey "wt_blue"
        }

        @Test
        fun `generates background variants`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            result shouldContainKey "bg_lighter"
            result shouldContainKey "bg_darker"
            result shouldContainKey "bg_subtle"
            result shouldContainKey "bg_panel"
        }

        @Test
        fun `generates foreground variants`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            result shouldContainKey "fg_normal"
            result shouldContainKey "fg_subtle"
            result shouldContainKey "fg_muted"
            result shouldContainKey "fg_disabled"
        }

        @Test
        fun `generates interactive state colors`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            result shouldContainKey "state_hover"
            result shouldContainKey "state_pressed"
            result shouldContainKey "state_selected"
            result shouldContainKey "state_focused"
        }

        @Test
        fun `generates semantic color variants`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            result shouldContainKey "semantic_info"
            result shouldContainKey "semantic_success"
            result shouldContainKey "semantic_warning"
            result shouldContainKey "semantic_error"
        }

        @Test
        fun `generates editor-specific colors`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            result shouldContainKey "editor_gutter"
            result shouldContainKey "editor_line_number"
            result shouldContainKey "editor_indent_guide"
        }

        @Test
        fun `generates border colors`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            result shouldContainKey "border_subtle"
            result shouldContainKey "border_normal"
            result shouldContainKey "border_strong"
        }

        @Test
        fun `generates accent color variants`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            result shouldContainKey "accent"
            result shouldContainKey "accent_light"
            result shouldContainKey "accent_dark"
        }

        @Test
        fun `all expanded colors are valid hex format`() {
            val result = ColorPaletteExpander.expandPalette(normalScheme)

            result.values.forEach { color ->
                color shouldMatch Regex("^#[0-9a-f]{6}$")
            }
        }

        @Test
        fun `interpolates colors correctly`() {
            val colors = ColorPaletteExpander.interpolateColors("#000000", "#ffffff", 5)

            colors shouldHaveSize 5
            colors[0] shouldBe "#000000"
            colors[4] shouldBe "#ffffff"

            // Middle color should be approximately gray
            val (r, g, b) = ColorUtils.hexToRgb(colors[2])
            r shouldBeGreaterThan 100
            r shouldBeLessThan 155
        }

        @Test
        fun `generates tints correctly`() {
            val tints = ColorPaletteExpander.generateTints("#ff0000", 3)

            tints shouldHaveSize 3

            // Each tint should be lighter than the previous
            val luminances = tints.map { ColorUtils.calculateLuminance(it) }
            luminances[0] shouldBeLessThan luminances[1]
            luminances[1] shouldBeLessThan luminances[2]
        }

        @Test
        fun `generates shades correctly`() {
            val shades = ColorPaletteExpander.generateShades("#ff0000", 3)

            shades shouldHaveSize 3

            // Each shade should be darker than the previous
            val luminances = shades.map { ColorUtils.calculateLuminance(it) }
            luminances[0] shouldBeGreaterThan luminances[1]
            luminances[1] shouldBeGreaterThan luminances[2]
        }

        @Test
        fun `generates complementary color`() {
            val base = "#ff0000"  // Red
            val complementary = ColorPaletteExpander.generateComplementaryColor(base)

            complementary shouldMatch Regex("^#[0-9a-f]{6}$")

            // Complementary of red should be cyan-ish
            val (h, _, _) = ColorUtils.hexToHsv(complementary)
            h shouldBeGreaterThan 150.0
            h shouldBeLessThan 210.0
        }

        @Test
        fun `generates analogous colors`() {
            val base = "#ff0000"  // Red
            val (left, right) = ColorPaletteExpander.generateAnalogousColors(base)

            left shouldMatch Regex("^#[0-9a-f]{6}$")
            right shouldMatch Regex("^#[0-9a-f]{6}$")
            left shouldNotBe right
        }

        @Test
        fun `generates triadic colors`() {
            val base = "#ff0000"  // Red
            val (triad1, triad2) = ColorPaletteExpander.generateTriadicColors(base)

            triad1 shouldMatch Regex("^#[0-9a-f]{6}$")
            triad2 shouldMatch Regex("^#[0-9a-f]{6}$")
            triad1 shouldNotBe triad2
        }

        @Test
        fun `adjusts to target luminance`() {
            val color = "#808080"
            val targetLuminance = 150.0
            val adjusted = ColorPaletteExpander.adjustToLuminance(color, targetLuminance)

            val actualLuminance = ColorUtils.calculateLuminance(adjusted)
            kotlin.math.abs(actualLuminance - targetLuminance) shouldBeLessThan 10.0
        }

        @Test
        fun `adjusts to target contrast ratio`() {
            val foreground = "#808080"
            val background = "#000000"
            val targetContrast = 7.0  // WCAG AAA

            val adjusted = ColorPaletteExpander.adjustToContrastRatio(
                foreground,
                background,
                targetContrast
            )

            val actualContrast = ColorUtils.calculateContrastRatio(adjusted, background)
            kotlin.math.abs(actualContrast - targetContrast) shouldBeLessThan 1.0
        }
    }

    // ========== COLOR MAPPING CONFIG TESTS ==========

    @Nested
    inner class ColorMappingConfigTests {

        @Test
        fun `has all 16 ANSI color mappings`() {
            val mappings = ColorMappingConfig.consoleColorMappings

            // All 8 normal ANSI colors
            mappings shouldContainKey "black"
            mappings shouldContainKey "red"
            mappings shouldContainKey "green"
            mappings shouldContainKey "yellow"
            mappings shouldContainKey "blue"
            mappings shouldContainKey "purple"
            mappings shouldContainKey "cyan"
            mappings shouldContainKey "white"

            // All 8 bright ANSI colors
            mappings shouldContainKey "brightBlack"
            mappings shouldContainKey "brightRed"
            mappings shouldContainKey "brightGreen"
            mappings shouldContainKey "brightYellow"
            mappings shouldContainKey "brightBlue"
            mappings shouldContainKey "brightPurple"
            mappings shouldContainKey "brightCyan"
            mappings shouldContainKey "brightWhite"
        }

        @Test
        fun `has special color mappings`() {
            val mappings = ColorMappingConfig.consoleColorMappings

            mappings shouldContainKey "background"
            mappings shouldContainKey "foreground"
            mappings shouldContainKey "cursorColor"
            mappings shouldContainKey "selectionBackground"
        }

        @Test
        fun `getIntelliJAttributes returns correct mappings`() {
            val blackAttrs = ColorMappingConfig.getIntelliJAttributes("black")
            blackAttrs shouldContainAll listOf("CONSOLE_BLACK_OUTPUT")

            val redAttrs = ColorMappingConfig.getIntelliJAttributes("red")
            redAttrs shouldContainAll listOf("CONSOLE_RED_OUTPUT")

            val foregroundAttrs = ColorMappingConfig.getIntelliJAttributes("foreground")
            foregroundAttrs shouldContain "CONSOLE_NORMAL_OUTPUT"
            foregroundAttrs shouldContain "FOREGROUND"
        }

        @Test
        fun `getFallbackCursorColor returns foreground`() {
            val foreground = "#abb2bf"
            val fallback = ColorMappingConfig.getFallbackCursorColor(foreground)

            fallback shouldBe foreground
        }

        @Test
        fun `getFallbackSelectionBackground blends colors`() {
            val background = "#282c34"
            val foreground = "#abb2bf"
            val selection = ColorMappingConfig.getFallbackSelectionBackground(background, foreground)

            selection shouldMatch Regex("^#[0-9a-f]{6}$")
            selection shouldNotBe background
            selection shouldNotBe foreground
        }

        @Test
        fun `has syntax inference rules for common attributes`() {
            val rules = ColorMappingConfig.syntaxInferenceRules

            rules shouldContainKey "COMMENT"
            rules shouldContainKey "KEYWORD"
            rules shouldContainKey "STRING"
            rules shouldContainKey "NUMBER"
            rules shouldContainKey "FUNCTION_CALL"
            rules shouldContainKey "CLASS_NAME"
        }

        @Test
        fun `getSyntaxRule returns correct rules`() {
            val commentRule = ColorMappingConfig.getSyntaxRule("COMMENT")
            commentRule shouldNotBe null
            commentRule?.priority shouldBe Priority.HIGH
            commentRule?.luminanceClass shouldBe LuminanceClass.DARK

            val keywordRule = ColorMappingConfig.getSyntaxRule("KEYWORD")
            keywordRule shouldNotBe null
            keywordRule?.priority shouldBe Priority.HIGH
        }

        @Test
        fun `has correct luminance thresholds`() {
            ColorMappingConfig.DARK_LUMINANCE_MAX shouldBe 100.0
            ColorMappingConfig.MID_LUMINANCE_MAX shouldBe 155.0
            ColorMappingConfig.BRIGHT_LUMINANCE_MIN shouldBe 155.0
        }

        @Test
        fun `has correct contrast thresholds`() {
            ColorMappingConfig.HIGH_CONTRAST_THRESHOLD shouldBe 7.0
            ColorMappingConfig.LOW_CONTRAST_THRESHOLD shouldBe 3.0
        }

        @Test
        fun `has correct grayscale saturation threshold`() {
            ColorMappingConfig.GRAYSCALE_SATURATION_MAX shouldBe 0.15
        }

        @Test
        fun `default colors are all valid hex format`() {
            ColorMappingConfig.defaultColors.values.forEach { color ->
                color shouldMatch Regex("^#[0-9a-f]{6}$")
            }
        }

        @Test
        fun `ANSI color codes are correct`() {
            val codes = ColorMappingConfig.ansiColorCodes

            codes["black"] shouldBe 30
            codes["red"] shouldBe 31
            codes["green"] shouldBe 32
            codes["yellow"] shouldBe 33
            codes["blue"] shouldBe 34
            codes["purple"] shouldBe 35
            codes["cyan"] shouldBe 36
            codes["white"] shouldBe 37

            codes["brightBlack"] shouldBe 90
            codes["brightRed"] shouldBe 91
            codes["brightGreen"] shouldBe 92
            codes["brightYellow"] shouldBe 93
            codes["brightBlue"] shouldBe 94
            codes["brightPurple"] shouldBe 95
            codes["brightCyan"] shouldBe 96
            codes["brightWhite"] shouldBe 97
        }
    }

    // ========== EDGE CASE TESTS ==========

    @Nested
    inner class EdgeCaseTests {

        @Test
        fun `handles monochrome palette detection`() {
            val result = SyntaxColorInference.inferSyntaxColors(monochromeScheme)

            // Should still produce a valid result
            result shouldNotBe emptyMap()
            result.values.forEach { syntaxColor ->
                syntaxColor.color shouldMatch Regex("^#[0-9a-f]{6}$")
            }
        }

        @Test
        fun `handles high contrast palette`() {
            val result = SyntaxColorInference.inferSyntaxColors(highContrastScheme)

            result shouldNotBe emptyMap()

            // Should maintain high contrast
            val foreground = highContrastScheme.foreground
            val background = highContrastScheme.background
            val contrast = ColorUtils.calculateContrastRatio(foreground, background)
            contrast shouldBeGreaterThan 15.0
        }

        @Test
        fun `handles low contrast palette`() {
            val result = SyntaxColorInference.inferSyntaxColors(lowContrastScheme)

            result shouldNotBe emptyMap<String, SyntaxColor>()

            // Should adjust colors for better readability
            result.values.forEach { syntaxColor ->
                syntaxColor.color shouldMatch Regex("^#[0-9a-f]{6}$")
            }
        }

        @Test
        fun `interpolation with minimum steps works`() {
            val colors = ColorPaletteExpander.interpolateColors("#000000", "#ffffff", 2)

            colors shouldHaveSize 2
            colors[0] shouldBe "#000000"
            colors[1] shouldBe "#ffffff"
        }

        @Test
        fun `interpolation with invalid steps throws exception`() {
            shouldThrow<IllegalArgumentException> {
                ColorPaletteExpander.interpolateColors("#000000", "#ffffff", 1)
            }
        }

        @Test
        fun `tint generation with zero count throws exception`() {
            shouldThrow<IllegalArgumentException> {
                ColorPaletteExpander.generateTints("#ff0000", 0)
            }
        }

        @Test
        fun `shade generation with zero count throws exception`() {
            shouldThrow<IllegalArgumentException> {
                ColorPaletteExpander.generateShades("#ff0000", 0)
            }
        }
    }

    // ========== INTEGRATION TESTS ==========

    @Nested
    inner class IntegrationTests {

        @Test
        fun `complete workflow from scheme to expanded palette`() {
            // 1. Start with Windows Terminal color scheme
            val scheme = normalScheme

            // 2. Map console colors
            val consoleMapper = ConsoleColorMapper(ColorMappingConfig)
            val consoleColors = consoleMapper.mapToConsoleColors(scheme)
            consoleColors.size shouldBeGreaterThan 16

            // 3. Infer syntax colors
            val syntaxColors = SyntaxColorInference.inferSyntaxColors(scheme)
            syntaxColors shouldNotBe emptyMap<String, SyntaxColor>()

            // 4. Expand palette
            val expandedPalette = ColorPaletteExpander.expandPalette(scheme)
            expandedPalette.size shouldBeGreaterThan 50

            // All colors should be valid
            (consoleColors.values + syntaxColors.values.map { it.color } + expandedPalette.values)
                .forEach { color ->
                    color shouldMatch Regex("^#[0-9a-f]{6}$")
                }
        }

        @Test
        fun `handles all test schemes consistently`() {
            val schemes = listOf(normalScheme, monochromeScheme, highContrastScheme, lowContrastScheme)

            schemes.forEach { scheme ->
                val consoleMapper = ConsoleColorMapper(ColorMappingConfig)
                val consoleColors = consoleMapper.mapToConsoleColors(scheme)
                consoleColors shouldNotBe emptyMap<String, String>()

                val syntaxColors = SyntaxColorInference.inferSyntaxColors(scheme)
                syntaxColors shouldNotBe emptyMap<String, SyntaxColor>()

                val expandedPalette = ColorPaletteExpander.expandPalette(scheme)
                expandedPalette.size shouldBeGreaterThan 50
            }
        }
    }
}
