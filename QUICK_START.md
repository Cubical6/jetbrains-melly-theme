# FASE 5.1 - Quick Start Guide

**Task:** Regenerate All Themes from FASE 5

## TL;DR - What You Need to Do

1. **Review the comprehensive guide:**
   ```bash
   cat FASE_5_1_REGENERATION_GUIDE.md
   ```

2. **Run the theme generation:**
   ```bash
   ./gradlew createThemes
   ```

3. **Validate the results:**
   ```bash
   ./validate-theme-regeneration.sh
   ```

4. **Spot-check popular themes:**
   ```bash
   # Use the reference guide
   cat SPOT_CHECK_REFERENCE.md
   ```

---

## Files Created for This Task

1. **FASE_5_1_REGENERATION_GUIDE.md** - Comprehensive documentation
   - Current state analysis
   - Backup information
   - Step-by-step instructions
   - Verification procedures
   - Troubleshooting guide

2. **validate-theme-regeneration.sh** - Automated validation script
   - Counts theme files
   - Validates JSON/XML syntax
   - Checks file sizes
   - Verifies popular themes
   - Checks plugin.xml updates

3. **SPOT_CHECK_REFERENCE.md** - Manual spot-check guide
   - 5 popular themes to verify
   - Expected values for each
   - Quick validation commands
   - Color verification

4. **QUICK_START.md** - This file (quick reference)

---

## Current State (Pre-Regeneration)

**Themes:** 1 (Lovelace reference implementation)
**Backup:** ✓ Created at `/tmp/theme-backup/themes/`
**Source Schemes:** 59 Windows Terminal color schemes available

---

## Expected Outcome (Post-Regeneration)

**Themes:** 59 complete themes
**Files:** 118 total (59 JSON + 59 XML)
**plugin.xml:** Updated with 59 theme entries
**Build:** Should succeed with no errors

---

## Quick Commands

### Run generation:
```bash
./gradlew createThemes
```

### Validate results:
```bash
./validate-theme-regeneration.sh
```

### Count themes:
```bash
ls -1 src/main/resources/themes/*.theme.json | wc -l
ls -1 src/main/resources/themes/*.xml | wc -l
```

### Check popular themes:
```bash
ls -1 src/main/resources/themes/wt-{dracula,solarized-dark,nord,gruvbox-dark,tokyo-night}-*
```

### Build plugin:
```bash
./gradlew buildPlugin
```

### View Gradle output:
```bash
./gradlew createThemes --info
```

---

## If Something Goes Wrong

### Restore from backup:
```bash
rm -rf src/main/resources/themes/*
cp -r /tmp/theme-backup/themes/* src/main/resources/themes/
```

### Clean and retry:
```bash
./gradlew clean
./gradlew createThemes
```

---

## Next Steps (After Success)

1. ✓ Mark Task 5.1 complete
2. → Proceed to Task 5.2 (Git Diff Analysis)
3. → Proceed to Task 5.3 (Build and Test)
4. → Proceed to Task 5.4 (Integration Testing)

---

**Note:** Claude cannot run Gradle commands in the web environment.
You must run `./gradlew createThemes` locally and report results.

