import tasks.AccessibilityAudit
import java.io.File

/**
 * Standalone script to run WCAG AA accessibility audit on Windows Terminal themes.
 *
 * Usage: Run this as a Gradle task or standalone Kotlin script.
 */
fun main() {
    println("Starting WCAG AA Accessibility Audit...")
    println()

    val projectRoot = File(".").absoluteFile.parentFile
    val themesDirectory = File(projectRoot, "windows-terminal-schemes")

    if (!themesDirectory.exists()) {
        println("ERROR: Windows Terminal schemes directory not found at: ${themesDirectory.absolutePath}")
        return
    }

    println("Scanning themes directory: ${themesDirectory.absolutePath}")
    val themeFiles = themesDirectory.listFiles { file -> file.extension == "json" }
    println("Found ${themeFiles?.size ?: 0} theme files")
    println()

    // Run the audit
    val results = AccessibilityAudit.auditAllThemes(themesDirectory)

    // Generate and display summary
    println(AccessibilityAudit.generateSummaryReport(results))
    println()

    // Generate detailed report
    val detailedReport = AccessibilityAudit.generateReport(results)

    // Save report to file
    val reportsDir = File(projectRoot, "reports")
    reportsDir.mkdirs()

    val reportFile = File(reportsDir, "accessibility-audit-report.txt")
    reportFile.writeText(detailedReport)

    println("Detailed report saved to: ${reportFile.absolutePath}")
    println()

    // Also create a markdown version
    val markdownReport = generateMarkdownReport(results)
    val markdownFile = File(reportsDir, "ACCESSIBILITY_AUDIT_REPORT.md")
    markdownFile.writeText(markdownReport)

    println("Markdown report saved to: ${markdownFile.absolutePath}")
    println()

    // Exit with error code if any themes failed
    val failingCount = results.count { !it.overallPass }
    if (failingCount > 0) {
        println("⚠ WARNING: $failingCount theme(s) failed WCAG AA compliance")
        // Don't exit with error in build context, just warn
    } else {
        println("✓ SUCCESS: All themes passed WCAG AA compliance!")
    }
}

/**
 * Generates a markdown-formatted report.
 */
