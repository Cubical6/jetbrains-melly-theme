# Contributing Windows Terminal Color Schemes

**Version**: 1.0
**Date**: 2025-11-21
**Status**: Active

---

## Thank You!

Thank you for your interest in contributing to the Windows Terminal to IntelliJ Theme Converter project! Color schemes
are the heart of this project, and your contribution will help developers enjoy beautiful, consistent themes across their
terminals and IDEs.

This guide will walk you through the process of submitting a new Windows Terminal color scheme, from finding or creating
the scheme to getting your pull request merged.

## How to Contribute a New Color Scheme

### Step 1: Find or Create a Scheme

You have several options for obtaining a Windows Terminal color scheme:

#### Option A: Find an Existing Scheme

**Popular Sources**:

1. **iTerm2-Color-Schemes Repository** (Recommended):
   - URL: [https://github.com/mbadolato/iTerm2-Color-Schemes](https://github.com/mbadolato/iTerm2-Color-Schemes)
   - Contains 250+ terminal themes in Windows Terminal format
   - Well-maintained and tested
   - Example download:
     ```bash
     curl -o windows-terminal-schemes/tokyo-night.json \
       https://raw.githubusercontent.com/mbadolato/iTerm2-Color-Schemes/master/windowsterminal/TokyoNight.json
     ```

2. **Windows Terminal Themes Website**:
   - URL: [https://windowsterminalthemes.dev/](https://windowsterminalthemes.dev/)
   - Browse and preview themes visually
   - Copy JSON directly from the website

3. **Official Theme Repositories**:
   - [Dracula](https://draculatheme.com/) - Official Dracula theme
   - [Nord](https://www.nordtheme.com/) - Arctic-inspired theme
   - [Catppuccin](https://github.com/catppuccin) - Pastel theme family
   - [Gruvbox](https://github.com/morhetz/gruvbox) - Retro groove theme

**License Considerations**:
- Verify the scheme's license is compatible (MIT, BSD, CC0, or public domain preferred)
- If license is unclear, contact the original author for permission
- Include license information in your PR description

#### Option B: Create Your Own Scheme

**Color Scheme Requirements**:

Create a JSON file with the following structure:

```json
{
  "name": "My Awesome Theme",
  "background": "#1a1b26",
  "foreground": "#c0caf5",
  "cursorColor": "#c0caf5",
  "selectionBackground": "#33467c",

  "black": "#15161e",
  "red": "#f7768e",
  "green": "#9ece6a",
  "yellow": "#e0af68",
  "blue": "#7aa2f7",
  "purple": "#bb9af7",
  "cyan": "#7dcfff",
  "white": "#a9b1d6",

  "brightBlack": "#414868",
  "brightRed": "#f7768e",
  "brightGreen": "#9ece6a",
  "brightYellow": "#e0af68",
  "brightBlue": "#7aa2f7",
  "brightPurple": "#bb9af7",
  "brightCyan": "#7dcfff",
  "brightWhite": "#c0caf5"
}
```

**Property Specifications**:

| Property | Type | Required | Description | Example |
|----------|------|----------|-------------|---------|
| `name` | string | Yes | Display name of the scheme | `"Dracula"` |
| `background` | hex color | Yes | Terminal background color | `"#282a36"` |
| `foreground` | hex color | Yes | Default text color | `"#f8f8f2"` |
| `cursorColor` | hex color | No | Cursor color (defaults to foreground) | `"#f8f8f2"` |
| `selectionBackground` | hex color | No | Selection highlight (auto-generated if missing) | `"#44475a"` |
| `black` | hex color | Yes | ANSI color 0 (30) | `"#21222c"` |
| `red` | hex color | Yes | ANSI color 1 (31) | `"#ff5555"` |
| `green` | hex color | Yes | ANSI color 2 (32) | `"#50fa7b"` |
| `yellow` | hex color | Yes | ANSI color 3 (33) | `"#f1fa8c"` |
| `blue` | hex color | Yes | ANSI color 4 (34) | `"#bd93f9"` |
| `purple` | hex color | Yes | ANSI color 5 (35) | `"#ff79c6"` |
| `cyan` | hex color | Yes | ANSI color 6 (36) | `"#8be9fd"` |
| `white` | hex color | Yes | ANSI color 7 (37) | `"#f8f8f2"` |
| `brightBlack` | hex color | Yes | ANSI color 8 (90) | `"#6272a4"` |
| `brightRed` | hex color | Yes | ANSI color 9 (91) | `"#ff6e6e"` |
| `brightGreen` | hex color | Yes | ANSI color 10 (92) | `"#69ff94"` |
| `brightYellow` | hex color | Yes | ANSI color 11 (93) | `"#ffffa5"` |
| `brightBlue` | hex color | Yes | ANSI color 12 (94) | `"#d6acff"` |
| `brightPurple` | hex color | Yes | ANSI color 13 (95) | `"#ff92df"` |
| `brightCyan` | hex color | Yes | ANSI color 14 (96) | `"#a4ffff"` |
| `brightWhite` | hex color | Yes | ANSI color 15 (97) | `"#ffffff"` |

**Color Format**:
- **Required Format**: `#RRGGBB` (hexadecimal with leading `#`)
- **Valid Examples**: `#ff5555`, `#282a36`, `#ABCDEF`
- **Invalid Examples**: `ff5555` (missing `#`), `#f55` (too short), `rgb(255,85,85)` (wrong format)

**Tips for Creating Good Color Schemes**:
1. **Ensure Sufficient Contrast**:
   - Foreground vs background ratio should be at least 3.0 (WCAG AA for large text)
   - Recommended: 4.5+ (WCAG AA for normal text)
   - Use [contrast checker tools](https://webaim.org/resources/contrastchecker/)

2. **Provide Color Variety**:
   - Use diverse hues (blues, greens, yellows, reds, purples, cyans)
   - Avoid monochrome palettes unless intentional
   - Bright variants should be visually distinct from normal variants

3. **Test in Terminal First**:
   - Apply scheme in Windows Terminal
   - Test with various applications (vim, git, ls, etc.)
   - Verify readability and aesthetics

4. **Consider Accessibility**:
   - Avoid colors that are difficult for colorblind users
   - Ensure sufficient brightness differentiation
   - Test with colorblindness simulators

#### Option C: Export from Windows Terminal

If you've customized a scheme in Windows Terminal:

1. Open Windows Terminal
2. Press `Ctrl+,` to open settings
3. Click on `Color schemes` in the sidebar
4. Select your custom scheme
5. Copy the JSON configuration
6. Save to a file: `windows-terminal-schemes/your-scheme-name.json`

**Note**: Windows Terminal stores schemes in `%LOCALAPPDATA%\Packages\Microsoft.WindowsTerminal_*\LocalState\settings.json`

### Step 2: Validate Your Scheme

Before submitting, validate your color scheme to ensure it meets all requirements.

#### Automated Validation

**JSON Syntax Check**:
```bash
# Using Python (built-in on most systems)
python -m json.tool windows-terminal-schemes/your-scheme.json

# Using Node.js
node -e "JSON.parse(require('fs').readFileSync('windows-terminal-schemes/your-scheme.json'))"

# Using jq (if installed)
jq . windows-terminal-schemes/your-scheme.json
```

**Verify Required Properties**:
```bash
# Check all required properties are present
grep -E '"(name|background|foreground|black|red|green|yellow|blue|purple|cyan|white|bright)"' \
  windows-terminal-schemes/your-scheme.json | wc -l

# Should output: 18 (for 18 required properties)
```

**Validate Color Format**:
```bash
# Check all colors are in #RRGGBB format
grep -oE '"#[0-9A-Fa-f]{6}"' windows-terminal-schemes/your-scheme.json

# Should show all your colors in correct format
```

#### Manual Validation

Use this checklist to manually validate your scheme:

**Validation Checklist**:
- [ ] JSON file is valid (no syntax errors)
- [ ] All 18 required properties are present
- [ ] All colors are in `#RRGGBB` hexadecimal format
- [ ] Scheme `name` is unique (not already in `windows-terminal-schemes/`)
- [ ] Foreground and background have sufficient contrast (ratio > 3.0)
- [ ] File name matches scheme name (kebab-case, e.g., `tokyo-night.json`)
- [ ] No trailing commas in JSON
- [ ] File uses UTF-8 encoding
- [ ] File has Unix line endings (LF, not CRLF) - use `dos2unix` if needed

**Contrast Ratio Validation**:

Calculate contrast ratio using this formula:
```
Contrast Ratio = (L1 + 0.05) / (L2 + 0.05)
where L1 is lighter color luminance, L2 is darker color luminance
```

Online tools:
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Contrast Ratio Calculator](https://contrast-ratio.com/)

**Minimum Requirements**:
- WCAG AA Large Text: 3.0:1 (absolute minimum)
- WCAG AA Normal Text: 4.5:1 (recommended)
- WCAG AAA: 7.0:1 (excellent)

### Step 3: Test the Conversion

Before submitting, test that your scheme converts correctly to an IntelliJ theme.

#### Run the Build

```bash
# Clean previous builds
./gradlew clean

# Generate themes
./gradlew createThemes

# On Windows:
gradlew.bat clean createThemes
```

**Expected Output**:
```
> Task :createThemes
Processing Windows Terminal schemes...
  ✓ Loaded: dracula.json
  ✓ Loaded: nord.json
  ✓ Loaded: your-scheme-name.json (NEW)

Analyzing color palettes...
  ✓ Analyzed: dracula.json (Edge case: NORMAL)
  ✓ Analyzed: nord.json (Edge case: NORMAL)
  ✓ Analyzed: your-scheme-name.json (Edge case: NORMAL)

Generating themes...
  ✓ Generated UI theme: build/themes/dracula.theme.json
  ✓ Generated color scheme: build/themes/dracula.xml
  ...
  ✓ Generated UI theme: build/themes/your-scheme-name.theme.json
  ✓ Generated color scheme: build/themes/your-scheme-name.xml

BUILD SUCCESSFUL in 6s
```

**Check for Warnings**:
```bash
# Review build logs for any warnings or adjustments
cat build/logs/theme-generation.log | grep -A 5 "your-scheme-name"
```

Common warnings to look for:
- Contrast ratio adjustments (colors lightened/darkened)
- Edge case handling (monochrome, high/low contrast)
- Missing optional properties (cursorColor, selectionBackground)
- Color pool limitations (insufficient color variety)

#### Install and Test in IntelliJ

1. **Locate generated files**:
   ```
   build/themes/your-scheme-name.theme.json
   build/themes/your-scheme-name.xml
   ```

2. **Copy to IntelliJ config directory**:
   ```bash
   # Linux/macOS
   cp build/themes/your-scheme-name.theme.json ~/.config/JetBrains/IntelliJIdea2023.1/themes/
   cp build/themes/your-scheme-name.xml ~/.config/JetBrains/IntelliJIdea2023.1/colors/

   # Windows (PowerShell)
   Copy-Item build/themes/your-scheme-name.theme.json $env:APPDATA\JetBrains\IntelliJIdea2023.1\themes\
   Copy-Item build/themes/your-scheme-name.xml $env:APPDATA\JetBrains\IntelliJIdea2023.1\colors\
   ```

3. **Restart IntelliJ IDEA**

4. **Apply the theme**:
   - Go to `Settings | Appearance & Behavior | Appearance`
   - Select your theme from the dropdown
   - Click `OK`

5. **Test thoroughly**:
   - **Editor Syntax Highlighting**:
     - Open Java, Kotlin, Python, JavaScript files
     - Verify keywords, strings, comments, functions, classes are distinguishable
     - Check syntax errors appear in red
   - **Terminal Colors**:
     - Open `View | Tool Windows | Terminal`
     - Run commands with colored output: `ls --color`, `git status`, `npm test`
     - Verify ANSI colors match Windows Terminal scheme
   - **UI Elements**:
     - Check tool windows, menus, dialogs
     - Verify borders, backgrounds, text are readable
     - Test both focused and unfocused states
   - **Diffs and VCS**:
     - Open a Git diff (`Git | Show History | Show Diff`)
     - Verify added/removed/changed lines are distinguishable
   - **Search Results**:
     - Use `Ctrl+Shift+F` to search project
     - Verify highlighted results are visible

6. **Take screenshots** (optional but recommended):
   - Editor with syntax-highlighted code
   - Terminal with colored output
   - UI elements (tool windows, menus)
   - Include in PR for reviewers

### Step 4: Quality Checklist

Before submitting your PR, complete this comprehensive quality checklist:

#### File Quality
- [ ] Valid JSON format (passes `json.tool` or `jq` validation)
- [ ] All 18 required properties present
- [ ] All colors in `#RRGGBB` format (uppercase or lowercase)
- [ ] Scheme name is descriptive and unique
- [ ] File name matches scheme name (kebab-case: `dracula.json`, not `Dracula.json`)
- [ ] File size < 1 KB (typical for color schemes)
- [ ] UTF-8 encoding (no BOM)
- [ ] Unix line endings (LF, not CRLF)
- [ ] No trailing commas
- [ ] Proper indentation (2 or 4 spaces)

#### Color Quality
- [ ] Foreground vs background contrast ratio > 3.0 (calculated and verified)
- [ ] Sufficient color variety (not monochrome unless intentional)
- [ ] Bright colors visually distinct from normal colors
- [ ] No duplicate color values (unless intentional, e.g., Solarized)
- [ ] Colors follow a cohesive aesthetic

#### Build Quality
- [ ] Build succeeds without errors (`./gradlew createThemes`)
- [ ] No critical warnings in build logs
- [ ] Generated `.theme.json` file is valid JSON
- [ ] Generated `.xml` file is valid XML
- [ ] Theme appears in IntelliJ theme selector after installation

#### Testing Quality
- [ ] Tested in IntelliJ IDEA (or other JetBrains IDE)
- [ ] Editor syntax highlighting looks correct
- [ ] Terminal colors match Windows Terminal
- [ ] UI elements are readable and aesthetically pleasing
- [ ] No contrast issues (text is readable on backgrounds)
- [ ] Tested with multiple file types (Java, Kotlin, Python, etc.)

#### Documentation Quality
- [ ] Original source/author credited in PR description
- [ ] License information included (if not public domain)
- [ ] Any special considerations noted (e.g., designed for colorblind users)
- [ ] Screenshots included (optional but helpful)

#### Licensing and Attribution
- [ ] Scheme license is compatible (MIT, BSD, CC0, public domain)
- [ ] Original author permission obtained (if required)
- [ ] Attribution included in PR description
- [ ] License file included if scheme requires separate license

### Step 5: Submit a Pull Request

Once all validation and testing is complete, submit your contribution!

#### Fork and Clone

```bash
# Fork the repository on GitHub (click "Fork" button)

# Clone your fork
git clone https://github.com/YOUR_USERNAME/jetbrains-one-dark-theme.git
cd jetbrains-one-dark-theme

# Add upstream remote
git remote add upstream https://github.com/one-dark/jetbrains-one-dark-theme.git
```

#### Create a Branch

```bash
# Create a feature branch
git checkout -b add-tokyo-night-scheme

# Or for multiple schemes:
git checkout -b add-catppuccin-variants
```

**Branch Naming Conventions**:
- Single scheme: `add-<scheme-name>-scheme` (e.g., `add-dracula-scheme`)
- Multiple schemes: `add-<family>-schemes` (e.g., `add-catppuccin-schemes`)
- Fix: `fix-<scheme-name>-colors` (e.g., `fix-nord-blue-color`)

#### Commit Your Changes

```bash
# Add your scheme file
git add windows-terminal-schemes/tokyo-night.json

# Commit with descriptive message
git commit -m "Add Tokyo Night color scheme

- Source: https://github.com/enkia/tokyo-night-vscode-theme
- License: MIT
- Description: Clean dark theme inspired by Tokyo's night skyline
- Tested in IntelliJ IDEA 2023.1"
```

**Commit Message Guidelines**:
- Use imperative mood ("Add" not "Added")
- First line: Brief summary (50 chars or less)
- Body: Detailed description, source, license, testing notes
- Include issue number if applicable: "Fixes #123"

#### Push and Create PR

```bash
# Push to your fork
git push origin add-tokyo-night-scheme

# Go to GitHub and create Pull Request
```

#### PR Template

Use this template for your pull request description:

```markdown
## Description

Add [Scheme Name] color scheme for Windows Terminal to IntelliJ conversion.

## Scheme Details

- **Name**: Tokyo Night
- **Type**: Dark / Light
- **Source**: [URL or "Original creation"]
- **Author**: [Original author name]
- **License**: MIT (or public domain, BSD, etc.)
- **Description**: Clean dark theme inspired by Tokyo's night skyline

## Testing

- [x] Validated JSON format
- [x] Verified all required properties
- [x] Built successfully (`./gradlew createThemes`)
- [x] Tested in IntelliJ IDEA 2023.1
- [x] Verified syntax highlighting
- [x] Verified terminal colors
- [x] Verified UI elements

## Screenshots

<!-- Optional: Include screenshots of the theme in use -->

**Editor**:
![Editor screenshot](url-to-screenshot)

**Terminal**:
![Terminal screenshot](url-to-screenshot)

## Attribution

Original color scheme by [Author Name], used with permission / under [License].

## Checklist

- [x] Followed contribution guidelines
- [x] Validated color contrast (ratio: 8.2:1)
- [x] Tested conversion to IntelliJ theme
- [x] Included license information
- [x] Used kebab-case file naming
```

## Testing Requirements

### Minimum Testing Requirements

Before submitting, you **must** complete these tests:

1. **JSON Validation**: Scheme file is valid JSON
2. **Build Test**: `./gradlew createThemes` succeeds without errors
3. **Installation Test**: Theme installs in IntelliJ and appears in theme selector
4. **Syntax Test**: Open at least one source file and verify syntax highlighting works
5. **Terminal Test**: Open terminal and verify ANSI colors display correctly

### Recommended Testing (Optional but Encouraged)

1. **Multi-Language Test**: Test with Java, Kotlin, Python, JavaScript, etc.
2. **VCS Test**: Verify Git diff colors are distinguishable
3. **Search Test**: Verify search result highlighting is visible
4. **UI Test**: Check tool windows, menus, dialogs for readability
5. **Contrast Test**: Use accessibility tools to verify WCAG compliance
6. **Colorblind Test**: Use colorblind simulators to check accessibility

### Testing Tools

**Contrast Checkers**:
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Contrast Ratio](https://contrast-ratio.com/)

**Colorblind Simulators**:
- [Coblis Color Blindness Simulator](https://www.color-blindness.com/coblis-color-blindness-simulator/)
- [Toptal Colorblind Web Page Filter](https://www.toptal.com/designers/colorfilter)

**JSON Validators**:
- [JSONLint](https://jsonlint.com/)
- Built-in: `python -m json.tool`
- Built-in: `jq`

## Naming Conventions

### File Names

Use **kebab-case** (lowercase with hyphens) for file names:

**Correct**:
- `dracula.json`
- `tokyo-night.json`
- `catppuccin-mocha.json`
- `one-half-dark.json`

**Incorrect**:
- `Dracula.json` (capital letters)
- `tokyo_night.json` (underscores)
- `TokyoNight.json` (camelCase)
- `tokyo night.json` (spaces)

### Scheme Names

The `name` property in the JSON file should be:
- **Title Case**: Each word capitalized
- **Descriptive**: Clearly identifies the scheme
- **Unique**: Not already used by another scheme

**Examples**:
- `"Dracula"`
- `"Tokyo Night"`
- `"Catppuccin Mocha"`
- `"One Half Dark"`
- `"Solarized Light"`

**Avoid**:
- Generic names: `"Dark Theme"`, `"My Theme"`
- Version numbers: `"Dracula v2.1"` (unless part of official name)
- IDE names: `"Dracula for IntelliJ"` (implied by context)

## Licensing

### Accepted Licenses

We accept color schemes under these licenses:

**Preferred** (most permissive):
- MIT License
- BSD License (2-clause or 3-clause)
- Apache License 2.0
- CC0 (Public Domain)

**Acceptable** (with restrictions):
- GPL v2 or v3 (if compatible with project)
- Creative Commons Attribution (CC BY)

**Not Accepted**:
- Proprietary licenses without permission
- Non-commercial licenses (NC restriction)
- No-derivatives licenses (ND restriction)

### Verifying License

1. **Check original repository**: Look for LICENSE or README file
2. **Check scheme comments**: Some schemes include license in JSON comments
3. **Contact author**: If license is unclear, ask the original author
4. **Document in PR**: Include license information in pull request description

### Attribution Requirements

When submitting a scheme:

1. **Credit Original Author**: Include author name in PR description
2. **Link to Source**: Provide URL to original scheme or author's website
3. **Preserve License**: If scheme includes license text, preserve it
4. **Note Modifications**: If you modified the scheme, note changes made

**Example Attribution**:
```
This scheme is based on the "Dracula" color theme by Zeno Rocha.
Source: https://draculatheme.com/
License: MIT
```

### Creating Original Schemes

If you create an original color scheme:

1. You retain copyright
2. You must license it under a compatible open-source license (MIT recommended)
3. Include license information in PR description
4. Consider adding yourself to contributors list

## Code of Conduct

### Our Standards

We are committed to providing a welcoming and inclusive environment for all contributors.

**Expected Behavior**:
- Be respectful and considerate
- Welcome diverse perspectives
- Focus on constructive feedback
- Accept responsibility for mistakes
- Show empathy toward others

**Unacceptable Behavior**:
- Harassment, discrimination, or offensive comments
- Personal attacks or insults
- Trolling or inflammatory comments
- Publishing private information without consent
- Any conduct that would be inappropriate in a professional setting

### Reporting

If you experience or witness unacceptable behavior:

1. **Contact maintainers**: Open a confidential issue or email maintainers
2. **Provide details**: Include date, time, and description of incident
3. **Expect privacy**: Reports will be kept confidential
4. **Expect action**: Maintainers will investigate and take appropriate action

### Enforcement

Maintainers have the right to:
- Remove, edit, or reject contributions that violate the Code of Conduct
- Temporarily or permanently ban contributors for inappropriate behavior
- Report violations to GitHub or authorities if necessary

## Review Process

### What to Expect During Review

1. **Automated Checks** (within minutes):
   - CI/CD pipeline runs build tests
   - JSON validation
   - File format checks
   - Naming convention validation

2. **Maintainer Review** (within 1-7 days):
   - Code review by project maintainers
   - Verification of quality checklist
   - Testing of generated themes
   - License and attribution verification

3. **Feedback Loop** (as needed):
   - Maintainers may request changes
   - You update PR based on feedback
   - Process repeats until approved

4. **Approval and Merge** (after review):
   - Maintainer approves PR
   - PR is merged to main branch
   - Scheme included in next release

### Common Review Feedback

**Formatting Issues**:
- "Please fix JSON indentation (use 2 spaces)"
- "File name should be kebab-case: `tokyo-night.json`"
- "Please remove trailing comma on line 23"

**Validation Issues**:
- "Missing required property: `brightWhite`"
- "Color format incorrect: use `#RRGGBB` not `#RGB`"
- "Contrast ratio too low (2.1:1), please adjust colors"

**Testing Issues**:
- "Build failed, please run `./gradlew createThemes` locally"
- "Please test theme in IntelliJ and confirm it works"
- "Screenshots would help verify the theme looks correct"

**Licensing Issues**:
- "Please include license information in PR description"
- "Original author permission needed (contacted via email?)"
- "License incompatible, please use MIT or similar"

### Accelerating Review

To speed up review:
- Complete all checklist items before submitting
- Include screenshots (reduces back-and-forth)
- Provide detailed PR description
- Respond promptly to feedback
- Run CI checks locally before pushing

## Attribution

### How Contributors Are Recognized

Contributors are recognized in several ways:

1. **Git History**: Your commits appear in project history
2. **Release Notes**: Contributors mentioned in CHANGELOG.md
3. **Contributors Section**: Added to README.md (if configured with [All Contributors](https://allcontributors.org/))
4. **GitHub Profile**: PR shows on your GitHub contribution graph

### Contributor Badge

After your PR is merged, you can add yourself to the contributors list:

```bash
# Install all-contributors CLI (if configured)
npm install --global all-contributors-cli

# Add yourself
all-contributors add <your-github-username> code

# Or request maintainers to add you
```

## Additional Resources

### Documentation

- [README_WINDOWS_TERMINAL.md](../README_WINDOWS_TERMINAL.md) - User guide for the converter
- [COLOR_MAPPING.md](COLOR_MAPPING.md) - Technical details of color mapping
- [SYNTAX_INFERENCE_ALGORITHM.md](SYNTAX_INFERENCE_ALGORITHM.md) - Algorithm documentation

### Windows Terminal Resources

- [Windows Terminal Documentation](https://docs.microsoft.com/en-us/windows/terminal/)
- [Windows Terminal Color Schemes](https://docs.microsoft.com/en-us/windows/terminal/customize-settings/color-schemes)
- [Windows Terminal Themes Website](https://windowsterminalthemes.dev/)

### IntelliJ Platform Resources

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Color Scheme Management](https://plugins.jetbrains.com/docs/intellij/color-scheme-management.html)
- [Editor Color Schemes](https://www.jetbrains.com/help/idea/configuring-colors-and-fonts.html)

### Color Theory and Accessibility

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Contrast Ratio Recommendations](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)
- [Color Blindness Types](https://www.color-blindness.com/types-of-color-blindness/)

## Frequently Asked Questions

**Q: Can I submit multiple color schemes in one PR?**

A: Yes, if they're related (e.g., "Catppuccin Latte" and "Catppuccin Mocha"). For unrelated schemes, separate PRs are preferred.

**Q: What if my scheme already exists in the repository?**

A: Check `windows-terminal-schemes/` directory first. If it exists and you're improving it, mention that in your PR. If it's a
variant (e.g., "Dracula Pro"), use a distinct name.

**Q: Can I submit a light theme?**

A: Absolutely! Light themes are welcome and needed for comprehensive testing.

**Q: What if my scheme has unusual colors (monochrome, high contrast)?**

A: Submit it! The system handles edge cases automatically. Mention the special characteristics in your PR description.

**Q: How long does review take?**

A: Typically 1-7 days. Complex PRs or those requiring licensing clarification may take longer.

**Q: Can I modify an existing scheme?**

A: Yes, but document changes clearly and explain why (e.g., "Improved contrast for accessibility").

**Q: What if the generated IntelliJ theme doesn't look quite right?**

A: This is feedback we want! Include details in PR description or open a separate issue. The inference algorithm may need adjustment.

---

**Thank you for contributing to the Windows Terminal to IntelliJ Theme Converter!**

Your contribution helps developers enjoy beautiful, consistent color schemes across their development tools. We appreciate your
time and effort in making this project better.

If you have questions not covered in this guide, please open a GitHub issue or contact the maintainers.

---

**Document Version**: 1.0
**Last Updated**: 2025-11-21
**Status**: Active
