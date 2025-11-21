package generators

import colorschemes.WindowsTerminalColorScheme
import utils.ColorUtils
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Generates theme metadata for IntelliJ themes derived from Windows Terminal color schemes.
 *
 * This generator provides:
 * - Unique, deterministic theme IDs (hash-based)
 * - Sanitized, formatted theme names
 * - Comprehensive metadata for theme management
 * - Version compatibility checks
 * - Theme fingerprinting for duplicate detection
 *
 * Based on TASK-503 and TASK-503a from Sprint 4 specifications.
 *
 * @property generatorVersion Version of the theme generator (defaults to "1.0.0")
 * @property defaultAuthor Default author name for themes (defaults to "Windows Terminal Converter")
 * @property intellijVersion Target IntelliJ version compatibility (defaults to "2020.3+")
 */
class ThemeMetadataGenerator(
    private val generatorVersion: String = "1.0.0",
    private val defaultAuthor: String = "Windows Terminal Converter",
    private val intellijVersion: String = "2020.3+"
) {

    companion object {
        /**
         * Minimum supported IntelliJ version (build number)
         */
        const val MIN_INTELLIJ_VERSION = "203.7148.57"

        /**
         * Maximum supported IntelliJ version (null = no maximum)
         */
        const val MAX_INTELLIJ_VERSION: String? = null

        /**
         * Default theme version for newly generated themes
         */
        const val DEFAULT_THEME_VERSION = "1.0.0"

        /**
         * Luminance threshold for determining dark vs light themes
         */
        const val DARK_THEME_THRESHOLD = 100.0

        /**
         * Valid characters for theme IDs (used for sanitization)
         */
        private val ID_VALID_CHARS_REGEX = Regex("[^a-zA-Z0-9-]")

        /**
         * Valid characters for theme names (used for sanitization)
         */
        private val NAME_VALID_CHARS_REGEX = Regex("[^a-zA-Z0-9\\s-]")
    }

    /**
     * Generates a unique, deterministic theme ID based on the color scheme.
     *
     * The ID is generated using SHA-256 hash of:
     * - Scheme name (normalized)
     * - All color values (18 colors)
     *
     * This ensures:
     * - Same scheme always generates same ID (deterministic)
     * - Different schemes (even with similar names) get different IDs
     * - IDs are compatible with IntelliJ's themeProvider format
     *
     * Format: "wt-{scheme-name-prefix}-{hash-suffix}"
     * Example: "wt-one-dark-a7f3e2b1"
     *
     * @param scheme The Windows Terminal color scheme
     * @return Unique theme ID suitable for plugin.xml
     */
    fun generateThemeId(scheme: WindowsTerminalColorScheme): String {
        // Create fingerprint for hashing
        val fingerprint = buildString {
            append(scheme.name.lowercase().trim())
            append("|")
            append(scheme.background)
            append(scheme.foreground)
            append(scheme.black)
            append(scheme.red)
            append(scheme.green)
            append(scheme.yellow)
            append(scheme.blue)
            append(scheme.purple)
            append(scheme.cyan)
            append(scheme.white)
            append(scheme.brightBlack)
            append(scheme.brightRed)
            append(scheme.brightGreen)
            append(scheme.brightYellow)
            append(scheme.brightBlue)
            append(scheme.brightPurple)
            append(scheme.brightCyan)
            append(scheme.brightWhite)
        }

        // Generate SHA-256 hash
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(fingerprint.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(8) // Take first 8 characters for brevity

        // Create sanitized name prefix
        val namePrefix = scheme.name
            .lowercase()
            .trim()
            .replace(ID_VALID_CHARS_REGEX, "-")
            .replace(Regex("-+"), "-") // Multiple dashes to single
            .removePrefix("-")
            .removeSuffix("-")
            .take(20) // Limit length

        return "wt-$namePrefix-$hash"
    }

    /**
     * Generates a human-readable theme name from the scheme name.
     *
     * Performs the following transformations:
     * - Removes special characters (except spaces and hyphens)
     * - Capitalizes words properly (Title Case)
     * - Removes excessive whitespace
     * - Adds "WT" prefix to indicate Windows Terminal origin
     *
     * Examples:
     * - "one-dark" -> "WT One Dark"
     * - "Solarized_Light" -> "WT Solarized Light"
     * - "Gruvbox-Dark-Hard" -> "WT Gruvbox Dark Hard"
     *
     * @param scheme The Windows Terminal color scheme
     * @return Formatted theme name
     */
    fun generateThemeName(scheme: WindowsTerminalColorScheme): String {
        val sanitized = scheme.name
            .trim()
            .replace(NAME_VALID_CHARS_REGEX, " ") // Remove invalid chars
            .replace("_", " ") // Underscores to spaces
            .replace("-", " ") // Hyphens to spaces
            .replace(Regex("\\s+"), " ") // Multiple spaces to single
            .trim()

        // Capitalize each word (Title Case)
        val titleCase = sanitized.split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

        return "WT $titleCase"
    }

    /**
     * Generates display name for the theme (same as theme name but without WT prefix).
     *
     * @param scheme The Windows Terminal color scheme
     * @return Display name for UI
     */
    fun generateDisplayName(scheme: WindowsTerminalColorScheme): String {
        return generateThemeName(scheme).removePrefix("WT ").trim()
    }

    /**
     * Determines if a color scheme represents a dark theme.
     *
     * Uses background color luminance to determine dark vs light.
     * Threshold: luminance < 100 = dark theme
     *
     * @param scheme The Windows Terminal color scheme
     * @return True if dark theme, false if light theme
     */
    fun isDarkTheme(scheme: WindowsTerminalColorScheme): Boolean {
        val luminance = ColorUtils.calculateLuminance(scheme.background)
        return luminance < DARK_THEME_THRESHOLD
    }

    /**
     * Generates complete theme metadata for a color scheme.
     *
     * Includes:
     * - Unique ID
     * - Theme name and display name
     * - Author attribution
     * - Creation timestamp
     * - Source scheme reference
     * - Generator version
     * - IntelliJ version compatibility
     * - Dark/light classification
     * - Semantic versioning
     * - Fingerprint for duplicate detection
     *
     * @param scheme The Windows Terminal color scheme
     * @param author Optional author override (defaults to defaultAuthor)
     * @return Complete theme metadata
     */
    fun generateMetadata(
        scheme: WindowsTerminalColorScheme,
        author: String? = null
    ): ThemeMetadata {
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

        return ThemeMetadata(
            id = generateThemeId(scheme),
            name = generateThemeName(scheme),
            displayName = generateDisplayName(scheme),
            author = author ?: defaultAuthor,
            createdDate = timestamp,
            sourceScheme = scheme.name,
            generatorVersion = generatorVersion,
            intellijVersion = intellijVersion,
            isDark = isDarkTheme(scheme),
            themeVersion = DEFAULT_THEME_VERSION,
            minIntellijVersion = MIN_INTELLIJ_VERSION,
            maxIntellijVersion = MAX_INTELLIJ_VERSION,
            fingerprint = generateFingerprint(scheme)
        )
    }

    /**
     * Generates a fingerprint for a color scheme based on its colors.
     *
     * The fingerprint is a hash of all color values (excluding name).
     * This allows detection of duplicate schemes with different names.
     *
     * Uses MD5 hash for performance (security not required for fingerprinting).
     *
     * @param scheme The Windows Terminal color scheme
     * @return 32-character fingerprint (MD5 hash)
     */
    fun generateFingerprint(scheme: WindowsTerminalColorScheme): String {
        val colorData = buildString {
            append(scheme.background)
            append(scheme.foreground)
            append(scheme.black)
            append(scheme.red)
            append(scheme.green)
            append(scheme.yellow)
            append(scheme.blue)
            append(scheme.purple)
            append(scheme.cyan)
            append(scheme.white)
            append(scheme.brightBlack)
            append(scheme.brightRed)
            append(scheme.brightGreen)
            append(scheme.brightYellow)
            append(scheme.brightBlue)
            append(scheme.brightPurple)
            append(scheme.brightCyan)
            append(scheme.brightWhite)
        }

        return MessageDigest.getInstance("MD5")
            .digest(colorData.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * Detects duplicate color schemes based on fingerprints.
     *
     * Returns pairs of scheme names that have identical color values
     * but different names (potential duplicates).
     *
     * @param schemes List of color schemes to analyze
     * @return List of pairs (scheme1 name, scheme2 name) that are duplicates
     */
    fun detectDuplicates(schemes: List<WindowsTerminalColorScheme>): List<Pair<String, String>> {
        val duplicates = mutableListOf<Pair<String, String>>()
        val fingerprintMap = mutableMapOf<String, MutableList<String>>()

        // Build fingerprint to scheme names mapping
        schemes.forEach { scheme ->
            val fingerprint = generateFingerprint(scheme)
            fingerprintMap.getOrPut(fingerprint) { mutableListOf() }.add(scheme.name)
        }

        // Find duplicates (fingerprints with multiple schemes)
        fingerprintMap.values.forEach { schemeNames ->
            if (schemeNames.size > 1) {
                // Add all pairs of duplicates
                for (i in 0 until schemeNames.size - 1) {
                    for (j in i + 1 until schemeNames.size) {
                        duplicates.add(Pair(schemeNames[i], schemeNames[j]))
                    }
                }
            }
        }

        return duplicates
    }

    /**
     * Checks if a theme is compatible with a specific IntelliJ version.
     *
     * Version format: "YEAR.MAJOR.PATCH" or build number "YYY.XXXXX.XX"
     *
     * @param intellijVersion IntelliJ version string (e.g., "2023.1" or "231.8109.175")
     * @return True if compatible, false otherwise
     */
    fun checkCompatibility(intellijVersion: String): Boolean {
        // Parse version as build number (e.g., "203.7148.57")
        val buildNumber = when {
            intellijVersion.contains(".") && intellijVersion.split(".").size == 3 -> {
                // Already in build number format
                intellijVersion
            }
            intellijVersion.matches(Regex("\\d{4}\\.\\d+")) -> {
                // Convert year.major format to build number (approximate)
                val parts = intellijVersion.split(".")
                val year = parts[0].toInt()
                val major = parts[1].toInt()
                val buildPrefix = (year - 2000) * 10 + major
                "$buildPrefix.0.0"
            }
            else -> return false
        }

        // Compare with min version
        if (compareVersions(buildNumber, MIN_INTELLIJ_VERSION) < 0) {
            return false
        }

        // Compare with max version (if specified)
        MAX_INTELLIJ_VERSION?.let { maxVersion ->
            if (compareVersions(buildNumber, maxVersion) > 0) {
                return false
            }
        }

        return true
    }

    /**
     * Compares two version strings.
     *
     * @return Negative if v1 < v2, zero if v1 == v2, positive if v1 > v2
     */
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val p1 = parts1.getOrNull(i) ?: 0
            val p2 = parts2.getOrNull(i) ?: 0
            if (p1 != p2) {
                return p1 - p2
            }
        }
        return 0
    }

    /**
     * Validates that a metadata object has valid values.
     *
     * @param metadata Theme metadata to validate
     * @return List of validation errors (empty if valid)
     */
    fun validateMetadata(metadata: ThemeMetadata): List<String> {
        val errors = mutableListOf<String>()

        if (metadata.id.isBlank()) {
            errors.add("Theme ID cannot be blank")
        }

        if (metadata.name.isBlank()) {
            errors.add("Theme name cannot be blank")
        }

        if (metadata.author.isBlank()) {
            errors.add("Author cannot be blank")
        }

        if (!metadata.themeVersion.matches(Regex("\\d+\\.\\d+\\.\\d+"))) {
            errors.add("Theme version must be in semantic versioning format (e.g., 1.0.0)")
        }

        if (!metadata.minIntellijVersion.matches(Regex("\\d+\\.\\d+\\.\\d+"))) {
            errors.add("Min IntelliJ version must be in build number format (e.g., 203.7148.57)")
        }

        return errors
    }
}

/**
 * Data class representing comprehensive theme metadata.
 *
 * @property id Unique theme identifier for plugin.xml (e.g., "wt-one-dark-a7f3e2b1")
 * @property name Theme name for display (e.g., "WT One Dark")
 * @property displayName Display name without prefix (e.g., "One Dark")
 * @property author Theme author/creator
 * @property createdDate Timestamp when theme was generated (ISO 8601 format)
 * @property sourceScheme Original Windows Terminal scheme name
 * @property generatorVersion Version of the theme generator
 * @property intellijVersion Target IntelliJ version compatibility string
 * @property isDark Whether this is a dark theme (true) or light theme (false)
 * @property themeVersion Semantic version of the theme (e.g., "1.0.0")
 * @property minIntellijVersion Minimum compatible IntelliJ version (build number)
 * @property maxIntellijVersion Maximum compatible IntelliJ version (null = no limit)
 * @property fingerprint Color-based fingerprint for duplicate detection
 */
data class ThemeMetadata(
    val id: String,
    val name: String,
    val displayName: String,
    val author: String,
    val createdDate: String,
    val sourceScheme: String,
    val generatorVersion: String,
    val intellijVersion: String,
    val isDark: Boolean,
    val themeVersion: String = ThemeMetadataGenerator.DEFAULT_THEME_VERSION,
    val minIntellijVersion: String = ThemeMetadataGenerator.MIN_INTELLIJ_VERSION,
    val maxIntellijVersion: String? = ThemeMetadataGenerator.MAX_INTELLIJ_VERSION,
    val fingerprint: String
) {
    /**
     * Converts metadata to a map for JSON serialization or template substitution.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "displayName" to displayName,
            "author" to author,
            "createdDate" to createdDate,
            "sourceScheme" to sourceScheme,
            "generatorVersion" to generatorVersion,
            "intellijVersion" to intellijVersion,
            "isDark" to isDark,
            "themeVersion" to themeVersion,
            "minIntellijVersion" to minIntellijVersion,
            "maxIntellijVersion" to maxIntellijVersion,
            "fingerprint" to fingerprint
        )
    }

    /**
     * Generates a summary string for logging.
     */
    fun toSummaryString(): String {
        return buildString {
            appendLine("Theme: $name")
            appendLine("  ID: $id")
            appendLine("  Display Name: $displayName")
            appendLine("  Author: $author")
            appendLine("  Version: $themeVersion")
            appendLine("  Type: ${if (isDark) "Dark" else "Light"}")
            appendLine("  Source: $sourceScheme")
            appendLine("  Created: $createdDate")
            appendLine("  IntelliJ: $intellijVersion (>= $minIntellijVersion)")
            appendLine("  Fingerprint: $fingerprint")
        }
    }
}
