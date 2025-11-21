package generators

import colorschemes.WindowsTerminalColorScheme
import java.nio.file.Paths

/**
 * Example usage of UIThemeGenerator.
 *
 * This file demonstrates how to use the UIThemeGenerator to convert
 * Windows Terminal color schemes to IntelliJ UI themes.
 */
object UIThemeGeneratorExample {

    @JvmStatic
    fun main(args: Array<String>) {
        val generator = UIThemeGenerator()

        // Example 1: Generate a dark theme
        val darkScheme = WindowsTerminalColorScheme(
            name = "One Dark Pro",
            background = "#282c34",
            foreground = "#abb2bf",
            black = "#282c34",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#e5c07b",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            brightBlack = "#5c6370",
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff",
            cursorColor = "#528bff",
            selectionBackground = "#3e4451"
        )

        // Analyze the color scheme first
        println("=== Color Scheme Analysis ===")
        val analysis = generator.analyzeColorScheme(darkScheme)
        analysis.forEach { (key, value) ->
            println("$key: $value")
        }
        println()

        // Generate the UI theme file
        println("=== Generating UI Theme ===")
        val outputPath = Paths.get("output", "one-dark-pro.theme.json")
        val result = generator.generateUITheme(darkScheme, outputPath)

        // Print the result summary
        println(result.getSummary())
        println()

        // Example 2: Generate theme content without writing to disk
        println("=== Generating Theme Content (In-Memory) ===")
        val themeContent = generator.generateUIThemeContent(darkScheme)
        println("Generated ${themeContent.length} characters of JSON content")
        println("First 200 characters:")
        println(themeContent.take(200))
        println("...")
        println()

        // Example 3: Generate a light theme
        val lightScheme = WindowsTerminalColorScheme(
            name = "One Light",
            background = "#fafafa",
            foreground = "#383a42",
            black = "#000000",
            red = "#e45649",
            green = "#50a14f",
            yellow = "#c18401",
            blue = "#0184bc",
            purple = "#a626a4",
            cyan = "#0997b3",
            white = "#fafafa",
            brightBlack = "#4f525e",
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#e5c07b",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff"
        )

        println("=== Analyzing Light Theme ===")
        val lightAnalysis = generator.analyzeColorScheme(lightScheme)
        println("Theme: ${lightAnalysis["name"]}")
        println("Type: ${lightAnalysis["themeType"]}")
        println("Luminance: ${lightAnalysis["luminance"]}")
        println()

        // Example 4: Demonstrate error handling
        println("=== Error Handling Example ===")
        try {
            val invalidScheme = WindowsTerminalColorScheme(
                name = "",  // Invalid: blank name
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
                brightRed = "#e06c75",
                brightGreen = "#98c379",
                brightYellow = "#e5c07b",
                brightBlue = "#61afef",
                brightPurple = "#c678dd",
                brightCyan = "#56b6c2",
                brightWhite = "#ffffff"
            )
            generator.generateUIThemeContent(invalidScheme)
        } catch (e: IllegalArgumentException) {
            println("Caught expected error: ${e.message}")
        }
        println()

        println("=== Examples Complete ===")
    }
}

/**
 * Example demonstrating batch theme generation.
 */
object BatchThemeGenerationExample {

    @JvmStatic
    fun main(args: Array<String>) {
        val generator = UIThemeGenerator()

        // List of popular color schemes to convert
        val schemes = listOf(
            WindowsTerminalColorScheme(
                name = "Dracula",
                background = "#282a36",
                foreground = "#f8f8f2",
                black = "#21222c",
                red = "#ff5555",
                green = "#50fa7b",
                yellow = "#f1fa8c",
                blue = "#bd93f9",
                purple = "#ff79c6",
                cyan = "#8be9fd",
                white = "#f8f8f2",
                brightBlack = "#6272a4",
                brightRed = "#ff6e6e",
                brightGreen = "#69ff94",
                brightYellow = "#ffffa5",
                brightBlue = "#d6acff",
                brightPurple = "#ff92df",
                brightCyan = "#a4ffff",
                brightWhite = "#ffffff"
            ),
            WindowsTerminalColorScheme(
                name = "Monokai",
                background = "#272822",
                foreground = "#f8f8f2",
                black = "#272822",
                red = "#f92672",
                green = "#a6e22e",
                yellow = "#f4bf75",
                blue = "#66d9ef",
                purple = "#ae81ff",
                cyan = "#a1efe4",
                white = "#f8f8f2",
                brightBlack = "#75715e",
                brightRed = "#f92672",
                brightGreen = "#a6e22e",
                brightYellow = "#f4bf75",
                brightBlue = "#66d9ef",
                brightPurple = "#ae81ff",
                brightCyan = "#a1efe4",
                brightWhite = "#f9f8f5"
            )
        )

        println("=== Batch Theme Generation ===")
        println("Processing ${schemes.size} color schemes...")
        println()

        schemes.forEach { scheme ->
            val sanitizedName = scheme.name
                .replace(Regex("[^a-zA-Z0-9\\s-]"), "")
                .replace(Regex("\\s+"), "_")
                .lowercase()

            val outputPath = Paths.get("output", "themes", "$sanitizedName.theme.json")
            val result = generator.generateUITheme(scheme, outputPath)

            if (result.success) {
                println("✓ ${scheme.name} → ${result.outputPath}")
            } else {
                println("✗ ${scheme.name} - ${result.error}")
            }
        }

        println()
        println("=== Batch Generation Complete ===")
    }
}
