# Theme Spot-Check Reference

Quick reference for manually validating the 5 popular themes after regeneration.

---

## 1. Dracula

**Source Scheme:** `windows-terminal-schemes/dracula.json`

**Expected Theme Files:**
- `src/main/resources/themes/wt-dracula-*.theme.json`
- `src/main/resources/themes/wt-dracula-*.xml`

### Key Colors (from source)
```json
{
  "background": "#282a36",
  "foreground": "#f8f8f2",
  "black": "#21222c",
  "red": "#ff5555",
  "green": "#50fa7b",
  "yellow": "#f1fa8c",
  "blue": "#bd93f9",
  "purple": "#ff79c6",
  "cyan": "#8be9fd",
  "white": "#f8f8f2"
}
```

### Quick Validation
```bash
# View theme JSON
cat src/main/resources/themes/wt-dracula-*.theme.json | jq '.name, .dark, .ui.background, .ui.foreground'

# Expected output:
# "Dracula"
# true
# "#282a36" (or derived)
# "#f8f8f2" (or derived)

# Check XML has colors
grep -c "option name" src/main/resources/themes/wt-dracula-*.xml
# Expected: 50+ color definitions
```

**Visual Check:**
- [ ] Theme name: "Dracula"
- [ ] Dark theme: `true`
- [ ] Purple/pink accent colors visible
- [ ] Dark blue-gray background

---

## 2. Solarized Dark

**Source Scheme:** `windows-terminal-schemes/solarized-dark.json`

**Expected Theme Files:**
- `src/main/resources/themes/wt-solarized-dark-*.theme.json`
- `src/main/resources/themes/wt-solarized-dark-*.xml`

### Quick Validation
```bash
cat src/main/resources/themes/wt-solarized-dark-*.theme.json | jq '.name, .dark'

# Expected:
# "Solarized Dark"
# true

grep -c "option name" src/main/resources/themes/wt-solarized-dark-*.xml
```

**Visual Check:**
- [ ] Theme name: "Solarized Dark"
- [ ] Dark theme: `true`
- [ ] Low contrast, muted colors
- [ ] Classic Solarized palette

---

## 3. Nord

**Source Scheme:** `windows-terminal-schemes/nord.json`

**Expected Theme Files:**
- `src/main/resources/themes/wt-nord-*.theme.json`
- `src/main/resources/themes/wt-nord-*.xml`

### Quick Validation
```bash
cat src/main/resources/themes/wt-nord-*.theme.json | jq '.name, .dark'

# Expected:
# "Nord"
# true

grep -c "option name" src/main/resources/themes/wt-nord-*.xml
```

**Visual Check:**
- [ ] Theme name: "Nord"
- [ ] Dark theme: `true`
- [ ] Cool blue-gray colors
- [ ] Arctic/snow theme aesthetic

---

## 4. Gruvbox Dark

**Source Scheme:** `windows-terminal-schemes/gruvbox-dark.json`

**Expected Theme Files:**
- `src/main/resources/themes/wt-gruvbox-dark-*.theme.json`
- `src/main/resources/themes/wt-gruvbox-dark-*.xml`

### Quick Validation
```bash
cat src/main/resources/themes/wt-gruvbox-dark-*.theme.json | jq '.name, .dark'

# Expected:
# "Gruvbox Dark"
# true

grep -c "option name" src/main/resources/themes/wt-gruvbox-dark-*.xml
```

**Visual Check:**
- [ ] Theme name: "Gruvbox Dark"
- [ ] Dark theme: `true`
- [ ] Warm, retro colors
- [ ] Brown/orange/yellow tones

---

## 5. Tokyo Night

**Source Scheme:** `windows-terminal-schemes/tokyo-night.json`

**Expected Theme Files:**
- `src/main/resources/themes/wt-tokyo-night-*.theme.json`
- `src/main/resources/themes/wt-tokyo-night-*.xml`

### Quick Validation
```bash
cat src/main/resources/themes/wt-tokyo-night-*.theme.json | jq '.name, .dark'

# Expected:
# "Tokyo Night"
# true

grep -c "option name" src/main/resources/themes/wt-tokyo-night-*.xml
```

