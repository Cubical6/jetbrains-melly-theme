package mapping

import colorschemes.WindowsTerminalColorScheme

/**
 * Example usage demonstrating the ColorPaletteExpander capabilities.
 * This is not a test file, but rather documentation showing how to use the expander.
 */
object ColorPaletteExpanderExampleUsage {

    /**
     * Example: Expanding a One Dark theme
     */
    fun exampleOneDarkExpansion() {
        val oneDark = WindowsTerminalColorScheme(
            name = "One Dark",
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

        // Expand the palette
        val expandedPalette = ColorPaletteExpander.expandPalette(oneDark)

        println("Original colors: ${oneDark.toColorPalette().size}")
        println("Expanded colors: ${expandedPalette.size}")
        println("\nSample expanded colors:")
        println("  Background lighter: ${expandedPalette["bg_lighter"]}")
        println("  Foreground subtle: ${expandedPalette["fg_subtle"]}")
        println("  Hover state: ${expandedPalette["state_hover"]}")
        println("  Accent: ${expandedPalette["accent"]}")
    }

    /**
     * Example: Creating color variations
     */
    fun exampleColorVariations() {
        val baseBlue = "#61afef"

        // Create tints (lighter variations)
        val tints = ColorPaletteExpander.generateTints(baseBlue, 3)
        println("Tints of $baseBlue:")
        tints.forEachIndexed { i, tint -> println("  Tint ${i + 1}: $tint") }

        // Create shades (darker variations)
        val shades = ColorPaletteExpander.generateShades(baseBlue, 3)
        println("\nShades of $baseBlue:")
        shades.forEachIndexed { i, shade -> println("  Shade ${i + 1}: $shade") }

        // Create saturation variants
        val satVariants = ColorPaletteExpander.generateSaturationVariants(baseBlue, 2)
        println("\nSaturation variants of $baseBlue:")
        satVariants.forEach { (name, color) -> println("  $name: $color") }
    }

    /**
     * Example: Creating color gradients
     */
    fun exampleColorGradients() {
        val dark = "#282c34"
        val light = "#abb2bf"

        // Create a smooth gradient
        val gradient = ColorPaletteExpander.interpolateColors(dark, light, 7)
        println("Gradient from $dark to $light:")
        gradient.forEachIndexed { i, color ->
            println("  Step $i: $color")
        }
    }

    /**
     * Example: Color harmony (complementary, analogous, triadic)
     */
    fun exampleColorHarmony() {
        val baseColor = "#61afef"

        // Complementary color (opposite on color wheel)
        val complementary = ColorPaletteExpander.generateComplementaryColor(baseColor)
        println("Base: $baseColor")
        println("Complementary: $complementary")

        // Analogous colors (adjacent on color wheel)
        val (analogous1, analogous2) = ColorPaletteExpander.generateAnalogousColors(baseColor)
        println("Analogous 1: $analogous1")
        println("Analogous 2: $analogous2")

        // Triadic colors (120 degrees apart)
        val (triadic1, triadic2) = ColorPaletteExpander.generateTriadicColors(baseColor)
        println("Triadic 1: $triadic1")
        println("Triadic 2: $triadic2")

        // Split complementary colors
        val (split1, split2) = ColorPaletteExpander.generateSplitComplementaryColors(baseColor)
        println("Split complementary 1: $split1")
        println("Split complementary 2: $split2")
    }

    /**
     * Example: Monochromatic palette
     */
    fun exampleMonochromaticPalette() {
        val baseColor = "#61afef"
        val palette = ColorPaletteExpander.generateMonochromaticPalette(baseColor, 7)

        println("Monochromatic palette from $baseColor:")
        palette.forEachIndexed { i, color ->
            println("  Variant ${i + 1}: $color")
        }
    }

    /**
     * Example: Accessibility - adjusting colors for contrast
     */
    fun exampleAccessibilityAdjustments() {
        val background = "#282c34"
        val foreground = "#61afef"

        // Adjust foreground to meet WCAG AA standard (4.5:1)
        val accessibleForeground = ColorPaletteExpander.adjustToContrastRatio(
            foreground,
            background,
            targetContrast = 4.5
        )
        println("Original foreground: $foreground")
        println("Accessible foreground (4.5:1): $accessibleForeground")

        // Adjust to meet WCAG AAA standard (7:1)
        val highContrastForeground = ColorPaletteExpander.adjustToContrastRatio(
            foreground,
            background,
            targetContrast = 7.0
        )
        println("High contrast foreground (7:1): $highContrastForeground")
    }

    /**
     * Example: Adjusting colors to specific luminance
     */
    fun exampleLuminanceAdjustment() {
        val color = "#61afef"

        // Adjust to different luminance levels
        val dark = ColorPaletteExpander.adjustToLuminance(color, 50.0)
        val medium = ColorPaletteExpander.adjustToLuminance(color, 100.0)
        val bright = ColorPaletteExpander.adjustToLuminance(color, 180.0)

        println("Original: $color")
        println("Dark (luminance 50): $dark")
        println("Medium (luminance 100): $medium")
        println("Bright (luminance 180): $bright")
    }

    /**
     * Example: Complete workflow - from Windows Terminal theme to full IntelliJ palette
     */
    fun exampleCompleteWorkflow() {
        // 1. Start with a Windows Terminal color scheme
        val terminalTheme = WindowsTerminalColorScheme(
            name = "Dracula",
            background = "#282a36",
            foreground = "#f8f8f2",
            black = "#000000",
            red = "#ff5555",
            green = "#50fa7b",
            yellow = "#f1fa8c",
            blue = "#bd93f9",
            purple = "#ff79c6",
            cyan = "#8be9fd",
            white = "#bfbfbf",
            brightBlack = "#4d4d4d",
            brightRed = "#ff6e67",
            brightGreen = "#5af78e",
            brightYellow = "#f4f99d",
            brightBlue = "#caa9fa",
            brightPurple = "#ff92d0",
            brightCyan = "#9aedfe",
            brightWhite = "#e6e6e6"
        )

        // 2. Expand to full palette
        val fullPalette = ColorPaletteExpander.expandPalette(terminalTheme)

        // 3. Use expanded colors for IntelliJ theme
        println("\n=== Complete IntelliJ Theme Palette ===")
        println("Total colors: ${fullPalette.size}")
        println("\n--- Background Variants ---")
        println("  Main background: ${fullPalette["wt_background"]}")
        println("  Panel background: ${fullPalette["bg_panel"]}")
        println("  Sidebar: ${fullPalette["bg_sidebar"]}")
        println("  Tooltip: ${fullPalette["bg_tooltip"]}")

        println("\n--- Foreground Variants ---")
        println("  Normal text: ${fullPalette["fg_normal"]}")
        println("  Subtle text: ${fullPalette["fg_subtle"]}")
        println("  Muted text: ${fullPalette["fg_muted"]}")
        println("  Disabled text: ${fullPalette["fg_disabled"]}")

        println("\n--- Interactive States ---")
        println("  Hover: ${fullPalette["state_hover"]}")
        println("  Selected: ${fullPalette["state_selected"]}")
        println("  Pressed: ${fullPalette["state_pressed"]}")

        println("\n--- Semantic Colors ---")
        println("  Info: ${fullPalette["semantic_info"]}")
        println("  Success: ${fullPalette["semantic_success"]}")
        println("  Warning: ${fullPalette["semantic_warning"]}")
        println("  Error: ${fullPalette["semantic_error"]}")

        println("\n--- Editor Colors ---")
        println("  Gutter: ${fullPalette["editor_gutter"]}")
        println("  Line numbers: ${fullPalette["editor_line_number"]}")
        println("  Current line: ${fullPalette["editor_current_line"]}")
        println("  Indent guide: ${fullPalette["editor_indent_guide"]}")

        println("\n--- Borders ---")
        println("  Subtle: ${fullPalette["border_subtle"]}")
        println("  Normal: ${fullPalette["border_normal"]}")
        println("  Strong: ${fullPalette["border_strong"]}")

        println("\n--- Accent Colors ---")
        println("  Main accent: ${fullPalette["accent"]}")
        println("  Light accent: ${fullPalette["accent_light"]}")
        println("  Dark accent: ${fullPalette["accent_dark"]}")

        // 4. Generate additional custom colors as needed
        val customBlue = fullPalette["wt_blue"]!!
        val blueVariants = ColorPaletteExpander.generateTints(customBlue, 5)
        println("\n--- Custom Blue Variants ---")
        blueVariants.forEachIndexed { i, color ->
            println("  Blue tint ${i + 1}: $color")
        }
    }
}
