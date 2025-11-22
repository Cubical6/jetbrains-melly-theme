# Lovelace iTerm Color Integration - Implementation Tasks

> **Voor Claude Code:** Voer deze taken fase voor fase uit. Elke subtask is een enkele actie (2-5 minuten). Gebruik TDD waar mogelijk. Commit regelmatig.

> **⚠️ BELANGRIJK - Building & Testing:**
> - Gradle builds werken NIET in Claude Code Web (geen netwerktoegang voor dependencies)
> - Claude Code zal code schrijven en committen zonder tests te runnen
> - **Voor elke subtask met `./gradlew` of test commands**: Claude moet de gebruiker vragen om dit lokaal uit te voeren
> - **Gebruiker rapporteert resultaat terug** aan Claude (PASS/FAIL/errors)
> - Claude gebruikt het resultaat om verder te gaan of debugging te doen
> - **Na voltooiing van ALLE taken**: Run lokaal `./gradlew test` en `./gradlew build` om te verifiëren
> - Als tests falen, maak issues aan voor fixes

> **📋 Template voor Test/Build Taken:**
> ```
> ⚠️ ASK USER: Run lokaal: `[command here]`
> Expected result: [wat we verwachten]
> → Gebruiker rapporteert: [PASS/FAIL + eventuele errors]
> ```

**Doel:** iTerm color schemes importeren en JetBrains themes genereren met Lovelace-kwaliteit (50+ afgeleide kleuren ipv 10).

**Architectuur:** Breid bestaand Windows Terminal systeem uit met iTerm parser en enhanced color derivation. Backwards compatible.

**Tech Stack:** Kotlin, Gradle, Gson (JSON), javax.xml (plist parsing)

---

## FASE 0: Pre-existing Test Cleanup (OPTIONEEL) ✅ VOLTOOID

> **✅ STATUS:** VOLTOOID op 2025-11-22
> **Branch:** `claude/cleanup-preexisting-tests-01VJMTm2mH5MdfupZHh2RoKN`
> **Commits:** 5 commits (5aa1fae → 6afc951)

> **⚠️ Note:** Deze fase is OPTIONEEL. De iTerm implementatie (Tasks 1.1 & 1.2) werkt correct en tests slagen met `./test-iterm-implementation.sh`. Deze fase lost pre-existing test **compilation errors** op die NIET gerelateerd zijn aan iTerm work.

**Context:** Het project had 46+ pre-existing test **compilation errors** in 8 test files. Deze blokkeerden `./gradlew compileTestKotlin` maar waren NIET veroorzaakt door recente iTerm work. De standalone test script werkte eromheen door broken tests tijdelijk te verplaatsen.

**Root Causes (Opgelost):**
1. **Missing Public API** (16 errors): `ColorUtils.normalizeColor()` was private maar werd aangeroepen in tests ✅
2. **Kotest API Mismatches** (17 errors): Oude kotest syntax en missing assertions ✅
3. **Type Inference Issues** (10 errors): Map types in ColorMappingTest niet properly inferred ✅
4. **Compiler Warnings** (3 warnings): Unused variables in ColorUtils.kt ✅

**Resultaten:**
- ✅ Alle 46+ compilation errors opgelost
- ✅ Code compileert zonder errors: `BUILD SUCCESSFUL`
- ✅ iTerm tests slagen nog steeds: `✅ ALL TESTS PASSED!`
- ⚠️ 71 runtime test failures blijven bestaan (pre-existing, buiten scope)

**Bestanden Gewijzigd:**
- ✅ `buildSrc/src/main/kotlin/utils/ColorUtils.kt` - Added normalizeColor(), removed unused vars
- ✅ `buildSrc/src/test/kotlin/integration/BuildIntegrationTest.kt` - Fixed kotest assertions
- ✅ `buildSrc/src/test/kotlin/integration/RegressionTest.kt` - Fixed shouldNotContain
- ✅ `buildSrc/src/test/kotlin/mapping/ColorMappingTest.kt` - Added explicit Map types
- ✅ `buildSrc/src/test/kotlin/mapping/ColorPaletteExpanderTest.kt` - Fixed shouldBeBetween (7×)
- ✅ `buildSrc/src/test/kotlin/mapping/SyntaxColorInferenceTest.kt` - Fixed Int comparisons (7×)
- ✅ `buildSrc/src/test/kotlin/tasks/AccessibilityAuditTest.kt` - Fixed type mismatches (2×)
- ✅ `buildSrc/src/test/kotlin/utils/ColorUtilsTest.kt` - Fixed Int.shouldBeLessThan (6×)

**Git Commits:**
```
6afc951 - fix: fix Int.shouldBeLessThan type mismatches in ColorUtilsTest
f9f104d - chore: remove unused variables in ColorUtils
9d56921 - fix: add explicit map types in ColorMappingTest
e05a11e - fix: update kotest assertions to correct API
5aa1fae - fix: make normalizeColor() public API in ColorUtils
```

**Scope Nota:** Deze fase richtte zich op **compilation errors** (code kan niet compileren), niet op runtime test failures (code compileert maar tests falen). De 71 runtime failures zijn pre-existing issues die in een aparte fase zouden moeten worden aangepakt.

### Task 0.1: Fix ColorUtils Missing Public API

**Subtask 0.1.1: Make normalizeColor() public**

Bestand: `buildSrc/src/main/kotlin/utils/ColorUtils.kt`

Zoek de `normalizeColor()` functie (momenteel private) en maak het public:

```kotlin
// Zoek:
private fun normalizeColor(color: String): String {
    // ... implementation
}

// Verander naar:
/**
 * Normalize color string to standard hex format (#RRGGBB)
 */
fun normalizeColor(color: String): String {
    // ... implementation (unchanged)
}
```

Run: Check dat de change correct is
```bash
cd buildSrc
grep -n "fun normalizeColor" src/main/kotlin/utils/ColorUtils.kt
```
Expected: Moet "fun normalizeColor" tonen (zonder "private")

**Subtask 0.1.2: Verify BuildIntegrationTest compiles**

Run: `cd buildSrc && ../gradlew compileTestKotlin 2>&1 | grep normalizeColor`
Expected: Geen "Unresolved reference: normalizeColor" errors meer (was 16 errors)

**Subtask 0.1.3: Commit ColorUtils fix**

```bash
git add buildSrc/src/main/kotlin/utils/ColorUtils.kt
git commit -m "fix: make normalizeColor() public API in ColorUtils

Make normalizeColor() public instead of private to fix 16 test
compilation errors in BuildIntegrationTest.kt.

This function is used by integration tests to validate theme color
normalization. Making it public allows proper testing of color
string normalization behavior."
```

---

### Task 0.2: Fix Kotest API Mismatches

**Subtask 0.2.1: Fix shouldBeBetween in ColorPaletteExpanderTest**

Bestand: `buildSrc/src/test/kotlin/mapping/ColorPaletteExpanderTest.kt`

Zoek alle 7 occurrences van `shouldBeBetween` met range syntax:

```kotlin
// VOOR (incorrect - 7 occurrences):
value shouldBeBetween (min..max)

// NA (correct):
value.shouldBeBetween(min, max, 0.01)
```

Specifieke lines to fix (ongeveer):
- Line 362: `hue shouldBeBetween (0.0..360.0)` → `hue.shouldBeBetween(0.0, 360.0, 0.01)`
- Line 413, 417, 458, 462, 498, 499: Similar changes

**Subtask 0.2.2: Fix missing shouldNotContain in RegressionTest**

Bestand: `buildSrc/src/test/kotlin/integration/RegressionTest.kt`

Lines ~370-373 gebruiken `shouldNotContain` op String - vervang met proper assertion:

```kotlin
// VOOR:
themeXml shouldNotContain "null"
themeJson shouldNotContain "null"
uiThemeJson shouldNotContain "null"

// NA:
assertFalse(themeXml.contains("null"), "Theme XML should not contain 'null'")
assertFalse(themeJson.contains("null"), "Theme JSON should not contain 'null'")
assertFalse(uiThemeJson.contains("null"), "UI theme JSON should not contain 'null'")
```

Add import: `import org.junit.jupiter.api.Assertions.assertFalse`

**Subtask 0.2.3: Fix Int.shouldBeLessThan type mismatches**

Bestand: `buildSrc/src/test/kotlin/mapping/SyntaxColorInferenceTest.kt`

Lines 171, 183, 207, 270, 285, 308, 448 gebruiken Double matchers op Int values:

```kotlin
// VOOR (7 occurrences):
colorCount.shouldBeLessThan(10)  // colorCount is Int, matcher expects Double

// NA - cast to Double:
colorCount.toDouble().shouldBeLessThan(10.0)
// Of gebruik standaard assertions:
assertTrue(colorCount < 10, "Color count should be less than 10")
```

**Subtask 0.2.4: Fix shouldBeGreaterThanOrEqual in AccessibilityAuditTest**

Bestand: `buildSrc/src/test/kotlin/tasks/AccessibilityAuditTest.kt`

Line 56:
```kotlin
// VOOR:
contrastRatio.shouldBeGreaterThanOrEqual(4.5)  // contrastRatio is Float

// NA:
contrastRatio.toDouble().shouldBeGreaterThanOrEqual(4.5)
// Of:
assertTrue(contrastRatio >= 4.5, "Contrast ratio should be >= 4.5")
```

**Subtask 0.2.5: Fix shouldNotBeEmpty type mismatch**

Bestand: `buildSrc/src/test/kotlin/integration/BuildIntegrationTest.kt` (line 495)
Bestand: `buildSrc/src/test/kotlin/tasks/AccessibilityAuditTest.kt` (line 128)

```kotlin
// VOOR:
themes.shouldNotBeEmpty()  // themes is Set or incorrect type

// NA (check actual type first):
assertTrue(themes.isNotEmpty(), "Themes should not be empty")
// Of if it's proper Collection:
assertFalse(themes.isEmpty())
```

**Subtask 0.2.6: Verify Kotest fixes compile**

Run: `cd buildSrc && ../gradlew compileTestKotlin 2>&1 | grep -E "shouldBeBetween|shouldNotContain|shouldBeLessThan"`
Expected: No more errors on these matchers (was 11+ errors)

**Subtask 0.2.7: Commit Kotest fixes**

```bash
git add buildSrc/src/test/kotlin/mapping/ColorPaletteExpanderTest.kt
git add buildSrc/src/test/kotlin/integration/RegressionTest.kt
git add buildSrc/src/test/kotlin/mapping/SyntaxColorInferenceTest.kt
git add buildSrc/src/test/kotlin/tasks/AccessibilityAuditTest.kt
git add buildSrc/src/test/kotlin/integration/BuildIntegrationTest.kt
git commit -m "fix: update kotest assertions to correct API

Fix 11+ kotest matcher errors:
- shouldBeBetween: use method syntax with tolerance param
- shouldNotContain: replace with standard assertFalse
- shouldBeLessThan/shouldBeGreaterThan: cast Int to Double
- shouldNotBeEmpty: use standard assertions for type safety

These were API incompatibilities from kotest version changes."
```

---

### Task 0.3: Fix Type Inference Issues

**Subtask 0.3.1: Fix ColorMappingTest map type inference**

Bestand: `buildSrc/src/test/kotlin/mapping/ColorMappingTest.kt`

Lines 260, 283, 295, 309, 753, 763, 776, 832, 852, 855 hebben "Not enough information to infer type variable K" errors.

Deze zijn meestal `mapOf()` calls waar Kotlin de key type niet kan inferren. Voeg explicit types toe:

```kotlin
// VOOR:
val expectedColors = mapOf(
    "foreground" to "#FFFFFF",
    "background" to "#000000"
)

// NA - explicit type:
val expectedColors: Map<String, String> = mapOf(
    "foreground" to "#FFFFFF",
    "background" to "#000000"
)
// Of:
val expectedColors = mapOf<String, String>(
    "foreground" to "#FFFFFF",
    "background" to "#000000"
)
```

Check each occurrence around the line numbers and add explicit types.

**Subtask 0.3.2: Verify type inference fixes**

Run: `cd buildSrc && ../gradlew compileTestKotlin 2>&1 | grep "infer type variable"`
Expected: No more "Not enough information to infer type variable K" (was 10 errors)

**Subtask 0.3.3: Commit type inference fixes**

