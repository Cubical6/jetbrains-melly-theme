# Windows Terminal to IntelliJ Theme Converter

**Automatically convert Windows Terminal color schemes to JetBrains IntelliJ themes**

This system provides seamless conversion of Windows Terminal color schemes to fully functional IntelliJ IDEA themes, preserving
the aesthetic of the original terminal theme while intelligently mapping colors to syntax highlighting attributes.

## Overview

The Windows Terminal to IntelliJ Theme Converter solves a common problem: Windows Terminal color schemes contain only 20 colors
(16 ANSI colors + 4 special colors), while IntelliJ IDEA requires 100+ distinct syntax highlighting attributes. This system uses
intelligent color inference algorithms to expand the limited terminal palette into comprehensive editor themes.

### What This System Does

- **Direct Console Color Mapping**: Maps all 16 ANSI terminal colors exactly to IntelliJ console attributes
- **Intelligent Syntax Inference**: Automatically assigns semantic colors to code elements (keywords, strings, functions, etc.)
- **Edge Case Handling**: Gracefully handles monochrome palettes, high/low contrast themes, and limited color ranges
- **Batch Processing**: Convert multiple Windows Terminal schemes in a single build operation
- **Theme Generation**: Produces both `.theme.json` (UI theme) and `.xml` (color scheme) files for IntelliJ

### Why Use This?

- **Consistency**: Use the same color scheme across your terminal and IDE
- **No Manual Work**: Automatic inference means you don't need to manually map 100+ colors
- **Quality**: Implements WCAG-compliant contrast validation and perceptual color theory
- **Extensibility**: Easy to add new Windows Terminal schemes and regenerate all themes

## Features

### Automatic Color Conversion

- **Exact ANSI Color Matching**: Terminal colors map 1:1 to IntelliJ console colors
- **Semantic Syntax Mapping**: Blues for keywords, greens for strings, reds for errors (customizable)
- **Perceptual Color Classification**: Uses luminance-based analysis to categorize colors
- **Intelligent Palette Expansion**: Generates variations through lightening, darkening, and interpolation

### Support for Light and Dark Themes

- Automatically detects theme brightness from background color
- Adjusts syntax highlighting and UI elements appropriately
- Maintains readability and contrast in both modes

### Edge Case Handling

- **Monochrome Palettes**: Uses font styles (bold, italic) to differentiate syntax elements
- **High Contrast**: Preserves accessibility features (WCAG AAA compliance)
- **Low Contrast**: Enhances contrast to meet WCAG AA standards (4.5:1 minimum)
- **Limited Color Palettes**: Falls back to luminance-based differentiation

### Batch Processing

- Process all Windows Terminal schemes in one command
- Consistent naming and organization
- Automatic validation before generation

## Quick Start

### Prerequisites

- **Java**: JDK 8 or higher
- **Gradle**: Included via Gradle Wrapper (no installation needed)
- **Git**: For cloning the repository

### 5-Minute Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Cubical6/jetbrains-melly-theme.git
   cd jetbrains-melly-theme
   ```

2. **Add your Windows Terminal color scheme**:
   ```bash
   # Copy your scheme to the windows-terminal-schemes directory
   cp /path/to/your-scheme.json windows-terminal-schemes/
   ```

3. **Run the build**:
   ```bash
   ./gradlew createThemes
   ```

4. **Find your generated themes**:
   ```
   build/themes/
   ├── your-scheme.theme.json     # UI theme
   └── your-scheme.xml            # Color scheme
   ```

5. **Install in IntelliJ**:
   - Copy both files to your IntelliJ config directory:
     - **Windows**: `%APPDATA%\JetBrains\<Product><Version>\`
     - **macOS**: `~/Library/Application Support/JetBrains/<Product><Version>/`
     - **Linux**: `~/.config/JetBrains/<Product><Version>/`
   - Restart IntelliJ IDEA
   - Go to `Settings | Appearance & Behavior | Appearance`
   - Select your new theme from the dropdown

## How to Add New Color Schemes

### Step 1: Obtain a Windows Terminal Scheme

You can get Windows Terminal color schemes from several sources:

#### Option A: Use iTerm2-Color-Schemes Repository

The [iTerm2-Color-Schemes](https://github.com/mbadolato/iTerm2-Color-Schemes) repository contains 250+ terminal themes:

```bash
# Download a specific scheme
curl -o windows-terminal-schemes/nord.json \
  https://raw.githubusercontent.com/mbadolato/iTerm2-Color-Schemes/master/windowsterminal/Nord.json
