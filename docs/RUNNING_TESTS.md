# Running Tests - Quick Reference Guide

This guide provides quick commands for running the Sprint 4 test suite.

## Prerequisites

```bash
# Navigate to project root
cd /home/user/jetbrains-melly-theme

# Verify test resources exist
ls buildSrc/src/test/resources/test-schemes/  # Should show 15+ test schemes
ls windows-terminal-schemes/                    # Should show 15+ WT schemes
```

## Quick Commands

### Run All Tests

```bash
# All tests in entire project
./gradlew test

# All tests in buildSrc only
./gradlew :buildSrc:test

# All integration tests only
./gradlew :buildSrc:test --tests "integration.*"
```

### Run Specific Test Suites

```bash
# E2E Integration Tests (TASK-604)
./gradlew :buildSrc:test --tests "integration.BuildIntegrationTest"

# Regression Tests (TASK-604a)
./gradlew :buildSrc:test --tests "integration.RegressionTest"
```

### Run Individual Tests

```bash
# Complete theme generation test
./gradlew :buildSrc:test --tests "integration.BuildIntegrationTest.testCompleteThemeGeneration"

# One Dark regression test (ensures backward compatibility with existing themes)
./gradlew :buildSrc:test --tests "integration.RegressionTest.testOneDarkThemesStillGenerate"

# Multiple scheme generation
./gradlew :buildSrc:test --tests "integration.BuildIntegrationTest.testMultipleSchemeGeneration"
```

### Run with Verbose Output

```bash
# Show test output during execution
./gradlew :buildSrc:test --tests "integration.*" --info

# Show full stack traces on failure
./gradlew :buildSrc:test --tests "integration.*" --stacktrace

# Show all output (very verbose)
./gradlew :buildSrc:test --tests "integration.*" --debug
```

### Run Tests Continuously

```bash
# Re-run tests on file changes
./gradlew :buildSrc:test --continuous

# With specific test pattern
./gradlew :buildSrc:test --tests "integration.*" --continuous
```

## Test Reports

### View Test Results

After running tests, view HTML reports:

```bash
# Open test report in browser
open buildSrc/build/reports/tests/test/index.html

# Or navigate manually to:
# buildSrc/build/reports/tests/test/index.html
```

### Generate Coverage Report

```bash
# Run tests with coverage
./gradlew :buildSrc:test jacocoTestReport

# View coverage report
open buildSrc/build/reports/jacoco/test/html/index.html
```

## Common Test Scenarios

### First Time Running Tests

```bash
# Clean build and run all tests
./gradlew clean :buildSrc:test --tests "integration.*"

# This will generate baseline fixtures for regression tests
# Fixtures saved to: buildSrc/src/test/resources/fixtures/
```

### After Changing Templates

```bash
# Delete old baseline fixtures
rm -rf buildSrc/src/test/resources/fixtures/*

# Run regression tests to regenerate baselines
./gradlew :buildSrc:test --tests "integration.RegressionTest.testBaselineComparison"

# Verify new baselines
ls -la buildSrc/src/test/resources/fixtures/
```

### After Changing Generators

```bash
# Run all tests to verify changes
./gradlew :buildSrc:test --tests "integration.*"

# Focus on integration tests
./gradlew :buildSrc:test --tests "integration.BuildIntegrationTest"

# Check regression
./gradlew :buildSrc:test --tests "integration.RegressionTest"
```

### Testing Specific Schemes

```bash
# Add a new test scheme
cp my-scheme.json buildSrc/src/test/resources/test-schemes/

# Run tests (will automatically discover new scheme)
./gradlew :buildSrc:test --tests "integration.BuildIntegrationTest.testMultipleSchemeGeneration"
```

## Troubleshooting

### Tests Fail to Compile

```bash
# Clean and rebuild
./gradlew clean build

# Check for syntax errors
./gradlew :buildSrc:compileTestKotlin
```

### Missing Test Resources

```bash
# Verify test schemes exist
ls -la buildSrc/src/test/resources/test-schemes/

# Should see files like:
# - normal-test.json
# - light-theme-test.json
# - monochrome-test.json
# - high-contrast-test.json
# - etc.
```

### Gradle Wrapper Issues

```bash
# If gradlew not executable
chmod +x gradlew

# If Gradle version issues
./gradlew wrapper --gradle-version=7.5.1
```

### Network Issues

Tests are designed to work offline. If initial Gradle setup fails:

```bash
# Check Gradle daemon status
./gradlew --status

# Stop daemon and retry
./gradlew --stop
./gradlew :buildSrc:test
```

