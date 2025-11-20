package themes

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows

@DisplayName("TemplateProcessor Tests")
class TemplateProcessorTest {
    private val processor = TemplateProcessor()

    @Nested
    @DisplayName("Basic Variable Replacement")
    inner class BasicVariableReplacement {
        @Test
        fun `should replace single variable`() {
            val template = "Background: \$wt_background\$"
            val variables = mapOf("wt_background" to "#282c34")

            val result = processor.processTemplate(template, variables)

            assertEquals("Background: #282c34", result.content)
            assertTrue(result.isSuccess)
            assertEquals(1, result.replacedVariables.size)
            assertTrue(result.unreplacedVariables.isEmpty())
        }

        @Test
        fun `should replace multiple variables`() {
            val template = """
                background: ${"$"}wt_background${"$"}
                foreground: ${"$"}wt_foreground${"$"}
                red: ${"$"}wt_red${"$"}
            """.trimIndent()

            val variables = mapOf(
                "wt_background" to "#282c34",
                "wt_foreground" to "#abb2bf",
                "wt_red" to "#e06c75"
            )

            val result = processor.processTemplate(template, variables)

            assertTrue(result.content.contains("#282c34"))
            assertTrue(result.content.contains("#abb2bf"))
            assertTrue(result.content.contains("#e06c75"))
            assertEquals(3, result.replacedVariables.size)
            assertTrue(result.isSuccess)
        }

        @Test
        fun `should replace same variable multiple times`() {
            val template = "\$color\$ and \$color\$ and \$color\$"
            val variables = mapOf("color" to "#123456")

            val result = processor.processTemplate(template, variables)

            assertEquals("#123456 and #123456 and #123456", result.content)
            assertEquals(1, result.replacedVariables.size) // Only unique variables counted
        }

        @Test
        fun `should handle empty template`() {
            val result = processor.processTemplate("", emptyMap())

            assertEquals("", result.content)
            assertTrue(result.isSuccess)
        }

        @Test
        fun `should handle template with no variables`() {
            val template = "This is plain text with no variables"
            val result = processor.processTemplate(template, emptyMap())

            assertEquals(template, result.content)
            assertTrue(result.isSuccess)
        }
    }

    @Nested
    @DisplayName("Windows Terminal Variables")
    inner class WindowsTerminalVariables {
        @Test
        fun `should replace all Windows Terminal color variables`() {
            val template = """
                <option name="BACKGROUND" value="${"$"}wt_background${"$"}"/>
                <option name="FOREGROUND" value="${"$"}wt_foreground${"$"}"/>
                <option name="RED" value="${"$"}wt_red${"$"}"/>
                <option name="GREEN" value="${"$"}wt_green${"$"}"/>
                <option name="BRIGHT_GREEN" value="${"$"}wt_brightGreen${"$"}"/>
            """.trimIndent()

            val variables = mapOf(
                "wt_background" to "#282c34",
                "wt_foreground" to "#abb2bf",
                "wt_red" to "#e06c75",
                "wt_green" to "#98c379",
                "wt_brightGreen" to "#a0e087"
            )

            val result = processor.processTemplate(template, variables)

            assertTrue(result.isSuccess)
            assertEquals(5, result.replacedVariables.size)
            assertTrue(result.content.contains("#282c34"))
            assertTrue(result.content.contains("#98c379"))
            assertTrue(result.content.contains("#a0e087"))
        }

        @Test
        fun `should handle complete Windows Terminal color scheme`() {
            val wtVariables = mapOf(
                "wt_background" to "#282c34",
                "wt_foreground" to "#abb2bf",
                "wt_black" to "#000000",
                "wt_red" to "#e06c75",
                "wt_green" to "#98c379",
                "wt_yellow" to "#d19a66",
                "wt_blue" to "#61afef",
                "wt_purple" to "#c678dd",
                "wt_cyan" to "#56b6c2",
                "wt_white" to "#abb2bf",
                "wt_brightBlack" to "#5c6370",
                "wt_brightRed" to "#e06c75",
                "wt_brightGreen" to "#98c379",
                "wt_brightYellow" to "#d19a66",
                "wt_brightBlue" to "#61afef",
                "wt_brightPurple" to "#c678dd",
                "wt_brightCyan" to "#56b6c2",
                "wt_brightWhite" to "#ffffff"
            )

            val template = TemplateProcessor.WINDOWS_TERMINAL_VARIABLES
                .joinToString("\n") { "\$${it}\$" }

            val result = processor.processTemplate(template, wtVariables)

            // Should have replaced most variables (some WT variables are optional)
            assertTrue(result.replacedVariables.size >= 16)
        }
    }