```

#### Option B: Create Your Own Scheme

Create a JSON file with this structure:

```json
{
  "name": "My Custom Theme",
  "background": "#282c34",
  "foreground": "#abb2bf",
  "cursorColor": "#528bff",
  "selectionBackground": "#3e4451",

  "black": "#282c34",
  "red": "#e06c75",
  "green": "#98c379",
  "yellow": "#e5c07b",
  "blue": "#61afef",
  "purple": "#c678dd",
  "cyan": "#56b6c2",
  "white": "#abb2bf",

  "brightBlack": "#5c6370",
  "brightRed": "#e06c75",
  "brightGreen": "#98c379",
  "brightYellow": "#e5c07b",
  "brightBlue": "#61afef",
  "brightPurple": "#c678dd",
  "brightCyan": "#56b6c2",
  "brightWhite": "#ffffff"
}
```

**Required Properties**:
- `name`: Display name of the scheme
- `background`, `foreground`: Special colors
- All 16 ANSI colors: `black`, `red`, `green`, `yellow`, `blue`, `purple`, `cyan`, `white` (and their `bright` variants)

**Optional Properties**:
- `cursorColor`: Cursor color (defaults to `foreground` if not provided)
- `selectionBackground`: Selection highlight (auto-generated if not provided)

#### Option C: Export from Windows Terminal

If you already have a scheme configured in Windows Terminal:

1. Open Windows Terminal settings (`Ctrl+,`)
2. Navigate to `Color Schemes`
3. Select your scheme
4. Copy the JSON configuration
5. Save to `windows-terminal-schemes/your-scheme-name.json`

### Step 2: Validate Your Scheme

Before building, validate your scheme file:

```bash
# Check JSON syntax
python -m json.tool windows-terminal-schemes/your-scheme.json

# Verify all required properties are present
grep -E '"(name|background|foreground|black|red|green|yellow|blue|purple|cyan|white)"' \
  windows-terminal-schemes/your-scheme.json
```

**Validation Checklist**:
- [ ] Valid JSON format (no syntax errors)
- [ ] All 20 required properties present (`name`, `background`, `foreground`, 16 ANSI colors)
- [ ] Colors in `#RRGGBB` hex format (e.g., `#ff5555`)
- [ ] Unique scheme name (not already in use)
- [ ] Readable contrast (foreground vs background ratio > 3.0)

### Step 3: Generate Themes

Run the theme generator:

```bash
# Generate all themes (including your new one)
./gradlew createThemes

# Or on Windows:
gradlew.bat createThemes
```

**Build Output**:
```
> Task :createThemes
Processing Windows Terminal schemes...
  ✓ Loaded: dracula.json
  ✓ Loaded: nord.json
  ✓ Loaded: your-scheme.json (NEW)

Generating themes...
  ✓ Generated: Dracula
  ✓ Generated: Nord
  ✓ Generated: Your Custom Theme

BUILD SUCCESSFUL in 4s
```

### Step 4: Test Your Theme

1. **Locate generated files**:
   ```
   build/themes/
   ├── your-custom-theme.theme.json
   └── your-custom-theme.xml
   ```

2. **Install manually** (for testing):
   - Copy files to IntelliJ config directory (see Quick Start above)
   - Restart IntelliJ
   - Select theme in settings

3. **Verify appearance**:
   - Check editor syntax highlighting (open a Java/Kotlin file)
   - Check terminal colors (open built-in terminal)
   - Check UI elements (tool windows, menus, dialogs)
   - Test both light and dark IDE themes if applicable

## Build Instructions

### Build System Overview

This project uses Gradle as its build system with custom build tasks defined in `buildSrc/`. The build process:

