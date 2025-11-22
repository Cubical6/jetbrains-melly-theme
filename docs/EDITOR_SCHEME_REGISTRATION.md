# Editor Color Scheme Registration in IntelliJ Plugins

## Problem Identified

During testing on November 21, 2025, we discovered a critical issue with the plugin's editor color scheme registration.

### The Issue

The plugin was only registering UI themes via `<themeProvider>` entries in `plugin.xml`:

```xml
<themeProvider id="wt-dracula-abc123" path="/themes/wt-dracula-abc123.theme.json"/>
```

And the `.theme.json` files referenced editor color schemes:

```json
{
  "name": "Dracula",
  "editorScheme": "/themes/wt-dracula-abc123.xml"
}
```

**However, this is insufficient!** According to JetBrains documentation, editor color schemes must be **separately registered** using the `<bundledColorScheme>` extension point.

### Symptoms

Without proper registration:
- ✅ UI theme loads correctly (toolbars, menus, panels)
- ❌ Editor color scheme is NOT automatically applied when selecting the theme
- ❌ Editor color schemes are NOT visible in Settings → Editor → Color Scheme
- ❌ Users cannot independently select editor color schemes

## Solution Implemented

We implemented a dual registration strategy in **Sprint 6** (TASK-1100 through TASK-1106).

### Changes Made

#### 1. PluginXmlUpdater.kt Enhancement

**File:** `buildSrc/src/main/kotlin/generators/PluginXmlUpdater.kt`

**New method added:**
```kotlin
fun addBundledColorScheme(baseName: String, themesDir: String = "/themes")
```

**Updated method (complete signature):**
```kotlin
fun updatePluginXml(
    themes: List<ThemeMetadata>,
    themesDir: String = "/themes"
): UpdateResult
```

Now registers **both** entries for each theme:
- `<themeProvider>` for the UI theme (path: `$themesDir/${metadata.id}.theme.json`)
- `<bundledColorScheme>` for the editor color scheme (path: `$themesDir/${metadata.id}`)

Returns an `UpdateResult` containing counts of added/removed themes and backup file location.

#### 2. Path Format Requirements

**Critical:** The two entries use different path formats:

```xml
<!-- UI Theme: includes .theme.json extension -->
<themeProvider id="wt-dracula-abc123" path="/themes/wt-dracula-abc123.theme.json"/>

<!-- Editor Color Scheme: NO .xml extension -->
<bundledColorScheme path="/themes/wt-dracula-abc123"/>
```

IntelliJ automatically appends `.icls` or `.xml` to the bundledColorScheme path.

#### 3. GenerateThemesWithMetadata.kt Update

**File:** `buildSrc/src/main/kotlin/tasks/GenerateThemesWithMetadata.kt`

Enhanced logging to show dual registration:

```
Registering themeProvider entries (UI themes)
Registering bundledColorScheme entries (editor color schemes)
✓ plugin.xml updated successfully
  Themes added: 60
  - 60 themeProvider entries (UI themes)
  - 60 bundledColorScheme entries (editor color schemes)
```

#### 4. Comprehensive Unit Tests

**File:** `buildSrc/src/test/kotlin/generators/PluginXmlUpdaterTest.kt`

Added 9 new test cases:
1. Test bundledColorScheme entry creation
2. Test correct path format (no .xml extension)
3. Test dual registration (both entries for each theme)
4. Test duplicate removal before adding
5. Test selective removal of WT bundled color schemes
6. Test custom themes directory support
7. Test XML well-formedness
8. Test path consistency validation
9. Additional duplicate removal scenarios

## Expected plugin.xml Output

After running `./gradlew generateThemesWithMetadata`, the plugin.xml should contain:

```xml
<extensions defaultExtensionNs="com.intellij">

  <!-- UI Themes -->
  <themeProvider id="wt-dracula-abc123" path="/themes/wt-dracula-abc123.theme.json"/>
  <themeProvider id="wt-nord-def456" path="/themes/wt-nord-def456.theme.json"/>
  <themeProvider id="wt-tokyo-night-ghi789" path="/themes/wt-tokyo-night-ghi789.theme.json"/>
  <!-- ... 57 more UI themes ... -->

  <!-- Editor Color Schemes -->
  <bundledColorScheme path="/themes/wt-dracula-abc123"/>
  <bundledColorScheme path="/themes/wt-nord-def456"/>
  <bundledColorScheme path="/themes/wt-tokyo-night-ghi789"/>
  <!-- ... 57 more editor schemes ... -->

</extensions>
```

## How It Works

### When User Selects a Theme