### Test-Specific Issues

```bash
# Run single test with full output
./gradlew :buildSrc:test \
  --tests "integration.BuildIntegrationTest.testCompleteThemeGeneration" \
  --info \
  --stacktrace

# Check test logs
cat buildSrc/build/test-results/test/TEST-integration.BuildIntegrationTest.xml
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Run Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Run tests
        run: ./gradlew :buildSrc:test --tests "integration.*"
      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v2
        with:
          name: test-report
          path: buildSrc/build/reports/tests/test/
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh './gradlew :buildSrc:test --tests "integration.*"'
            }
        }
        stage('Report') {
            steps {
                publishHTML([
                    reportDir: 'buildSrc/build/reports/tests/test',
                    reportFiles: 'index.html',
                    reportName: 'Test Report'
                ])
            }
        }
    }
}
```

## Test Output Interpretation

### Successful Test Run

```
> Task :buildSrc:test

integration.BuildIntegrationTest > testCompleteThemeGeneration() PASSED
integration.BuildIntegrationTest > testMultipleSchemeGeneration() PASSED
integration.BuildIntegrationTest > testDarkThemeGeneration() PASSED
... (more tests)

BUILD SUCCESSFUL in 15s
```

### Failed Test Example

```
> Task :buildSrc:test

integration.BuildIntegrationTest > testCompleteThemeGeneration() FAILED
    io.kotest.assertions.AssertionFailedError: expected:<true> but was:<false>
        at integration.BuildIntegrationTest.testCompleteThemeGeneration(BuildIntegrationTest.kt:123)

BUILD FAILED in 8s
```

View full report: `buildSrc/build/reports/tests/test/index.html`

## Performance Benchmarks

Expected test execution times:

| Test Suite | Approximate Time |
|------------|-----------------|
| BuildIntegrationTest | 5-10 seconds |
| RegressionTest | 5-10 seconds |
| All Integration Tests | 10-20 seconds |
| Full Test Suite | 30-60 seconds |

Times may vary based on system performance.

## Manual Testing

For comprehensive manual testing, follow:

```bash
# Open the manual testing checklist
cat docs/MANUAL_TESTING_CHECKLIST.md

# Or in a markdown viewer
# docs/MANUAL_TESTING_CHECKLIST.md
```

## Quick Health Check

Run this to verify everything is working:

```bash
#!/bin/bash
echo "=== Sprint 4 Test Suite Health Check ==="
echo ""

echo "1. Checking test resources..."
test -d buildSrc/src/test/resources/test-schemes && echo "✓ Test schemes directory exists" || echo "✗ Test schemes missing"
test -d windows-terminal-schemes && echo "✓ Windows Terminal schemes directory exists" || echo "✗ WT schemes missing"

echo ""
echo "2. Checking test files..."
test -f buildSrc/src/test/kotlin/integration/BuildIntegrationTest.kt && echo "✓ BuildIntegrationTest.kt exists" || echo "✗ BuildIntegrationTest.kt missing"
test -f buildSrc/src/test/kotlin/integration/RegressionTest.kt && echo "✓ RegressionTest.kt exists" || echo "✗ RegressionTest.kt missing"

echo ""
echo "3. Counting test schemes..."
SCHEME_COUNT=$(ls -1 buildSrc/src/test/resources/test-schemes/*.json 2>/dev/null | wc -l)
echo "   Test schemes: $SCHEME_COUNT (minimum 5 required)"

echo ""
echo "4. Running quick test..."
./gradlew :buildSrc:test --tests "integration.BuildIntegrationTest.testCompleteThemeGeneration" -q

echo ""
echo "=== Health Check Complete ==="
```

Save as `check-tests.sh`, make executable with `chmod +x check-tests.sh`, and run.

## Additional Resources

- **Full Documentation**: [docs/SPRINT_4_TESTING_DELIVERABLES.md](SPRINT_4_TESTING_DELIVERABLES.md)
- **Manual Testing**: [docs/MANUAL_TESTING_CHECKLIST.md](MANUAL_TESTING_CHECKLIST.md)
- **Integration Tests Guide**: [buildSrc/src/test/kotlin/integration/README.md](../buildSrc/src/test/kotlin/integration/README.md)

## Support

For issues or questions:
1. Check test logs: `buildSrc/build/reports/tests/test/`
2. Review documentation above
3. Check existing test patterns in test files
4. Create issue with test output and error details

---

**Last Updated**: 2025-11-21
**Version**: 1.0
