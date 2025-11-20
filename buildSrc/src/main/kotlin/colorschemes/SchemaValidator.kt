package colorschemes

import utils.ColorUtils

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
        val fgBgRatio = ColorUtils.calculateContrastRatio(scheme.foreground, scheme.background)
        if (fgBgRatio < MINIMUM_CONTRAST) {
            warnings.add("Low contrast between foreground and background: ${"%.2f".format(fgBgRatio)} (minimum: $MINIMUM_CONTRAST)")
        } else if (fgBgRatio < WCAG_AA_NORMAL) {
            warnings.add("Contrast between foreground and background is below WCAG AA: ${"%.2f".format(fgBgRatio)} (recommended: $WCAG_AA_NORMAL)")
        }

        // Check cursor color contrast if present
        scheme.cursorColor?.let { cursor ->
            val cursorRatio = ColorUtils.calculateContrastRatio(cursor, scheme.background)
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

        val luminances = colors.map { ColorUtils.calculateLuminance(it) }
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
        val fgBgRatio = ColorUtils.calculateContrastRatio(scheme.foreground, scheme.background)
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
        val luminances = colors.map { ColorUtils.calculateLuminance(it) }

        val minLuminance = luminances.minOrNull() ?: 0.0
        val maxLuminance = luminances.maxOrNull() ?: 0.0

        val range = maxLuminance - minLuminance
        return range < (255 * MONOCHROME_THRESHOLD)
    }

    /**
     * Checks if the color scheme has a limited palette (few unique hues).
     * Filters out grayscale colors to avoid false positives.
     */
    private fun hasLimitedPalette(scheme: WindowsTerminalColorScheme): Boolean {
        val colors = scheme.getAllColors()

        // Filter out grayscale colors to avoid false positives
        // (all grayscale colors would have hue = 0, which would skew the count)
        val chromaticColors = colors.filterNot { ColorUtils.isGrayscale(it) }

        // If no chromatic colors, it's definitely a limited palette
        if (chromaticColors.isEmpty()) return true

        val uniqueHues = chromaticColors.map { ColorUtils.extractHue(it).toInt() }.toSet()

        // Consider limited if less than 3 distinct hues
        return uniqueHues.size < 3
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