1. **UI Theme Applied:**
   - IntelliJ reads the `<themeProvider>` entry
   - Loads `/themes/wt-dracula-abc123.theme.json`
   - Applies all UI colors (toolbars, menus, panels, etc.)

2. **Editor Scheme Referenced:**
   - The `.theme.json` contains `"editorScheme": "/themes/wt-dracula-abc123.xml"`
   - IntelliJ looks for a registered bundled color scheme at that path

3. **Editor Scheme Applied:**
   - Because we registered `<bundledColorScheme path="/themes/wt-dracula-abc123"/>`
   - IntelliJ finds the scheme and applies it automatically
   - Editor syntax highlighting matches the theme

### Independent Selection

Users can also select editor color schemes independently:
1. Go to Settings → Editor → Color Scheme
2. All 60 Windows Terminal color schemes are now visible
3. Select any scheme (doesn't have to match the UI theme)
4. Editor colors change without affecting UI theme

## Available Template Placeholders

The Windows Terminal template system supports the following color placeholders:

### Base Terminal Colors (16 ANSI colors)
These map directly to Windows Terminal color schemes:
- `$wt_background$` - Main background color
- `$wt_foreground$` - Main foreground/text color
- `$wt_black$`, `$wt_red$`, `$wt_green$`, `$wt_yellow$`, `$wt_blue$`, `$wt_purple$`, `$wt_cyan$`, `$wt_white$`
- `$wt_brightBlack$`, `$wt_brightRed$`, `$wt_brightGreen$`, `$wt_brightYellow$`, `$wt_brightBlue$`, `$wt_brightPurple$`, `$wt_brightCyan$`, `$wt_brightWhite$`

### Special Terminal Colors
- `$wt_cursorColor$` - Cursor color (fallback: foreground)
- `$wt_selectionBackground$` - Selection background (fallback: blend of bg/fg)

### Derived UI Surface Colors (Auto-calculated)
These are automatically lightened versions of the background color for UI consistency:
- `$wt_surface$` - Background lightened 5% (for subtle elevated surfaces)
- `$wt_surface_light$` - Background lightened 10% (for secondary UI elements)
- `$wt_surface_lighter$` - Background lightened 15% (for highlighted areas)

### Derived UI Helper Colors (Auto-calculated)
These blend background and foreground colors for UI elements:
- `$wt_line_numbers$` - Blend bg/fg 30% (for line numbers)
- `$wt_guide_color$` - Blend bg/fg 15% (for indent guides)
- `$wt_divider_color$` - Blend bg/fg 25% (for visual separators)
- `$wt_muted_foreground$` - Blend bg/fg 60% (for less important text)

### Derived Semantic Colors (Auto-calculated)
These blend background with ANSI colors for semantic highlights:
- `$wt_error_background$` - Blend bg/red 20% (for error backgrounds)
- `$wt_warning_background$` - Blend bg/yellow 20% (for warning backgrounds)
- `$wt_info_background$` - Blend bg/blue 20% (for info backgrounds)

**Note**: All derived placeholders are automatically calculated at build time based on the base terminal colors.

## Verification Steps

### After Regenerating plugin.xml

1. **Count Entries:**
   ```bash
   grep -c '<themeProvider' src/main/resources/META-INF/plugin.xml
   # Should output: 60

   grep -c '<bundledColorScheme' src/main/resources/META-INF/plugin.xml
   # Should output: 60
   ```

2. **Verify Path Formats:**
   ```bash
   # themeProvider should have .theme.json
   grep '<themeProvider' src/main/resources/META-INF/plugin.xml | head -3

   # bundledColorScheme should NOT have .xml
   grep '<bundledColorScheme' src/main/resources/META-INF/plugin.xml | head -3
   ```

3. **Check XML Well-formedness:**
   ```bash
   xmllint --noout src/main/resources/META-INF/plugin.xml && echo "XML is valid"
   ```

### Manual Testing in IDE

1. Build plugin: `./gradlew buildPlugin`
2. Install in test IDE
3. Go to Settings → Appearance & Behavior → Appearance
4. Select a Windows Terminal theme (e.g., "Dracula")
5. **Verify:** Editor syntax highlighting changes automatically
6. Go to Settings → Editor → Color Scheme
7. **Verify:** All 60 Windows Terminal schemes are listed
8. Select a different editor scheme
9. **Verify:** Editor colors change, UI theme stays the same

## Troubleshooting

### Editor Background Not Changing with Theme

**Symptom:** Editor background doesn't synchronize when switching themes

If the editor background doesn't change when switching between Windows Terminal themes, check the following template configurations:

**What to Check:**
1. **Console Background**: Ensure `CONSOLE_BACKGROUND_KEY` uses `$wt_background$` (not empty)
2. **Gutter Background**: Ensure `GUTTER_BACKGROUND` uses a placeholder (not hardcoded like `282c34`)
3. **ScrollBar Background**: Ensure `ScrollBar.background` uses a placeholder

**Example fix in template XML:**
```xml
<!-- ❌ Wrong: Empty or hardcoded -->
<option name="CONSOLE_BACKGROUND_KEY" value=""/>
<option name="GUTTER_BACKGROUND" value="282c34"/>

<!-- ✅ Correct: Using placeholders -->
<option name="CONSOLE_BACKGROUND_KEY" value="$wt_background$"/>
<option name="GUTTER_BACKGROUND" value="$wt_background$"/>
```

**Root Cause:** When these critical background attributes are empty or hardcoded, the template system cannot dynamically apply color scheme backgrounds, resulting in the editor keeping its previous background color even when the theme changes.

### Low Contrast Warnings

During build, you may see warnings about low contrast ratios:
```
⚠️  WARNING: Low contrast ratio (2.31) between background and foreground
   WCAG recommends minimum 4.5:1 for normal text, 3:1 for large text
```

**What this means:** The selected color scheme may have readability issues.

**Recommended actions:**
- Choose a different Windows Terminal color scheme with better contrast
- Adjust the foreground/background colors manually in your Windows Terminal settings
- Use high-contrast variants of popular themes

### Build Validation Errors

If you see errors like:
```
Required color placeholder 'wt_background' is missing or empty in color palette
```

**What this means:** A required color is not defined in your Windows Terminal color scheme.

**Solution:** Ensure all base ANSI colors are present in your Windows Terminal color scheme. The template system requires all 16 ANSI colors plus background and foreground to generate a complete theme.

### Editor Scheme Not Applied

**Symptom:** UI theme loads but editor keeps default colors

**Solution:** Check that plugin.xml contains bundledColorScheme entry:
```bash
grep "bundledColorScheme.*wt-dracula" src/main/resources/META-INF/plugin.xml
```

### Editor Schemes Not Visible in Settings

**Symptom:** Color Scheme dropdown doesn't show Windows Terminal themes

**Possible causes:**
1. Missing bundledColorScheme entries in plugin.xml
2. Incorrect path format (has .xml extension)
3. XML files not in /themes/ directory
4. Plugin not properly installed/restarted

### Duplicate Entries

**Symptom:** Multiple bundledColorScheme entries for same theme

**Solution:** The PluginXmlUpdater automatically removes duplicates before adding:
```kotlin
removeAllWtBundledColorSchemes(doc)  // Removes all old entries
// Then adds new entries
```

## JetBrains Documentation References

- [Color Scheme Management](https://plugins.jetbrains.com/docs/intellij/color-scheme-management.html)
- [Theme Structure](https://plugins.jetbrains.com/docs/intellij/theme-structure.html)
- [Themes - Editor Schemes and Background Images](https://plugins.jetbrains.com/docs/intellij/themes-extras.html)

## Implementation Status

**Sprint 6 - Editor Color Scheme Registration:**

**Completed in this PR:**
- ✅ **TASK-1100:** PluginXmlUpdater enhanced with bundledColorScheme support (+214 lines)
- ✅ **TASK-1101:** GenerateThemesWithMetadata updated for dual registration (+30 lines)
- ✅ **TASK-1102:** Comprehensive unit tests added (9 test cases, +220 lines)
- ✅ **TASK-1105:** Documentation created (this file, +250 lines)
- ✅ **TASK-1106:** Changes committed and pushed (commit: aced9eb, 27bda46)

**Remaining tasks (requires user action):**
- ⏳ **TASK-1103:** Regenerate plugin.xml (requires: `./gradlew generateThemesWithMetadata`)
- ⏳ **TASK-1104:** Build and manual testing in IDE (pending TASK-1103)

**Note:** TASK-1103 requires network access to download Gradle dependencies. The implementation is complete; the task simply needs to be executed by the user in an environment with network connectivity.

## Next Steps

1. Once network access is available, run:
   ```bash
   ./gradlew generateThemesWithMetadata
   ```

2. Verify plugin.xml contains both entry types

3. Build and test:
   ```bash
   ./gradlew buildPlugin
   # Install in IDE and verify editor schemes work
   ```

4. Commit and push changes

---

*Document created: 2025-11-21*
*Sprint: 6 - Editor Color Scheme Registration*
*Related Tasks: TASK-1100, TASK-1101, TASK-1102, TASK-1105*
