package colorschemes

import utils.ColorUtils

/**
 * Data class representing a Windows Terminal color scheme.
 *
 * Conforms to the Windows Terminal color scheme JSON format:
 * https://learn.microsoft.com/en-us/windows/terminal/customize-settings/color-schemes
 *
 * @property name The unique name of the color scheme
 * @property background Terminal background color (#RRGGBB)
 * @property foreground Default text color (#RRGGBB)
 * @property cursorColor Cursor color (optional, defaults to foreground)
 * @property selectionBackground Selection highlight color (optional, defaults to blend)
 * @property black ANSI color 0 (black)
 * @property red ANSI color 1 (red)
 * @property green ANSI color 2 (green)
 * @property yellow ANSI color 3 (yellow)
 * @property blue ANSI color 4 (blue)
 * @property purple ANSI color 5 (magenta/purple)
 * @property cyan ANSI color 6 (cyan)
 * @property white ANSI color 7 (white/gray)
 * @property brightBlack ANSI color 8 (bright black/dark gray)
 * @property brightRed ANSI color 9 (bright red)
 * @property brightGreen ANSI color 10 (bright green)
 * @property brightYellow ANSI color 11 (bright yellow)
 * @property brightBlue ANSI color 12 (bright blue)
 * @property brightPurple ANSI color 13 (bright magenta/purple)
 * @property brightCyan ANSI color 14 (bright cyan)
 * @property brightWhite ANSI color 15 (bright white)
 */