```bash
git add buildSrc/src/test/kotlin/mapping/ColorMappingTest.kt
git commit -m "fix: add explicit map types in ColorMappingTest

Add explicit Map<String, String> types to fix 10 type inference errors.
Kotlin couldn't infer generic type K from mapOf() context in these cases."
```

---

### Task 0.4: Full Test Suite Verification ✅ VOLTOOID

**Subtask 0.4.1: Run complete test suite** ✅

```bash
cd buildSrc && ../gradlew test
```

**Resultaat:**
- ✅ Compilation: `BUILD SUCCESSFUL`
- ✅ 461 tests compiled successfully
- ⚠️ 71 runtime test failures (pre-existing, buiten scope van FASE 0)
- ℹ️ Compilation errors waren het doel, niet runtime failures

**Subtask 0.4.2: Verify iTerm tests still pass** ✅

```bash
./test-iterm-implementation.sh
```

**Resultaat:**
```
✅ ALL TESTS PASSED!
BUILD SUCCESSFUL in 5s
```

iTerm implementatie blijft 100% functioneel na cleanup.

**Subtask 0.4.3: Run full project build** ✅

```bash
./gradlew build
```

**Resultaat:**
```
BUILD SUCCESSFUL in 10s
15 actionable tasks: 14 executed, 1 up-to-date
```

**Subtask 0.4.4: Push all commits** ✅

```bash
git push -u origin claude/cleanup-preexisting-tests-01VJMTm2mH5MdfupZHh2RoKN
```

**Resultaat:** 5 commits succesvol gepusht naar remote branch

**Samenvatting:**
- ✅ Alle 46+ compilation errors opgelost
- ✅ Code compileert zonder errors
- ✅ iTerm functionaliteit intact
- ✅ Alle wijzigingen gecommit en gepusht

---

## FASE 1: iTerm Import Infrastructure

> **⚠️ Testing Instructie:** Voor alle subtaken met test/build commando's (`./gradlew`):
> - Claude kan deze NIET uitvoeren in web omgeving
> - Claude vraagt gebruiker om lokaal te runnen: `⚠️ ASK USER: Run lokaal: [command]`
> - Gebruiker rapporteert resultaat (PASS/FAIL + eventuele errors)
> - Claude gebruikt resultaat om verder te gaan of te debuggen

**Bestanden:**
- Create: `buildSrc/src/main/kotlin/colorschemes/ITermColorScheme.kt`
- Create: `buildSrc/src/main/kotlin/parsers/ITermPlistParser.kt`
- Create: `buildSrc/src/main/kotlin/converters/ITermToWindowsTerminalConverter.kt`
- Create: `buildSrc/src/main/kotlin/tasks/ImportITermSchemes.kt`
- Create: `buildSrc/src/test/kotlin/colorschemes/ITermColorSchemeTest.kt`

### Task 1.1: ITermColorScheme Data Class

**Subtask 1.1.1: Create ITermColorScheme.kt met basis structuur**

Bestand: `buildSrc/src/main/kotlin/colorschemes/ITermColorScheme.kt`

```kotlin
package colorschemes

/**
 * Represents an iTerm2 color scheme (.itermcolors format)
 * iTerm uses XML plist format with RGB components as floats (0.0-1.0)
 */
data class ITermColorScheme(
    val name: String,
    val ansiColors: Map<Int, ITermColor>, // 0-15 for ANSI colors
    val foreground: ITermColor,
    val background: ITermColor,
    val selection: ITermColor,
    val cursor: ITermColor,
    val cursorText: ITermColor? = null,
    val bold: ITermColor? = null,
    val link: ITermColor? = null
) {
    data class ITermColor(
        val red: Float,   // 0.0 - 1.0
        val green: Float, // 0.0 - 1.0
        val blue: Float,  // 0.0 - 1.0
        val alpha: Float = 1.0f
    ) {
        init {
            require(red in 0.0f..1.0f) { "Red must be 0.0-1.0, got $red" }
            require(green in 0.0f..1.0f) { "Green must be 0.0-1.0, got $green" }
            require(blue in 0.0f..1.0f) { "Blue must be 0.0-1.0, got $blue" }
            require(alpha in 0.0f..1.0f) { "Alpha must be 0.0-1.0, got $alpha" }
        }

        /**
         * Convert iTerm float RGB (0.0-1.0) to hex string
         */
        fun toHexString(): String {
            val r = (red * 255).toInt().coerceIn(0, 255)
            val g = (green * 255).toInt().coerceIn(0, 255)
            val b = (blue * 255).toInt().coerceIn(0, 255)
            return "#%02X%02X%02X".format(r, g, b)
        }

        companion object {
            /**
             * Parse hex color to iTerm float format
             */
            fun fromHex(hex: String): ITermColor {
                val clean = hex.removePrefix("#")
                require(clean.length == 6) { "Invalid hex color: $hex" }

                val r = clean.substring(0, 2).toInt(16) / 255.0f
                val g = clean.substring(2, 4).toInt(16) / 255.0f
                val b = clean.substring(4, 6).toInt(16) / 255.0f

                return ITermColor(r, g, b)
            }
        }
    }

    /**
     * Validate that scheme has all required colors
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        // Check all 16 ANSI colors present
        for (i in 0..15) {
            if (!ansiColors.containsKey(i)) {
                errors.add("Missing ANSI color $i")
            }
        }

        return errors
    }
}
```

**Subtask 1.1.2: Write test voor ITermColor.toHexString()**

Bestand: `buildSrc/src/test/kotlin/colorschemes/ITermColorSchemeTest.kt`

```kotlin
package colorschemes

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ITermColorSchemeTest {

    @Test
    fun `toHexString converts float RGB to hex correctly`() {
        val color = ITermColorScheme.ITermColor(
            red = 0.29f,    // ~74 = 0x4A
            green = 0.18f,  // ~46 = 0x2E
            blue = 0.44f    // ~112 = 0x70
        )

        val hex = color.toHexString()
        assertEquals("#4A2E70", hex)
    }

    @Test
    fun `toHexString handles edge cases`() {
        // Pure black
        assertEquals("#000000", ITermColorScheme.ITermColor(0f, 0f, 0f).toHexString())

        // Pure white
        assertEquals("#FFFFFF", ITermColorScheme.ITermColor(1f, 1f, 1f).toHexString())

        // Pure red
        assertEquals("#FF0000", ITermColorScheme.ITermColor(1f, 0f, 0f).toHexString())
    }

    @Test
    fun `fromHex converts hex to float RGB correctly`() {
        val color = ITermColorScheme.ITermColor.fromHex("#4A2E70")

        // Allow small floating point errors
        assertEquals(0.29f, color.red, 0.01f)
        assertEquals(0.18f, color.green, 0.01f)
        assertEquals(0.44f, color.blue, 0.01f)
    }

    @Test
    fun `fromHex handles with and without hash prefix`() {
        val color1 = ITermColorScheme.ITermColor.fromHex("#FF6B6B")
        val color2 = ITermColorScheme.ITermColor.fromHex("FF6B6B")

        assertEquals(color1.red, color2.red, 0.001f)
        assertEquals(color1.green, color2.green, 0.001f)
        assertEquals(color1.blue, color2.blue, 0.001f)
    }

    @Test
    fun `ITermColor validates range`() {
        assertThrows(IllegalArgumentException::class.java) {
            ITermColorScheme.ITermColor(-0.1f, 0.5f, 0.5f)
        }

        assertThrows(IllegalArgumentException::class.java) {
            ITermColorScheme.ITermColor(0.5f, 1.1f, 0.5f)
        }
    }

    @Test
    fun `validate detects missing ANSI colors`() {
        val scheme = ITermColorScheme(
            name = "Incomplete",
            ansiColors = mapOf(
                0 to ITermColorScheme.ITermColor(0f, 0f, 0f),
                1 to ITermColorScheme.ITermColor(1f, 0f, 0f)
                // Missing 2-15
            ),
            foreground = ITermColorScheme.ITermColor(1f, 1f, 1f),
            background = ITermColorScheme.ITermColor(0f, 0f, 0f),
            selection = ITermColorScheme.ITermColor(0.5f, 0.5f, 0.5f),
            cursor = ITermColorScheme.ITermColor(1f, 1f, 1f)
        )

        val errors = scheme.validate()
        assertEquals(14, errors.size) // Missing colors 2-15
        assertTrue(errors.any { it.contains("ANSI color 2") })
    }
}
```

**Subtask 1.1.3: Commit**

```bash
git add buildSrc/src/main/kotlin/colorschemes/ITermColorScheme.kt
git add buildSrc/src/test/kotlin/colorschemes/ITermColorSchemeTest.kt
git commit -m "feat: add ITermColorScheme data class with RGB conversion"
```

---

### Task 1.2: iTerm Plist XML Parser

**Subtask 1.2.1: Create ITermPlistParser.kt met XML parsing**

Bestand: `buildSrc/src/main/kotlin/parsers/ITermPlistParser.kt`

```kotlin
package parsers

import colorschemes.ITermColorScheme
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parser for iTerm2 .itermcolors files (XML plist format)
 *
 * Format example:
 * <plist version="1.0">
 *   <dict>
 *     <key>Ansi 0 Color</key>
 *     <dict>
 *       <key>Red Component</key><real>0.11</real>
 *       <key>Green Component</key><real>0.12</real>
 *       <key>Blue Component</key><real>0.16</real>
 *     </dict>
 *   </dict>
 * </plist>
 */
object ITermPlistParser {

    private val COLOR_KEYS = mapOf(
        "Ansi 0 Color" to 0,
        "Ansi 1 Color" to 1,
        "Ansi 2 Color" to 2,
        "Ansi 3 Color" to 3,
        "Ansi 4 Color" to 4,
        "Ansi 5 Color" to 5,
        "Ansi 6 Color" to 6,
        "Ansi 7 Color" to 7,
        "Ansi 8 Color" to 8,
        "Ansi 9 Color" to 9,
        "Ansi 10 Color" to 10,
        "Ansi 11 Color" to 11,
        "Ansi 12 Color" to 12,
        "Ansi 13 Color" to 13,
        "Ansi 14 Color" to 14,
        "Ansi 15 Color" to 15
    )

    /**
     * Parse .itermcolors file to ITermColorScheme
     */
    fun parse(file: File): ITermColorScheme {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        require(file.extension == "itermcolors") { "File must be .itermcolors, got: ${file.name}" }

        val doc = parseXML(file)
        val rootDict = getRootDict(doc)

        val ansiColors = mutableMapOf<Int, ITermColorScheme.ITermColor>()
        var foreground: ITermColorScheme.ITermColor? = null
        var background: ITermColorScheme.ITermColor? = null
        var selection: ITermColorScheme.ITermColor? = null
        var cursor: ITermColorScheme.ITermColor? = null
        var cursorText: ITermColorScheme.ITermColor? = null
        var bold: ITermColorScheme.ITermColor? = null
        var link: ITermColorScheme.ITermColor? = null

        // Parse all key-value pairs in root dict
        val entries = parseDictEntries(rootDict)

        for ((key, valueDict) in entries) {
            val color = parseColorDict(valueDict)

            when {
                COLOR_KEYS.containsKey(key) -> ansiColors[COLOR_KEYS[key]!!] = color
                key == "Foreground Color" -> foreground = color
                key == "Background Color" -> background = color
                key == "Selection Color" -> selection = color
                key == "Cursor Color" -> cursor = color
                key == "Cursor Text Color" -> cursorText = color
                key == "Bold Color" -> bold = color
                key == "Link Color" -> link = color
            }
        }

        // Validate required colors
        requireNotNull(foreground) { "Missing 'Foreground Color' in ${file.name}" }
        requireNotNull(background) { "Missing 'Background Color' in ${file.name}" }
        requireNotNull(selection) { "Missing 'Selection Color' in ${file.name}" }
        requireNotNull(cursor) { "Missing 'Cursor Color' in ${file.name}" }

        return ITermColorScheme(
            name = file.nameWithoutExtension,
            ansiColors = ansiColors,
            foreground = foreground,
            background = background,
            selection = selection,
            cursor = cursor,
            cursorText = cursorText,
            bold = bold,
            link = link
        )
    }

    private fun parseXML(file: File): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        return builder.parse(file)
    }

    private fun getRootDict(doc: Document): Element {
        val plist = doc.getElementsByTagName("plist").item(0) as? Element
            ?: throw IllegalArgumentException("No <plist> element found")

        val dict = plist.getElementsByTagName("dict").item(0) as? Element
            ?: throw IllegalArgumentException("No root <dict> element found")

        return dict
    }

    private fun parseDictEntries(dict: Element): Map<String, Element> {
        val entries = mutableMapOf<String, Element>()
        val children = dict.childNodes

        var i = 0
        while (i < children.length) {
            val node = children.item(i)

            if (node is Element && node.tagName == "key") {
                val key = node.textContent.trim()

                // Find next dict element
                var j = i + 1
                while (j < children.length) {
                    val valueNode = children.item(j)
                    if (valueNode is Element && valueNode.tagName == "dict") {
                        entries[key] = valueNode
                        break
                    }
                    j++
                }
            }

            i++
        }

        return entries
    }

    private fun parseColorDict(dict: Element): ITermColorScheme.ITermColor {
        val components = parseDictValues(dict)

        val red = components["Red Component"]?.toFloatOrNull()
            ?: throw IllegalArgumentException("Missing 'Red Component'")
        val green = components["Green Component"]?.toFloatOrNull()
            ?: throw IllegalArgumentException("Missing 'Green Component'")
        val blue = components["Blue Component"]?.toFloatOrNull()
            ?: throw IllegalArgumentException("Missing 'Blue Component'")
        val alpha = components["Alpha Component"]?.toFloatOrNull() ?: 1.0f

        return ITermColorScheme.ITermColor(red, green, blue, alpha)
    }

    private fun parseDictValues(dict: Element): Map<String, String> {
        val values = mutableMapOf<String, String>()
        val children = dict.childNodes

        var i = 0
        while (i < children.length) {
            val node = children.item(i)

            if (node is Element && node.tagName == "key") {
                val key = node.textContent.trim()

                // Find next real/string/integer element
                var j = i + 1
                while (j < children.length) {
                    val valueNode = children.item(j)
                    if (valueNode is Element && valueNode.tagName in listOf("real", "string", "integer")) {
                        values[key] = valueNode.textContent.trim()
                        break
                    }
                    j++
                }
            }

            i++
        }

        return values
    }
}
```

