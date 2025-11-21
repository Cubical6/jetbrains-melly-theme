#!/usr/bin/env kotlin

/**
 * Standalone WCAG AA Accessibility Audit Script
 *
 * This script can be run directly with: kotlinc -script accessibility-audit.kts
 * Or as an executable: ./accessibility-audit.kts (if kotlinc is in PATH)
 */

import java.io.File
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.abs

// Inline ColorUtils for standalone execution
object ColorUtils {
    fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        val cleanHex = hex.removePrefix("#")
        val r = cleanHex.substring(0, 2).toInt(16)
        val g = cleanHex.substring(2, 4).toInt(16)
        val b = cleanHex.substring(4, 6).toInt(16)
        return Triple(r, g, b)
    }

    fun rgbToHex(r: Int, g: Int, b: Int): String {
        return "#%02x%02x%02x".format(r, g, b)
    }

    fun calculateRelativeLuminance(hexColor: String): Double {
        val (r, g, b) = hexToRgb(hexColor)
        val sR = r / 255.0
        val sG = g / 255.0
        val sB = b / 255.0

        val rLin = if (sR <= 0.03928) sR / 12.92 else ((sR + 0.055) / 1.055).pow(2.4)
        val gLin = if (sG <= 0.03928) sG / 12.92 else ((sG + 0.055) / 1.055).pow(2.4)
        val bLin = if (sB <= 0.03928) sB / 12.92 else ((sB + 0.055) / 1.055).pow(2.4)

        return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin
    }

    fun calculateContrastRatio(color1: String, color2: String): Double {
        val l1 = calculateRelativeLuminance(color1)
        val l2 = calculateRelativeLuminance(color2)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    fun lighten(hex: String, percentage: Double): String {
        val (r, g, b) = hexToRgb(hex)
        val newR = (r + (255 - r) * percentage).toInt().coerceIn(0, 255)
        val newG = (g + (255 - g) * percentage).toInt().coerceIn(0, 255)
        val newB = (b + (255 - b) * percentage).toInt().coerceIn(0, 255)
        return rgbToHex(newR, newG, newB)
    }

    fun darken(hex: String, percentage: Double): String {
        val (r, g, b) = hexToRgb(hex)
        val newR = (r * (1 - percentage)).toInt().coerceIn(0, 255)
        val newG = (g * (1 - percentage)).toInt().coerceIn(0, 255)
        val newB = (b * (1 - percentage)).toInt().coerceIn(0, 255)
        return rgbToHex(newR, newG, newB)
    }
}

// Simple JSON parser for theme files
fun parseTheme(jsonText: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    val lines = jsonText.lines()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.contains(":")) {
            val parts = trimmed.split(":")
            if (parts.size >= 2) {
                val key = parts[0].trim().removeSurrounding("\"")
                val value = parts[1].trim().removeSurrounding("\"", ",").removeSurrounding("\"")
                if (key.isNotEmpty() && value.isNotEmpty() && value.startsWith("#")) {
                    map[key] = value
                }
            }
        }
    }

    return map
}

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

