package utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeLessThan as intShouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.math.abs

class ColorUtilsTest {

    @Test
    fun `hexToRgb converts valid hex colors correctly`() {
        ColorUtils.hexToRgb("#000000") shouldBe Triple(0, 0, 0)
        ColorUtils.hexToRgb("#ffffff") shouldBe Triple(255, 255, 255)
        ColorUtils.hexToRgb("#ff0000") shouldBe Triple(255, 0, 0)
        ColorUtils.hexToRgb("#00ff00") shouldBe Triple(0, 255, 0)
        ColorUtils.hexToRgb("#0000ff") shouldBe Triple(0, 0, 255)
        ColorUtils.hexToRgb("#98c379") shouldBe Triple(152, 195, 121)
    }

    @Test
    fun `hexToRgb handles uppercase hex colors`() {
        ColorUtils.hexToRgb("#FFFFFF") shouldBe Triple(255, 255, 255)
        ColorUtils.hexToRgb("#FF00FF") shouldBe Triple(255, 0, 255)
    }

    @Test
    fun `hexToRgb throws exception for invalid format`() {
        shouldThrow<IllegalArgumentException> { ColorUtils.hexToRgb("#fff") }
        shouldThrow<IllegalArgumentException> { ColorUtils.hexToRgb("#fffffff") }
        shouldThrow<IllegalArgumentException> { ColorUtils.hexToRgb("ffffff") }
        shouldThrow<IllegalArgumentException> { ColorUtils.hexToRgb("#gggggg") }
    }

    @Test
    fun `rgbToHex converts RGB values correctly`() {
        ColorUtils.rgbToHex(0, 0, 0) shouldBe "#000000"
        ColorUtils.rgbToHex(255, 255, 255) shouldBe "#ffffff"
        ColorUtils.rgbToHex(255, 0, 0) shouldBe "#ff0000"
        ColorUtils.rgbToHex(0, 255, 0) shouldBe "#00ff00"
        ColorUtils.rgbToHex(0, 0, 255) shouldBe "#0000ff"
        ColorUtils.rgbToHex(152, 195, 121) shouldBe "#98c379"
    }

    @Test
    fun `rgbToHex throws exception for out of range values`() {
        shouldThrow<IllegalArgumentException> { ColorUtils.rgbToHex(-1, 0, 0) }
        shouldThrow<IllegalArgumentException> { ColorUtils.rgbToHex(256, 0, 0) }
        shouldThrow<IllegalArgumentException> { ColorUtils.rgbToHex(0, -1, 0) }
        shouldThrow<IllegalArgumentException> { ColorUtils.rgbToHex(0, 256, 0) }
        shouldThrow<IllegalArgumentException> { ColorUtils.rgbToHex(0, 0, -1) }
        shouldThrow<IllegalArgumentException> { ColorUtils.rgbToHex(0, 0, 256) }
    }

    @Test
    fun `hexToRgb and rgbToHex are inverse operations`() {
        val testColors = listOf(
            "#000000", "#ffffff", "#ff0000", "#00ff00", "#0000ff",
            "#98c379", "#e06c75", "#61afef", "#c678dd", "#e5c07b"
        )

        testColors.forEach { hex ->
            val (r, g, b) = ColorUtils.hexToRgb(hex)
            val roundTrip = ColorUtils.rgbToHex(r, g, b)
            roundTrip shouldBe hex.lowercase()
        }
    }

    @Test
    fun `lighten makes colors brighter`() {
        val dark = "#333333"
        val lightened = ColorUtils.lighten(dark, 0.5)

        val (r1, g1, b1) = ColorUtils.hexToRgb(dark)
        val (r2, g2, b2) = ColorUtils.hexToRgb(lightened)

        (r2 > r1) shouldBe true
        (g2 > g1) shouldBe true
        (b2 > b1) shouldBe true
    }

    @Test
    fun `lighten with 0 percent returns same color`() {
        val color = "#98c379"
        ColorUtils.lighten(color, 0.0) shouldBe color
    }

    @Test
    fun `lighten with 100 percent returns white`() {
        val color = "#98c379"
        ColorUtils.lighten(color, 1.0) shouldBe "#ffffff"
    }

    @Test
    fun `darken makes colors darker`() {
        val bright = "#cccccc"
        val darkened = ColorUtils.darken(bright, 0.5)

        val (r1, g1, b1) = ColorUtils.hexToRgb(bright)
        val (r2, g2, b2) = ColorUtils.hexToRgb(darkened)

        (r2 < r1) shouldBe true
        (g2 < g1) shouldBe true
        (b2 < b1) shouldBe true
    }

