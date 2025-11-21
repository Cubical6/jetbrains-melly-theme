package mapping

import colorschemes.WindowsTerminalColorScheme
import utils.ColorUtils

/**
 * Intelligent syntax color inference engine.
 *
 * Converts Windows Terminal color schemes (16 ANSI colors) to IntelliJ syntax highlighting
 * attributes (100+ attributes) using intelligent classification and mapping algorithms.
 *
 * Algorithm:
 * 1. Classify all colors by luminance (DARK/MID/BRIGHT)
 * 2. Detect edge cases (monochrome, limited palette, extreme contrast)
 * 3. Map colors to syntax attributes using semantic rules
 * 4. Apply fallback strategies when needed (font styles for monochrome)
 */
object SyntaxColorInference {

    /**
     * Infers syntax colors from a Windows Terminal color scheme.
     *
     * @param scheme Windows Terminal color scheme to analyze
     * @return Map of IntelliJ attribute names to color values (hex format)
     */
    fun inferSyntaxColors(scheme: WindowsTerminalColorScheme): Map<String, SyntaxColor> {
        // Detect edge cases
        val isMonochrome = detectMonochrome(scheme)
        val contrastLevel = analyzeContrast(scheme)
        val paletteAnalysis = analyzePalette(scheme)

        // Classify all colors
        val colorClassifications = classifyColors(scheme)

        // Build result map using inference rules
        val result = mutableMapOf<String, SyntaxColor>()

        ColorMappingConfig.syntaxInferenceRules.forEach { (attributeName, rule) ->
            val syntaxColor = inferColorForAttribute(
                attributeName = attributeName,
                rule = rule,
                scheme = scheme,
                colorClassifications = colorClassifications,
                isMonochrome = isMonochrome,
                contrastLevel = contrastLevel,
                paletteAnalysis = paletteAnalysis
            )
            result[attributeName] = syntaxColor
        }

        // Add common attributes that may not be in the rules
        addCommonAttributes(result, scheme, colorClassifications, isMonochrome)

        return result
    }

    /**
     * Infers color for a single syntax attribute.
     */
    private fun inferColorForAttribute(
        attributeName: String,
        rule: SyntaxRule,
        scheme: WindowsTerminalColorScheme,
        colorClassifications: Map<String, ColorClassification>,
        isMonochrome: Boolean,
        contrastLevel: ContrastLevel,
        paletteAnalysis: PaletteAnalysis
    ): SyntaxColor {
        // If inheriting from another attribute, use that as base
        val baseColor = rule.inheritFrom?.let { parentAttr ->
            ColorMappingConfig.getSyntaxRule(parentAttr)?.let { parentRule ->
                inferColorForAttribute(
                    parentAttr, parentRule, scheme, colorClassifications,
                    isMonochrome, contrastLevel, paletteAnalysis
                )
            }
        }

        // Find best matching color from preferred sources
        var selectedColor: String? = null

        for (source in rule.preferredSources) {
            val color = getColorFromScheme(scheme, source)
            val classification = colorClassifications[color]

            // Check if color matches the rule requirements
            val matchesLuminance = rule.luminanceClass == null ||
                    rule.luminanceClass == classification?.luminanceClass

            val matchesHue = rule.hueRange == null ||
                    classification?.hue?.let { it in rule.hueRange } == true

            if (matchesLuminance && matchesHue) {
                selectedColor = color
                break
            }
        }

        // Fallback: use first preferred source if no match found
        if (selectedColor == null && rule.preferredSources.isNotEmpty()) {
            selectedColor = getColorFromScheme(scheme, rule.preferredSources.first())
        }

        // Fallback: use foreground color as last resort
        if (selectedColor == null) {
            selectedColor = scheme.foreground
        }

        // Apply transformations
        var finalColor = selectedColor

        val afterDimming = rule.applyDimming?.let { dimFactor ->
            ColorUtils.darken(finalColor!!, 1.0 - dimFactor)
        } ?: finalColor

        finalColor = afterDimming

        val afterLightening = rule.applyLightening?.let { lightenAmount ->
            ColorUtils.lighten(finalColor!!, lightenAmount)
        } ?: finalColor

        finalColor = afterLightening

        // Determine font style
        val fontStyle = if (isMonochrome) {
            // For monochrome palettes, use font styles to differentiate
            determineFontStyleForMonochrome(attributeName, rule)
        } else {
            rule.fontStyle ?: FontStyle.REGULAR
        }

        // Adjust for contrast if needed
        val adjustedColor = if (contrastLevel == ContrastLevel.LOW) {
            adjustForLowContrast(finalColor!!, scheme.background)
        } else if (contrastLevel == ContrastLevel.HIGH) {
            adjustForHighContrast(finalColor!!, scheme.background)
        } else {
            finalColor!!
        }

        return SyntaxColor(
            color = adjustedColor,
            fontStyle = fontStyle,
            effectType = rule.effectType
        )
    }