data class WindowsTerminalColorScheme(
    // Required properties
    val name: String,
    val background: String,
    val foreground: String,

    // ANSI colors (8 normal colors) - required
    val black: String,
    val red: String,
    val green: String,
    val yellow: String,
    val blue: String,
    val purple: String,
    val cyan: String,
    val white: String,

    // ANSI bright colors (8 bright colors) - required
    val brightBlack: String,
    val brightRed: String,
    val brightGreen: String,
    val brightYellow: String,
    val brightBlue: String,
    val brightPurple: String,
    val brightCyan: String,
    val brightWhite: String,

    // Optional properties
    val cursorColor: String? = null,
    val selectionBackground: String? = null
) {
    companion object {
        private val HEX_COLOR_REGEX = Regex("^#[0-9A-Fa-f]{6}$")

        /**
         * List of all required property names for validation
         */
        val REQUIRED_PROPERTIES = listOf(
            "name", "background", "foreground",
            "black", "red", "green", "yellow", "blue", "purple", "cyan", "white",
            "brightBlack", "brightRed", "brightGreen", "brightYellow",
            "brightBlue", "brightPurple", "brightCyan", "brightWhite"
        )

        /**
         * List of optional property names
         */
        val OPTIONAL_PROPERTIES = listOf("cursorColor", "selectionBackground")
    }

    /**
     * Validates that a color string is in valid hex format (#RRGGBB)
     */
    fun isValidHexColor(color: String): Boolean {
        return HEX_COLOR_REGEX.matches(color)
    }

    /**
     * Validates the entire color scheme and returns a list of validation errors.
     * Empty list means the scheme is valid.
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        // Validate name
        if (name.isBlank()) {
            errors.add("Name cannot be blank")
        }

        // Validate all required colors
        val colorProperties = mapOf(
            "background" to background,
            "foreground" to foreground,
            "black" to black,
            "red" to red,
            "green" to green,
            "yellow" to yellow,
            "blue" to blue,
            "purple" to purple,
            "cyan" to cyan,
            "white" to white,
            "brightBlack" to brightBlack,
            "brightRed" to brightRed,
            "brightGreen" to brightGreen,
            "brightYellow" to brightYellow,
            "brightBlue" to brightBlue,
            "brightPurple" to brightPurple,
            "brightCyan" to brightCyan,
            "brightWhite" to brightWhite
        )

        colorProperties.forEach { (propertyName, colorValue) ->
            if (!isValidHexColor(colorValue)) {
                errors.add("Invalid hex color for $propertyName: $colorValue (expected #RRGGBB format)")
            }
        }

        // Validate optional colors if present
        cursorColor?.let { color ->
            if (!isValidHexColor(color)) {
                errors.add("Invalid hex color for cursorColor: $color (expected #RRGGBB format)")
            }
        }

        selectionBackground?.let { color ->
            if (!isValidHexColor(color)) {
                errors.add("Invalid hex color for selectionBackground: $color (expected #RRGGBB format)")
            }
        }

        return errors
    }

    /**
     * Converts the color scheme to an enhanced ColorPalette with 50 derived colors.
     *
     * This method generates a comprehensive color palette from the base Windows Terminal
     * color scheme, including:
     * - Original 12 derived colors (surface variations, UI helpers, semantic colors)
     * - 38 new derived colors for advanced theming
     *
     * All colors are calculated automatically using ColorUtils methods to ensure
     * visual consistency and proper contrast ratios.
     */
    fun toColorPalette(): ColorPalette {
        // Existing 12 colors (keep current implementation)
        val surface = ColorUtils.lighten(background, 0.05)
        val surfaceLight = ColorUtils.lighten(background, 0.10)
        val surfaceLighter = ColorUtils.lighten(background, 0.15)
        val lineNumbers = ColorUtils.blend(background, foreground, 0.30)
        val guideColor = ColorUtils.blend(background, foreground, 0.15)
        val dividerColor = ColorUtils.blend(background, foreground, 0.25)
        val mutedForeground = ColorUtils.blend(background, foreground, 0.60)
        val errorBackground = ColorUtils.blend(background, red, 0.20)
        val warningBackground = ColorUtils.blend(background, yellow, 0.20)
        val infoBackground = ColorUtils.blend(background, blue, 0.20)
        val uiBorderColor = ColorUtils.createVisibleBorderColor(background, minContrast = 3.0)
        val uiComponentBackground = ColorUtils.createVisibleComponentBackground(background, minContrast = 1.5)

        // NEW: Surface variations (4)
        val surfaceDark = ColorUtils.darken(background, 0.05)
        val surfaceDarker = ColorUtils.darken(background, 0.10)
        val surfaceDarkest = ColorUtils.darken(background, 0.15)
        val surfaceSubtle = ColorUtils.lighten(background, 0.03)

        // NEW: Selection variations (3)
        val selectionBg = selectionBackground ?: ColorUtils.blend(background, brightBlue, 0.30)
        val selectionInactive = ColorUtils.darken(selectionBg, 0.40)
        val selectionLight = ColorUtils.lighten(selectionBg, 0.20)
        val selectionBorder = ColorUtils.lighten(selectionBg, 0.30)

        // NEW: Focus/Accent colors (5)
        val accentPrimary = brightBlue
        val accentSecondary = brightPurple
        val accentTertiary = brightCyan
        val focusColor = ColorUtils.lighten(accentSecondary, 0.15)
        val focusBorder = ColorUtils.darken(accentSecondary, 0.15)

        // NEW: Button/Component colors (6)
        val buttonBorder = ColorUtils.lighten(surface, 0.10)
        val buttonBorderFocused = accentPrimary
        val popupBackground = ColorUtils.blend(background, purple, 0.15)
        val popupBorder = ColorUtils.lighten(popupBackground, 0.20)
        val headerBackground = ColorUtils.blend(surface, purple, 0.10)
        val hoverBackground = ColorUtils.lighten(surface, 0.08)

        // NEW: Syntax-specific derived colors (6)
        val instanceField = ColorUtils.blend(purple, red, 0.40) // Pink
        val todoColor = ColorUtils.blend(brightCyan, green, 0.50) // Teal
        val deprecatedColor = ColorUtils.blend(foreground, background, 0.50)
        val stringEscape = ColorUtils.lighten(green, 0.15)
        val numberAlt = ColorUtils.blend(blue, cyan, 0.30)
        val constantColor = purple

        // NEW: Progress/Status colors (6)
        val gradient = ColorUtils.generateColorGradient(
            ColorUtils.blend(background, purple, 0.30),
            ColorUtils.blend(background, cyan, 0.30),
            1
        )
        val progressStart = gradient[0]
        val progressMid = gradient[1]
        val progressEnd = gradient[2]
        val memoryIndicator = ColorUtils.blend(background, purple, 0.40)
        val passedColor = green
        val failedColor = red

        // NEW: Additional UI colors (8)
        val breadcrumbCurrent = foreground
        val breadcrumbHover = ColorUtils.lighten(foreground, 0.10)
        val separatorColor = dividerColor
        val disabledText = brightBlack
        val counterBackground = surfaceDark
        val tooltipBackground = ColorUtils.lighten(popupBackground, 0.05)
        val linkHover = ColorUtils.lighten(blue, 0.15)
        val iconColor = mutedForeground

        // Island styling (modern floating tool windows)
        val islandBorderColor = ColorUtils.darken(background, 0.15)

        // Editor tab underline styling
        val underlinedTabBorderColor = selectionBg
        val underlinedTabBackground = ColorUtils.blend(background, selectionBg, 0.3)
        val inactiveUnderlinedTabBorderColor = ColorUtils.desaturate(selectionBg, 0.5)
        val inactiveUnderlinedTabBackground = ColorUtils.blend(background, surface, 0.5)

        // Icon colors - Actions category
        val actionsRed = ColorUtils.blend(background, "#DB5860", 0.7)
        val actionsYellow = ColorUtils.blend(background, "#EDA200", 0.7)
        val actionsGreen = ColorUtils.blend(background, "#59A869", 0.7)
        val actionsBlue = selectionBg
        val actionsGrey = ColorUtils.desaturate(foreground, 0.3)

        // Icon colors - Objects category
        val objectsGreen = actionsGreen
        val objectsYellow = actionsYellow
        val objectsBlue = actionsBlue
        val objectsGrey = actionsGrey
        val objectsRed = actionsRed

        return ColorPalette(
            // Existing 12
            surface = surface,
            surfaceLight = surfaceLight,
            surfaceLighter = surfaceLighter,
            lineNumbers = lineNumbers,
            guideColor = guideColor,
            dividerColor = dividerColor,
            mutedForeground = mutedForeground,
            errorBackground = errorBackground,
            warningBackground = warningBackground,
            infoBackground = infoBackground,
            uiBorderColor = uiBorderColor,
            uiComponentBackground = uiComponentBackground,

            // NEW: Surface variations (4)
            surfaceDark = surfaceDark,
            surfaceDarker = surfaceDarker,
            surfaceDarkest = surfaceDarkest,
            surfaceSubtle = surfaceSubtle,

            // NEW: Selection variations (3)
            selectionInactive = selectionInactive,
            selectionLight = selectionLight,
            selectionBorder = selectionBorder,

            // NEW: Focus/Accent colors (5)
            focusColor = focusColor,
            focusBorder = focusBorder,
            accentPrimary = accentPrimary,
            accentSecondary = accentSecondary,
            accentTertiary = accentTertiary,

            // NEW: Button/Component colors (6)
            buttonBorder = buttonBorder,
            buttonBorderFocused = buttonBorderFocused,
            popupBackground = popupBackground,
            popupBorder = popupBorder,
            headerBackground = headerBackground,
            hoverBackground = hoverBackground,

            // NEW: Syntax-specific (6)
            instanceField = instanceField,
            todoColor = todoColor,
            deprecatedColor = deprecatedColor,
            stringEscape = stringEscape,
            numberAlt = numberAlt,
            constantColor = constantColor,

            // NEW: Progress/Status (6)
            progressStart = progressStart,
            progressMid = progressMid,
            progressEnd = progressEnd,
            memoryIndicator = memoryIndicator,
            passedColor = passedColor,
            failedColor = failedColor,

            // NEW: Additional UI (8)
            breadcrumbCurrent = breadcrumbCurrent,
            breadcrumbHover = breadcrumbHover,
            separatorColor = separatorColor,
            disabledText = disabledText,
            counterBackground = counterBackground,
            tooltipBackground = tooltipBackground,
            linkHover = linkHover,
            iconColor = iconColor,

            // Island
            islandBorderColor = islandBorderColor,

            // Editor tab underline styling
            underlinedTabBorderColor = underlinedTabBorderColor,
            underlinedTabBackground = underlinedTabBackground,
            inactiveUnderlinedTabBorderColor = inactiveUnderlinedTabBorderColor,
            inactiveUnderlinedTabBackground = inactiveUnderlinedTabBackground,

            // Icon colors
            actionsRed = actionsRed,
            actionsYellow = actionsYellow,
            actionsGreen = actionsGreen,
            actionsBlue = actionsBlue,
            actionsGrey = actionsGrey,
            objectsGreen = objectsGreen,
            objectsYellow = objectsYellow,
            objectsBlue = objectsBlue,
            objectsGrey = objectsGrey,
            objectsRed = objectsRed
        )
    }

    /**
     * Returns all colors (required and optional) as a list of hex strings.
     * Useful for color analysis and classification.
     */
    fun getAllColors(): List<String> {
        return listOf(
            background, foreground,
            black, red, green, yellow, blue, purple, cyan, white,
            brightBlack, brightRed, brightGreen, brightYellow,
            brightBlue, brightPurple, brightCyan, brightWhite
        )
    }

    /**
     * Returns ANSI colors only (16 colors) as a map of ANSI code to hex color.
     */
    fun getAnsiColors(): Map<Int, String> {
        return mapOf(
            30 to black,
            31 to red,
            32 to green,
            33 to yellow,
            34 to blue,
            35 to purple,
            36 to cyan,
            37 to white,
            90 to brightBlack,
            91 to brightRed,
            92 to brightGreen,
            93 to brightYellow,
            94 to brightBlue,
            95 to brightPurple,
            96 to brightCyan,
            97 to brightWhite
        )
    }
}

