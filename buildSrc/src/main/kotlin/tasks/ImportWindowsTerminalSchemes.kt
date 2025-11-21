package tasks

import colorschemes.ColorSchemeRegistry
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Gradle task to import and validate Windows Terminal color schemes.
 *
 * This task:
 * - Loads all .json files from the input directory
 * - Validates each scheme using ColorSchemeRegistry
 * - Generates a detailed validation report
 * - Logs progress to console with visual indicators
 * - Handles errors gracefully without failing the build
 *
 * Usage:
 *   ./gradlew importWindowsTerminalSchemes
 *
 * Configuration:
 *   - Input directory: windows-terminal-schemes/ (default)
 *   - Output report: build/reports/wt-scheme-validation.txt (default)
 *
 * Based on TASK-401 from Sprint 3 specifications.
 */
open class ImportWindowsTerminalSchemes : DefaultTask() {

    /**
     * Input directory containing Windows Terminal JSON color scheme files.
     * Configurable via Gradle, defaults to "windows-terminal-schemes/".
     */
    @get:InputDirectory
    val inputDir: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.projectDirectory.dir("windows-terminal-schemes"))

    /**
     * Output file for the validation report.
     * Configurable via Gradle, defaults to "build/reports/wt-scheme-validation.txt".
     */
    @get:OutputFile
    val validationReport: RegularFileProperty = project.objects.fileProperty()
        .convention(project.layout.buildDirectory.file("reports/wt-scheme-validation.txt"))

    init {
        group = "themes"
        description = "Import and validate Windows Terminal color schemes"
    }

    /**
     * Main task action that imports and validates color schemes.
     */
    @TaskAction
    fun run() {
        val inputDirectory = inputDir.get().asFile.toPath()
        val outputFile = validationReport.get().asFile

        logger.lifecycle("")
        logger.lifecycle("=" .repeat(70))
        logger.lifecycle("  Importing Windows Terminal Color Schemes")
        logger.lifecycle("=" .repeat(70))
        logger.lifecycle("")
        logger.lifecycle("Input directory:  $inputDirectory")
        logger.lifecycle("Validation report: ${outputFile.absolutePath}")
        logger.lifecycle("")

        // Ensure output directory exists
        val reportDir = outputFile.parentFile
        if (!reportDir.exists()) {
            reportDir.mkdirs()
            logger.debug("Created report directory: ${reportDir.absolutePath}")
        }

        // Validate input directory exists
        if (!Files.exists(inputDirectory)) {
            val errorMessage = "Input directory does not exist: $inputDirectory"
            logger.error("✗ $errorMessage")
            writeErrorReport(outputFile, errorMessage)
            return
        }

        if (!Files.isDirectory(inputDirectory)) {
            val errorMessage = "Input path is not a directory: $inputDirectory"
            logger.error("✗ $errorMessage")
            writeErrorReport(outputFile, errorMessage)
            return
        }

        try {
            // Use ColorSchemeRegistry to load and validate all schemes
            logger.lifecycle("Loading color schemes...")
            logger.lifecycle("")

            val registry = ColorSchemeRegistry(inputDirectory)
            val loadedCount = registry.loadAll()

            // Get results
            val schemes = registry.getAllSchemes()
            val errors = registry.getErrors()
            val statistics = registry.getStatistics()

            // Log results to console with visual indicators
            logger.lifecycle("Results:")
            logger.lifecycle("-".repeat(70))

            if (schemes.isNotEmpty()) {
                schemes.values.forEach { scheme ->
                    logger.lifecycle("  ✓ ${scheme.name}")
                }
            }

            if (errors.isNotEmpty()) {
                logger.lifecycle("")
                errors.forEach { (fileName, error) ->
                    logger.lifecycle("  ✗ $fileName")
                    logger.lifecycle("    Error: $error")
                }
            }

            logger.lifecycle("")
            logger.lifecycle("-".repeat(70))
            logger.lifecycle("Summary:")
            logger.lifecycle("  Total files processed: ${statistics.totalSchemes + statistics.totalErrors}")
            logger.lifecycle("  Successfully imported: ${statistics.totalSchemes} schemes")
            logger.lifecycle("  Failed: ${statistics.totalErrors} schemes")
            logger.lifecycle("  Success rate: ${"%.1f".format(statistics.successRate * 100)}%")
            logger.lifecycle("")

            // Generate detailed validation report
            val report = generateValidationReport(
                inputDirectory = inputDirectory,
                schemes = schemes.values.toList(),
                errors = errors,
                statistics = statistics
            )

            // Write report to file
            outputFile.writeText(report)
            logger.lifecycle("Validation report written to: ${outputFile.absolutePath}")
            logger.lifecycle("")

            // Final status message
            if (errors.isEmpty()) {
                logger.lifecycle("✓ All color schemes validated successfully!")
            } else {
                logger.warn("⚠ Some color schemes failed validation. Check the report for details.")
            }

            logger.lifecycle("=" .repeat(70))
            logger.lifecycle("")

        } catch (e: Exception) {
            logger.error("✗ Unexpected error during import: ${e.message}", e)
            writeErrorReport(outputFile, "Unexpected error: ${e.message}\n\n${e.stackTraceToString()}")
            throw e
        }
    }

    /**
     * Generates a detailed validation report.
     */
    private fun generateValidationReport(
        inputDirectory: java.nio.file.Path,
        schemes: List<colorschemes.WindowsTerminalColorScheme>,
        errors: Map<String, String>,
        statistics: ColorSchemeRegistry.Statistics
    ): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        return buildString {
            appendLine("Windows Terminal Color Schemes - Validation Report")
            appendLine("=".repeat(80))
            appendLine()
            appendLine("Generated: $timestamp")
            appendLine("Task: importWindowsTerminalSchemes")
            appendLine("Input Directory: $inputDirectory")
            appendLine()
            appendLine("=".repeat(80))
            appendLine("SUMMARY")
            appendLine("=".repeat(80))
            appendLine()
            appendLine("Total files processed:    ${statistics.totalSchemes + statistics.totalErrors}")
            appendLine("Successfully imported:    ${statistics.totalSchemes}")
            appendLine("Failed:                   ${statistics.totalErrors}")
            appendLine("Success rate:             ${"%.1f".format(statistics.successRate * 100)}%")
            appendLine()

            if (schemes.isNotEmpty()) {
                appendLine("=".repeat(80))
                appendLine("SUCCESSFULLY IMPORTED SCHEMES (${schemes.size})")
                appendLine("=".repeat(80))
                appendLine()

                schemes.sortedBy { it.name }.forEach { scheme ->
                    appendLine("✓ ${scheme.name}")
                    appendLine("  - Background: ${scheme.background}")
                    appendLine("  - Foreground: ${scheme.foreground}")
                    appendLine("  - ANSI Colors: ${scheme.getAllColors().size} colors")
                    appendLine("  - Cursor Color: ${scheme.cursorColor ?: "default (foreground)"}")
                    appendLine("  - Selection: ${scheme.selectionBackground ?: "default (blended)"}")
                    appendLine()
                }
            }

            if (errors.isNotEmpty()) {
                appendLine("=".repeat(80))
                appendLine("FAILED SCHEMES (${errors.size})")
                appendLine("=".repeat(80))
                appendLine()

                errors.entries.sortedBy { it.key }.forEach { (fileName, error) ->
                    appendLine("✗ $fileName")
                    appendLine("  Error: $error")
                    appendLine()
                }
            }

            if (schemes.isEmpty() && errors.isEmpty()) {
                appendLine("=".repeat(80))
                appendLine("NO COLOR SCHEMES FOUND")
                appendLine("=".repeat(80))
                appendLine()
                appendLine("No .json files were found in the input directory.")
                appendLine("Please add Windows Terminal color scheme JSON files to:")
                appendLine("  $inputDirectory")
                appendLine()
            }

            appendLine("=".repeat(80))
            appendLine("END OF REPORT")
            appendLine("=".repeat(80))
        }
    }

    /**
     * Writes an error report when the task encounters a fatal error.
     */
    private fun writeErrorReport(outputFile: java.io.File, errorMessage: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val report = buildString {
            appendLine("Windows Terminal Color Schemes - Validation Report")
            appendLine("=".repeat(80))
            appendLine()
            appendLine("Generated: $timestamp")
            appendLine("Task: importWindowsTerminalSchemes")
            appendLine()
            appendLine("=".repeat(80))
            appendLine("FATAL ERROR")
            appendLine("=".repeat(80))
            appendLine()
            appendLine(errorMessage)
            appendLine()
            appendLine("=".repeat(80))
            appendLine("END OF REPORT")
            appendLine("=".repeat(80))
        }

        outputFile.parentFile?.mkdirs()
        outputFile.writeText(report)
    }
}