    /**
     * Classifies all colors in the scheme by luminance and hue.
     */
    private fun classifyColors(scheme: WindowsTerminalColorScheme): Map<String, ColorClassification> {
        val result = mutableMapOf<String, ColorClassification>()

        scheme.getAllColors().forEach { color ->
            result[color] = classifyColor(color)
        }

        // Also classify foreground/background
        result[scheme.foreground] = classifyColor(scheme.foreground)
        result[scheme.background] = classifyColor(scheme.background)

        return result
    }

    /**
     * Classifies a single color.
     */
    private fun classifyColor(hexColor: String): ColorClassification {
        val luminance = ColorUtils.calculateLuminance(hexColor)
        val (hue, saturation, _) = ColorUtils.hexToHsv(hexColor)

        val luminanceClass = when {
            luminance < ColorMappingConfig.DARK_LUMINANCE_MAX -> LuminanceClass.DARK
            luminance < ColorMappingConfig.MID_LUMINANCE_MAX -> LuminanceClass.MID
            else -> LuminanceClass.BRIGHT
        }

        return ColorClassification(
            luminance = luminance,
            luminanceClass = luminanceClass,
            hue = hue,
            saturation = saturation,
            isGrayscale = saturation < ColorMappingConfig.GRAYSCALE_SATURATION_MAX
        )
    }

    /**
     * Detects if the palette is monochrome (all grayscale or very limited color variation).
     */
    private fun detectMonochrome(scheme: WindowsTerminalColorScheme): Boolean {
        val luminances = scheme.getAllColors().map { ColorUtils.calculateLuminance(it) }
        val range = (luminances.maxOrNull() ?: 0.0) - (luminances.minOrNull() ?: 0.0)
        val normalizedRange = range / 255.0

        // Check if all colors are grayscale
        val allGrayscale = scheme.getAllColors().all {
            ColorUtils.isGrayscale(it, ColorMappingConfig.GRAYSCALE_SATURATION_MAX)
        }

        return allGrayscale || normalizedRange < ColorMappingConfig.MONOCHROME_THRESHOLD
    }

    /**
     * Analyzes the contrast level of the scheme.
     */
    private fun analyzeContrast(scheme: WindowsTerminalColorScheme): ContrastLevel {
        val contrastRatio = ColorUtils.calculateContrastRatio(scheme.foreground, scheme.background)

        return when {
            contrastRatio >= ColorMappingConfig.HIGH_CONTRAST_THRESHOLD -> ContrastLevel.HIGH
            contrastRatio <= ColorMappingConfig.LOW_CONTRAST_THRESHOLD -> ContrastLevel.LOW
            else -> ContrastLevel.NORMAL
        }
    }

    /**
     * Analyzes the palette for variety and distribution.
     */
    private fun analyzePalette(scheme: WindowsTerminalColorScheme): PaletteAnalysis {
        val colors = scheme.getAllColors()
        val hues = colors.map { ColorUtils.extractHue(it) }
        val saturations = colors.map { ColorUtils.extractSaturation(it) }

        // Count unique hues (group by 30-degree segments)
        val uniqueHueSegments = hues.map { (it / 30).toInt() }.toSet().size

        val isLimitedPalette = uniqueHueSegments <= ColorMappingConfig.LIMITED_PALETTE_HUE_COUNT

        // Check brightness uniformity
        val luminanceClasses = colors.map { classifyColor(it).luminanceClass }
        val mostCommonClass = luminanceClasses.groupingBy { it }.eachCount().maxByOrNull { it.value }
        val brightnessUniformity = (mostCommonClass?.value ?: 0) / colors.size.toDouble()

        val isUniformBrightness = brightnessUniformity >= ColorMappingConfig.BRIGHTNESS_UNIFORMITY_THRESHOLD

        return PaletteAnalysis(
            uniqueHueCount = uniqueHueSegments,
            isLimitedPalette = isLimitedPalette,
            isUniformBrightness = isUniformBrightness,
            averageSaturation = saturations.average()
        )
    }

    /**
     * Gets a color value from the scheme by property name.
     */
    private fun getColorFromScheme(scheme: WindowsTerminalColorScheme, property: String): String {
        return when (property) {
            "background" -> scheme.background
            "foreground" -> scheme.foreground
            "black" -> scheme.black
            "red" -> scheme.red
            "green" -> scheme.green
            "yellow" -> scheme.yellow
            "blue" -> scheme.blue
            "purple" -> scheme.purple
            "cyan" -> scheme.cyan
            "white" -> scheme.white
            "brightBlack" -> scheme.brightBlack
            "brightRed" -> scheme.brightRed
            "brightGreen" -> scheme.brightGreen
            "brightYellow" -> scheme.brightYellow
            "brightBlue" -> scheme.brightBlue
            "brightPurple" -> scheme.brightPurple
            "brightCyan" -> scheme.brightCyan
            "brightWhite" -> scheme.brightWhite
            else -> scheme.foreground  // Fallback
        }
    }