/**
 * Enhanced color palette with 65 derived colors for comprehensive theme generation.
 *
 * Provides a rich set of colors derived from the base Windows Terminal color scheme,
 * including surface variations, selection states, focus colors, UI components,
 * syntax-specific colors, progress indicators, and additional UI elements.
 *
 * Total: 12 existing + 53 new = 65 derived colors
 */
data class ColorPalette(
    // Existing 12 colors
    val surface: String,
    val surfaceLight: String,
    val surfaceLighter: String,
    val lineNumbers: String,
    val guideColor: String,
    val dividerColor: String,
    val mutedForeground: String,
    val errorBackground: String,
    val warningBackground: String,
    val infoBackground: String,
    val uiBorderColor: String,
    val uiComponentBackground: String,

    // NEW: Surface variations (4 new)
    val surfaceDark: String,        // Darken bg 5%
    val surfaceDarker: String,      // Darken bg 10%
    val surfaceDarkest: String,     // Darken bg 15%
    val surfaceSubtle: String,      // Lighten bg 3%

    // NEW: Selection variations (3 new)
    val selectionInactive: String,   // Dim selection 40%
    val selectionLight: String,      // Lighten selection 20%
    val selectionBorder: String,     // Lighter selection for borders

    // NEW: Focus/Accent colors (5 new)
    val focusColor: String,          // Brighten primary accent
    val focusBorder: String,         // Dim accent for borders
    val accentPrimary: String,       // Use brightBlue
    val accentSecondary: String,     // Use brightPurple
    val accentTertiary: String,      // Use brightCyan

    // NEW: Button/Component colors (6 new)
    val buttonBorder: String,        // Subtle border from surface
    val buttonBorderFocused: String, // Accent border for focused state
    val popupBackground: String,     // Blend purple + background
    val popupBorder: String,         // Lighter than popup bg
    val headerBackground: String,    // Custom mid-tone
    val hoverBackground: String,     // Light hover state

    // NEW: Syntax-specific derived colors (6 new)
    val instanceField: String,       // Blend purple + red (pink)
    val todoColor: String,           // Blend cyan + green (teal)
    val deprecatedColor: String,     // Dim foreground
    val stringEscape: String,        // Brighten green
    val numberAlt: String,           // Alternative number color
    val constantColor: String,       // Constant values

    // NEW: Progress/Status colors (6 new)
    val progressStart: String,       // Gradient start
    val progressMid: String,         // Gradient middle
    val progressEnd: String,         // Gradient end
    val memoryIndicator: String,     // Memory usage color
    val passedColor: String,         // Test passed
    val failedColor: String,         // Test failed

    // NEW: Additional UI colors (8 new)
    val breadcrumbCurrent: String,   // Current breadcrumb
    val breadcrumbHover: String,     // Breadcrumb hover
    val separatorColor: String,      // Separator lines
    val disabledText: String,        // Disabled foreground
    val counterBackground: String,   // Counter badges
    val tooltipBackground: String,   // Tooltip bg
    val linkHover: String,           // Link hover state
    val iconColor: String,           // Default icon color

    // Island (1 new)
    val islandBorderColor: String,   // Border color for floating Islands

    // Editor tab underline styling (4 new)
    val underlinedTabBorderColor: String,
    val underlinedTabBackground: String,
    val inactiveUnderlinedTabBorderColor: String,
    val inactiveUnderlinedTabBackground: String,

    // Icon colors - Actions category (5 new)
    val actionsRed: String,
    val actionsYellow: String,
    val actionsGreen: String,
    val actionsBlue: String,
    val actionsGrey: String,

    // Icon colors - Objects category (5 new)
    val objectsGreen: String,
    val objectsYellow: String,
    val objectsBlue: String,
    val objectsGrey: String,
    val objectsRed: String
) {
    /**
     * Converts the ColorPalette to a Map for backward compatibility with template processors.
     * All keys are prefixed with "wt_" to match the original toColorPalette() format.
     *
     * Note: This only contains the derived colors. Base ANSI colors (wt_background, wt_foreground, etc.)
     * must be added separately by the calling code using WindowsTerminalColorScheme properties.
     */
    fun toMap(): Map<String, String> {
        return mapOf(
            // Existing 12 colors
            "wt_surface" to surface,
            "wt_surface_light" to surfaceLight,
            "wt_surface_lighter" to surfaceLighter,
            "wt_line_numbers" to lineNumbers,
            "wt_guide_color" to guideColor,
            "wt_divider_color" to dividerColor,
            "wt_muted_foreground" to mutedForeground,
            "wt_error_background" to errorBackground,
            "wt_warning_background" to warningBackground,
            "wt_info_background" to infoBackground,
            "wt_uiBorderColor" to uiBorderColor,
            "wt_uiComponentBackground" to uiComponentBackground,
            // Snake_case aliases for consistency
            "wt_ui_border_color" to uiBorderColor,
            "wt_ui_component_background" to uiComponentBackground,

            // NEW: Surface variations (4)
            "wt_surface_dark" to surfaceDark,
            "wt_surface_darker" to surfaceDarker,
            "wt_surface_darkest" to surfaceDarkest,
            "wt_surface_subtle" to surfaceSubtle,

            // NEW: Selection variations (3)
            "wt_selection_inactive" to selectionInactive,
            "wt_selection_light" to selectionLight,
            "wt_selection_border" to selectionBorder,

            // NEW: Focus/Accent colors (5)
            "wt_focus_color" to focusColor,
            "wt_focus_border" to focusBorder,
            "wt_accent_primary" to accentPrimary,
            "wt_accent_secondary" to accentSecondary,
            "wt_accent_tertiary" to accentTertiary,

            // NEW: Button/Component colors (6)
            "wt_button_border" to buttonBorder,
            "wt_button_border_focused" to buttonBorderFocused,
            "wt_popup_background" to popupBackground,
            "wt_popup_border" to popupBorder,
            "wt_header_background" to headerBackground,
            "wt_hover_background" to hoverBackground,

            // NEW: Syntax-specific (6)
            "wt_instance_field" to instanceField,
            "wt_todo_color" to todoColor,
            "wt_deprecated_color" to deprecatedColor,
            "wt_string_escape" to stringEscape,
            "wt_number_alt" to numberAlt,
            "wt_constant_color" to constantColor,

            // NEW: Progress/Status (6)
            "wt_progress_start" to progressStart,
            "wt_progress_mid" to progressMid,
            "wt_progress_end" to progressEnd,
            "wt_memory_indicator" to memoryIndicator,
            "wt_passed_color" to passedColor,
            "wt_failed_color" to failedColor,

            // NEW: Additional UI (8)
            "wt_breadcrumb_current" to breadcrumbCurrent,
            "wt_breadcrumb_hover" to breadcrumbHover,
            "wt_separator_color" to separatorColor,
            "wt_disabled_text" to disabledText,
            "wt_counter_background" to counterBackground,
            "wt_tooltip_background" to tooltipBackground,
            "wt_link_hover" to linkHover,
            "wt_icon_color" to iconColor,

            // Island (1)
            "wt_island_border" to islandBorderColor,

            // Editor tab underline styling (4)
            "wt_underlined_tab_border_color" to underlinedTabBorderColor,
            "wt_underlined_tab_background" to underlinedTabBackground,
            "wt_inactive_underlined_tab_border_color" to inactiveUnderlinedTabBorderColor,
            "wt_inactive_underlined_tab_background" to inactiveUnderlinedTabBackground,

            // Icon colors - Actions (5)
            "Actions.Red" to actionsRed,
            "Actions.Yellow" to actionsYellow,
            "Actions.Green" to actionsGreen,
            "Actions.Blue" to actionsBlue,
            "Actions.Grey" to actionsGrey,

            // Icon colors - Objects (5)
            "Objects.Green" to objectsGreen,
            "Objects.Yellow" to objectsYellow,
            "Objects.Blue" to objectsBlue,
            "Objects.Grey" to objectsGrey,
            "Objects.Red" to objectsRed
        )
    }
}

