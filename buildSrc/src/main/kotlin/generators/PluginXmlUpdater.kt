package generators

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Manages plugin.xml file for IntelliJ IDEA theme plugins.
 *
 * This updater provides:
 * - Safe parsing and updating of plugin.xml
 * - Adding/removing themeProvider entries
 * - Preserving existing theme entries (especially One Dark themes)
 * - Automatic backup before modifications
 * - Proper XML formatting with indentation
 * - Error handling and validation
 *
 * Based on TASK-504 from Sprint 4 specifications.
 *
 * Usage:
 * ```kotlin
 * val updater = PluginXmlUpdater(Path.of("src/main/resources/META-INF/plugin.xml"))
 * updater.backupPluginXml()
 * updater.addThemeProvider("wt-one-dark-abc123", "/themes/wt-one-dark.theme.json")
 * updater.updatePluginXml(themeMetadataList)
 * ```
 *
 * @property pluginXmlPath Path to the plugin.xml file
 */
class PluginXmlUpdater(private val pluginXmlPath: Path) {

    companion object {
        /**
         * XML namespace for IntelliJ extensions
         */
        private const val INTELLIJ_NAMESPACE = "com.intellij"

        /**
         * Default indentation for XML formatting
         */
        private const val XML_INDENT = "  "

        /**
         * Backup file suffix
         */
        private const val BACKUP_SUFFIX = ".backup"

        /**
         * Theme provider element name
         */
        private const val THEME_PROVIDER_ELEMENT = "themeProvider"

        /**
         * Bundled color scheme element name
         */
        private const val BUNDLED_COLOR_SCHEME_ELEMENT = "bundledColorScheme"

        /**
         * Extensions element name
         */
        private const val EXTENSIONS_ELEMENT = "extensions"

        /**
         * Default extension namespace attribute name
         */
        private const val DEFAULT_EXTENSION_NS_ATTR = "defaultExtensionNs"

        /**
         * Prefix for Windows Terminal generated theme IDs
         */
        private const val WT_THEME_PREFIX = "wt-"
    }

    /**
     * Validates that the plugin.xml file exists and is readable.
     *
     * @throws IllegalStateException if file doesn't exist or isn't readable
     */
    init {
        require(pluginXmlPath.exists()) {
            "plugin.xml does not exist at: $pluginXmlPath"
        }
        require(Files.isReadable(pluginXmlPath)) {
            "plugin.xml is not readable at: $pluginXmlPath"
        }
    }

    /**
     * Creates a backup of the plugin.xml file with timestamp.
     *
     * Backup format: "plugin.xml.backup-YYYY-MM-DD-HHmmss"
     *
     * @return Path to the backup file
     * @throws Exception if backup creation fails
     */
    fun backupPluginXml(): Path {
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"))
        val backupPath = pluginXmlPath.resolveSibling(
            "${pluginXmlPath.fileName}$BACKUP_SUFFIX-$timestamp"
        )

        Files.copy(pluginXmlPath, backupPath, StandardCopyOption.REPLACE_EXISTING)

        return backupPath
    }

    /**
     * Adds a single themeProvider entry to plugin.xml.
     *
     * If a themeProvider with the same ID already exists, it will be updated.
     * Preserves existing non-WT theme providers.
     *
     * @param themeId Unique theme ID (e.g., "wt-one-dark-abc123")
     * @param themePath Path to theme file relative to resources (e.g., "/themes/theme.theme.json")
     * @throws Exception if XML parsing or writing fails
     */
    fun addThemeProvider(themeId: String, themePath: String) {
        val doc = parsePluginXml()
        val extensions = getOrCreateExtensionsElement(doc)

        // Remove existing entry with same ID (if any)
        removeThemeProviderFromDocument(doc, themeId)

        // Create new themeProvider element
        val themeProvider = doc.createElement(THEME_PROVIDER_ELEMENT)
        themeProvider.setAttribute("id", themeId)
        themeProvider.setAttribute("path", themePath)

        // Add to extensions
        extensions.appendChild(createIndentNode(doc, 2))
        extensions.appendChild(themeProvider)
        extensions.appendChild(createIndentNode(doc, 1))

        // Write back to file
        writePluginXml(doc)
    }

    /**
     * Removes a themeProvider entry from plugin.xml by ID.
     *
     * @param themeId Theme ID to remove
     * @throws Exception if XML parsing or writing fails
     */
    fun removeThemeProvider(themeId: String) {
        val doc = parsePluginXml()
        val removed = removeThemeProviderFromDocument(doc, themeId)

        if (removed) {
            writePluginXml(doc)
        }
    }

