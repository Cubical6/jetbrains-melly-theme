# ColorPaletteExpander - Technical Documentation

## Overview

The `ColorPaletteExpander` is a sophisticated color generation system that transforms a limited 16-color ANSI palette (from Windows Terminal) into a comprehensive IntelliJ theme palette with 50+ colors. It uses advanced color theory techniques to create harmonious, accessible, and visually consistent color variants.

## Core Functionality

### Main Expansion Function

```kotlin
fun expandPalette(scheme: WindowsTerminalColorScheme): Map<String, String>
```

Takes a `WindowsTerminalColorScheme` with 16 ANSI colors and generates:
- **Background variants**: For panels, sidebars, tooltips, dialogs
- **Foreground variants**: For different text states (normal, subtle, muted, disabled)
- **Interactive states**: Hover, pressed, selected, focused colors
- **Semantic colors**: Info, success, warning, error with backgrounds and borders
- **Editor colors**: Gutter, line numbers, indent guides, current line
- **Border colors**: At various intensities
- **Accent colors**: With light/dark variants
- **Gradient steps**: For smooth transitions

**Result**: Typically generates 50+ colors from just 16 input colors.

## Color Generation Techniques

### 1. Linear Interpolation

**Function**: `interpolateColors(color1: String, color2: String, steps: Int): List<String>`

Creates smooth color gradients by linearly blending RGB values between two colors.

**Algorithm**:
```
For each step i from 0 to steps-1:
  ratio = i / (steps - 1)
  result[i] = blend(color1, color2, ratio)
```

**Use Cases**:
- Creating smooth gradients for backgrounds
- Generating intermediate shades for UI elements
- Building color ramps for data visualization

**Example**:
```kotlin
val gradient = interpolateColors("#282c34", "#abb2bf", 5)
// Result: ["#282c34", "#4e5563", "#747e92", "#9aa7c1", "#abb2bf"]
```

### 2. Tint Generation

**Function**: `generateTints(baseColor: String, count: Int): List<String>`

Creates lighter variants by mixing with white while preserving hue.

**Algorithm**:
```
For each step i from 1 to count:
  percentage = i / (count + 1)
  tint[i] = lighten(baseColor, percentage)
```

**Mathematical Basis**:
- RGB Lightening: `R_new = R + (255 - R) * percentage`
- Preserves color relationships
- Maintains hue consistency

**Use Cases**:
- Light theme backgrounds
- Hover states
- Disabled/inactive elements
- Subtle highlights

**Example**:
```kotlin
val tints = generateTints("#61afef", 3)
// Result: Progressively lighter blues while maintaining the hue
```

### 3. Shade Generation

**Function**: `generateShades(baseColor: String, count: Int): List<String>`

Creates darker variants by mixing with black while preserving hue.

**Algorithm**:
```
For each step i from 1 to count:
  percentage = i / (count + 1)
  shade[i] = darken(baseColor, percentage)
```

**Mathematical Basis**:
- RGB Darkening: `R_new = R * (1 - percentage)`
- Preserves color relationships
- Maintains hue consistency

**Use Cases**:
- Dark theme backgrounds
- Pressed states
- Shadows
- Deep accents

### 4. HSV Manipulation

**Functions**:
- `generateSaturationVariants(baseColor: String, count: Int): Map<String, String>`
- `generateMonochromaticPalette(baseColor: String, count: Int): List<String>`

**Color Space**: HSV (Hue, Saturation, Value)
- **Hue**: Color type (0-360°)
- **Saturation**: Color intensity (0-100%)
- **Value**: Brightness (0-100%)

**Saturation Variants**:
```
saturated = HSV(H, S + amount, V)
desaturated = HSV(H, S - amount, V)
```

**Monochromatic Palette**:
```
For each step i from 0 to count-1:
  value = (i + 1) / (count + 1)
  color[i] = HSV(H, S, value)
```

