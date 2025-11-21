package colorschemes

import java.nio.file.Path
import java.nio.file.Files
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile

/**
 * Registry for managing Windows Terminal color schemes.
 * Provides functionality to load, validate, and access color schemes from a directory.
 */
class ColorSchemeRegistry(private val schemeDirectory: Path) {
    private val parser = ColorSchemeParser()
    private val validator = SchemaValidator()
    private val schemes = mutableMapOf<String, WindowsTerminalColorScheme>()
    private val errors = mutableMapOf<String, String>()

    init {
        require(Files.exists(schemeDirectory)) {
            "Color scheme directory does not exist: $schemeDirectory"
        }
        require(Files.isDirectory(schemeDirectory)) {
            "Path is not a directory: $schemeDirectory"
        }
    }

    /**
     * Loads all color schemes from the registry directory.
     * @return Number of successfully loaded schemes
     */
    fun loadAll(): Int {
        schemes.clear()
        errors.clear()

        Files.walk(schemeDirectory, 1).use { stream ->
            stream.filter { it.isRegularFile() && it.extension == "json" }
                .forEach { file ->
                    try {
                        val result = parser.parse(file)
                        result.fold(
                            onSuccess = { scheme ->
                                val validationResult = validator.validate(scheme)
                                if (validationResult.errors.isEmpty()) {
                                    schemes[scheme.name] = scheme
                                } else {
                                    errors[file.fileName.toString()] = validationResult.errors.joinToString("; ")
                                }
                            },
                            onFailure = { error ->
                                errors[file.fileName.toString()] = error.message ?: "Unknown error"
                            }
                        )
                    } catch (e: Exception) {
                        errors[file.fileName.toString()] = "Unexpected error: ${e.message}"
                    }
                }
        }

        return schemes.size
    }

    /**
     * Gets a color scheme by name.
     * @param name The name of the color scheme
     * @return The color scheme, or null if not found
     */
    fun getScheme(name: String): WindowsTerminalColorScheme? {
        return schemes[name]
    }

    /**
     * Gets all loaded color schemes.
     * @return Map of scheme names to color schemes
     */
    fun getAllSchemes(): Map<String, WindowsTerminalColorScheme> {
        return schemes.toMap()
    }

    /**
     * Gets all schemes as a list.
     * @return List of all color schemes
     */
    fun getSchemesList(): List<WindowsTerminalColorScheme> {
        return schemes.values.toList()
    }

    /**
     * Gets all loading errors.
     * @return Map of file names to error messages
     */
    fun getErrors(): Map<String, String> {
        return errors.toMap()
    }

    /**
     * Checks if there are any loading errors.
     * @return True if there are errors
     */
    fun hasErrors(): Boolean {
        return errors.isNotEmpty()
    }

    /**
     * Gets a summary of the registry status.
     * @return Summary string
     */
    fun getSummary(): String {
        return """
            Color Scheme Registry Summary
            =============================
            Directory: $schemeDirectory
            Total schemes loaded: ${schemes.size}
            Failed to load: ${errors.size}

            ${if (errors.isNotEmpty()) "Errors:\n" + errors.entries.joinToString("\n") { "  ${it.key}: ${it.value}" } else "No errors"}
        """.trimIndent()
    }

    /**
     * Validates that a minimum number of schemes were loaded.
     * @param minSchemes Minimum number of required schemes
     * @throws IllegalStateException if not enough schemes were loaded
     */
    fun requireMinimumSchemes(minSchemes: Int) {
        require(schemes.size >= minSchemes) {
            "Expected at least $minSchemes color schemes, but only ${schemes.size} were loaded successfully. " +
                "Check errors: ${errors.keys.joinToString(", ")}"
        }
    }

    /**
     * Gets statistics about the loaded schemes.
     */
    data class Statistics(
        val totalSchemes: Int,
        val totalErrors: Int,
        val successRate: Double
    )

    /**
     * Gets loading statistics.
     * @return Statistics object
     */
    fun getStatistics(): Statistics {
        val total = schemes.size + errors.size
        val successRate = if (total > 0) schemes.size.toDouble() / total else 0.0
        return Statistics(
            totalSchemes = schemes.size,
            totalErrors = errors.size,
            successRate = successRate
        )
    }
}