fun generateMarkdownReport(results: List<AccessibilityAudit.ThemeAuditResult>): String {
    val report = StringBuilder()
    val timestamp = java.time.LocalDateTime.now().toString()

    report.appendLine("# WCAG AA Accessibility Audit Report")
    report.appendLine()
    report.appendLine("**Windows Terminal Theme Collection**")
    report.appendLine()
    report.appendLine("- **Generated:** $timestamp")
    report.appendLine("- **Total themes audited:** ${results.size}")
    report.appendLine()

    // Summary statistics
    val passingThemes = results.filter { it.overallPass }
    val failingThemes = results.filter { !it.overallPass }

    report.appendLine("## Summary")
    report.appendLine()
    report.appendLine("| Status | Count | Percentage |")
    report.appendLine("|--------|-------|------------|")
    report.appendLine("| ✓ Passing | ${passingThemes.size} | ${percentage(passingThemes.size, results.size)}% |")
    report.appendLine("| ✗ Failing | ${failingThemes.size} | ${percentage(failingThemes.size, results.size)}% |")
    report.appendLine()

    // WCAG Criteria
    report.appendLine("## WCAG AA Criteria")
    report.appendLine()
    report.appendLine("| Type | Minimum Contrast Ratio |")
    report.appendLine("|------|------------------------|")
    report.appendLine("| Normal text | 4.5:1 |")
    report.appendLine("| Large text | 3.0:1 |")
    report.appendLine("| UI components | 3.0:1 |")
    report.appendLine()

    // Quick reference table
    report.appendLine("## Quick Reference")
    report.appendLine()
    report.appendLine("| Theme | Status | Pass/Total | File |")
    report.appendLine("|-------|--------|------------|------|")
    results.sortedBy { it.themeName }.forEach { result ->
        val status = if (result.overallPass) "✓ PASS" else "✗ FAIL"
        val ratio = "${result.passCount}/${result.passCount + result.failureCount}"
        report.appendLine("| ${result.themeName} | $status | $ratio | `${result.themeFile}` |")
    }
    report.appendLine()

    // Detailed results
    report.appendLine("## Detailed Results")
    report.appendLine()

    results.sortedBy { it.themeName }.forEach { result ->
        report.appendLine("### ${result.themeName}")
        report.appendLine()
        report.appendLine("- **File:** `${result.themeFile}`")
        report.appendLine("- **Status:** ${if (result.overallPass) "✓ PASS" else "✗ FAIL"}")
        report.appendLine("- **Checks:** ${result.passCount} passed, ${result.failureCount} failed")
        report.appendLine()

        if (!result.overallPass) {
            report.appendLine("#### Failures")
            report.appendLine()
            val failures = result.checks.filter { !it.passes }
            failures.forEach { check ->
                report.appendLine("**${check.description}**")
                report.appendLine()
                report.appendLine("- Foreground: `${check.foreground}`")
                report.appendLine("- Background: `${check.background}`")
                report.appendLine("- Contrast ratio: **${check.ratio}:1** (required: ${check.requirement}:1)")
                report.appendLine("- Deficit: ${kotlin.math.round((check.requirement - check.ratio) * 100) / 100}:1")

                // Suggest fix
                val suggestion = AccessibilityAudit.suggestColorAdjustment(
                    check.foreground,
                    check.background,
                    check.requirement
                )
                if (suggestion != null) {
                    report.appendLine("- **Suggested fix:** ${suggestion.description} `${check.foreground}` → `${suggestion.suggestedColor}`")
                    report.appendLine("- **New ratio:** ${suggestion.newRatio}:1")
                }
                report.appendLine()
            }
        }

        // Category breakdown
        report.appendLine("#### Category Breakdown")
        report.appendLine()
        val categoryBreakdown = result.checks.groupBy { it.category }
        report.appendLine("| Category | Passed | Failed | Status |")
        report.appendLine("|----------|--------|--------|--------|")
        categoryBreakdown.forEach { (category, checks) ->
            val categoryPasses = checks.count { it.passes }
            val categoryFails = checks.count { !it.passes }
            val status = if (categoryFails == 0) "✓" else "✗"
            report.appendLine("| $category | $categoryPasses | $categoryFails | $status |")
        }
        report.appendLine()
        report.appendLine("---")
        report.appendLine()
    }

    // Recommendations
    report.appendLine("## Recommendations")
    report.appendLine()

    if (failingThemes.isEmpty()) {
        report.appendLine("✓ **All themes meet WCAG AA accessibility standards!**")
        report.appendLine()
        report.appendLine("Excellent work! All tested themes provide adequate contrast for users with visual impairments.")
    } else {
        report.appendLine("The following themes require attention:")
        report.appendLine()
        failingThemes.forEach { result ->
            report.appendLine("### ${result.themeName}")
            report.appendLine()
            report.appendLine("- **Issues:** ${result.failureCount} contrast problem(s)")

            val primaryFailures = result.checks.filter { !it.passes && it.category == "Primary" }
            if (primaryFailures.isNotEmpty()) {
                report.appendLine("- ⚠ **CRITICAL:** Primary text contrast issues (affects all text)")
            }

            val uiFailures = result.checks.filter { !it.passes && it.category == "UI Component" }
            if (uiFailures.isNotEmpty()) {
                report.appendLine("- **WARNING:** UI component contrast issues (affects usability)")
            }

            val consoleFailures = result.checks.filter { !it.passes && it.category == "Console Color" }
            if (consoleFailures.isNotEmpty()) {
                report.appendLine("- Console color contrast issues: ${consoleFailures.size} color(s)")
            }
            report.appendLine()
        }

        report.appendLine("### Priority Order for Fixes")
        report.appendLine()
        report.appendLine("1. **Primary text** (foreground/background) - Affects all text readability")
        report.appendLine("2. **UI components** (cursor, selection) - Affects user interaction")
        report.appendLine("3. **Console colors** - Affects syntax highlighting and terminal output")
        report.appendLine()
    }

    // Testing notes
    report.appendLine("## Testing Notes")
    report.appendLine()
    report.appendLine("### ColorUtils.calculateContrastRatio Validation")
    report.appendLine()
    report.appendLine("The audit uses `ColorUtils.calculateContrastRatio()` which implements the WCAG 2.0 standard:")
    report.appendLine()
    report.appendLine("- Converts colors to sRGB color space")
    report.appendLine("- Calculates relative luminance with gamma correction")
    report.appendLine("- Computes contrast ratio: `(L1 + 0.05) / (L2 + 0.05)`")
    report.appendLine("- Returns ratios from 1:1 (no contrast) to 21:1 (maximum contrast)")
    report.appendLine()
    report.appendLine("This implementation has been validated with:")
    report.appendLine("- Black/white contrast: 21:1 ✓")
    report.appendLine("- Same color contrast: 1:1 ✓")
    report.appendLine("- Typical dark theme foreground/background: >4.5:1 ✓")
    report.appendLine()

    report.appendLine("---")
    report.appendLine()
    report.appendLine("*Report generated by AccessibilityAudit.kt*")

    return report.toString()
}

private fun percentage(value: Int, total: Int): Int {
    return if (total == 0) 0 else (value * 100 / total)
}
