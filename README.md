# Windows Terminal & PowerShell Themes for JetBrains IDEs


> Import any Windows Terminal color scheme as a complete JetBrains IDE theme with matching console colors!

![Code example](https://github.com/Cubical6/jetbrains-melly-theme/raw/master/docs/screenshots/default.png)

## Overview

This plugin automatically converts Windows Terminal and PowerShell color schemes to JetBrains IDE themes! Bring your favorite terminal color schemes to your IDE with full ANSI color support and intelligent syntax highlighting.

### How It Works

The system intelligently expands Windows Terminal's 20 colors (16 ANSI + 4 special) into 100+ syntax highlighting attributes:

```
Windows Terminal Scheme (20 colors)
          ↓
    Color Analysis
    ↓            ↓
Console Mapping   Syntax Inference
(Direct 1:1)     (Semantic Algorithm)
    ↓            ↓
Complete JetBrains Theme
```

**Key Features:**
- ✅ Full ANSI color mapping (all 16 colors + foreground/background)
- ✅ Intelligent syntax highlighting inference from terminal colors
- ✅ 60+ pre-configured popular color schemes included
- ✅ Easy addition of custom Windows Terminal schemes
- ✅ Consistent colors between terminal and IDE
- ✅ Edge case handling (monochrome, high/low contrast)
- ✅ WCAG accessibility support

## Available Themes

The plugin includes **60+ popular Windows Terminal color schemes**:

**Dark Themes:**
- **Dracula** - Vibrant purple and pink accents
- **Nord** - Arctic, north-bluish palette
- **Tokyo Night** - Clean theme inspired by Tokyo's skyline
- **Gruvbox Dark** - Retro groove with warm colors
- **Monokai Soda** - Enhanced Monokai variation
- **Catppuccin Mocha** - Soothing pastel theme
- **GitHub Dark** - GitHub's official dark scheme
- **Material** - Google's Material Design palette
- **Solarized Dark** - Classic scientifically-designed colors
- **Breeze** - KDE Plasma's terminal scheme
- And 50+ more!

**Light Themes:**
- **Gruvbox Light** - Retro groove with warm light colors
- **Solarized Light** - Perfect for daylight use
- **Ayu Light** - Minimalist light theme
- **Atom One Light** - Atom editor's popular light theme
- And more!

See [windows-terminal-schemes/SCHEMES.md](windows-terminal-schemes/SCHEMES.md) for the complete list with previews.

## Prerequisites

**Supported JetBrains IDEs:**
- IntelliJ IDEA (Community & Ultimate)
- PyCharm (Community & Professional)
- WebStorm
- PhpStorm
- RubyMine
- GoLand
- CLion
- DataGrip
- Rider
- Android Studio
- All other JetBrains IDEs based on IntelliJ Platform

**Minimum Requirements:**
- JetBrains IDE version 2020.1 or higher
- No additional dependencies required

**Operating Systems:**
- ✅ Windows 10/11
- ✅ macOS (all versions)
- ✅ Linux (Ubuntu, Fedora, Arch, etc.)

## Installation

### Method 1: Manual Installation from GitHub Releases (Recommended)

**For Windows:**

1. Download the latest `.zip` plugin file from [GitHub Releases](https://github.com/Cubical6/jetbrains-melly-theme/releases)
2. Open your JetBrains IDE
3. Go to `File` → `Settings` → `Plugins`
4. Click the gear icon ⚙️ → `Install Plugin from Disk...`
5. Navigate to the downloaded `.zip` file and select it
6. Click **OK** and restart the IDE

**For Ubuntu/Linux:**

1. Download the latest `.zip` plugin file:
   ```bash
   cd ~/Downloads
   wget https://github.com/Cubical6/jetbrains-melly-theme/releases/latest/download/jetbrains-melly-theme.zip
   ```
2. Open your JetBrains IDE
3. Go to `File` → `Settings` → `Plugins`
4. Click the gear icon ⚙️ → `Install Plugin from Disk...`
5. Navigate to `~/Downloads/jetbrains-melly-theme.zip`
6. Click **OK** and restart the IDE

**For macOS:**

1. Download the latest `.zip` plugin file from [GitHub Releases](https://github.com/Cubical6/jetbrains-melly-theme/releases)
2. Open your JetBrains IDE
3. Go to `IntelliJ IDEA` → `Preferences` → `Plugins`
4. Click the gear icon ⚙️ → `Install Plugin from Disk...`
5. Navigate to the downloaded `.zip` file in your Downloads folder
6. Click **OK** and restart the IDE

### Method 2: Build from Source (For Developers)

**Prerequisites:**
- JDK 11 or higher
- Git

**For Windows:**

```powershell
# Clone the repository
git clone https://github.com/Cubical6/jetbrains-melly-theme.git
cd jetbrains-melly-theme

# Build the plugin
.\gradlew.bat buildPlugin

# The plugin will be in build/distributions/
```

**For Ubuntu/Linux:**

```bash
# Install dependencies (if needed)
sudo apt update
sudo apt install git openjdk-11-jdk  # Ubuntu/Debian
# OR
sudo dnf install git java-11-openjdk-devel  # Fedora
# OR
sudo pacman -S git jdk11-openjdk  # Arch

# Clone the repository
git clone https://github.com/Cubical6/jetbrains-melly-theme.git
cd jetbrains-melly-theme

# Build the plugin
./gradlew buildPlugin

# The plugin will be in build/distributions/
```

**For macOS:**

```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java
brew install openjdk@11

# Clone the repository
git clone https://github.com/Cubical6/jetbrains-melly-theme.git
cd jetbrains-melly-theme

# Build the plugin
./gradlew buildPlugin

# The plugin will be in build/distributions/
```

Then install the generated `.zip` file using Method 2 above.

## Setup & Configuration

### Activating a Theme

**For Windows:**

1. After installation, go to `File` → `Settings` → `Appearance & Behavior` → `Appearance`
2. In the **Theme** dropdown, you'll see all Windows Terminal themes
3. Select your preferred theme (e.g., "Dracula", "Nord", "Tokyo Night")
4. Click **Apply** to preview or **OK** to confirm
5. The theme applies immediately to both UI and editor colors

**For Ubuntu/Linux:**

1. After installation, press `Ctrl+Alt+S` to open Settings (or `File` → `Settings`)
2. Navigate to `Appearance & Behavior` → `Appearance`
3. In the **Theme** dropdown, browse available Windows Terminal themes
4. Select your preferred theme (e.g., "Gruvbox Dark", "Material", "Solarized Dark")
5. Click **Apply** to preview or **OK** to confirm
6. Changes take effect immediately

**For macOS:**

1. After installation, press `⌘,` to open Preferences (or `IntelliJ IDEA` → `Preferences`)
2. Navigate to `Appearance & Behavior` → `Appearance`
3. In the **Theme** dropdown, select a Windows Terminal theme
4. Click **Apply** to preview or **OK** to confirm

![Configuration example](https://github.com/Cubical6/jetbrains-melly-theme/raw/master/docs/screenshots/configuration.png)

### Verifying Console Colors

To verify that console ANSI colors are working correctly:

**All Platforms:**

1. Open the **Terminal** tool window in your IDE (`View` → `Tool Windows` → `Terminal`)
2. Run a command that outputs colored text:
   ```bash
   # Linux/macOS
   ls --color=auto

   # Windows PowerShell
   Get-ChildItem | Format-Table -AutoSize
   ```
3. Colors should match your selected Windows Terminal theme

### Switching Between Themes

You can quickly switch themes without restarting:

1. Press `Ctrl+Shift+A` (Windows/Linux) or `⌘⇧A` (macOS) to open Actions
2. Type "Theme" and select **Preferences: Theme**
3. Select a different Windows Terminal theme
4. Changes apply instantly

## Adding Custom Schemes

Want to use your own Windows Terminal color scheme?

### Quick Method

**For Windows:**

1. Copy your `.json` scheme file to the plugin directory:
   ```powershell
   # Example path (adjust for your IDE and version)
   Copy-Item "C:\path\to\your-scheme.json" "$env:APPDATA\JetBrains\IntelliJIdea2024.1\plugins\jetbrains-melly-theme\windows-terminal-schemes\"
   ```

2. Rebuild the plugin or restart the IDE

**For Ubuntu/Linux:**

1. Copy your `.json` scheme file:
   ```bash
   # Example path (adjust for your IDE and version)
   cp /path/to/your-scheme.json ~/.local/share/JetBrains/IntelliJIdea2024.1/jetbrains-melly-theme/windows-terminal-schemes/
   ```

2. Rebuild the plugin or restart the IDE

**For macOS:**

1. Copy your `.json` scheme file:
   ```bash
   # Example path (adjust for your IDE and version)
   cp /path/to/your-scheme.json ~/Library/Application\ Support/JetBrains/IntelliJIdea2024.1/jetbrains-melly-theme/windows-terminal-schemes/
   ```

2. Rebuild the plugin or restart the IDE

### Developer Method (Build from Source)

If you've cloned the repository:

**All Platforms:**

1. Add your `.json` color scheme file to `windows-terminal-schemes/`
2. Run the theme generator:
   ```bash
   # Linux/macOS
   ./gradlew generateThemesFromWindowsTerminal

   # Windows
   .\gradlew.bat generateThemesFromWindowsTerminal
   ```
3. Your custom theme will be automatically generated in `build/themes/`
4. Rebuild and reinstall the plugin

**JSON Scheme Format:**

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

For detailed instructions, see [docs/CONTRIBUTING_SCHEMES.md](docs/CONTRIBUTING_SCHEMES.md)

## Importing iTerm Color Schemes

You can import iTerm2 color schemes and automatically convert them to JetBrains IDE themes with enhanced color derivation!

### Import Process

**For All Platforms:**

1. **Place your iTerm color scheme files** in the `iterm-schemes/` directory:
   ```bash
   # Copy your .itermcolors files
   cp /path/to/your-scheme.itermcolors iterm-schemes/
   ```

2. **Import the iTerm schemes** to convert them to the internal format:
   ```bash
   # Linux/macOS
   ./gradlew importITermSchemes

   # Windows
   .\gradlew.bat importITermSchemes
   ```

   This will parse the `.itermcolors` XML files and convert them to JSON color schemes.

3. **Generate JetBrains themes** with intelligent color derivation:
   ```bash
   # Linux/macOS
   ./gradlew createThemes

   # Windows
   .\gradlew.bat createThemes
   ```

   The theme generator will automatically derive approximately **50 additional semantic colors** from the iTerm color scheme's base colors using intelligent color analysis algorithms.

### Enhanced Color Derivation

The import process includes advanced features:

- **Intelligent Color Analysis** - Automatically detects theme brightness, contrast, and color temperature
- **Semantic Color Derivation** - Generates ~50 semantic colors for syntax highlighting, UI elements, and editor features
- **Contrast Optimization** - Ensures derived colors maintain readability and WCAG accessibility standards
- **Tone Consistency** - Preserves the original theme's aesthetic while expanding the color palette

The derived colors include:
- Syntax highlighting colors (keywords, strings, comments, etc.)
- UI element colors (selections, highlights, backgrounds)
- Editor gutter and margin colors
- Search and occurrence highlights
- Diff and VCS colors
- Error, warning, and info indicators

This allows iTerm2 color schemes (which typically have 16-20 colors) to be expanded into complete IDE themes with 100+ carefully coordinated colors.

## Troubleshooting

### Theme doesn't appear in dropdown

**Windows:**
- Verify plugin installation: `File` → `Settings` → `Plugins` → Check "Windows Terminal Theme" is installed and enabled
- Try: `File` → `Invalidate Caches / Restart...` → `Invalidate and Restart`

**Ubuntu/Linux:**
- Check plugin status: `File` → `Settings` → `Plugins`
- Clear caches: `File` → `Invalidate Caches / Restart...`
- Check file permissions:
  ```bash
  ls -la ~/.local/share/JetBrains/IntelliJIdea*/plugins/
  # Ensure you have read access to plugin directory
  ```

**macOS:**
- Verify installation in `Preferences` → `Plugins`
- Clear caches: `File` → `Invalidate Caches / Restart...`

### Colors look wrong or washed out

**All Platforms:**
1. Ensure you selected a Windows Terminal theme (not built-in IntelliJ themes)
2. Try a different theme to isolate the issue
3. Check if IDE is using the correct color scheme:
   - Go to `Settings/Preferences` → `Editor` → `Color Scheme`
   - Should match your selected theme
4. Clear IDE caches: `File` → `Invalidate Caches / Restart...`

### Plugin won't install

**Windows:**
- Ensure you're running IDE as Administrator if installing manually
- Check disk space availability
- Verify `.zip` file is not corrupted (re-download if needed)

**Ubuntu/Linux:**
- Check Java version:
  ```bash
  java -version  # Should be Java 11 or higher
  ```
- Verify file permissions:
  ```bash
  chmod +r /path/to/downloaded-plugin.zip
  ```

**macOS:**
- Check security settings: `System Preferences` → `Security & Privacy`
- Ensure IDE has necessary permissions

### Console colors not working

**All Platforms:**
1. Verify theme is applied: `Settings/Preferences` → `Appearance & Behavior` → `Appearance`
2. Check color scheme matches: `Settings/Preferences` → `Editor` → `Color Scheme` → `Console Colors`
3. Test with a fresh terminal session (close and reopen Terminal tool window)

For more help, see [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) or [submit an issue](https://github.com/Cubical6/jetbrains-melly-theme/issues/new).

## Documentation

- 📖 [Complete User Guide](docs/README.md) - Comprehensive documentation
- 🎨 [Available Themes](windows-terminal-schemes/SCHEMES.md) - Browse all 60+ themes
- 🔧 [Contributing Schemes](docs/CONTRIBUTING_SCHEMES.md) - Add your own themes
- 💻 [Development Guide](docs/contributing/development.md) - Build and contribute
- 🧮 [Color Mapping Algorithm](docs/SYNTAX_INFERENCE_ALGORITHM.md) - Technical details
- 📐 [Architecture](docs/WINDOWS_TERMINAL_TEMPLATE.md) - System design

## FAQ

**Q: Which JetBrains IDEs are supported?**
A: All IntelliJ Platform-based IDEs (IntelliJ IDEA, PyCharm, WebStorm, PhpStorm, etc.)

**Q: Will this affect my existing themes?**
A: No, it adds new themes without modifying built-in themes.

**Q: Can I use this with Windows Terminal?**
A: Yes! That's the whole point - match your IDE colors to Windows Terminal.

**Q: How do I uninstall?**
A: Go to `Settings/Preferences` → `Plugins` → Find plugin → Click gear icon → `Uninstall`.

**Q: Do I need Windows Terminal installed?**
A: No, the plugin works independently. You just use Windows Terminal color schemes.

**Q: Can I customize a theme?**
A: Yes, you can modify the generated theme files or create custom color schemes. See [docs/CONTRIBUTING_SCHEMES.md](docs/CONTRIBUTING_SCHEMES.md).

## For Developers

### Build Tasks

```bash
# Generate themes from Windows Terminal schemes
./gradlew generateThemesFromWindowsTerminal

# Build the plugin
./gradlew buildPlugin

# Run tests
./gradlew test

# Run IDE with plugin for testing
./gradlew runIde
```

See [docs/contributing/development.md](docs/contributing/development.md) for detailed development instructions.

## Contributing

We welcome contributions! Here's how you can help:

- 🎨 **Add new color schemes** - Share your favorite Windows Terminal themes
- 🐛 **Report bugs** - [Submit an issue](https://github.com/Cubical6/jetbrains-melly-theme/issues/new)
- 📝 **Improve documentation** - Fix typos, add examples, clarify instructions
- 💻 **Submit code** - Fix bugs or add features

See [docs/CONTRIBUTING_SCHEMES.md](docs/CONTRIBUTING_SCHEMES.md) for contribution guidelines.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

Individual Windows Terminal color schemes maintain their original licenses. Most schemes are MIT licensed or public domain.

## Credits & Acknowledgments

- **Windows Terminal Team** - For creating the excellent Windows Terminal and color scheme specification
- **[iTerm2-Color-Schemes](https://github.com/mbadolato/iTerm2-Color-Schemes)** - Comprehensive collection of terminal color schemes
- **JetBrains** - For the IntelliJ Platform SDK and theme documentation
- **[Atom One Dark Theme](https://github.com/atom/atom/tree/master/packages/one-dark-syntax)** - Original inspiration that started this project
- **[Egor Yurtaev](https://github.com/yurtaev)** - Created the repository this project was based on

---

**Made with ❤️ for developers who love consistent color schemes across their tools**
