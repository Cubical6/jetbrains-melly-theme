package tasks

import com.google.gson.Gson
import com.google.gson.JsonObject
import utils.ColorUtils
import java.io.File
import kotlin.math.round

/**
 * WCAG AA Accessibility Audit for Windows Terminal themes.
 *
 * Checks contrast ratios for:
 * - Foreground/Background (primary text)
 * - Console colors against background
 * - Cursor visibility
 * - Selection visibility
 *
 * WCAG AA Requirements:
 * - Normal text: 4.5:1 minimum
 * - Large text: 3.0:1 minimum
 * - UI components: 3.0:1 minimum
 */
object AccessibilityAudit {

    private const val WCAG_AA_NORMAL_TEXT = 4.5
    private const val WCAG_AA_LARGE_TEXT = 3.0
    private const val WCAG_AA_UI_COMPONENT = 3.0

    data class ContrastCheck(
        val description: String,
        val foreground: String,
        val background: String,
        val ratio: Double,
        val requirement: Double,
        val passes: Boolean,
        val category: String
    )

    data class ThemeAuditResult(
        val themeName: String,
        val themeFile: String,
        val overallPass: Boolean,
        val checks: List<ContrastCheck>,
        val failureCount: Int,
        val passCount: Int
    )

    data class ColorAdjustmentSuggestion(
        val originalColor: String,
        val suggestedColor: String,
        val originalRatio: Double,
        val newRatio: Double,
        val description: String
    )

    /**
     * Runs accessibility audit on all Windows Terminal themes.
     */
    fun auditAllThemes(themesDirectory: File): List<ThemeAuditResult> {
        val themeFiles = themesDirectory.listFiles { file ->
            file.extension == "json"
        }?.sortedBy { it.name } ?: emptyList()

        return themeFiles.map { auditTheme(it) }
    }

    /**
     * Audits a single theme file.
     */
    fun auditTheme(themeFile: File): ThemeAuditResult {
        val gson = Gson()
        val theme = gson.fromJson(themeFile.readText(), JsonObject::class.java)

        val themeName = theme.get("name")?.asString ?: themeFile.nameWithoutExtension
        val background = theme.get("background")?.asString ?: "#000000"
        val foreground = theme.get("foreground")?.asString ?: "#ffffff"
        val cursorColor = theme.get("cursorColor")?.asString ?: foreground
        val selectionBg = theme.get("selectionBackground")?.asString ?: background

        val checks = mutableListOf<ContrastCheck>()

        // 1. Primary foreground/background check (most critical)
        checks.add(checkContrast(
            description = "Primary text (foreground on background)",
            foreground = foreground,
            background = background,
            requirement = WCAG_AA_NORMAL_TEXT,
            category = "Primary"
        ))

        // 2. Cursor visibility
        checks.add(checkContrast(
            description = "Cursor visibility (cursor on background)",
            foreground = cursorColor,
            background = background,
            requirement = WCAG_AA_UI_COMPONENT,
            category = "UI Component"
        ))

        // 3. Selection visibility
        checks.add(checkContrast(
            description = "Selection visibility (foreground on selection)",
            foreground = foreground,
            background = selectionBg,
            requirement = WCAG_AA_NORMAL_TEXT,
            category = "Primary"
        ))

        // 4. Console colors (8 standard colors)
        val consoleColors = listOf(
            "black", "red", "green", "yellow",
            "blue", "purple", "cyan", "white"
        )

        consoleColors.forEach { colorName ->
            val color = theme.get(colorName)?.asString
            if (color != null) {
                checks.add(checkContrast(
                    description = "Console $colorName on background",
                    foreground = color,
                    background = background,
                    requirement = WCAG_AA_NORMAL_TEXT,
                    category = "Console Color"
                ))
            }
        }

        // 5. Bright console colors
        val brightColors = listOf(
            "brightBlack", "brightRed", "brightGreen", "brightYellow",
            "brightBlue", "brightPurple", "brightCyan", "brightWhite"
        )

        brightColors.forEach { colorName ->
            val color = theme.get(colorName)?.asString
            if (color != null) {
                checks.add(checkContrast(
                    description = "Console $colorName on background",
                    foreground = color,
                    background = background,
                    requirement = WCAG_AA_NORMAL_TEXT,
                    category = "Console Color"
                ))
            }
        }

        val failures = checks.filter { !it.passes }
        val passes = checks.filter { it.passes }

        return ThemeAuditResult(
            themeName = themeName,
            themeFile = themeFile.name,
            overallPass = failures.isEmpty(),
            checks = checks,
            failureCount = failures.size,
            passCount = passes.size
        )
    }

