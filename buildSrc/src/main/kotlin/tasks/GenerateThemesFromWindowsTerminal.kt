package tasks

import colorschemes.ColorSchemeRegistry
import colorschemes.WindowsTerminalColorScheme
import generators.UIThemeGenerator
import generators.XMLColorSchemeGenerator
import mapping.ColorMappingConfig
import mapping.ConsoleColorMapper
import mapping.SyntaxColorInference
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.nameWithoutExtension
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Gradle task to generate IntelliJ themes from Windows Terminal color schemes.
 *
 * This task:
 * - Loads all Windows Terminal color schemes from the input directory
 * - For each scheme, generates both .xml (editor color scheme) and .theme.json (UI theme)
 * - Uses ColorSchemeRegistry for loading and validation
 * - Uses ConsoleColorMapper and SyntaxColorInference for color mapping
 * - Uses XMLColorSchemeGenerator and UIThemeGenerator for file generation
 * - Handles errors gracefully, continuing generation even if single schemes fail
 * - Creates .failed marker files for debugging failed schemes
 * - Logs progress with visual indicators (✓ for success, ✗ for failure)
 * - Generates a detailed summary report
 *
 * Usage:
 *   ./gradlew generateThemesFromWindowsTerminal
 *
 * Configuration:
 *   - Input directory: windows-terminal-schemes/ (default)
 *   - Output directory: src/main/resources/themes/ (default)
 *   - Generate variants: false (default)
 *
 * Based on TASK-402 from Sprint 3 specifications.
 */
open class GenerateThemesFromWindowsTerminal : DefaultTask() {

