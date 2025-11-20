package colorschemes

import kotlin.math.pow

/**
 * Advanced validator for Windows Terminal color schemes.
 *
 * Performs comprehensive validation including:
 * - Hex color format validation
 * - WCAG contrast ratio checks
 * - Brightness distribution analysis
 * - Edge case detection (monochrome, high/low contrast)
 *
 * Based on COLOR_MAPPING_SPEC.yaml validation rules.
 */
class SchemaValidator {
    companion object {
        // WCAG contrast ratio thresholds
        const val WCAG_AAA = 7.0
        const val WCAG_AA_NORMAL = 4.5
        const val WCAG_AA_LARGE = 3.0
        const val MINIMUM_CONTRAST = 3.0

        // Brightness thresholds
        const val MONOCHROME_THRESHOLD = 0.05  // 5% brightness variation
        const val BRIGHTNESS_UNIFORMITY_THRESHOLD = 0.8  // 80% in same class
    }

    /**
     * Validates a color scheme with comprehensive checks.
     *
     * @param scheme The color scheme to validate
     * @return ValidationResult with errors, warnings, and metadata
     */
    fun validate(scheme: WindowsTerminalColorScheme): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Basic validation (already done by scheme.validate())
        errors.addAll(scheme.validate())

        // Contrast validation
        val contrastIssues = validateContrast(scheme)
        warnings.addAll(contrastIssues)

        // Brightness distribution
        val brightnessIssues = validateBrightnessDistribution(scheme)
        warnings.addAll(brightnessIssues)

        // Edge case detection
        val edgeCases = detectEdgeCases(scheme)

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            edgeCases = edgeCases,
            schemeName = scheme.name
        )
    }

    /**
     * Validates contrast ratios between foreground/background pairs.
     */
    private fun validateContrast(scheme: WindowsTerminalColorScheme): List<String> {
        val warnings = mutableListOf<String>()

        // Check foreground/background contrast
        val fgBgRatio = calculateContrastRatio(scheme.foreground, scheme.background)
        if (fgBgRatio < MINIMUM_CONTRAST) {
            warnings.add("Low contrast between foreground and background: ${"%.2f".format(fgBgRatio)} (minimum: $MINIMUM_CONTRAST)")
        } else if (fgBgRatio < WCAG_AA_NORMAL) {
            warnings.add("Contrast between foreground and background is below WCAG AA: ${"%.2f".format(fgBgRatio)} (recommended: $WCAG_AA_NORMAL)")
        }

        // Check cursor color contrast if present
        scheme.cursorColor?.let { cursor ->
            val cursorRatio = calculateContrastRatio(cursor, scheme.background)
            if (cursorRatio < MINIMUM_CONTRAST) {
                warnings.add("Low contrast between cursor and background: ${"%.2f".format(cursorRatio)}")
            }
        }

        return warnings
    }

    /**
     * Validates brightness distribution across all colors.
     */
    private fun validateBrightnessDistribution(scheme: WindowsTerminalColorScheme): List<String> {
        val warnings = mutableListOf<String>()
        val colors = scheme.getAllColors()

        val luminances = colors.map { calculateLuminance(it) }
        val darkCount = luminances.count { it < 100 }
        val brightCount = luminances.count { it > 155 }

        val darkRatio = darkCount.toDouble() / colors.size
        val brightRatio = brightCount.toDouble() / colors.size

        if (darkRatio > BRIGHTNESS_UNIFORMITY_THRESHOLD) {
            warnings.add("Most colors are dark (${(darkRatio * 100).toInt()}%). This may limit syntax highlighting variety.")
        }

        if (brightRatio > BRIGHTNESS_UNIFORMITY_THRESHOLD) {
            warnings.add("Most colors are bright (${(brightRatio * 100).toInt()}%). This may cause visual fatigue.")
        }

        return warnings
    }

    /**
     * Detects edge cases in the color scheme.
     */
    private fun detectEdgeCases(scheme: WindowsTerminalColorScheme): List<EdgeCase> {
        val edgeCases = mutableListOf<EdgeCase>()

        // Check for monochrome palette
        if (isMonochrome(scheme)) {
            edgeCases.add(EdgeCase.MONOCHROME)
        }

        // Check for high contrast
        val fgBgRatio = calculateContrastRatio(scheme.foreground, scheme.background)
        if (fgBgRatio >= WCAG_AAA) {
            edgeCases.add(EdgeCase.HIGH_CONTRAST)
        }

        // Check for low contrast
        if (fgBgRatio < MINIMUM_CONTRAST) {
            edgeCases.add(EdgeCase.LOW_CONTRAST)
        }

        // Check for limited palette (few unique hues)
        if (hasLimitedPalette(scheme)) {
            edgeCases.add(EdgeCase.LIMITED_PALETTE)
        }

        return edgeCases
    }

    /**
     * Checks if the color scheme is effectively monochrome.
     */
    private fun isMonochrome(scheme: WindowsTerminalColorScheme): Boolean {
        val colors = scheme.getAllColors()
        val luminances = colors.map { calculateLuminance(it) }

        val minLuminance = luminances.minOrNull() ?: 0.0
        val maxLuminance = luminances.maxOrNull() ?: 0.0

        val range = maxLuminance - minLuminance
        return range < (255 * MONOCHROME_THRESHOLD)
    }

    /**
     * Checks if the color scheme has a limited palette (few unique hues).
     */
    private fun hasLimitedPalette(scheme: WindowsTerminalColorScheme): Boolean {
        val colors = scheme.getAllColors()
        val uniqueHues = colors.map { extractHue(it) }.toSet()

        // Consider limited if less than 3 distinct hues
        return uniqueHues.size < 3
    }

    /**
     * Calculates WCAG contrast ratio between two colors.
     */
    private fun calculateContrastRatio(color1: String, color2: String): Double {
        val l1 = calculateRelativeLuminance(color1)
        val l2 = calculateRelativeLuminance(color2)

        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)

        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * Calculates relative luminance for WCAG contrast calculation.
     */
    private fun calculateRelativeLuminance(hexColor: String): Double {
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
     * Calculates perceived luminance (simpler than relative luminance).
     */
    private fun calculateLuminance(hexColor: String): Double {
        val (r, g, b) = hexToRgb(hexColor)
        return 0.299 * r + 0.587 * g + 0.114 * b
    }

    /**
     * Extracts hue from a hex color (simplified HSV conversion).
     */
    private fun extractHue(hexColor: String): Int {
        val (r, g, b) = hexToRgb(hexColor)

        val max = maxOf(r, g, b).toDouble()
        val min = minOf(r, g, b).toDouble()
        val delta = max - min

        if (delta == 0.0) return 0  // Grayscale (no hue)

        val hue = when (max) {
            r.toDouble() -> ((g - b) / delta % 6) * 60
            g.toDouble() -> ((b - r) / delta + 2) * 60
            else -> ((r - g) / delta + 4) * 60
        }

        return ((hue + 360) % 360).toInt()
    }

    /**
     * Converts hex color to RGB components.
     */
    private fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        val cleanHex = hex.removePrefix("#")
        require(cleanHex.length == 6) { "Invalid hex color: $hex" }

        val r = cleanHex.substring(0, 2).toInt(16)
        val g = cleanHex.substring(2, 4).toInt(16)
        val b = cleanHex.substring(4, 6).toInt(16)

        return Triple(r, g, b)
    }
}

