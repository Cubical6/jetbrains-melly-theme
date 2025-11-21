package tasks

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import io.kotest.matchers.shouldBe
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.collections.shouldNotBeEmpty

class AccessibilityAuditTest {

    @Test
    fun `calculateContrastRatio is accurate for known values`() {
        // Test with examples from the existing ColorUtilsTest
        val blackWhiteRatio = utils.ColorUtils.calculateContrastRatio("#000000", "#ffffff")
        blackWhiteRatio shouldBeGreaterThanOrEqual 20.9 // Should be ~21:1

        val sameColorRatio = utils.ColorUtils.calculateContrastRatio("#ff0000", "#ff0000")
        sameColorRatio shouldBe 1.0 // Same color = 1:1
    }

    @Test
    fun `auditTheme detects failing themes`(@TempDir tempDir: File) {
        // Create a theme with poor contrast
        val themeFile = File(tempDir, "poor-contrast.json")
        themeFile.writeText("""
            {
              "name": "Poor Contrast Test",
              "background": "#cccccc",
              "foreground": "#dddddd",
              "cursorColor": "#dddddd",
              "selectionBackground": "#cccccc",
              "black": "#000000",
              "red": "#ff0000",
              "green": "#00ff00",
              "yellow": "#ffff00",
              "blue": "#0000ff",
              "purple": "#ff00ff",
              "cyan": "#00ffff",
              "white": "#ffffff",
              "brightBlack": "#808080",
              "brightRed": "#ff0000",
              "brightGreen": "#00ff00",
              "brightYellow": "#ffff00",
              "brightBlue": "#0000ff",
              "brightPurple": "#ff00ff",
              "brightCyan": "#00ffff",
              "brightWhite": "#ffffff"
            }
        """.trimIndent())

        val result = AccessibilityAudit.auditTheme(themeFile)

        result.themeName shouldBe "Poor Contrast Test"
        result.overallPass shouldBe false // Should fail due to poor fg/bg contrast
        result.failureCount shouldBeGreaterThanOrEqual 1
    }

    @Test
    fun `auditTheme passes themes with good contrast`(@TempDir tempDir: File) {
        // Create a theme with excellent contrast
        val themeFile = File(tempDir, "good-contrast.json")
        themeFile.writeText("""
            {
              "name": "Good Contrast Test",
              "background": "#000000",
              "foreground": "#ffffff",
              "cursorColor": "#ffffff",
              "selectionBackground": "#333333",
              "black": "#000000",
              "red": "#ff6b68",
              "green": "#a8ff60",
              "yellow": "#ffffb6",
              "blue": "#96cbfe",
              "purple": "#ff73fd",
              "cyan": "#c6c5fe",
              "white": "#f1f1f0",
              "brightBlack": "#b0b0b0",
              "brightRed": "#ff8785",
              "brightGreen": "#c1ff87",
              "brightYellow": "#ffffcd",
              "brightBlue": "#b5dcfe",
              "brightPurple": "#ff9cfe",
              "brightCyan": "#dfdffe",
              "brightWhite": "#ffffff"
            }
        """.trimIndent())

        val result = AccessibilityAudit.auditTheme(themeFile)

        result.themeName shouldBe "Good Contrast Test"
        result.overallPass shouldBe true
        result.failureCount shouldBe 0
    }

    @Test
    fun `generateReport creates valid output`(@TempDir tempDir: File) {
        val themeFile = File(tempDir, "test.json")
        themeFile.writeText("""
            {
              "name": "Test Theme",
              "background": "#000000",
              "foreground": "#ffffff",
              "cursorColor": "#ffffff",
              "selectionBackground": "#333333",
              "black": "#000000",
              "red": "#ff0000",
              "green": "#00ff00",
              "yellow": "#ffff00",
              "blue": "#0000ff",
              "purple": "#ff00ff",
              "cyan": "#00ffff",
              "white": "#ffffff",
              "brightBlack": "#808080",
              "brightRed": "#ff0000",
              "brightGreen": "#00ff00",
              "brightYellow": "#ffff00",
              "brightBlue": "#0000ff",
              "brightPurple": "#ff00ff",
              "brightCyan": "#00ffff",
              "brightWhite": "#ffffff"
            }
        """.trimIndent())

        val results = listOf(AccessibilityAudit.auditTheme(themeFile))
        val report = AccessibilityAudit.generateReport(results)

        report.shouldNotBeEmpty()
        report.contains("WCAG AA ACCESSIBILITY AUDIT REPORT") shouldBe true
        report.contains("Test Theme") shouldBe true
    }

    @Test
    fun `suggestColorAdjustment improves contrast`() {
        val foreground = "#cccccc"
        val background = "#dddddd"
        val targetRatio = 4.5

        val suggestion = AccessibilityAudit.suggestColorAdjustment(
            foreground,
            background,
            targetRatio
        )

        if (suggestion != null) {
            suggestion.newRatio shouldBeGreaterThanOrEqual suggestion.originalRatio
        }
    }
}