**Subtask 1.2.2: Create test .itermcolors bestand**

Bestand: `buildSrc/src/test/resources/test-scheme.itermcolors`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>Ansi 0 Color</key>
	<dict>
		<key>Red Component</key><real>0.11372549019607843</real>
		<key>Green Component</key><real>0.12156862745098039</real>
		<key>Blue Component</key><real>0.1568627450980392</real>
	</dict>
	<key>Ansi 1 Color</key>
	<dict>
		<key>Red Component</key><real>1.0</real>
		<key>Green Component</key><real>0.4196078431372549</real>
		<key>Blue Component</key><real>0.4196078431372549</real>
	</dict>
	<key>Ansi 2 Color</key>
	<dict>
		<key>Red Component</key><real>0.6431372549019608</real>
		<key>Green Component</key><real>0.8313725490196079</real>
		<key>Blue Component</key><real>0.5647058823529412</real>
	</dict>
	<key>Ansi 3 Color</key>
	<dict>
		<key>Red Component</key><real>1.0</real>
		<key>Green Component</key><real>0.8313725490196079</real>
		<key>Blue Component</key><real>0.4745098039215686</real>
	</dict>
	<key>Ansi 4 Color</key>
	<dict>
		<key>Red Component</key><real>0.35294117647058826</real>
		<key>Green Component</key><real>0.6509803921568628</real>
		<key>Blue Component</key><real>1.0</real>
	</dict>
	<key>Ansi 5 Color</key>
	<dict>
		<key>Red Component</key><real>0.7764705882352941</real>
		<key>Green Component</key><real>0.47058823529411764</real>
		<key>Blue Component</key><real>0.8666666666666667</real>
	</dict>
	<key>Ansi 6 Color</key>
	<dict>
		<key>Red Component</key><real>0.30196078431372547</real>
		<key>Green Component</key><real>0.8156862745098039</real>
		<key>Blue Component</key><real>0.8823529411764706</real>
	</dict>
	<key>Ansi 7 Color</key>
	<dict>
		<key>Red Component</key><real>0.6392156862745098</real>
		<key>Green Component</key><real>0.6627450980392157</real>
		<key>Blue Component</key><real>0.7137254901960784</real>
	</dict>
	<key>Ansi 8 Color</key>
	<dict>
		<key>Red Component</key><real>0.39215686274509803</real>
		<key>Green Component</key><real>0.3764705882352941</real>
		<key>Blue Component</key><real>0.47058823529411764</real>
	</dict>
	<key>Ansi 9 Color</key>
	<dict>
		<key>Red Component</key><real>0.9490196078431372</real>
		<key>Green Component</key><real>0.4235294117647059</real>
		<key>Blue Component</key><real>0.5529411764705883</real>
	</dict>
	<key>Ansi 10 Color</key>
	<dict>
		<key>Red Component</key><real>0.7098039215686275</real>
		<key>Green Component</key><real>0.807843137254902</real>
		<key>Blue Component</key><real>0.6588235294117647</real>
	</dict>
	<key>Ansi 11 Color</key>
	<dict>
		<key>Red Component</key><real>1.0</real>
		<key>Green Component</key><real>0.7607843137254902</real>
		<key>Blue Component</key><real>0.43529411764705883</real>
	</dict>
	<key>Ansi 12 Color</key>
	<dict>
		<key>Red Component</key><real>0.3803921568627451</real>
		<key>Green Component</key><real>0.6862745098039216</real>
		<key>Blue Component</key><real>0.9372549019607843</real>
	</dict>
	<key>Ansi 13 Color</key>
	<dict>
		<key>Red Component</key><real>0.8156862745098039</real>
		<key>Green Component</key><real>0.5058823529411764</real>
		<key>Blue Component</key><real>1.0</real>
	</dict>
	<key>Ansi 14 Color</key>
	<dict>
		<key>Red Component</key><real>0.37254901960784315</real>
		<key>Green Component</key><real>0.8549019607843137</real>
		<key>Blue Component</key><real>0.796078431372549</real>
	</dict>
	<key>Ansi 15 Color</key>
	<dict>
		<key>Red Component</key><real>0.8627450980392157</real>
		<key>Green Component</key><real>0.8627450980392157</real>
		<key>Blue Component</key><real>0.8627450980392157</real>
	</dict>
	<key>Background Color</key>
	<dict>
		<key>Red Component</key><real>0.11372549019607843</real>
		<key>Green Component</key><real>0.12156862745098039</real>
		<key>Blue Component</key><real>0.1568627450980392</real>
	</dict>
	<key>Foreground Color</key>
	<dict>
		<key>Red Component</key><real>0.8156862745098039</real>
		<key>Green Component</key><real>0.8156862745098039</real>
		<key>Blue Component</key><real>0.8509803921568627</real>
	</dict>
	<key>Selection Color</key>
	<dict>
		<key>Red Component</key><real>0.4392156862745098</real>
		<key>Green Component</key><real>0.3803921568627451</real>
		<key>Blue Component</key><real>0.5529411764705883</real>
	</dict>
	<key>Cursor Color</key>
	<dict>
		<key>Red Component</key><real>1.0</real>
		<key>Green Component</key><real>1.0</real>
		<key>Blue Component</key><real>1.0</real>
	</dict>
</dict>
</plist>
```

**Subtask 1.2.3: Write test voor ITermPlistParser**

Bestand toevoegen aan: `buildSrc/src/test/kotlin/colorschemes/ITermColorSchemeTest.kt`

```kotlin
// Add to existing test file

import parsers.ITermPlistParser
import java.io.File

class ITermPlistParserTest {

    @Test
    fun `parse valid itermcolors file`() {
        val file = File("src/test/resources/test-scheme.itermcolors")
        val scheme = ITermPlistParser.parse(file)

        assertEquals("test-scheme", scheme.name)

        // Verify ANSI colors parsed
        assertEquals(16, scheme.ansiColors.size)

        // Verify Ansi 0 Color (background equivalent)
        val ansi0 = scheme.ansiColors[0]!!
        assertEquals("#1D1F28", ansi0.toHexString())

        // Verify Ansi 1 Color (red)
        val ansi1 = scheme.ansiColors[1]!!
        assertEquals("#FF6B6B", ansi1.toHexString())

        // Verify foreground
        assertEquals("#D0D0D9", scheme.foreground.toHexString())

        // Verify background
        assertEquals("#1D1F28", scheme.background.toHexString())

        // Verify selection
        assertEquals("#70618D", scheme.selection.toHexString())

        // Verify cursor
        assertEquals("#FFFFFF", scheme.cursor.toHexString())
    }

    @Test
    fun `parse fails for missing file`() {
        assertThrows(IllegalArgumentException::class.java) {
            ITermPlistParser.parse(File("nonexistent.itermcolors"))
        }
    }

    @Test
    fun `parse fails for wrong extension`() {
        val file = File.createTempFile("test", ".txt")
        assertThrows(IllegalArgumentException::class.java) {
            ITermPlistParser.parse(file)
        }
        file.delete()
    }
}
```

**Subtask 1.2.4: Commit**

```bash
git add buildSrc/src/main/kotlin/parsers/ITermPlistParser.kt
git add buildSrc/src/test/resources/test-scheme.itermcolors
git add buildSrc/src/test/kotlin/colorschemes/ITermColorSchemeTest.kt
git commit -m "feat: add iTerm plist parser for .itermcolors files"
```

---

### Task 1.3: iTerm to Windows Terminal Converter

**Subtask 1.3.1: Create ITermToWindowsTerminalConverter.kt**

Bestand: `buildSrc/src/main/kotlin/converters/ITermToWindowsTerminalConverter.kt`

```kotlin
package converters

import colorschemes.ITermColorScheme
import colorschemes.WindowsTerminalColorScheme

/**
 * Converts iTerm2 color schemes to Windows Terminal format
 */
object ITermToWindowsTerminalConverter {

    /**
     * Convert ITermColorScheme to WindowsTerminalColorScheme
     *
     * iTerm ANSI mapping:
     * 0 = black, 1 = red, 2 = green, 3 = yellow
     * 4 = blue, 5 = purple, 6 = cyan, 7 = white
     * 8-15 = bright variants
     */
    fun convert(iTerm: ITermColorScheme): WindowsTerminalColorScheme {
        require(iTerm.ansiColors.size == 16) {
            "iTerm scheme must have 16 ANSI colors, got ${iTerm.ansiColors.size}"
        }

        return WindowsTerminalColorScheme(
            name = iTerm.name,
            background = iTerm.background.toHexString(),
            foreground = iTerm.foreground.toHexString(),

            // Normal ANSI colors (0-7)
            black = iTerm.ansiColors[0]!!.toHexString(),
            red = iTerm.ansiColors[1]!!.toHexString(),
            green = iTerm.ansiColors[2]!!.toHexString(),
            yellow = iTerm.ansiColors[3]!!.toHexString(),
            blue = iTerm.ansiColors[4]!!.toHexString(),
            purple = iTerm.ansiColors[5]!!.toHexString(),
            cyan = iTerm.ansiColors[6]!!.toHexString(),
            white = iTerm.ansiColors[7]!!.toHexString(),

            // Bright ANSI colors (8-15)
            brightBlack = iTerm.ansiColors[8]!!.toHexString(),
            brightRed = iTerm.ansiColors[9]!!.toHexString(),
            brightGreen = iTerm.ansiColors[10]!!.toHexString(),
            brightYellow = iTerm.ansiColors[11]!!.toHexString(),
            brightBlue = iTerm.ansiColors[12]!!.toHexString(),
            brightPurple = iTerm.ansiColors[13]!!.toHexString(),
            brightCyan = iTerm.ansiColors[14]!!.toHexString(),
            brightWhite = iTerm.ansiColors[15]!!.toHexString(),

            // Optional colors
            cursorColor = iTerm.cursor.toHexString(),
            selectionBackground = iTerm.selection.toHexString()
        )
    }
}
```

**Subtask 1.3.2: Write test voor converter**

```kotlin
// Add to ITermColorSchemeTest.kt

import converters.ITermToWindowsTerminalConverter

class ITermToWindowsTerminalConverterTest {

