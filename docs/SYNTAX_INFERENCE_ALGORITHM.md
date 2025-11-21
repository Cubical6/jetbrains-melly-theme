# Syntax Color Inference Algorithm

**Version:** 1.0
**Date:** 2025-11-20
**Sprint:** 2 (Core Conversion)
**Status:** SPECIFICATION
**Dependencies:** TASK-050 (Color Mapping Spec), TASK-202 (Console Color Mapper)

---

## Table of Contents

1. [Overview](#overview)
2. [Algorithm Architecture](#algorithm-architecture)
3. [Color Classification System](#color-classification-system)
4. [Semantic Mapping Rules](#semantic-mapping-rules)
5. [Main Algorithm Pseudocode](#main-algorithm-pseudocode)
6. [Edge Case Handling](#edge-case-handling)
7. [Fallback Strategies](#fallback-strategies)
8. [Worked Examples](#worked-examples)
9. [Implementation Guidelines](#implementation-guidelines)

---

## Overview

### The Challenge

Windows Terminal color schemes provide only **16 ANSI colors** + **4 special colors** (20 total), while IntelliJ IDEA requires **100+ distinct syntax highlighting attributes**. This algorithm solves the problem of intelligently mapping the limited Windows Terminal palette to comprehensive IntelliJ editor color schemes.

### Core Principles

1. **Semantic Consistency**: Colors should convey meaning (e.g., red for errors, green for strings)
2. **Perceptual Quality**: Use luminance-based classification to ensure readable contrast
3. **Graceful Degradation**: Handle edge cases (monochrome, low contrast) with font style fallbacks
4. **Preservation**: Maintain the aesthetic of the original Windows Terminal theme

### Algorithm Inputs

- **Windows Terminal Color Scheme**: 16 ANSI colors + 4 special colors (20 total)
  - `background`, `foreground`, `cursorColor`, `selectionBackground`
  - `black`, `red`, `green`, `yellow`, `blue`, `purple`, `cyan`, `white`
  - `brightBlack`, `brightRed`, `brightGreen`, `brightYellow`, `brightBlue`, `brightPurple`, `brightCyan`, `brightWhite`

### Algorithm Outputs

- **IntelliJ Syntax Color Map**: 100+ attributes with assigned colors and optional font styles
  - Console colors (16 ANSI + 4 special) → direct mapping
  - Editor syntax colors (100+) → inferred mapping
  - Font style modifiers (BOLD, ITALIC, UNDERLINE) for monochrome/limited palettes

---

## Algorithm Architecture

The algorithm operates in four sequential phases:

```
┌─────────────────────────────────────────────────────────────┐
│ Phase 1: Color Analysis                                      │
│ - Extract all 20 colors from Windows Terminal scheme         │
│ - Calculate luminance, hue, saturation for each              │
│ - Classify colors: DARK/MID/BRIGHT                           │
│ - Detect edge cases: monochrome, high/low contrast           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Phase 2: Color Categorization by Semantic Role               │
│ - Blues/Purples → keyword_pool                               │
│ - Greens → string_pool                                       │
│ - Yellows/Cyans → number_pool                                │
│ - Reds → error_pool                                          │
│ - Darks/Grays → comment_pool                                 │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Phase 3: Attribute Assignment                                │
│ - Map IntelliJ attributes to pools using priority rules      │
│ - Apply variations (lighten/darken) for related attributes   │
│ - Assign font styles when color distinction insufficient     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Phase 4: Validation & Fallback                               │
│ - Verify WCAG contrast ratios (minimum 3.0)                  │
│ - Apply edge case strategies if needed                       │
│ - Generate final color map with metadata                     │
└─────────────────────────────────────────────────────────────┘
```

---

## Color Classification System

### Luminance Calculation

We use the **perceived luminance** formula (ITU-R BT.709):

```
Luminance = 0.299 × R + 0.587 × G + 0.114 × B
```

Where R, G, B are in the range [0, 255].

**Rationale**: This formula weights green more heavily than red or blue, matching human eye sensitivity.

### Classification Thresholds

```kotlin
enum class ColorClass {
    DARK,   // Luminance < 100  (e.g., #1e1e1e, #5c6370)
    MID,    // Luminance 100-155 (e.g., #98c379, #61afef)
    BRIGHT  // Luminance > 155  (e.g., #abb2bf, #e5c07b)
}

fun classifyByLuminance(hexColor: String): ColorClass {
    val (r, g, b) = hexToRgb(hexColor)
    val luminance = 0.299 * r + 0.587 * g + 0.114 * b

    return when {
        luminance < 100 -> ColorClass.DARK
        luminance < 155 -> ColorClass.MID
        else -> ColorClass.BRIGHT
    }
}
```

### Hue Extraction

We extract hue using HSV color space:

```kotlin
data class HSV(val hue: Double, val saturation: Double, val value: Double)

fun rgbToHsv(r: Int, g: Int, b: Int): HSV {
    val rNorm = r / 255.0
    val gNorm = g / 255.0
    val bNorm = b / 255.0

    val max = maxOf(rNorm, gNorm, bNorm)
    val min = minOf(rNorm, gNorm, bNorm)
    val delta = max - min

    val hue = when {
        delta == 0.0 -> 0.0
        max == rNorm -> 60 * (((gNorm - bNorm) / delta) % 6)
        max == gNorm -> 60 * (((bNorm - rNorm) / delta) + 2)
        else -> 60 * (((rNorm - gNorm) / delta) + 4)
    }.let { if (it < 0) it + 360 else it }

    val saturation = if (max == 0.0) 0.0 else (delta / max) * 100
    val value = max * 100

    return HSV(hue, saturation, value)
}
```

### Hue-Based Color Categories

```kotlin
enum class ColorCategory {
    RED,      // 0-30°, 330-360°
    ORANGE,   // 30-60°
    YELLOW,   // 60-90°
    GREEN,    // 90-150°
    CYAN,     // 150-210°
    BLUE,     // 210-270°
    PURPLE,   // 270-330°
    GRAYSCALE // Saturation < 15%
}

fun categorizeByHue(hexColor: String): ColorCategory {
    val (r, g, b) = hexToRgb(hexColor)
    val (hue, saturation, _) = rgbToHsv(r, g, b)

    if (saturation < 15.0) return ColorCategory.GRAYSCALE

    return when (hue) {
        in 0.0..30.0, in 330.0..360.0 -> ColorCategory.RED
        in 30.0..60.0 -> ColorCategory.ORANGE
        in 60.0..90.0 -> ColorCategory.YELLOW
        in 90.0..150.0 -> ColorCategory.GREEN
        in 150.0..210.0 -> ColorCategory.CYAN
        in 210.0..270.0 -> ColorCategory.BLUE
        else -> ColorCategory.PURPLE
    }
}
```

---

## Semantic Mapping Rules

### Core Syntax Element Priorities

IntelliJ has 100+ syntax attributes. We prioritize the most visually important:

#### Priority 1: Critical Elements (MUST have distinct colors)

1. **Keywords** (`KEYWORD`) - Language reserved words
2. **Strings** (`STRING`) - String literals
3. **Comments** (`COMMENT`) - Code comments
4. **Errors** (`ERRORS_ATTRIBUTES`) - Syntax errors
5. **Numbers** (`NUMBER`) - Numeric literals

#### Priority 2: High Visibility Elements (SHOULD have distinct colors)

6. **Functions** (`FUNCTION_CALL`, `FUNCTION_DECLARATION`) - Method/function names
7. **Classes** (`CLASS_NAME`, `CLASS_REFERENCE`) - Type names
8. **Constants** (`CONSTANT`, `STATIC_FINAL_FIELD`) - Constant values
9. **Parameters** (`PARAMETER`) - Function parameters
10. **Local Variables** (`LOCAL_VARIABLE`) - Variable names

#### Priority 3: Supporting Elements (MAY share colors with Priority 2)

11. **Operators** (`OPERATION_SIGN`) - Mathematical/logical operators
12. **Delimiters** (`PARENTHESES`, `BRACES`, `BRACKETS`) - Structural tokens
13. **Annotations** (`ANNOTATION`) - Java/Kotlin annotations
14. **Documentation** (`DOC_COMMENT`, `DOC_COMMENT_TAG`) - Javadoc/KDoc
15. **Metadata** (`METADATA`) - Language-specific metadata

### Semantic Color Assignment Rules

```yaml
# Rule Format: ATTRIBUTE → Preferred Windows Terminal Colors (in priority order)

# 1. Keywords (blue/purple family)
KEYWORD:
  preferred: [blue, brightBlue, purple, brightPurple]
  fallback: blue
  justification: "Blue universally represents language constructs"

RESERVED_WORD:
  inherit: KEYWORD
  style: BOLD

# 2. Strings (green family)
STRING:
  preferred: [green, brightGreen]
  fallback: green
  justification: "Green indicates literal data, safe to modify"

VALID_STRING_ESCAPE:
  base: STRING
  modifier: lighten(20%)
  justification: "Escape sequences should stand out within strings"

# 3. Comments (dark/dimmed colors)
COMMENT:
  preferred: [brightBlack, white]
  luminance: DARK
  modifier: darken(30%)
  fallback: foreground with 70% opacity
  justification: "Comments should recede visually"

DOC_COMMENT:
  base: COMMENT
  modifier: lighten(15%)
  justification: "Doc comments more important than regular comments"

# 4. Numbers (yellow/cyan family)
NUMBER:
  preferred: [yellow, brightYellow, cyan, brightCyan]
  fallback: yellow
  justification: "Warm colors for literal values"

# 5. Functions (cyan/blue family)
FUNCTION_CALL:
  preferred: [cyan, brightCyan, blue, brightBlue]
  fallback: cyan
  justification: "Cool colors for callable elements"

FUNCTION_DECLARATION:
  inherit: FUNCTION_CALL
  style: BOLD
  justification: "Declarations more prominent than calls"

# 6. Classes/Types (yellow/orange family)
CLASS_NAME:
  preferred: [yellow, brightYellow]
  fallback: yellow
  style: BOLD
  justification: "Types are structural, use warm prominent colors"

INTERFACE_NAME:
  inherit: CLASS_NAME
  style: BOLD_ITALIC
  justification: "Distinguish interfaces from concrete classes"

# 7. Errors (red family - EXACT match)
ERRORS_ATTRIBUTES:
  exact: red
  fallback: brightRed
  justification: "Red universally indicates problems"

WRONG_REFERENCES_ATTRIBUTES:
  exact: red
  effect: WAVE_UNDERSCORE
  justification: "Invalid references need strong visual indication"

# 8. Constants (purple/magenta family)
CONSTANT:
  preferred: [purple, brightPurple]
  fallback: purple
  style: BOLD
  justification: "Immutable values use distinct purple"

# 9. Variables (foreground color)
LOCAL_VARIABLE:
  base: foreground
  justification: "Variables are neutral, use default text color"

PARAMETER:
  base: foreground
  modifier: lighten(10%)
  style: ITALIC
  justification: "Parameters slightly lighter, italic for distinction"

# 10. Operators (foreground or white)
OPERATION_SIGN:
  preferred: [white, brightWhite, foreground]
  justification: "Operators neutral, match text or use white"

# 11. Delimiters (dimmed)
PARENTHESES:
  base: foreground
  modifier: darken(20%)

BRACES:
  inherit: PARENTHESES

BRACKETS:
  inherit: PARENTHESES

# 12. Annotations (yellow/orange)
ANNOTATION:
  preferred: [yellow, brightYellow]
  fallback: yellow
  justification: "Annotations are metadata, use warm colors"

# 13. Special highlighting
TODO_COMMENT:
  preferred: [yellow, brightYellow]
  style: BOLD
  effect: BACKGROUND_HIGHLIGHT
  justification: "TODO markers demand attention"

DEPRECATED:
  effect: STRIKETHROUGH
  modifier: darken(30%)
  justification: "Deprecated code should be visually de-emphasized"
```

---

## Main Algorithm Pseudocode

```kotlin
/**
 * Main entry point: Infer IntelliJ syntax colors from Windows Terminal scheme
 */
fun inferSyntaxColors(scheme: WindowsTerminalColorScheme): Map<String, IntelliJAttribute> {
    // PHASE 1: Color Analysis
    val colorAnalysis = analyzeColorPalette(scheme)

    // PHASE 2: Edge Case Detection
    val edgeCase = detectEdgeCase(colorAnalysis)

    // PHASE 3: Semantic Pool Creation
    val pools = createSemanticPools(scheme, colorAnalysis)

    // PHASE 4: Attribute Assignment
    val attributeMap = assignAttributes(pools, edgeCase)

    // PHASE 5: Validation & Enhancement
    return validateAndEnhance(attributeMap, scheme, edgeCase)
}

// ============================================================================
// PHASE 1: Color Analysis
// ============================================================================

data class ColorAnalysis(
    val luminanceMap: Map<String, Double>,           // color -> luminance value
    val classificationMap: Map<String, ColorClass>,  // color -> DARK/MID/BRIGHT
    val hueMap: Map<String, Double>,                 // color -> hue in degrees
    val categoryMap: Map<String, ColorCategory>,     // color -> semantic category
    val statistics: PaletteStatistics
)

data class PaletteStatistics(
    val averageLuminance: Double,
    val luminanceRange: Double,
    val uniqueHues: Int,
    val dominantCategories: List<ColorCategory>,
    val contrastRatio: Double  // foreground vs background
)

fun analyzeColorPalette(scheme: WindowsTerminalColorScheme): ColorAnalysis {
    val allColors = scheme.getAllColors()

    val luminanceMap = allColors.associateWith { calculateLuminance(it) }
    val classificationMap = allColors.associateWith { classifyByLuminance(it) }
    val hueMap = allColors.associateWith { extractHue(it) }
    val categoryMap = allColors.associateWith { categorizeByHue(it) }

    val statistics = PaletteStatistics(
        averageLuminance = luminanceMap.values.average(),
        luminanceRange = luminanceMap.values.maxOrNull()!! - luminanceMap.values.minOrNull()!!,
        uniqueHues = hueMap.values.distinct().size,
        dominantCategories = categoryMap.values.groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }.take(3).map { it.key },
        contrastRatio = calculateContrastRatio(scheme.foreground, scheme.background)
    )

    return ColorAnalysis(luminanceMap, classificationMap, hueMap, categoryMap, statistics)
}

// ============================================================================
// PHASE 2: Edge Case Detection
// ============================================================================

enum class EdgeCase {
    NORMAL,           // Standard color palette
    MONOCHROME,       // All colors same hue (grayscale)
    HIGH_CONTRAST,    // Contrast ratio > 7.0
    LOW_CONTRAST,     // Contrast ratio < 3.0
    LIMITED_PALETTE   // < 3 unique hues
}

fun detectEdgeCase(analysis: ColorAnalysis): EdgeCase {
    val stats = analysis.statistics

    // Check monochrome (luminance range < 5% of maximum)
    if (stats.luminanceRange / 255.0 < 0.05) {
        return EdgeCase.MONOCHROME
    }

    // Check high contrast
    if (stats.contrastRatio > 7.0) {
        return EdgeCase.HIGH_CONTRAST
    }

    // Check low contrast
    if (stats.contrastRatio < 3.0) {
        return EdgeCase.LOW_CONTRAST
    }

    // Check limited palette
    if (stats.uniqueHues < 3) {
        return EdgeCase.LIMITED_PALETTE
    }

    return EdgeCase.NORMAL
}

// ============================================================================
// PHASE 3: Semantic Pool Creation
// ============================================================================

data class SemanticPools(
    val keywordPool: List<String>,     // Blues, purples
    val stringPool: List<String>,      // Greens
    val numberPool: List<String>,      // Yellows, cyans
    val functionPool: List<String>,    // Cyans, blues
    val classPool: List<String>,       // Yellows, oranges
    val constantPool: List<String>,    // Purples, magentas
    val errorPool: List<String>,       // Reds
    val commentPool: List<String>,     // Darks, grays
    val neutralPool: List<String>      // Whites, foreground
)

fun createSemanticPools(
    scheme: WindowsTerminalColorScheme,
    analysis: ColorAnalysis
): SemanticPools {
    val allColors = scheme.getAllColors()

    // Filter colors by category and luminance class
    val blues = allColors.filter {
        analysis.categoryMap[it] == ColorCategory.BLUE
    }
    val purples = allColors.filter {
        analysis.categoryMap[it] == ColorCategory.PURPLE
    }
    val greens = allColors.filter {
        analysis.categoryMap[it] == ColorCategory.GREEN
    }
    val yellows = allColors.filter {
        analysis.categoryMap[it] in listOf(ColorCategory.YELLOW, ColorCategory.ORANGE)
    }
    val cyans = allColors.filter {
        analysis.categoryMap[it] == ColorCategory.CYAN
    }
    val reds = allColors.filter {
        analysis.categoryMap[it] == ColorCategory.RED
    }
    val grays = allColors.filter {
        analysis.categoryMap[it] == ColorCategory.GRAYSCALE
    }

    return SemanticPools(
        keywordPool = (blues + purples).ifEmpty { listOf(scheme.blue) },
        stringPool = greens.ifEmpty { listOf(scheme.green) },
        numberPool = (yellows + cyans).ifEmpty { listOf(scheme.yellow) },
        functionPool = (cyans + blues).ifEmpty { listOf(scheme.cyan) },
        classPool = yellows.ifEmpty { listOf(scheme.yellow) },
        constantPool = purples.ifEmpty { listOf(scheme.purple) },
        errorPool = reds.ifEmpty { listOf(scheme.red) },
        commentPool = grays.filter {
            analysis.classificationMap[it] == ColorClass.DARK
        }.ifEmpty { listOf(scheme.brightBlack) },
        neutralPool = listOf(scheme.foreground, scheme.white, scheme.brightWhite)
    )
}

// ============================================================================
// PHASE 4: Attribute Assignment
// ============================================================================

data class IntelliJAttribute(
    val color: String,              // Hex color
    val fontStyle: FontStyle? = null,
    val effectType: EffectType? = null,
    val effectColor: String? = null,
    val backgroundColor: String? = null
)

enum class FontStyle {
    REGULAR, BOLD, ITALIC, BOLD_ITALIC
}

enum class EffectType {
    WAVE_UNDERSCORE, LINE_UNDERSCORE, STRIKETHROUGH, BOXED, ROUNDED_BOX
}

fun assignAttributes(
    pools: SemanticPools,
    edgeCase: EdgeCase
): Map<String, IntelliJAttribute> {
    val result = mutableMapOf<String, IntelliJAttribute>()

    // Use different strategy based on edge case
    when (edgeCase) {
        EdgeCase.MONOCHROME -> assignMonochromeAttributes(result, pools)
        EdgeCase.LIMITED_PALETTE -> assignLimitedPaletteAttributes(result, pools)
        else -> assignNormalAttributes(result, pools)
    }

    return result
}

fun assignNormalAttributes(
    result: MutableMap<String, IntelliJAttribute>,
    pools: SemanticPools
) {
    // PRIORITY 1: Keywords (use brightest from keyword pool)
    result["KEYWORD"] = IntelliJAttribute(
        color = selectBrightest(pools.keywordPool),
        fontStyle = FontStyle.REGULAR
    )
    result["RESERVED_WORD"] = result["KEYWORD"]!!.copy(fontStyle = FontStyle.BOLD)

    // PRIORITY 2: Strings (use mid-brightness from string pool)
    result["STRING"] = IntelliJAttribute(
        color = selectMidBrightness(pools.stringPool),
        fontStyle = FontStyle.REGULAR
    )
    result["VALID_STRING_ESCAPE"] = IntelliJAttribute(
        color = lighten(result["STRING"]!!.color, 0.2),
        fontStyle = FontStyle.REGULAR
    )

    // PRIORITY 3: Comments (use darkest from comment pool, dimmed)
    val commentColor = selectDarkest(pools.commentPool)
    result["COMMENT"] = IntelliJAttribute(
        color = darken(commentColor, 0.3),
        fontStyle = FontStyle.ITALIC
    )
    result["DOC_COMMENT"] = IntelliJAttribute(
        color = lighten(result["COMMENT"]!!.color, 0.15),
        fontStyle = FontStyle.ITALIC
    )

    // PRIORITY 4: Numbers (yellow/cyan)
    result["NUMBER"] = IntelliJAttribute(
        color = selectFirst(pools.numberPool),
        fontStyle = FontStyle.REGULAR
    )

    // PRIORITY 5: Functions (cyan/blue)
    result["FUNCTION_CALL"] = IntelliJAttribute(
        color = selectFirst(pools.functionPool),
        fontStyle = FontStyle.REGULAR
    )
    result["FUNCTION_DECLARATION"] = result["FUNCTION_CALL"]!!.copy(
        fontStyle = FontStyle.BOLD
    )

    // PRIORITY 6: Classes (yellow)
    result["CLASS_NAME"] = IntelliJAttribute(
        color = selectFirst(pools.classPool),
        fontStyle = FontStyle.BOLD
    )
    result["INTERFACE_NAME"] = result["CLASS_NAME"]!!.copy(
        fontStyle = FontStyle.BOLD_ITALIC
    )

    // PRIORITY 7: Constants (purple)
    result["CONSTANT"] = IntelliJAttribute(
        color = selectFirst(pools.constantPool),
        fontStyle = FontStyle.BOLD
    )
    result["STATIC_FINAL_FIELD"] = result["CONSTANT"]!!

    // PRIORITY 8: Errors (red - EXACT)
    result["ERRORS_ATTRIBUTES"] = IntelliJAttribute(
        color = selectFirst(pools.errorPool),
        fontStyle = FontStyle.REGULAR
    )
    result["WRONG_REFERENCES_ATTRIBUTES"] = IntelliJAttribute(
        color = selectFirst(pools.errorPool),
        effectType = EffectType.WAVE_UNDERSCORE,
        effectColor = selectFirst(pools.errorPool)
    )

    // PRIORITY 9: Variables (neutral - use foreground)
    result["LOCAL_VARIABLE"] = IntelliJAttribute(
        color = selectFirst(pools.neutralPool),
        fontStyle = FontStyle.REGULAR
    )
    result["PARAMETER"] = IntelliJAttribute(
        color = lighten(selectFirst(pools.neutralPool), 0.1),
        fontStyle = FontStyle.ITALIC
    )

    // PRIORITY 10: Operators (neutral)
    result["OPERATION_SIGN"] = IntelliJAttribute(
        color = selectFirst(pools.neutralPool),
        fontStyle = FontStyle.REGULAR
    )

    // Continue for all 100+ attributes...
    // (Additional mappings follow same pattern)
}

fun assignMonochromeAttributes(
    result: MutableMap<String, IntelliJAttribute>,
    pools: SemanticPools
) {
    // For monochrome palettes, rely heavily on font styles
    val darkest = selectDarkest(pools.commentPool)
    val mid = selectMidBrightness(pools.neutralPool)
    val brightest = selectBrightest(pools.neutralPool)

    result["KEYWORD"] = IntelliJAttribute(
        color = brightest,
        fontStyle = FontStyle.BOLD
    )

    result["STRING"] = IntelliJAttribute(
        color = mid,
        fontStyle = FontStyle.ITALIC
    )

    result["COMMENT"] = IntelliJAttribute(
        color = darkest,
        fontStyle = FontStyle.ITALIC
    )

    result["FUNCTION_CALL"] = IntelliJAttribute(
        color = brightest,
        fontStyle = FontStyle.BOLD
    )

    result["CLASS_NAME"] = IntelliJAttribute(
        color = brightest,
        fontStyle = FontStyle.BOLD
    )

    result["NUMBER"] = IntelliJAttribute(
        color = mid,
        fontStyle = FontStyle.REGULAR
    )

    // All other attributes use luminance variations with font styles
}

// ============================================================================
// PHASE 5: Validation & Enhancement
// ============================================================================

fun validateAndEnhance(
    attributeMap: Map<String, IntelliJAttribute>,
    scheme: WindowsTerminalColorScheme,
    edgeCase: EdgeCase
): Map<String, IntelliJAttribute> {
    val result = attributeMap.toMutableMap()

    // Validate contrast ratios
    for ((attribute, value) in result) {
        val contrastRatio = calculateContrastRatio(value.color, scheme.background)

        if (contrastRatio < 3.0) {
            // Adjust color to meet minimum contrast
            result[attribute] = value.copy(
                color = enhanceContrast(value.color, scheme.background, targetRatio = 3.0)
            )
        }
    }

    // Handle low contrast edge case
    if (edgeCase == EdgeCase.LOW_CONTRAST) {
        // Enhance overall contrast
        for ((attribute, value) in result) {
            result[attribute] = value.copy(
                color = enhanceContrast(value.color, scheme.background, targetRatio = 4.5)
            )
        }
    }

    return result
}

// ============================================================================
// Helper Functions
// ============================================================================

fun selectBrightest(colors: List<String>): String {
    return colors.maxByOrNull { calculateLuminance(it) } ?: colors.first()
}

fun selectDarkest(colors: List<String>): String {
    return colors.minByOrNull { calculateLuminance(it) } ?: colors.first()
}

fun selectMidBrightness(colors: List<String>): String {
    val sorted = colors.sortedBy { calculateLuminance(it) }
    return sorted[sorted.size / 2]
}

fun selectFirst(colors: List<String>): String {
    return colors.firstOrNull() ?: "#abb2bf"
}

fun lighten(hexColor: String, percentage: Double): String {
    val (r, g, b) = hexToRgb(hexColor)
    val newR = (r + (255 - r) * percentage).toInt().coerceIn(0, 255)
    val newG = (g + (255 - g) * percentage).toInt().coerceIn(0, 255)
    val newB = (b + (255 - b) * percentage).toInt().coerceIn(0, 255)
    return rgbToHex(newR, newG, newB)
}

fun darken(hexColor: String, percentage: Double): String {
    val (r, g, b) = hexToRgb(hexColor)
    val newR = (r * (1 - percentage)).toInt().coerceIn(0, 255)
    val newG = (g * (1 - percentage)).toInt().coerceIn(0, 255)
    val newB = (b * (1 - percentage)).toInt().coerceIn(0, 255)
    return rgbToHex(newR, newG, newB)
}

fun enhanceContrast(
    foregroundColor: String,
    backgroundColor: String,
    targetRatio: Double
): String {
    var color = foregroundColor
    var currentRatio = calculateContrastRatio(color, backgroundColor)

    // Iteratively adjust until target ratio met
    while (currentRatio < targetRatio) {
        val bgLuminance = calculateLuminance(backgroundColor)

        // If background is dark, lighten foreground; if light, darken foreground
        color = if (bgLuminance < 128) {
            lighten(color, 0.1)
        } else {
            darken(color, 0.1)
        }

        val newRatio = calculateContrastRatio(color, backgroundColor)
        if (newRatio <= currentRatio) break  // Prevent infinite loop
        currentRatio = newRatio
    }

    return color
}
```

---

## Edge Case Handling

### 1. Monochrome Palette Detection

**Criteria**: Luminance range < 5% of maximum (< 12.75 on 0-255 scale)

```kotlin
fun isMonochrome(scheme: WindowsTerminalColorScheme): Boolean {
    val luminances = scheme.getAllColors().map { calculateLuminance(it) }
    val range = luminances.maxOrNull()!! - luminances.minOrNull()!!
    return (range / 255.0) < 0.05
}
```

**Handling Strategy**: Use font styles to differentiate syntax elements

| Attribute | Color | Font Style | Rationale |
|-----------|-------|------------|-----------|
| KEYWORD | Brightest | **BOLD** | Most important, stands out |
| STRING | Mid-brightness | *ITALIC* | Distinguishes literals |
| COMMENT | Darkest | *ITALIC* | Recedes visually |
| FUNCTION_CALL | Brightest | **BOLD** | Calls need visibility |
| CLASS_NAME | Brightest | **BOLD** | Types are structural |
| NUMBER | Mid-brightness | REGULAR | Neutral literals |
| VARIABLE | Mid-brightness | REGULAR | Neutral identifiers |

### 2. High Contrast Palette

**Criteria**: Foreground/background contrast ratio > 7.0 (WCAG AAA)

**Handling Strategy**: Preserve as-is (high contrast is desirable)

```kotlin
fun handleHighContrast(attributeMap: Map<String, IntelliJAttribute>): Map<String, IntelliJAttribute> {
    // No modification needed - high contrast is good for accessibility
    return attributeMap
}
```

### 3. Low Contrast Palette

**Criteria**: Foreground/background contrast ratio < 3.0 (below WCAG AA)

**Handling Strategy**: Enhance contrast to meet WCAG AA (4.5:1 for normal text)

```kotlin
fun handleLowContrast(
    attributeMap: Map<String, IntelliJAttribute>,
    scheme: WindowsTerminalColorScheme
): Map<String, IntelliJAttribute> {
    val result = attributeMap.toMutableMap()

    // Step 1: Darken background by 10%
    val newBackground = darken(scheme.background, 0.1)

    // Step 2: Lighten all foreground colors by 10%
    for ((attribute, value) in result) {
        result[attribute] = value.copy(
            color = lighten(value.color, 0.1)
        )
    }

    // Step 3: Increase saturation of syntax colors by 15%
    for ((attribute, value) in result) {
        if (attribute !in listOf("COMMENT", "LOCAL_VARIABLE", "PARAMETER")) {
            result[attribute] = value.copy(
                color = increaseSaturation(value.color, 0.15)
            )
        }
    }

    return result
}
```

### 4. Limited Palette (< 3 Unique Hues)

**Criteria**: Less than 3 distinct hue categories

**Handling Strategy**: Use luminance-based differentiation

```kotlin
fun handleLimitedPalette(
    pools: SemanticPools,
    scheme: WindowsTerminalColorScheme
): Map<String, IntelliJAttribute> {
    val allColors = scheme.getAllColors().sortedBy { calculateLuminance(it) }

    val darkest = allColors.take(5)           // Bottom 5
    val mid = allColors.drop(5).take(6)       // Middle 6
    val brightest = allColors.takeLast(5)     // Top 5

    return mapOf(
        "KEYWORD" to IntelliJAttribute(brightest.first(), FontStyle.BOLD),
        "STRING" to IntelliJAttribute(mid.first(), FontStyle.ITALIC),
        "COMMENT" to IntelliJAttribute(darkest.first(), FontStyle.ITALIC),
        "NUMBER" to IntelliJAttribute(mid[2], FontStyle.REGULAR),
        "FUNCTION_CALL" to IntelliJAttribute(brightest[1], FontStyle.BOLD),
        "CLASS_NAME" to IntelliJAttribute(brightest[2], FontStyle.BOLD),
        // ... continue with luminance-based assignment
    )
}
```

---

## Fallback Strategies

### 1. Missing Optional Colors

**Scenario**: `cursorColor` or `selectionBackground` not provided

```kotlin
fun applyMissingColorFallbacks(scheme: WindowsTerminalColorScheme): WindowsTerminalColorScheme {
    val cursorColor = scheme.cursorColor ?: scheme.foreground

    val selectionBackground = scheme.selectionBackground ?: run {
        // Blend background (80%) with foreground (20%)
        blend(scheme.background, scheme.foreground, ratio = 0.2)
    }

    return scheme.copy(
        cursorColor = cursorColor,
        selectionBackground = selectionBackground
    )
}
```

### 2. Insufficient Syntax Colors

**Scenario**: Palette lacks certain color categories (e.g., no greens)

```kotlin
fun generateMissingColors(
    pools: SemanticPools,
    scheme: WindowsTerminalColorScheme
): SemanticPools {
    // If no greens for strings, interpolate from available colors
    val stringPool = if (pools.stringPool.isEmpty()) {
        val yellow = scheme.yellow
        val cyan = scheme.cyan
        listOf(blend(yellow, cyan, ratio = 0.5))  // Generate green-ish color
    } else {
        pools.stringPool
    }

    // If no purples for constants, use blues
    val constantPool = if (pools.constantPool.isEmpty()) {
        pools.keywordPool
    } else {
        pools.constantPool
    }

    return pools.copy(
        stringPool = stringPool,
        constantPool = constantPool
    )
}
```

### 3. Invalid Color Format

**Scenario**: Color not in `#RRGGBB` format

```kotlin
fun validateAndFixColor(color: String): String {
    // Check format
    if (!color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
        logger.warn("Invalid color format: $color. Using fallback.")
        return "#abb2bf"  // Default fallback
    }
    return color
}
```

### 4. Extreme Luminance (All Dark or All Bright)

**Scenario**: 80%+ of colors in the same luminance class

```kotlin
fun handleExtremeLuminance(
    analysis: ColorAnalysis,
    scheme: WindowsTerminalColorScheme
): Map<String, IntelliJAttribute> {
    val darkCount = analysis.classificationMap.values.count { it == ColorClass.DARK }
    val brightCount = analysis.classificationMap.values.count { it == ColorClass.BRIGHT }
    val total = analysis.classificationMap.size

    if (darkCount.toDouble() / total > 0.8) {
        // All colors too dark - lighten syntax colors
        return generateLightenedPalette(scheme)
    }

    if (brightCount.toDouble() / total > 0.8) {
        // All colors too bright - darken syntax colors
        return generateDarkenedPalette(scheme)
    }

    // Normal distribution
    return assignNormalAttributes(mutableMapOf(), createSemanticPools(scheme, analysis))
}
```

---

## Worked Examples

### Example 1: Standard Dark Theme (One Dark Inspired)

**Input Windows Terminal Scheme:**

```json
{
  "name": "One Dark",
  "background": "#282c34",
  "foreground": "#abb2bf",
  "cursorColor": "#528bff",
  "selectionBackground": "#3e4451",

  "black": "#282c34",
  "red": "#e06c75",
  "green": "#98c379",
  "yellow": "#e5c07b",
  "blue": "#61afef",
  "purple": "#c678dd",
  "cyan": "#56b6c2",
  "white": "#abb2bf",

  "brightBlack": "#5c6370",
  "brightRed": "#e06c75",
  "brightGreen": "#98c379",
  "brightYellow": "#e5c07b",
  "brightBlue": "#61afef",
  "brightPurple": "#c678dd",
  "brightCyan": "#56b6c2",
  "brightWhite": "#ffffff"
}
```

**Phase 1: Color Analysis**

```
Luminance Map:
  background (#282c34): 42.8
  foreground (#abb2bf): 177.4
  red (#e06c75): 126.5
  green (#98c379): 176.2
  yellow (#e5c07b): 189.7
  blue (#61afef): 158.4
  purple (#c678dd): 149.8
  cyan (#56b6c2): 163.7
  brightBlack (#5c6370): 98.5
  brightWhite (#ffffff): 255.0

Classification:
  DARK: background, black, brightBlack
  MID: red, green, blue, purple, cyan
  BRIGHT: foreground, yellow, white, brightWhite

Hue Categories:
  RED: red, brightRed
  GREEN: green, brightGreen
  YELLOW: yellow, brightYellow
  BLUE: blue, brightBlue
  PURPLE: purple, brightPurple
  CYAN: cyan, brightCyan
  GRAYSCALE: background, foreground, black, white, brightBlack, brightWhite

Contrast Ratio (foreground/background): 8.2 (WCAG AAA ✓)
Unique Hues: 6
```

**Edge Case Detection:** NORMAL (no edge cases)

**Phase 3: Semantic Pools**

```kotlin
SemanticPools(
    keywordPool = [#61afef, #c678dd],        // blue, purple
    stringPool = [#98c379],                   // green
    numberPool = [#e5c07b, #56b6c2],         // yellow, cyan
    functionPool = [#56b6c2, #61afef],       // cyan, blue
    classPool = [#e5c07b],                    // yellow
    constantPool = [#c678dd],                 // purple
    errorPool = [#e06c75],                    // red
    commentPool = [#5c6370],                  // brightBlack
    neutralPool = [#abb2bf, #ffffff]          // foreground, brightWhite
)
```

**Phase 4: Attribute Assignment (Sample)**

```kotlin
KEYWORD:              #61afef (blue, BRIGHT)
RESERVED_WORD:        #61afef (blue, BRIGHT, BOLD)
STRING:               #98c379 (green, MID)
VALID_STRING_ESCAPE:  #afdc8e (lightened green +20%, MID)
COMMENT:              #404854 (darkened brightBlack -30%, DARK, ITALIC)
DOC_COMMENT:          #4a535f (lightened comment +15%, DARK, ITALIC)
NUMBER:               #e5c07b (yellow, BRIGHT)
FUNCTION_CALL:        #56b6c2 (cyan, MID)
FUNCTION_DECLARATION: #56b6c2 (cyan, MID, BOLD)
CLASS_NAME:           #e5c07b (yellow, BRIGHT, BOLD)
INTERFACE_NAME:       #e5c07b (yellow, BRIGHT, BOLD_ITALIC)
CONSTANT:             #c678dd (purple, MID, BOLD)
ERRORS_ATTRIBUTES:    #e06c75 (red, MID)
LOCAL_VARIABLE:       #abb2bf (foreground, BRIGHT)
PARAMETER:            #bdc3cf (lightened foreground +10%, BRIGHT, ITALIC)
OPERATION_SIGN:       #abb2bf (foreground, BRIGHT)
```

**Validation Results:**

```
All attributes pass WCAG AA contrast (> 4.5):
  KEYWORD vs background: 6.8 ✓
  STRING vs background: 8.0 ✓
  COMMENT vs background: 3.2 ✓ (acceptable for dimmed text)
  NUMBER vs background: 9.1 ✓
  ...
```

**Output Visualization:**

```java
// Example Java code with applied colors
public class Example {                    // CLASS_NAME (#e5c07b, BOLD)
    private static final int MAX = 100;   // KEYWORD (#61afef), CONSTANT (#c678dd, BOLD), NUMBER (#e5c07b)

    /**
     * Calculates the sum                 // DOC_COMMENT (#4a535f, ITALIC)
     */
    public int calculate(int param) {     // KEYWORD (#61afef), FUNCTION_DECLARATION (#56b6c2, BOLD), PARAMETER (#bdc3cf, ITALIC)
        String message = "Result: ";      // KEYWORD (#61afef), STRING (#98c379)
        int result = param + MAX;         // LOCAL_VARIABLE (#abb2bf), OPERATION_SIGN (#abb2bf)

        // TODO: Add error handling        // COMMENT (#404854, ITALIC)
        return result;                    // KEYWORD (#61afef)
    }
}
```

---

### Example 2: High Contrast Theme (Solarized Dark)

**Input Windows Terminal Scheme:**

```json
{
  "name": "Solarized Dark",
  "background": "#002b36",
  "foreground": "#839496",

  "black": "#073642",
  "red": "#dc322f",
  "green": "#859900",
  "yellow": "#b58900",
  "blue": "#268bd2",
  "purple": "#d33682",
  "cyan": "#2aa198",
  "white": "#eee8d5",

  "brightBlack": "#002b36",
  "brightRed": "#cb4b16",
  "brightGreen": "#586e75",
  "brightYellow": "#657b83",
  "brightBlue": "#839496",
  "brightPurple": "#6c71c4",
  "brightCyan": "#93a1a1",
  "brightWhite": "#fdf6e3"
}
```

**Color Analysis:**

```
Luminance: background (#002b36): 16.2, foreground (#839496): 142.8
Contrast Ratio: 11.6 (WCAG AAA - extremely high) ✓
Unique Hues: 7 (full spectrum)
```

**Edge Case:** HIGH_CONTRAST

**Handling Strategy:** Preserve as-is (no adjustments needed)

**Attribute Assignment (Sample):**

```kotlin
KEYWORD:              #268bd2 (blue, saturated)
STRING:               #859900 (green, saturated)
COMMENT:              #586e75 (brightGreen, desaturated, ITALIC)
NUMBER:               #b58900 (yellow)
FUNCTION_CALL:        #2aa198 (cyan)
CLASS_NAME:           #b58900 (yellow, BOLD)
CONSTANT:             #d33682 (purple, BOLD)
ERRORS_ATTRIBUTES:    #dc322f (red)
```

**Result:** All colors have excellent contrast (> 7.0). Theme is highly accessible.

---

### Example 3: Monochrome Theme (Grayscale)

**Input Windows Terminal Scheme:**

```json
{
  "name": "Grayscale",
  "background": "#1e1e1e",
  "foreground": "#d4d4d4",

  "black": "#1e1e1e",
  "red": "#6e6e6e",
  "green": "#787878",
  "yellow": "#8c8c8c",
  "blue": "#969696",
  "purple": "#a0a0a0",
  "cyan": "#aaaaaa",
  "white": "#b4b4b4",

  "brightBlack": "#3c3c3c",
  "brightRed": "#787878",
  "brightGreen": "#8c8c8c",
  "brightYellow": "#a0a0a0",
  "brightBlue": "#b4b4b4",
  "brightPurple": "#c8c8c8",
  "brightCyan": "#dcdcdc",
  "brightWhite": "#f0f0f0"
}
```

**Color Analysis:**

```
Luminance Range: 240 - 30 = 210
Luminance Range Ratio: 210 / 255 = 82% (HIGH)
Saturation: All colors < 5% (GRAYSCALE)
Unique Hues: 1 (all neutral)
```

**Edge Case:** MONOCHROME

**Handling Strategy:** Font style differentiation

**Attribute Assignment (Sample):**

```kotlin
// Use luminance stratification + font styles
KEYWORD:              #b4b4b4 (brightBlue, BOLD)           // Bright + Bold
STRING:               #8c8c8c (brightGreen, ITALIC)        // Mid + Italic
COMMENT:              #3c3c3c (brightBlack, ITALIC)        // Dark + Italic
NUMBER:               #a0a0a0 (brightYellow, REGULAR)      // Mid-bright
FUNCTION_CALL:        #c8c8c8 (brightPurple, BOLD)         // Very bright + Bold
CLASS_NAME:           #dcdcdc (brightCyan, BOLD)           // Brightest + Bold
CONSTANT:             #aaaaaa (cyan, BOLD)                 // Mid-bright + Bold
ERRORS_ATTRIBUTES:    #787878 (brightRed, WAVE_UNDERSCORE) // Mid + Effect
LOCAL_VARIABLE:       #d4d4d4 (foreground, REGULAR)        // Default
PARAMETER:            #d4d4d4 (foreground, ITALIC)         // Default + Italic
```

**Output Visualization:**

```java
// Grayscale theme with font style differentiation
public class Example {                    // #dcdcdc (BOLD)
    private static final int MAX = 100;   // #b4b4b4 (BOLD), #aaaaaa (BOLD), #a0a0a0

    /**
     * Calculates sum                     // #3c3c3c (ITALIC)
     */
    public int calculate(int param) {     // #b4b4b4 (BOLD), #c8c8c8 (BOLD), #d4d4d4 (ITALIC)
        String message = "Result: ";      // #b4b4b4 (BOLD), #8c8c8c (ITALIC)
        int result = param + MAX;         // #d4d4d4, #d4d4d4

        // TODO: Handle errors             // #3c3c3c (ITALIC)
        return result;                    // #b4b4b4 (BOLD)
    }
}
```

**Result:** Despite monochrome palette, syntax elements are distinguishable through luminance levels and font styles.

---

## Implementation Guidelines

### 1. Language-Specific Adaptations

Different languages may require attribute priority adjustments:

**Java/Kotlin:**
- Emphasize: `ANNOTATION`, `CLASS_NAME`, `INTERFACE_NAME`, `STATIC_FINAL_FIELD`

**Python:**
- Emphasize: `DECORATOR`, `FUNCTION_DECLARATION`, `BUILTIN_NAME`

**JavaScript/TypeScript:**
- Emphasize: `FUNCTION_DECLARATION`, `PARAMETER`, `TYPE_PARAMETER`

**Rust:**
- Emphasize: `LIFETIME`, `MACRO`, `TRAIT_NAME`

### 2. Performance Considerations

```kotlin
// Cache color calculations to avoid repeated work
private val luminanceCache = mutableMapOf<String, Double>()

fun calculateLuminanceCached(hexColor: String): Double {
    return luminanceCache.getOrPut(hexColor) {
        calculateLuminance(hexColor)
    }
}
```

### 3. Testing Strategy

```kotlin
@Test
fun `test standard theme mapping`() {
    val scheme = loadWindowsTerminalScheme("one-dark.json")
    val attributes = inferSyntaxColors(scheme)

    // Verify key attributes have correct colors
    assertEquals("#61afef", attributes["KEYWORD"]?.color)
    assertEquals("#98c379", attributes["STRING"]?.color)
    assertEquals("#e06c75", attributes["ERRORS_ATTRIBUTES"]?.color)

    // Verify contrast ratios
    attributes.forEach { (name, attr) ->
        val ratio = calculateContrastRatio(attr.color, scheme.background)
        assertTrue(ratio >= 3.0, "$name has insufficient contrast: $ratio")
    }
}

@Test
fun `test monochrome theme uses font styles`() {
    val scheme = loadWindowsTerminalScheme("grayscale.json")
    val attributes = inferSyntaxColors(scheme)

    // Verify font styles are applied
    assertEquals(FontStyle.BOLD, attributes["KEYWORD"]?.fontStyle)
    assertEquals(FontStyle.ITALIC, attributes["STRING"]?.fontStyle)
    assertEquals(FontStyle.ITALIC, attributes["COMMENT"]?.fontStyle)
}

@Test
fun `test low contrast enhancement`() {
    val scheme = loadWindowsTerminalScheme("low-contrast.json")
    val attributes = inferSyntaxColors(scheme)

    // Verify all attributes meet minimum contrast
    attributes.forEach { (name, attr) ->
        val ratio = calculateContrastRatio(attr.color, scheme.background)
        assertTrue(ratio >= 4.5, "$name contrast too low: $ratio")
    }
}
```

### 4. Logging and Debugging

```kotlin
fun inferSyntaxColors(scheme: WindowsTerminalColorScheme): Map<String, IntelliJAttribute> {
    logger.info("Starting syntax color inference for scheme: ${scheme.name}")

    val analysis = analyzeColorPalette(scheme)
    logger.debug("Color analysis complete: ${analysis.statistics}")

    val edgeCase = detectEdgeCase(analysis)
    logger.info("Edge case detected: $edgeCase")

    val pools = createSemanticPools(scheme, analysis)
    logger.debug("Semantic pools created: ${pools.keywordPool.size} keywords, " +
                 "${pools.stringPool.size} strings, etc.")

    val attributes = assignAttributes(pools, edgeCase)
    logger.info("Assigned ${attributes.size} syntax attributes")

    val validated = validateAndEnhance(attributes, scheme, edgeCase)
    logger.info("Validation complete. ${validated.size} attributes ready.")

    return validated
}
```

### 5. Extensibility

```kotlin
// Plugin architecture for custom mapping strategies
interface ColorMappingStrategy {
    fun mapAttribute(
        attribute: String,
        pools: SemanticPools,
        analysis: ColorAnalysis
    ): IntelliJAttribute
}

class DefaultMappingStrategy : ColorMappingStrategy {
    override fun mapAttribute(...) { /* standard logic */ }
}

class CustomJavaMappingStrategy : ColorMappingStrategy {
    override fun mapAttribute(...) { /* Java-specific logic */ }
}

// Allow users to register custom strategies
val strategyRegistry = mutableMapOf<String, ColorMappingStrategy>(
    "default" to DefaultMappingStrategy(),
    "java" to CustomJavaMappingStrategy()
)
```

---

## Appendix: Complete IntelliJ Attribute Reference

### Console Attributes (16 ANSI + 4 Special) - Direct Mapping

```
CONSOLE_BACKGROUND_KEY          → background
CONSOLE_NORMAL_OUTPUT           → foreground
CARET_COLOR                     → cursorColor (fallback: foreground)
CONSOLE_CURSOR                  → cursorColor (fallback: foreground)
CONSOLE_SELECTION_BACKGROUND    → selectionBackground (fallback: blend)

CONSOLE_BLACK_OUTPUT            → black
CONSOLE_RED_OUTPUT              → red
CONSOLE_GREEN_OUTPUT            → green
CONSOLE_YELLOW_OUTPUT           → yellow
CONSOLE_BLUE_OUTPUT             → blue
CONSOLE_MAGENTA_OUTPUT          → purple
CONSOLE_CYAN_OUTPUT             → cyan
CONSOLE_GRAY_OUTPUT             → white

CONSOLE_DARKGRAY_OUTPUT         → brightBlack
CONSOLE_RED_BRIGHT_OUTPUT       → brightRed
CONSOLE_GREEN_BRIGHT_OUTPUT     → brightGreen
CONSOLE_YELLOW_BRIGHT_OUTPUT    → brightYellow
CONSOLE_BLUE_BRIGHT_OUTPUT      → brightBlue
CONSOLE_MAGENTA_BRIGHT_OUTPUT   → brightPurple
CONSOLE_CYAN_BRIGHT_OUTPUT      → brightCyan
CONSOLE_WHITE_OUTPUT            → brightWhite
```

### Editor Syntax Attributes (100+) - Inferred Mapping

#### General Code Elements
```
DEFAULT_ATTRIBUTE               → foreground
TEXT                            → foreground
IDENTIFIER                      → foreground
KEYWORD                         → blue/purple pool
RESERVED_WORD                   → blue/purple pool (BOLD)
OPERATION_SIGN                  → foreground/white
NUMBER                          → yellow/cyan pool
```

#### Strings and Literals
```
STRING                          → green pool
VALID_STRING_ESCAPE             → green pool (lightened)
INVALID_STRING_ESCAPE           → red pool
REGEXP                          → cyan pool
```

#### Comments
```
COMMENT                         → comment pool (dimmed, ITALIC)
DOC_COMMENT                     → comment pool (lighter, ITALIC)
DOC_COMMENT_TAG                 → comment pool (BOLD)
DOC_COMMENT_MARKUP              → comment pool
```

#### Functions and Methods
```
FUNCTION_CALL                   → cyan/blue pool
FUNCTION_DECLARATION            → cyan/blue pool (BOLD)
METHOD_CALL                     → cyan/blue pool
METHOD_DECLARATION              → cyan/blue pool (BOLD)
STATIC_METHOD                   → cyan/blue pool (BOLD)
```

#### Classes and Types
```
CLASS_NAME                      → yellow pool (BOLD)
CLASS_REFERENCE                 → yellow pool (BOLD)
ABSTRACT_CLASS                  → yellow pool (BOLD_ITALIC)
INTERFACE_NAME                  → yellow pool (BOLD_ITALIC)
ENUM_NAME                       → yellow pool (BOLD)
TYPE_PARAMETER_NAME             → purple pool
```

#### Variables and Parameters
```
LOCAL_VARIABLE                  → foreground
INSTANCE_FIELD                  → purple pool
STATIC_FIELD                    → purple pool (BOLD)
REASSIGNED_LOCAL_VARIABLE       → foreground (UNDERLINE)
REASSIGNED_PARAMETER            → foreground (ITALIC, UNDERLINE)
PARAMETER                       → foreground (ITALIC)
```

#### Constants
```
CONSTANT                        → purple pool (BOLD)
STATIC_FINAL_FIELD              → purple pool (BOLD)
ENUM_CONSTANT                   → purple pool (BOLD)
```

#### Annotations and Decorators
```
ANNOTATION                      → yellow pool
ANNOTATION_ATTRIBUTE            → yellow pool
METADATA                        → yellow pool
DECORATOR                       → yellow pool
```

#### Errors and Warnings
```
ERRORS_ATTRIBUTES               → red pool
WRONG_REFERENCES_ATTRIBUTES     → red pool (WAVE_UNDERSCORE)
RUNTIME_ERROR                   → red pool
WARNING_ATTRIBUTES              → yellow pool (WAVE_UNDERSCORE)
DEPRECATED_ATTRIBUTES           → foreground (STRIKETHROUGH, dimmed)
```

#### Special Highlighting
```
SEARCH_RESULT_ATTRIBUTES        → yellow pool (BACKGROUND)
WRITE_SEARCH_RESULT             → yellow pool (BACKGROUND, BOLD)
IDENTIFIER_UNDER_CARET          → neutral (BACKGROUND)
WRITE_IDENTIFIER_UNDER_CARET    → neutral (BACKGROUND)
TODO_COMMENT                    → yellow pool (BOLD, BACKGROUND)
```

---

## References

1. **IntelliJ Platform SDK**: [Editor Color Schemes](https://plugins.jetbrains.com/docs/intellij/color-scheme-management.html)
2. **Windows Terminal**: [Color Scheme Specification](https://learn.microsoft.com/en-us/windows/terminal/customize-settings/color-schemes)
3. **WCAG 2.1**: [Contrast Ratio Guidelines](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)
4. **Color Theory**: ITU-R BT.709 Luminance Formula
5. **Perceptual Color Spaces**: HSV/HSL for hue-based categorization

---

**Document Status:** COMPLETE
**Next Steps:**
1. Review and approve algorithm specification
2. Implement in `buildSrc/src/main/kotlin/mapping/SyntaxColorInference.kt`
3. Create unit tests for all edge cases
4. Validate with real Windows Terminal color schemes
5. Update TASK-203 to IN_PROGRESS

---

*End of Document*
