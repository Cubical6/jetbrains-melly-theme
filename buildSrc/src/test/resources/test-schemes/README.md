# Windows Terminal Test Color Schemes

This directory contains edge case and validation test color schemes for testing the Windows Terminal to IntelliJ theme conversion process.

## Purpose

These test schemes are designed to validate that the theme converter handles various edge cases, unusual scenarios, and error conditions correctly. Each scheme tests specific aspects of the conversion pipeline.

## Test Schemes

### 1. monochrome-test.json
**Purpose:** Tests monochrome/grayscale color detection and syntax highlighting fallback.

**Characteristics:**
- All colors are shades of gray with very low saturation
- Background: #1a1a1a (dark gray)
- Foreground: #d4d4d4 (light gray)
- ANSI colors: Progressive grayscale values (#333333 to #dadada)

**Tests:**
- Monochrome palette detection algorithm
- Syntax highlighting inference fallback to font styles (bold, italic)
- Low saturation color handling

---

### 2. high-contrast-test.json
**Purpose:** Tests high contrast ratio handling and accessibility features.

**Characteristics:**
- Background: #000000 (pure black)
- Foreground: #ffffff (pure white)
- ANSI colors: Fully saturated primary colors (#ff0000, #00ff00, #0000ff, etc.)
- Bright variants: Lighter tints of primary colors

**Tests:**
- High contrast ratio calculation
- Maximum saturation color handling
- Readability in extreme contrast scenarios
- Color distinction with fully saturated hues

---

### 3. low-contrast-test.json
**Purpose:** Tests low contrast detection and potential warning/adjustment mechanisms.

**Characteristics:**
- Background: #2d2d2d (dark gray)
- Foreground: #3e3e3e (slightly lighter gray)
- ANSI colors: Muted, desaturated colors with minimal contrast
- Very subtle color differences throughout

**Tests:**
- Low contrast ratio detection
- Contrast warning mechanisms
- Color adjustment algorithms for readability
- Handling of barely distinguishable colors

---

### 4. minimal-test.json
**Purpose:** Tests fallback strategies for missing optional properties.

**Characteristics:**
- Only required properties: name, background, foreground, 16 ANSI colors
- NO optional fields (cursorColor, selectionBackground, etc.)
- Standard VS Code-like color palette

**Tests:**
- Fallback strategy for missing cursorColor
- Fallback strategy for missing selectionBackground
- Minimal valid configuration handling
- Default value assignment for optional properties

---

### 5. light-theme-test.json
**Purpose:** Tests dark/light theme detection algorithm.

**Characteristics:**
- Background: #ffffff (pure white)
- Foreground: #000000 (pure black)
- Adjusted ANSI colors suitable for light backgrounds
- Includes cursorColor and selectionBackground

**Tests:**
- Light vs. dark theme classification
- Background luminance calculation
- Color adaptation for light backgrounds
- Selection and cursor color visibility on light backgrounds

---

### 6. pastel-test.json
**Purpose:** Tests color palette expansion with limited saturation.

**Characteristics:**
- Background: #f5f5f0 (light cream)
- Foreground: #5a5a5a (medium gray)
- ANSI colors: Soft, desaturated pastel shades
- Low saturation throughout (20-40% saturation range)

**Tests:**
- Low saturation color handling
- Pastel palette conversion
- Color distinction with limited saturation
- Hue differentiation when saturation is minimal
- Semantic color mapping with subtle colors

---

### 7. invalid-colors-test.json
**Purpose:** Tests validation and error handling for malformed data.

**Characteristics:**
- Intentionally malformed hex colors:
  - "notahexcolor" - non-hex string
  - "#gggggg" - invalid hex characters
  - "#00000" - incomplete hex (5 chars)
  - "#ff000000" - too many characters (8 chars)
  - "rgb(0,255,0)" - wrong format (RGB instead of hex)
  - "#xyz123" - invalid hex characters
  - "56b6c2" - missing # prefix
- Missing required properties (white, brightCyan, brightWhite)

**Tests:**
- Input validation
- Error detection and reporting
- Graceful handling of malformed data
- Missing property detection
- Format validation (hex color requirements)

---

## Additional Test Files

### invalid-colors.json
Similar to invalid-colors-test.json, contains various invalid color formats for validation testing.

### missing-properties.json
Tests handling of incomplete color schemes with missing required ANSI color properties.

### invalid-json.json
Tests JSON parsing error handling (malformed JSON syntax).

### valid-scheme.json, another-valid-scheme.json, normal-test.json
Valid baseline test schemes for comparison and positive test cases.

### limited-palette-test.json
Tests conversion with a limited or reduced color palette.

### valid-scheme-no-optionals.json
Another minimal valid scheme without optional properties.

---

## Usage in Tests

These test schemes are used by the integration tests in the `buildSrc` module to validate:

1. **Parser correctness** - Can the system correctly parse Windows Terminal JSON?
2. **Validation logic** - Does it properly detect and report invalid schemes?
3. **Color conversion** - Are colors correctly converted from hex to IntelliJ format?
4. **Edge case handling** - Does it gracefully handle unusual but valid configurations?
5. **Error recovery** - Does it provide meaningful errors for invalid data?
6. **Theme classification** - Does it correctly identify light vs. dark themes?
7. **Contrast analysis** - Does it properly calculate and handle contrast ratios?
8. **Semantic mapping** - Are ANSI colors properly mapped to IntelliJ semantic colors?

---

## Adding New Test Schemes

When adding new test schemes:

1. Create a `.json` file following Windows Terminal color scheme format
2. Document its purpose and characteristics in this README
3. Clearly specify what edge case or scenario it tests
4. Add corresponding test cases in the test suite
5. Use descriptive file names ending in `-test.json`

---

## Windows Terminal Color Scheme Format

A valid Windows Terminal color scheme requires:

**Required Properties:**
- `name` (string) - Scheme name
- `background` (string) - Background color (hex format #RRGGBB)
- `foreground` (string) - Foreground/text color (hex format #RRGGBB)
- `black`, `red`, `green`, `yellow`, `blue`, `purple`, `cyan`, `white` - ANSI colors 0-7
- `brightBlack`, `brightRed`, `brightGreen`, `brightYellow`, `brightBlue`, `brightPurple`, `brightCyan`, `brightWhite` - ANSI colors 8-15

**Optional Properties:**
- `cursorColor` (string) - Cursor color (hex format)
- `selectionBackground` (string) - Selection highlight color (hex format)

All color values should be in hex format: `#RRGGBB` (e.g., `#ff0000` for red).