    @Test
    fun `convert iTerm to WindowsTerminal preserves all colors`() {
        val iTerm = createTestITermScheme()
        val wt = ITermToWindowsTerminalConverter.convert(iTerm)

        assertEquals("TestScheme", wt.name)
        assertEquals("#1D1F28", wt.background)
        assertEquals("#D0D0D9", wt.foreground)
        assertEquals("#1D1F28", wt.black)
        assertEquals("#FF6B6B", wt.red)
        assertEquals("#A4D490", wt.green)
        assertEquals("#FFD479", wt.yellow)
        assertEquals("#5AA6FF", wt.blue)
        assertEquals("#C678DD", wt.purple)
        assertEquals("#4DD0E1", wt.cyan)
        assertEquals("#A3A9B6", wt.white)
        assertEquals("#646078", wt.brightBlack)
        assertEquals("#FFFFFF", wt.cursorColor)
        assertEquals("#70618D", wt.selectionBackground)
    }

    @Test
    fun `convert validates iTerm has 16 colors`() {
        val incomplete = ITermColorScheme(
            name = "Incomplete",
            ansiColors = mapOf(0 to ITermColorScheme.ITermColor(0f, 0f, 0f)),
            foreground = ITermColorScheme.ITermColor(1f, 1f, 1f),
            background = ITermColorScheme.ITermColor(0f, 0f, 0f),
            selection = ITermColorScheme.ITermColor(0.5f, 0.5f, 0.5f),
            cursor = ITermColorScheme.ITermColor(1f, 1f, 1f)
        )

        assertThrows(IllegalArgumentException::class.java) {
            ITermToWindowsTerminalConverter.convert(incomplete)
        }
    }

    private fun createTestITermScheme(): ITermColorScheme {
        return ITermColorScheme(
            name = "TestScheme",
            ansiColors = mapOf(
                0 to ITermColorScheme.ITermColor.fromHex("#1D1F28"),
                1 to ITermColorScheme.ITermColor.fromHex("#FF6B6B"),
                2 to ITermColorScheme.ITermColor.fromHex("#A4D490"),
                3 to ITermColorScheme.ITermColor.fromHex("#FFD479"),
                4 to ITermColorScheme.ITermColor.fromHex("#5AA6FF"),
                5 to ITermColorScheme.ITermColor.fromHex("#C678DD"),
                6 to ITermColorScheme.ITermColor.fromHex("#4DD0E1"),
                7 to ITermColorScheme.ITermColor.fromHex("#A3A9B6"),
                8 to ITermColorScheme.ITermColor.fromHex("#646078"),
                9 to ITermColorScheme.ITermColor.fromHex("#F26C8D"),
                10 to ITermColorScheme.ITermColor.fromHex("#B5CEA8"),
                11 to ITermColorScheme.ITermColor.fromHex("#FFC26F"),
                12 to ITermColorScheme.ITermColor.fromHex("#61AFEF"),
                13 to ITermColorScheme.ITermColor.fromHex("#D081FF"),
                14 to ITermColorScheme.ITermColor.fromHex("#5FDACB"),
                15 to ITermColorScheme.ITermColor.fromHex("#DCDCDC")
            ),
            foreground = ITermColorScheme.ITermColor.fromHex("#D0D0D9"),
            background = ITermColorScheme.ITermColor.fromHex("#1D1F28"),
            selection = ITermColorScheme.ITermColor.fromHex("#70618D"),
            cursor = ITermColorScheme.ITermColor.fromHex("#FFFFFF")
        )
    }
}
```

**Subtask 1.3.3: Run tests**

Run: `./gradlew test --tests ITermToWindowsTerminalConverterTest`
Expected: PASS

**Subtask 1.3.4: Commit**

```bash
git add buildSrc/src/main/kotlin/converters/ITermToWindowsTerminalConverter.kt
git add buildSrc/src/test/kotlin/colorschemes/ITermColorSchemeTest.kt
git commit -m "feat: add iTerm to Windows Terminal converter"
```

---

### Task 1.4: Import iTerm Schemes Gradle Task

**Subtask 1.4.1: Create ImportITermSchemes.kt task**

Bestand: `buildSrc/src/main/kotlin/tasks/ImportITermSchemes.kt`

```kotlin
package tasks

import converters.ITermToWindowsTerminalConverter
import parsers.ITermPlistParser
import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Gradle task to import iTerm2 .itermcolors files and convert to Windows Terminal JSON
 *
 * Usage: ./gradlew importITermSchemes
 *
 * Input: iterm-schemes/*.itermcolors
 * Output: windows-terminal-schemes/*.json
 */
open class ImportITermSchemes : DefaultTask() {

    init {
        group = "theme"
        description = "Import iTerm2 .itermcolors files to Windows Terminal JSON format"
    }

    @TaskAction
    fun importSchemes() {
        val iTermDir = project.file("iterm-schemes")
        val outputDir = project.file("windows-terminal-schemes")

        if (!iTermDir.exists()) {
            println("Creating iterm-schemes/ directory...")
            iTermDir.mkdirs()
            println("Place .itermcolors files in iterm-schemes/ and run this task again.")
            return
        }

        val iTermFiles = iTermDir.listFiles { file ->
            file.extension == "itermcolors"
        } ?: emptyArray()

        if (iTermFiles.isEmpty()) {
            println("No .itermcolors files found in iterm-schemes/")
            return
        }

        println("Found ${iTermFiles.size} iTerm scheme(s) to import")

        outputDir.mkdirs()
        val gson = GsonBuilder().setPrettyPrinting().create()

        var successCount = 0
        var errorCount = 0

        for (iTermFile in iTermFiles) {
            try {
                println("Importing: ${iTermFile.name}")

                // Parse iTerm scheme
                val iTermScheme = ITermPlistParser.parse(iTermFile)

                // Validate
                val errors = iTermScheme.validate()
                if (errors.isNotEmpty()) {
                    println("  ⚠️  Validation errors:")
                    errors.forEach { println("     - $it") }
                    errorCount++
                    continue
                }

                // Convert to Windows Terminal
                val wtScheme = ITermToWindowsTerminalConverter.convert(iTermScheme)

                // Write JSON
                val outputFile = File(outputDir, "${wtScheme.name}.json")
                outputFile.writeText(gson.toJson(wtScheme))

                println("  ✓ Converted to: ${outputFile.name}")
                successCount++

            } catch (e: Exception) {
                println("  ✗ Error: ${e.message}")
                errorCount++
            }
        }

        println("\nImport complete:")
        println("  Success: $successCount")
        println("  Errors: $errorCount")
    }
}
```

**Subtask 1.4.2: Register task in build.gradle.kts**

Bestand: `buildSrc/build.gradle.kts` (of main `build.gradle.kts`)

Check waar tasks geregistreerd worden. Voeg toe:

```kotlin
tasks.register<tasks.ImportITermSchemes>("importITermSchemes")
```

**Subtask 1.4.3: Test task manueel**

Run: `./gradlew importITermSchemes`
Expected: Output "No .itermcolors files found" of task succeeds

**Subtask 1.4.4: Create test .itermcolors in iterm-schemes/**

Copy test-scheme.itermcolors naar `iterm-schemes/test-import.itermcolors`

**Subtask 1.4.5: Run import task**

Run: `./gradlew importITermSchemes`
Expected: Creates `windows-terminal-schemes/test-import.json`

**Subtask 1.4.6: Verify generated JSON**

Read: `windows-terminal-schemes/test-import.json`
Expected: Valid Windows Terminal JSON met alle 20 kleuren

**Subtask 1.4.7: Cleanup test files**

```bash
rm iterm-schemes/test-import.itermcolors
rm windows-terminal-schemes/test-import.json
```

**Subtask 1.4.8: Commit**

```bash
git add buildSrc/src/main/kotlin/tasks/ImportITermSchemes.kt
git add buildSrc/build.gradle.kts
git commit -m "feat: add Gradle task to import iTerm schemes"
```

### FASE 1 Verificatie

**Na voltooiing van alle Task 1.x subtaken, voer verificatie uit:**

⚠️ **ASK USER:** Run lokaal de volgende verificatie stappen:

1. **Compilatie Check:**
   ```bash
   ./gradlew compileKotlin compileTestKotlin
   ```
   Expected: BUILD SUCCESSFUL

2. **Unit Tests:**
   ```bash
   ./gradlew test --tests ITermColorSchemeTest
   ./gradlew test --tests ITermPlistParserTest
   ./gradlew test --tests ITermToWindowsTerminalConverterTest
   ```
   Expected: All tests PASS

3. **Import Task Werkt:**
   ```bash
   ./gradlew importITermSchemes
   ```
   Expected: Successfully imports .itermcolors files from iterm-schemes/

→ **Gebruiker rapporteert resultaten:**
- [ ] Compilatie: _[PASS/FAIL + errors]_
- [ ] Unit tests: _[PASS/FAIL + which tests failed]_
- [ ] Import task: _[PASS/FAIL + output]_

**Als FAIL:** Claude debugt op basis van error output.
**Als PASS:** Ga verder naar FASE 2.

---

## FASE 2: Enhanced Color Derivation

> **⚠️ Testing Instructie:** Voor alle subtaken met test/build commando's (`./gradlew`):
> - Claude kan deze NIET uitvoeren in web omgeving
> - Claude vraagt gebruiker: `⚠️ ASK USER: Run lokaal: [command]`
> - Gebruiker rapporteert resultaat (PASS/FAIL + errors)

**Bestanden:**
- Modify: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`
- Modify: `buildSrc/src/main/kotlin/utils/ColorUtils.kt`
- Create: `buildSrc/src/test/kotlin/utils/ColorUtilsEnhancedTest.kt`

### Task 2.1: Intermediate Shade Generation

**Subtask 2.1.1: Add generateIntermediateShade to ColorUtils.kt**

Bestand: `buildSrc/src/main/kotlin/utils/ColorUtils.kt`

Voeg toe aan bestaande ColorUtils object:

```kotlin
/**
 * Generate intermediate shade between two colors
 * @param color1 First color in hex format
 * @param color2 Second color in hex format
 * @param ratio Mix ratio (0.0 = color1, 1.0 = color2, 0.5 = halfway)
 */
fun generateIntermediateShade(color1: String, color2: String, ratio: Float = 0.5f): String {
    require(ratio in 0.0f..1.0f) { "Ratio must be 0.0-1.0, got $ratio" }

    val rgb1 = parseHexColor(color1)
    val rgb2 = parseHexColor(color2)

    val r = (rgb1.red * (1 - ratio) + rgb2.red * ratio).toInt()
    val g = (rgb1.green * (1 - ratio) + rgb2.green * ratio).toInt()
    val b = (rgb1.blue * (1 - ratio) + rgb2.blue * ratio).toInt()

    return "#%02X%02X%02X".format(r, g, b)
}

/**
 * Generate series of shades between two colors
 * @param start Start color in hex
 * @param end End color in hex
 * @param steps Number of intermediate steps (not including start/end)
 */
fun generateColorGradient(start: String, end: String, steps: Int): List<String> {
    require(steps >= 0) { "Steps must be >= 0" }

    if (steps == 0) return listOf(start, end)

    val gradient = mutableListOf(start)
    for (i in 1..steps) {
        val ratio = i.toFloat() / (steps + 1)
        gradient.add(generateIntermediateShade(start, end, ratio))
    }
    gradient.add(end)

    return gradient
}

/**
 * Create a color with alpha transparency (ARGB format)
 * @param color Base color in hex
 * @param alpha Alpha value 0.0-1.0 (0=transparent, 1=opaque)
 */
fun addAlpha(color: String, alpha: Float): String {
    require(alpha in 0.0f..1.0f) { "Alpha must be 0.0-1.0, got $alpha" }

    val alphaHex = (alpha * 255).toInt().coerceIn(0, 255)
    val rgb = parseHexColor(color)

    return "#%02X%02X%02X%02X".format(alphaHex, rgb.red, rgb.green, rgb.blue)
}

private data class RGB(val red: Int, val green: Int, val blue: Int)

private fun parseHexColor(hex: String): RGB {
    val clean = hex.removePrefix("#")
    require(clean.length == 6 || clean.length == 8) { "Invalid hex color: $hex" }

    val r = clean.substring(0, 2).toInt(16)
    val g = clean.substring(2, 4).toInt(16)
    val b = clean.substring(4, 6).toInt(16)

    return RGB(r, g, b)
}
```

**Subtask 2.1.2: Write tests voor new utility functions**

Bestand: `buildSrc/src/test/kotlin/utils/ColorUtilsEnhancedTest.kt`