1. **Loads** Windows Terminal schemes from `windows-terminal-schemes/`
2. **Analyzes** color palettes (luminance, hue, saturation)
3. **Maps** console colors (direct ANSI mapping)
4. **Infers** syntax colors (semantic color assignment)
5. **Generates** IntelliJ theme files (`.theme.json` and `.xml`)
6. **Validates** output (contrast ratios, required attributes)

### Available Build Tasks

```bash
# Generate all IntelliJ themes from Windows Terminal schemes
./gradlew createThemes

# Clean build artifacts
./gradlew clean

# Full build (includes theme generation)
./gradlew build

# Run tests
./gradlew test

# Generate and install plugin locally
./gradlew buildPlugin

# Run IntelliJ with the plugin for testing
./gradlew runIde
```

### Detailed Build Process

#### Task: `createThemes`

**What it does**:
1. Scans `windows-terminal-schemes/*.json` for color scheme files
2. Validates each scheme (JSON format, required properties, color format)
3. For each valid scheme:
   - Analyzes color palette
   - Detects edge cases (monochrome, high/low contrast)
   - Maps console colors (exact ANSI mapping)
   - Infers syntax colors (semantic algorithm)
   - Generates UI theme JSON
   - Generates color scheme XML
4. Outputs themes to `build/themes/`

**Input**: `windows-terminal-schemes/*.json`

**Output**: `build/themes/<scheme-name>.{theme.json,xml}`

**Dependencies**: None (standalone task)

#### Task: `importWindowsTerminalSchemes`

**What it does**: Downloads popular schemes from iTerm2-Color-Schemes repository

**Usage**:
```bash
./gradlew importWindowsTerminalSchemes
```

This will download curated schemes to `windows-terminal-schemes/`.

### Build Configuration

Key build configuration files:

- **`build.gradle`**: Main Gradle build configuration
- **`buildSrc/src/main/kotlin/one-dark-theme-plugin.gradle.kts`**: Plugin conventions
- **`buildSrc/src/main/kotlin/tasks/GenerateThemesFromWindowsTerminal.kt`**: Theme generation task
- **`buildSrc/src/main/kotlin/generators/XMLColorSchemeGenerator.kt`**: XML color scheme generator
- **`buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt`**: UI theme JSON generator
- **`buildSrc/src/main/kotlin/mapping/ConsoleColorMapper.kt`**: Console color mapper
- **`buildSrc/src/main/kotlin/mapping/SyntaxColorInference.kt`**: Syntax color inference algorithm

### Troubleshooting Build Issues

**Build fails with "Scheme validation failed"**:
- Check JSON syntax: `python -m json.tool windows-terminal-schemes/your-scheme.json`
- Verify all required properties are present
- Ensure colors are in `#RRGGBB` format

**Generated themes look incorrect**:
- Review build output for warnings
- Check `build/logs/theme-generation.log` for detailed mapping decisions
- Verify input scheme has good contrast (foreground vs background)

**Build is slow**:
- First build downloads Gradle dependencies (one-time cost)
- Subsequent builds should be faster (~5-10 seconds)
- Use `./gradlew createThemes --offline` to skip dependency checks

## Architecture

### High-Level System Design

```
┌─────────────────────────────────────────────────────────────┐
│ Windows Terminal Color Scheme (.json)                       │
│ - 20 colors total (16 ANSI + 4 special)                     │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────────────────┐
│ Color Analysis & Classification                             │
│ - Luminance calculation (DARK/MID/BRIGHT)                   │
│ - Hue extraction (HSV color space)                          │
│ - Edge case detection (monochrome, contrast)                │
└──────────────────────┬──────────────────────────────────────┘
                       │
              ┌────────┴────────┐
              │                 │
              ↓                 ↓
┌──────────────────────┐  ┌──────────────────────────────┐
│ Console Color Mapper │  │ Syntax Color Inference       │
│ (Direct 1:1 mapping) │  │ (Semantic algorithm)         │
│                      │  │                              │
│ 20 colors →          │  │ 20 colors → 100+ attributes  │
│ 20 IntelliJ attrs    │  │ via intelligent expansion    │
└──────────┬───────────┘  └─────────┬────────────────────┘
           │                        │
           └────────┬───────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ Theme Generation                                             │
│ - UI Theme (.theme.json): Colors, icons, borders            │
│ - Color Scheme (.xml): Editor syntax highlighting           │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────────────────┐
│ IntelliJ IDEA Theme (ready to install)                      │
└─────────────────────────────────────────────────────────────┘
```

