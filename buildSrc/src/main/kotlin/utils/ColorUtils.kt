package utils

import kotlin.math.pow

/**
 * Comprehensive color utility functions for theme generation.
 *
 * Provides:
 * - Color format conversion (hex ↔ RGB)
 * - Color manipulation (lighten, darken)
 * - WCAG contrast calculation
 * - Color blending and interpolation
 * - Color analysis (luminance, hue, saturation)
 *
 * All hex colors are in #RRGGBB format.
 *
 * Performance optimizations:
 * - Caches frequently used calculations (luminance, HSV conversion, RGB conversion)
 * - Thread-safe caching using ConcurrentHashMap
 */
object ColorUtils {

    // Caches for expensive calculations (thread-safe)
    private val hexToRgbCache = java.util.concurrent.ConcurrentHashMap<String, Triple<Int, Int, Int>>()
    private val luminanceCache = java.util.concurrent.ConcurrentHashMap<String, Double>()
    private val hsvCache = java.util.concurrent.ConcurrentHashMap<String, Triple<Double, Double, Double>>()
    private val contrastRatioCache = java.util.concurrent.ConcurrentHashMap<Pair<String, String>, Double>()

    /**
     * Converts hex color to RGB components with caching.
     *
     * @param hex Color in #RRGGBB format
     * @return Triple of (R, G, B) values (0-255)
     * @throws IllegalArgumentException if hex format is invalid
     */
    fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        return hexToRgbCache.getOrPut(hex) {
            val cleanHex = hex.removePrefix("#")
            require(cleanHex.length == 6) { "Invalid hex color: $hex (expected #RRGGBB format)" }
            require(cleanHex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                "Invalid hex color: $hex (contains invalid characters)"
            }

            val r = cleanHex.substring(0, 2).toInt(16)
            val g = cleanHex.substring(2, 4).toInt(16)
            val b = cleanHex.substring(4, 6).toInt(16)

            Triple(r, g, b)
        }
    }

    /**
     * Converts RGB components to hex color.
     *
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @return Color in #RRGGBB format
     * @throws IllegalArgumentException if RGB values are out of range
     */
    fun rgbToHex(r: Int, g: Int, b: Int): String {
        require(r in 0..255) { "Red value out of range: $r (must be 0-255)" }
        require(g in 0..255) { "Green value out of range: $g (must be 0-255)" }
        require(b in 0..255) { "Blue value out of range: $b (must be 0-255)" }

        return "#%02x%02x%02x".format(r, g, b)
    }

    /**
     * Lightens a color by the specified percentage.
     *
     * @param hex Color in #RRGGBB format
     * @param percentage Amount to lighten (0.0 = no change, 1.0 = white)
     * @return Lightened color in #RRGGBB format
     */
    fun lighten(hex: String, percentage: Double): String {
        require(percentage in 0.0..1.0) { "Percentage must be between 0.0 and 1.0" }

        val (r, g, b) = hexToRgb(hex)

        val newR = (r + (255 - r) * percentage).toInt().coerceIn(0, 255)
        val newG = (g + (255 - g) * percentage).toInt().coerceIn(0, 255)
        val newB = (b + (255 - b) * percentage).toInt().coerceIn(0, 255)

        return rgbToHex(newR, newG, newB)
    }

    /**
     * Darkens a color by the specified percentage.
     *
     * @param hex Color in #RRGGBB format
     * @param percentage Amount to darken (0.0 = no change, 1.0 = black)
     * @return Darkened color in #RRGGBB format
     */
    fun darken(hex: String, percentage: Double): String {
        require(percentage in 0.0..1.0) { "Percentage must be between 0.0 and 1.0" }

        val (r, g, b) = hexToRgb(hex)

        val newR = (r * (1 - percentage)).toInt().coerceIn(0, 255)
        val newG = (g * (1 - percentage)).toInt().coerceIn(0, 255)
        val newB = (b * (1 - percentage)).toInt().coerceIn(0, 255)

        return rgbToHex(newR, newG, newB)
    }

    /**
     * Calculates WCAG 2.0 contrast ratio between two colors with caching.
     *
     * @param color1 First color in #RRGGBB format
     * @param color2 Second color in #RRGGBB format
     * @return Contrast ratio (1.0 to 21.0)
     */
    fun calculateContrastRatio(color1: String, color2: String): Double {
        val key = Pair(color1, color2)
        return contrastRatioCache.getOrPut(key) {
            val l1 = calculateRelativeLuminance(color1)
            val l2 = calculateRelativeLuminance(color2)

            val lighter = maxOf(l1, l2)
            val darker = minOf(l1, l2)

            (lighter + 0.05) / (darker + 0.05)
        }
    }

    /**
     * Calculates relative luminance according to WCAG 2.0.
     *
     * @param hexColor Color in #RRGGBB format
     * @return Relative luminance (0.0 to 1.0)
     */
    fun calculateRelativeLuminance(hexColor: String): Double {
        val (r, g, b) = hexToRgb(hexColor)

        val sR = r / 255.0
        val sG = g / 255.0
        val sB = b / 255.0

        // Apply gamma correction
        val rLin = if (sR <= 0.03928) sR / 12.92 else ((sR + 0.055) / 1.055).pow(2.4)
        val gLin = if (sG <= 0.03928) sG / 12.92 else ((sG + 0.055) / 1.055).pow(2.4)
        val bLin = if (sB <= 0.03928) sB / 12.92 else ((sB + 0.055) / 1.055).pow(2.4)

        return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin
    }

    /**
     * Calculates perceived luminance (simpler than relative luminance) with caching.
     * Useful for color classification.
     *
     * @param hexColor Color in #RRGGBB format
     * @return Perceived luminance (0.0 to 255.0)
     */
    fun calculateLuminance(hexColor: String): Double {
        return luminanceCache.getOrPut(hexColor) {
            val (r, g, b) = hexToRgb(hexColor)
            0.299 * r + 0.587 * g + 0.114 * b
        }
    }

    /**
     * Blends two colors with the specified ratio.
     *
     * @param color1 First color in #RRGGBB format
     * @param color2 Second color in #RRGGBB format
     * @param ratio Blend ratio (0.0 = all color1, 1.0 = all color2)
     * @return Blended color in #RRGGBB format
     */
    fun blend(color1: String, color2: String, ratio: Double = 0.5): String {
        require(ratio in 0.0..1.0) { "Ratio must be between 0.0 and 1.0" }

        val (r1, g1, b1) = hexToRgb(color1)
        val (r2, g2, b2) = hexToRgb(color2)

        val r = (r1 * (1 - ratio) + r2 * ratio).toInt().coerceIn(0, 255)
        val g = (g1 * (1 - ratio) + g2 * ratio).toInt().coerceIn(0, 255)
        val b = (b1 * (1 - ratio) + b2 * ratio).toInt().coerceIn(0, 255)

        return rgbToHex(r, g, b)
    }

    /**
     * Interpolates between multiple colors.
     *
     * @param colors List of colors to interpolate between
     * @param position Position in the gradient (0.0 to 1.0)
     * @return Interpolated color in #RRGGBB format
     */
    fun interpolate(colors: List<String>, position: Double): String {
        require(colors.isNotEmpty()) { "Colors list cannot be empty" }
        require(position in 0.0..1.0) { "Position must be between 0.0 and 1.0" }

        if (colors.size == 1) return colors[0]

        val segmentSize = 1.0 / (colors.size - 1)
        val segmentIndex = (position / segmentSize).toInt().coerceIn(0, colors.size - 2)
        val segmentPosition = (position - segmentIndex * segmentSize) / segmentSize

        return blend(colors[segmentIndex], colors[segmentIndex + 1], segmentPosition)
    }

    /**
     * Converts hex color to HSV (Hue, Saturation, Value) with caching.
     *
     * @param hexColor Color in #RRGGBB format
     * @return Triple of (H: 0-360, S: 0.0-1.0, V: 0.0-1.0)
     */
    fun hexToHsv(hexColor: String): Triple<Double, Double, Double> {
        return hsvCache.getOrPut(hexColor) {
            val (r, g, b) = hexToRgb(hexColor)

            val rNorm = r / 255.0
            val gNorm = g / 255.0
            val bNorm = b / 255.0

            val max = maxOf(rNorm, gNorm, bNorm)
            val min = minOf(rNorm, gNorm, bNorm)
            val delta = max - min

            // Hue calculation
            val hue = when {
                delta == 0.0 -> 0.0
                max == rNorm -> ((gNorm - bNorm) / delta % 6) * 60
                max == gNorm -> ((bNorm - rNorm) / delta + 2) * 60
                else -> ((rNorm - gNorm) / delta + 4) * 60
            }

            // Saturation calculation
            val saturation = if (max == 0.0) 0.0 else delta / max

            // Value calculation
            val value = max

            Triple((hue + 360) % 360, saturation, value)
        }
    }

    /**
     * Converts HSV to hex color.
     *
     * @param hue Hue (0-360)
     * @param saturation Saturation (0.0-1.0)
     * @param value Value (0.0-1.0)
     * @return Color in #RRGGBB format
     */
    fun hsvToHex(hue: Double, saturation: Double, value: Double): String {
        require(hue in 0.0..360.0) { "Hue must be between 0 and 360" }
        require(saturation in 0.0..1.0) { "Saturation must be between 0.0 and 1.0" }
        require(value in 0.0..1.0) { "Value must be between 0.0 and 1.0" }

        val c = value * saturation
        val x = c * (1 - kotlin.math.abs((hue / 60) % 2 - 1))
        val m = value - c

        val (rPrime, gPrime, bPrime) = when {
            hue < 60 -> Triple(c, x, 0.0)
            hue < 120 -> Triple(x, c, 0.0)
            hue < 180 -> Triple(0.0, c, x)
            hue < 240 -> Triple(0.0, x, c)
            hue < 300 -> Triple(x, 0.0, c)
            else -> Triple(c, 0.0, x)
        }

        val r = ((rPrime + m) * 255).toInt().coerceIn(0, 255)
        val g = ((gPrime + m) * 255).toInt().coerceIn(0, 255)
        val b = ((bPrime + m) * 255).toInt().coerceIn(0, 255)

        return rgbToHex(r, g, b)
    }

    /**
     * Extracts hue from a color.
     *
     * @param hexColor Color in #RRGGBB format
     * @return Hue (0-360)
     */
    fun extractHue(hexColor: String): Double {
        return hexToHsv(hexColor).first
    }

    /**
     * Extracts saturation from a color.
     *
     * @param hexColor Color in #RRGGBB format
     * @return Saturation (0.0-1.0)
     */
    fun extractSaturation(hexColor: String): Double {
        return hexToHsv(hexColor).second
    }

    /**
     * Checks if a color is grayscale (low saturation).
     *
     * @param hexColor Color in #RRGGBB format
     * @param threshold Saturation threshold (default: 0.15 = 15%)
     * @return True if grayscale
     */
    fun isGrayscale(hexColor: String, threshold: Double = 0.15): Boolean {
        return extractSaturation(hexColor) < threshold
    }

    /**
     * Checks if a color is dark (low luminance).
     *
     * @param hexColor Color in #RRGGBB format
     * @param threshold Luminance threshold (default: 100)
     * @return True if dark
     */
    fun isDark(hexColor: String, threshold: Double = 100.0): Boolean {
        return calculateLuminance(hexColor) < threshold
    }

    /**
     * Checks if a color is bright (high luminance).
     *
     * @param hexColor Color in #RRGGBB format
     * @param threshold Luminance threshold (default: 155)
     * @return True if bright
     */
    fun isBright(hexColor: String, threshold: Double = 155.0): Boolean {
        return calculateLuminance(hexColor) > threshold
    }

    /**
     * Increases saturation of a color.
     *
     * @param hexColor Color in #RRGGBB format
     * @param amount Amount to increase (0.0 to 1.0)
     * @return Color with increased saturation
     */
    fun saturate(hexColor: String, amount: Double): String {
        require(amount in 0.0..1.0) { "Amount must be between 0.0 and 1.0" }

        val (h, s, v) = hexToHsv(hexColor)
        val newSaturation = (s + amount).coerceIn(0.0, 1.0)
        return hsvToHex(h, newSaturation, v)
    }

    /**
     * Decreases saturation of a color.
     *
     * @param hexColor Color in #RRGGBB format
     * @param amount Amount to decrease (0.0 to 1.0)
     * @return Color with decreased saturation
     */
    fun desaturate(hexColor: String, amount: Double): String {
        require(amount in 0.0..1.0) { "Amount must be between 0.0 and 1.0" }

        val (h, s, v) = hexToHsv(hexColor)
        val newSaturation = (s - amount).coerceIn(0.0, 1.0)
        return hsvToHex(h, newSaturation, v)
    }
}