    /**
     * Determines appropriate font style for monochrome palettes.
     */
    private fun determineFontStyleForMonochrome(attributeName: String, rule: SyntaxRule): FontStyle {
        // If rule already specifies a font style, use it
        if (rule.fontStyle != null) {
            return rule.fontStyle
        }

        // Apply font styles based on semantic meaning
        return when {
            attributeName.contains("KEYWORD", ignoreCase = true) -> FontStyle.BOLD
            attributeName.contains("COMMENT", ignoreCase = true) -> FontStyle.ITALIC
            attributeName.contains("STRING", ignoreCase = true) -> FontStyle.REGULAR
            attributeName.contains("FUNCTION", ignoreCase = true) -> FontStyle.BOLD
            attributeName.contains("CLASS", ignoreCase = true) -> FontStyle.BOLD
            attributeName.contains("ERROR", ignoreCase = true) -> FontStyle.BOLD
            attributeName.contains("WARNING", ignoreCase = true) -> FontStyle.ITALIC
            attributeName.contains("DOC", ignoreCase = true) -> FontStyle.ITALIC
            else -> FontStyle.REGULAR
        }
    }

    /**
     * Adjusts color for low contrast situations.
     */
    private fun adjustForLowContrast(color: String, background: String): String {
        val currentContrast = ColorUtils.calculateContrastRatio(color, background)

        // If contrast is too low, increase it
        if (currentContrast < 4.5) {  // WCAG AA minimum
            val bgLuminance = ColorUtils.calculateLuminance(background)

            // If background is dark, lighten the color; if light, darken it
            return if (bgLuminance < 127.5) {
                ColorUtils.lighten(color, 0.3)
            } else {
                ColorUtils.darken(color, 0.3)
            }
        }

        return color
    }

    /**
     * Adjusts color for high contrast situations (reduce intensity).
     */
    private fun adjustForHighContrast(color: String, background: String): String {
        val currentContrast = ColorUtils.calculateContrastRatio(color, background)

        // If contrast is very high, slightly reduce it for comfort
        if (currentContrast > 15.0) {
            val bgLuminance = ColorUtils.calculateLuminance(background)

            // Bring the color slightly closer to the background
            return if (bgLuminance < 127.5) {
                ColorUtils.darken(color, 0.1)
            } else {
                ColorUtils.lighten(color, 0.1)
            }
        }

        return color
    }

    /**
     * Adds common IntelliJ attributes that may not be explicitly defined in rules.
     */
    private fun addCommonAttributes(
        result: MutableMap<String, SyntaxColor>,
        scheme: WindowsTerminalColorScheme,
        colorClassifications: Map<String, ColorClassification>,
        isMonochrome: Boolean
    ) {
        // If DEFAULT_TEXT not set, use foreground
        if (!result.containsKey("DEFAULT_TEXT")) {
            result["DEFAULT_TEXT"] = SyntaxColor(
                color = scheme.foreground,
                fontStyle = FontStyle.REGULAR
            )
        }

        // If LINE_NUMBER not set, use dimmed foreground
        if (!result.containsKey("LINE_NUMBER")) {
            result["LINE_NUMBER"] = SyntaxColor(
                color = ColorUtils.darken(scheme.foreground, 0.4),
                fontStyle = FontStyle.REGULAR
            )
        }

        // If BACKGROUND not set, use background
        if (!result.containsKey("BACKGROUND")) {
            result["BACKGROUND"] = SyntaxColor(
                color = scheme.background,
                fontStyle = FontStyle.REGULAR
            )
        }

        // If CARET not set, use foreground or cursor color
        if (!result.containsKey("CARET")) {
            result["CARET"] = SyntaxColor(
                color = scheme.cursorColor ?: scheme.foreground,
                fontStyle = FontStyle.REGULAR
            )
        }

        // If SELECTION_BACKGROUND not set, use selection or blended color
        if (!result.containsKey("SELECTION_BACKGROUND")) {
            result["SELECTION_BACKGROUND"] = SyntaxColor(
                color = scheme.selectionBackground ?: ColorUtils.blend(scheme.background, scheme.foreground, 0.2),
                fontStyle = FontStyle.REGULAR
            )
        }
    }
}

// ========== DATA CLASSES ==========

/**
 * Represents a color classification with luminance and hue information.
 */
data class ColorClassification(
    val luminance: Double,
    val luminanceClass: LuminanceClass,
    val hue: Double,
    val saturation: Double,
    val isGrayscale: Boolean
)

/**
 * Represents a syntax color with optional font style and effects.
 */
data class SyntaxColor(
    val color: String,
    val fontStyle: FontStyle = FontStyle.REGULAR,
    val effectType: EffectType? = null
)

/**
 * Contrast level classification.
 */
enum class ContrastLevel {
    LOW,      // Below WCAG AA
    NORMAL,   // Between AA and AAA
    HIGH      // WCAG AAA or higher
}

/**
 * Palette analysis results.
 */
data class PaletteAnalysis(
    val uniqueHueCount: Int,
    val isLimitedPalette: Boolean,
    val isUniformBrightness: Boolean,
    val averageSaturation: Double
)
