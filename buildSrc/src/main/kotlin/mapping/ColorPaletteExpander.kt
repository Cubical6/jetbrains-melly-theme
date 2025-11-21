package mapping

import colorschemes.WindowsTerminalColorScheme
import utils.ColorUtils
import kotlin.math.abs

/**
 * Expands a 16-color ANSI palette into a full IntelliJ theme color palette.
 *
 * Takes the limited Windows Terminal color scheme (16 ANSI colors + 4 special colors)
 * and generates additional colors needed for a complete IntelliJ theme through:
 * - Color interpolation (creating gradients between colors)
 * - Tint/shade generation (lightening/darkening variants)
 * - HSV manipulation (saturation/value adjustments)
 * - Complementary color generation
 *
 * This expander is essential for creating rich, nuanced themes from minimal input.
 */
object ColorPaletteExpander {

    /**
     * Expands a Windows Terminal color scheme into a full palette.
     *
     * Generates additional colors for:
     * - UI elements (borders, panels, tooltips)
     * - Interactive states (hover, pressed, selected)
     * - Editor elements (gutter, line numbers, indent guides)
     * - Semantic variants (info, success, warning, error)
     * - Gradient transitions
     *
     * @param scheme The Windows Terminal color scheme with 16 ANSI colors
     * @return Map of color names to hex values, including original and generated colors
     */
    fun expandPalette(scheme: WindowsTerminalColorScheme): Map<String, String> {
        val expanded = mutableMapOf<String, String>()

        // Start with the base palette from the scheme
        expanded.putAll(scheme.toColorPalette())

        // Generate background variants for UI panels and borders
        expanded.putAll(generateBackgroundVariants(scheme.background, scheme.foreground))

        // Generate foreground variants for different text states
        expanded.putAll(generateForegroundVariants(scheme.foreground, scheme.background))

        // Generate interactive state colors (hover, pressed, selected)
        expanded.putAll(generateInteractiveStates(scheme.background, scheme.foreground))

        // Generate semantic color variants (info, success, warning, error)
        expanded.putAll(generateSemanticVariants(scheme))

        // Generate editor-specific colors (gutter, line numbers, indent guides)
        expanded.putAll(generateEditorColors(scheme.background, scheme.foreground))

        // Generate border colors at various intensities
        expanded.putAll(generateBorderColors(scheme.background, scheme.foreground))

        // Generate accent color variants from brightest ANSI color
        expanded.putAll(generateAccentVariants(findAccentColor(scheme)))

        // Generate gradient steps for smooth transitions
        expanded.putAll(generateGradientSteps(scheme))

        return expanded
    }

    /**
     * Generates background variants for panels, sidebars, and other UI elements.
     */
    private fun generateBackgroundVariants(background: String, foreground: String): Map<String, String> {
        return mapOf(
            "bg_lighter" to ColorUtils.lighten(background, 0.05),
            "bg_darker" to ColorUtils.darken(background, 0.05),
            "bg_subtle" to ColorUtils.lighten(background, 0.02),
            "bg_panel" to ColorUtils.lighten(background, 0.03),
            "bg_sidebar" to ColorUtils.darken(background, 0.02),
            "bg_tooltip" to ColorUtils.blend(background, foreground, 0.15),
            "bg_popup" to ColorUtils.lighten(background, 0.04),
            "bg_dialog" to background,
            "bg_menu" to ColorUtils.lighten(background, 0.02)
        )
    }

    /**
     * Generates foreground variants for different text states.
     */
    private fun generateForegroundVariants(foreground: String, background: String): Map<String, String> {
        return mapOf(
            "fg_normal" to foreground,
            "fg_subtle" to ColorUtils.blend(foreground, background, 0.6),
            "fg_muted" to ColorUtils.blend(foreground, background, 0.4),
            "fg_placeholder" to ColorUtils.blend(foreground, background, 0.3),
            "fg_disabled" to ColorUtils.blend(foreground, background, 0.25),
            "fg_bright" to ColorUtils.lighten(foreground, 0.1),
            "fg_link" to ColorUtils.saturate(foreground, 0.3)
        )
    }

    /**
     * Generates colors for interactive states (hover, pressed, selected).
     */
    private fun generateInteractiveStates(background: String, foreground: String): Map<String, String> {
        return mapOf(
            "state_hover" to ColorUtils.blend(background, foreground, 0.1),
            "state_pressed" to ColorUtils.blend(background, foreground, 0.15),
            "state_selected" to ColorUtils.blend(background, foreground, 0.2),
            "state_focused" to ColorUtils.blend(background, foreground, 0.12),
            "state_active" to ColorUtils.blend(background, foreground, 0.18),
            "state_inactive" to ColorUtils.blend(background, foreground, 0.05)
        )
    }