```kotlin
package utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ColorUtilsEnhancedTest {

    @Test
    fun `generateIntermediateShade creates halfway color`() {
        val result = ColorUtils.generateIntermediateShade("#000000", "#FFFFFF", 0.5f)
        assertEquals("#808080", result.uppercase()) // 128 = 0x80
    }

    @Test
    fun `generateIntermediateShade at ratio 0 returns first color`() {
        val result = ColorUtils.generateIntermediateShade("#FF0000", "#00FF00", 0.0f)
        assertEquals("#FF0000", result.uppercase())
    }

    @Test
    fun `generateIntermediateShade at ratio 1 returns second color`() {
        val result = ColorUtils.generateIntermediateShade("#FF0000", "#00FF00", 1.0f)
        assertEquals("#00FF00", result.uppercase())
    }

    @Test
    fun `generateColorGradient creates correct number of steps`() {
        val gradient = ColorUtils.generateColorGradient("#000000", "#FFFFFF", 3)

        assertEquals(5, gradient.size) // start + 3 steps + end
        assertEquals("#000000", gradient.first().uppercase())
        assertEquals("#FFFFFF", gradient.last().uppercase())
    }

    @Test
    fun `generateColorGradient with 0 steps returns start and end`() {
        val gradient = ColorUtils.generateColorGradient("#FF0000", "#0000FF", 0)

        assertEquals(2, gradient.size)
        assertEquals("#FF0000", gradient[0].uppercase())
        assertEquals("#0000FF", gradient[1].uppercase())
    }

    @Test
    fun `addAlpha creates ARGB format`() {
        val result = ColorUtils.addAlpha("#FF6B6B", 0.5f)
        assertEquals("#80FF6B6B", result.uppercase()) // 0.5 * 255 = 127.5 ≈ 0x80
    }

    @Test
    fun `addAlpha with 0 is fully transparent`() {
        val result = ColorUtils.addAlpha("#FFFFFF", 0.0f)
        assertEquals("#00FFFFFF", result.uppercase())
    }

    @Test
    fun `addAlpha with 1 is fully opaque`() {
        val result = ColorUtils.addAlpha("#000000", 1.0f)
        assertEquals("#FF000000", result.uppercase())
    }
}
```

**Subtask 2.1.3: Run tests**

Run: `./gradlew test --tests ColorUtilsEnhancedTest`
Expected: PASS

**Subtask 2.1.4: Commit**

```bash
git add buildSrc/src/main/kotlin/utils/ColorUtils.kt
git add buildSrc/src/test/kotlin/utils/ColorUtilsEnhancedTest.kt
git commit -m "feat: add intermediate shade and gradient generation to ColorUtils"
```

---

### Task 2.2: Extend toColorPalette with 50+ Derived Colors

**Subtask 2.2.1: Read current toColorPalette implementation**

Read: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`
Zoek de `toColorPalette()` functie en begrijp huidige 10 derived colors

**Subtask 2.2.2: Create enhanced ColorPalette data class**

In `WindowsTerminalColorScheme.kt`, extend de ColorPalette data class:

```kotlin
data class ColorPalette(
    // Existing 10 colors
    val surface: String,
    val surfaceLight: String,
    val surfaceLighter: String,
    val lineNumbers: String,
    val guideColor: String,
    val dividerColor: String,
    val mutedForeground: String,
    val errorBackground: String,
    val warningBackground: String,
    val infoBackground: String,
    val uiBorderColor: String,
    val uiComponentBackground: String,

    // NEW: Surface variations (10 new)
    val surfaceDark: String,        // Darken bg 5%
    val surfaceDarker: String,      // Darken bg 10%
    val surfaceDarkest: String,     // Darken bg 15%
    val surfaceSubtle: String,      // Lighten bg 3%

    // NEW: Selection variations (4 new)
    val selectionInactive: String,   // Dim selection 40%
    val selectionLight: String,      // Lighten selection 20%
    val selectionBorder: String,     // Lighter selection for borders

    // NEW: Focus/Accent colors (6 new)
    val focusColor: String,          // Brighten primary accent
    val focusBorder: String,         // Dim accent for borders
    val accentPrimary: String,       // Use brightBlue
    val accentSecondary: String,     // Use brightPurple
    val accentTertiary: String,      // Use brightCyan

    // NEW: Button/Component colors (8 new)
    val buttonBorder: String,        // Subtle border from surface
    val buttonBorderFocused: String, // Accent border for focused state
    val popupBackground: String,     // Blend purple + background
    val popupBorder: String,         // Lighter than popup bg
    val headerBackground: String,    // Custom mid-tone
    val hoverBackground: String,     // Light hover state

    // NEW: Syntax-specific derived colors (12 new)
    val instanceField: String,       // Blend purple + red (pink)
    val todoColor: String,           // Blend cyan + green (teal)
    val deprecatedColor: String,     // Dim foreground
    val stringEscape: String,        // Brighten green
    val numberAlt: String,           // Alternative number color
    val constantColor: String,       // Constant values

    // NEW: Progress/Status colors (6 new)
    val progressStart: String,       // Gradient start
    val progressMid: String,         // Gradient middle
    val progressEnd: String,         // Gradient end
    val memoryIndicator: String,     // Memory usage color
    val passedColor: String,         // Test passed
    val failedColor: String,         // Test failed

    // NEW: Additional UI colors (8 new)
    val breadcrumbCurrent: String,   // Current breadcrumb
    val breadcrumbHover: String,     // Breadcrumb hover
    val separatorColor: String,      // Separator lines
    val disabledText: String,        // Disabled foreground
    val counterBackground: String,   // Counter badges
    val tooltipBackground: String,   // Tooltip bg
    val linkHover: String,           // Link hover state
    val iconColor: String            // Default icon color
)
```

**Subtask 2.2.3: Implement enhanced toColorPalette() function**

Vervang de bestaande `toColorPalette()` met:

```kotlin
fun toColorPalette(): ColorPalette {
    // Existing 12 colors (keep current implementation)
    val surface = ColorUtils.lightenColor(background, 0.05f)
    val surfaceLight = ColorUtils.lightenColor(background, 0.10f)
    val surfaceLighter = ColorUtils.lightenColor(background, 0.15f)
    val lineNumbers = ColorUtils.blendColors(background, foreground, 0.30f)
    val guideColor = ColorUtils.blendColors(background, foreground, 0.15f)
    val dividerColor = ColorUtils.blendColors(background, foreground, 0.25f)
    val mutedForeground = ColorUtils.blendColors(background, foreground, 0.60f)
    val errorBackground = ColorUtils.blendColors(background, red, 0.20f)
    val warningBackground = ColorUtils.blendColors(background, yellow, 0.20f)
    val infoBackground = ColorUtils.blendColors(background, blue, 0.20f)
    val uiBorderColor = ColorUtils.createVisibleBorderColor(background, foreground)
    val uiComponentBackground = ColorUtils.createVisibleComponentBackground(background, foreground)

    // NEW: Surface variations (10)
    val surfaceDark = ColorUtils.darkenColor(background, 0.05f)
    val surfaceDarker = ColorUtils.darkenColor(background, 0.10f)
    val surfaceDarkest = ColorUtils.darkenColor(background, 0.15f)
    val surfaceSubtle = ColorUtils.lightenColor(background, 0.03f)

    // NEW: Selection variations (4)
    val selectionBg = selectionBackground ?: ColorUtils.blendColors(background, brightBlue, 0.30f)
    val selectionInactive = ColorUtils.darkenColor(selectionBg, 0.40f)
    val selectionLight = ColorUtils.lightenColor(selectionBg, 0.20f)
    val selectionBorder = ColorUtils.lightenColor(selectionBg, 0.30f)

    // NEW: Focus/Accent colors (6)
    val accentPrimary = brightBlue
    val accentSecondary = brightPurple
    val accentTertiary = brightCyan
    val focusColor = ColorUtils.lightenColor(accentSecondary, 0.15f)
    val focusBorder = ColorUtils.darkenColor(accentSecondary, 0.15f)

    // NEW: Button/Component colors (8)
    val buttonBorder = ColorUtils.lightenColor(surface, 0.10f)
    val buttonBorderFocused = accentPrimary
    val popupBackground = ColorUtils.blendColors(background, purple, 0.15f)
    val popupBorder = ColorUtils.lightenColor(popupBackground, 0.20f)
    val headerBackground = ColorUtils.blendColors(surface, purple, 0.10f)
    val hoverBackground = ColorUtils.lightenColor(surface, 0.08f)

    // NEW: Syntax-specific derived colors (12)
    val instanceField = ColorUtils.blendColors(purple, red, 0.40f) // Pink
    val todoColor = ColorUtils.blendColors(brightCyan, green, 0.50f) // Teal
    val deprecatedColor = ColorUtils.blendColors(foreground, background, 0.50f)
    val stringEscape = ColorUtils.lightenColor(green, 0.15f)
    val numberAlt = ColorUtils.blendColors(blue, cyan, 0.30f)
    val constantColor = purple

    // NEW: Progress/Status colors (6)
    val gradient = ColorUtils.generateColorGradient(
        ColorUtils.blendColors(background, purple, 0.30f),
        ColorUtils.blendColors(background, cyan, 0.30f),
        1
    )
    val progressStart = gradient[0]
    val progressMid = gradient[1]
    val progressEnd = gradient[2]
    val memoryIndicator = ColorUtils.blendColors(background, purple, 0.40f)
    val passedColor = green
    val failedColor = red

    // NEW: Additional UI colors (8)
    val breadcrumbCurrent = foreground
    val breadcrumbHover = ColorUtils.lightenColor(foreground, 0.10f)
    val separatorColor = dividerColor
    val disabledText = brightBlack
    val counterBackground = surfaceDark
    val tooltipBackground = ColorUtils.lightenColor(popupBackground, 0.05f)
    val linkHover = ColorUtils.lightenColor(blue, 0.15f)
    val iconColor = mutedForeground

    return ColorPalette(
        // Existing 12
        surface = surface,
        surfaceLight = surfaceLight,
        surfaceLighter = surfaceLighter,
        lineNumbers = lineNumbers,
        guideColor = guideColor,
        dividerColor = dividerColor,
        mutedForeground = mutedForeground,
        errorBackground = errorBackground,
        warningBackground = warningBackground,
        infoBackground = infoBackground,
        uiBorderColor = uiBorderColor,
        uiComponentBackground = uiComponentBackground,

        // NEW: Surface variations (10)
        surfaceDark = surfaceDark,
        surfaceDarker = surfaceDarker,
        surfaceDarkest = surfaceDarkest,
        surfaceSubtle = surfaceSubtle,

        // NEW: Selection variations (4)
        selectionInactive = selectionInactive,
        selectionLight = selectionLight,
        selectionBorder = selectionBorder,

        // NEW: Focus/Accent colors (6)
        focusColor = focusColor,
        focusBorder = focusBorder,
        accentPrimary = accentPrimary,
        accentSecondary = accentSecondary,
        accentTertiary = accentTertiary,

        // NEW: Button/Component colors (8)
        buttonBorder = buttonBorder,
        buttonBorderFocused = buttonBorderFocused,
        popupBackground = popupBackground,
        popupBorder = popupBorder,
        headerBackground = headerBackground,
        hoverBackground = hoverBackground,

        // NEW: Syntax-specific (12)
        instanceField = instanceField,
        todoColor = todoColor,
        deprecatedColor = deprecatedColor,
        stringEscape = stringEscape,
        numberAlt = numberAlt,
        constantColor = constantColor,

        // NEW: Progress/Status (6)
        progressStart = progressStart,
        progressMid = progressMid,
        progressEnd = progressEnd,
        memoryIndicator = memoryIndicator,
        passedColor = passedColor,
        failedColor = failedColor,

        // NEW: Additional UI (8)
        breadcrumbCurrent = breadcrumbCurrent,
        breadcrumbHover = breadcrumbHover,
        separatorColor = separatorColor,
        disabledText = disabledText,
        counterBackground = counterBackground,
        tooltipBackground = tooltipBackground,
        linkHover = linkHover,
        iconColor = iconColor
    )
}
```

**Subtask 2.2.4: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: SUCCESS (no compilation errors)

**Subtask 2.2.5: Commit**

```bash
git add buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt
git commit -m "feat: extend ColorPalette from 12 to 60+ derived colors"
```

---

## FASE 3: Template Updates

> **⚠️ Testing Instructie:** Voor alle subtaken met test/build commando's (`./gradlew`):
> - Claude kan deze NIET uitvoeren in web omgeving
> - Claude vraagt gebruiker: `⚠️ ASK USER: Run lokaal: [command]`
> - Gebruiker rapporteert resultaat (PASS/FAIL + errors)

**Bestanden:**
- Modify: `buildSrc/templates/windows-terminal.template.theme.json`
- Modify: `buildSrc/templates/windows-terminal.template.xml`
- Modify: `buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt`
- Modify: `buildSrc/src/main/kotlin/generators/XMLColorSchemeGenerator.kt`

### Task 3.1: Update Theme JSON Template

**Subtask 3.1.1: Read current template**

Read: `buildSrc/templates/windows-terminal.template.theme.json`
Begrijp huidige placeholder structuur

**Subtask 3.1.2: Add new placeholders voor derived colors**

In template file, voeg toe waar relevant. Bijvoorbeeld bij ComplexPopup:

```json
"ComplexPopup.Header.background": "$wt_headerBackground$",
"ComplexPopup.Border.color": "$wt_popupBorder$",
```

Bij Button:

```json
"Button.default.borderColor": "$wt_buttonBorder$",
"Button.default.focusedBorderColor": "$wt_buttonBorderFocused$",
```

Bij Selection:

```json
"Editor.SelectionBackgroundColor": "$wt_selectionBackground$",
"Editor.Caret.SelectionBackgroundColor": "$wt_selectionInactive$",
```

**NOTE:** Dit is een groot bestand. Focus op belangrijkste ~20 placeholders eerst:
- $wt_popupBackground$
- $wt_headerBackground$
- $wt_buttonBorder$
- $wt_selectionInactive$
- $wt_focusColor$
- $wt_focusBorder$
- $wt_instanceField$
- $wt_todoColor$
- $wt_hoverBackground$
- $wt_progressStart$
- $wt_memoryIndicator$
- $wt_disabledText$
- $wt_linkHover$

**Subtask 3.1.3: Verify template syntax**

Check JSON syntax validity (kan met editor of `jq`)

**Subtask 3.1.4: Commit**

```bash
git add buildSrc/templates/windows-terminal.template.theme.json
git commit -m "feat: add new color placeholders to theme.json template"
```

---

### Task 3.2: Update XML Template

**Subtask 3.2.1: Read current XML template**

Read: `buildSrc/templates/windows-terminal.template.xml`

**Subtask 3.2.2: Add new placeholders voor syntax colors**

Voeg toe aan relevante syntax attributes:

```xml
<option name="DEFAULT_INSTANCE_FIELD">
  <value>
    <option name="FOREGROUND" value="$wt_instanceField$"/>
  </value>
