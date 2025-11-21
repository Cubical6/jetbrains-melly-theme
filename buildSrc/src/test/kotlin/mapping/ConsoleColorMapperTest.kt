package mapping

import colorschemes.WindowsTerminalColorScheme
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

class ConsoleColorMapperTest {

    private lateinit var mapper: ConsoleColorMapper
    private lateinit var config: ColorMappingConfig

    @BeforeEach
    fun setup() {
        config = ColorMappingConfig
        mapper = ConsoleColorMapper(config)
    }

    // ========== ANSI COLOR MAPPING TESTS ==========

    @Test
    fun `mapToConsoleColors maps all 16 ANSI colors correctly`() {
        val scheme = createTestScheme()
        val result = mapper.mapToConsoleColors(scheme)

        // Verify all 16 ANSI colors are mapped
        // Normal colors (30-37)
        result["CONSOLE_BLACK_OUTPUT"] shouldBe "#000000"
        result["CONSOLE_RED_OUTPUT"] shouldBe "#e06c75"
        result["CONSOLE_GREEN_OUTPUT"] shouldBe "#98c379"
        result["CONSOLE_YELLOW_OUTPUT"] shouldBe "#e5c07b"
        result["CONSOLE_BLUE_OUTPUT"] shouldBe "#61afef"
        result["CONSOLE_MAGENTA_OUTPUT"] shouldBe "#c678dd"
        result["CONSOLE_CYAN_OUTPUT"] shouldBe "#56b6c2"
        result["CONSOLE_GRAY_OUTPUT"] shouldBe "#abb2bf"

        // Bright colors (90-97)
        result["CONSOLE_DARKGRAY_OUTPUT"] shouldBe "#5c6370"
        result["CONSOLE_RED_BRIGHT_OUTPUT"] shouldBe "#ff6c6b"
        result["CONSOLE_GREEN_BRIGHT_OUTPUT"] shouldBe "#b5cea8"
        result["CONSOLE_YELLOW_BRIGHT_OUTPUT"] shouldBe "#ffd700"
        result["CONSOLE_BLUE_BRIGHT_OUTPUT"] shouldBe "#61afef"
        result["CONSOLE_MAGENTA_BRIGHT_OUTPUT"] shouldBe "#c678dd"
        result["CONSOLE_CYAN_BRIGHT_OUTPUT"] shouldBe "#4ec9b0"
        result["CONSOLE_WHITE_OUTPUT"] shouldBe "#ffffff"
    }

    @Test
    fun `mapToConsoleColors maps special colors correctly`() {
        val scheme = createTestScheme()
        val result = mapper.mapToConsoleColors(scheme)

        // Verify special colors
        result["CONSOLE_BACKGROUND_KEY"] shouldBe "#282c34"
        result["CONSOLE_NORMAL_OUTPUT"] shouldBe "#abb2bf"
        result["FOREGROUND"] shouldBe "#abb2bf"
    }

    // ========== FALLBACK STRATEGY TESTS ==========

    @Test
    fun `mapToConsoleColors uses fallback for missing cursorColor`() {
        val scheme = WindowsTerminalColorScheme(
            name = "Test Scheme",
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
            brightWhite = "#ffffff",
            cursorColor = null,  // Missing cursor color
            selectionBackground = "#3e4451"
        )

        val result = mapper.mapToConsoleColors(scheme)

        // Cursor color should fallback to foreground
        result["CARET_COLOR"] shouldBe "#abb2bf"
        result["CONSOLE_CURSOR"] shouldBe "#abb2bf"
    }

    @Test
    fun `mapToConsoleColors uses fallback for missing selectionBackground`() {
        val scheme = WindowsTerminalColorScheme(
            name = "Test Scheme",
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
            brightWhite = "#ffffff",
            cursorColor = "#abb2bf",
            selectionBackground = null  // Missing selection background
        )

        val result = mapper.mapToConsoleColors(scheme)

        // Selection background should be a blend of background and foreground
        result shouldContainKey "CONSOLE_SELECTION_BACKGROUND"
        // The fallback is calculated by ColorMappingConfig.getFallbackSelectionBackground
        // which blends background and foreground with 20% ratio
        result["CONSOLE_SELECTION_BACKGROUND"] shouldBe "#42464f"
    }