### Component Breakdown

#### 1. Color Analysis (`ColorAnalysis.kt`)

**Responsibility**: Analyze and classify colors from Windows Terminal scheme

**Key Functions**:
- `calculateLuminance(hexColor)`: Perceived luminance using ITU-R BT.709 formula
- `classifyByLuminance(hexColor)`: Classify as DARK/MID/BRIGHT
- `extractHue(hexColor)`: Extract hue in HSV color space
- `detectEdgeCase(scheme)`: Identify monochrome, high/low contrast, limited palette

**Output**: `ColorAnalysis` object with luminance map, classification map, hue map, and statistics

#### 2. Console Color Mapper (`ConsoleColorMapper.kt`)

**Responsibility**: Direct 1:1 mapping of Windows Terminal colors to IntelliJ console attributes

**Mapping Table**:
```
Windows Terminal    →  IntelliJ Attribute
─────────────────────────────────────────────
background          →  CONSOLE_BACKGROUND_KEY
foreground          →  CONSOLE_NORMAL_OUTPUT
black               →  CONSOLE_BLACK_OUTPUT
red                 →  CONSOLE_RED_OUTPUT
... (16 ANSI colors total)
```

**Output**: Map of 20 IntelliJ console attributes with hex colors

#### 3. Syntax Color Inference (`SyntaxColorInference.kt`)

**Responsibility**: Intelligent mapping of limited palette to 100+ syntax attributes

**Algorithm** (detailed in [docs/COLOR_MAPPING.md](docs/COLOR_MAPPING.md)):
1. Create semantic color pools (blues for keywords, greens for strings, etc.)
2. Assign high-priority attributes (keywords, strings, comments, errors)
3. Assign medium-priority attributes (functions, classes, constants)
4. Assign low-priority attributes (variables, operators, delimiters)
5. Apply variations (lighten/darken) for related attributes
6. Validate contrast ratios and enhance if needed

**Output**: Map of 100+ IntelliJ syntax attributes with hex colors and font styles

#### 4. Theme Generators

**XMLColorSchemeGenerator.kt**: Generates IntelliJ color scheme XML

**UIThemeGenerator.kt**: Generates IntelliJ UI theme JSON

**Output**: Two files per scheme that together form a complete IntelliJ theme

## Color Mapping

The color mapping system is the core of this converter. It consists of two parts:

### 1. Direct Console Color Mapping (Exact)

All 16 ANSI terminal colors map exactly to IntelliJ console attributes:

| Windows Terminal Property | IntelliJ Attribute | Usage |
|---------------------------|-------------------|-------|
| `black` | `CONSOLE_BLACK_OUTPUT` | ANSI color 0 (30) |
| `red` | `CONSOLE_RED_OUTPUT` | ANSI color 1 (31) |
| `green` | `CONSOLE_GREEN_OUTPUT` | ANSI color 2 (32) |
| ... | ... | ... |

See [docs/COLOR_MAPPING.md](docs/COLOR_MAPPING.md) for the complete mapping table.

### 2. Syntax Color Inference (Semantic)

Syntax highlighting colors are inferred using semantic rules:

- **Keywords** (e.g., `public`, `class`, `if`) → Blue/purple from palette
- **Strings** (e.g., `"hello"`) → Green from palette
- **Comments** (e.g., `// comment`) → Dimmed gray from palette
- **Numbers** (e.g., `42`, `3.14`) → Yellow/cyan from palette
- **Functions** (e.g., `myMethod()`) → Cyan/blue from palette
- **Errors** (e.g., syntax errors) → Red from palette (exact match)

The inference algorithm uses:
- **Luminance-based classification**: Categorize colors by brightness
- **Hue-based categorization**: Group colors by hue (red, green, blue, etc.)
- **Semantic priority rules**: Assign most important syntax elements first
- **Color variations**: Generate lighter/darker variants for related elements

