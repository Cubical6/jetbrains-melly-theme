package tasks

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * Tests for ImportWindowsTerminalSchemes task.
 *
 * Note: These tests focus on task configuration and basic functionality.
 * Full integration testing requires ColorSchemeRegistry which has its own test suite.
 */
class ImportWindowsTerminalSchemesTest {

    @Test
    fun `task can be registered and configured`() {
        // Create a test project
        val project: Project = ProjectBuilder.builder().build()

        // Register the task
        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Verify task properties
        task.group shouldBe "themes"
        task.description shouldBe "Import and validate Windows Terminal color schemes"
        task shouldNotBe null
    }

    @Test
    fun `task has correct default input directory convention`(@TempDir tempDir: Path) {
        val project: Project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .build()

        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Verify default convention is set
        val inputDir = task.inputDir.get().asFile
        inputDir.name shouldBe "windows-terminal-schemes"
    }

    @Test
    fun `task has correct default output file convention`(@TempDir tempDir: Path) {
        val project: Project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .build()

        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Verify default convention is set
        val outputFile = task.validationReport.get().asFile
        outputFile.path shouldContain "build"
        outputFile.path shouldContain "reports"
        outputFile.name shouldBe "wt-scheme-validation.txt"
    }

    @Test
    fun `task input directory can be configured`(@TempDir tempDir: Path) {
        val project: Project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .build()

        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Configure custom input directory
        val customDir = tempDir.resolve("custom-schemes").createDirectories()
        task.inputDir.set(customDir.toFile())

        // Verify configuration
        task.inputDir.get().asFile.toPath() shouldBe customDir
    }

    @Test
    fun `task output file can be configured`(@TempDir tempDir: Path) {
        val project: Project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .build()

        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Configure custom output file
        val customOutput = tempDir.resolve("custom-report.txt").toFile()
        task.validationReport.set(customOutput)

        // Verify configuration
        task.validationReport.get().asFile shouldBe customOutput
    }

    @Test
    fun `task handles empty input directory gracefully`(@TempDir tempDir: Path) {
        val project: Project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .build()

        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Create empty input directory
        val emptyDir = tempDir.resolve("empty-schemes").createDirectories()
        task.inputDir.set(emptyDir.toFile())

        val reportFile = tempDir.resolve("report.txt").toFile()
        task.validationReport.set(reportFile)

        // Execute task - should not throw
        task.run()

        // Verify report was generated
        reportFile.exists() shouldBe true
        val reportContent = reportFile.readText()
        reportContent shouldContain "Windows Terminal Color Schemes - Validation Report"
        reportContent shouldContain "Total files processed:    0"
    }

    @Test
    fun `task processes valid scheme successfully`(@TempDir tempDir: Path) {
        val project: Project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .build()

        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Create input directory with valid scheme
        val inputDir = tempDir.resolve("schemes").createDirectories()
        val schemeFile = inputDir.resolve("test-scheme.json")
        schemeFile.writeText("""
            {
              "name": "Test Scheme",
              "background": "#282c34",
              "foreground": "#abb2bf",
              "black": "#1e2127",
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
              "brightWhite": "#ffffff",
              "cursorColor": "#528bff",
              "selectionBackground": "#3e4451"
            }
        """.trimIndent())

        task.inputDir.set(inputDir.toFile())
        val reportFile = tempDir.resolve("report.txt").toFile()
        task.validationReport.set(reportFile)

        // Execute task
        task.run()

        // Verify report
        reportFile.exists() shouldBe true
        val reportContent = reportFile.readText()
        reportContent shouldContain "Windows Terminal Color Schemes - Validation Report"
        reportContent shouldContain "Successfully imported:    1"
        reportContent shouldContain "Test Scheme"
        reportContent shouldContain "✓ Test Scheme"
    }