    @Test
    fun `mapToConsoleColors handles both optional colors missing`() {
        val scheme = WindowsTerminalColorScheme(
            name = "Test Scheme",
            background = "#1e1e1e",
            foreground = "#d4d4d4",
            black = "#000000",
            red = "#cd3131",
            green = "#0dbc79",
            yellow = "#e5e510",
            blue = "#2472c8",
            purple = "#bc3fbc",
            cyan = "#11a8cd",
            white = "#e5e5e5",
            brightBlack = "#666666",
            brightRed = "#f14c4c",
            brightGreen = "#23d18b",
            brightYellow = "#f5f543",
            brightBlue = "#3b8eea",
            brightPurple = "#d670d6",
            brightCyan = "#29b8db",
            brightWhite = "#ffffff",
            cursorColor = null,
            selectionBackground = null
        )

        val result = mapper.mapToConsoleColors(scheme)

        // Both should use fallbacks
        result["CARET_COLOR"] shouldBe "#d4d4d4"  // Fallback to foreground
        result["CONSOLE_CURSOR"] shouldBe "#d4d4d4"
        result shouldContainKey "CONSOLE_SELECTION_BACKGROUND"  // Fallback to blend
    }

    @Test
    fun `mapToConsoleColors uses provided optional colors when available`() {
        val scheme = createTestScheme()  // Has both optional colors
        val result = mapper.mapToConsoleColors(scheme)

        // Should use provided values, not fallbacks
        result["CARET_COLOR"] shouldBe "#abb2bf"
        result["CONSOLE_CURSOR"] shouldBe "#abb2bf"
        result["CONSOLE_SELECTION_BACKGROUND"] shouldBe "#42464f"
    }

    // ========== COLOR FORMAT NORMALIZATION TESTS ==========

    @Test
    fun `mapToConsoleColors normalizes colors to lowercase #RRGGBB format`() {
        val scheme = WindowsTerminalColorScheme(
            name = "Test Scheme",
            background = "#282C34",  // Uppercase
            foreground = "ABB2BF",    // No hash prefix
            black = "#000000",
            red = "#E06C75",          // Uppercase
            green = "#98C379",
            yellow = "#E5C07B",
            blue = "#61AFEF",
            purple = "#C678DD",
            cyan = "#56B6C2",
            white = "#ABB2BF",
            brightBlack = "#5C6370",
            brightRed = "#FF6C6B",
            brightGreen = "#B5CEA8",
            brightYellow = "#FFD700",
            brightBlue = "#61AFEF",
            brightPurple = "#C678DD",
            brightCyan = "#4EC9B0",
            brightWhite = "#FFFFFF"
        )

        val result = mapper.mapToConsoleColors(scheme)

        // All colors should be normalized to lowercase with # prefix
        result["CONSOLE_BACKGROUND_KEY"] shouldBe "#282c34"
        result["CONSOLE_NORMAL_OUTPUT"] shouldBe "#abb2bf"
        result["CONSOLE_RED_OUTPUT"] shouldBe "#e06c75"
        result["CONSOLE_WHITE_OUTPUT"] shouldBe "#ffffff"
    }

    // ========== MULTIPLE SCHEMES TESTS ==========

    @Test
    fun `mapToConsoleColors works with dark theme scheme`() {
        val darkScheme = WindowsTerminalColorScheme(
            name = "Dark+ (default dark)",
            background = "#1e1e1e",
            foreground = "#d4d4d4",
            black = "#000000",
            red = "#cd3131",
            green = "#0dbc79",
            yellow = "#e5e510",
            blue = "#2472c8",
            purple = "#bc3fbc",
            cyan = "#11a8cd",
            white = "#e5e5e5",
            brightBlack = "#666666",
            brightRed = "#f14c4c",
            brightGreen = "#23d18b",
            brightYellow = "#f5f543",
            brightBlue = "#3b8eea",
            brightPurple = "#d670d6",
            brightCyan = "#29b8db",
            brightWhite = "#ffffff",
            cursorColor = "#d4d4d4",
            selectionBackground = "#264f78"
        )

        val result = mapper.mapToConsoleColors(darkScheme)

        // Verify all 20 keys are present (16 ANSI + 4 special)
        result.size shouldBe 20

        // Spot check a few colors
        result["CONSOLE_BACKGROUND_KEY"] shouldBe "#1e1e1e"
        result["CONSOLE_BLUE_OUTPUT"] shouldBe "#2472c8"
        result["CARET_COLOR"] shouldBe "#d4d4d4"
        result["CONSOLE_SELECTION_BACKGROUND"] shouldBe "#264f78"
    }