    @Nested
    @DisplayName("Legacy One Dark Variables")
    inner class LegacyOneDarkVariables {
        @Test
        fun `should replace legacy color variables`() {
            val template = """
                <option name="GREEN" value="${"$"}green${"$"}"/>
                <option name="CORAL" value="${"$"}coral${"$"}"/>
                <option name="CHALKY" value="${"$"}chalky${"$"}"/>
            """.trimIndent()

            val variables = mapOf(
                "green" to "#98c379",
                "coral" to "#e06c75",
                "chalky" to "#e5c07b"
            )

            val result = processor.processTemplate(template, variables)

            assertTrue(result.isSuccess)
            assertEquals(3, result.replacedVariables.size)
        }

        @Test
        fun `should work with mix of Windows Terminal and legacy variables`() {
            val template = "\$wt_background\$ and \$green\$ and \$wt_red\$"
            val variables = mapOf(
                "wt_background" to "#282c34",
                "green" to "#98c379",
                "wt_red" to "#e06c75"
            )

            val result = processor.processTemplate(template, variables)

            assertTrue(result.isSuccess)
            assertEquals(3, result.replacedVariables.size)
        }
    }

    @Nested
    @DisplayName("Error Handling and Validation")
    inner class ErrorHandlingAndValidation {
        @Test
        fun `should detect unreplaced variables in non-strict mode`() {
            val template = "\$var1\$ \$var2\$ \$var3\$"
            val variables = mapOf("var1" to "value1")

            val result = processor.processTemplate(template, variables, strict = false)

            assertFalse(result.isSuccess)
            assertEquals(1, result.replacedVariables.size)
            assertEquals(2, result.unreplacedVariables.size)
            assertTrue(result.unreplacedVariables.contains("var2"))
            assertTrue(result.unreplacedVariables.contains("var3"))
            assertTrue(result.warnings.isNotEmpty())
        }

        @Test
        fun `should throw exception for unreplaced variables in strict mode`() {
            val template = "\$missing_var\$"
            val variables = emptyMap<String, String>()

            val exception = assertThrows<IllegalArgumentException> {
                processor.processTemplate(template, variables, strict = true)
            }

            assertTrue(exception.message!!.contains("missing_var"))
        }

        @Test
        fun `should provide helpful error messages`() {
            val template = "\$var1\$ \$var2\$"
            val variables = emptyMap<String, String>()

            val result = processor.processTemplate(template, variables, strict = false)

            assertEquals(2, result.warnings.size)
            assertTrue(result.warnings.any { it.contains("var1") })
            assertTrue(result.warnings.any { it.contains("var2") })
        }

        @Test
        fun `validateAllVariablesReplaced should return true when all replaced`() {
            val content = "Background: #282c34, Foreground: #abb2bf"
            assertTrue(processor.validateAllVariablesReplaced(content))
        }

        @Test
        fun `validateAllVariablesReplaced should return false when variables remain`() {
            val content = "Background: \$wt_background\$, Foreground: #abb2bf"
            assertFalse(processor.validateAllVariablesReplaced(content))
        }

        @Test
        fun `should validate required variables`() {
            val required = listOf("wt_background", "wt_foreground", "wt_red")
            val provided = mapOf(
                "wt_background" to "#282c34",
                "wt_red" to "#e06c75"
            )

            val missing = processor.validateRequiredVariables(required, provided)

            assertEquals(1, missing.size)
            assertTrue(missing.contains("wt_foreground"))
        }

        @Test
        fun `should validate color values`() {
            val variables = mapOf(
                "wt_background" to "#282c34",  // Valid
                "wt_foreground" to "invalid",   // Invalid
                "wt_red" to "e06c75",           // Valid (no # prefix)
                "wt_green" to "#ZZZ"            // Invalid
            )

            val errors = processor.validateColorValues(variables)

            assertTrue(errors.containsKey("wt_foreground"))
            assertTrue(errors.containsKey("wt_green"))
            assertFalse(errors.containsKey("wt_background"))
        }
    }

