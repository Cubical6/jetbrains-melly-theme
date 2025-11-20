package colorschemes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

/**
 * Parser for Windows Terminal color scheme JSON files.
 *
 * Supports:
 * - Single file parsing
 * - Directory batch parsing
 * - Detailed error messages
 * - Schema validation
 */
class ColorSchemeParser {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Parses a single Windows Terminal color scheme JSON file.
     *
     * @param jsonPath Path to the JSON file
     * @return Result containing either the parsed scheme or an error
     */
    fun parse(jsonPath: Path): Result<WindowsTerminalColorScheme> {
        return try {
            // Check file exists
            if (!Files.exists(jsonPath)) {
                return Result.failure(ParserException("File not found: $jsonPath"))
            }

            if (!jsonPath.isRegularFile()) {
                return Result.failure(ParserException("Not a regular file: $jsonPath"))
            }

            // Read and parse JSON
            val jsonContent = Files.readString(jsonPath)
            if (jsonContent.isBlank()) {
                return Result.failure(ParserException("File is empty: $jsonPath"))
            }

            val scheme = try {
                gson.fromJson(jsonContent, WindowsTerminalColorScheme::class.java)
            } catch (e: JsonSyntaxException) {
                return Result.failure(ParserException("Invalid JSON syntax in ${jsonPath.name}: ${e.message}", e))
            }

            // Validate parsed scheme
            if (scheme == null) {
                return Result.failure(ParserException("Failed to parse JSON (null result): $jsonPath"))
            }

            // Validate required properties
            val validationErrors = validateRequiredProperties(scheme, jsonPath)
            if (validationErrors.isNotEmpty()) {
                return Result.failure(
                    ParserException(
                        "Missing required properties in ${jsonPath.name}:\n${validationErrors.joinToString("\n")}"
                    )
                )
            }

            // Validate color formats
            val colorErrors = scheme.validate()
            if (colorErrors.isNotEmpty()) {
                return Result.failure(
                    ParserException(
                        "Invalid colors in ${jsonPath.name}:\n${colorErrors.joinToString("\n")}"
                    )
                )
            }

            Result.success(scheme)
        } catch (e: Exception) {
            Result.failure(ParserException("Unexpected error parsing ${jsonPath.name}: ${e.message}", e))
        }
    }

    /**
     * Parses all .json files in a directory.
     *
     * @param dirPath Path to the directory containing JSON files
     * @return List of Results for each file (success or failure)
     */
    fun parseDirectory(dirPath: Path): List<Pair<Path, Result<WindowsTerminalColorScheme>>> {
        if (!Files.exists(dirPath)) {
            return emptyList()
        }

        if (!Files.isDirectory(dirPath)) {
            return listOf(dirPath to Result.failure(ParserException("Not a directory: $dirPath")))
        }

        return Files.list(dirPath)
            .filter { it.isRegularFile() && it.extension == "json" }
            .map { jsonFile -> jsonFile to parse(jsonFile) }
            .toList()
    }

    /**
     * Validates that all required properties are present (non-null).
     */
    private fun validateRequiredProperties(
        scheme: WindowsTerminalColorScheme,
        sourcePath: Path
    ): List<String> {
        val errors = mutableListOf<String>()

        // Check required string properties
        if (scheme.name.isBlank()) errors.add("- 'name' is missing or blank")
        if (scheme.background.isBlank()) errors.add("- 'background' is missing or blank")
        if (scheme.foreground.isBlank()) errors.add("- 'foreground' is missing or blank")

        // Check ANSI colors
        if (scheme.black.isBlank()) errors.add("- 'black' is missing or blank")
        if (scheme.red.isBlank()) errors.add("- 'red' is missing or blank")
        if (scheme.green.isBlank()) errors.add("- 'green' is missing or blank")
        if (scheme.yellow.isBlank()) errors.add("- 'yellow' is missing or blank")
        if (scheme.blue.isBlank()) errors.add("- 'blue' is missing or blank")
        if (scheme.purple.isBlank()) errors.add("- 'purple' is missing or blank")
        if (scheme.cyan.isBlank()) errors.add("- 'cyan' is missing or blank")
        if (scheme.white.isBlank()) errors.add("- 'white' is missing or blank")

        if (scheme.brightBlack.isBlank()) errors.add("- 'brightBlack' is missing or blank")
        if (scheme.brightRed.isBlank()) errors.add("- 'brightRed' is missing or blank")
        if (scheme.brightGreen.isBlank()) errors.add("- 'brightGreen' is missing or blank")
        if (scheme.brightYellow.isBlank()) errors.add("- 'brightYellow' is missing or blank")
        if (scheme.brightBlue.isBlank()) errors.add("- 'brightBlue' is missing or blank")
        if (scheme.brightPurple.isBlank()) errors.add("- 'brightPurple' is missing or blank")
        if (scheme.brightCyan.isBlank()) errors.add("- 'brightCyan' is missing or blank")
        if (scheme.brightWhite.isBlank()) errors.add("- 'brightWhite' is missing or blank")

        return errors
    }

    /**
     * Parses multiple color schemes and returns summary statistics.
     */
    fun parseWithSummary(dirPath: Path): ParseSummary {
        val results = parseDirectory(dirPath)
        val successful = results.filter { it.second.isSuccess }
        val failed = results.filter { it.second.isFailure }

        return ParseSummary(
            totalFiles = results.size,
            successCount = successful.size,
            failureCount = failed.size,
            successfulSchemes = successful.mapNotNull { it.second.getOrNull() },
            failures = failed.map { (path, result) ->
                ParseFailure(
                    fileName = path.name,
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        )
    }
}

/**
 * Summary of parsing results for a batch of files.
 */
data class ParseSummary(
    val totalFiles: Int,
    val successCount: Int,
    val failureCount: Int,
    val successfulSchemes: List<WindowsTerminalColorScheme>,
    val failures: List<ParseFailure>
) {
    /**
     * Generates a human-readable report.
     */
    fun toReport(): String {
        return buildString {
            appendLine("Windows Terminal Color Scheme Parsing Summary")
            appendLine("=".repeat(50))
            appendLine("Total files:      $totalFiles")
            appendLine("Successful:       $successCount")
            appendLine("Failed:           $failureCount")
            appendLine()

            if (successfulSchemes.isNotEmpty()) {
                appendLine("Successfully parsed schemes:")
                successfulSchemes.forEach { scheme ->
                    appendLine("  ✓ ${scheme.name}")
                }
                appendLine()
            }

            if (failures.isNotEmpty()) {
                appendLine("Failed to parse:")
                failures.forEach { failure ->
                    appendLine("  ✗ ${failure.fileName}")
                    appendLine("    Error: ${failure.error}")
                }
            }
        }
    }
}

/**
 * Represents a parsing failure for a specific file.
 */
data class ParseFailure(
    val fileName: String,
    val error: String
)

/**
 * Custom exception for parser errors.
 */
class ParserException(message: String, cause: Throwable? = null) : Exception(message, cause)