    @Test
    fun `task handles invalid scheme with appropriate error reporting`(@TempDir tempDir: Path) {
        val project: Project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .build()

        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Create input directory with invalid scheme (missing required properties)
        val inputDir = tempDir.resolve("schemes").createDirectories()
        val schemeFile = inputDir.resolve("invalid-scheme.json")
        schemeFile.writeText("""
            {
              "name": "Invalid Scheme",
              "background": "#282c34"
            }
        """.trimIndent())

        task.inputDir.set(inputDir.toFile())
        val reportFile = tempDir.resolve("report.txt").toFile()
        task.validationReport.set(reportFile)

        // Execute task - should not throw
        task.run()

        // Verify report contains error information
        reportFile.exists() shouldBe true
        val reportContent = reportFile.readText()
        reportContent shouldContain "Failed:                   1"
        reportContent shouldContain "✗ invalid-scheme.json"
    }

    @Test
    fun `task handles non-existent input directory`(@TempDir tempDir: Path) {
        val project: Project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .build()

        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Set non-existent directory
        val nonExistentDir = tempDir.resolve("non-existent")
        task.inputDir.set(nonExistentDir.toFile())

        val reportFile = tempDir.resolve("report.txt").toFile()
        task.validationReport.set(reportFile)

        // Execute task - should not throw, but generate error report
        task.run()

        // Verify error report was generated
        reportFile.exists() shouldBe true
        val reportContent = reportFile.readText()
        reportContent shouldContain "FATAL ERROR"
        reportContent shouldContain "Input directory does not exist"
    }

    @Test
    fun `task processes multiple schemes and generates summary`(@TempDir tempDir: Path) {
        val project: Project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .build()

        val task = project.tasks.create("testImport", ImportWindowsTerminalSchemes::class.java)

        // Create input directory with multiple schemes
        val inputDir = tempDir.resolve("schemes").createDirectories()

        // Valid scheme 1
        inputDir.resolve("scheme1.json").writeText("""
            {
              "name": "Scheme One",
              "background": "#000000",
              "foreground": "#ffffff",
              "black": "#000000", "red": "#ff0000", "green": "#00ff00", "yellow": "#ffff00",
              "blue": "#0000ff", "purple": "#ff00ff", "cyan": "#00ffff", "white": "#ffffff",
              "brightBlack": "#808080", "brightRed": "#ff8080", "brightGreen": "#80ff80",
              "brightYellow": "#ffff80", "brightBlue": "#8080ff", "brightPurple": "#ff80ff",
              "brightCyan": "#80ffff", "brightWhite": "#ffffff"
            }
        """.trimIndent())

        // Valid scheme 2
        inputDir.resolve("scheme2.json").writeText("""
            {
              "name": "Scheme Two",
              "background": "#ffffff",
              "foreground": "#000000",
              "black": "#000000", "red": "#ff0000", "green": "#00ff00", "yellow": "#ffff00",
              "blue": "#0000ff", "purple": "#ff00ff", "cyan": "#00ffff", "white": "#ffffff",
              "brightBlack": "#808080", "brightRed": "#ff8080", "brightGreen": "#80ff80",
              "brightYellow": "#ffff80", "brightBlue": "#8080ff", "brightPurple": "#ff80ff",
              "brightCyan": "#80ffff", "brightWhite": "#ffffff"
            }
        """.trimIndent())

        // Invalid scheme
        inputDir.resolve("invalid.json").writeText("""
            {
              "name": "Invalid",
              "background": "invalid-color"
            }
        """.trimIndent())

        task.inputDir.set(inputDir.toFile())
        val reportFile = tempDir.resolve("report.txt").toFile()
        task.validationReport.set(reportFile)

        // Execute task
        task.run()

        // Verify report contains summary
        reportFile.exists() shouldBe true
        val reportContent = reportFile.readText()
        reportContent shouldContain "Total files processed:    3"
        reportContent shouldContain "Successfully imported:    2"
        reportContent shouldContain "Failed:                   1"
        reportContent shouldContain "✓ Scheme One"
        reportContent shouldContain "✓ Scheme Two"
        reportContent shouldContain "✗ invalid.json"
    }
}
