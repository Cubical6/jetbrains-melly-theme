#!/usr/bin/env kotlin

// Standalone verification script for ITermColorScheme
// Run with: kotlinc -script verify-iterm-colorscheme.kts

// Copy of ITermColorScheme implementation
data class ITermColorScheme(
    val name: String,
    val ansiColors: Map<Int, ITermColor>,
    val foreground: ITermColor,
    val background: ITermColor,
    val selection: ITermColor,
    val cursor: ITermColor,
    val cursorText: ITermColor? = null,
    val bold: ITermColor? = null,
    val link: ITermColor? = null
) {
    data class ITermColor(
        val red: Float,
        val green: Float,
        val blue: Float,
        val alpha: Float = 1.0f
    ) {
        init {
            require(red in 0.0f..1.0f) { "Red must be 0.0-1.0, got $red" }
            require(green in 0.0f..1.0f) { "Green must be 0.0-1.0, got $green" }
            require(blue in 0.0f..1.0f) { "Blue must be 0.0-1.0, got $blue" }
            require(alpha in 0.0f..1.0f) { "Alpha must be 0.0-1.0, got $alpha" }
        }

        fun toHexString(): String {
            val r = (red * 255).toInt().coerceIn(0, 255)
            val g = (green * 255).toInt().coerceIn(0, 255)
            val b = (blue * 255).toInt().coerceIn(0, 255)
            return "#%02X%02X%02X".format(r, g, b)
        }

        companion object {
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

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        for (i in 0..15) {
            if (!ansiColors.containsKey(i)) {
                errors.add("Missing ANSI color $i")
            }
        }
        return errors
    }
}

// Test runner
fun main() {
    var passed = 0
    var failed = 0

    fun test(name: String, assertion: () -> Boolean) {
        try {
            if (assertion()) {
                println("✓ $name")
                passed++
            } else {
                println("✗ $name - assertion failed")
                failed++
            }
        } catch (e: Exception) {
            println("✗ $name - ${e.message}")
            failed++
        }
    }

    println("Testing ITermColorScheme implementation...")
    println("=" * 60)

    // Test 1: toHexString converts float RGB to hex correctly
    test("toHexString converts float RGB to hex correctly") {
        val color = ITermColorScheme.ITermColor(0.29f, 0.18f, 0.44f)
        val hex = color.toHexString()
        hex == "#4A2E70"
    }

    // Test 2: toHexString handles edge cases
    test("toHexString: pure black") {
        ITermColorScheme.ITermColor(0f, 0f, 0f).toHexString() == "#000000"
    }

    test("toHexString: pure white") {
        ITermColorScheme.ITermColor(1f, 1f, 1f).toHexString() == "#FFFFFF"
    }

    test("toHexString: pure red") {
        ITermColorScheme.ITermColor(1f, 0f, 0f).toHexString() == "#FF0000"
    }

    // Test 3: fromHex converts hex to float RGB correctly
    test("fromHex converts hex to float RGB correctly") {
        val color = ITermColorScheme.ITermColor.fromHex("#4A2E70")
        val redOk = kotlin.math.abs(color.red - 0.29f) < 0.01f
        val greenOk = kotlin.math.abs(color.green - 0.18f) < 0.01f
        val blueOk = kotlin.math.abs(color.blue - 0.44f) < 0.01f
        redOk && greenOk && blueOk
    }

    // Test 4: fromHex handles with and without hash prefix
    test("fromHex handles with/without # prefix") {
        val color1 = ITermColorScheme.ITermColor.fromHex("#FF6B6B")
        val color2 = ITermColorScheme.ITermColor.fromHex("FF6B6B")
        kotlin.math.abs(color1.red - color2.red) < 0.001f &&
        kotlin.math.abs(color1.green - color2.green) < 0.001f &&
        kotlin.math.abs(color1.blue - color2.blue) < 0.001f
    }

    // Test 5: ITermColor validates range
    test("ITermColor validates range - negative value") {
        try {
            ITermColorScheme.ITermColor(-0.1f, 0.5f, 0.5f)
            false
        } catch (e: IllegalArgumentException) {
            true
        }
    }

    test("ITermColor validates range - value > 1.0") {
        try {
            ITermColorScheme.ITermColor(0.5f, 1.1f, 0.5f)
            false
        } catch (e: IllegalArgumentException) {
            true
        }
    }

    // Test 6: validate detects missing ANSI colors
    test("validate detects missing ANSI colors") {
        val scheme = ITermColorScheme(
            name = "Incomplete",
            ansiColors = mapOf(
                0 to ITermColorScheme.ITermColor(0f, 0f, 0f),
                1 to ITermColorScheme.ITermColor(1f, 0f, 0f)
            ),
            foreground = ITermColorScheme.ITermColor(1f, 1f, 1f),
            background = ITermColorScheme.ITermColor(0f, 0f, 0f),
            selection = ITermColorScheme.ITermColor(0.5f, 0.5f, 0.5f),
            cursor = ITermColorScheme.ITermColor(1f, 1f, 1f)
        )
        val errors = scheme.validate()
        errors.size == 14 && errors.any { it.contains("ANSI color 2") }
    }

    println("=" * 60)
    println("Results: $passed passed, $failed failed")

    if (failed > 0) {
        System.exit(1)
    }
}

main()