    @Test
    fun `darken with 0 percent returns same color`() {
        val color = "#98c379"
        ColorUtils.darken(color, 0.0) shouldBe color
    }

    @Test
    fun `darken with 100 percent returns black`() {
        val color = "#98c379"
        ColorUtils.darken(color, 1.0) shouldBe "#000000"
    }

    @Test
    fun `calculateContrastRatio returns correct values for known pairs`() {
        // Black and white should have maximum contrast (21:1)
        val blackWhiteRatio = ColorUtils.calculateContrastRatio("#000000", "#ffffff")
        abs(blackWhiteRatio - 21.0) shouldBeLessThan 0.1

        // Same color should have minimum contrast (1:1)
        val sameColorRatio = ColorUtils.calculateContrastRatio("#ff0000", "#ff0000")
        abs(sameColorRatio - 1.0) shouldBeLessThan 0.01

        // Contrast ratio should be symmetric
        val ratio1 = ColorUtils.calculateContrastRatio("#000000", "#ffffff")
        val ratio2 = ColorUtils.calculateContrastRatio("#ffffff", "#000000")
        ratio1 shouldBe ratio2
    }

    @Test
    fun `calculateContrastRatio meets WCAG AA for typical theme colors`() {
        // Typical dark theme: light text on dark background
        val foreground = "#abb2bf"
        val background = "#282c34"

        val ratio = ColorUtils.calculateContrastRatio(foreground, background)
        ratio shouldBeGreaterThan 4.5  // WCAG AA minimum for normal text
    }

    @Test
    fun `blend with ratio 0 returns first color`() {
        ColorUtils.blend("#ff0000", "#0000ff", 0.0) shouldBe "#ff0000"
    }

    @Test
    fun `blend with ratio 1 returns second color`() {
        ColorUtils.blend("#ff0000", "#0000ff", 1.0) shouldBe "#0000ff"
    }

    @Test
    fun `blend with ratio 0_5 returns midpoint`() {
        val result = ColorUtils.blend("#000000", "#ffffff", 0.5)
        val (r, g, b) = ColorUtils.hexToRgb(result)

        // Should be approximately gray (127, 127, 127)
        abs(r - 127) intShouldBeLessThan 2
        abs(g - 127) intShouldBeLessThan 2
        abs(b - 127) intShouldBeLessThan 2
    }

    @Test
    fun `calculateLuminance returns expected range`() {
        val blackLuminance = ColorUtils.calculateLuminance("#000000")
        val whiteLuminance = ColorUtils.calculateLuminance("#ffffff")

        blackLuminance shouldBe 0.0
        whiteLuminance shouldBe 255.0
    }

    @Test
    fun `hexToHsv converts colors correctly`() {
        // Pure red
        val (h1, s1, v1) = ColorUtils.hexToHsv("#ff0000")
        abs(h1 - 0.0) shouldBeLessThan 1.0
        abs(s1 - 1.0) shouldBeLessThan 0.01
        abs(v1 - 1.0) shouldBeLessThan 0.01

        // Pure green
        val (h2, s2, v2) = ColorUtils.hexToHsv("#00ff00")
        abs(h2 - 120.0) shouldBeLessThan 1.0
        abs(s2 - 1.0) shouldBeLessThan 0.01
        abs(v2 - 1.0) shouldBeLessThan 0.01

        // Pure blue
        val (h3, s3, v3) = ColorUtils.hexToHsv("#0000ff")
        abs(h3 - 240.0) shouldBeLessThan 1.0
        abs(s3 - 1.0) shouldBeLessThan 0.01
        abs(v3 - 1.0) shouldBeLessThan 0.01

        // Grayscale (no saturation)
        val (_, s4, _) = ColorUtils.hexToHsv("#808080")
        abs(s4) shouldBeLessThan 0.01
    }

    @Test
    fun `hsvToHex converts HSV correctly`() {
        // Pure red
        ColorUtils.hsvToHex(0.0, 1.0, 1.0) shouldBe "#ff0000"

        // Pure green
        ColorUtils.hsvToHex(120.0, 1.0, 1.0) shouldBe "#00ff00"

        // Pure blue
        ColorUtils.hsvToHex(240.0, 1.0, 1.0) shouldBe "#0000ff"

        // White (no saturation, max value)
        ColorUtils.hsvToHex(0.0, 0.0, 1.0) shouldBe "#ffffff"

        // Black (no value)
        ColorUtils.hsvToHex(0.0, 0.0, 0.0) shouldBe "#000000"
    }

