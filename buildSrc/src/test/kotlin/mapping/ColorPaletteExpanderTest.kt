package mapping

import colorschemes.WindowsTerminalColorScheme
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import org.junit.jupiter.api.Test
import utils.ColorUtils
import kotlin.math.abs

class ColorPaletteExpanderTest {

    // Test color scheme (One Dark inspired)
    private val testScheme = WindowsTerminalColorScheme(
        name = "Test Scheme",
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

    @Test
    fun `expandPalette returns expanded palette with all base colors`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        // Should contain all base Windows Terminal colors
        expanded shouldContainKey "wt_background"
        expanded shouldContainKey "wt_foreground"
        expanded shouldContainKey "wt_red"
        expanded shouldContainKey "wt_green"
        expanded shouldContainKey "wt_blue"
    }

    @Test
    fun `expandPalette generates background variants`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        expanded shouldContainKey "bg_lighter"
        expanded shouldContainKey "bg_darker"
        expanded shouldContainKey "bg_subtle"
        expanded shouldContainKey "bg_panel"
        expanded shouldContainKey "bg_sidebar"
    }

    @Test
    fun `expandPalette generates foreground variants`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        expanded shouldContainKey "fg_normal"
        expanded shouldContainKey "fg_subtle"
        expanded shouldContainKey "fg_muted"
        expanded shouldContainKey "fg_disabled"
        expanded shouldContainKey "fg_bright"
    }

    @Test
    fun `expandPalette generates interactive state colors`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        expanded shouldContainKey "state_hover"
        expanded shouldContainKey "state_pressed"
        expanded shouldContainKey "state_selected"
        expanded shouldContainKey "state_focused"
    }

    @Test
    fun `expandPalette generates semantic color variants`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        expanded shouldContainKey "semantic_info"
        expanded shouldContainKey "semantic_success"
        expanded shouldContainKey "semantic_warning"
        expanded shouldContainKey "semantic_error"
        expanded shouldContainKey "semantic_info_bg"
        expanded shouldContainKey "semantic_success_border"
    }

    @Test
    fun `expandPalette generates editor colors`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        expanded shouldContainKey "editor_gutter"
        expanded shouldContainKey "editor_line_number"
        expanded shouldContainKey "editor_indent_guide"
        expanded shouldContainKey "editor_current_line"
    }

    @Test
    fun `expandPalette generates border colors`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        expanded shouldContainKey "border_subtle"
        expanded shouldContainKey "border_normal"
        expanded shouldContainKey "border_strong"
        expanded shouldContainKey "border_focus"
    }

    @Test
    fun `expandPalette generates accent variants`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        expanded shouldContainKey "accent"
        expanded shouldContainKey "accent_light"
        expanded shouldContainKey "accent_dark"
        expanded shouldContainKey "accent_muted"
    }

    @Test
    fun `expandPalette generates gradient steps`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        expanded shouldContainKey "gradient_bg_fg_0"
        expanded shouldContainKey "gradient_bg_fg_1"
        expanded shouldContainKey "gradient_bg_fg_2"
    }

    @Test
    fun `expandPalette returns significant number of colors`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        // Should have at least 50 colors (base + generated)
        expanded.size shouldBeGreaterThan 50
    }

    @Test
    fun `all expanded colors are valid hex colors`() {
        val expanded = ColorPaletteExpander.expandPalette(testScheme)

        expanded.values.forEach { color ->
            color shouldMatch Regex("^#[0-9a-f]{6}$")
        }
    }

    // ========== interpolateColors tests ==========

    @Test
    fun `interpolateColors creates smooth gradient`() {
        val start = "#000000"
        val end = "#ffffff"
        val gradient = ColorPaletteExpander.interpolateColors(start, end, 5)

        gradient.size shouldBe 5
        gradient.first() shouldBe start
        gradient.last() shouldBe end

        // Middle color should be gray
        val middle = gradient[2]
        val (r, g, b) = ColorUtils.hexToRgb(middle)
        r shouldBe g
        g shouldBe b
    }

    @Test
    fun `interpolateColors with 2 steps returns endpoints`() {
        val color1 = "#ff0000"
        val color2 = "#00ff00"
        val gradient = ColorPaletteExpander.interpolateColors(color1, color2, 2)

        gradient.size shouldBe 2
        gradient[0] shouldBe color1
        gradient[1] shouldBe color2
    }

    @Test
    fun `interpolateColors creates increasing brightness`() {
        val dark = "#333333"
        val bright = "#cccccc"
        val gradient = ColorPaletteExpander.interpolateColors(dark, bright, 5)

        val luminances = gradient.map { ColorUtils.calculateLuminance(it) }

        // Each step should be brighter than the previous
        for (i in 0 until luminances.size - 1) {
            luminances[i + 1] shouldBeGreaterThan luminances[i]
        }
    }

    @Test
    fun `interpolateColors throws exception for steps less than 2`() {
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.interpolateColors("#000000", "#ffffff", 1)
        }
    }

    // ========== generateTints tests ==========

    @Test
    fun `generateTints creates lighter variants`() {
        val base = "#61afef"
        val tints = ColorPaletteExpander.generateTints(base, 3)

        tints.size shouldBe 3

        val baseLuminance = ColorUtils.calculateLuminance(base)
        tints.forEach { tint ->
            val tintLuminance = ColorUtils.calculateLuminance(tint)
            tintLuminance shouldBeGreaterThan baseLuminance
        }
    }

    @Test
    fun `generateTints creates progressively lighter colors`() {
        val base = "#61afef"
        val tints = ColorPaletteExpander.generateTints(base, 5)

        val luminances = tints.map { ColorUtils.calculateLuminance(it) }

        // Each tint should be lighter than the previous
        for (i in 0 until luminances.size - 1) {
            luminances[i + 1] shouldBeGreaterThan luminances[i]
        }
    }

    @Test
    fun `generateTints preserves hue`() {
        val base = "#61afef"
        val tints = ColorPaletteExpander.generateTints(base, 3)

        val baseHue = ColorUtils.extractHue(base)
        tints.forEach { tint ->
            val tintHue = ColorUtils.extractHue(tint)
            // Hue should be approximately the same (allowing small variation due to RGB conversion)
            abs(tintHue - baseHue) shouldBeLessThan 5.0
        }
    }

    @Test
    fun `generateTints throws exception for count less than 1`() {
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.generateTints("#61afef", 0)
        }
    }

    // ========== generateShades tests ==========

    @Test
    fun `generateShades creates darker variants`() {
        val base = "#61afef"
        val shades = ColorPaletteExpander.generateShades(base, 3)

        shades.size shouldBe 3

        val baseLuminance = ColorUtils.calculateLuminance(base)
        shades.forEach { shade ->
            val shadeLuminance = ColorUtils.calculateLuminance(shade)
            shadeLuminance shouldBeLessThan baseLuminance
        }
    }

    @Test
    fun `generateShades creates progressively darker colors`() {
        val base = "#61afef"
        val shades = ColorPaletteExpander.generateShades(base, 5)

        val luminances = shades.map { ColorUtils.calculateLuminance(it) }

        // Each shade should be darker than the previous
        for (i in 0 until luminances.size - 1) {
            luminances[i + 1] shouldBeLessThan luminances[i]
        }
    }

    @Test
    fun `generateShades preserves hue`() {
        val base = "#61afef"
        val shades = ColorPaletteExpander.generateShades(base, 3)

        val baseHue = ColorUtils.extractHue(base)
        shades.forEach { shade ->
            val shadeHue = ColorUtils.extractHue(shade)
            // Hue should be approximately the same (allowing small variation)
            abs(shadeHue - baseHue) shouldBeLessThan 5.0
        }
    }

    @Test
    fun `generateShades throws exception for count less than 1`() {
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.generateShades("#61afef", 0)
        }
    }

    // ========== generateSaturationVariants tests ==========

    @Test
    fun `generateSaturationVariants creates both saturated and desaturated colors`() {
        val base = "#61afef"
        val variants = ColorPaletteExpander.generateSaturationVariants(base, 2)

        variants.size shouldBe 4  // 2 saturated + 2 desaturated
        variants shouldContainKey "saturated_1"
        variants shouldContainKey "saturated_2"
        variants shouldContainKey "desaturated_1"
        variants shouldContainKey "desaturated_2"
    }

    @Test
    fun `generateSaturationVariants increases saturation correctly`() {
        val base = "#61afef"
        val variants = ColorPaletteExpander.generateSaturationVariants(base, 3)

        val baseSaturation = ColorUtils.extractSaturation(base)
        (1..3).forEach { i ->
            val saturated = variants["saturated_$i"]!!
            val saturation = ColorUtils.extractSaturation(saturated)
            saturation shouldBeGreaterThan baseSaturation
        }
    }

    @Test
    fun `generateSaturationVariants decreases saturation correctly`() {
        val base = "#61afef"
        val variants = ColorPaletteExpander.generateSaturationVariants(base, 3)

        val baseSaturation = ColorUtils.extractSaturation(base)
        (1..3).forEach { i ->
            val desaturated = variants["desaturated_$i"]!!
            val saturation = ColorUtils.extractSaturation(desaturated)
            saturation shouldBeLessThan baseSaturation
        }
    }

    @Test
    fun `generateSaturationVariants throws exception for count less than 1`() {
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.generateSaturationVariants("#61afef", 0)
        }
    }

    // ========== generateComplementaryColor tests ==========

    @Test
    fun `generateComplementaryColor creates opposite hue`() {
        val base = "#ff0000"  // Red (hue ~0)
        val complement = ColorPaletteExpander.generateComplementaryColor(base)

        val baseHue = ColorUtils.extractHue(base)
        val complementHue = ColorUtils.extractHue(complement)

        // Complementary hue should be ~180 degrees away
        val hueDiff = abs(complementHue - baseHue)
        hueDiff.shouldBeBetween(170.0, 190.0, 0.01)
    }

    @Test
    fun `generateComplementaryColor preserves saturation and value`() {
        val base = "#61afef"
        val complement = ColorPaletteExpander.generateComplementaryColor(base)

        val (_, baseSat, baseVal) = ColorUtils.hexToHsv(base)
        val (_, compSat, compVal) = ColorUtils.hexToHsv(complement)

        baseSat shouldBe compSat
        baseVal shouldBe compVal
    }

    @Test
    fun `generateComplementaryColor is reversible`() {
        val base = "#61afef"
        val complement = ColorPaletteExpander.generateComplementaryColor(base)
        val complementOfComplement = ColorPaletteExpander.generateComplementaryColor(complement)

        val baseHue = ColorUtils.extractHue(base)
        val finalHue = ColorUtils.extractHue(complementOfComplement)

        // Should return to approximately the same hue
        abs(finalHue - baseHue) shouldBeLessThan 1.0
    }

    // ========== generateAnalogousColors tests ==========

    @Test
    fun `generateAnalogousColors creates colors with shifted hues`() {
        val base = "#61afef"
        val (left, right) = ColorPaletteExpander.generateAnalogousColors(base, 30.0)

        left shouldNotBe base
        right shouldNotBe base
        left shouldNotBe right
    }

    @Test
    fun `generateAnalogousColors shifts hue by specified degrees`() {
        val base = "#ff0000"  // Red
        val (left, right) = ColorPaletteExpander.generateAnalogousColors(base, 30.0)

        val baseHue = ColorUtils.extractHue(base)
        val leftHue = ColorUtils.extractHue(left)
        val rightHue = ColorUtils.extractHue(right)

        // Left should be ~30 degrees counterclockwise
        val leftDiff = (baseHue - leftHue + 360) % 360
        leftDiff.shouldBeBetween(25.0, 35.0, 0.01)

        // Right should be ~30 degrees clockwise
        val rightDiff = (rightHue - baseHue + 360) % 360
        rightDiff.shouldBeBetween(25.0, 35.0, 0.01)
    }

    @Test
    fun `generateAnalogousColors preserves saturation and value`() {
        val base = "#61afef"
        val (left, right) = ColorPaletteExpander.generateAnalogousColors(base)

        val (_, baseSat, baseVal) = ColorUtils.hexToHsv(base)
        val (_, leftSat, leftVal) = ColorUtils.hexToHsv(left)
        val (_, rightSat, rightVal) = ColorUtils.hexToHsv(right)

        baseSat shouldBe leftSat
        baseSat shouldBe rightSat
        baseVal shouldBe leftVal
        baseVal shouldBe rightVal
    }

    @Test
    fun `generateAnalogousColors throws exception for invalid degrees`() {
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.generateAnalogousColors("#61afef", -10.0)
        }
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.generateAnalogousColors("#61afef", 200.0)
        }
    }

    // ========== generateTriadicColors tests ==========

    @Test
    fun `generateTriadicColors creates three colors 120 degrees apart`() {
        val base = "#ff0000"  // Red
        val (triad1, triad2) = ColorPaletteExpander.generateTriadicColors(base)

        val baseHue = ColorUtils.extractHue(base)
        val triad1Hue = ColorUtils.extractHue(triad1)
        val triad2Hue = ColorUtils.extractHue(triad2)

        // First triad should be ~120 degrees away
        val diff1 = (triad1Hue - baseHue + 360) % 360
        diff1.shouldBeBetween(115.0, 125.0, 0.01)

        // Second triad should be ~240 degrees away
        val diff2 = (triad2Hue - baseHue + 360) % 360
        diff2.shouldBeBetween(235.0, 245.0, 0.01)
    }

    @Test
    fun `generateTriadicColors preserves saturation and value`() {
        val base = "#61afef"
        val (triad1, triad2) = ColorPaletteExpander.generateTriadicColors(base)

        val (_, baseSat, baseVal) = ColorUtils.hexToHsv(base)
        val (_, sat1, val1) = ColorUtils.hexToHsv(triad1)
        val (_, sat2, val2) = ColorUtils.hexToHsv(triad2)

        baseSat shouldBe sat1
        baseSat shouldBe sat2
        baseVal shouldBe val1
        baseVal shouldBe val2
    }

    // ========== generateSplitComplementaryColors tests ==========

    @Test
    fun `generateSplitComplementaryColors creates colors around complement`() {
        val base = "#ff0000"  // Red (hue ~0)
        val (split1, split2) = ColorPaletteExpander.generateSplitComplementaryColors(base, 30.0)

        val baseHue = ColorUtils.extractHue(base)
        val split1Hue = ColorUtils.extractHue(split1)
        val split2Hue = ColorUtils.extractHue(split2)

        // Both should be around 180 degrees (complementary) +/- 30
        val complementHue = (baseHue + 180) % 360

        val diff1 = abs(split1Hue - complementHue)
        val diff2 = abs(split2Hue - complementHue)

        // One should be ~30 degrees before complement, one ~30 degrees after
        diff1.shouldBeBetween(25.0, 35.0, 0.01)
        diff2.shouldBeBetween(25.0, 35.0, 0.01)
    }

    @Test
    fun `generateSplitComplementaryColors throws exception for invalid degrees`() {
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.generateSplitComplementaryColors("#61afef", -10.0)
        }
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.generateSplitComplementaryColors("#61afef", 100.0)
        }
    }

    // ========== generateMonochromaticPalette tests ==========

    @Test
    fun `generateMonochromaticPalette creates colors with same hue`() {
        val base = "#61afef"
        val palette = ColorPaletteExpander.generateMonochromaticPalette(base, 5)

        palette.size shouldBe 5

        val baseHue = ColorUtils.extractHue(base)
        palette.forEach { color ->
            val hue = ColorUtils.extractHue(color)
            abs(hue - baseHue) shouldBeLessThan 1.0
        }
    }

    @Test
    fun `generateMonochromaticPalette creates varying brightness`() {
        val base = "#61afef"
        val palette = ColorPaletteExpander.generateMonochromaticPalette(base, 5)

        val luminances = palette.map { ColorUtils.calculateLuminance(it) }

        // Should have varying brightness
        val uniqueLuminances = luminances.distinct()
        uniqueLuminances.size shouldBeGreaterThan 3
    }

    @Test
    fun `generateMonochromaticPalette throws exception for count less than 1`() {
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.generateMonochromaticPalette("#61afef", 0)
        }
    }

    // ========== adjustToLuminance tests ==========

    @Test
    fun `adjustToLuminance adjusts color to target luminance`() {
        val color = "#61afef"
        val targetLuminance = 150.0
        val adjusted = ColorPaletteExpander.adjustToLuminance(color, targetLuminance)

        val resultLuminance = ColorUtils.calculateLuminance(adjusted)
        abs(resultLuminance - targetLuminance) shouldBeLessThan 10.0
    }

    @Test
    fun `adjustToLuminance can make colors darker`() {
        val bright = "#ffffff"
        val targetLuminance = 100.0
        val adjusted = ColorPaletteExpander.adjustToLuminance(bright, targetLuminance)

        val resultLuminance = ColorUtils.calculateLuminance(adjusted)
        resultLuminance shouldBeLessThan 150.0
    }

    @Test
    fun `adjustToLuminance can make colors lighter`() {
        val dark = "#000000"
        val targetLuminance = 150.0
        val adjusted = ColorPaletteExpander.adjustToLuminance(dark, targetLuminance)

        val resultLuminance = ColorUtils.calculateLuminance(adjusted)
        resultLuminance shouldBeGreaterThan 100.0
    }

    @Test
    fun `adjustToLuminance throws exception for invalid target`() {
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.adjustToLuminance("#61afef", -10.0)
        }
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.adjustToLuminance("#61afef", 300.0)
        }
    }

    // ========== adjustToContrastRatio tests ==========

    @Test
    fun `adjustToContrastRatio increases contrast when needed`() {
        val foreground = "#808080"
        val background = "#7a7a7a"
        val targetContrast = 4.5
        val adjusted = ColorPaletteExpander.adjustToContrastRatio(foreground, background, targetContrast)

        val resultContrast = ColorUtils.calculateContrastRatio(adjusted, background)
        resultContrast shouldBeGreaterThan 4.0
    }

    @Test
    fun `adjustToContrastRatio works with dark backgrounds`() {
        val foreground = "#333333"
        val background = "#000000"
        val targetContrast = 7.0
        val adjusted = ColorPaletteExpander.adjustToContrastRatio(foreground, background, targetContrast)

        val resultContrast = ColorUtils.calculateContrastRatio(adjusted, background)
        resultContrast shouldBeGreaterThan 5.0
    }

    @Test
    fun `adjustToContrastRatio works with light backgrounds`() {
        val foreground = "#cccccc"
        val background = "#ffffff"
        val targetContrast = 7.0
        val adjusted = ColorPaletteExpander.adjustToContrastRatio(foreground, background, targetContrast)

        val resultContrast = ColorUtils.calculateContrastRatio(adjusted, background)
        resultContrast shouldBeGreaterThan 5.0
    }

    @Test
    fun `adjustToContrastRatio throws exception for invalid contrast ratio`() {
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.adjustToContrastRatio("#61afef", "#282c34", 0.5)
        }
        shouldThrow<IllegalArgumentException> {
            ColorPaletteExpander.adjustToContrastRatio("#61afef", "#282c34", 25.0)
        }
    }
}