**For complete details**, see:
- [docs/COLOR_MAPPING.md](docs/COLOR_MAPPING.md) - Full mapping specification
- [docs/SYNTAX_INFERENCE_ALGORITHM.md](docs/SYNTAX_INFERENCE_ALGORITHM.md) - Algorithm details

## Contributing

We welcome contributions of new Windows Terminal color schemes and improvements to the conversion algorithm!

### How to Contribute

1. **Add a new color scheme**: Follow the "How to Add New Color Schemes" section above
2. **Submit a pull request**: See [docs/CONTRIBUTING_SCHEMES.md](docs/CONTRIBUTING_SCHEMES.md) for detailed guidelines
3. **Report issues**: Use GitHub Issues for bugs or feature requests
4. **Improve documentation**: Documentation improvements are always welcome

### Contribution Guidelines

Before submitting a color scheme:

- [ ] **Validate JSON format**: No syntax errors
- [ ] **Test the generated theme**: Install and verify in IntelliJ
- [ ] **Include attribution**: Credit original scheme author in PR description
- [ ] **Check licensing**: Ensure scheme is compatible with MIT license
- [ ] **Follow naming conventions**: Use kebab-case for file names (e.g., `dracula.json`, `nord.json`)

For detailed contribution guidelines, see [docs/CONTRIBUTING_SCHEMES.md](docs/CONTRIBUTING_SCHEMES.md).

## Examples

### Example 1: Dracula (Dark Theme)

**Input** (`windows-terminal-schemes/dracula.json`):
```json
{
  "name": "Dracula",
  "background": "#282a36",
  "foreground": "#f8f8f2",
  "blue": "#bd93f9",
  "green": "#50fa7b",
  "red": "#ff5555",
  ...
}
```

**Generated Color Mappings**:
- **Keywords** (`public`, `class`) → `#bd93f9` (blue)
- **Strings** (`"text"`) → `#50fa7b` (green)
- **Errors** (syntax errors) → `#ff5555` (red)
- **Comments** (`// comment`) → `#6272a4` (dimmed blue-gray)

**Result**: Vibrant dark theme with excellent contrast, suitable for long coding sessions

**Screenshots**: *(Screenshot placeholder: Dracula theme in IntelliJ showing Java code with syntax highlighting)*

### Example 2: Solarized Light (Light Theme)

**Input** (`windows-terminal-schemes/solarized-light.json`):
```json
{
  "name": "Solarized Light",
  "background": "#fdf6e3",
  "foreground": "#657b83",
  "blue": "#268bd2",
  "green": "#859900",
  "red": "#dc322f",
  ...
}
```

**Generated Color Mappings**:
- **Keywords** → `#268bd2` (saturated blue)
- **Strings** → `#859900` (olive green)
- **Errors** → `#dc322f` (crimson red)
- **Comments** → `#93a1a1` (desaturated gray)

**Result**: Classic light theme optimized for daytime use, scientifically designed for reduced eye strain

**Screenshots**: *(Screenshot placeholder: Solarized Light theme in IntelliJ showing Kotlin code)*

### Example 3: Nord (Cool Dark Theme)

**Input** (`windows-terminal-schemes/nord.json`):
```json
{
  "name": "Nord",
  "background": "#2e3440",
  "foreground": "#d8dee9",
  "blue": "#5e81ac",
  "green": "#a3be8c",
  "red": "#bf616a",
  ...
}
```

**Generated Color Mappings**:
- **Keywords** → `#5e81ac` (muted blue)
- **Strings** → `#a3be8c` (muted green)
- **Errors** → `#bf616a` (muted red)
- **Comments** → `#4c566a` (dark slate)

**Result**: Professional arctic-themed dark scheme with cool tones and medium contrast

**Screenshots**: *(Screenshot placeholder: Nord theme in IntelliJ showing Python code)*

## FAQ

### General Questions

**Q: How many Windows Terminal schemes can I convert?**

A: Unlimited. The system processes all `.json` files in `windows-terminal-schemes/` directory during build.

**Q: Can I use this with other JetBrains IDEs (PyCharm, WebStorm, etc.)?**

