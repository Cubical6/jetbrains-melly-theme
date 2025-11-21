# Migration Guide

**Version:** 1.0
**Date:** 2025-11-21
**Target Audience:** Existing One Dark Theme users
**Plugin Version:** 6.0.0 (Windows Terminal Integration)

---

## Table of Contents

1. [Overview](#overview)
2. [What's Changing](#whats-changing)
3. [What's Staying the Same](#whats-staying-the-same)
4. [Migration Scenarios](#migration-scenarios)
5. [Step-by-Step Migration](#step-by-step-migration)
6. [Theme Cleanup](#theme-cleanup)
7. [Troubleshooting](#troubleshooting)
8. [FAQ](#faq)

---

## Overview

### What is this migration?

Version 6.0.0 of the JetBrains One Dark Theme plugin introduces **Windows Terminal color scheme integration**. This is a major enhancement that allows you to use any Windows Terminal color scheme as a complete IntelliJ theme with matching console colors and intelligent syntax highlighting.

### Do I need to migrate?

**No, migration is optional.** Your existing One Dark themes will continue to work exactly as before. This is a **non-breaking change** with **full backward compatibility**.

You should consider migrating if you want to:
- Try new color schemes from the Windows Terminal ecosystem (60+ included)
- Have consistent colors between your terminal and IDE
- Customize your IDE theme by importing your own Windows Terminal color schemes
- Access a wider variety of professionally-designed color schemes

### What if I'm happy with the current themes?

**You don't need to do anything.** Your favorite One Dark themes (regular, italic, vivid, vivid-italic) will continue to work exactly as they do today. You can simply ignore the new Windows Terminal themes if you prefer to stick with the classic One Dark look.

---

## What's Changing

### New Features in 6.0.0

#### 1. **60+ New Windows Terminal Themes**

The plugin now includes 60 professionally-designed color schemes from the Windows Terminal ecosystem:

**Popular Dark Themes:**
- Dracula, Nord, Tokyo Night, Gruvbox Dark, Catppuccin Mocha
- GitHub Dark, Material, Monokai, Solarized Dark
- Rose Pine (3 variants), Night Owl, Poimandres, Oceanic Next
- JetBrains Darcula, Atom, Zenburn, IR Black, Molokai
- And 40+ more!

**Light Themes:**
- Gruvbox Light, Solarized Light, Ayu Light, Tomorrow
- Atom One Light, One Half Light, Rose Pine Dawn, Pencil Light

#### 2. **Windows Terminal Integration**

- Full ANSI color mapping (all 16 console colors + special colors)
- Intelligent syntax highlighting inference from terminal colors
- Consistent colors between terminal and IDE
- Easy addition of custom Windows Terminal color schemes

#### 3. **Build System Enhancements**

- New Gradle tasks for theme generation
- Automated theme validation
- Performance optimizations (60-70% faster builds)
- Parallel theme processing

#### 4. **Developer Tools**

- Accessibility audit tools (WCAG AA compliance)
- Color mapping utilities
- Comprehensive documentation

### Breaking Changes

**None.** This is a fully backward-compatible release.

### Deprecated Features

The following internal APIs are deprecated but still functional:
- `Extensions.kt` - Use direct Kotlin idioms instead
- `ThemeConstructor.kt` (legacy mode) - Use new generator classes

These will be removed in version 7.0.0 (planned for 2026).

---

## What's Staying the Same

### Unchanged Features

✅ **All existing One Dark themes** (4 variants) work exactly as before
✅ **Theme installation process** - No changes to how you install or update
✅ **Theme selection** - Same UI in Preferences
✅ **Plugin settings** - No new required configuration
✅ **Performance** - Actually improved by 60-70%
✅ **Compatibility** - Same IntelliJ version support (2021.3+)
✅ **Console colors** - Existing themes unaffected

### Your Settings

✅ **Selected theme** - Will remain active after update
✅ **Custom settings** - Any customizations will be preserved
✅ **Editor colors** - Your personalized color scheme settings stay intact

---

## Migration Scenarios

### Scenario 1: Happy with Current Theme

**Situation:** You love your current One Dark theme and don't want to change.

**Action Required:** None! Continue using your existing theme.

**What Happens:**
- Plugin updates to 6.0.0
- Your current theme selection remains active
- New themes appear in the theme dropdown (can be ignored)
- Everything continues to work as before

---

### Scenario 2: Try New Themes

**Situation:** You want to explore the new Windows Terminal themes while keeping your current theme as fallback.

**Action Required:** Just switch themes in the UI.

**What Happens:**
1. Update plugin to 6.0.0
2. Go to `Preferences | Appearance & Behavior | Appearance`
3. Open theme dropdown - you'll see 60+ new themes
4. Select a new theme (e.g., "Dracula", "Tokyo Night")
5. Click OK to apply
6. If you don't like it, switch back to your original One Dark theme

**No permanent changes are made** - switching is instant and reversible.

---

### Scenario 3: Full Migration to Windows Terminal Theme

**Situation:** You want to fully adopt a Windows Terminal theme and potentially clean up old themes.

**Action Required:** Switch theme and optionally clean up.

**Steps:**
1. **Choose New Theme:**
   - Go to `Preferences | Appearance & Behavior | Appearance`
   - Select a Windows Terminal theme from dropdown
   - Click OK to apply

2. **Test Thoroughly:**
   - Use your IDE for a few days
   - Check console colors (Run tool window)
   - Review syntax highlighting in your languages
   - Test in different lighting conditions

3. **Optional Cleanup:**
   - Once you're sure, you can hide old themes (see [Theme Cleanup](#theme-cleanup))
   - Or just leave them - they don't affect performance

---

### Scenario 4: Import Custom Windows Terminal Scheme

**Situation:** You have a custom Windows Terminal color scheme you want to use in IntelliJ.

**Action Required:** Import and rebuild plugin (advanced users only).

**Steps:**
1. **Export from Windows Terminal:**
   - Open Windows Terminal settings (`Ctrl+,`)
   - Go to `Color schemes` section
   - Copy your color scheme JSON

2. **Add to Plugin:**
   - Save JSON to `windows-terminal-schemes/my-custom-theme.json`
   - Ensure valid format (see `windows-terminal-schemes/SCHEMES.md`)

3. **Rebuild Plugin:**
   ```bash
   ./gradlew generateThemesFromWindowsTerminal
   ./gradlew buildPlugin
   ```

4. **Reinstall Plugin:**
   - `Preferences | Plugins | Installed`
   - Uninstall current version
   - Install from disk: `build/distributions/*.zip`

5. **Select Your Theme:**
   - Your custom theme will appear in the theme dropdown

---

## Step-by-Step Migration

### For Most Users (Scenario 1 or 2)

**Time Required:** 2-5 minutes

#### Step 1: Update Plugin

1. Open IntelliJ IDEA
2. Go to `Preferences | Plugins | Installed`
3. Find "One Dark Theme"
4. Click "Update" (if available)
5. Restart IDE when prompted

#### Step 2: Verify Current Theme

1. After restart, check your current theme is still active
2. `Preferences | Appearance & Behavior | Appearance`
3. Your previous theme selection should be unchanged

#### Step 3: Explore New Themes (Optional)

1. Click the theme dropdown
2. Scroll through 60+ available themes
3. Look for familiar names:
   - **Dracula** - Purple and pink accents
   - **Tokyo Night** - Clean, modern dark theme
   - **Nord** - Arctic, blue-tinted theme
   - **Gruvbox Dark** - Warm, retro colors
   - **Catppuccin Mocha** - Soothing pastels

4. Select a theme to preview (immediate change)
5. Click "Cancel" to revert, or "OK" to keep

#### Step 4: Fine-Tune (Optional)

If you like a new theme but want to adjust it:

1. `Preferences | Editor | Color Scheme`
2. Duplicate your selected theme (gear icon → "Duplicate")
3. Customize colors as desired
4. Your custom version won't be overwritten by updates

---

### For Advanced Users (Scenario 4)

**Time Required:** 15-30 minutes

See detailed instructions in [Adding Custom Schemes](#scenario-4-import-custom-windows-terminal-scheme) above and in `windows-terminal-schemes/README.md`.

---

## Theme Cleanup

### Managing the Theme List

With 64 total themes (4 One Dark + 60 Windows Terminal), the theme dropdown can be long. Here are strategies to manage it:

#### Option 1: Just Ignore

The theme list is alphabetically sorted. Your favorite theme is easy to find. No cleanup needed.

#### Option 2: Hide Unused Themes (Advanced)

**Warning:** This involves modifying plugin files. Not recommended for most users.

You can edit `src/main/resources/META-INF/plugin.xml` to comment out unwanted themes:

```xml
<!-- Uncomment themes you want to use, comment out others -->
<themeProvider id="wt.dracula" path="/themes/dracula.theme.json"/>
<!-- <themeProvider id="wt.tokyo-night" path="/themes/tokyo-night.theme.json"/> -->
```

After editing, rebuild the plugin:
```bash
./gradlew buildPlugin
```

#### Option 3: Custom Build

If you want only specific themes, you can:
1. Fork the repository
2. Remove unwanted JSON files from `windows-terminal-schemes/`
3. Build your custom version
4. Install from disk

---

## Troubleshooting

### Issue: Plugin won't update

**Symptoms:** Update button is grayed out or fails

**Solution:**
1. Manually uninstall current version
2. Restart IDE
3. Download the latest `.zip` from GitHub releases and install from disk

---

### Issue: Theme looks wrong after update

**Symptoms:** Colors are off, UI is broken, text is unreadable

**Solution:**
1. Verify you're using the correct theme:
   - `Preferences | Appearance & Behavior | Appearance`
   - Check theme name matches what you expect

2. Try switching to another theme and back:
   - Select "IntelliJ Light" or "Darcula"
   - Apply
   - Switch back to your desired theme

3. Reset to defaults:
   - `File | Manage IDE Settings | Restore Default Settings`
   - Warning: This resets ALL settings

4. Clear caches:
   - `File | Invalidate Caches`
   - Select "Invalidate and Restart"

---

### Issue: Console colors are wrong

**Symptoms:** Terminal or run window has incorrect colors

**Solution:**
1. Check you're using a Windows Terminal theme (not legacy One Dark)
2. Verify console color scheme matches UI theme:
   - `Preferences | Editor | Color Scheme | Console Colors`
   - Should match your selected theme

3. Reset console colors:
   - `Preferences | Editor | Color Scheme`
   - Select your theme
   - Right-click → "Restore Defaults"

---

### Issue: Can't find my favorite theme

**Symptoms:** Theme dropdown doesn't show a theme you used before

**Solution:**
1. Check theme naming:
   - Old: "One Dark", "One Dark Italic", "One Dark Vivid", "One Dark Vivid Italic"
   - These should still be available

2. If missing, check plugin installation:
   ```bash
   ls ~/.local/share/JetBrains/*/One-Dark-Theme/
   ```

3. Reinstall plugin if themes are missing

---

### Issue: Build fails with custom scheme

**Symptoms:** `generateThemesFromWindowsTerminal` task fails

**Solution:**
1. Validate your JSON:
   ```bash
   python3 -m json.tool your-scheme.json
   ```

2. Check required properties:
   - `name` (string)
   - `background` (hex color)
   - `foreground` (hex color)
   - All 16 ANSI colors: `black`, `red`, `green`, `yellow`, `blue`, `purple`, `cyan`, `white`, `brightBlack`, `brightRed`, etc.

3. Run validation:
   ```bash
   ./gradlew importWindowsTerminalSchemes
   ```
   Check `build/reports/wt-scheme-validation.txt` for errors

4. See `windows-terminal-schemes/SCHEMES.md` for format specification

---

### Issue: Performance is slower after update

**Symptoms:** IDE feels sluggish, high CPU usage

**Solution:**
This shouldn't happen - performance is actually 60-70% better. But if you experience issues:

1. Check background tasks:
   - Bottom toolbar → "Background Tasks"
   - Ensure no tasks are stuck

2. Increase memory:
   - `Help | Change Memory Settings`
   - Increase to at least 2GB

3. Disable unused plugins:
   - `Preferences | Plugins`
   - Disable plugins you don't use

4. Clear caches:
   - `File | Invalidate Caches | Invalidate and Restart`

---

## FAQ

### General Questions

#### Q: Will my current theme stop working?
**A:** No. All existing One Dark themes (regular, italic, vivid, vivid-italic) continue to work exactly as before.

#### Q: Do I have to use Windows Terminal themes?
**A:** No. The new themes are optional. You can continue using your existing theme indefinitely.

#### Q: Will the plugin be renamed?
**A:** No. The plugin will remain "One Dark Theme" for compatibility and brand recognition.

#### Q: Can I use both old and new themes?
**A:** Yes! All themes coexist peacefully. Switch between them anytime in Preferences.

#### Q: Will this affect my IDE performance?
**A:** No. In fact, build performance is 60-70% better in 6.0.0.

---

### Technical Questions

#### Q: What version of IntelliJ is required?
**A:** Same as before: 2021.3+ (all editions: IC, IU, PyCharm, PhpStorm, etc.)

#### Q: Are Windows Terminal themes compatible with all JetBrains IDEs?
**A:** Yes. They work in all JetBrains IDEs that support custom themes (2021.3+).

#### Q: Can I export my IntelliJ theme to Windows Terminal?
**A:** Not yet. This is planned for a future release (see TASKS.md, TASK-801).

#### Q: How are Windows Terminal colors mapped to IntelliJ?
**A:** See `docs/COLOR_MAPPING.md` and `docs/SYNTAX_INFERENCE_ALGORITHM.md` for detailed technical documentation.

#### Q: Do Windows Terminal themes support all programming languages?
**A:** Yes. The intelligent syntax inference algorithm maps the 16 ANSI colors to 100+ IntelliJ color attributes, covering all supported languages.

#### Q: Can I customize a Windows Terminal theme?
**A:** Yes. Duplicate any theme in `Preferences | Editor | Color Scheme`, then customize as desired. Your changes won't be overwritten by updates.

---

### Theme Selection Questions

#### Q: Which theme should I choose?
**A:** This is personal preference! Here are some recommendations:

**Popular Starting Points:**
- **Dracula** - Vibrant, purple-tinted, very popular
- **Tokyo Night** - Clean, modern, great readability
- **Nord** - Calm, blue-tinted, easy on the eyes
- **Gruvbox Dark** - Warm, retro, low contrast
- **Catppuccin Mocha** - Soothing pastels, gentle colors

**For High Productivity:**
- **JetBrains Darcula** - Official JetBrains dark theme
- **GitHub Dark** - Familiar if you use GitHub
- **Solarized Dark** - Scientifically designed for readability

**For Light Themes:**
- **Solarized Light** - Classic light theme, high contrast
- **Gruvbox Light** - Warm, retro light colors
- **Ayu Light** - Contemporary, clean design

Try several and use them for a few days each. Your eyes will tell you which one feels right.

#### Q: Can I see screenshots of all themes?
**A:** Yes! See `windows-terminal-schemes/SCHEMES.md` for links to screenshots of all 60 themes.

#### Q: How do I know which theme has good contrast?
**A:** All themes have been audited for WCAG AA compliance. See `reports/ACCESSIBILITY_AUDIT_REPORT.md` for detailed contrast ratios.

---

### Custom Scheme Questions

#### Q: Can I import my own Windows Terminal color scheme?
**A:** Yes! See [Scenario 4](#scenario-4-import-custom-windows-terminal-scheme) above for detailed instructions.

#### Q: Where can I find Windows Terminal color schemes?
**A:** Several sources:
- [Windows Terminal Themes](https://windowsterminalthemes.dev/) - Browse and download
- [iTerm2 Color Schemes](https://github.com/mbadolato/iTerm2-Color-Schemes) - 450+ schemes
- [terminal.sexy](https://terminal.sexy/) - Create your own
- Windows Terminal settings (if you've customized your own)

#### Q: What format do custom schemes need to be in?
**A:** Standard Windows Terminal JSON format. See `windows-terminal-schemes/SCHEMES.md` for specification and examples.

#### Q: Can I share my custom theme with others?
**A:** Yes! You can:
1. Share your JSON file directly
2. Submit a pull request to add it to the plugin
3. Publish your custom build on GitHub

See `docs/CONTRIBUTING_SCHEMES.md` for contribution guidelines.

---

### Support Questions

#### Q: Where can I get help?
**A:** Several options:
- [Documentation](https://one-dark.gitbook.io/jetbrains)
- [GitHub Issues](https://github.com/one-dark/jetbrains-one-dark-theme/issues)

#### Q: How do I report a bug?
**A:** [Open a GitHub issue](https://github.com/one-dark/jetbrains-one-dark-theme/issues/new) with:
- Plugin version (6.0.0+)
- IntelliJ version and edition
- Theme name
- Description of the issue
- Screenshots if applicable

#### Q: How can I contribute?
**A:** See `docs/CONTRIBUTING_SCHEMES.md` for:
- Adding new Windows Terminal schemes
- Fixing color mappings
- Improving documentation
- Reporting issues

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-11-21 | Initial migration guide for 6.0.0 release |

---

## Additional Resources

- [Windows Terminal Schemes Collection](../windows-terminal-schemes/SCHEMES.md) - All included themes
- [Color Mapping Documentation](COLOR_MAPPING.md) - How colors are mapped
- [Syntax Inference Algorithm](SYNTAX_INFERENCE_ALGORITHM.md) - How syntax highlighting works
- [Contributing Schemes Guide](CONTRIBUTING_SCHEMES.md) - Add your own themes
- [Accessibility Audit Report](../reports/ACCESSIBILITY_AUDIT_REPORT.md) - WCAG compliance data
- [Performance Metrics](PERFORMANCE_METRICS.md) - Build performance data
- [Versioning Strategy](VERSIONING_STRATEGY.md) - Theme versioning details

---

## Summary

**Key Takeaways:**

✅ **Backward Compatible** - All existing themes work unchanged
✅ **Optional Migration** - You don't have to switch if you don't want to
✅ **Easy to Try** - Switching themes is instant and reversible
✅ **60+ New Themes** - Huge variety of professionally-designed color schemes
✅ **Better Performance** - 60-70% faster builds
✅ **Well-Documented** - Comprehensive docs for all features
✅ **Accessible** - All themes audited for WCAG AA compliance
✅ **Customizable** - Import your own Windows Terminal schemes

**Bottom Line:** Update confidently. Your favorite theme will still work, and you'll have access to 60+ new themes to explore at your leisure.

---

*Last updated: 2025-11-21*
*Plugin version: 6.0.0*
*Migration guide version: 1.0*
