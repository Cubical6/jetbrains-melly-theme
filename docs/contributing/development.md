# Development

## Windows Terminal Theme Generation

This plugin converts Windows Terminal color schemes into JetBrains IDE themes using [custom UI themes](https://blog.jetbrains.com/idea/2019/03/brighten-up-your-day-add-color-to-intellij-idea) for the JetBrains platform.

### Input Format

Windows Terminal color schemes are JSON files with 16 ANSI colors plus special colors:
- **Required colors**: `background`, `foreground`, plus 8 standard ANSI colors (`black`, `red`, `green`, `yellow`, `blue`, `purple`, `cyan`, `white`) and their 8 bright variants
- **Optional colors**: `cursorColor`, `selectionBackground`

Color schemes are stored in `windows-terminal-schemes/` directory.

### Theme Generation

Themes are automatically generated during the build process or when running `./gradlew createThemes`.

The generation process:
1. Loads Windows Terminal schemes from `windows-terminal-schemes/*.json`
2. Analyzes color palettes (luminance, hue, saturation)
3. Maps console colors (direct 1:1 ANSI mapping)
4. Infers syntax colors (intelligent semantic algorithm)
5. Generates UI theme JSON and color scheme XML files

For details on the color mapping algorithm, see [SYNTAX_INFERENCE_ALGORITHM.md](../SYNTAX_INFERENCE_ALGORITHM.md).

### Template Structure

The Windows Terminal template uses placeholder variables that are replaced during build:

- `$wt_red$`, `$wt_blue$`, etc. - Direct ANSI color mappings from Windows Terminal scheme
- `$keyword$`, `$string$`, etc. - Inferred syntax colors based on semantic rules
- Font style modifiers (`bold`, `italic`) are applied based on scheme characteristics

Template file: `buildSrc/templates/windows-terminal.template.xml`

## Color Mapping

The plugin uses an intelligent color inference algorithm to expand Windows Terminal's 20 colors into 100+ IDE syntax attributes.

**Semantic Mapping Rules:**
- **Keywords** (e.g., `public`, `class`) → Blue/purple from palette
- **Strings** (e.g., `"text"`) → Green from palette
- **Comments** → Dimmed gray/dark color
- **Numbers** → Yellow/cyan from palette
- **Functions** → Cyan/blue from palette
- **Errors** → Red from palette (exact match)

For the complete mapping specification, see [COLOR_MAPPING.md](../COLOR_MAPPING.md).

## Building the plugin

To build the plugin, run `./gradlew build`. 
If using IntelliJ, sync the Gradle project and run the **Build** task.

## Testing the plugin

To test the plugin, run `./gradlew runIde` to build and launch the plugin in a fresh instance of IntelliJ.
If using IntelliJ, run the **Run IDE** task.
All plugins will be disabled and settings will be the default settings for a new installation.

When testing Windows Terminal themes:
1. Go to `Preferences | Appearance & Behavior | Appearance`
2. Windows Terminal themes are available in the theme dropdown alongside built-in JetBrains themes
3. Select any Windows Terminal theme (e.g., Dracula, Nord, Tokyo Night) to test
4. Verify syntax highlighting in the editor and console colors in the built-in terminal