A: Yes! Generated themes work with all JetBrains IDEs (IntelliJ IDEA, PyCharm, WebStorm, PhpStorm, RubyMine, etc.).

**Q: Do I need to manually map all 100+ syntax colors?**

A: No! The intelligent inference algorithm automatically assigns semantic colors based on the 20 Windows Terminal colors.

**Q: What if my Windows Terminal scheme has unusual colors?**

A: The system handles edge cases automatically (monochrome, high/low contrast, limited palette). See "Edge Case Handling" in
Architecture section.

### Color Mapping Questions

**Q: Why doesn't my keyword color exactly match the terminal blue?**

A: The system may lighten or darken colors to ensure WCAG-compliant contrast (minimum 3.0 ratio). Check build logs for
contrast adjustments.

**Q: Can I customize which Windows Terminal colors map to which syntax elements?**

A: Yes, edit `buildSrc/src/main/kotlin/mapping/SyntaxColorInference.kt` to change semantic mapping rules. See
[docs/COLOR_MAPPING.md](docs/COLOR_MAPPING.md) for details.

**Q: What happens if my scheme doesn't have enough color variety?**

A: The system uses font styles (bold, italic) to differentiate syntax elements when colors are insufficient. For example,
in monochrome schemes, keywords are bold while strings are italic.

### Technical Questions

**Q: What color format is required?**

A: Hexadecimal RGB format: `#RRGGBB` (e.g., `#ff5555`). Both uppercase and lowercase are accepted.

**Q: Are `cursorColor` and `selectionBackground` required?**

A: No. If not provided, `cursorColor` defaults to `foreground` and `selectionBackground` is auto-generated by blending
background (80%) with foreground (20%).

**Q: How are light vs dark themes detected?**

A: Background luminance is calculated. If background luminance < 128, it's a dark theme; otherwise, it's a light theme.

**Q: Can I generate only one specific theme instead of all schemes?**

A: Currently, `createThemes` processes all schemes. To generate one theme, temporarily move other schemes out of
`windows-terminal-schemes/` directory or modify the build task.

## Troubleshooting

### Common Issues and Solutions

#### Issue: Theme doesn't appear in IntelliJ after installation

**Symptoms**:
- New theme not listed in `Settings | Appearance & Behavior | Appearance`
- IntelliJ still shows default themes only

**Solutions**:
1. **Verify file locations**:
   - `.theme.json` file must be in `themes/` subdirectory
   - `.xml` file must be in `colors/` subdirectory
   - Example paths:
     - Windows: `%APPDATA%\JetBrains\IntelliJIdea2023.1\themes\dracula.theme.json`
     - macOS: `~/Library/Application Support/JetBrains/IntelliJIdea2023.1/themes/dracula.theme.json`
     - Linux: `~/.config/JetBrains/IntelliJIdea2023.1/themes/dracula.theme.json`

2. **Restart IntelliJ**: Themes are loaded on startup

3. **Check file permissions**: Ensure files are readable

4. **Validate JSON syntax**: Malformed JSON prevents theme loading
   ```bash
   python -m json.tool dracula.theme.json
   ```

#### Issue: Colors look wrong or washed out

**Symptoms**:
- Syntax highlighting colors don't match expectations
- All colors appear dimmed or overly bright
- Poor contrast between text and background

**Solutions**:
1. **Check original scheme contrast**: Verify `foreground` vs `background` contrast ratio
   ```bash
   # Use online tool or calculate manually
   # Minimum ratio: 3.0 (WCAG AA large text)
   # Recommended: 4.5 (WCAG AA normal text)
   ```

2. **Review build logs**: Check for contrast enhancement warnings
   ```
   build/logs/theme-generation.log
   ```

3. **Verify color format**: Ensure all colors are in `#RRGGBB` format

4. **Test on different code**: Some languages use different syntax attributes

#### Issue: Build fails with validation errors

**Symptoms**:
```
> Task :createThemes FAILED
Scheme validation failed: dracula.json
Error: Missing required property 'brightWhite'
```