    @Nested
    @DisplayName("Variable Discovery and Analysis")
    inner class VariableDiscoveryAndAnalysis {
        @Test
        fun `should find all variables in template`() {
            val template = "\$var1\$ some text \$var2\$ more text \$var3\$"

            val variables = processor.findVariables(template)

            assertEquals(3, variables.size)
            assertTrue(variables.containsAll(listOf("var1", "var2", "var3")))
        }

        @Test
        fun `should find variables with underscores and numbers`() {
            val template = "\$wt_brightGreen\$ \$var_123\$ \$VAR_NAME_2\$"

            val variables = processor.findVariables(template)

            assertEquals(3, variables.size)
            assertTrue(variables.contains("wt_brightGreen"))
            assertTrue(variables.contains("var_123"))
            assertTrue(variables.contains("VAR_NAME_2"))
        }

        @Test
        fun `should return unique variable names`() {
            val template = "\$color\$ \$color\$ \$color\$"

            val variables = processor.findVariables(template)

            assertEquals(1, variables.size)
            assertEquals("color", variables[0])
        }

        @Test
        fun `should filter variables by prefix`() {
            val variables = mapOf(
                "wt_background" to "#282c34",
                "wt_foreground" to "#abb2bf",
                "green" to "#98c379",
                "coral" to "#e06c75"
            )

            val wtVars = processor.filterVariablesByPrefix(variables, "wt_")

            assertEquals(2, wtVars.size)
            assertTrue(wtVars.containsKey("wt_background"))
            assertTrue(wtVars.containsKey("wt_foreground"))
            assertFalse(wtVars.containsKey("green"))
        }
    }

    @Nested
    @DisplayName("Utility Functions")
    inner class UtilityFunctions {
        @Test
        fun `replaceVariables should replace all matching variables`() {
            val content = "\$a\$ \$b\$ \$c\$"
            val variables = mapOf(
                "a" to "1",
                "b" to "2",
                "c" to "3"
            )

            val result = processor.replaceVariables(content, variables)

            assertEquals("1 2 3", result)
        }

        @Test
        fun `replaceVariables should ignore missing variables`() {
            val content = "\$a\$ \$missing\$"
            val variables = mapOf("a" to "1")

            val result = processor.replaceVariables(content, variables)

            assertEquals("1 \$missing\$", result)
        }

        @Test
        fun `mergeVariables should combine multiple maps`() {
            val defaults = mapOf("a" to "1", "b" to "2")
            val overrides = mapOf("b" to "3", "c" to "4")

            val merged = processor.mergeVariables(defaults, overrides)

            assertEquals(3, merged.size)
            assertEquals("1", merged["a"])
            assertEquals("3", merged["b"]) // Override value
            assertEquals("4", merged["c"])
        }

        @Test
        fun `normalizeColorValues should add # prefix to colors without it`() {
            val variables = mapOf(
                "color1" to "#282c34",  // Already has #
                "color2" to "e06c75",   // Missing #
                "color3" to "98c379",   // Missing #
                "notColor" to "sometext" // Not a color
            )

            val normalized = processor.normalizeColorValues(variables)

            assertEquals("#282c34", normalized["color1"])
            assertEquals("#e06c75", normalized["color2"])
            assertEquals("#98c379", normalized["color3"])
            assertEquals("sometext", normalized["notColor"])
        }

        @Test
        fun `generateVariableReport should provide useful analysis`() {
            val template = "\$wt_background\$ \$wt_foreground\$ \$missing\$"
            val variables = mapOf(
                "wt_background" to "#282c34",
                "wt_foreground" to "#abb2bf"
            )

            val report = processor.generateVariableReport(template, variables)

            assertTrue(report.contains("Total variables"))
            assertTrue(report.contains("wt_background"))
            assertTrue(report.contains("wt_foreground"))
            assertTrue(report.contains("missing"))
        }
    }

