# Fix Kotlin Version Incompatibility Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Resolve the Kotlin version incompatibility between Gradle's embedded Kotlin (1.6.21) and project dependencies (1.8.20+)

**Architecture:** The buildSrc module uses the `kotlin-dsl` plugin which relies on Gradle's embedded Kotlin version (1.6.21 in Gradle 7.5.1). However, the project's root build.gradle uses Kotlin 1.8.22, and the buildSrc dependencies include libraries compiled with Kotlin 1.8.0+ (kotlinx-coroutines-core 1.7.3 requires Kotlin 1.8.0+). This creates a metadata version mismatch.

**Tech Stack:** Gradle 7.5.1, Kotlin 1.6.21 (embedded), Kotlin 1.8.22 (project), Groovy build scripts, Kotlin DSL (buildSrc)

---

## Root Cause Analysis

### Primary Issue: Kotlin Metadata Version Mismatch

The build fails with this core error:
```
Module was compiled with an incompatible version of Kotlin.
The binary version of its metadata is 1.8.0, expected version is 1.6.0.
```

**Why this happens:**

1. **Gradle 7.5.1** embeds **Kotlin 1.6.21** (verified via `./gradlew --version`)
2. **buildSrc/build.gradle.kts** uses `kotlin-dsl` plugin, which depends on Gradle's embedded Kotlin (1.6.21)
3. **buildSrc dependencies** include `kotlinx-coroutines-core:1.7.3`, compiled with Kotlin 1.8.0 metadata
4. **Root build.gradle** uses Kotlin plugin version 1.8.22
5. Kotlin 1.6.21 compiler cannot read Kotlin 1.8.0 metadata format

### Secondary Issues: API Incompatibilities

Once metadata is readable, additional compilation errors occur because the buildSrc code uses APIs introduced after Kotlin 1.6.21:

- `kotlin.Result` as return type (restricted in 1.6.x)
- `isRegularFile()` extension (requires newer kotlin-stdlib)
- `lowercase()` / `lowercaseChar()` (replaced `toLowerCase()` in Kotlin 1.5+, but stdlib version matters)
- `runBlocking`, `async`, coroutines APIs from incompatible kotlinx-coroutines version

**File locations with errors:**
- `buildSrc/src/main/kotlin/colorschemes/ColorSchemeParser.kt`
- `buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt`
- `buildSrc/src/main/kotlin/generators/XMLColorSchemeGenerator.kt`
- `buildSrc/src/main/kotlin/mapping/ConsoleColorMapper.kt`
- `buildSrc/src/main/kotlin/mapping/SyntaxColorInference.kt`
- `buildSrc/src/main/kotlin/tasks/GenerateThemesFromWindowsTerminal.kt`
- `buildSrc/src/main/kotlin/utils/ColorUtils.kt`

---

## Solution Strategy

**Two possible approaches:**

### Approach A: Upgrade Gradle (Recommended)
Upgrade Gradle to version 8.0+ which embeds Kotlin 1.8.20+, aligning with project dependencies and root build script.

**Pros:**
- Cleanly resolves metadata version mismatch
- Allows modern Kotlin APIs
- Future-proof (Gradle 7.5.1 is from 2022)
- No code changes needed (likely)

**Cons:**
- May require testing plugin compatibility (org.jetbrains.intellij 1.9.0)
- Gradle wrapper update needed

### Approach B: Downgrade Dependencies
Keep Gradle 7.5.1, downgrade buildSrc dependencies and refactor code to Kotlin 1.6-compatible APIs.

**Pros:**
- No Gradle version change
- Minimal infrastructure risk

**Cons:**
- Requires code refactoring across 7+ files
- Locks project to older Kotlin APIs
- Uses outdated coroutines library
- Technical debt accumulation

**Recommendation:** Approach A (Gradle upgrade) is superior unless there's a specific constraint preventing Gradle 8+ usage.

---

## Task 1: Verify Plugin Compatibility

**Files:**
- Read: `build.gradle:10-14`

**Step 1: Check IntelliJ Platform Gradle Plugin compatibility**

