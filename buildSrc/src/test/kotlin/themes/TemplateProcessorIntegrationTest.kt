package themes

import colorschemes.WindowsTerminalColorScheme
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Integration tests demonstrating how TemplateProcessor works with
 * WindowsTerminalColorScheme to generate IntelliJ themes.
 */
@DisplayName("TemplateProcessor Integration Tests")
class TemplateProcessorIntegrationTest {

    private val processor = TemplateProcessor()

    @Test
    @DisplayName("Complete workflow: WindowsTerminalColorScheme to IntelliJ theme")
    fun `should process complete Windows Terminal scheme into IntelliJ theme`() {
        // Step 1: Create a Windows Terminal color scheme (e.g., from imported JSON)
        val wtScheme = WindowsTerminalColorScheme(
            name = "One Dark",
            background = "#282c34",
            foreground = "#abb2bf",
            black = "#000000",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#d19a66",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            brightBlack = "#5c6370",
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#d19a66",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff",
            cursorColor = "#528bff",
            selectionBackground = "#3e4451"
        )

        // Step 2: Validate the Windows Terminal scheme
        val validationErrors = wtScheme.validate()
        assertTrue(validationErrors.isEmpty(), "Windows Terminal scheme should be valid")

        // Step 3: Convert to color palette map
        val colorVariables = wtScheme.toColorPalette()
        assertEquals(20, colorVariables.size, "Should have all Windows Terminal color variables")

        // Step 4: Create a simple template
        val template = """
            <scheme name="${"$"}name${"$"}" version="142">
              <colors>
                <option name="BACKGROUND" value="${"$"}wt_background${"$"}"/>
                <option name="FOREGROUND" value="${"$"}wt_foreground${"$"}"/>
                <option name="CONSOLE_RED" value="${"$"}wt_red${"$"}"/>
                <option name="CONSOLE_GREEN" value="${"$"}wt_green${"$"}"/>
                <option name="CONSOLE_BRIGHT_GREEN" value="${"$"}wt_brightGreen${"$"}"/>
              </colors>
            </scheme>
        """.trimIndent()

        // Step 5: Add the name variable
        val allVariables = colorVariables + mapOf("name" to wtScheme.name)

        // Step 6: Process the template
        val result = processor.processTemplate(template, allVariables, strict = true)

        // Step 7: Verify the output
        assertTrue(result.isSuccess)
        assertTrue(result.content.contains("<scheme name=\"One Dark\""))
        assertTrue(result.content.contains("value=\"#282c34\""))
        assertTrue(result.content.contains("value=\"#e06c75\""))
        assertTrue(result.content.contains("value=\"#98c379\""))
        assertFalse(result.content.contains("$"), "No placeholders should remain")
    }

    @Test
    @DisplayName("Load and process actual Windows Terminal template file")
    fun `should load and process Windows Terminal template from file`() {
        // Try to load the sample template
        val templatePath = Paths.get(
            System.getProperty("user.dir"),
            "src", "test", "resources", "windows-terminal-sample.template.xml"
        )

        // Skip if file doesn't exist (in environments without file system)
        if (!Files.exists(templatePath)) {
            println("Template file not found, skipping file-based test")
            return
        }

        val template = Files.readString(templatePath)

        // Create Windows Terminal color scheme
        val wtScheme = WindowsTerminalColorScheme(
            name = "One Dark Pro",
            background = "#282c34",
            foreground = "#abb2bf",
            black = "#000000",
            red = "#e06c75",
            green = "#98c379",
            yellow = "#d19a66",
            blue = "#61afef",
            purple = "#c678dd",
            cyan = "#56b6c2",
            white = "#abb2bf",
            brightBlack = "#5c6370",
            brightRed = "#e06c75",
            brightGreen = "#98c379",
            brightYellow = "#d19a66",
            brightBlue = "#61afef",
            brightPurple = "#c678dd",
            brightCyan = "#56b6c2",
            brightWhite = "#ffffff"
        )

        // Convert to variables and process
        val variables = wtScheme.toColorPalette()
        val result = processor.processTemplate(template, variables)

        // Verify
        assertTrue(result.isSuccess, "All variables should be replaced")
        assertFalse(result.content.contains("\$wt_"), "No Windows Terminal placeholders should remain")
        assertTrue(result.content.contains("#282c34"))
        assertTrue(result.content.contains("#e06c75"))
    }