    @Nested
    @DisplayName("Real-world XML Template Scenarios")
    inner class RealWorldXmlScenarios {
        @Test
        fun `should process IntelliJ color scheme XML template`() {
            val template = """
                <?xml version="1.0" encoding="UTF-8"?>
                <scheme name="Windows Terminal One Dark" parent_scheme="Darcula" version="142">
                  <colors>
                    <option name="BACKGROUND" value="${"$"}wt_background${"$"}"/>
                    <option name="FOREGROUND" value="${"$"}wt_foreground${"$"}"/>
                    <option name="CONSOLE_RED" value="${"$"}wt_red${"$"}"/>
                    <option name="CONSOLE_GREEN" value="${"$"}wt_green${"$"}"/>
                  </colors>
                </scheme>
            """.trimIndent()

            val variables = mapOf(
                "wt_background" to "#282c34",
                "wt_foreground" to "#abb2bf",
                "wt_red" to "#e06c75",
                "wt_green" to "#98c379"
            )

            val result = processor.processTemplate(template, variables)

            assertTrue(result.isSuccess)
            assertTrue(result.content.contains("#282c34"))
            assertTrue(result.content.contains("#abb2bf"))
            assertTrue(result.content.contains("#e06c75"))
            assertTrue(result.content.contains("#98c379"))
            assertFalse(result.content.contains("\$wt_"))
        }

        @Test
        fun `should process theme JSON template`() {
            val template = """
                {
                  "name": "Windows Terminal One Dark",
                  "ui": {
                    "background": "${"$"}wt_background${"$"}",
                    "foreground": "${"$"}wt_foreground${"$"}"
                  }
                }
            """.trimIndent()

            val variables = mapOf(
                "wt_background" to "#282c34",
                "wt_foreground" to "#abb2bf"
            )

            val result = processor.processTemplate(template, variables)

            assertTrue(result.isSuccess)
            assertTrue(result.content.contains("\"background\": \"#282c34\""))
            assertTrue(result.content.contains("\"foreground\": \"#abb2bf\""))
        }
    }

    @Nested
    @DisplayName("Edge Cases and Special Characters")
    inner class EdgeCasesAndSpecialCharacters {
        @Test
        fun `should not replace variables with single delimiter`() {
            val template = "Price: \$50"
            val variables = mapOf("50" to "replaced")

            val result = processor.processTemplate(template, variables)

            assertEquals("Price: \$50", result.content)
            assertTrue(result.isSuccess)
        }

        @Test
        fun `should handle escaped delimiters`() {
            // Variables must be valid identifiers (start with letter or underscore)
            val template = "\$_var\$ and \$var_name\$"
            val variables = mapOf(
                "_var" to "value1",
                "var_name" to "value2"
            )

            val result = processor.processTemplate(template, variables)

            assertTrue(result.isSuccess)
            assertTrue(result.content.contains("value1"))
            assertTrue(result.content.contains("value2"))
        }

        @Test
        fun `should handle adjacent variables`() {
            val template = "\$a\$\$b\$\$c\$"
            val variables = mapOf(
                "a" to "1",
                "b" to "2",
                "c" to "3"
            )

            val result = processor.processTemplate(template, variables)

            assertEquals("123", result.content)
        }

        @Test
        fun `should handle multiline templates`() {
            val template = """
                Line 1: ${"$"}var1${"$"}
                Line 2: ${"$"}var2${"$"}
                Line 3: ${"$"}var3${"$"}
            """.trimIndent()

            val variables = mapOf(
                "var1" to "A",
                "var2" to "B",
                "var3" to "C"
            )

            val result = processor.processTemplate(template, variables)

            assertTrue(result.isSuccess)
            assertTrue(result.content.contains("Line 1: A"))
            assertTrue(result.content.contains("Line 2: B"))
            assertTrue(result.content.contains("Line 3: C"))
        }
    }

    @Nested
    @DisplayName("Processing Result")
    inner class ProcessingResultTests {
        @Test
        fun `ProcessingResult should provide useful summary`() {
            val template = "\$var1\$ \$var2\$ \$missing\$"
            val variables = mapOf("var1" to "a", "var2" to "b")

            val result = processor.processTemplate(template, variables, strict = false)
            val summary = result.getSummary()

            assertTrue(summary.contains("Replaced: 2"))
            assertTrue(summary.contains("Unreplaced: 1"))
            assertTrue(summary.contains("missing"))
        }

        @Test
        fun `ProcessingResult isSuccess should be true when all variables replaced`() {
            val template = "\$var1\$"
            val variables = mapOf("var1" to "value")

            val result = processor.processTemplate(template, variables)

            assertTrue(result.isSuccess)
            assertTrue(result.unreplacedVariables.isEmpty())
        }

        @Test
        fun `ProcessingResult isSuccess should be false when variables remain`() {
            val template = "\$var1\$ \$var2\$"
            val variables = mapOf("var1" to "value")

            val result = processor.processTemplate(template, variables)

            assertFalse(result.isSuccess)
            assertFalse(result.unreplacedVariables.isEmpty())
        }
    }
}
