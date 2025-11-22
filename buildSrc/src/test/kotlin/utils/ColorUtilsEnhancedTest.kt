package utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ColorUtilsEnhancedTest {

    @Test
    fun `generateIntermediateShade creates halfway color`() {
        val result = ColorUtils.generateIntermediateShade("#000000", "#ffffff", 0.5f)
        assertEquals("#808080", result) // 128 = 0x80
    }

    @Test
    fun `generateIntermediateShade at ratio 0 returns first color`() {
        val result = ColorUtils.generateIntermediateShade("#ff0000", "#00ff00", 0.0f)
        assertEquals("#ff0000", result)
    }

    @Test
    fun `generateIntermediateShade at ratio 1 returns second color`() {
        val result = ColorUtils.generateIntermediateShade("#ff0000", "#00ff00", 1.0f)
        assertEquals("#00ff00", result)
    }

    @Test
    fun `generateColorGradient creates correct number of steps`() {
        val gradient = ColorUtils.generateColorGradient("#000000", "#ffffff", 3)

        assertEquals(5, gradient.size) // start + 3 steps + end
        assertEquals("#000000", gradient.first())
        assertEquals("#ffffff", gradient.last())
    }

    @Test
    fun `generateColorGradient with 0 steps returns start and end`() {
        val gradient = ColorUtils.generateColorGradient("#ff0000", "#0000ff", 0)

        assertEquals(2, gradient.size)
        assertEquals("#ff0000", gradient[0])
        assertEquals("#0000ff", gradient[1])
    }

    @Test
    fun `addAlpha creates ARGB format`() {
        val result = ColorUtils.addAlpha("#ff6b6b", 0.5f)
        assertEquals("#7fff6b6b", result) // 0.5 * 255 = 127 = 0x7f
    }

    @Test
    fun `addAlpha with 0 is fully transparent`() {
        val result = ColorUtils.addAlpha("#ffffff", 0.0f)
        assertEquals("#00ffffff", result)
    }

    @Test
    fun `addAlpha with 1 is fully opaque`() {
        val result = ColorUtils.addAlpha("#000000", 1.0f)
        assertEquals("#ff000000", result)
    }
}