    @Test
    fun `hexToHsv and hsvToHex are inverse operations`() {
        val testColors = listOf(
            "#ff0000", "#00ff00", "#0000ff", "#ffff00",
            "#ff00ff", "#00ffff", "#ffffff", "#000000"
        )

        testColors.forEach { hex ->
            val (h, s, v) = ColorUtils.hexToHsv(hex)
            val roundTrip = ColorUtils.hsvToHex(h, s, v)
            roundTrip shouldBe hex.lowercase()
        }
    }

    @Test
    fun `extractHue returns correct hue values`() {
        val redHue = ColorUtils.extractHue("#ff0000")
        abs(redHue - 0.0) shouldBeLessThan 1.0

        val greenHue = ColorUtils.extractHue("#00ff00")
        abs(greenHue - 120.0) shouldBeLessThan 1.0

        val blueHue = ColorUtils.extractHue("#0000ff")
        abs(blueHue - 240.0) shouldBeLessThan 1.0
    }

    @Test
    fun `extractSaturation returns correct saturation values`() {
        // Fully saturated colors
        ColorUtils.extractSaturation("#ff0000") shouldBeGreaterThan 0.99

        // Grayscale (no saturation)
        ColorUtils.extractSaturation("#808080") shouldBeLessThan 0.01
    }

    @Test
    fun `isGrayscale detects grayscale colors`() {
        ColorUtils.isGrayscale("#000000") shouldBe true
        ColorUtils.isGrayscale("#808080") shouldBe true
        ColorUtils.isGrayscale("#ffffff") shouldBe true

        ColorUtils.isGrayscale("#ff0000") shouldBe false
        ColorUtils.isGrayscale("#00ff00") shouldBe false
        ColorUtils.isGrayscale("#0000ff") shouldBe false
    }

    @Test
    fun `isDark detects dark colors`() {
        ColorUtils.isDark("#000000") shouldBe true
        ColorUtils.isDark("#333333") shouldBe true

        ColorUtils.isDark("#ffffff") shouldBe false
        ColorUtils.isDark("#cccccc") shouldBe false
    }

    @Test
    fun `isBright detects bright colors`() {
        ColorUtils.isBright("#ffffff") shouldBe true
        ColorUtils.isBright("#cccccc") shouldBe true

        ColorUtils.isBright("#000000") shouldBe false
        ColorUtils.isBright("#333333") shouldBe false
    }

    @Test
    fun `saturate increases saturation`() {
        val grayish = "#808080"
        val saturated = ColorUtils.saturate(grayish, 0.5)

        val originalSat = ColorUtils.extractSaturation(grayish)
        val newSat = ColorUtils.extractSaturation(saturated)

        newSat shouldBeGreaterThan originalSat
    }

    @Test
    fun `desaturate decreases saturation`() {
        val vibrant = "#ff0000"
        val desaturated = ColorUtils.desaturate(vibrant, 0.5)

        val originalSat = ColorUtils.extractSaturation(vibrant)
        val newSat = ColorUtils.extractSaturation(desaturated)

        newSat shouldBeLessThan originalSat
    }

    @Test
    fun `interpolate with empty list throws exception`() {
        shouldThrow<IllegalArgumentException> {
            ColorUtils.interpolate(emptyList(), 0.5)
        }
    }

    @Test
    fun `interpolate with single color returns that color`() {
        ColorUtils.interpolate(listOf("#ff0000"), 0.0) shouldBe "#ff0000"
        ColorUtils.interpolate(listOf("#ff0000"), 0.5) shouldBe "#ff0000"
        ColorUtils.interpolate(listOf("#ff0000"), 1.0) shouldBe "#ff0000"
    }

    @Test
    fun `interpolate between two colors works correctly`() {
        val colors = listOf("#000000", "#ffffff")

        ColorUtils.interpolate(colors, 0.0) shouldBe "#000000"
        ColorUtils.interpolate(colors, 1.0) shouldBe "#ffffff"

        val mid = ColorUtils.interpolate(colors, 0.5)
        val (r, g, b) = ColorUtils.hexToRgb(mid)
        abs(r - 127) intShouldBeLessThan 2
        abs(g - 127) intShouldBeLessThan 2
        abs(b - 127) intShouldBeLessThan 2
    }

    @Test
    fun `interpolate between multiple colors works correctly`() {
        val colors = listOf("#ff0000", "#00ff00", "#0000ff")

        // At 0.0, should be red
        ColorUtils.interpolate(colors, 0.0) shouldBe "#ff0000"

        // At 1.0, should be blue
        ColorUtils.interpolate(colors, 1.0) shouldBe "#0000ff"

        // At 0.5, should be green
        ColorUtils.interpolate(colors, 0.5) shouldBe "#00ff00"
    }
}