Research compatibility:
- Current: `org.jetbrains.intellij:1.9.0`
- Gradle 8 requirement: Check [plugin compatibility matrix](https://plugins.gradle.org/plugin/org.jetbrains.intellij)

```bash
# If needed, identify latest compatible version
echo "Check https://github.com/JetBrains/gradle-intellij-plugin/releases"
echo "Version 1.9.0 released 2022-09 - likely supports Gradle 7.x only"
echo "Gradle 8.x likely requires plugin version 1.10.0+"
```

**Step 2: Check other plugins**

```bash
# org.jlleitschuh.gradle.ktlint:8.2.0 - very old (2019)
# org.kordamp.gradle.markdown:2.2.0 - check Gradle 8 compat
```

**Step 3: Document findings**

Create notes on required plugin version bumps if any are Gradle 8-incompatible.

---

## Task 2: Upgrade Gradle Wrapper

**Files:**
- Modify: `gradle/wrapper/gradle-wrapper.properties`
- Execute: `./gradlew wrapper --gradle-version 8.5`

**Step 1: Update Gradle wrapper to 8.5**

```bash
./gradlew wrapper --gradle-version 8.5
```

**Expected output:**
```
BUILD SUCCESSFUL in Xs
```

**Step 2: Verify wrapper upgrade**

```bash
./gradlew --version
```

**Expected output:**
```
Gradle 8.5
Kotlin: 1.9.x (or 1.8.x)
```

**Step 3: Check generated wrapper files**

```bash
cat gradle/wrapper/gradle-wrapper.properties | grep distributionUrl
```

**Expected:**
```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```

---

## Task 3: Update Plugin Versions (If Needed)

**Files:**
- Modify: `build.gradle:10-14`

**Step 1: Update IntelliJ Platform plugin (if incompatible)**

If Task 1 identified incompatibility, update in `build.gradle`:

```groovy
plugins {
    id 'org.jetbrains.intellij' version '1.17.2'  // Update from 1.9.0
    id 'org.jetbrains.kotlin.jvm' version '1.9.22'  // Optional: update Kotlin
    id 'org.jlleitschuh.gradle.ktlint' version '12.1.0'  // Update from ancient 8.2.0
    id 'org.kordamp.gradle.markdown' version '2.2.0'
}
```

**Step 2: Update ktlint plugin configuration**

If ktlint plugin is upgraded, may need configuration changes:

```bash
# Check for deprecation warnings after upgrade
./gradlew tasks | grep ktlint
```

---

## Task 4: Test Build with Gradle 8

**Files:**
- Execute: `./gradlew clean build`

**Step 1: Run clean build**

```bash
./gradlew clean build
```

**Expected outcomes:**

**Best case:** Build succeeds completely
```
BUILD SUCCESSFUL in Xs
```

**Likely case:** Some deprecation warnings but build succeeds
```
> Task :buildSrc:compileKotlin
> Task :compileKotlin
BUILD SUCCESSFUL in Xs
3 actionable tasks: 3 executed
```

**Problematic case:** New errors appear due to Gradle 8 API changes
```
FAILURE: Build failed with an exception.
* What went wrong:
[Plugin compatibility issue or Gradle API change]
```

**Step 2: Review build output**

```bash
# If build succeeds with warnings, save output
./gradlew clean build 2>&1 | tee build-output.log
```

**Step 3: Verify buildSrc compilation**

```bash
# Check buildSrc specifically
./gradlew :buildSrc:build
```

**Expected:**
```
BUILD SUCCESSFUL
```

This confirms Kotlin version compatibility is resolved.

---

## Task 5: Handle Build Failures (If Any)

**Files:**
- TBD based on errors

**Step 1: Analyze failure messages**

If Task 4 produces failures, categorize:

1. **Plugin API changes**: Update plugin usage in build.gradle
2. **Gradle API changes**: Update custom task code in buildSrc
3. **Kotlin API changes**: Update buildSrc source code

**Step 2: Address plugin API changes**

Example - IntelliJ plugin API changes:
```groovy
// Old (Gradle 7 / plugin 1.9.0)
intellij {
  version.set('2021.3.1')
  type.set('IC')
}

// New (Gradle 8 / plugin 1.17+) - likely same, check docs
intellij {
  version.set('2021.3.1')
  type = 'IC'  // Possibly no .set() needed
}
```

**Step 3: Address Gradle API changes**

If custom tasks fail, check Gradle 8 migration guide:
```bash
echo "See https://docs.gradle.org/8.0/userguide/upgrading_version_7.html"
```

Common changes:
- `configurations.compile` → `configurations.implementation`
- Provider API strictness increased
- Some deprecated APIs removed

**Step 4: Re-test after fixes**

```bash
./gradlew clean build
```

---

## Task 6: Verify Theme Generation Tasks

**Files:**
- Execute: `./gradlew createThemes`

**Step 1: Run custom theme generation task**

```bash
./gradlew createThemes
```

**Expected:**
```
> Task :createThemes
[Theme generation output]
BUILD SUCCESSFUL in Xs
```

This verifies buildSrc custom tasks work with Gradle 8.

**Step 2: Verify generated theme files exist**

```bash
ls -la src/main/resources/themes/ | head -20
```

**Expected:** Theme .json and .xml files present

**Step 3: Check for data corruption**

```bash
# Verify a sample theme file has valid content
head -20 src/main/resources/themes/3024\ Night.json
```

**Expected:** Valid JSON structure visible

---

## Task 7: Run Full Test Suite

**Files:**
- Execute: `./gradlew test`

**Step 1: Run all tests**

```bash
./gradlew test
```

**Expected:**
```
BUILD SUCCESSFUL in Xs
X tests completed, X succeeded
```

**Step 2: Check for test failures**

If tests fail:
```bash
# View test report
cat build/reports/tests/test/index.html
```

**Step 3: Investigate buildSrc tests specifically**

```bash
./gradlew :buildSrc:test
```

**Expected:**
```
BUILD SUCCESSFUL
5 tests completed, 5 succeeded
```

Per buildSrc/build.gradle.kts, there are JUnit/Kotest tests that must pass.

---

## Task 8: Commit Gradle Upgrade

**Files:**
- Stage: `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat`
- Stage: `build.gradle` (if plugins updated)

**Step 1: Stage wrapper files**

```bash
git add gradle/wrapper/gradle-wrapper.properties \
        gradle/wrapper/gradle-wrapper.jar \
        gradlew \
        gradlew.bat
```

**Step 2: Stage build config changes**

```bash
git add build.gradle
```

**Step 3: Commit**

```bash
git commit -m "$(cat <<'EOF'
fix: upgrade Gradle to 8.5 to resolve Kotlin version incompatibility

Root cause: Gradle 7.5.1 embeds Kotlin 1.6.21, incompatible with
buildSrc dependencies (kotlinx-coroutines-core 1.7.3 requires
Kotlin 1.8.0+ metadata). This caused compilation failures in buildSrc.

Solution: Upgraded Gradle wrapper to 8.5 (embeds Kotlin 1.9.x),
resolving metadata version mismatch and API compatibility issues.

Plugin updates (if any):
- org.jetbrains.intellij: 1.9.0 -> 1.17.2
- org.jlleitschuh.gradle.ktlint: 8.2.0 -> 12.1.0

Verified:
- buildSrc compiles successfully
- createThemes task executes
- Test suite passes

Fixes build failure with errors:
- "Module was compiled with an incompatible version of Kotlin"
- "Unresolved reference" errors in buildSrc source files

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

---

## Alternative Task Flow: Approach B (Downgrade Dependencies)

**Only use if Gradle upgrade is blocked by external constraints.**

### Alt Task 1: Downgrade kotlinx-coroutines

**Files:**
- Modify: `buildSrc/build.gradle.kts:13`

**Step 1: Find Kotlin 1.6-compatible coroutines version**

Check Maven Central for kotlinx-coroutines-core versions compatible with Kotlin 1.6:
- Version 1.6.4 supports Kotlin 1.6.21

**Step 2: Update dependency**

```kotlin
dependencies {
  implementation("org.jsoup:jsoup:1.13.1")
  implementation("com.google.code.gson:gson:2.8.9")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")  // Downgrade from 1.7.3

  testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
  testImplementation("io.kotest:kotest-assertions-core:5.5.4")
}
```

**Step 3: Verify compatibility**

```bash
./gradlew :buildSrc:dependencies --configuration implementation
```

Check that all transitive Kotlin dependencies are 1.6.x.

---

### Alt Task 2: Refactor Code for Kotlin 1.6 Compatibility

**Files:**
- Modify: `buildSrc/src/main/kotlin/colorschemes/ColorSchemeParser.kt:32`
- Modify: `buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt:305`
- Modify: `buildSrc/src/main/kotlin/utils/ColorUtils.kt:40`
- Modify: `buildSrc/src/main/kotlin/mapping/ConsoleColorMapper.kt:106`
- Modify: `buildSrc/src/main/kotlin/generators/XMLColorSchemeGenerator.kt:183`

**Step 1: Replace kotlin.Result return type**

In `ColorSchemeParser.kt:32`:

```kotlin
// Old (Kotlin 1.7+)
fun parseColorScheme(file: File): Result<ColorScheme> { ... }

// New (Kotlin 1.6 compatible)
fun parseColorScheme(file: File): ColorScheme? {
    return try {
        // parsing logic
        colorScheme
    } catch (e: Exception) {
        logger.error("Failed to parse", e)
        null
    }
}
```

Update all call sites to handle nullable return instead of Result.

**Step 2: Replace lowercase() with toLowerCase()**

In multiple files (UIThemeGenerator.kt:305, ColorUtils.kt:40, etc.):

```kotlin
// Old
val lower = text.lowercase()

// New (Kotlin 1.6)
val lower = text.toLowerCase(java.util.Locale.ROOT)
```

**Step 3: Replace lowercaseChar() with toLowerCase()**

In `ColorUtils.kt:40`:

```kotlin
// Old
val lowerChar = char.lowercaseChar()

// New (Kotlin 1.6)
val lowerChar = char.toLowerCase()
```

**Step 4: Fix smart cast issues**

In `SyntaxColorInference.kt:113,117,130,132,136`:

```kotlin
// Old (smart cast fails in closure)
var finalColor: String? = null
closure {
    if (finalColor != null) {
        use(finalColor)  // Smart cast fails
    }
}

// New (explicit not-null assertion or temp variable)
var finalColor: String? = null
closure {
    val color = finalColor
    if (color != null) {
        use(color)  // Smart cast succeeds
    }
}
```

---

### Alt Task 3: Test Downgraded Build

**Files:**
- Execute: `./gradlew clean build`

**Step 1: Clean rebuild**

```bash
./gradlew clean build
```

**Expected:**
```
BUILD SUCCESSFUL in Xs
```

**Step 2: Verify no Kotlin version errors**

Check build output contains no "incompatible version" errors.

**Step 3: Run tests**

```bash
./gradlew test
```

Ensure refactored code still passes tests.

---

### Alt Task 4: Commit Downgrade Changes

**Files:**
- Stage: `buildSrc/build.gradle.kts`
- Stage: All refactored .kt files

**Step 1: Stage changes**

```bash
git add buildSrc/build.gradle.kts \
        buildSrc/src/main/kotlin/colorschemes/ColorSchemeParser.kt \
        buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt \
        # ... (all modified files)
```

**Step 2: Commit**

```bash
git commit -m "$(cat <<'EOF'
fix: downgrade kotlinx-coroutines and refactor for Kotlin 1.6 compatibility

Root cause: Gradle 7.5.1 embeds Kotlin 1.6.21, incompatible with
kotlinx-coroutines-core 1.7.3 (requires Kotlin 1.8.0+ metadata).

Solution: Downgraded kotlinx-coroutines to 1.6.4 and refactored
buildSrc code to use Kotlin 1.6-compatible APIs:
- Result<T> return types -> nullable types
- lowercase() -> toLowerCase()
- lowercaseChar() -> toLowerCase()
- Fixed smart cast issues in closures

Verified: buildSrc compiles, tests pass, theme generation works.

Fixes build failure with Kotlin metadata version mismatch.

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

---

## Testing & Verification Checklist

After implementing either approach:

- [ ] `./gradlew --version` shows expected Gradle/Kotlin versions
- [ ] `./gradlew clean` succeeds
- [ ] `./gradlew :buildSrc:build` succeeds without errors
- [ ] `./gradlew build` succeeds completely
- [ ] `./gradlew test` - all tests pass
- [ ] `./gradlew createThemes` generates themes without errors
- [ ] Theme files in `src/main/resources/themes/` are valid
- [ ] `./gradlew runIde` launches IntelliJ with plugin (if IDE path configured)
- [ ] No Kotlin version warnings in build output
- [ ] Git commit created with detailed explanation

---

## Troubleshooting Guide

### If Gradle 8 build still fails after upgrade:

1. **Check plugin compatibility**
   - Visit plugin GitHub repos
   - Check minimum Gradle version requirements
   - Update to latest compatible versions

2. **Check for Gradle API deprecations**
   ```bash
   ./gradlew build --warning-mode all
   ```
   - Review deprecation warnings
   - Consult Gradle 8 migration guide

3. **Check Kotlin version alignment**
   ```bash
   ./gradlew buildEnvironment
   ```
   - Ensure buildSrc Kotlin = Gradle embedded Kotlin
   - Ensure root project Kotlin ≤ Gradle embedded Kotlin + 1 minor version

### If coroutines downgrade causes runtime errors:

1. **Verify coroutine API compatibility**
   - Check if buildSrc code uses coroutines features added after 1.6.4
   - Example: `async`/`await` available, but structured concurrency differs

2. **Check stdlib transitive dependencies**
   ```bash
   ./gradlew :buildSrc:dependencies --configuration runtimeClasspath
   ```
   - Ensure no Kotlin stdlib 1.8+ leaks through transitive deps

---

## Summary

**Recommended approach:** Upgrade Gradle to 8.5 (Tasks 1-8)

**Root cause:** Kotlin metadata version incompatibility between Gradle 7.5.1's embedded Kotlin 1.6.21 and kotlinx-coroutines-core 1.7.3 (requires Kotlin 1.8.0+)

**Solution:** Upgrade Gradle wrapper to 8.5, which embeds Kotlin 1.9.x, eliminating version mismatch

**Fallback:** Downgrade kotlinx-coroutines to 1.6.4 and refactor code for Kotlin 1.6 APIs (Alt Tasks 1-4)

**Estimated completion:**
- Approach A: ~15-30 minutes (mostly testing/verification)
- Approach B: ~1-2 hours (code refactoring across 7+ files)

**Risk level:**
- Approach A: Low (standard Gradle upgrade, well-documented)
- Approach B: Medium (code changes, potential API behavior differences)