**Use Cases**:
- Creating emphasis without changing brightness
- Muted color variants for backgrounds
- Vibrant accents
- Maintaining color family consistency

### 5. Complementary Colors

**Function**: `generateComplementaryColor(baseColor: String): String`

Creates colors opposite on the color wheel (180° hue rotation).

**Algorithm**:
```
(H, S, V) = hexToHsv(baseColor)
complementaryHue = (H + 180) % 360
result = hsvToHex(complementaryHue, S, V)
```

**Color Theory**:
- Maximum contrast while maintaining harmony
- Classic color scheme: Red ↔ Cyan, Blue ↔ Orange
- Useful for creating visual separation

**Use Cases**:
- Accent colors that stand out
- Call-to-action buttons
- Error vs success colors
- Creating visual hierarchy

### 6. Analogous Colors

**Function**: `generateAnalogousColors(baseColor: String, degrees: Double = 30.0): Pair<String, String>`

Creates harmonious colors adjacent on the color wheel (±30° by default).

**Algorithm**:
```
(H, S, V) = hexToHsv(baseColor)
leftHue = (H - degrees + 360) % 360
rightHue = (H + degrees) % 360
left = hsvToHex(leftHue, S, V)
right = hsvToHex(rightHue, S, V)
```

**Color Theory**:
- Creates harmonious, pleasing combinations
- Low contrast, high cohesion
- Natural color relationships

**Use Cases**:
- Related UI elements
- Sequential data visualization
- Creating color families
- Subtle variations

### 7. Triadic Colors

**Function**: `generateTriadicColors(baseColor: String): Pair<String, String>`

Creates vibrant schemes with colors 120° apart on the color wheel.

**Algorithm**:
```
(H, S, V) = hexToHsv(baseColor)
triad1Hue = (H + 120) % 360
triad2Hue = (H + 240) % 360
```

**Color Theory**:
- Balanced, vibrant color schemes
- Good visual contrast
- Maintains harmony through equal spacing

**Use Cases**:
- Info/success/warning color systems
- Three-category data visualization
- Balanced accent colors

### 8. Split-Complementary Colors

**Function**: `generateSplitComplementaryColors(baseColor: String, degrees: Double = 30.0): Pair<String, String>`

Creates colors at complement ±30° for softer contrast than pure complementary.

**Algorithm**:
```
(H, S, V) = hexToHsv(baseColor)
complementaryHue = (H + 180) % 360
split1Hue = (complementaryHue - degrees + 360) % 360
split2Hue = (complementaryHue + degrees) % 360
```

**Color Theory**:
- Strong contrast but less jarring than complementary
- More versatile than pure complementary
- Maintains visual interest

**Use Cases**:
- Balanced color schemes
- Multiple accent colors
- Creating visual hierarchy without clash

## Accessibility Features

### Contrast Ratio Adjustment

**Function**: `adjustToContrastRatio(baseColor: String, backgroundColor: String, targetContrast: Double, maxIterations: Int = 20): String`

Ensures WCAG compliance by iteratively adjusting color brightness.

**Algorithm**:
```
currentContrast = calculateContrastRatio(color, background)
while (|currentContrast - targetContrast| > 0.1 && iterations < max):
  if currentContrast < target:
    color = isDark(background) ? lighten(color) : darken(color)
  else:
    color = isDark(background) ? darken(color) : lighten(color)
  currentContrast = calculateContrastRatio(color, background)
```

**WCAG Standards**:
- **AA Normal Text**: 4.5:1
- **AA Large Text**: 3:1
- **AAA Normal Text**: 7:1
- **AAA Large Text**: 4.5:1

**Use Cases**:
- Ensuring text readability
- Meeting accessibility requirements
- Supporting low-vision users
- Legal compliance

### Luminance Adjustment

**Function**: `adjustToLuminance(color: String, targetLuminance: Double, maxIterations: Int = 10): String`

Adjusts colors to specific brightness levels while preserving hue.