    @Test
    @DisplayName("Handle multiple color schemes with same template")
    fun `should process multiple color schemes with same template`() {
        val template = """
            <scheme name="${"$"}name${"$"}">
              <option name="BG" value="${"$"}wt_background${"$"}"/>
              <option name="FG" value="${"$"}wt_foreground${"$"}"/>
            </scheme>
        """.trimIndent()

        // Scheme 1: One Dark
        val oneDark = WindowsTerminalColorScheme(
            name = "One Dark",
            background = "#282c34",
            foreground = "#abb2bf",
            black = "#000000", red = "#e06c75", green = "#98c379", yellow = "#d19a66",
            blue = "#61afef", purple = "#c678dd", cyan = "#56b6c2", white = "#abb2bf",
            brightBlack = "#5c6370", brightRed = "#e06c75", brightGreen = "#98c379",
            brightYellow = "#d19a66", brightBlue = "#61afef", brightPurple = "#c678dd",
            brightCyan = "#56b6c2", brightWhite = "#ffffff"
        )

        // Scheme 2: Dracula-style
        val dracula = WindowsTerminalColorScheme(
            name = "Dracula",
            background = "#282a36",
            foreground = "#f8f8f2",
            black = "#000000", red = "#ff5555", green = "#50fa7b", yellow = "#f1fa8c",
            blue = "#bd93f9", purple = "#ff79c6", cyan = "#8be9fd", white = "#bbbbbb",
            brightBlack = "#555555", brightRed = "#ff5555", brightGreen = "#50fa7b",
            brightYellow = "#f1fa8c", brightBlue = "#bd93f9", brightPurple = "#ff79c6",
            brightCyan = "#8be9fd", brightWhite = "#ffffff"
        )

        // Process both schemes
        val oneDarkVars = oneDark.toColorPalette() + mapOf("name" to oneDark.name)
        val draculaVars = dracula.toColorPalette() + mapOf("name" to dracula.name)

        val oneDarkResult = processor.processTemplate(template, oneDarkVars)
        val draculaResult = processor.processTemplate(template, draculaVars)

        // Verify both results
        assertTrue(oneDarkResult.isSuccess)
        assertTrue(draculaResult.isSuccess)

        assertTrue(oneDarkResult.content.contains("One Dark"))
        assertTrue(oneDarkResult.content.contains("#282c34"))

        assertTrue(draculaResult.content.contains("Dracula"))
        assertTrue(draculaResult.content.contains("#282a36"))
        assertTrue(draculaResult.content.contains("#f8f8f2"))
    }

    @Test
    @DisplayName("Validate and report template variables before processing")
    fun `should analyze template requirements before processing`() {
        val template = """
            ${"$"}wt_background${"$"}
            ${"$"}wt_foreground${"$"}
            ${"$"}wt_red${"$"}
            ${"$"}wt_green${"$"}
            ${"$"}wt_blue${"$"}
        """.trimIndent()

        // Find what variables the template needs
        val requiredVars = processor.findVariables(template)
        assertEquals(5, requiredVars.size)

        // Create partial color scheme (missing some colors)
        val partialVariables = mapOf(
            "wt_background" to "#282c34",
            "wt_foreground" to "#abb2bf",
            "wt_red" to "#e06c75"
            // Missing wt_green and wt_blue
        )

        // Validate before processing
        val missingVars = processor.validateRequiredVariables(requiredVars, partialVariables)
        assertEquals(2, missingVars.size)
        assertTrue(missingVars.contains("wt_green"))
        assertTrue(missingVars.contains("wt_blue"))

        // Generate analysis report
        val report = processor.generateVariableReport(template, partialVariables)
        assertTrue(report.contains("Missing variables: 2"))
        assertTrue(report.contains("wt_green"))
        assertTrue(report.contains("wt_blue"))
    }