</option>

<option name="TODO_DEFAULT_ATTRIBUTES">
  <value>
    <option name="FOREGROUND" value="$wt_todoColor$"/>
  </value>
</option>

<option name="DEFAULT_CONSTANT">
  <value>
    <option name="FOREGROUND" value="$wt_constantColor$"/>
  </value>
</option>
```

**Subtask 3.2.3: Verify XML syntax**

Check XML validity (kan met xmllint of editor)

**Subtask 3.2.4: Commit**

```bash
git add buildSrc/templates/windows-terminal.template.xml
git commit -m "feat: add new syntax color placeholders to XML template"
```

---

### Task 3.3: Update Template Generators

**Subtask 3.3.1: Update UIThemeGenerator placeholder replacement**

Bestand: `buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt`

Vind waar placeholders worden replaced. Extend met nieuwe colors:

```kotlin
// Add after existing placeholder replacements
.replace("\$wt_popupBackground$", palette.popupBackground)
.replace("\$wt_headerBackground$", palette.headerBackground)
.replace("\$wt_buttonBorder$", palette.buttonBorder)
.replace("\$wt_buttonBorderFocused$", palette.buttonBorderFocused)
.replace("\$wt_selectionInactive$", palette.selectionInactive)
.replace("\$wt_selectionLight$", palette.selectionLight)
.replace("\$wt_focusColor$", palette.focusColor)
.replace("\$wt_focusBorder$", palette.focusBorder)
.replace("\$wt_instanceField$", palette.instanceField)
.replace("\$wt_todoColor$", palette.todoColor)
.replace("\$wt_hoverBackground$", palette.hoverBackground)
.replace("\$wt_progressStart$", palette.progressStart)
.replace("\$wt_progressMid$", palette.progressMid)
.replace("\$wt_progressEnd$", palette.progressEnd)
.replace("\$wt_memoryIndicator$", palette.memoryIndicator)
.replace("\$wt_disabledText$", palette.disabledText)
.replace("\$wt_linkHover$", palette.linkHover)
.replace("\$wt_accentPrimary$", palette.accentPrimary)
.replace("\$wt_accentSecondary$", palette.accentSecondary)
// etc...
```

**Subtask 3.3.2: Update XMLColorSchemeGenerator placeholder replacement**

Bestand: `buildSrc/src/main/kotlin/generators/XMLColorSchemeGenerator.kt`

Similarly add new placeholders:

```kotlin
.replace("\$wt_instanceField$", palette.instanceField)
.replace("\$wt_todoColor$", palette.todoColor)
.replace("\$wt_constantColor$", palette.constantColor)
// etc...
```

**Subtask 3.3.3: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: SUCCESS

**Subtask 3.3.4: Commit**

```bash
git add buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt
git add buildSrc/src/main/kotlin/generators/XMLColorSchemeGenerator.kt
git commit -m "feat: update generators to handle new color placeholders"
```

---

## FASE 4: Lovelace Reference Implementation

> **⚠️ Testing Instructie:** Voor alle subtaken met test/build commando's (`./gradlew`):
> - Claude kan deze NIET uitvoeren in web omgeving
> - Claude vraagt gebruiker: `⚠️ ASK USER: Run lokaal: [command]`
> - Gebruiker rapporteert resultaat (PASS/FAIL + errors)

**Bestanden:**
- Create: `windows-terminal-schemes/lovelace.json`
- Create: `iterm-schemes/lovelace.itermcolors`

### Task 4.1: Create Lovelace Windows Terminal Scheme

**Subtask 4.1.1: Create lovelace.json**

Bestand: `windows-terminal-schemes/lovelace.json`

```json
{
  "name": "Lovelace",
  "background": "#1D1F28",
  "foreground": "#D0D0D9",
  "cursorColor": "#FFFFFF",
  "selectionBackground": "#70618D",
  "black": "#1D1F28",
  "red": "#FF6B6B",
  "green": "#A4D490",
  "yellow": "#FFD479",
  "blue": "#5AA6FF",
  "purple": "#C678DD",
  "cyan": "#4DD0E1",
  "white": "#A3A9B6",
  "brightBlack": "#646078",
  "brightRed": "#F26C8D",
  "brightGreen": "#B5CEA8",
  "brightYellow": "#FFC26F",
  "brightBlue": "#61AFEF",
  "brightPurple": "#D081FF",
  "brightCyan": "#5FDACB",
  "brightWhite": "#DCDCDC"
}
```

**Subtask 4.1.2: Validate JSON**

Run: `cat windows-terminal-schemes/lovelace.json | jq .`
Expected: Valid JSON output

**Subtask 4.1.3: Commit**

```bash
git add windows-terminal-schemes/lovelace.json
git commit -m "feat: add Lovelace Windows Terminal color scheme"
```

---

### Task 4.2: Create Lovelace iTerm Scheme

**Subtask 4.2.1: Create lovelace.itermcolors**

Bestand: `iterm-schemes/lovelace.itermcolors`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>Ansi 0 Color</key>
	<dict>
		<key>Red Component</key><real>0.11372549019607843</real>
		<key>Green Component</key><real>0.12156862745098039</real>
		<key>Blue Component</key><real>0.1568627450980392</real>
	</dict>
	<key>Ansi 1 Color</key>
	<dict>
		<key>Red Component</key><real>1.0</real>
		<key>Green Component</key><real>0.4196078431372549</real>
		<key>Blue Component</key><real>0.4196078431372549</real>
	</dict>
	<key>Ansi 2 Color</key>
	<dict>
		<key>Red Component</key><real>0.6431372549019608</real>
		<key>Green Component</key><real>0.8313725490196079</real>
		<key>Blue Component</key><real>0.5647058823529412</real>
	</dict>
	<key>Ansi 3 Color</key>
	<dict>
		<key>Red Component</key><real>1.0</real>
		<key>Green Component</key><real>0.8313725490196079</real>
		<key>Blue Component</key><real>0.4745098039215686</real>
	</dict>
	<key>Ansi 4 Color</key>
	<dict>
		<key>Red Component</key><real>0.35294117647058826</real>
		<key>Green Component</key><real>0.6509803921568628</real>
		<key>Blue Component</key><real>1.0</real>
	</dict>
	<key>Ansi 5 Color</key>
	<dict>
		<key>Red Component</key><real>0.7764705882352941</real>
		<key>Green Component</key><real>0.47058823529411764</real>
		<key>Blue Component</key><real>0.8666666666666667</real>
	</dict>
	<key>Ansi 6 Color</key>
	<dict>
		<key>Red Component</key><real>0.30196078431372547</real>
		<key>Green Component</key><real>0.8156862745098039</real>
		<key>Blue Component</key><real>0.8823529411764706</real>
	</dict>
	<key>Ansi 7 Color</key>
	<dict>
		<key>Red Component</key><real>0.6392156862745098</real>
		<key>Green Component</key><real>0.6627450980392157</real>
		<key>Blue Component</key><real>0.7137254901960784</real>
	</dict>
	<key>Ansi 8 Color</key>
	<dict>
		<key>Red Component</key><real>0.39215686274509803</real>
		<key>Green Component</key><real>0.3764705882352941</real>
		<key>Blue Component</key><real>0.47058823529411764</real>
	</dict>
	<key>Ansi 9 Color</key>
	<dict>
		<key>Red Component</key><real>0.9490196078431372</real>
		<key>Green Component</key><real>0.4235294117647059</real>
		<key>Blue Component</key><real>0.5529411764705883</real>
	</dict>
	<key>Ansi 10 Color</key>
	<dict>
		<key>Red Component</key><real>0.7098039215686275</real>
		<key>Green Component</key><real>0.807843137254902</real>
		<key>Blue Component</key><real>0.6588235294117647</real>
	</dict>
	<key>Ansi 11 Color</key>
	<dict>
		<key>Red Component</key><real>1.0</real>
		<key>Green Component</key><real>0.7607843137254902</real>
		<key>Blue Component</key><real>0.43529411764705883</real>
	</dict>
	<key>Ansi 12 Color</key>
	<dict>
		<key>Red Component</key><real>0.3803921568627451</real>
		<key>Green Component</key><real>0.6862745098039216</real>
		<key>Blue Component</key><real>0.9372549019607843</real>
	</dict>
	<key>Ansi 13 Color</key>
	<dict>
		<key>Red Component</key><real>0.8156862745098039</real>
		<key>Green Component</key><real>0.5058823529411764</real>
		<key>Blue Component</key><real>1.0</real>
	</dict>
	<key>Ansi 14 Color</key>
	<dict>
		<key>Red Component</key><real>0.37254901960784315</real>
		<key>Green Component</key><real>0.8549019607843137</real>
		<key>Blue Component</key><real>0.796078431372549</real>
	</dict>
	<key>Ansi 15 Color</key>
	<dict>
		<key>Red Component</key><real>0.8627450980392157</real>
		<key>Green Component</key><real>0.8627450980392157</real>
		<key>Blue Component</key><real>0.8627450980392157</real>
	</dict>
	<key>Background Color</key>
	<dict>
		<key>Red Component</key><real>0.11372549019607843</real>
		<key>Green Component</key><real>0.12156862745098039</real>
		<key>Blue Component</key><real>0.1568627450980392</real>
	</dict>
	<key>Foreground Color</key>
	<dict>
		<key>Red Component</key><real>0.8156862745098039</real>
		<key>Green Component</key><real>0.8156862745098039</real>
		<key>Blue Component</key><real>0.8509803921568627</real>
	</dict>
	<key>Selection Color</key>
	<dict>
		<key>Red Component</key><real>0.4392156862745098</real>
		<key>Green Component</key><real>0.3803921568627451</real>
		<key>Blue Component</key><real>0.5529411764705883</real>
	</dict>
	<key>Cursor Color</key>
	<dict>
		<key>Red Component</key><real>1.0</real>
		<key>Green Component</key><real>1.0</real>
		<key>Blue Component</key><real>1.0</real>
	</dict>
</dict>
</plist>
```