    @Test
    fun `mapToConsoleColors works with light theme scheme`() {
        val lightScheme = WindowsTerminalColorScheme(
            name = "Light+ (default light)",
            background = "#ffffff",
            foreground = "#000000",
            black = "#000000",
            red = "#cd3131",
            green = "#00bc00",
            yellow = "#949800",
            blue = "#0451a5",
            purple = "#bc05bc",
            cyan = "#0598bc",
            white = "#555555",
            brightBlack = "#666666",
            brightRed = "#cd3131",
            brightGreen = "#14ce14",
            brightYellow = "#b5ba00",
            brightBlue = "#0451a5",
            brightPurple = "#bc05bc",
            brightCyan = "#0598bc",
            brightWhite = "#a5a5a5",
            cursorColor = "#000000",
            selectionBackground = "#add6ff"
        )

        val result = mapper.mapToConsoleColors(lightScheme)

        // Verify all 20 keys are present
        result.size shouldBe 20

        // Spot check a few colors
        result["CONSOLE_BACKGROUND_KEY"] shouldBe "#ffffff"
        result["CONSOLE_NORMAL_OUTPUT"] shouldBe "#000000"
        result["CONSOLE_GREEN_OUTPUT"] shouldBe "#00bc00"
        result["CONSOLE_SELECTION_BACKGROUND"] shouldBe "#add6ff"
    }

    @Test
    fun `mapToConsoleColors works with Solarized Dark scheme`() {
        val solarizedDark = WindowsTerminalColorScheme(
            name = "Solarized Dark",
            background = "#002b36",
            foreground = "#839496",
            black = "#002b36",
            red = "#dc322f",
            green = "#859900",
            yellow = "#b58900",
            blue = "#268bd2",
            purple = "#d33682",
            cyan = "#2aa198",
            white = "#eee8d5",
            brightBlack = "#073642",
            brightRed = "#cb4b16",
            brightGreen = "#586e75",
            brightYellow = "#657b83",
            brightBlue = "#839496",
            brightPurple = "#6c71c4",
            brightCyan = "#93a1a1",
            brightWhite = "#fdf6e3",
            cursorColor = "#839496",
            selectionBackground = "#073642"
        )

        val result = mapper.mapToConsoleColors(solarizedDark)

        // Verify all 20 keys are present
        result.size shouldBe 20

        // Spot check Solarized colors
        result["CONSOLE_BACKGROUND_KEY"] shouldBe "#002b36"
        result["CONSOLE_YELLOW_OUTPUT"] shouldBe "#b58900"
        result["CONSOLE_CYAN_OUTPUT"] shouldBe "#2aa198"
    }

    // ========== EDGE CASES ==========

    @Test
    fun `mapToConsoleColors handles monochrome palette`() {
        val monochromeScheme = WindowsTerminalColorScheme(
            name = "Monochrome",
            background = "#000000",
            foreground = "#ffffff",
            black = "#000000",
            red = "#444444",
            green = "#555555",
            yellow = "#666666",
            blue = "#777777",
            purple = "#888888",
            cyan = "#999999",
            white = "#aaaaaa",
            brightBlack = "#bbbbbb",
            brightRed = "#cccccc",
            brightGreen = "#dddddd",
            brightYellow = "#eeeeee",
            brightBlue = "#f0f0f0",
            brightPurple = "#f5f5f5",
            brightCyan = "#fafafa",
            brightWhite = "#ffffff"
        )

        val result = mapper.mapToConsoleColors(monochromeScheme)

        // Should still map all colors correctly
        result.size shouldBe 20
        result["CONSOLE_BACKGROUND_KEY"] shouldBe "#000000"
        result["CONSOLE_WHITE_OUTPUT"] shouldBe "#ffffff"
    }

    // ========== HELPER METHODS ==========

    /**
     * Creates a standard test scheme (based on One Dark Pro)
     */
    private fun createTestScheme(): WindowsTerminalColorScheme {
        return WindowsTerminalColorScheme(
            name = "One Dark Pro",
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
            brightWhite = "#ffffff",
            cursorColor = "#abb2bf",
            selectionBackground = "#42464f"
        )
    }
}