    @Test
    @DisplayName("Merge default variables with scheme-specific overrides")
    fun `should merge default and custom variables`() {
        // Default IDE colors that apply to all themes
        val defaultVariables = mapOf(
            "ide_error_color" to "#f44747",
            "ide_warning_color" to "#cca700",
            "ide_info_color" to "#75beff"
        )

        // Windows Terminal scheme colors
        val wtScheme = WindowsTerminalColorScheme(
            name = "Custom Theme",
            background = "#1e1e1e",
            foreground = "#d4d4d4",
            black = "#000000", red = "#cd3131", green = "#0dbc79", yellow = "#e5e510",
            blue = "#2472c8", purple = "#bc3fbc", cyan = "#11a8cd", white = "#e5e5e5",
            brightBlack = "#666666", brightRed = "#f14c4c", brightGreen = "#23d18b",
            brightYellow = "#f5f543", brightBlue = "#3b8eea", brightPurple = "#d670d6",
            brightCyan = "#29b8db", brightWhite = "#ffffff"
        )

        // Merge all variables
        val allVariables = processor.mergeVariables(
            defaultVariables,
            wtScheme.toColorPalette()
        )

        // Should have both default and Windows Terminal variables
        assertTrue(allVariables.containsKey("ide_error_color"))
        assertTrue(allVariables.containsKey("wt_background"))
        assertTrue(allVariables.size > 20)

        // Process a template using both
        val template = """
            Background: ${"$"}wt_background${"$"}
            Error: ${"$"}ide_error_color${"$"}
        """.trimIndent()

        val result = processor.processTemplate(template, allVariables)
        assertTrue(result.isSuccess)
        assertTrue(result.content.contains("#1e1e1e"))
        assertTrue(result.content.contains("#f44747"))
    }

    @Test
    @DisplayName("Demonstrate error handling with helpful messages")
    fun `should provide helpful error messages for debugging`() {
        val template = """
            <option name="BG" value="${"$"}wt_background${"$"}"/>
            <option name="FG" value="${"$"}wt_forground${"$"}"/>
            <option name="RED" value="${"$"}wt_red${"$"}"/>
        """.trimIndent()

        // Note the typo: "forground" instead of "foreground"
        val variables = mapOf(
            "wt_background" to "#282c34",
            "wt_foreground" to "#abb2bf",  // Correct spelling
            "wt_red" to "#e06c75"
        )

        // Process in non-strict mode to get detailed feedback
        val result = processor.processTemplate(template, variables, strict = false)

        // Should report the missing variable
        assertFalse(result.isSuccess)
        assertEquals(1, result.unreplacedVariables.size)
        assertEquals("wt_forground", result.unreplacedVariables[0])

        // Generate detailed report
        val report = processor.generateVariableReport(template, variables)
        assertTrue(report.contains("wt_forground"))
        // Should suggest the similar variable "wt_foreground"
        assertTrue(report.toLowerCase().contains("foreground"))
    }

    @Test
    @DisplayName("Normalize color values before processing")
    fun `should normalize color values to ensure consistency`() {
        // Some color values might be missing the # prefix
        val unnormalizedVariables = mapOf(
            "wt_background" to "282c34",     // Missing #
            "wt_foreground" to "#abb2bf",    // Has #
            "wt_red" to "e06c75"             // Missing #
        )

        // Normalize before processing
        val normalized = processor.normalizeColorValues(unnormalizedVariables)

        assertEquals("#282c34", normalized["wt_background"])
        assertEquals("#abb2bf", normalized["wt_foreground"])
        assertEquals("#e06c75", normalized["wt_red"])

        // Use normalized values in template
        val template = "<color value=\"\$wt_background\$\"/>"
        val result = processor.processTemplate(template, normalized)

        assertTrue(result.isSuccess)
        assertTrue(result.content.contains("#282c34"))
    }
}