/**
 * Extension function to convert WindowsTerminalColorScheme to a complete Map including base colors.
 * This provides backward compatibility with code that expects a Map<String, String>.
 */
fun WindowsTerminalColorScheme.toColorPaletteMap(): Map<String, String> {
    return buildMap {
        // Required colors
        put("wt_background", background)
        put("wt_foreground", foreground)
        put("wt_black", black)
        put("wt_red", red)
        put("wt_green", green)
        put("wt_yellow", yellow)
        put("wt_blue", blue)
        put("wt_purple", purple)
        put("wt_cyan", cyan)
        put("wt_white", white)
        put("wt_brightBlack", brightBlack)
        put("wt_brightRed", brightRed)
        put("wt_brightGreen", brightGreen)
        put("wt_brightYellow", brightYellow)
        put("wt_brightBlue", brightBlue)
        put("wt_brightPurple", brightPurple)
        put("wt_brightCyan", brightCyan)
        put("wt_brightWhite", brightWhite)

        // Optional colors with fallbacks
        put("wt_cursorColor", cursorColor ?: foreground)
        put("wt_selectionBackground", selectionBackground ?: ColorUtils.blend(background, foreground, 0.2))

        // Add all derived colors from ColorPalette
        putAll(toColorPalette().toMap())

        // Template compatibility aliases (snake_case for bright colors)
        put("wt_bright_black", brightBlack)
        put("wt_bright_red", brightRed)
        put("wt_bright_green", brightGreen)
        put("wt_bright_yellow", brightYellow)
        put("wt_bright_blue", brightBlue)
        put("wt_bright_cyan", brightCyan)
        put("wt_bright_white", brightWhite)

        // Magenta/Purple aliases (template uses 'magenta', WT uses 'purple')
        put("wt_magenta", purple)
        put("wt_bright_magenta", brightPurple)
    }
}
