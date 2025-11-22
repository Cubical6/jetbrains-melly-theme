# Documentation Index

Welcome to the theme generation system documentation. This directory contains comprehensive guides for understanding, maintaining, and troubleshooting the Windows Terminal to IntelliJ theme conversion system.

## Quick Navigation

### 📚 Core Documentation

#### [Theme Generation System](theme-generation.md)
**Start here** for a complete overview of the theme generation architecture.

Contents:
- System architecture and key components
- Process flow from color scheme to theme file
- How to add new UI sections
- Testing instructions
- Requirements and file locations

**Best for:** Understanding how the system works, making architectural changes, adding new features.

---

#### [Placeholder Conventions](placeholder-conventions.md)
**Critical reference** for placeholder naming rules and standards.

Contents:
- The golden rule: Use snake_case (except ANSI colors)
- Complete placeholder reference
- How to add new placeholders
- Common mistakes and how to avoid them

**Best for:** Adding new colors, fixing naming issues, template development.

---

#### [Troubleshooting Guide](troubleshooting.md)
**Problem-solving reference** for common issues and debugging.

Contents:
- Placeholders not replaced (most common issue)
- Rounded corners not showing
- Colors looking wrong
- Debugging theme generation
- Verification procedures

**Best for:** Fixing bugs, debugging generation issues, verifying output.

---

## Quick Reference

### Common Tasks

| Task | Documentation | Key Sections |
|------|---------------|--------------|
| Add a new UI color | [Theme Generation](theme-generation.md) | "How to Add New UI Sections" |
| Fix unreplaced placeholder | [Troubleshooting](troubleshooting.md) | "Issue 1: Placeholders Not Replaced" |
| Understand placeholder naming | [Placeholder Conventions](placeholder-conventions.md) | "The Golden Rule", "Complete Reference" |
| Debug theme generation | [Troubleshooting](troubleshooting.md) | "How to Debug Theme Generation" |
| Add rounded corners | [Theme Generation](theme-generation.md) | "Arc Placeholders" section |
| Verify generated themes | [Troubleshooting](troubleshooting.md) | "How to Verify Generated Themes" |

### Key Concepts

| Concept | Where to Learn |
|---------|---------------|
| snake_case naming requirement | [Placeholder Conventions](placeholder-conventions.md) - "The Golden Rule" |
| Color palette generation | [Theme Generation](theme-generation.md) - "WindowsTerminalColorScheme" |
| Template processing | [Theme Generation](theme-generation.md) - "TemplateProcessor" |
| Theme variants | [Theme Generation](theme-generation.md) - "Process Flow" |
| Arc values | [Placeholder Conventions](placeholder-conventions.md) - "Arc Placeholders" |

## Common Questions

### "Why aren't my placeholders being replaced?"

See: [Troubleshooting](troubleshooting.md) - Issue 1

**Quick answer:** Most likely a naming mismatch. Derived colors must use `snake_case` (e.g., `$wt_surface_light$` not `$wt_surfaceLight$`).

### "How do I add a new color to the palette?"

See: [Placeholder Conventions](placeholder-conventions.md) - "How to Add New Placeholders"

**Quick steps:**
1. Add property to `ColorPalette` data class
2. Calculate color in `toColorPalette()` method
3. Add mapping in `ColorPalette.toMap()`
4. Use in template as `$wt_my_new_color$`

### "Rounded corners aren't showing in my theme"

See: [Troubleshooting](troubleshooting.md) - Issue 2

**Quick checklist:**
- [ ] Using `windows-terminal-rounded.template.theme.json`?
- [ ] Arc placeholders replaced? (check for `$arc_` in output)
- [ ] IntelliJ version 2024.1+?
- [ ] New UI enabled?

### "How do I test my changes?"

See: [Theme Generation](theme-generation.md) - "Testing Instructions"

**Quick commands:**
```bash
# Run tests
./gradlew test --tests "themes.TemplateProcessorTest"

# Generate themes
./gradlew generateThemesFromWindowsTerminal

# Check for unreplaced placeholders
grep '\$' themes/ui/*.theme.json
```

## File Locations Quick Reference

| Component | Location |
|-----------|----------|
| Color scheme model | `/buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt` |
| Theme generator | `/buildSrc/src/main/kotlin/generators/UIThemeGenerator.kt` |
| Template processor | `/buildSrc/src/main/kotlin/themes/TemplateProcessor.kt` |
| Theme variants | `/buildSrc/src/main/kotlin/variants/ThemeVariant.kt` |
| Standard template | `/buildSrc/templates/windows-terminal.template.theme.json` |
| Rounded template | `/buildSrc/templates/windows-terminal-rounded.template.theme.json` |
| Generated themes | `/themes/ui/*.theme.json` |
| Unit tests | `/buildSrc/src/test/kotlin/` |

## Recent Fixes Reference

This documentation was created as part of a comprehensive fix for the theme generation system. Key fixes included:

1. **Task 1**: Fixed snake_case placeholder naming (e.g., `surfaceLight` → `surface_light`)
2. **Task 2**: Updated rounded template with proper snake_case placeholders
3. **Task 3**: Fixed arc placeholder replacement in UIThemeGenerator
4. **Task 4**: Ensured consistent naming across all templates
5. **Task 5**: Created comprehensive unit tests
6. **Task 6**: Validated all fixes with integration tests

See individual documentation files for detailed explanations of these fixes.

## Contributing

When making changes to the theme generation system:

1. **Read** the relevant documentation first
2. **Follow** placeholder naming conventions strictly
3. **Test** your changes with unit tests
4. **Verify** generated themes manually
5. **Update** documentation if you add new features

## Documentation Maintenance

This documentation should be updated when:
- New placeholders are added
- New UI sections are supported
- Template structure changes
- ColorPalette is expanded
- New variants are added
- Generation process changes

---

## Getting Started

**New to the system?** Read in this order:

1. [Theme Generation](theme-generation.md) - Understand the architecture
2. [Placeholder Conventions](placeholder-conventions.md) - Learn the naming rules
3. [Troubleshooting](troubleshooting.md) - Know how to debug issues

**Making changes?** Keep these open:

- [Placeholder Conventions](placeholder-conventions.md) - For naming reference
- [Troubleshooting](troubleshooting.md) - For verification steps

**Debugging issues?** Start with:

- [Troubleshooting](troubleshooting.md) - Common issues and solutions

---

*Last updated: 2025-11-22*
*Documentation created for comprehensive theme generation system maintenance*
