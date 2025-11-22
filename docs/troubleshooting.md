# Troubleshooting Guide

## Common Issues

### Issue 1: Placeholders Not Replaced

**Symptoms:**
- Generated `.theme.json` files contain `$variable_name$` placeholders
- IntelliJ shows errors when loading the theme
- Colors appear as literal text instead of hex values

**Causes:**

1. **Naming mismatch**: Placeholder name doesn't match the key in ColorPalette map
2. **Missing color**: Color not added to ColorPalette.toMap()
3. **Typo**: Spelling error in placeholder or map key

**Solutions:**

#### Check Placeholder Naming

All derived color placeholders MUST use `snake_case`:

**Wrong:**
```json
"Component.background": "$wt_surfaceLight$"
```

**Right:**
```json
"Component.background": "$wt_surface_light$"
```

Exception: ANSI colors use camelCase (`$wt_brightBlue$`, `$wt_brightRed$`, etc.)

#### Verify ColorPalette.toMap()

Check that the placeholder key exists in `/buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`:

```kotlin
fun toMap(): Map<String, String> {
    return mapOf(
        // ...
        "wt_surface_light" to surfaceLight,  // ✓ Key matches placeholder
        // ...
    )
}
```

#### Debug Process

1. **Search the codebase** for the placeholder name:
   ```bash
   grep -r "surface_light" buildSrc/src/main/kotlin/colorschemes/
   ```

2. **Check template file** for exact placeholder:
   ```bash
   grep -r "surfaceLight" buildSrc/templates/
   ```

3. **Compare**: If they don't match, fix the placeholder name

4. **Regenerate themes**:
   ```bash
   ./gradlew generateThemesFromWindowsTerminal
   ```

5. **Verify** no `$` symbols remain in generated files:
   ```bash
   grep '\$' themes/ui/*.theme.json
   ```

#### Recent Fix Example

In Task 1, we discovered placeholders like `$wt_surfaceLight$` that should have been `$wt_surface_light$`. The fix involved:

1. Identified in `ColorPalette.toMap()` that keys use snake_case
2. Updated template to use snake_case placeholders
3. All placeholders were successfully replaced after the fix

---

### Issue 2: Rounded Corners Not Showing

**Symptoms:**
- Rounded variant theme doesn't show rounded corners
- UI components appear with sharp corners
- No visual difference between Standard and Rounded variants

**Multiple Possible Causes:**

#### Cause 1: Arc Placeholders Not Replaced

**Check:** Search for `$arc_` in generated theme file

```bash
grep '\$arc_' themes/ui/*_rounded.theme.json
```

If found, arc placeholders weren't replaced.

**Solution:** Verify `ThemeVariant.toPlaceholders()` is called in UIThemeGenerator:

```kotlin
// In UIThemeGenerator.generateVariant()
variant.arcValues.toPlaceholders().forEach { (placeholder, value) ->
    content = content.replace(placeholder, value)
}
```

This was fixed in Task 3.

#### Cause 2: Wrong Template Used

**Check:** Verify the Rounded variant uses the correct template

```kotlin
val templateName = when (variant) {
    ThemeVariant.Standard -> "windows-terminal.template.theme.json"
    ThemeVariant.Rounded -> "windows-terminal-rounded.template.theme.json"  // Must use rounded template
}
```

#### Cause 3: Template Missing Arc Properties

**Check:** Verify rounded template contains arc properties:

```bash
grep '"arc":' buildSrc/templates/windows-terminal-rounded.template.theme.json
```

Should find entries like:
```json
"arc": $arc_button$
```

#### Cause 4: IntelliJ Version Too Old

**Requirement:** IntelliJ 2024.1 or later

Arc properties are a modern UI feature. Older versions don't support them.

**Check version:**
1. Open IntelliJ IDEA
2. Help > About
3. Look for version number

**Solution:** Upgrade to IntelliJ 2024.1 or later

#### Cause 5: New UI Not Enabled

**Check:** Verify New UI is enabled (required for modern features)

1. Settings > Appearance & Behavior > New UI
2. Enable "New UI" checkbox
3. Restart IntelliJ

---

### Issue 3: Colors Look Wrong

**Symptoms:**
- Theme appears too bright/dark
- Colors don't match Windows Terminal preview
- Specific UI elements have unexpected colors

**Causes & Solutions:**

#### Cause 1: Wrong Parent Theme

Dark themes should use `ExperimentalDark`, light themes should use `ExperimentalLight`.

**Check:** Look at the `dark` field in generated theme:

```json
{
  "name": "My Theme",
  "dark": true,                    // ← Should be true for dark themes
  "parentTheme": "ExperimentalDark" // ← Must match the "dark" value
}
```

The `detectDarkTheme()` method automatically sets this based on background luminance (threshold: 100).

**Fix:** If incorrect, the scheme's background color might be ambiguous. Manually override if needed.

#### Cause 2: Color Derivation Issues

Derived colors are calculated from base colors using `ColorUtils` methods. If base colors are unusual, derived colors might not look good.

**Example:**
```kotlin
val surface = ColorUtils.lighten(background, 0.05)  // 5% lighter
```

If the background is already very light, this might not provide enough contrast.

**Solution:** Check ColorUtils methods in `/buildSrc/src/main/kotlin/utils/ColorUtils.kt` and adjust blending percentages if needed.