/**
 * Result of schema validation.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>,
    val edgeCases: List<EdgeCase>,
    val schemeName: String
) {
    /**
     * Generates a human-readable validation report.
     */
    fun toReport(): String {
        return buildString {
            appendLine("Validation Report: $schemeName")
            appendLine("=".repeat(50))

            if (isValid) {
                appendLine("✓ Schema is valid")
            } else {
                appendLine("✗ Schema has errors")
            }

            appendLine()

            if (errors.isNotEmpty()) {
                appendLine("ERRORS:")
                errors.forEach { appendLine("  ✗ $it") }
                appendLine()
            }

            if (warnings.isNotEmpty()) {
                appendLine("WARNINGS:")
                warnings.forEach { appendLine("  ⚠ $it") }
                appendLine()
            }

            if (edgeCases.isNotEmpty()) {
                appendLine("EDGE CASES DETECTED:")
                edgeCases.forEach { appendLine("  ⓘ ${it.description}") }
                appendLine()
            }

            if (errors.isEmpty() && warnings.isEmpty() && edgeCases.isEmpty()) {
                appendLine("No issues found. Excellent color scheme!")
            }
        }
    }
}

/**
 * Edge cases that may require special handling.
 */
enum class EdgeCase(val description: String) {
    MONOCHROME("Monochrome palette detected (< 5% brightness variation)"),
    HIGH_CONTRAST("High contrast scheme (WCAG AAA)"),
    LOW_CONTRAST("Low contrast scheme (below WCAG minimum)"),
    LIMITED_PALETTE("Limited color palette (< 3 unique hues)")
}