    /**
     * Generates semantic color variants (info, success, warning, error) with tints and shades.
     */
    private fun generateSemanticVariants(scheme: WindowsTerminalColorScheme): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // Info (blue)
        val info = scheme.brightBlue
        result["semantic_info"] = info
        result["semantic_info_bg"] = ColorUtils.blend(scheme.background, info, 0.15)
        result["semantic_info_border"] = ColorUtils.blend(scheme.background, info, 0.3)

        // Success (green)
        val success = scheme.brightGreen
        result["semantic_success"] = success
        result["semantic_success_bg"] = ColorUtils.blend(scheme.background, success, 0.15)
        result["semantic_success_border"] = ColorUtils.blend(scheme.background, success, 0.3)

        // Warning (yellow)
        val warning = scheme.brightYellow
        result["semantic_warning"] = warning
        result["semantic_warning_bg"] = ColorUtils.blend(scheme.background, warning, 0.15)
        result["semantic_warning_border"] = ColorUtils.blend(scheme.background, warning, 0.3)

        // Error (red)
        val error = scheme.brightRed
        result["semantic_error"] = error
        result["semantic_error_bg"] = ColorUtils.blend(scheme.background, error, 0.15)
        result["semantic_error_border"] = ColorUtils.blend(scheme.background, error, 0.3)