    /**
     * Adds a bundledColorScheme entry to plugin.xml.
     *
     * Creates a bundledColorScheme XML element with the specified path.
     * The path should be the base name without the .xml extension.
     *
     * Example:
     * ```kotlin
     * addBundledColorScheme("wt-dracula")
     * // Creates: <bundledColorScheme path="/themes/wt-dracula"/>
     * ```
     *
     * @param baseName Base name of the color scheme (without .xml extension)
     * @param themesDir Directory containing theme files (default: "/themes")
     * @throws Exception if XML parsing or writing fails
     */
    fun addBundledColorScheme(baseName: String, themesDir: String = "/themes") {
        val doc = parsePluginXml()
        val extensions = getOrCreateExtensionsElement(doc)

        // Remove existing entry with same path (if any)
        val path = "$themesDir/$baseName"
        removeBundledColorSchemeFromDocument(doc, path)

        // Create new bundledColorScheme element
        val bundledColorScheme = doc.createElement(BUNDLED_COLOR_SCHEME_ELEMENT)
        bundledColorScheme.setAttribute("path", path)

        // Add to extensions
        extensions.appendChild(createIndentNode(doc, 2))
        extensions.appendChild(bundledColorScheme)
        extensions.appendChild(createIndentNode(doc, 1))

        // Write back to file
        writePluginXml(doc)
    }

    /**
     * Updates plugin.xml with multiple theme providers and bundled color schemes.
     *
     * Strategy:
     * 1. Backup existing plugin.xml
     * 2. Remove all existing WT theme providers and bundled color schemes (prefix "wt-")
     * 3. Keep all non-WT theme providers (e.g., One Dark themes)
     * 4. Add all new theme providers from metadata list
     * 5. Add all new bundled color schemes from metadata list
     * 6. Write formatted XML back to file
     *
     * @param themes List of theme metadata to add
     * @param themesDir Directory containing theme files (default: "/themes")
     * @return UpdateResult with statistics
     */
    fun updatePluginXml(
        themes: List<ThemeMetadata>,
        themesDir: String = "/themes"
    ): UpdateResult {
        // Backup first
        val backupPath = try {
            backupPluginXml()
        } catch (e: Exception) {
            return UpdateResult(
                success = false,
                themesAdded = 0,
                themesRemoved = 0,
                backupPath = null,
                error = "Failed to create backup: ${e.message}"
            )
        }

        try {
            val doc = parsePluginXml()
            @Suppress("UNUSED_VARIABLE")
            val extensions = getOrCreateExtensionsElement(doc)

            // Count existing WT themes
            val existingWtThemes = getThemeProviders(doc)
                .filter { it.getAttribute("id").startsWith(WT_THEME_PREFIX) }
                .size

            // Remove all existing WT theme providers and bundled color schemes
            removeAllWtThemeProviders(doc)
            removeAllWtBundledColorSchemes(doc)

            // Add new theme providers for BOTH variants (Standard and Rounded)
            var totalThemesAdded = 0
            themes.forEach { metadata ->
                // Standard variant (no suffix)
                val standardThemePath = "$themesDir/${metadata.id}.theme.json"
                addThemeProviderToDocument(doc, metadata.id, standardThemePath)
                totalThemesAdded++

                // Rounded variant (with .rounded suffix in ID and _rounded in filename)
                val roundedThemeId = "${metadata.id}.rounded"
                val roundedThemePath = "$themesDir/${metadata.id}_rounded.theme.json"
                addThemeProviderToDocument(doc, roundedThemeId, roundedThemePath)
                totalThemesAdded++
            }

            // Add bundled color schemes (shared between variants, so only one per scheme)
            themes.forEach { metadata ->
                val colorSchemePath = "$themesDir/${metadata.id}"
                addBundledColorSchemeToDocument(doc, colorSchemePath)
            }

            // Write formatted XML
            writePluginXml(doc)

            return UpdateResult(
                success = true,
                themesAdded = totalThemesAdded,
                themesRemoved = existingWtThemes,
                backupPath = backupPath,
                error = null
            )

        } catch (e: Exception) {
            // Try to restore from backup
            try {
                Files.copy(backupPath, pluginXmlPath, StandardCopyOption.REPLACE_EXISTING)
            } catch (restoreError: Exception) {
                return UpdateResult(
                    success = false,
                    themesAdded = 0,
                    themesRemoved = 0,
                    backupPath = backupPath,
                    error = "Update failed: ${e.message}. Restore also failed: ${restoreError.message}"
                )
            }

            return UpdateResult(
                success = false,
                themesAdded = 0,
                themesRemoved = 0,
                backupPath = backupPath,
                error = "Update failed: ${e.message}. Restored from backup."
            )
        }
    }