    /**
     * Checks contrast ratio between two colors.
     */
    private fun checkContrast(
        description: String,
        foreground: String,
        background: String,
        requirement: Double,
        category: String
    ): ContrastCheck {
        val ratio = ColorUtils.calculateContrastRatio(foreground, background)
        val passes = ratio >= requirement

        return ContrastCheck(
            description = description,
            foreground = foreground,
            background = background,
            ratio = round(ratio * 100) / 100,
            requirement = requirement,
            passes = passes,
            category = category
        )
    }

    /**
     * Suggests color adjustments to fix contrast issues.
     */
    fun suggestColorAdjustment(
        foreground: String,
        background: String,
        targetRatio: Double
    ): ColorAdjustmentSuggestion? {
        val currentRatio = ColorUtils.calculateContrastRatio(foreground, background)
        if (currentRatio >= targetRatio) {
            return null // Already meets requirement
        }

        // Determine if background is dark or light
        val bgLuminance = ColorUtils.calculateRelativeLuminance(background)
        val isDarkBackground = bgLuminance < 0.5

        // Try adjusting the foreground color
        var adjustedColor = foreground
        var bestRatio = currentRatio
        var step = 0.05

        // If dark background, lighten foreground; if light background, darken foreground
        for (i in 1..20) {
            val testColor = if (isDarkBackground) {
                ColorUtils.lighten(foreground, step * i)
            } else {
                ColorUtils.darken(foreground, step * i)
            }

            val testRatio = ColorUtils.calculateContrastRatio(testColor, background)
            if (testRatio > bestRatio) {
                bestRatio = testRatio
                adjustedColor = testColor
            }

            if (testRatio >= targetRatio) {
                break
            }
        }

        return if (bestRatio > currentRatio) {
            ColorAdjustmentSuggestion(
                originalColor = foreground,
                suggestedColor = adjustedColor,
                originalRatio = round(currentRatio * 100) / 100,
                newRatio = round(bestRatio * 100) / 100,
                description = if (isDarkBackground) "Lightened" else "Darkened"
            )
        } else {
            null
        }
    }

