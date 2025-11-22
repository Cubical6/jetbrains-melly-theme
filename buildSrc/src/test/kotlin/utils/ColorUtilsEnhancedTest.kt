package utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ColorUtilsEnhancedTest {

    @Test
    fun `generateIntermediateShade creates halfway color`() {
        val result = ColorUtils.generateIntermediateShade("#000000", "#FFFFFF", 0.5f)
        assertEquals("#808080", result.uppercase()) // 128 = 0x80
    }

    @Test
    fun `generateIntermediateShade at ratio 0 returns first color`() {
        val result = ColorUtils.generateIntermediateShade("#FF0000", "#00FF00", 0.0f)
        assertEquals("#FF0000", result.uppercase())
    }

    @Test
    fun `generateIntermediateShade at ratio 1 returns second color`() {
        val result = ColorUtils.generateIntermediateShade("#FF0000", "#00FF00", 1.0f)
        assertEquals("#00FF00", result.uppercase())
    }

    @Test
    fun `generateColorGradient creates correct number of steps`() {
        val gradient = ColorUtils.generateColorGradient("#000000", "#FFFFFF", 3)

        assertEquals(5, gradient.size) // start + 3 steps + end
        assertEquals("#000000", gradient.first().uppercase())
        assertEquals("#FFFFFF", gradient.last().uppercase())
    }

    @Test
    fun `generateColorGradient with 0 steps returns start and end`() {
        val gradient = ColorUtils.generateColorGradient("#FF0000", "#0000FF", 0)

        assertEquals(2, gradient.size)
        assertEquals("#FF0000", gradient[0].uppercase())
        assertEquals("#0000FF", gradient[1].uppercase())
    }

    @Test
    fun `addAlpha creates ARGB format`() {
        val result = ColorUtils.addAlpha("#FF6B6B", 0.5f)
        assertEquals("#80FF6B6B", result.uppercase()) // 0.5 * 255 = 127.5 ≈ 0x80
    }

    @Test
    fun `addAlpha with 0 is fully transparent`() {
        val result = ColorUtils.addAlpha("#FFFFFF", 0.0f)
        assertEquals("#00FFFFFF", result.uppercase())
    }

    @Test
    fun `addAlpha with 1 is fully opaque`() {
        val result = ColorUtils.addAlpha("#000000", 1.0f)
        assertEquals("#FF000000", result.uppercase())
    }
}