        return result
    }

    /**
     * Generates editor-specific colors (gutter, line numbers, indent guides).
     */
    private fun generateEditorColors(background: String, foreground: String): Map<String, String> {
        return mapOf(
            "editor_gutter" to ColorUtils.darken(background, 0.03),
            "editor_line_number" to ColorUtils.blend(foreground, background, 0.35),
            "editor_line_number_active" to ColorUtils.blend(foreground, background, 0.7),
            "editor_indent_guide" to ColorUtils.blend(foreground, background, 0.15),
            "editor_indent_guide_selected" to ColorUtils.blend(foreground, background, 0.25),
            "editor_current_line" to ColorUtils.lighten(background, 0.04),
            "editor_caret_row" to ColorUtils.lighten(background, 0.05),
            "editor_whitespace" to ColorUtils.blend(foreground, background, 0.2)
        )
    }

    /**
     * Generates border colors at various intensities.
     */
    private fun generateBorderColors(background: String, foreground: String): Map<String, String> {
        return mapOf(
            "border_subtle" to ColorUtils.blend(background, foreground, 0.1),
            "border_normal" to ColorUtils.blend(background, foreground, 0.2),
            "border_strong" to ColorUtils.blend(background, foreground, 0.35),
            "border_focus" to ColorUtils.blend(background, foreground, 0.5),
            "border_error" to ColorUtils.blend(background, foreground, 0.4)
        )
    }

    /**
     * Finds the most suitable accent color from the scheme.
     * Uses the brightest saturated color for accent.
     */
    private fun findAccentColor(scheme: WindowsTerminalColorScheme): String {
        val candidates = listOf(
            scheme.brightBlue,
            scheme.brightCyan,
            scheme.brightPurple,
            scheme.brightYellow
        )

        // Find the color with the highest saturation and luminance
        return candidates.maxByOrNull { color ->
            val saturation = ColorUtils.extractSaturation(color)
            val luminance = ColorUtils.calculateLuminance(color)
            saturation * 0.7 + (luminance / 255.0) * 0.3
        } ?: scheme.brightBlue
    }

    /**
     * Generates accent color variants for highlights and emphasis.
     */
    private fun generateAccentVariants(accentColor: String): Map<String, String> {
        return mapOf(
            "accent" to accentColor,
            "accent_light" to ColorUtils.lighten(accentColor, 0.2),
            "accent_lighter" to ColorUtils.lighten(accentColor, 0.4),
            "accent_dark" to ColorUtils.darken(accentColor, 0.2),
            "accent_darker" to ColorUtils.darken(accentColor, 0.4),
            "accent_muted" to ColorUtils.desaturate(accentColor, 0.3)
        )
    }

    /**
     * Generates gradient steps between key colors for smooth transitions.
     */
    private fun generateGradientSteps(scheme: WindowsTerminalColorScheme): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // Background to foreground gradient (useful for various UI elements)
        val bgToFg = interpolateColors(scheme.background, scheme.foreground, 5)
        bgToFg.forEachIndexed { index, color ->
            result["gradient_bg_fg_$index"] = color
        }

        return result
    }

    /**
     * Interpolates between two colors, creating a smooth gradient.
     *
     * @param color1 Starting color in #RRGGBB format
     * @param color2 Ending color in #RRGGBB format
     * @param steps Number of intermediate steps (including endpoints)
     * @return List of interpolated colors from color1 to color2
     */
    fun interpolateColors(color1: String, color2: String, steps: Int): List<String> {
        require(steps >= 2) { "Steps must be at least 2" }

        return (0 until steps).map { step ->
            val ratio = step.toDouble() / (steps - 1)
            ColorUtils.blend(color1, color2, ratio)
        }
    }

    /**
     * Generates tints (lighter variants) of a base color.
     *
     * Tints are created by mixing the color with white, making it lighter
     * while preserving the hue. Useful for creating lighter backgrounds,
     * hover states, and disabled states.
     *
     * @param baseColor Base color in #RRGGBB format
     * @param count Number of tints to generate
     * @return List of tints from subtle to very light
     */
    fun generateTints(baseColor: String, count: Int): List<String> {
        require(count > 0) { "Count must be positive" }

        return (1..count).map { step ->
            val percentage = step.toDouble() / (count + 1)
            ColorUtils.lighten(baseColor, percentage)
        }
    }

    /**
     * Generates shades (darker variants) of a base color.
     *
     * Shades are created by mixing the color with black, making it darker
     * while preserving the hue. Useful for creating darker backgrounds,
     * shadows, and pressed states.
     *
     * @param baseColor Base color in #RRGGBB format
     * @param count Number of shades to generate
     * @return List of shades from subtle to very dark
     */
    fun generateShades(baseColor: String, count: Int): List<String> {
        require(count > 0) { "Count must be positive" }

        return (1..count).map { step ->
            val percentage = step.toDouble() / (count + 1)
            ColorUtils.darken(baseColor, percentage)
        }
    }

    /**
     * Generates saturation variants of a base color.
     *
     * Creates both more saturated (vivid) and less saturated (muted) versions.
     * Useful for creating emphasis variations without changing lightness.
     *
     * @param baseColor Base color in #RRGGBB format
     * @param count Number of variants to generate on each side
     * @return Map of variant names to colors (e.g., "saturated_1", "desaturated_1")
     */
    fun generateSaturationVariants(baseColor: String, count: Int): Map<String, String> {
        require(count > 0) { "Count must be positive" }

        val result = mutableMapOf<String, String>()

        // Generate more saturated variants
        (1..count).forEach { step ->
            val amount = step.toDouble() / (count + 1)
            result["saturated_$step"] = ColorUtils.saturate(baseColor, amount)
        }

        // Generate less saturated (desaturated) variants
        (1..count).forEach { step ->
            val amount = step.toDouble() / (count + 1)
            result["desaturated_$step"] = ColorUtils.desaturate(baseColor, amount)
        }

        return result
    }

    /**
     * Generates a complementary color (opposite on the color wheel).
     *
     * The complementary color is found by rotating the hue by 180 degrees.
     * Useful for creating accent colors that contrast well with the base.
     *
     * @param baseColor Base color in #RRGGBB format
     * @return Complementary color in #RRGGBB format
     */
    fun generateComplementaryColor(baseColor: String): String {
        val (h, s, v) = ColorUtils.hexToHsv(baseColor)
        val complementaryHue = (h + 180) % 360
        return ColorUtils.hsvToHex(complementaryHue, s, v)
    }

    /**
     * Generates analogous colors (adjacent on the color wheel).
     *
     * Analogous colors are typically 30 degrees apart on the color wheel.
     * They create harmonious color schemes. Useful for creating related
     * but distinct colors for different UI elements.
     *
     * @param baseColor Base color in #RRGGBB format
     * @param degrees Degrees of separation on the color wheel (default: 30)
     * @return Pair of analogous colors (left and right on the color wheel)
     */
    fun generateAnalogousColors(baseColor: String, degrees: Double = 30.0): Pair<String, String> {
        require(degrees in 0.0..180.0) { "Degrees must be between 0 and 180" }

        val (h, s, v) = ColorUtils.hexToHsv(baseColor)
        val leftHue = (h - degrees + 360) % 360
        val rightHue = (h + degrees) % 360

        return Pair(
            ColorUtils.hsvToHex(leftHue, s, v),
            ColorUtils.hsvToHex(rightHue, s, v)
        )
    }

    /**
     * Generates triadic colors (120 degrees apart on the color wheel).
     *
     * Triadic colors create vibrant color schemes with good contrast.
     * Useful for creating distinct semantic colors (info, success, warning).
     *
     * @param baseColor Base color in #RRGGBB format
     * @return Pair of triadic colors
     */
    fun generateTriadicColors(baseColor: String): Pair<String, String> {
        val (h, s, v) = ColorUtils.hexToHsv(baseColor)
        val triad1Hue = (h + 120) % 360
        val triad2Hue = (h + 240) % 360

        return Pair(
            ColorUtils.hsvToHex(triad1Hue, s, v),
            ColorUtils.hsvToHex(triad2Hue, s, v)
        )
    }

    /**
     * Generates split-complementary colors (complementary +/- 30 degrees).
     *
     * Split-complementary colors provide strong contrast while being less
     * jarring than true complementary colors. Useful for creating balanced
     * color schemes with visual interest.
     *
     * @param baseColor Base color in #RRGGBB format
     * @param degrees Degrees of separation from complementary (default: 30)
     * @return Pair of split-complementary colors
     */
    fun generateSplitComplementaryColors(baseColor: String, degrees: Double = 30.0): Pair<String, String> {
        require(degrees in 0.0..90.0) { "Degrees must be between 0 and 90" }

        val (h, s, v) = ColorUtils.hexToHsv(baseColor)
        val complementaryHue = (h + 180) % 360
        val split1Hue = (complementaryHue - degrees + 360) % 360
        val split2Hue = (complementaryHue + degrees) % 360

        return Pair(
            ColorUtils.hsvToHex(split1Hue, s, v),
            ColorUtils.hsvToHex(split2Hue, s, v)
        )
    }

    /**
     * Generates a monochromatic palette from a base color.
     *
     * Creates variations by adjusting value (brightness) while keeping
     * hue and saturation constant. Useful for creating cohesive UI elements.
     *
     * @param baseColor Base color in #RRGGBB format
     * @param count Number of variations to generate
     * @return List of monochromatic colors from dark to light
     */
    fun generateMonochromaticPalette(baseColor: String, count: Int): List<String> {
        require(count > 0) { "Count must be positive" }

        val (h, s, _) = ColorUtils.hexToHsv(baseColor)

        return (0 until count).map { step ->
            val value = (step + 1).toDouble() / (count + 1)
            ColorUtils.hsvToHex(h, s, value)
        }
    }

    /**
     * Adjusts a color's luminance to meet a specific target.
     *
     * Useful for ensuring colors meet WCAG contrast requirements or
     * maintaining consistent brightness across a palette.
     *
     * @param color Color in #RRGGBB format
     * @param targetLuminance Target luminance (0.0 to 255.0)
     * @param maxIterations Maximum adjustment iterations (default: 10)
     * @return Color adjusted to target luminance
     */
    fun adjustToLuminance(color: String, targetLuminance: Double, maxIterations: Int = 10): String {
        require(targetLuminance in 0.0..255.0) { "Target luminance must be between 0 and 255" }
        require(maxIterations > 0) { "Max iterations must be positive" }

        var adjusted = color
        var currentLuminance = ColorUtils.calculateLuminance(adjusted)

        repeat(maxIterations) {
            if (abs(currentLuminance - targetLuminance) < 5.0) {
                return adjusted
            }

            adjusted = if (currentLuminance < targetLuminance) {
                val percentage = 0.1
                ColorUtils.lighten(adjusted, percentage)
            } else {
                val percentage = 0.1
                ColorUtils.darken(adjusted, percentage)
            }

            currentLuminance = ColorUtils.calculateLuminance(adjusted)
        }

        return adjusted
    }

    /**
     * Generates a color with specified contrast ratio against a background.
     *
     * Useful for generating foreground colors that meet WCAG requirements.
     *
     * @param baseColor Starting color in #RRGGBB format
     * @param backgroundColor Background color for contrast calculation
     * @param targetContrast Target WCAG contrast ratio (e.g., 4.5 for AA, 7.0 for AAA)
     * @param maxIterations Maximum adjustment iterations
     * @return Color adjusted to meet target contrast ratio
     */
    fun adjustToContrastRatio(
        baseColor: String,
        backgroundColor: String,
        targetContrast: Double,
        maxIterations: Int = 20
    ): String {
        require(targetContrast in 1.0..21.0) { "Contrast ratio must be between 1 and 21" }
        require(maxIterations > 0) { "Max iterations must be positive" }

        var adjusted = baseColor
        var currentContrast = ColorUtils.calculateContrastRatio(adjusted, backgroundColor)

        repeat(maxIterations) {
            if (abs(currentContrast - targetContrast) < 0.1) {
                return adjusted
            }

            adjusted = if (currentContrast < targetContrast) {
                // Need more contrast - make it lighter or darker away from background
                if (ColorUtils.isDark(backgroundColor)) {
                    ColorUtils.lighten(adjusted, 0.05)
                } else {
                    ColorUtils.darken(adjusted, 0.05)
                }
            } else {
                // Too much contrast - move closer to background
                if (ColorUtils.isDark(backgroundColor)) {
                    ColorUtils.darken(adjusted, 0.05)
                } else {
                    ColorUtils.lighten(adjusted, 0.05)
                }
            }

            currentContrast = ColorUtils.calculateContrastRatio(adjusted, backgroundColor)
        }

        return adjusted
    }
}
