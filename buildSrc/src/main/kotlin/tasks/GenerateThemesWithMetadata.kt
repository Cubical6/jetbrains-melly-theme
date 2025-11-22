package tasks

import colorschemes.ColorSchemeRegistry
import colorschemes.WindowsTerminalColorScheme
import generators.*
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.nameWithoutExtension

/**
 * Enhanced theme generation task with metadata generation and plugin.xml integration.
 *
 * This task extends the basic theme generation with:
 * - Automatic metadata generation for each theme
 * - Duplicate detection and warnings
 * - plugin.xml automatic updates
 * - Version compatibility checks
 * - Comprehensive logging and reporting
 *
 * Integration example for TASK-503, TASK-503a, and TASK-504.
 *
 * Usage:
 *   ./gradlew generateThemesWithMetadata
 */
open class GenerateThemesWithMetadata : DefaultTask() {

    @get:InputDirectory
    val inputDir: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.projectDirectory.dir("windows-terminal-schemes"))

    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.projectDirectory.dir("src/main/resources/themes"))

    @get:Input
    val generateVariants: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

    @get:Input
    val updatePluginXml: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(true)

    @get:Input
    val authorName: Property<String> = project.objects.property(String::class.java)
        .convention("Windows Terminal Converter")

    init {
        group = "themes"
        description = "Generate IntelliJ themes from Windows Terminal schemes with metadata and plugin.xml integration"
    }

    @TaskAction
    fun run() {
        val inputDirectory = inputDir.get().asFile.toPath()
        val outputDirectory = outputDir.get().asFile.toPath()
        val shouldGenerateVariants = generateVariants.get()
        val shouldUpdatePluginXml = updatePluginXml.get()
        val author = authorName.get()

        printHeader(inputDirectory, outputDirectory, shouldGenerateVariants, shouldUpdatePluginXml)

        // Validate input directory
        if (!Files.exists(inputDirectory)) {
            logger.error("✗ Input directory does not exist: $inputDirectory")
            return
        }

        // Ensure output directory exists
        Files.createDirectories(outputDirectory)

        // Initialize generators
        val metadataGenerator = ThemeMetadataGenerator(
            generatorVersion = project.version.toString(),
            defaultAuthor = author,
            intellijVersion = "2020.3+"
        )
        val consoleColorMapper = ConsoleColorMapper(ColorMappingConfig)
        val xmlGenerator = XMLColorSchemeGenerator()
        val uiThemeGenerator = UIThemeGenerator()

        // Load color schemes
        logger.lifecycle("Loading color schemes from: $inputDirectory")
        logger.lifecycle("")

        val registry = ColorSchemeRegistry(inputDirectory)
        val loadedCount = registry.loadAll()
        val schemes = registry.getSchemesList()

        if (schemes.isEmpty()) {
            logger.warn("⚠ No valid color schemes found")
            return
        }

        logger.lifecycle("Loaded $loadedCount color schemes")

        // Detect duplicates
        logger.lifecycle("")
        logger.lifecycle("Checking for duplicates...")
        val duplicates = metadataGenerator.detectDuplicates(schemes)
        if (duplicates.isNotEmpty()) {
            logger.warn("⚠ Found ${duplicates.size} duplicate color scheme(s):")
            duplicates.forEach { (name1, name2) ->
                logger.warn("  - '$name1' and '$name2' have identical colors")
            }
            logger.lifecycle("")
        } else {
            logger.lifecycle("✓ No duplicates found")
            logger.lifecycle("")
        }

        // Generate themes
        logger.lifecycle("Generating themes...")
        logger.lifecycle("-".repeat(70))

        var successCount = 0
        var failureCount = 0
        val allMetadata = mutableListOf<ThemeMetadata>()
        val failedSchemes = mutableListOf<String>()

        schemes.forEach { scheme ->
            try {
                // Generate metadata first
                val metadata = metadataGenerator.generateMetadata(scheme, author = author)

                // Validate metadata
                val validationErrors = metadataGenerator.validateMetadata(metadata)
                if (validationErrors.isNotEmpty()) {
                    throw IllegalArgumentException("Metadata validation failed: ${validationErrors.joinToString(", ")}")
                }

                // Generate theme files using metadata.id as filename
                generateThemeForScheme(
                    scheme = scheme,
                    metadata = metadata,
                    outputDirectory = outputDirectory,
                    consoleColorMapper = consoleColorMapper,
                    xmlGenerator = xmlGenerator,
                    uiThemeGenerator = uiThemeGenerator,
                    generateVariants = shouldGenerateVariants
                )

                allMetadata.add(metadata)
                logger.lifecycle("  ✓ ${metadata.displayName} (${if (metadata.isDark) "Dark" else "Light"})")
                logger.debug("    ID: ${metadata.id}")
                logger.debug("    Fingerprint: ${metadata.fingerprint}")
                successCount++

            } catch (e: Exception) {
                logger.lifecycle("  ✗ ${scheme.name}")
                logger.error("    Error: ${e.message}")
                failureCount++
                failedSchemes.add(scheme.name)
            }
        }

        logger.lifecycle("-".repeat(70))

        // Update plugin.xml with theme providers and bundled color schemes
        if (shouldUpdatePluginXml && allMetadata.isNotEmpty()) {
            logger.lifecycle("")
            logger.lifecycle("Updating plugin.xml...")
            logger.lifecycle("  Registering themeProvider entries (UI themes)")
            logger.lifecycle("  Registering bundledColorScheme entries (editor color schemes)")

            try {
                val pluginXmlPath = project.file("src/main/resources/META-INF/plugin.xml").toPath()
                val xmlUpdater = PluginXmlUpdater(pluginXmlPath)

                // This call registers BOTH themeProvider and bundledColorScheme for each theme
                val result = xmlUpdater.updatePluginXml(allMetadata, themesDir = "/themes")

                if (result.success) {
                    logger.lifecycle("✓ plugin.xml updated successfully")
                    logger.lifecycle("  Themes added: ${result.themesAdded}")
                    logger.lifecycle("  - ${result.themesAdded} themeProvider entries (UI themes)")
                    logger.lifecycle("  - ${allMetadata.size} bundledColorScheme entries (editor color schemes)")
                    logger.lifecycle("  Themes removed: ${result.themesRemoved}")
                    logger.lifecycle("  Backup created: ${result.backupPath}")
                } else {
                    logger.error("✗ Failed to update plugin.xml: ${result.error}")
                }
            } catch (e: Exception) {
                logger.error("✗ Failed to update plugin.xml: ${e.message}")
            }
        }

        // Print summary
        printSummary(
            totalSchemes = schemes.size,
            successCount = successCount,
            failureCount = failureCount,
            failedSchemes = failedSchemes,
            duplicateCount = duplicates.size,
            outputDirectory = outputDirectory
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateThemeForScheme(
        scheme: WindowsTerminalColorScheme,
        metadata: ThemeMetadata,
        outputDirectory: Path,
        consoleColorMapper: ConsoleColorMapper,
        xmlGenerator: XMLColorSchemeGenerator,
        uiThemeGenerator: UIThemeGenerator,
        generateVariants: Boolean
    ) {
        // Validate scheme
        val validationErrors = scheme.validate()
        if (validationErrors.isNotEmpty()) {
            throw IllegalArgumentException("Invalid color scheme: ${validationErrors.joinToString(", ")}")
        }

        // Use metadata.id as base filename for consistency with plugin.xml
        val baseName = metadata.id

        // Generate XML editor color scheme (shared between variants)
        val xmlOutputPath = outputDirectory.resolve("$baseName.xml")
        xmlGenerator.generate(scheme, xmlOutputPath)

        // Generate UI theme variants (standard + rounded) using metadata.id as base filename
        val uiThemeFiles = generators.UIThemeGenerator.generate(scheme, outputDirectory.toFile(), baseFileName = metadata.id)
        logger.debug("  Generated ${uiThemeFiles.size} UI theme variant(s)")

        // Validate that at least one theme was generated
        if (uiThemeFiles.isEmpty()) {
            throw IllegalStateException("UI theme generation failed: no variants generated")
        }

        // Note: The generateVariants parameter is now deprecated since variants
        // are always generated. Kept for backward compatibility.
    }

    @Suppress("UNUSED_PARAMETER")
    private fun printHeader(
        inputDirectory: Path,
        outputDirectory: Path,
        generateVariants: Boolean,
        updatePluginXml: Boolean
    ) {
        logger.lifecycle("")
        logger.lifecycle("=".repeat(70))
        logger.lifecycle("  Generate IntelliJ Themes with Metadata")
        logger.lifecycle("=".repeat(70))
        logger.lifecycle("")
        logger.lifecycle("Input directory:    $inputDirectory")
        logger.lifecycle("Output directory:   $outputDirectory")
        logger.lifecycle("Theme variants:     Standard + Rounded (always generated)")
        logger.lifecycle("Update plugin.xml:  $updatePluginXml")
        logger.lifecycle("Project version:    ${project.version}")
        logger.lifecycle("")
    }

    private fun printSummary(
        totalSchemes: Int,
        successCount: Int,
        failureCount: Int,
        failedSchemes: List<String>,
        duplicateCount: Int,
        outputDirectory: Path
    ) {
        logger.lifecycle("")
        logger.lifecycle("Summary:")
        logger.lifecycle("  Total schemes processed: $totalSchemes")
        logger.lifecycle("  Successfully generated:  $successCount")
        logger.lifecycle("  Failed:                  $failureCount")
        logger.lifecycle("  Duplicates detected:     $duplicateCount")

        if (successCount > 0) {
            val successRate = (successCount.toDouble() / totalSchemes * 100)
            logger.lifecycle("  Success rate:            ${"%.1f".format(successRate)}%")
            logger.lifecycle("")
            logger.lifecycle("Generated files:")
            logger.lifecycle("  UI theme variants:       ${successCount * 2} (2 per scheme)")
            logger.lifecycle("  Editor schemes:          $successCount")
        }

        logger.lifecycle("")

        if (failureCount > 0) {
            logger.lifecycle("Failed schemes:")
            failedSchemes.forEach { name ->
                logger.lifecycle("  ✗ $name")
            }
            logger.lifecycle("")
        }

        if (successCount > 0) {
            logger.lifecycle("✓ Theme generation complete!")
        }

        logger.lifecycle("")
        logger.lifecycle("Output directory: $outputDirectory")
        logger.lifecycle("=".repeat(70))
        logger.lifecycle("")
    }
}
