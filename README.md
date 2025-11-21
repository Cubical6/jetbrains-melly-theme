# Windows Terminal & PowerShell Themes for JetBrains IDEs

[![All Contributors](https://img.shields.io/badge/all_contributors-12-orange.svg)](#contributors)

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

### Method 1: Install from JetBrains Marketplace (Recommended)

**For Windows:**

1. **Open your JetBrains IDE** (e.g., IntelliJ IDEA, PyCharm, WebStorm)
2. Go to `File` → `Settings` → `Plugins`
3. Click on the `Marketplace` tab
4. Search for **"Windows Terminal Theme"** or **"Melly Theme"**
5. Click **Install**
6. Click **Restart IDE** when prompted

**For Ubuntu/Linux:**

1. **Open your JetBrains IDE**
2. Go to `File` → `Settings` → `Plugins` (or `Configure` → `Plugins` from welcome screen)
3. Click on the `Marketplace` tab
4. Search for **"Windows Terminal Theme"** or **"Melly Theme"**
5. Click **Install**
6. Click **Restart IDE** when prompted

**For macOS:**

1. **Open your JetBrains IDE**
2. Go to `IntelliJ IDEA` → `Preferences` → `Plugins` (or `⌘,` then Plugins)
3. Click on the `Marketplace` tab
4. Search for **"Windows Terminal Theme"** or **"Melly Theme"**
5. Click **Install**
6. Click **Restart IDE** when prompted

### Method 2: Manual Installation from GitHub Releases

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

### Method 3: Build from Source (For Developers)

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

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/mskelton"><img src="https://avatars3.githubusercontent.com/u/25914066?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Mark Skelton</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/commits?author=mskelton" title="Code">💻</a> <a href="#question-mskelton" title="Answering Questions">💬</a> <a href="https://github.com/one-dark/jetbrains-one-dark-theme/commits?author=mskelton" title="Documentation">📖</a> <a href="#ideas-mskelton" title="Ideas, Planning, & Feedback">🤔</a> <a href="#maintenance-mskelton" title="Maintenance">🚧</a></td>
    <td align="center"><a href="https://www.dacoto.com"><img src="https://avatars2.githubusercontent.com/u/16915053?v=4?s=100" width="100px;" alt=""/><br /><sub><b>David Cortés</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Adacoto" title="Bug reports">🐛</a></td>
    <td align="center"><a href="http://x1unix.com"><img src="https://avatars0.githubusercontent.com/u/9203548?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Denis Sedchenko</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Ax1unix" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/zakh508"><img src="https://avatars1.githubusercontent.com/u/3613383?v=4?s=100" width="100px;" alt=""/><br /><sub><b>zakh508</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Azakh508" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/GauthierPLM"><img src="https://avatars0.githubusercontent.com/u/2579741?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Gauthier Pogam--Le Montagner</b></sub></a><br /><a href="#ideas-GauthierPLM" title="Ideas, Planning, & Feedback">🤔</a></td>
    <td align="center"><a href="https://github.com/eickit"><img src="https://avatars3.githubusercontent.com/u/4112464?v=4?s=100" width="100px;" alt=""/><br /><sub><b>eickit</b></sub></a><br /><a href="#design-eickit" title="Design">🎨</a></td>
    <td align="center"><a href="https://github.com/cnfn"><img src="https://avatars3.githubusercontent.com/u/1445517?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Cnfn</b></sub></a><br /><a href="#design-cnfn" title="Design">🎨</a></td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/cliffordp"><img src="https://avatars0.githubusercontent.com/u/1812179?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Clifford</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Acliffordp" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://defman.me"><img src="https://avatars2.githubusercontent.com/u/7100645?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Sergey Kislyakov</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Adefman21" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/sundongmin"><img src="https://avatars2.githubusercontent.com/u/17910228?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Sun Dongmin</b></sub></a><br /><a href="#design-sundongmin" title="Design">🎨</a></td>
    <td align="center"><a href="https://github.com/levani"><img src="https://avatars0.githubusercontent.com/u/184472?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Levani Melikishvili</b></sub></a><br /><a href="#design-levani" title="Design">🎨</a></td>
    <td align="center"><a href="https://blog.csdn.net/qq_21019419"><img src="https://avatars2.githubusercontent.com/u/12908403?v=4?s=100" width="100px;" alt=""/><br /><sub><b>lynn</b></sub></a><br /><a href="#design-tulongxCodes" title="Design">🎨</a></td>
    <td align="center"><a href="https://unthrottled.io"><img src="https://avatars1.githubusercontent.com/u/15972415?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Alex Simons</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/commits?author=Unthrottled" title="Code">💻</a> <a href="#question-Unthrottled" title="Answering Questions">💬</a></td>
    <td align="center"><a href="https://github.com/XanderCheung"><img src="https://avatars1.githubusercontent.com/u/28296509?v=4?s=100" width="100px;" alt=""/><br /><sub><b>XanderCheung</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3AXanderCheung" title="Bug reports">🐛</a></td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/maxmalov"><img src="https://avatars2.githubusercontent.com/u/284129?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Maxim Malov</b></sub></a><br /><a href="#design-maxmalov" title="Design">🎨</a></td>
    <td align="center"><a href="https://github.com/dorudumitru"><img src="https://avatars0.githubusercontent.com/u/11142539?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Doru Dumitru</b></sub></a><br /><a href="#ideas-dorudumitru" title="Ideas, Planning, & Feedback">🤔</a></td>
    <td align="center"><a href="https://blog.orange233.top/"><img src="https://avatars0.githubusercontent.com/u/30137964?v=4?s=100" width="100px;" alt=""/><br /><sub><b>chengziorange</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Achengziorange" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/hongsefb"><img src="https://avatars3.githubusercontent.com/u/29223722?v=4?s=100" width="100px;" alt=""/><br /><sub><b>hongsefb</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Ahongsefb" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/erikdewit87"><img src="https://avatars0.githubusercontent.com/u/1140942?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Erik de Wit</b></sub></a><br /><a href="#ideas-erikdewit87" title="Ideas, Planning, & Feedback">🤔</a> <a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Aerikdewit87" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/skrubbel"><img src="https://avatars1.githubusercontent.com/u/868432?v=4?s=100" width="100px;" alt=""/><br /><sub><b>skrubbel</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Askrubbel" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/ChrisCarini"><img src="https://avatars1.githubusercontent.com/u/6374067?v=4?s=100" width="100px;" alt=""/><br /><sub><b>ChrisCarini</b></sub></a><br /><a href="#maintenance-ChrisCarini" title="Maintenance">🚧</a></td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/chenchenfang"><img src="https://avatars1.githubusercontent.com/u/50065243?v=4?s=100" width="100px;" alt=""/><br /><sub><b>chenchenfang</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Achenchenfang" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/RichardConfused"><img src="https://avatars3.githubusercontent.com/u/54979163?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Dust Wind</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3ARichardConfused" title="Bug reports">🐛</a></td>
    <td align="center"><a href="http://www.jellysoft.pl"><img src="https://avatars1.githubusercontent.com/u/2669079?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Krzysztof Jeliński</b></sub></a><br /><a href="#design-jelinski" title="Design">🎨</a></td>
    <td align="center"><a href="https://github.com/infix"><img src="https://avatars1.githubusercontent.com/u/40860821?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Amr</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Ainfix" title="Bug reports">🐛</a></td>
    <td align="center"><a href="http://heitorcolangelo.dev"><img src="https://avatars.githubusercontent.com/u/6201773?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Heitor Colangelo</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Aheitorcolangelo" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://www.linkedin.com/in/yuri-karpovich-09737b27"><img src="https://avatars.githubusercontent.com/u/7230069?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Yuri Karpovich</b></sub></a><br /><a href="#design-yuri-karpovich" title="Design">🎨</a></td>
    <td align="center"><a href="https://github.com/liy-cn"><img src="https://avatars.githubusercontent.com/u/2853829?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Michael Lee</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Aliy-cn" title="Bug reports">🐛</a></td>
  </tr>
  <tr>
    <td align="center"><a href="https://kamilki.me/"><img src="https://avatars.githubusercontent.com/u/10383567?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Kamil Trysiński</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3AKamilkime" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/Lignium"><img src="https://avatars.githubusercontent.com/u/41531939?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Lignium</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3ALignium" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/dorudumitru-hh"><img src="https://avatars.githubusercontent.com/u/40240395?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Doru Dumitru</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Adorudumitru-hh" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://www.muyu.party"><img src="https://avatars.githubusercontent.com/u/20837526?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Zhou Yu</b></sub></a><br /><a href="#design-muyu66" title="Design">🎨</a></td>
    <td align="center"><a href="https://lovesykun.cn"><img src="https://avatars.githubusercontent.com/u/5022927?v=4?s=100" width="100px;" alt=""/><br /><sub><b>LoveSy</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Ayujincheng08" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/ziishaned"><img src="https://avatars.githubusercontent.com/u/16267321?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Zeeshan Ahmad</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/issues?q=author%3Aziishaned" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/ilyapopovs"><img src="https://avatars.githubusercontent.com/u/16862411?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Ilya Popovs</b></sub></a><br /><a href="https://github.com/one-dark/jetbrains-one-dark-theme/commits?author=ilyapopovs" title="Documentation">📖</a> <a href="#design-ilyapopovs" title="Design">🎨</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

---

**Made with ❤️ for developers who love consistent color schemes across their tools**
