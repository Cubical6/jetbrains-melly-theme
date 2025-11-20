# Theme Versioning and Compatibility Strategy

**Version:** 1.0
**Date:** 2025-11-20
**Sprint:** 1 (Foundation)
**Status:** DRAFT

---

## Table of Contents

1. [Overview](#overview)
2. [Theme ID Generation](#theme-id-generation)
3. [Version Numbering](#version-numbering)
4. [Backward Compatibility](#backward-compatibility)
5. [IntelliJ Version Compatibility](#intellij-version-compatibility)
6. [Migration Strategy](#migration-strategy)
7. [Metadata Schema](#metadata-schema)
8. [Implementation](#implementation)

---

## Overview

This document defines the versioning and compatibility strategy for the JetBrains Melly Theme system, specifically addressing the integration of Windows Terminal color scheme conversion while maintaining backward compatibility with existing One Dark themes.

### Design Principles

1. **No Breaking Changes**: Existing One Dark themes must continue to work
2. **Unique Identification**: Each generated theme must have a unique, stable ID
3. **Traceability**: Track source scheme, generation version, and creation date
4. **Version Safety**: Ensure themes are compatible with target IntelliJ versions
5. **Future-Proof**: Design for extensibility (future color scheme sources)

---

## Theme ID Generation

### Strategy: Hybrid Approach

**Format**: `{source-type}.{scheme-name-hash}.{variant}`

**Components**:
- `source-type`: Origin of the theme (`onedark` | `wt` | future sources)
- `scheme-name-hash`: First 8 characters of SHA-256 hash of normalized scheme name
- `variant`: Theme variant (`regular` | `italic` | `vivid` | `vivid-italic`)

### Examples

```
onedark.a3f4b9c2.regular           # Existing One Dark Regular
onedark.a3f4b9c2.italic            # Existing One Dark Italic
wt.d7e8f1a2.regular                # Windows Terminal "Dracula" Regular
wt.5c9a2b4f.regular                # Windows Terminal "Gruvbox Dark" Regular
```

### Rationale

- **Source Type Prefix**: Prevents collisions between different color scheme sources
- **Hash-Based ID**: Stable, deterministic, and collision-resistant
- **Human-Readable**: Includes recognizable scheme name in metadata
- **Short**: 8-character hash balances uniqueness with readability

### Implementation

```kotlin
fun generateThemeId(sourceType: String, schemeName: String, variant: String): String {
    val normalizedName = schemeName.lowercase().replace(Regex("[^a-z0-9]+"), "")
    val hash = MessageDigest.getInstance("SHA-256")
        .digest(normalizedName.toByteArray())
        .joinToString("") { "%02x".format(it) }
        .take(8)
    return "$sourceType.$hash.$variant"
}
```

### Collision Handling

- **Probability**: Negligible (2^32 possible values)
- **Detection**: Log warning if collision detected during build
- **Resolution**: Append incrementing suffix (`.1`, `.2`, etc.)

---

## Version Numbering

### Theme Version Format

**Format**: `MAJOR.MINOR.PATCH` (Semantic Versioning)

**Components**:
- `MAJOR`: Incompatible changes (new IntelliJ version requirement, breaking schema changes)
- `MINOR`: New features (new color attributes, enhanced inference algorithm)
- `PATCH`: Bug fixes (color correction, typo fixes)

### Current Versions

- **Existing One Dark Themes**: `1.0.0` (retroactively assigned)
- **Windows Terminal Themes**: Start at `2.0.0` (new major feature)

### Version Metadata

Each theme file includes version metadata:

```xml
<scheme name="Theme Name" version="142" parent_scheme="Darcula">
  <metaInfo>
    <property name="created">2025-11-20T00:00:00Z</property>
    <property name="ide">IntelliJ IDEA</property>
    <property name="ideVersion">2021.3.1.0.0</property>
    <property name="modified">2025-11-20T00:00:00Z</property>
    <property name="originalScheme">Windows Terminal - Dracula</property>

    <!-- Custom metadata -->
    <property name="theme.id">wt.d7e8f1a2.regular</property>
    <property name="theme.version">2.0.0</property>
    <property name="theme.source">windows-terminal</property>
    <property name="theme.generator.version">1.0.0</property>
  </metaInfo>
  <!-- ... color attributes ... -->
</scheme>
```

---

## Backward Compatibility

### Existing One Dark Themes

**Guarantee**: All existing One Dark themes will continue to work without modification.

### Compatibility Requirements

1. **No Template Changes**: Keep `one-dark.template.xml` unchanged
2. **No Task Renaming**: Existing `createThemes` task remains
3. **No Dependency Breaking**: Maintain existing Gradle task dependencies
4. **Parallel Generation**: New Windows Terminal themes generated alongside One Dark

### Implementation Strategy

#### Dual Template System

```kotlin
enum class ThemeSource {
    ONE_DARK,
    WINDOWS_TERMINAL
}

class ThemeConstructor(private val source: ThemeSource) {
    fun getTemplate(): Path {
        return when (source) {
            ThemeSource.ONE_DARK -> Paths.get("buildSrc/templates/one-dark.template.xml")
            ThemeSource.WINDOWS_TERMINAL -> Paths.get("buildSrc/templates/windows-terminal.template.xml")
        }
    }
}
```

#### Separate Gradle Tasks

```kotlin
// Existing task (unchanged)
tasks.register("createThemes", ThemeConstructor::class) {
    group = "themes"
    description = "Generate One Dark themes (existing)"
    source = ThemeSource.ONE_DARK
}

// New task for Windows Terminal
tasks.register("generateWindowsTerminalThemes", GenerateWindowsTerminalThemes::class) {
    group = "themes"
    description = "Generate themes from Windows Terminal color schemes"
}
```

### Testing Backward Compatibility

**Regression Test Suite** (TASK-604a):
- Verify all 4 existing One Dark themes still generate correctly
- Compare generated XML files against known-good baseline
- Test theme loading in IntelliJ IDEA
- Verify no changes to plugin.xml entries for existing themes

---

## IntelliJ Version Compatibility

### Target Versions

**Primary Target**: IntelliJ IDEA 2021.3.1+
**Extended Support**: 2020.3+ (if possible)
**Future Support**: 2025.1+ (forward compatibility)

### Version Compatibility Matrix

| Theme Version | IntelliJ Version | Notes                          |
|---------------|------------------|--------------------------------|
| 1.0.0         | 2021.3.1+        | Existing One Dark themes       |
| 2.0.0         | 2021.3.1+        | Windows Terminal themes        |
| 2.1.0         | 2022.1+          | Enhanced syntax inference      |
| 3.0.0         | 2025.1+          | Future major IntelliJ changes  |

### Compatibility Checks

**During Generation**:
```kotlin
fun validateCompatibility(theme: Theme, targetVersion: String): Boolean {
    val minVersion = "2021.3.1"
    return compareVersions(targetVersion, minVersion) >= 0
}
```

**In plugin.xml**:
```xml
<idea-version since-build="213" until-build="253.*"/>
<!-- 213 = 2021.3, 253 = 2025.3 -->
```

### Handling Version-Specific Features

**Conditional Attribute Inclusion**:
```kotlin
fun includeAttribute(attribute: String, targetVersion: String): Boolean {
    return when {
        attribute == "NEW_FEATURE" && compareVersions(targetVersion, "2023.1") < 0 -> false
        else -> true
    }
}
```

---

## Migration Strategy

### For Existing Users

**Scenario**: Users currently have One Dark themes installed

**Migration Options**:

#### Option 1: Coexistence (Recommended)
- Keep existing One Dark themes
- Add Windows Terminal themes alongside
- Users can switch via IDE settings
- No forced migration

#### Option 2: Opt-In Migration
- Provide migration wizard (future feature)
- Allow users to replace One Dark with equivalent Windows Terminal theme
- Preserve user customizations

#### Option 3: Hybrid Mode
- Generate both One Dark and Windows Terminal themes
- Provide mapping: One Dark → Closest Windows Terminal equivalent
- User chooses via configuration

### Migration Tool (Future)

```kotlin
class ThemeMigrationTool {
    fun findEquivalentWindowsTerminalTheme(oneDarkVariant: String): String? {
        return when (oneDarkVariant) {
            "onedark.a3f4b9c2.regular" -> "wt.onedark-port.regular"
            "onedark.a3f4b9c2.vivid" -> "wt.onedark-port.vivid"
            else -> null
        }
    }
}
```

---

## Metadata Schema

### Theme Metadata (Embedded in XML)

```yaml
metadata:
  # Core Identification
  theme.id: "wt.d7e8f1a2.regular"
  theme.name: "Windows Terminal - Dracula"
  theme.version: "2.0.0"

  # Source Information
  theme.source: "windows-terminal"
  theme.source.file: "dracula.json"
  theme.source.url: "https://windowsterminalthemes.dev/dracula"

  # Generation Information
  theme.generator.version: "1.0.0"
  theme.generator.timestamp: "2025-11-20T12:34:56Z"

  # Compatibility
  theme.intellij.min.version: "2021.3.1"
  theme.intellij.max.version: "2025.3.*"

  # Attribution
  theme.author: "Original: Zeno Rocha; Conversion: Automated"
  theme.license: "MIT"

  # Classification
  theme.background: "dark"  # or "light"
  theme.contrast: "medium"  # low, medium, high
  theme.color.count: 16     # Number of unique colors
```

### Metadata Storage

**Primary**: Embedded in theme XML file (IntelliJ standard)
**Secondary**: External `theme-metadata.json` (for tooling)

```json
{
  "themes": [
    {
      "id": "wt.d7e8f1a2.regular",
      "name": "Windows Terminal - Dracula",
      "version": "2.0.0",
      "source": "windows-terminal",
      "file": "wt.dracula.regular.xml",
      "created": "2025-11-20T12:34:56Z"
    }
  ]
}
```

---

## Implementation

### Sprint 1 Tasks

1. **TASK-100**: Define versioning strategy ✅ (this document)
2. **TASK-503**: Implement metadata generator
3. **TASK-503a**: Implement version compatibility checks

### Code Structure

```kotlin
// buildSrc/src/main/kotlin/versioning/
├── ThemeId.kt              # ID generation logic
├── ThemeVersion.kt         # Version comparison and validation
├── CompatibilityChecker.kt # IntelliJ version compatibility
└── MetadataGenerator.kt    # Metadata embedding
```

### Example Implementation

```kotlin
data class ThemeMetadata(
    val id: String,
    val name: String,
    val version: String,
    val source: ThemeSource,
    val generatorVersion: String,
    val created: Instant,
    val intellijMinVersion: String = "2021.3.1",
    val intellijMaxVersion: String = "2025.3.*"
)

class MetadataGenerator {
    fun generateMetadata(
        sourceScheme: WindowsTerminalColorScheme,
        variant: String
    ): ThemeMetadata {
        val id = generateThemeId("wt", sourceScheme.name, variant)
        return ThemeMetadata(
            id = id,
            name = "Windows Terminal - ${sourceScheme.name}",
            version = "2.0.0",
            source = ThemeSource.WINDOWS_TERMINAL,
            generatorVersion = "1.0.0",
            created = Instant.now()
        )
    }

    fun embedInXml(xml: Document, metadata: ThemeMetadata) {
        val metaInfo = xml.createElement("metaInfo")
        metadata.toProperties().forEach { (key, value) ->
            val property = xml.createElement("property")
            property.setAttribute("name", key)
            property.textContent = value
            metaInfo.appendChild(property)
        }
        xml.documentElement.appendChild(metaInfo)
    }
}
```

---

## Testing Strategy

### Version Compatibility Tests

```kotlin
@Test
fun `theme ID generation is stable and unique`() {
    val id1 = generateThemeId("wt", "Dracula", "regular")
    val id2 = generateThemeId("wt", "Dracula", "regular")
    assertEquals(id1, id2)  // Stable

    val id3 = generateThemeId("wt", "Gruvbox Dark", "regular")
    assertNotEquals(id1, id3)  // Unique
}

@Test
fun `backward compatibility preserved`() {
    val oneDarkThemes = generateOneDarkThemes()
    val baseline = loadBaselineThemes()

    oneDarkThemes.forEach { theme ->
        val expected = baseline[theme.id]
        assertNotNull(expected)
        assertEquals(expected.colors, theme.colors)
    }
}
```

---

## Future Considerations

### Additional Color Scheme Sources

**Potential Sources**:
- iTerm2 color schemes
- VSCode themes
- TextMate themes
- Custom JSON/YAML formats

**Source Type Prefixes**:
- `wt`: Windows Terminal
- `iterm`: iTerm2
- `vscode`: VSCode
- `tm`: TextMate
- `custom`: User-defined

### Advanced Versioning

**Per-Theme Version Tracking**:
- Track individual theme versions independently
- Allow selective theme updates
- Maintain changelog per theme

**Automatic Update Checks**:
- Compare local theme versions with upstream
- Notify users of available updates
- One-click update mechanism

---

## References

- [Semantic Versioning](https://semver.org/)
- [IntelliJ Platform Plugin Compatibility](https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html)
- [JetBrains Theme Structure](https://plugins.jetbrains.com/docs/intellij/theme-structure.html)

---

## Version History

| Version | Date       | Changes                          | Author          |
|---------|------------|----------------------------------|-----------------|
| 1.0     | 2025-11-20 | Initial versioning strategy      | Automated System|

---

**Status**: Ready for Review
**Next Steps**: Implement MetadataGenerator (TASK-503) and CompatibilityChecker (TASK-503a)