    /**
     * Gets all existing themeProvider entries from plugin.xml.
     *
     * @return List of theme provider info (id, path)
     */
    fun getExistingThemeProviders(): List<ThemeProviderInfo> {
        val doc = parsePluginXml()
        return getThemeProviders(doc).map { element ->
            ThemeProviderInfo(
                id = element.getAttribute("id"),
                path = element.getAttribute("path")
            )
        }
    }

    /**
     * Checks if a theme provider with the given ID exists.
     *
     * @param themeId Theme ID to check
     * @return True if exists, false otherwise
     */
    fun hasThemeProvider(themeId: String): Boolean {
        return getExistingThemeProviders().any { it.id == themeId }
    }

    /**
     * Removes all Windows Terminal theme providers (prefix "wt-").
     *
     * @return Number of themes removed
     */
    fun removeAllWtThemeProviders(): Int {
        val doc = parsePluginXml()
        val removed = removeAllWtThemeProvidersFromDocument(doc)
        if (removed > 0) {
            writePluginXml(doc)
        }
        return removed
    }

    // Private helper methods

    /**
     * Parses the plugin.xml file into a DOM Document.
     */
    private fun parsePluginXml(): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isIgnoringComments = false
        factory.isIgnoringElementContentWhitespace = false
        val builder = factory.newDocumentBuilder()
        return builder.parse(pluginXmlPath.toFile())
    }

    /**
     * Writes a DOM Document back to the plugin.xml file with formatting.
     */
    private fun writePluginXml(doc: Document) {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")

        // Normalize document
        doc.normalize()

        val source = DOMSource(doc)
        val result = StreamResult(pluginXmlPath.toFile())
        transformer.transform(source, result)
    }

    /**
     * Gets or creates the <extensions> element.
     */
    private fun getOrCreateExtensionsElement(doc: Document): Element {
        val nodeList = doc.getElementsByTagName(EXTENSIONS_ELEMENT)
        if (nodeList.length > 0) {
            return nodeList.item(0) as Element
        }

        // Create new extensions element
        val extensions = doc.createElement(EXTENSIONS_ELEMENT)
        extensions.setAttribute(DEFAULT_EXTENSION_NS_ATTR, INTELLIJ_NAMESPACE)
        doc.documentElement.appendChild(extensions)
        return extensions
    }

    /**
     * Gets all themeProvider elements from the document.
     */
    private fun getThemeProviders(doc: Document): List<Element> {
        val nodeList = doc.getElementsByTagName(THEME_PROVIDER_ELEMENT)
        return (0 until nodeList.length)
            .map { nodeList.item(it) as Element }
    }

    /**
     * Removes a themeProvider from document by ID.
     *
     * @return True if removed, false if not found
     */
    private fun removeThemeProviderFromDocument(doc: Document, themeId: String): Boolean {
        val themeProviders = getThemeProviders(doc)
        val toRemove = themeProviders.find { it.getAttribute("id") == themeId }

        return if (toRemove != null) {
            // Remove adjacent whitespace/newline nodes for clean formatting
            val parent = toRemove.parentNode
            val previousSibling = toRemove.previousSibling
            parent.removeChild(toRemove)

            // Remove preceding whitespace if it's a text node
            if (previousSibling != null && previousSibling.nodeType == Node.TEXT_NODE &&
                previousSibling.textContent?.trim()?.isEmpty() == true
            ) {
                parent.removeChild(previousSibling)
            }

            true
        } else {
            false
        }
    }

    /**
     * Removes all WT theme providers from document.
     *
     * @return Number of themes removed
     */
    private fun removeAllWtThemeProvidersFromDocument(doc: Document): Int {
        val themeProviders = getThemeProviders(doc)
        val wtThemes = themeProviders.filter {
            it.getAttribute("id").startsWith(WT_THEME_PREFIX)
        }

        wtThemes.forEach { themeProvider ->
            val parent = themeProvider.parentNode
            val previousSibling = themeProvider.previousSibling
            parent.removeChild(themeProvider)

            // Remove preceding whitespace
            if (previousSibling != null && previousSibling.nodeType == Node.TEXT_NODE &&
                previousSibling.textContent?.trim()?.isEmpty() == true
            ) {
                parent.removeChild(previousSibling)
            }
        }

        return wtThemes.size
    }

    /**
     * Removes all WT theme providers without writing to file.
     */
    private fun removeAllWtThemeProviders(doc: Document) {
        removeAllWtThemeProvidersFromDocument(doc)
    }

    /**
     * Adds a themeProvider to the document without writing to file.
     */
    private fun addThemeProviderToDocument(doc: Document, themeId: String, themePath: String) {
        val extensions = getOrCreateExtensionsElement(doc)

        val themeProvider = doc.createElement(THEME_PROVIDER_ELEMENT)
        themeProvider.setAttribute("id", themeId)
        themeProvider.setAttribute("path", themePath)

        // Add with proper indentation
        extensions.appendChild(createIndentNode(doc, 2))
        extensions.appendChild(themeProvider)
        extensions.appendChild(createIndentNode(doc, 1))
    }

    /**
     * Creates a text node for indentation.
     */
    private fun createIndentNode(doc: Document, level: Int): Node {
        val indent = "\n" + XML_INDENT.repeat(level)
        return doc.createTextNode(indent)
    }

    /**
     * Gets all bundledColorScheme elements from the document.
     */
    private fun getBundledColorSchemes(doc: Document): List<Element> {
        val nodeList = doc.getElementsByTagName(BUNDLED_COLOR_SCHEME_ELEMENT)
        return (0 until nodeList.length)
            .map { nodeList.item(it) as Element }
    }

    /**
     * Removes a bundledColorScheme from document by path.
     *
     * @return True if removed, false if not found
     */
    private fun removeBundledColorSchemeFromDocument(doc: Document, path: String): Boolean {
        val bundledColorSchemes = getBundledColorSchemes(doc)
        val toRemove = bundledColorSchemes.find { it.getAttribute("path") == path }

        return if (toRemove != null) {
            // Remove adjacent whitespace/newline nodes for clean formatting
            val parent = toRemove.parentNode
            val previousSibling = toRemove.previousSibling
            parent.removeChild(toRemove)

            // Remove preceding whitespace if it's a text node
            if (previousSibling != null && previousSibling.nodeType == Node.TEXT_NODE &&
                previousSibling.textContent?.trim()?.isEmpty() == true
            ) {
                parent.removeChild(previousSibling)
            }

            true
        } else {
            false
        }
    }

    /**
     * Removes all WT bundled color schemes from document.
     *
     * @return Number of color schemes removed
     */
    private fun removeAllWtBundledColorSchemesFromDocument(doc: Document): Int {
        val bundledColorSchemes = getBundledColorSchemes(doc)
        val wtColorSchemes = bundledColorSchemes.filter {
            val path = it.getAttribute("path")
            // Match paths like "/themes/wt-*"
            path.contains("/$WT_THEME_PREFIX")
        }

        wtColorSchemes.forEach { colorScheme ->
            val parent = colorScheme.parentNode
            val previousSibling = colorScheme.previousSibling
            parent.removeChild(colorScheme)

            // Remove preceding whitespace
            if (previousSibling != null && previousSibling.nodeType == Node.TEXT_NODE &&
                previousSibling.textContent?.trim()?.isEmpty() == true
            ) {
                parent.removeChild(previousSibling)
            }
        }

        return wtColorSchemes.size
    }

    /**
     * Removes all WT bundled color schemes without writing to file.
     */
    private fun removeAllWtBundledColorSchemes(doc: Document) {
        removeAllWtBundledColorSchemesFromDocument(doc)
    }

    /**
     * Adds a bundledColorScheme to the document without writing to file.
     */
    private fun addBundledColorSchemeToDocument(doc: Document, path: String) {
        val extensions = getOrCreateExtensionsElement(doc)

        val bundledColorScheme = doc.createElement(BUNDLED_COLOR_SCHEME_ELEMENT)
        bundledColorScheme.setAttribute("path", path)

        // Add with proper indentation
        extensions.appendChild(createIndentNode(doc, 2))
        extensions.appendChild(bundledColorScheme)
        extensions.appendChild(createIndentNode(doc, 1))
    }
}

/**
 * Information about a theme provider entry.
 *
 * @property id Theme ID
 * @property path Theme file path
 */
data class ThemeProviderInfo(
    val id: String,
    val path: String
)

/**
 * Result of a plugin.xml update operation.
 *
 * @property success Whether the update succeeded
 * @property themesAdded Number of themes added
 * @property themesRemoved Number of themes removed
 * @property backupPath Path to backup file (if created)
 * @property error Error message (if failed)
 */
data class UpdateResult(
    val success: Boolean,
    val themesAdded: Int,
    val themesRemoved: Int,
    val backupPath: Path?,
    val error: String?
) {
    /**
     * Generates a summary string for logging.
     */
    fun toSummaryString(): String {
        return if (success) {
            buildString {
                appendLine("Plugin.xml updated successfully")
                appendLine("  Themes added: $themesAdded")
                appendLine("  Themes removed: $themesRemoved")
                backupPath?.let { appendLine("  Backup: $it") }
            }
        } else {
            "Plugin.xml update failed: $error"
        }
    }
}