    /**
     * Generates a detailed accessibility audit report.
     */
    fun generateReport(results: List<ThemeAuditResult>): String {
        val report = StringBuilder()
        val timestamp = java.time.LocalDateTime.now().toString()

        report.appendLine("=" .repeat(80))
        report.appendLine("WCAG AA ACCESSIBILITY AUDIT REPORT")
        report.appendLine("Windows Terminal Theme Collection")
        report.appendLine("=" .repeat(80))
        report.appendLine()
        report.appendLine("Generated: $timestamp")
        report.appendLine("Total themes audited: ${results.size}")
        report.appendLine()

        // Summary statistics
        val passingThemes = results.filter { it.overallPass }
        val failingThemes = results.filter { !it.overallPass }

        report.appendLine("SUMMARY")
        report.appendLine("-" .repeat(80))
        report.appendLine("Passing themes: ${passingThemes.size} (${percentage(passingThemes.size, results.size)}%)")
        report.appendLine("Failing themes: ${failingThemes.size} (${percentage(failingThemes.size, results.size)}%)")
        report.appendLine()

        // WCAG Criteria
        report.appendLine("WCAG AA CRITERIA")
        report.appendLine("-" .repeat(80))
        report.appendLine("Normal text:     4.5:1 minimum contrast ratio")
        report.appendLine("Large text:      3.0:1 minimum contrast ratio")
        report.appendLine("UI components:   3.0:1 minimum contrast ratio")
        report.appendLine()

        // Detailed results for each theme
        report.appendLine("DETAILED RESULTS")
        report.appendLine("=" .repeat(80))
        report.appendLine()

        results.forEach { result ->
            val status = if (result.overallPass) "✓ PASS" else "✗ FAIL"
            report.appendLine("Theme: ${result.themeName}")
            report.appendLine("File: ${result.themeFile}")
            report.appendLine("Status: $status")
            report.appendLine("Checks: ${result.passCount} passed, ${result.failureCount} failed")
            report.appendLine()

            if (!result.overallPass) {
                report.appendLine("  FAILURES:")
                val failures = result.checks.filter { !it.passes }
                failures.forEach { check ->
                    report.appendLine("    • ${check.description}")
                    report.appendLine("      Foreground: ${check.foreground}")
                    report.appendLine("      Background: ${check.background}")
                    report.appendLine("      Ratio: ${check.ratio}:1 (required: ${check.requirement}:1)")
                    report.appendLine("      Deficit: ${round((check.requirement - check.ratio) * 100) / 100}:1")

                    // Suggest fix
                    val suggestion = suggestColorAdjustment(
                        check.foreground,
                        check.background,
                        check.requirement
                    )
                    if (suggestion != null) {
                        report.appendLine("      Suggested fix: ${suggestion.description} ${check.foreground} → ${suggestion.suggestedColor}")
                        report.appendLine("      New ratio: ${suggestion.newRatio}:1")
                    }
                    report.appendLine()
                }
            }

            // Show category breakdown
            val categoryBreakdown = result.checks.groupBy { it.category }
            report.appendLine("  CATEGORY BREAKDOWN:")
            categoryBreakdown.forEach { (category, checks) ->
                val categoryPasses = checks.count { it.passes }
                val categoryFails = checks.count { !it.passes }
                val categoryStatus = if (categoryFails == 0) "✓" else "✗"
                report.appendLine("    $categoryStatus $category: $categoryPasses passed, $categoryFails failed")
            }
            report.appendLine()
            report.appendLine("-" .repeat(80))
            report.appendLine()
        }

        // Recommendations
        report.appendLine("RECOMMENDATIONS")
        report.appendLine("=" .repeat(80))
        report.appendLine()

        if (failingThemes.isEmpty()) {
            report.appendLine("All themes meet WCAG AA accessibility standards. Excellent work!")
        } else {
            report.appendLine("The following themes require attention:")
            report.appendLine()
            failingThemes.forEach { result ->
                report.appendLine("${result.themeName} (${result.themeFile}):")
                report.appendLine("  - ${result.failureCount} contrast issue(s) detected")

                val primaryFailures = result.checks.filter { !it.passes && it.category == "Primary" }
                if (primaryFailures.isNotEmpty()) {
                    report.appendLine("  - ⚠ CRITICAL: Primary text contrast issues")
                }

                report.appendLine()
            }

            report.appendLine("Priority order for fixes:")
            report.appendLine("1. Primary text (foreground/background) - affects all text")
            report.appendLine("2. UI components (cursor, selection) - affects usability")
            report.appendLine("3. Console colors - affects syntax highlighting and terminal output")
        }

        report.appendLine()
        report.appendLine("=" .repeat(80))
        report.appendLine("END OF REPORT")
        report.appendLine("=" .repeat(80))

        return report.toString()
    }

    /**
     * Generates a concise summary report.
     */
    fun generateSummaryReport(results: List<ThemeAuditResult>): String {
        val report = StringBuilder()

        report.appendLine("WCAG AA Accessibility Audit Summary")
        report.appendLine("=" .repeat(60))
        report.appendLine()

        results.sortedBy { it.themeName }.forEach { result ->
            val status = if (result.overallPass) "✓" else "✗"
            val statusText = if (result.overallPass) "PASS" else "FAIL"
            report.appendLine("$status ${result.themeName.padEnd(30)} $statusText (${result.passCount}/${result.passCount + result.failureCount})")
        }

        report.appendLine()
        report.appendLine("Summary: ${results.count { it.overallPass }}/${results.size} themes passing")

        return report.toString()
    }

    private fun percentage(value: Int, total: Int): Int {
        return if (total == 0) 0 else (value * 100 / total)
    }
}