**Subtask 4.2.2: Test iTerm import**

Run: `./gradlew importITermSchemes`
Expected: Converts lovelace.itermcolors → lovelace.json (may overwrite existing)

**Subtask 4.2.3: Verify generated matches manual version**

Compare generated with manually created lovelace.json
Expected: Should be identical or very close

**Subtask 4.2.4: Commit**

```bash
git add iterm-schemes/lovelace.itermcolors
git commit -m "feat: add Lovelace iTerm color scheme"
```

---

### Task 4.3: Generate and Test Lovelace Theme

**Subtask 4.3.1: Generate themes from lovelace.json**

Run: `./gradlew generateThemesFromWindowsTerminal`
Expected: Creates `src/main/resources/themes/Lovelace.theme.json` and `.xml`

**Subtask 4.3.2: Read generated Lovelace.theme.json**

Read: `src/main/resources/themes/Lovelace.theme.json`
Verify nieuwe placeholders zijn ingevuld met kleuren

**Subtask 4.3.3: Read generated Lovelace.xml**

Read: `src/main/resources/themes/Lovelace.xml`
Verify syntax colors zijn correct toegewezen

**Subtask 4.3.4: Visual comparison met demo theme**

Open side-by-side:
- `demo/Lovelace-Theme/resources/theme/LoveLaceTheme.theme.json`
- `src/main/resources/themes/Lovelace.theme.json`

Check key colors match visually (exact match niet vereist, visually equivalent is OK)

**Subtask 4.3.5: Document any significant differences**

Als er grote afwijkingen zijn, noteer deze. Visually equivalent is acceptable.

**Subtask 4.3.6: Commit generated themes**

```bash
git add src/main/resources/themes/Lovelace.theme.json
git add src/main/resources/themes/Lovelace.xml
git commit -m "feat: generate Lovelace theme from color scheme"
```

---

## FASE 5: Validation & Testing

> **⚠️ Testing Instructie:** Voor alle subtaken met test/build commando's (`./gradlew`):
> - Claude kan deze NIET uitvoeren in web omgeving
> - Claude vraagt gebruiker: `⚠️ ASK USER: Run lokaal: [command]`
> - Gebruiker rapporteert resultaat (PASS/FAIL + errors)

### Task 5.1: Regenerate All Themes

**Subtask 5.1.1: Backup current generated themes**

```bash
mkdir -p /tmp/theme-backup
cp -r src/main/resources/themes /tmp/theme-backup/
```

**Subtask 5.1.2: Clean generated themes**

```bash
rm -rf src/main/resources/themes/*.theme.json
rm -rf src/main/resources/themes/*.xml
```

**Subtask 5.1.3: Regenerate all themes**

Run: `./gradlew generateThemesFromWindowsTerminal`
Expected: Generates 60+ themes successfully

**Subtask 5.1.4: Count generated themes**

```bash
ls -1 src/main/resources/themes/*.theme.json | wc -l
ls -1 src/main/resources/themes/*.xml | wc -l
```
Expected: Same number as before (60+)

**Subtask 5.1.5: Check for generation errors**

Review Gradle output for any errors or warnings during generation

**Subtask 5.1.6: Spot-check 5 popular themes**

Read en visueel check deze themes:
1. `Dracula.theme.json`
2. `One Dark.theme.json`
3. `Solarized Dark.theme.json`
4. `Nord.theme.json`
5. `Gruvbox Dark.theme.json`

Verify nieuwe placeholders zijn correct ingevuld

---

### Task 5.2: Git Diff Analysis

**Subtask 5.2.1: Check git status**

Run: `git status`
Expected: Modified files in src/main/resources/themes/

**Subtask 5.2.2: Review diff sample**

```bash
git diff src/main/resources/themes/Dracula.theme.json | head -100
```

Verify nieuwe kleuren zijn toegevoegd, oude kleuren behouden

**Subtask 5.2.3: Check file sizes**

```bash
ls -lh src/main/resources/themes/Dracula.theme.json
```

Compare to backup. Files should be larger (more colors) but not dramatically different.

**Subtask 5.2.4: Validate JSON syntax**

```bash
find src/main/resources/themes -name "*.theme.json" -exec sh -c 'jq empty "$1" 2>/dev/null || echo "Invalid: $1"' _ {} \;
```

Expected: No "Invalid" output

**Subtask 5.2.5: Validate XML syntax**

```bash
find src/main/resources/themes -name "*.xml" -exec sh -c 'xmllint --noout "$1" 2>/dev/null || echo "Invalid: $1"' _ {} \;
```

Expected: No "Invalid" output (or install xmllint if missing)

---

### Task 5.3: Build and Test Plugin

**Subtask 5.3.1: Build plugin JAR**

Run: `./gradlew buildPlugin`
Expected: SUCCESS, JAR created in build/distributions/

**Subtask 5.3.2: Check JAR size**

```bash
ls -lh build/distributions/*.jar
```

Should be similar size to previous builds (slightly larger due to more colors)

**Subtask 5.3.3: Extract and verify theme files in JAR**

```bash
unzip -l build/distributions/*.jar | grep -i lovelace
```

Expected: Lovelace.theme.json and Lovelace.xml present in JAR

---

### Task 5.4: Documentation

**Subtask 5.4.1: Update README with iTerm import instructions**

Bestand: `README.md`

Voeg sectie toe:

```markdown
## Importing iTerm Color Schemes

You can import iTerm2 `.itermcolors` files and automatically convert them to JetBrains themes:

1. Place `.itermcolors` files in `iterm-schemes/` directory
2. Run import task:
   ```bash
   ./gradlew importITermSchemes
   ```
3. Generated Windows Terminal JSON files will be in `windows-terminal-schemes/`
4. Generate JetBrains themes:
   ```bash
   ./gradlew generateThemesFromWindowsTerminal
   ```

The system now generates 60+ derived colors from the base 16-color palette for richer, more polished themes.
```

**Subtask 5.4.2: Create CHANGELOG entry**

Bestand: `CHANGELOG.md` (or create if missing)

```markdown
## [Unreleased]

### Added
- iTerm2 .itermcolors import support
- ITermColorScheme data class and parser
- ITermToWindowsTerminalConverter for format conversion
- `importITermSchemes` Gradle task
- Enhanced color derivation: 60+ derived colors (up from 12)
- Intermediate shade generation utilities
- Color gradient generation
- Alpha transparency support
- Lovelace theme based on Lovelace iTerm color scheme

### Changed
- Extended ColorPalette with 50+ new derived colors
- Updated theme.json template with new color placeholders
- Updated XML template with enhanced syntax colors
- All 60+ existing themes now have richer color palettes

### Technical
- Added ColorUtils functions: generateIntermediateShade, generateColorGradient, addAlpha
- Enhanced toColorPalette() method in WindowsTerminalColorScheme
- Updated UIThemeGenerator and XMLColorSchemeGenerator for new placeholders
```

**Subtask 5.4.3: Commit documentation**

```bash
git add README.md CHANGELOG.md
git commit -m "docs: add iTerm import documentation and changelog"
```

---

### Task 5.5: Final Validation Commit

**Subtask 5.5.1: Review all changes**

Run: `git status`
Review all modified and new files

**Subtask 5.5.2: Commit regenerated themes**

```bash
git add src/main/resources/themes/
git commit -m "refactor: regenerate all themes with enhanced 60+ color derivation"
```

**Subtask 5.5.3: Create summary commit message**

Als alle individuele commits gemaakt zijn, optioneel squash naar feature commit:

```bash
git log --oneline -20
```

Review of alle commits logisch zijn

**Subtask 5.5.4: Tag release (optioneel)**

```bash
git tag -a v2.0.0-lovelace -m "Add iTerm import and enhanced color derivation"
```

---

## FASE 6: Rounded Theme Variants

> **⚠️ Testing Instructie:** Voor alle subtaken met test/build commando's (`./gradlew`):
> - Claude kan deze NIET uitvoeren in web omgeving
> - Claude vraagt gebruiker: `⚠️ ASK USER: Run lokaal: [command]`
> - Gebruiker rapporteert resultaat (PASS/FAIL + errors)

**Bestanden:**
- Create: `buildSrc/src/main/kotlin/variants/ThemeVariant.kt`
- Create: `buildSrc/templates/windows-terminal-rounded.template.theme.json`
- Modify: `buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt`
- Modify: `buildSrc/src/main/kotlin/tasks/GenerateThemesFromWindowsTerminal.kt`

**Doel:** Van elk color scheme twee UI theme varianten genereren: Standard (scherpe hoeken) en Rounded (moderne afgeronde UI).

### Task 6.1: Theme Variant Infrastructure

**Subtask 6.1.1: Create ThemeVariant.kt**

Bestand: `buildSrc/src/main/kotlin/variants/ThemeVariant.kt`

```kotlin
package variants

/**
 * Represents different UI style variants for themes
 */
sealed class ThemeVariant(
    val suffix: String,
    val displayName: String,
    val arcValues: ArcValues
) {
    /**
     * Standard variant with sharp corners (arc = 0)
     */
    object Standard : ThemeVariant(
        suffix = "",
        displayName = "",
        arcValues = ArcValues(
            component = 0,
            button = 0,
            tabbedPane = 0,
            progressBar = 0,
            comboBox = 0,
            textField = 0,
            checkBox = 0,
            tree = 0,
            table = 0,
            popup = 0
        )
    )

    /**
     * Rounded variant with modern rounded corners (arc = 6-12)
     */
    object Rounded : ThemeVariant(
        suffix = " Rounded",
        displayName = "Rounded",
        arcValues = ArcValues(
            component = 8,
            button = 6,
            tabbedPane = 8,
            progressBar = 4,
            comboBox = 4,
            textField = 4,
            checkBox = 3,
            tree = 4,
            table = 0,
            popup = 12
        )
    )

    companion object {
        fun all(): List<ThemeVariant> = listOf(Standard, Rounded)
    }
}

/**
 * Arc (border radius) values for different UI components
 */
data class ArcValues(
    val component: Int,      // General component arc
    val button: Int,         // Button arc
    val tabbedPane: Int,     // Tab arc
    val progressBar: Int,    // Progress bar arc
    val comboBox: Int,       // Combo box arc
    val textField: Int,      // Text field arc
    val checkBox: Int,       // Checkbox arc
    val tree: Int,           // Tree row arc
    val table: Int,          // Table cell arc
    val popup: Int           // Popup window arc
) {
    /**
     * Convert to template placeholder map
     */
    fun toPlaceholders(): Map<String, String> = mapOf(
        "\$arc_component$" to component.toString(),
        "\$arc_button$" to button.toString(),
        "\$arc_tabbedPane$" to tabbedPane.toString(),
        "\$arc_progressBar$" to progressBar.toString(),
        "\$arc_comboBox$" to comboBox.toString(),
        "\$arc_textField$" to textField.toString(),
        "\$arc_checkBox$" to checkBox.toString(),
        "\$arc_tree$" to tree.toString(),
        "\$arc_table$" to table.toString(),
        "\$arc_popup$" to popup.toString()
    )
}
```

**Subtask 6.1.2: Write test voor ThemeVariant**

Bestand: `buildSrc/src/test/kotlin/variants/ThemeVariantTest.kt`

```kotlin
package variants

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ThemeVariantTest {

    @Test
    fun `standard variant has no suffix`() {
        assertEquals("", ThemeVariant.Standard.suffix)
        assertEquals("", ThemeVariant.Standard.displayName)
    }

    @Test
    fun `rounded variant has suffix`() {
        assertEquals(" Rounded", ThemeVariant.Rounded.suffix)
        assertEquals("Rounded", ThemeVariant.Rounded.displayName)
    }

    @Test
    fun `standard variant has all arcs set to 0`() {
        val arcs = ThemeVariant.Standard.arcValues
        assertEquals(0, arcs.component)
        assertEquals(0, arcs.button)
        assertEquals(0, arcs.tabbedPane)
        assertEquals(0, arcs.progressBar)
    }

    @Test
    fun `rounded variant has positive arc values`() {
        val arcs = ThemeVariant.Rounded.arcValues
        assertTrue(arcs.component > 0)
        assertTrue(arcs.button > 0)
        assertTrue(arcs.popup > 0)
    }

    @Test
    fun `all returns both variants`() {
        val variants = ThemeVariant.all()
        assertEquals(2, variants.size)
        assertTrue(variants.contains(ThemeVariant.Standard))
        assertTrue(variants.contains(ThemeVariant.Rounded))
    }

    @Test
    fun `toPlaceholders creates correct map`() {
        val placeholders = ThemeVariant.Rounded.arcValues.toPlaceholders()

        assertEquals("8", placeholders["\$arc_component$"])
        assertEquals("6", placeholders["\$arc_button$"])
        assertEquals("12", placeholders["\$arc_popup$"])
        assertEquals(10, placeholders.size) // 10 arc properties
    }
}
```

