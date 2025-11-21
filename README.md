# Windows Terminal & PowerShell Themes for JetBrains IDEs

[![All Contributors](https://img.shields.io/badge/all_contributors-12-orange.svg)](#contributors)

> Import any Windows Terminal color scheme as a complete JetBrains IDE theme with matching console colors!

![Code example](https://github.com/Cubical6/jetbrains-melly-theme/raw/master/docs/screenshots/default.png)

## Overview

This plugin automatically converts Windows Terminal and PowerShell color schemes to IntelliJ themes! Bring your favorite terminal color schemes to your IDE with full ANSI color support and intelligent syntax highlighting.

### Features

- Full ANSI color mapping (all 16 colors + foreground/background)
- Intelligent syntax highlighting inference from terminal colors
- 15+ pre-configured popular color schemes included
- Easy addition of custom Windows Terminal schemes
- Consistent colors between terminal and IDE

### Available Themes

The plugin includes **60+ popular Windows Terminal color schemes**:

**Dark Themes:**
- Dracula - Vibrant purple and pink accents
- Nord - Arctic, north-bluish palette
- Tokyo Night - Clean theme inspired by Tokyo's skyline
- Gruvbox Dark - Retro groove with warm colors
- Monokai Soda - Enhanced Monokai variation
- Catppuccin Mocha - Soothing pastel theme
- GitHub Dark - GitHub's official dark scheme
- Material - Google's Material Design palette
- Solarized Dark - Classic scientifically-designed colors
- Breeze - KDE Plasma's terminal scheme
- And 50+ more!

**Light Themes:**
- Gruvbox Light - Retro groove with warm light colors
- Solarized Light - Perfect for daylight use
- Ayu Light - Minimalist light theme
- Atom One Light - Atom editor's popular light theme
- And more!

See [windows-terminal-schemes/SCHEMES.md](windows-terminal-schemes/SCHEMES.md) for the complete list.

### Quick Start

1. **Install the plugin** from the JetBrains marketplace
2. **Select a theme** by going to `Preferences | Appearance & Behavior | Appearance`
3. **Choose a Windows Terminal theme** from the theme dropdown (e.g., "Dracula", "Nord", "Tokyo Night")
4. **Click OK** to apply the changes

### Adding Custom Schemes

Want to use your own Windows Terminal color scheme?

1. Add your `.json` color scheme file to `windows-terminal-schemes/`
2. Run `./gradlew generateThemesFromWindowsTerminal`
3. Your custom theme will be automatically generated

For detailed instructions, see [windows-terminal-schemes/README.md](windows-terminal-schemes/README.md)

### Build Tasks

The plugin provides Gradle tasks for Windows Terminal integration:

- `importWindowsTerminalSchemes` - Import and validate color schemes
- `generateThemesFromWindowsTerminal` - Generate IntelliJ themes from schemes
- `build` - Automatically includes theme generation

### Learn More

- [Windows Terminal Schemes Collection](windows-terminal-schemes/README.md) - View all included themes
- [Windows Terminal Template Documentation](docs/WINDOWS_TERMINAL_TEMPLATE.md) - Technical details
- [Color Mapping Strategy](docs/SYNTAX_INFERENCE_ALGORITHM.md) - How colors are mapped

## Do you need help?

If you have any problems, please [submit an issue](https://github.com/Cubical6/jetbrains-melly-theme/issues/new).

## Setup

After installing the plugin, go to `Preferences | Appearance & Behavior | Appearance` and select one of the Windows Terminal themes in the theme dropdown. Once you have selected a theme, click the **OK** button to apply the changes!

![Configuration example](https://github.com/Cubical6/jetbrains-melly-theme/raw/master/docs/screenshots/configuration.png)

## Further Documentation

Check out the [docs](docs/README.md) for additional information and contributing guidelines.

- [Windows Terminal Theme Documentation](README_WINDOWS_TERMINAL.md)
- [Contributing Schemes](docs/CONTRIBUTING_SCHEMES.md)
- [Development Guide](docs/contributing/development.md)

## Thanks

- Thanks to [Egor Yurtaev](https://github.com/yurtaev) who created the repository this project was based on. Without that starting point, this plugin may have never been created.
- Thanks to GitBook for hosting the docs!
- Finally, thanks to everyone who has contributed to this project through issues, pull requests, and plugin usage. Your usage and feedback has helped to make this plugin what it is!

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