fun checkContrast(
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

fun auditTheme(themeFile: File): ThemeAuditResult {
    val theme = parseTheme(themeFile.readText())

    val themeName = theme["name"] ?: themeFile.nameWithoutExtension
    val background = theme["background"] ?: "#000000"
    val foreground = theme["foreground"] ?: "#ffffff"
    val cursorColor = theme["cursorColor"] ?: foreground
    val selectionBg = theme["selectionBackground"] ?: background

    val checks = mutableListOf<ContrastCheck>()

    // Primary checks
    checks.add(checkContrast(
        "Primary text (foreground on background)",
        foreground, background, 4.5, "Primary"
    ))

    checks.add(checkContrast(
        "Cursor visibility (cursor on background)",
        cursorColor, background, 3.0, "UI Component"
    ))

    checks.add(checkContrast(
        "Selection visibility (foreground on selection)",
        foreground, selectionBg, 4.5, "Primary"
    ))

    // Console colors
    val colors = listOf(
        "black", "red", "green", "yellow", "blue", "purple", "cyan", "white",
        "brightBlack", "brightRed", "brightGreen", "brightYellow",
        "brightBlue", "brightPurple", "brightCyan", "brightWhite"
    )

    colors.forEach { colorName ->
        theme[colorName]?.let { color ->
            checks.add(checkContrast(
                "Console $colorName on background",
                color, background, 4.5, "Console Color"
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

fun suggestColorAdjustment(foreground: String, background: String, targetRatio: Double): String? {
    val currentRatio = ColorUtils.calculateContrastRatio(foreground, background)
    if (currentRatio >= targetRatio) return null

    val bgLuminance = ColorUtils.calculateRelativeLuminance(background)
    val isDarkBackground = bgLuminance < 0.5

    var bestColor = foreground
    var bestRatio = currentRatio

    for (i in 1..20) {
        val testColor = if (isDarkBackground) {
            ColorUtils.lighten(foreground, 0.05 * i)
        } else {
            ColorUtils.darken(foreground, 0.05 * i)
        }

        val testRatio = ColorUtils.calculateContrastRatio(testColor, background)
        if (testRatio > bestRatio) {
            bestRatio = testRatio
            bestColor = testColor
        }

        if (testRatio >= targetRatio) break
    }

    return if (bestRatio > currentRatio) {
        "$bestColor (${round(bestRatio * 100) / 100}:1)"
    } else {
        null
    }
}

// Main execution
fun main() {
    println("=" .repeat(80))
    println("WCAG AA ACCESSIBILITY AUDIT REPORT")
    println("Windows Terminal Theme Collection")
    println("=" .repeat(80))
    println()

    val projectRoot = File(".").absoluteFile
    val themesDir = File(projectRoot, "windows-terminal-schemes")

    if (!themesDir.exists()) {
        println("ERROR: windows-terminal-schemes directory not found")
        return
    }

    val themeFiles = themesDir.listFiles { f -> f.extension == "json" }?.sortedBy { it.name } ?: emptyArray()
    println("Found ${themeFiles.size} theme files")
    println()

    val results = themeFiles.map { auditTheme(it) }

    // Summary
    val passing = results.count { it.overallPass }
    val failing = results.count { !it.overallPass }

    println("SUMMARY")
    println("-" .repeat(80))
    println("Passing themes: $passing (${passing * 100 / results.size}%)")
    println("Failing themes: $failing (${failing * 100 / results.size}%)")
    println()

    // Quick reference
    println("QUICK REFERENCE")
    println("-" .repeat(80))
    results.forEach { result ->
        val status = if (result.overallPass) "✓ PASS" else "✗ FAIL"
        println("${status.padEnd(8)} ${result.themeName.padEnd(30)} (${result.passCount}/${result.passCount + result.failureCount})")
    }
    println()

    // Detailed failures
    val failedThemes = results.filter { !it.overallPass }
    if (failedThemes.isNotEmpty()) {
        println("DETAILED FAILURES")
        println("=" .repeat(80))
        println()

        failedThemes.forEach { result ->
            println("Theme: ${result.themeName}")
            println("File: ${result.themeFile}")
            println("Failed checks: ${result.failureCount}")
            println()

            val failures = result.checks.filter { !it.passes }
            failures.forEach { check ->
                println("  • ${check.description}")
                println("    FG: ${check.foreground}  BG: ${check.background}")
                println("    Ratio: ${check.ratio}:1 (required: ${check.requirement}:1)")
                val suggestion = suggestColorAdjustment(check.foreground, check.background, check.requirement)
                if (suggestion != null) {
                    println("    Suggestion: $suggestion")
                }
                println()
            }
            println("-" .repeat(80))
            println()
        }
    }

    println("Audit complete!")
}

// Run main
main()