**Subtask 6.1.3: Run tests**

Run: `./gradlew test --tests ThemeVariantTest`
Expected: PASS

**Subtask 6.1.4: Commit**

```bash
git add buildSrc/src/main/kotlin/variants/ThemeVariant.kt
git add buildSrc/src/test/kotlin/variants/ThemeVariantTest.kt
git commit -m "feat: add ThemeVariant infrastructure for standard/rounded variants"
```

---

### Task 6.2: Rounded Template

**Subtask 6.2.1: Copy standard template to rounded template**

```bash
cp buildSrc/templates/windows-terminal.template.theme.json \
   buildSrc/templates/windows-terminal-rounded.template.theme.json
```

**Subtask 6.2.2: Add arc placeholders to rounded template**

Bestand: `buildSrc/templates/windows-terminal-rounded.template.theme.json`

Voeg na de `"ui"` section toe (rond regel 50-100):

```json
"Component": {
    "arc": $arc_component$,
    "focusWidth": 2
},
"Button": {
    "arc": $arc_button$,
    "default": {
        "startBackground": "$wt_blue$",
        "endBackground": "$wt_blue$",
        "foreground": "$wt_background$",
        "focusedBorderColor": "$wt_buttonBorderFocused$",
        "borderColor": "$wt_buttonBorder$"
    }
},
"TabbedPane": {
    "tabArc": $arc_tabbedPane$,
    "contentBorderInsets": "0,0,0,0"
},
"ProgressBar": {
    "arc": $arc_progressBar$,
    "trackArc": $arc_progressBar$
},
"ComboBox": {
    "arc": $arc_comboBox$,
    "ArrowButton.background": "$wt_surface$"
},
"TextField": {
    "arc": $arc_textField$
},
"CheckBox": {
    "arc": $arc_checkBox$
},
"Tree": {
    "rowHeight": 24,
    "arc": $arc_tree$
},
"Table": {
    "arc": $arc_table$
},
"Popup": {
    "arc": $arc_popup$,
    "borderColor": "$wt_popupBorder$"
},
```

**Subtask 6.2.3: Verify JSON syntax (na manual edit)**

Run: `cat buildSrc/templates/windows-terminal-rounded.template.theme.json | jq empty`
Expected: No errors (of fix syntax errors)

**Subtask 6.2.4: Commit**

```bash
git add buildSrc/templates/windows-terminal-rounded.template.theme.json
git commit -m "feat: add rounded theme template with arc placeholders"
```

---

### Task 6.3: Update UIThemeGenerator for Variants

**Subtask 6.3.1: Modify UIThemeGenerator to support variants**

Bestand: `buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt`

Voeg nieuwe functie toe:

```kotlin
import variants.ThemeVariant

/**
 * Generate UI theme for specific variant
 */
fun generateVariant(
    scheme: WindowsTerminalColorScheme,
    variant: ThemeVariant,
    outputDir: File
): File {
    val palette = scheme.toColorPalette()

    // Choose template based on variant
    val templateName = when (variant) {
        ThemeVariant.Standard -> "windows-terminal.template.theme.json"
        ThemeVariant.Rounded -> "windows-terminal-rounded.template.theme.json"
    }

    val templateFile = File("buildSrc/templates/$templateName")
    require(templateFile.exists()) { "Template not found: $templateName" }

    val template = templateFile.readText()

    // Replace all placeholders (colors + arc values)
    var content = template
        // Existing color replacements...
        .replace("\$wt_name$", scheme.name + variant.suffix)
        .replace("\$wt_scheme_name$", scheme.name)
        .replace("\$wt_background$", scheme.background)
        .replace("\$wt_foreground$", scheme.foreground)
        // ... (all existing replacements) ...

    // Add arc value replacements
    variant.arcValues.toPlaceholders().forEach { (placeholder, value) ->
        content = content.replace(placeholder, value)
    }

    // Detect dark/light theme
    val isDark = ColorUtils.isColorDark(scheme.background)
    content = content.replace("\$wt_dark$", isDark.toString())

    // Write output
    val fileName = "${scheme.name}${variant.suffix}.theme.json"
    val outputFile = File(outputDir, fileName)
    outputFile.writeText(content)

    return outputFile
}

// Modify existing generate() function to use generateVariant:
fun generate(scheme: WindowsTerminalColorScheme, outputDir: File): List<File> {
    val generatedFiles = mutableListOf<File>()

    // Generate all variants
    for (variant in ThemeVariant.all()) {
        val file = generateVariant(scheme, variant, outputDir)
        generatedFiles.add(file)
        println("Generated ${variant.displayName.ifEmpty { "standard" }} variant: ${file.name}")
    }

    return generatedFiles
}
```

**Subtask 6.3.2: Update GenerateThemesFromWindowsTerminal task**

Bestand: `buildSrc/src/main/kotlin/tasks/GenerateThemesFromWindowsTerminal.kt`

Update de generate loop:

```kotlin
for (scheme in schemes) {
    try {
        println("Generating themes for: ${scheme.name}")

        // Generate UI theme variants (standard + rounded)
        val uiThemeFiles = UIThemeGenerator.generate(scheme, themesDir)
        println("  Generated ${uiThemeFiles.size} UI theme variant(s)")

        // Generate editor color scheme (shared between variants)
        val xmlFile = XMLColorSchemeGenerator.generate(scheme, themesDir)
        println("  Generated editor scheme: ${xmlFile.name}")

        successCount++
    } catch (e: Exception) {
        println("  Error: ${e.message}")
        errorCount++
    }
}

println("\nGeneration complete:")
println("  Schemes processed: $successCount")
println("  UI theme variants: ${successCount * 2}") // 2 variants per scheme
println("  Editor schemes: $successCount")
println("  Errors: $errorCount")
```

**Subtask 6.3.3: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: SUCCESS

**Subtask 6.3.4: Commit**

```bash
git add buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt
git add buildSrc/src/main/kotlin/tasks/GenerateThemesFromWindowsTerminal.kt
git commit -m "feat: generate standard and rounded variants for each theme"
```

---

### Task 6.4: Test Rounded Variant Generation

**Subtask 6.4.1: Clean existing generated themes**

```bash
rm src/main/resources/themes/*.theme.json
```

**Subtask 6.4.2: Generate themes with variants**

Run: `./gradlew generateThemesFromWindowsTerminal`
Expected: Generates 120+ theme.json files (60 standard + 60 rounded)

**Subtask 6.4.3: Verify Lovelace variants exist**

```bash
ls -1 src/main/resources/themes/Lovelace*.theme.json
```

Expected output:
```
Lovelace.theme.json
Lovelace Rounded.theme.json
```

**Subtask 6.4.4: Compare standard vs rounded Lovelace**

Read both files:
- `src/main/resources/themes/Lovelace.theme.json`
- `src/main/resources/themes/Lovelace Rounded.theme.json`

Verify:
- Standard has `"Component": { "arc": 0 }`
- Rounded has `"Component": { "arc": 8 }`
- Rounded has `"Popup": { "arc": 12 }`
- Beide hebben identieke kleuren

**Subtask 6.4.5: Count total generated files**

```bash
echo "UI Themes (standard + rounded):"
ls -1 src/main/resources/themes/*.theme.json | wc -l

echo "Editor Schemes (shared):"
ls -1 src/main/resources/themes/*.xml | wc -l
```

Expected:
- UI Themes: 120+ (2 per scheme)
- Editor Schemes: 60+ (1 per scheme)

**Subtask 6.4.6: Commit generated themes**

```bash
git add src/main/resources/themes/
git commit -m "feat: generate standard and rounded variants for all themes"
```

---

### Task 6.5: Update Plugin Metadata

**Subtask 6.5.1: Update plugin.xml theme registrations**

Bestand: `src/main/resources/META-INF/plugin.xml`

Voor elke theme moet er nu 2 entries zijn. Bijvoorbeeld voor Lovelace:

```xml
<!-- Lovelace Standard -->
<themeProvider id="com.github.cubical6.lovelace" path="/themes/Lovelace.theme.json"/>

<!-- Lovelace Rounded -->
<themeProvider id="com.github.cubical6.lovelace.rounded" path="/themes/Lovelace Rounded.theme.json"/>
```

**NOTE:** Dit kan geautomatiseerd worden met een script, of manueel voor Lovelace only als test.

**Subtask 6.5.2: Update README met rounded variant info**

Bestand: `README.md`

Update de features sectie:

```markdown
## Features

- **120+ Theme Variants**: Import iTerm color schemes and generate both Standard and Rounded UI variants
- **Standard Variant**: Classic sharp corners (arc = 0)
- **Rounded Variant**: Modern rounded UI with polished corners (arc = 6-12)
- **60+ Editor Color Schemes**: Syntax highlighting shared between variants
- **Enhanced Color Derivation**: 60+ algorithmically derived colors from 16-color palette
- **Automatic iTerm Import**: Convert .itermcolors files to JetBrains themes
```

**Subtask 6.5.3: Update CHANGELOG**

Bestand: `CHANGELOG.md`

Voeg toe aan `[Unreleased]` sectie:

```markdown
### Added
- Rounded theme variants for modern UI style
- ThemeVariant infrastructure (Standard and Rounded)
- Rounded template with arc properties
- Automatic generation of both variants for each color scheme

### Changed
- UIThemeGenerator now generates 2 variants per color scheme
- GenerateThemesFromWindowsTerminal outputs 120+ theme files (60 standard + 60 rounded)
- Plugin.xml now registers both variants
```

**Subtask 6.5.4: Commit documentation**

```bash
git add README.md CHANGELOG.md src/main/resources/META-INF/plugin.xml
git commit -m "docs: add rounded variant documentation and plugin registration"
```

---

### Task 6.6: Build and Validate

**Subtask 6.6.1: Build plugin with variants**

Run: `./gradlew buildPlugin`
Expected: SUCCESS

**Subtask 6.6.2: Verify JAR contents**

```bash
unzip -l build/distributions/*.jar | grep "Lovelace.*theme.json"
```

Expected: Both Lovelace.theme.json AND "Lovelace Rounded.theme.json"

**Subtask 6.6.3: Test in IDE (manual)**

1. Install plugin in IntelliJ
2. Go to Settings > Appearance & Behavior > Appearance > Theme
3. Verify beide "Lovelace" and "Lovelace Rounded" beschikbaar zijn
4. Switch tussen beiden en verifieer visuele verschillen (rounded corners)

**Subtask 6.6.4: Create final tag**

```bash
git tag -a v2.1.0-rounded -m "Add rounded theme variants"
```

---

## COMPLETION CHECKLIST

Na voltooiing van alle fases:

- [ ] Fase 1: iTerm Import Infrastructure (7 commits)
- [ ] Fase 2: Enhanced Color Derivation (2 commits)
- [ ] Fase 3: Template Updates (3 commits)
- [ ] Fase 4: Lovelace Reference (3 commits)
- [ ] Fase 5: Validation & Testing (3 commits)
- [ ] Fase 6: Rounded Theme Variants (6 commits)

**Totaal: ~24 commits, 120+ theme variants, 60+ editor schemes, iTerm import support**

---

## TROUBLESHOOTING

### If tests fail:
1. Check Kotlin version compatibility
2. Verify ColorUtils functions exist
3. Check test resource files are in correct location

### If template generation fails:
1. Verify all placeholders in templates match ColorPalette properties
2. Check for typos in placeholder names
3. Ensure all new ColorPalette properties are returned by toColorPalette()

### If iTerm import fails:
1. Verify XML structure of .itermcolors file
2. Check that all required colors are present
3. Review parser error messages for specific missing keys

### If themes look wrong:
1. Compare generated colors with expected values
2. Check color derivation algorithms
3. Verify template placeholders are correctly replaced
4. Review git diff to see what changed

---

**EINDE VAN TASKS.MD**
