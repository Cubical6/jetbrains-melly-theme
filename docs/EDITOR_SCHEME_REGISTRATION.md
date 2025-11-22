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

**Updated method:**
```kotlin
fun updatePluginXml(themes: List<ThemeMetadata>)
```

Now registers **both** entries for each theme:
- `<themeProvider>` for the UI theme
- `<bundledColorScheme>` for the editor color scheme

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

- ✅ **TASK-1100:** PluginXmlUpdater enhanced with bundledColorScheme support
- ✅ **TASK-1101:** GenerateThemesWithMetadata updated for dual registration
- ✅ **TASK-1102:** Comprehensive unit tests added (9 test cases)
- ⏳ **TASK-1103:** Regenerate plugin.xml (blocked by network issues)
- ⏳ **TASK-1104:** Build and manual testing (pending regeneration)
- ✅ **TASK-1105:** Documentation updated (this file)
- ⏳ **TASK-1106:** Commit and push (pending completion)

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
