# Sprint 6 Testing - Quick Start Guide

## TL;DR - What You Need to Do

**Goal:** Test that editor color schemes automatically apply when you select a UI theme.

**Time Required:** ~40 minutes

**You Need:**
- IntelliJ IDEA (or any JetBrains IDE)
- The plugin ZIP file (already built)

---

## Quick Testing Steps

### 1. Verify Build (2 minutes)
```bash
cd /home/bithons/github/jetbrains-melly-theme
./verify_build.sh
```
**Expected:** All checks pass with ✅

### 2. Install Plugin (5 minutes)
1. Open IntelliJ IDEA
2. Go to **Settings → Plugins**
3. Click gear icon → **Install Plugin from Disk**
4. Select: `/home/bithons/github/jetbrains-melly-theme/build/distributions/one-dark-theme.zip`
5. Restart IDE

### 3. Core Test - Automatic Application (5 minutes)
This is the MOST IMPORTANT test!

1. **Settings → Appearance & Behavior → Appearance**
2. Select theme: **"wt-dracula"**
3. Click **Apply**
4. Navigate to **Settings → Editor → Color Scheme**
5. **CHECK:** Does it show "wt-dracula"? (It should - automatically!)

**If YES:** ✅ Feature works!
**If NO:** ❌ Critical bug - report in test results

### 4. Verify Syntax Highlighting (5 minutes)
1. Open any code file (.java, .kt, .py, etc.)
2. **CHECK:** Are keywords, strings, comments all colored differently?

**If YES:** ✅ Works!
**If NO:** ❌ Issue - report it

### 5. Test Multiple Themes (5 minutes)
Switch between these themes (Settings → Appearance → Theme):
- wt-dracula
- wt-nord
- wt-gruvbox-dark

For each, verify the editor color scheme changes automatically.

### 6. Document Results (5 minutes)
Fill out the checklist in `SPRINT_6_TEST_RESULTS.md`

---

## Critical Success Criteria

The plugin PASSES if:
1. ✅ Plugin installs without errors
2. ✅ Editor color schemes visible in dropdown (57 themes)
3. ✅ **Editor scheme AUTOMATICALLY applies when selecting UI theme** (most important!)
4. ✅ Syntax highlighting works
5. ✅ No errors during theme switching

The plugin FAILS if:
1. ❌ Plugin won't install or crashes IDE
2. ❌ Editor schemes don't appear in Color Scheme settings
3. ❌ **Editor scheme does NOT auto-apply** (must manually select)
4. ❌ Syntax highlighting broken or missing colors
5. ❌ Errors appear when switching themes

---

## Files to Use

| File | Purpose |
|------|---------|
| **MANUAL_TESTING_INSTRUCTIONS.md** | Detailed step-by-step instructions |
| **SPRINT_6_TEST_RESULTS.md** | Checklist to fill out as you test |
| **verify_build.sh** | Automated verification script |
| **build/distributions/one-dark-theme.zip** | Plugin to install |

---

## Quick Troubleshooting

**Problem:** Themes don't appear in dropdown
- **Fix:** Restart IDE, check plugin is enabled in Settings → Plugins

**Problem:** Editor scheme doesn't auto-apply
- **Fix:** This is a critical bug - document it in test results

**Problem:** Can't find the plugin ZIP
- **Fix:** Run `./gradlew buildPlugin` to rebuild

---

## Questions?

- **Detailed instructions:** Read MANUAL_TESTING_INSTRUCTIONS.md
- **What to check:** See SPRINT_6_TEST_RESULTS.md
- **Need to verify build first:** Run `./verify_build.sh`

---

**Ready to test?**
1. Run `./verify_build.sh` to confirm build is correct
2. Open MANUAL_TESTING_INSTRUCTIONS.md for detailed steps
3. Keep SPRINT_6_TEST_RESULTS.md open to check off items as you go

**Good luck!** 🚀