**Algorithm**:
```
currentLuminance = calculateLuminance(color)
while (|currentLuminance - targetLuminance| > 5.0 && iterations < max):
  if currentLuminance < target:
    color = lighten(color, 0.1)
  else:
    color = darken(color, 0.1)
  currentLuminance = calculateLuminance(color)
```

**Luminance Formula**: `L = 0.299*R + 0.587*G + 0.114*B`

**Use Cases**:
- Consistent brightness across palette
- Creating balanced color systems
- Maintaining visual hierarchy
- Supporting different lighting conditions

## Generated Color Categories

### Background Variants
- `bg_lighter`: Slightly lighter than base background
- `bg_darker`: Slightly darker than base background
- `bg_subtle`: Very subtle variation for hover states
- `bg_panel`: For side panels and tool windows
- `bg_sidebar`: For project/file tree sidebar
- `bg_tooltip`: For tooltip backgrounds
- `bg_popup`: For popup menus and dialogs
- `bg_dialog`: For modal dialogs
- `bg_menu`: For menu backgrounds

### Foreground Variants
- `fg_normal`: Default text color
- `fg_subtle`: De-emphasized text (60% blend with background)
- `fg_muted`: Very de-emphasized text (40% blend)
- `fg_placeholder`: Placeholder text (30% blend)
- `fg_disabled`: Disabled text (25% blend)
- `fg_bright`: Emphasized text (10% lighter)
- `fg_link`: Hyperlink color (30% more saturated)

### Interactive States
- `state_hover`: Element hover state (10% foreground blend)
- `state_pressed`: Button pressed state (15% blend)
- `state_selected`: Selected item background (20% blend)
- `state_focused`: Focused element highlight (12% blend)
- `state_active`: Active tab/item (18% blend)
- `state_inactive`: Inactive state (5% blend)

### Semantic Colors
For each semantic type (info, success, warning, error):
- `semantic_X`: Main semantic color
- `semantic_X_bg`: Background for semantic messages (15% blend)
- `semantic_X_border`: Border for semantic messages (30% blend)

### Editor Colors
- `editor_gutter`: Line number gutter background
- `editor_line_number`: Line number color (35% foreground blend)
- `editor_line_number_active`: Active line number (70% blend)
- `editor_indent_guide`: Indent guide lines (15% blend)
- `editor_indent_guide_selected`: Selected indent guides (25% blend)
- `editor_current_line`: Current line highlight
- `editor_caret_row`: Caret row background
- `editor_whitespace`: Whitespace indicators (20% blend)

### Border Colors
- `border_subtle`: Very subtle borders (10% foreground blend)
- `border_normal`: Standard borders (20% blend)
- `border_strong`: Emphasized borders (35% blend)
- `border_focus`: Focused element border (50% blend)
- `border_error`: Error border (40% blend)

### Accent Colors
- `accent`: Main accent color (brightest saturated ANSI color)
- `accent_light`: 20% lighter accent
- `accent_lighter`: 40% lighter accent
- `accent_dark`: 20% darker accent
- `accent_darker`: 40% darker accent
- `accent_muted`: 30% desaturated accent

### Gradient Steps
- `gradient_bg_fg_N`: N steps from background to foreground

## Implementation Details

### Color Format
- **Input**: `#RRGGBB` format (6-digit hex)
- **Output**: `#RRGGBB` format (lowercase)
- **Validation**: Regex `^#[0-9a-f]{6}$`

### Error Handling
- Invalid hex colors throw `IllegalArgumentException`
- Out-of-range parameters throw `IllegalArgumentException`
- RGB values clamped to 0-255 range
- Hue values wrapped modulo 360
- Saturation/value clamped to 0.0-1.0

### Performance Considerations
- All color operations are pure functions
- No mutable state
- Efficient RGB/HSV conversions
- Caching not needed due to fast computation
- Typical expansion: <1ms for full palette

## Color Theory Background

### RGB Color Space
- Additive color model
- Direct mapping to display hardware
- Simple arithmetic operations
- Good for blending and interpolation