**Solutions**:
1. **Add missing properties**: Ensure all 18 required properties are present
   - `name`, `background`, `foreground`
   - 8 normal ANSI colors: `black`, `red`, `green`, `yellow`, `blue`, `purple`, `cyan`, `white`
   - 8 bright ANSI colors: `brightBlack`, `brightRed`, ..., `brightWhite`

2. **Fix color format**: Change colors to `#RRGGBB` hex format
   ```json
   // Wrong:
   "red": "rgb(255, 85, 85)"
   "red": "ff5555"

   // Correct:
   "red": "#ff5555"
   ```

3. **Validate JSON syntax**:
   ```bash
   python -m json.tool windows-terminal-schemes/your-scheme.json
   ```

#### Issue: Monochrome theme looks identical to normal theme

**Symptoms**:
- Grayscale scheme doesn't use font styles
- All syntax elements have similar colors
- Difficult to distinguish keywords from strings

**Solutions**:
1. **Verify edge case detection**: Check build logs for "Edge case detected: MONOCHROME"

2. **Ensure luminance variance is low enough**: Monochrome detection threshold is 5% variance
   - If variance > 5%, system treats it as normal palette
   - Manually adjust colors to reduce variance if needed

3. **Check font rendering**: IntelliJ must support bold/italic fonts
   - Go to `Settings | Editor | Font`
   - Verify font family supports bold and italic styles

#### Issue: Light theme appears dark (or vice versa)

**Symptoms**:
- Light scheme generates dark UI
- Dark scheme generates light UI
- Background/foreground colors inverted

**Solutions**:
1. **Check background luminance**: System auto-detects light vs dark from background color
   - Dark theme: background luminance < 128
   - Light theme: background luminance >= 128

2. **Verify background color**: Ensure `background` property is correct
   ```json
   // Dark theme:
   "background": "#282c34"  // Low luminance

   // Light theme:
   "background": "#fdf6e3"  // High luminance
   ```

3. **Manual override**: If auto-detection fails, adjust background color slightly

### Getting Help

If you encounter an issue not listed here:

1. **Check existing issues**: [GitHub Issues](https://github.com/Cubical6/jetbrains-melly-theme/issues)
2. **Review documentation**: [docs/](docs/) directory contains detailed technical docs
3. **Enable debug logging**: Set `org.gradle.logging.level=debug` in `gradle.properties`
4. **Submit a bug report**: Include:
   - Windows Terminal scheme file
   - Build output / error messages
   - IntelliJ version
   - Operating system

## Credits

### Acknowledgments

- **Windows Terminal Team**: For creating the excellent Windows Terminal and color scheme specification
- **iTerm2-Color-Schemes**: [mbadolato/iTerm2-Color-Schemes](https://github.com/mbadolato/iTerm2-Color-Schemes) for the comprehensive collection of terminal color schemes
- **JetBrains**: For the IntelliJ Platform SDK and theme documentation
- **Original One Dark Theme**: [atom/one-dark-syntax](https://github.com/atom/one-dark-syntax) for the inspiration
- **WCAG Guidelines**: W3C for accessibility and contrast standards

### Related Projects

- [Windows Terminal](https://github.com/microsoft/terminal) - Modern terminal for Windows
- [iTerm2-Color-Schemes](https://github.com/mbadolato/iTerm2-Color-Schemes) - 250+ terminal color schemes
- [base16](https://github.com/chriskempson/base16) - Architecture for building themes
- [Dracula Theme](https://draculatheme.com/) - Popular dark theme available for many applications

### License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

Individual Windows Terminal color schemes maintain their original licenses. Most schemes in the `windows-terminal-schemes/`
directory are either MIT licensed or public domain.

### Contributors

See [README.md](README.md) for the full list of contributors to the Windows Terminal Themes for JetBrains project.

---

**Documentation Version**: 1.0
**Last Updated**: 2025-11-21
**Status**: Complete

For technical details, see:
- [Color Mapping Specification](docs/COLOR_MAPPING.md)
- [Syntax Inference Algorithm](docs/SYNTAX_INFERENCE_ALGORITHM.md)
- [Contributing Guidelines](docs/CONTRIBUTING_SCHEMES.md)
