#!/usr/bin/env kotlin

/**
 * Simple verification script for SyntaxColorInference implementation.
 * This script validates that the implementation meets the basic requirements.
 */

import java.io.File

fun main() {
    println("Verifying SyntaxColorInference Implementation...")
    println("=" * 60)

    val sourceFile = File("src/main/kotlin/mapping/SyntaxColorInference.kt")
    if (!sourceFile.exists()) {
        println("ERROR: SyntaxColorInference.kt not found!")
        return
    }

    val content = sourceFile.readText()

    // Check Phase 1: Color Classification
    println("\nPhase 1: Color Classification")
    val hasClassifyColor = content.contains("fun classifyColor") || content.contains("private fun classifyColor")
    val hasLuminanceCalc = content.contains("calculateLuminance")
    val hasLuminanceClass = content.contains("LuminanceClass")

    println("  ✓ classifyColor method: ${if (hasClassifyColor) "FOUND" else "MISSING"}")
    println("  ✓ Luminance calculation: ${if (hasLuminanceCalc) "FOUND" else "MISSING"}")
    println("  ✓ LuminanceClass enum: ${if (hasLuminanceClass) "FOUND" else "MISSING"}")

    // Check Phase 2: Semantic Mapping
    println("\nPhase 2: Semantic Mapping")
    val hasInferSyntaxColors = content.contains("fun inferSyntaxColors")
    val usesColorMappingConfig = content.contains("ColorMappingConfig")
    val hasSyntaxRule = content.contains("SyntaxRule")

    println("  ✓ inferSyntaxColors method: ${if (hasInferSyntaxColors) "FOUND" else "MISSING"}")
    println("  ✓ ColorMappingConfig usage: ${if (usesColorMappingConfig) "FOUND" else "MISSING"}")
    println("  ✓ SyntaxRule handling: ${if (hasSyntaxRule) "FOUND" else "MISSING"}")

    // Check Phase 3: Edge Case Handling
    println("\nPhase 3: Edge Case Handling")
    val hasMonochromeDetection = content.contains("detectMonochrome")
    val hasContrastAnalysis = content.contains("analyzeContrast")
    val hasFontStyleFallback = content.contains("determineFontStyleForMonochrome")
    val hasContrastAdjustment = content.contains("adjustForLowContrast") && content.contains("adjustForHighContrast")

    println("  ✓ Monochrome detection: ${if (hasMonochromeDetection) "FOUND" else "MISSING"}")
    println("  ✓ Contrast analysis: ${if (hasContrastAnalysis) "FOUND" else "MISSING"}")
    println("  ✓ Font style fallback: ${if (hasFontStyleFallback) "FOUND" else "MISSING"}")
    println("  ✓ Contrast adjustment: ${if (hasContrastAdjustment) "FOUND" else "MISSING"}")

    // Check Data Classes
    println("\nData Classes")
    val hasColorClassification = content.contains("data class ColorClassification")
    val hasSyntaxColor = content.contains("data class SyntaxColor")
    val hasContrastLevel = content.contains("enum class ContrastLevel")
    val hasPaletteAnalysis = content.contains("data class PaletteAnalysis")

    println("  ✓ ColorClassification: ${if (hasColorClassification) "FOUND" else "MISSING"}")
    println("  ✓ SyntaxColor: ${if (hasSyntaxColor) "FOUND" else "MISSING"}")
    println("  ✓ ContrastLevel: ${if (hasContrastLevel) "FOUND" else "MISSING"}")
    println("  ✓ PaletteAnalysis: ${if (hasPaletteAnalysis) "FOUND" else "MISSING"}")

    // Check test resources
    println("\nTest Resources")
    val testResourcesDir = File("src/test/resources/test-schemes")
    if (testResourcesDir.exists()) {
        val testSchemes = testResourcesDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()
        println("  ✓ Test schemes directory: FOUND")
        println("  ✓ Test scheme count: ${testSchemes.size}")
        testSchemes.forEach { println("    - ${it.name}") }
    } else {
        println("  ✗ Test schemes directory: NOT FOUND")
    }

    // Check test file
    println("\nTest File")
    val testFile = File("src/test/kotlin/mapping/SyntaxColorInferenceTest.kt")
    if (testFile.exists()) {
        val testContent = testFile.readText()
        val testCount = testContent.split("@Test").size - 1
        println("  ✓ SyntaxColorInferenceTest.kt: FOUND")
        println("  ✓ Number of tests: $testCount")
    } else {
        println("  ✗ SyntaxColorInferenceTest.kt: NOT FOUND")
    }

    println("\n" + "=" * 60)
    println("Verification Complete!")
}

main()
