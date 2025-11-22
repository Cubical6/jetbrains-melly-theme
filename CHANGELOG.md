# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **iTerm2 Color Scheme Import System**
  - Added `importITermSchemes` Gradle task to convert `.itermcolors` files to JSON format
  - Added `ITermColorsParser` to parse iTerm2 XML color scheme files
  - Added `iterm-schemes/` directory for placing `.itermcolors` files for import
  - Added automatic detection and conversion of iTerm2 RGB color values
  - Added support for importing and converting iTerm2's 16 ANSI colors plus special colors

- **Enhanced Color Derivation System**
  - Added intelligent derivation of ~50 semantic colors from base color schemes
  - Added color analysis algorithms to detect theme brightness, contrast, and temperature
  - Added semantic color generation for syntax highlighting elements
  - Added automatic UI element color derivation (selections, highlights, backgrounds)
  - Added contrast optimization to ensure WCAG accessibility standards
  - Added tone consistency preservation while expanding color palettes

- **Lovelace Reference Theme**
  - Added Lovelace color scheme as a reference implementation
  - Added Lovelace iTerm2 color scheme file (`Lovelace.itermcolors`)
  - Added generated Lovelace JetBrains IDE theme
  - Added comprehensive color palette demonstrating all derivation capabilities

- **Color Utility Functions**
  - Added `ColorUtils.lighten()` for creating lighter color variants
  - Added `ColorUtils.darken()` for creating darker color variants
  - Added `ColorUtils.adjustSaturation()` for saturation manipulation
  - Added `ColorUtils.blend()` for color blending operations
  - Added `ColorUtils.calculateContrast()` for WCAG contrast ratio calculations
  - Added `ColorUtils.ensureContrast()` for automatic contrast correction

- **Documentation**
  - Added "Importing iTerm Color Schemes" section to README.md
  - Added comprehensive import process instructions for all platforms
  - Added documentation of the 50-color derivation feature
  - Added enhanced color derivation feature descriptions
  - Added CHANGELOG.md to track project changes

### Changed

- **Extended ColorPalette Model**
  - Extended `ColorPalette` class to support derived semantic colors
  - Updated color palette structure to include syntax highlighting colors
  - Updated palette to include UI element colors (selections, highlights, gutters)
  - Updated palette to include editor-specific colors (margins, backgrounds)
  - Updated palette to include VCS and diff colors
  - Updated palette to include diagnostic colors (errors, warnings, info)
  - Expanded total color definitions from ~20 to 100+ colors

- **Template Enhancements**
  - Updated theme templates to utilize newly derived colors
  - Enhanced template structure to support semantic color assignments
  - Improved template organization for better maintainability
  - Updated color mapping logic for more intelligent assignments

- **Theme Generation Improvements**
  - Enriched generated themes with comprehensive color coverage
  - Improved theme quality with better contrast and readability
  - Enhanced consistency across different theme elements
  - Optimized color derivation algorithms for better aesthetic results

### Technical Details

- **Build System**
  - Added `importITermSchemes` task to Gradle build configuration
  - Updated `createThemes` task to leverage enhanced color derivation
  - Integrated iTerm2 import pipeline with existing theme generation

- **Color Processing Pipeline**
  - Implemented multi-stage color derivation pipeline
  - Added color analysis phase for theme property detection
  - Added semantic color generation phase
  - Added contrast validation and optimization phase
  - Added tone consistency verification phase

- **Parser Implementation**
  - Implemented robust XML parsing for `.itermcolors` format
  - Added error handling for malformed iTerm2 color files
  - Added validation for color value ranges and formats
  - Implemented color space conversion (RGB to hex)

- **Generator Enhancements**
  - Enhanced theme generator with color derivation capabilities
  - Improved algorithm for inferring syntax colors from terminal palettes
  - Added intelligent fallback strategies for missing colors
  - Optimized color selection for various UI contexts

## [0.1.0] - Initial Release

### Added
- Initial project setup
- Windows Terminal color scheme support
- Basic theme generation from Windows Terminal schemes
- 60+ pre-configured popular color schemes
- Full ANSI color mapping support
- Intelligent syntax highlighting inference
- JetBrains IDE plugin structure
- Gradle build system
- Core documentation

---

**Note**: This changelog covers significant changes and new features. For a complete list of commits and minor changes, see the [Git commit history](https://github.com/Cubical6/jetbrains-melly-theme/commits).