**Visual Check:**
- [ ] Theme name: "Tokyo Night"
- [ ] Dark theme: `true`
- [ ] Modern dark blue/purple palette
- [ ] Vibrant accent colors

---

## General Validation Checklist

For ALL themes, verify:

### JSON Structure
```bash
# Run for each theme
cat src/main/resources/themes/wt-THEME-*.theme.json | jq 'keys'

# Should contain at minimum:
# - "name"
# - "dark"
# - "author"
# - "ui"
# - "colors" (or similar)
```

### XML Structure
```bash
# Check XML has scheme name
grep 'scheme name=' src/main/resources/themes/wt-THEME-*.xml | head -1

# Count color definitions (should be 50+)
grep -c '<option name=' src/main/resources/themes/wt-THEME-*.xml

# Count attributes (should be 50+)
grep -c '<attributes>' src/main/resources/themes/wt-THEME-*.xml
```

### Required Fields

**JSON (`*.theme.json`):**
- [ ] `name` - Theme display name
- [ ] `dark` - Boolean (true for dark themes)
- [ ] `author` - "Windows Terminal Converter"
- [ ] `ui` - Object with UI color definitions
- [ ] Valid JSON syntax

**XML (`*.xml`):**
- [ ] `<scheme name="...">` - Root element with name
- [ ] `<colors>` - Section with color definitions
- [ ] `<attributes>` - Section for syntax highlighting
- [ ] Minimum 50+ color options
- [ ] Valid XML syntax

---

## Quick One-Liner Checks

### Verify all 5 themes exist
```bash
ls -1 src/main/resources/themes/wt-{dracula,solarized-dark,nord,gruvbox-dark,tokyo-night}-*.theme.json
```

### Check all are dark themes
```bash
for theme in dracula solarized-dark nord gruvbox-dark tokyo-night; do
  echo -n "$theme: "
  cat src/main/resources/themes/wt-${theme}-*.theme.json | jq -r '.dark'
done
```

### Verify theme names
```bash
for theme in dracula solarized-dark nord gruvbox-dark tokyo-night; do
  echo -n "$theme: "
  cat src/main/resources/themes/wt-${theme}-*.theme.json | jq -r '.name'
done
```

### Check file sizes
```bash
ls -lh src/main/resources/themes/wt-{dracula,solarized-dark,nord,gruvbox-dark,tokyo-night}-*.theme.json
ls -lh src/main/resources/themes/wt-{dracula,solarized-dark,nord,gruvbox-dark,tokyo-night}-*.xml
```

---

## Advanced Validation

### Check color derivation quality

```bash
# For Dracula, check if background color is preserved
THEME_FILE="src/main/resources/themes/wt-dracula-*.theme.json"
EXPECTED_BG="#282a36"

ACTUAL_BG=$(cat $THEME_FILE | jq -r '.ui["*"].background // .ui.background // "not-found"')

if [[ "$ACTUAL_BG" == *"282a36"* ]]; then
  echo "✓ Dracula background color preserved"
else
  echo "⚠ Dracula background color may have changed: $ACTUAL_BG"
fi
```

### Compare with source scheme

```bash
# Example: Compare Dracula colors
SOURCE="windows-terminal-schemes/dracula.json"
THEME="src/main/resources/themes/wt-dracula-*.theme.json"

echo "Source background: $(jq -r '.background' $SOURCE)"
echo "Theme background: $(cat $THEME | jq -r '.ui["*"].background // .ui.background')"
```

---

## Spot-Check Completion

After checking all 5 themes, mark as complete:

- [ ] Dracula - All checks passed
- [ ] Solarized Dark - All checks passed
- [ ] Nord - All checks passed
- [ ] Gruvbox Dark - All checks passed
- [ ] Tokyo Night - All checks passed

**If all passed:**
✓ Spot-check complete! Themes are ready for integration testing.

**If any failed:**
✗ Review failures, check Gradle output, and regenerate if needed.

---

Generated for FASE 5.1 - Theme Regeneration
