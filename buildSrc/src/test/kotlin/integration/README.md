# Integration Tests

This directory contains integration tests for the Windows Terminal to IntelliJ theme conversion system.

## Test Files

### BuildIntegrationTest.kt

End-to-end integration tests for the complete build process.

**Purpose**: Verify that the entire theme generation pipeline works correctly from Windows Terminal JSON to IntelliJ theme files.

**Test Coverage**:
- Complete theme generation (XML + JSON)
- Multiple scheme generation
- Dark theme detection and generation
- Light theme detection and generation
- Monochrome theme generation
- High contrast theme generation
- Normal contrast theme generation
- Output file structure validation
- XML validation (well-formed, parseable)
- JSON validation (well-formed, parseable)
- Theme metadata generation
- Console color mapping (exact ANSI color preservation)
- Theme ID uniqueness
- File naming conventions
- Plugin XML compatibility
- Error handling

**Number of Tests**: 18 test methods

**Key Test Scenarios**:
1. `testCompleteThemeGeneration` - Verifies both XML and JSON files are generated
2. `testMultipleSchemeGeneration` - Tests generation for multiple schemes at once
3. `testDarkThemeGeneration` - Validates dark theme detection
4. `testLightThemeGeneration` - Validates light theme detection
5. `testMonochromeThemeGeneration` - Edge case for grayscale themes
6. `testHighContrastThemeGeneration` - Edge case for high contrast themes
7. `testXmlValidation` - Ensures XML is well-formed and parseable
8. `testJsonValidation` - Ensures JSON is well-formed and parseable
9. `testConsoleColorMapping` - Verifies ANSI colors match Windows Terminal exactly
10. `testThemeIdUniqueness` - Ensures no duplicate theme IDs

### RegressionTest.kt

Backward compatibility and regression tests.

**Purpose**: Ensure that changes to the build system don't break existing functionality and that One Dark themes continue to generate correctly.

**Test Coverage**:
- One Dark theme generation (no errors)
- One Dark theme content consistency
- One Dark color preservation (signature colors)
- Baseline comparison (fixture-based)
- Content stability (deterministic generation)
- Legacy build tasks (ColorSchemeRegistry)
- Legacy scheme loading (all Windows Terminal schemes)
- Plugin XML backward compatibility
- Version 1.0 compatibility
- Console color mapping regression
- Color normalization consistency
- Template processor backward compatibility
- Template variable substitution

**Number of Tests**: 13 test methods

**Key Test Scenarios**:
1. `testOneDarkThemesStillGenerate` - Verifies One Dark generates without errors
2. `testOneDarkThemeContentUnchanged` - Validates structure matches baseline
3. `testBaselineComparison` - Compares with fixture files
4. `testContentStability` - Ensures deterministic output
5. `testLegacyBuildTasksStillWork` - Tests ColorSchemeRegistry
6. `testNoColorMappingRegression` - Verifies console colors
7. `testTemplateProcessorBackwardCompatibility` - Tests template processing

## Running the Tests

### Run All Integration Tests

```bash
./gradlew :buildSrc:test --tests "integration.*"
```

### Run BuildIntegrationTest Only

```bash
./gradlew :buildSrc:test --tests "integration.BuildIntegrationTest"
```

### Run RegressionTest Only

```bash
./gradlew :buildSrc:test --tests "integration.RegressionTest"
```

### Run Specific Test

```bash
./gradlew :buildSrc:test --tests "integration.BuildIntegrationTest.testCompleteThemeGeneration"
```

## Test Resources

### Test Schemes

Located in: `buildSrc/src/test/resources/test-schemes/`

Available test schemes:
- `normal-test.json` - Standard One Dark-based theme
- `light-theme-test.json` - Light theme for contrast testing
- `monochrome-test.json` - Grayscale theme edge case
- `high-contrast-test.json` - High contrast colors
- `low-contrast-test.json` - Low contrast edge case
- `minimal-test.json` - Minimal required properties
- `pastel-test.json` - Pastel colors
- And more...

### Fixtures

Located in: `buildSrc/src/test/resources/fixtures/`

Baseline fixtures for regression testing:
- `baseline-*.xml` - Expected XML output
- `baseline-*.theme.json` - Expected JSON output

**Note**: Fixtures are generated on first test run and used for comparison on subsequent runs.

## Test Dependencies

The tests use:
- JUnit 5 (Jupiter) for test framework
- Kotest matchers for assertions
- Gson for JSON parsing and comparison
- Java XML parsers for XML validation

All dependencies are defined in `buildSrc/build.gradle.kts`.

## Writing New Tests

### Test Structure

```kotlin
@Test
fun `testDescription - what this test verifies`() {
    // Arrange - set up test data
    val scheme = loadTestScheme("test-file.json")
    val outputPath = tempOutputDir.resolve("output.xml")

    // Act - perform the action
    generator.generate(scheme, outputPath)

    // Assert - verify results
    outputPath.exists() shouldBe true
}
```

### Helper Methods

Both test classes provide common helper methods:
- `loadTestScheme(fileName)` - Load scheme from test-schemes directory
- `loadWindowsTerminalScheme(fileName)` - Load from windows-terminal-schemes
- `sanitizeFileName(name)` - Sanitize scheme name for file system
- `validateXmlFile(path)` - Validate XML is well-formed
- `validateJsonFile(path)` - Validate JSON is well-formed
- `compareFileContent(file1, file2)` - Compare file contents
- `calculateFileHash(file)` - Calculate SHA-256 hash

### Using Temporary Directories

Tests automatically create and clean up temporary directories:

```kotlin
@BeforeEach
fun setup() {
    tempOutputDir = Files.createTempDirectory("test-output")
}

@AfterEach
fun cleanup() {
    if (tempOutputDir.exists()) {
        tempOutputDir.toFile().deleteRecursively()
    }
}
```

## Continuous Integration

These tests should be run:
- On every commit
- Before every pull request merge
- Before release builds
- After template changes
- After generator changes

## Test Coverage Goals

Target coverage:
- Line coverage: > 80%
- Branch coverage: > 75%
- Integration coverage: > 90%

## Troubleshooting

### Tests Fail Due to Missing Schemes

Ensure test schemes are present:
```bash
ls buildSrc/src/test/resources/test-schemes/
```

### Tests Fail Due to Missing Windows Terminal Schemes

Ensure Windows Terminal schemes are present:
```bash
ls windows-terminal-schemes/
```

Should contain at least 15 schemes.

### Baseline Comparison Failures

If baseline fixtures are outdated:
1. Delete existing fixtures: `rm buildSrc/src/test/resources/fixtures/*`
2. Re-run tests to regenerate baselines
3. Commit new baselines if changes are expected

### XML Validation Failures

Check that:
- Template file exists: `buildSrc/templates/windows-terminal.template.xml`
- Template is well-formed XML
- All placeholders are defined

### JSON Validation Failures

Check that:
- Template file exists: `buildSrc/templates/windows-terminal.template.theme.json`
- Template is well-formed JSON
- All placeholders are defined

## Related Documentation

- [Manual Testing Checklist](../../../docs/MANUAL_TESTING_CHECKLIST.md)
- [Build System Documentation](../../../docs/BUILD.md) (if exists)
- [Contributing Guidelines](../../../CONTRIBUTING.md) (if exists)

## Maintainers

These tests are part of Sprint 4 (TASK-604, TASK-604a).

For questions or issues, refer to project documentation or create an issue.