#### Cause 3: Missing selectionBackground

If the Windows Terminal scheme doesn't define `selectionBackground`, a default is calculated:

```kotlin
put("wt_selectionBackground", selectionBackground ?: ColorUtils.blend(background, foreground, 0.2))
```

This might not match your expectations.

**Solution:** Add explicit `selectionBackground` to the color scheme JSON.

---

## How to Debug Theme Generation

### 1. Enable Verbose Logging

Run with verbose output:

```bash
./gradlew generateThemesFromWindowsTerminal --info
```

### 2. Check Validation Errors

The generation process validates color schemes:

```kotlin
val validationErrors = scheme.validate()
require(validationErrors.isEmpty()) {
    "Invalid color scheme: ${validationErrors.joinToString("; ")}"
}
```

If validation fails, you'll see specific error messages.

### 3. Inspect Generated JSON

Open the generated theme file and check:

1. **Search for `$`**: Any dollar signs mean unreplaced placeholders
   ```bash
   grep '\$' themes/ui/my_theme.theme.json
   ```

2. **Validate JSON**: Use a JSON validator
   ```bash
   python -m json.tool themes/ui/my_theme.theme.json > /dev/null
   ```

3. **Check color values**: All should be hex colors (#RRGGBB)
   ```bash
   grep -E '"#[0-9A-Fa-f]{6}"' themes/ui/my_theme.theme.json
   ```

### 4. Run Unit Tests

Test the individual components:

```bash
# Test template processor
./gradlew test --tests "themes.TemplateProcessorTest"

# Test UI theme generator
./gradlew test --tests "generators.UIThemeGeneratorTest"

# Test color palette generation
./gradlew test --tests "colorschemes.WindowsTerminalColorSchemeTest"
```

### 5. Compare with Working Theme

Compare a broken theme with a known working theme:

```bash
diff -u themes/ui/working_theme.theme.json themes/ui/broken_theme.theme.json
```

---

## How to Verify Generated Themes

### Automated Verification

#### Step 1: Check for Unreplaced Placeholders

```bash
# Should return no results
grep -r '\$wt_' themes/ui/
grep -r '\$arc_' themes/ui/
```

#### Step 2: Validate JSON

```bash
for file in themes/ui/*.theme.json; do
    echo "Validating $file"
    python -m json.tool "$file" > /dev/null || echo "FAILED: $file"
done
```

#### Step 3: Check Required Fields

Every theme must have:
- `name` (string)
- `dark` (boolean)
- `parentTheme` (string)
- `editorScheme` (string path)
- `colors` (object)
- `ui` (object)

```bash
# Check for required fields
jq '.name, .dark, .parentTheme, .editorScheme' themes/ui/my_theme.theme.json
```

### Visual Verification

#### Step 1: Build Plugin

```bash
./gradlew buildPlugin
```

#### Step 2: Install in IntelliJ

1. Go to Settings > Plugins
2. Click gear icon > Install Plugin from Disk
3. Select `build/distributions/*.zip`
4. Restart IntelliJ

#### Step 3: Test Theme

1. Settings > Appearance & Behavior > Appearance
2. Select generated theme from dropdown
3. Click Apply

**What to check:**
- [ ] Overall color scheme matches expectations
- [ ] Syntax highlighting uses appropriate colors
- [ ] UI elements are readable (good contrast)
- [ ] Selection background is visible
- [ ] Focus indicators are clear
- [ ] Error/warning colors are distinguishable
- [ ] Rounded corners appear (Rounded variant only)

#### Step 4: Test Different Scenarios

- [ ] Open code files (Java, Kotlin, Python, etc.)
- [ ] View diff highlighting
- [ ] Check tool windows (Project, Structure, etc.)
- [ ] Test popup menus and dialogs
- [ ] Examine search results highlighting
- [ ] Review terminal colors

---

## Diagnostic Commands

### List All Generated Themes

```bash
ls -lh themes/ui/*.theme.json
```

### Count Placeholders in Template

```bash
grep -o '\$[a-zA-Z_][a-zA-Z0-9_]*\$' buildSrc/templates/windows-terminal.template.theme.json | sort -u | wc -l
```

### Find All Placeholder Definitions

```bash
grep -n '"wt_' buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt
```

### Check Color Palette Size

```bash
# Should show 70+ colors (base + derived)
grep -c 'put("wt_' buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt
```

### Verify Template Processor

```bash
./gradlew test --tests "themes.TemplateProcessorTest" --info
```

---

## Getting Help

If you're still stuck after trying these troubleshooting steps:

1. **Check recent changes**: Review recent commits for template or generator changes
   ```bash
   git log --oneline --all -- buildSrc/templates/ buildSrc/src/main/kotlin/generators/
   ```

2. **Review test failures**: Run all tests and check for patterns
   ```bash
   ./gradlew test --tests "*Theme*" --tests "*Template*"
   ```

3. **Compare with reference implementation**: Check the documentation files in `/docs`

4. **Create a minimal reproduction**: Try generating a theme with a simple color scheme

---

## Related Documentation

- [Theme Generation](theme-generation.md) - System architecture and process flow
- [Placeholder Conventions](placeholder-conventions.md) - Naming rules and reference