### HSV Color Space
- Intuitive color manipulation
- Separates hue (color type) from brightness
- Easy saturation adjustments
- Natural for color harmony rules

### Perceptual Luminance
- Models human eye sensitivity
- Green = 58.7% weight (most sensitive)
- Red = 29.9% weight
- Blue = 11.4% weight (least sensitive)
- Critical for accessibility

### Color Harmony Rules
1. **Monochromatic**: Same hue, varying value
2. **Analogous**: Adjacent hues (±30°)
3. **Complementary**: Opposite hues (180°)
4. **Split-Complementary**: Complement ±30°
5. **Triadic**: Evenly spaced (120°)
6. **Tetradic**: Two complementary pairs (90°)

## Usage Best Practices

### 1. Start with a Good Base Palette
- Ensure 16 ANSI colors are well-balanced
- Check contrast ratios beforehand
- Verify hue distribution
- Test with both light and dark backgrounds

### 2. Validate Expanded Colors
- Check WCAG contrast ratios
- Verify color distinctiveness
- Test with colorblind simulators
- Review in different lighting conditions

### 3. Customize as Needed
- Use individual functions for specific needs
- Adjust degrees/steps for finer control
- Combine techniques for unique effects
- Test with real UI components

### 4. Consider Theme Type
- **Dark themes**: Use shades, careful with tints
- **Light themes**: Use tints, careful with shades
- **High contrast**: Increase adjustment percentages
- **Subtle themes**: Decrease adjustment percentages

## Integration with Theme System

The expanded palette integrates with:
- `ColorMappingConfig`: Maps expanded colors to IntelliJ attributes
- `ConsoleColorMapper`: Uses expanded colors for console
- `SyntaxColorInference`: Leverages expanded colors for syntax
- `ThemeConstructor`: Consumes expanded palette for theme generation

## Testing

Comprehensive test coverage includes:
- Gradient smoothness validation
- Color harmony verification
- Accessibility compliance checks
- Edge case handling
- Mathematical correctness
- Format validation

See `ColorPaletteExpanderTest.kt` for 40+ test cases covering all functions.

## Example Workflows

### Expanding a Theme
```kotlin
val scheme = WindowsTerminalColorScheme(...)
val expanded = ColorPaletteExpander.expandPalette(scheme)
// Use expanded palette for theme generation
```

### Creating Custom Variants
```kotlin
val baseColor = "#61afef"
val tints = ColorPaletteExpander.generateTints(baseColor, 5)
val shades = ColorPaletteExpander.generateShades(baseColor, 5)
val palette = tints + listOf(baseColor) + shades
```

### Ensuring Accessibility
```kotlin
val fg = "#61afef"
val bg = "#282c34"
val accessibleFg = ColorPaletteExpander.adjustToContrastRatio(fg, bg, 4.5)
```

### Creating Color Schemes
```kotlin
val base = "#61afef"
val (analogous1, analogous2) = ColorPaletteExpander.generateAnalogousColors(base)
val complement = ColorPaletteExpander.generateComplementaryColor(base)
// Use for creating balanced UI color schemes
```

## Future Enhancements

Potential improvements:
- LAB/LCH color space support for perceptually uniform gradients
- Automatic palette optimization for colorblindness
- Machine learning-based color harmony
- Adaptive contrast for different display technologies
- Theme-aware color generation (light vs dark)
- Color naming based on hue/saturation/lightness

## References

- WCAG 2.1 Guidelines: https://www.w3.org/WAI/WCAG21/
- Color Theory Basics: https://www.colormatters.com/
- HSV Color Space: https://en.wikipedia.org/wiki/HSL_and_HSV
- IntelliJ Theme Structure: https://plugins.jetbrains.com/docs/intellij/theme-structure.html
- Windows Terminal Color Schemes: https://learn.microsoft.com/en-us/windows/terminal/customize-settings/color-schemes