    /**
     * Input directory containing Windows Terminal JSON color scheme files.
     * Configurable via Gradle, defaults to "windows-terminal-schemes/".
     */
    @get:InputDirectory
    val inputDir: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.projectDirectory.dir("windows-terminal-schemes"))

    /**
     * Output directory for generated IntelliJ theme files (.xml and .theme.json).
     * Configurable via Gradle, defaults to "src/main/resources/themes/".
     */
    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.projectDirectory.dir("src/main/resources/themes"))

    /**
     * Whether to generate theme variants (e.g., italic versions).
     * Configurable via Gradle, defaults to false.
     */
    @get:Input
    val generateVariants: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

    init {
        group = "themes"
        description = "Generate IntelliJ themes from Windows Terminal schemes"
    }

    /**
     * Main task action that generates themes from Windows Terminal color schemes.
     */
    @TaskAction
    fun run() {
        val inputDirectory = inputDir.get().asFile.toPath()
        val outputDirectory = outputDir.get().asFile.toPath()
        val shouldGenerateVariants = generateVariants.get()

        printHeader(inputDirectory, outputDirectory, shouldGenerateVariants)

        // Validate input directory
        if (!Files.exists(inputDirectory)) {
            logger.error("✗ Input directory does not exist: $inputDirectory")
            logger.error("  Please create the directory and add Windows Terminal color scheme JSON files.")
            return
        }

        if (!Files.isDirectory(inputDirectory)) {
            logger.error("✗ Input path is not a directory: $inputDirectory")
            return
        }

        // Ensure output directory exists
        try {
            Files.createDirectories(outputDirectory)
            logger.debug("Output directory ready: $outputDirectory")
        } catch (e: Exception) {
            logger.error("✗ Failed to create output directory: ${e.message}")
            return
        }

        // Initialize generators and mappers
        val consoleColorMapper = ConsoleColorMapper(ColorMappingConfig)
        val xmlGenerator = XMLColorSchemeGenerator()
        val uiThemeGenerator = UIThemeGenerator()

        // Load color schemes using ColorSchemeRegistry
        logger.lifecycle("Loading color schemes from: $inputDirectory")
        logger.lifecycle("")

        val registry = try {
            ColorSchemeRegistry(inputDirectory)
        } catch (e: Exception) {
            logger.error("✗ Failed to initialize ColorSchemeRegistry: ${e.message}", e)
            return
        }

        val loadedCount = registry.loadAll()
        val schemes = registry.getSchemesList()
        val loadErrors = registry.getErrors()

        if (loadErrors.isNotEmpty()) {
            logger.warn("⚠ ${loadErrors.size} schemes failed to load:")
            loadErrors.forEach { (fileName, error) ->
                logger.warn("  ✗ $fileName: $error")
            }
            logger.lifecycle("")
        }

        if (schemes.isEmpty()) {
            logger.warn("⚠ No valid color schemes found in: $inputDirectory")
            logger.warn("  Please add Windows Terminal JSON files to this directory.")
            return
        }

        logger.lifecycle("Loaded $loadedCount color schemes")
        logger.lifecycle("")
        logger.lifecycle("Generating themes...")
        logger.lifecycle("-".repeat(70))

        // Generation tracking (thread-safe for parallel processing)
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)
        val failedSchemes = ConcurrentHashMap<String, FailureRecord>()

        // Measure generation time
        val startTime = System.currentTimeMillis()

        // Generate themes in parallel using coroutines
        runBlocking {
            val parallelism = Runtime.getRuntime().availableProcessors()
            logger.lifecycle("Using parallel generation with $parallelism threads")
            logger.lifecycle("")

            schemes.map { scheme ->
                async(Dispatchers.Default) {
                    try {
                        generateThemeForScheme(
                            scheme = scheme,
                            outputDirectory = outputDirectory,
                            consoleColorMapper = consoleColorMapper,
                            xmlGenerator = xmlGenerator,
                            uiThemeGenerator = uiThemeGenerator,
                            generateVariants = shouldGenerateVariants
                        )

                        synchronized(logger) {
                            logger.lifecycle("  ✓ ${scheme.name}")
                        }
                        successCount.incrementAndGet()

                    } catch (e: Exception) {
                        synchronized(logger) {
                            logger.lifecycle("  ✗ ${scheme.name}")
                            logger.error("    Error: ${e.message}")
                        }
                        failureCount.incrementAndGet()

                        // Record failure for summary
                        failedSchemes[scheme.name] = FailureRecord(
                            schemeName = scheme.name,
                            errorMessage = e.message ?: "Unknown error",
                            stackTrace = e.stackTraceToString()
                        )

                        // Create .failed marker file for debugging
                        createFailedMarkerFile(
                            outputDirectory = outputDirectory,
                            scheme = scheme,
                            error = e
                        )
                    }
                }
            }.awaitAll()
        }

        val endTime = System.currentTimeMillis()
        val durationSeconds = (endTime - startTime) / 1000.0

        // Print summary
        printSummary(
            totalSchemes = schemes.size,
            successCount = successCount.get(),
            failureCount = failureCount.get(),
            failedSchemes = failedSchemes.values.toList(),
            outputDirectory = outputDirectory,
            durationSeconds = durationSeconds
        )
    }

    /**
     * Generates theme files (XML and JSON) for a single color scheme.
     */
    private fun generateThemeForScheme(
        scheme: WindowsTerminalColorScheme,
        outputDirectory: Path,
        consoleColorMapper: ConsoleColorMapper,
        xmlGenerator: XMLColorSchemeGenerator,
        uiThemeGenerator: UIThemeGenerator,
        generateVariants: Boolean
    ) {
        // Validate scheme
        val validationErrors = scheme.validate()
        if (validationErrors.isNotEmpty()) {
            throw IllegalArgumentException(
                "Invalid color scheme: ${validationErrors.joinToString(", ")}"
            )
        }

        // Generate sanitized file name
        val baseName = sanitizeFileName(scheme.name)

        // Map console colors
        val consoleColors = consoleColorMapper.mapToConsoleColors(scheme)
        logger.debug("  Mapped ${consoleColors.size} console colors for ${scheme.name}")

        // Infer syntax colors
        val syntaxColors = SyntaxColorInference.inferSyntaxColors(scheme)
        logger.debug("  Inferred ${syntaxColors.size} syntax colors for ${scheme.name}")

        // Generate UI theme variants (standard + rounded)
        val uiThemeFiles = generators.UIThemeGenerator.generate(scheme, outputDirectory.toFile())
        logger.debug("  Generated ${uiThemeFiles.size} UI theme variant(s)")

        // Generate XML editor color scheme (shared between variants)
        val xmlOutputPath = outputDirectory.resolve("$baseName.xml")
        xmlGenerator.generate(scheme, xmlOutputPath)
        logger.debug("  Generated editor scheme: ${xmlOutputPath.fileName}")

        // Note: The generateVariants parameter is now deprecated since variants
        // are always generated. The old variant generation method is kept for
        // backward compatibility but not called.
    }

    /**
     * Generates theme variants (e.g., italic version).
     * Note: Full variant generation will be implemented in TASK-505.
     */
    private fun generateThemeVariants(
        scheme: WindowsTerminalColorScheme,
        baseName: String,
        outputDirectory: Path,
        xmlGenerator: XMLColorSchemeGenerator,
        uiThemeGenerator: UIThemeGenerator
    ) {
        logger.debug("  Generating variants for ${scheme.name}")
        // TODO: Implement variant generation in TASK-505
        // For now, this is a placeholder that logs the request
        logger.debug("  Variant generation not yet implemented (see TASK-505)")
    }

    /**
     * Creates a .failed marker file for a scheme that failed to generate.
     * Contains error details and timestamp for debugging.
     */
    private fun createFailedMarkerFile(
        outputDirectory: Path,
        scheme: WindowsTerminalColorScheme,
        error: Exception
    ) {
        try {
            val baseName = sanitizeFileName(scheme.name)
            val markerPath = outputDirectory.resolve("$baseName.failed")
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val content = buildString {
                appendLine("Theme Generation Failed")
                appendLine("=".repeat(80))
                appendLine()
                appendLine("Scheme Name: ${scheme.name}")
                appendLine("Timestamp: $timestamp")
                appendLine("Task: generateThemesFromWindowsTerminal")
                appendLine()
                appendLine("=".repeat(80))
                appendLine("ERROR")
                appendLine("=".repeat(80))
                appendLine()
                appendLine("${error::class.simpleName}: ${error.message}")
                appendLine()
                appendLine("=".repeat(80))
                appendLine("STACK TRACE")
                appendLine("=".repeat(80))
                appendLine()
                appendLine(error.stackTraceToString())
                appendLine()
                appendLine("=".repeat(80))
                appendLine("COLOR SCHEME DATA")
                appendLine("=".repeat(80))
                appendLine()
                appendLine("Background: ${scheme.background}")
                appendLine("Foreground: ${scheme.foreground}")
                appendLine("Cursor: ${scheme.cursorColor ?: "default"}")
                appendLine("Selection: ${scheme.selectionBackground ?: "default"}")
                appendLine()
                appendLine("ANSI Colors:")
                appendLine("  Black: ${scheme.black}")
                appendLine("  Red: ${scheme.red}")
                appendLine("  Green: ${scheme.green}")
                appendLine("  Yellow: ${scheme.yellow}")
                appendLine("  Blue: ${scheme.blue}")
                appendLine("  Purple: ${scheme.purple}")
                appendLine("  Cyan: ${scheme.cyan}")
                appendLine("  White: ${scheme.white}")
                appendLine()
                appendLine("ANSI Bright Colors:")
                appendLine("  Bright Black: ${scheme.brightBlack}")
                appendLine("  Bright Red: ${scheme.brightRed}")
                appendLine("  Bright Green: ${scheme.brightGreen}")
                appendLine("  Bright Yellow: ${scheme.brightYellow}")
                appendLine("  Bright Blue: ${scheme.brightBlue}")
                appendLine("  Bright Purple: ${scheme.brightPurple}")
                appendLine("  Bright Cyan: ${scheme.brightCyan}")
                appendLine("  Bright White: ${scheme.brightWhite}")
                appendLine()
                appendLine("=".repeat(80))
            }

            Files.writeString(
                markerPath,
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )

            logger.debug("  Created failure marker: $markerPath")

        } catch (e: Exception) {
            logger.warn("  Failed to create .failed marker file: ${e.message}")
        }
    }

    /**
     * Sanitizes a scheme name for use as a file name.
     */
    private fun sanitizeFileName(name: String): String {
        return name
            .trim()
            .replace(Regex("[^a-zA-Z0-9\\s-]"), "")  // Remove special chars
            .replace(Regex("\\s+"), "_")              // Spaces to underscores
            .replace(Regex("_+"), "_")                // Multiple underscores to single
            .lowercase()
    }

    /**
     * Prints the task header with configuration details.
     */
    private fun printHeader(
        inputDirectory: Path,
        outputDirectory: Path,
        generateVariants: Boolean
    ) {
        logger.lifecycle("")
        logger.lifecycle("=".repeat(70))
        logger.lifecycle("  Generate IntelliJ Themes from Windows Terminal")
        logger.lifecycle("=".repeat(70))
        logger.lifecycle("")
        logger.lifecycle("Input directory:    $inputDirectory")
        logger.lifecycle("Output directory:   $outputDirectory")
        logger.lifecycle("Generate variants:  $generateVariants")
        logger.lifecycle("")
    }

    /**
     * Prints the generation summary with statistics.
     */
    private fun printSummary(
        totalSchemes: Int,
        successCount: Int,
        failureCount: Int,
        failedSchemes: List<FailureRecord>,
        outputDirectory: Path,
        durationSeconds: Double
    ) {
        logger.lifecycle("-".repeat(70))
        logger.lifecycle("")
        logger.lifecycle("Summary:")
        logger.lifecycle("  Total schemes processed: $totalSchemes")
        logger.lifecycle("  Successfully generated:  $successCount")
        logger.lifecycle("  Failed:                  $failureCount")

        if (successCount > 0) {
            val successRate = (successCount.toDouble() / totalSchemes * 100)
            logger.lifecycle("  Success rate:            ${"%.1f".format(successRate)}%")
            logger.lifecycle("")
            logger.lifecycle("Generated files:")
            logger.lifecycle("  UI theme variants:       ${successCount * 2} (2 per scheme)")
            logger.lifecycle("  Editor schemes:          $successCount")
        }

        logger.lifecycle("")
        logger.lifecycle("Performance:")
        logger.lifecycle("  Total time:              ${"%.2f".format(durationSeconds)} seconds")
        if (successCount > 0) {
            val avgTime = durationSeconds / successCount
            logger.lifecycle("  Average per scheme:      ${"%.2f".format(avgTime)} seconds")
            val throughput = successCount / durationSeconds
            logger.lifecycle("  Throughput:              ${"%.1f".format(throughput)} schemes/second")
        }

        logger.lifecycle("")

        if (failureCount > 0) {
            logger.lifecycle("Failed schemes:")
            failedSchemes.forEach { record ->
                logger.lifecycle("  ✗ ${record.schemeName}")
                logger.lifecycle("    ${record.errorMessage}")
            }
            logger.lifecycle("")
            logger.warn("⚠ Some themes failed to generate.")
            logger.warn("  Check .failed files in output directory for detailed error information:")
            logger.warn("  $outputDirectory")
            logger.lifecycle("")
        } else {
            logger.lifecycle("✓ All themes generated successfully!")
            logger.lifecycle("")
        }

        logger.lifecycle("Output directory: $outputDirectory")
        logger.lifecycle("=".repeat(70))
        logger.lifecycle("")
    }

    /**
     * Data class to track failed scheme generation attempts.
     */
    private data class FailureRecord(
        val schemeName: String,
        val errorMessage: String,
        val stackTrace: String
    )
}
