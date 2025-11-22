package parsers

import colorschemes.ITermColorScheme
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parser for iTerm2 .itermcolors files (XML plist format)
 *
 * Format example:
 * <plist version="1.0">
 *   <dict>
 *     <key>Ansi 0 Color</key>
 *     <dict>
 *       <key>Red Component</key><real>0.11</real>
 *       <key>Green Component</key><real>0.12</real>
 *       <key>Blue Component</key><real>0.16</real>
 *     </dict>
 *   </dict>
 * </plist>
 */
object ITermPlistParser {

    private val COLOR_KEYS = mapOf(
        "Ansi 0 Color" to 0,
        "Ansi 1 Color" to 1,
        "Ansi 2 Color" to 2,
        "Ansi 3 Color" to 3,
        "Ansi 4 Color" to 4,
        "Ansi 5 Color" to 5,
        "Ansi 6 Color" to 6,
        "Ansi 7 Color" to 7,
        "Ansi 8 Color" to 8,
        "Ansi 9 Color" to 9,
        "Ansi 10 Color" to 10,
        "Ansi 11 Color" to 11,
        "Ansi 12 Color" to 12,
        "Ansi 13 Color" to 13,
        "Ansi 14 Color" to 14,
        "Ansi 15 Color" to 15
    )

    /**
     * Parse .itermcolors file to ITermColorScheme
     */
    fun parse(file: File): ITermColorScheme {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        require(file.extension == "itermcolors") { "File must be .itermcolors, got: ${file.name}" }

        val doc = parseXML(file)
        val rootDict = getRootDict(doc)

        val ansiColors = mutableMapOf<Int, ITermColorScheme.ITermColor>()
        var foreground: ITermColorScheme.ITermColor? = null
        var background: ITermColorScheme.ITermColor? = null
        var selection: ITermColorScheme.ITermColor? = null
        var cursor: ITermColorScheme.ITermColor? = null
        var cursorText: ITermColorScheme.ITermColor? = null
        var bold: ITermColorScheme.ITermColor? = null
        var link: ITermColorScheme.ITermColor? = null

        // Parse all key-value pairs in root dict
        val entries = parseDictEntries(rootDict)

        for ((key, valueDict) in entries) {
            val color = parseColorDict(valueDict)

            when {
                COLOR_KEYS.containsKey(key) -> ansiColors[COLOR_KEYS[key]!!] = color
                key == "Foreground Color" -> foreground = color
                key == "Background Color" -> background = color
                key == "Selection Color" -> selection = color
                key == "Cursor Color" -> cursor = color
                key == "Cursor Text Color" -> cursorText = color
                key == "Bold Color" -> bold = color
                key == "Link Color" -> link = color
            }
        }

        // Validate required colors
        requireNotNull(foreground) { "Missing 'Foreground Color' in ${file.name}" }
        requireNotNull(background) { "Missing 'Background Color' in ${file.name}" }
        requireNotNull(selection) { "Missing 'Selection Color' in ${file.name}" }
        requireNotNull(cursor) { "Missing 'Cursor Color' in ${file.name}" }

        return ITermColorScheme(
            name = file.nameWithoutExtension,
            ansiColors = ansiColors,
            foreground = foreground,
            background = background,
            selection = selection,
            cursor = cursor,
            cursorText = cursorText,
            bold = bold,
            link = link
        )
    }

    private fun parseXML(file: File): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        return builder.parse(file)
    }

    private fun getRootDict(doc: Document): Element {
        val plist = doc.getElementsByTagName("plist").item(0) as? Element
            ?: throw IllegalArgumentException("No <plist> element found")

        val dict = plist.getElementsByTagName("dict").item(0) as? Element
            ?: throw IllegalArgumentException("No root <dict> element found")

        return dict
    }

    private fun parseDictEntries(dict: Element): Map<String, Element> {
        val entries = mutableMapOf<String, Element>()
        val children = dict.childNodes

        var i = 0
        while (i < children.length) {
            val node = children.item(i)

            if (node is Element && node.tagName == "key") {
                val key = node.textContent.trim()

                // Find next dict element
                var j = i + 1
                while (j < children.length) {
                    val valueNode = children.item(j)
                    if (valueNode is Element && valueNode.tagName == "dict") {
                        entries[key] = valueNode
                        break
                    }
                    j++
                }
            }

            i++
        }

        return entries
    }

    private fun parseColorDict(dict: Element): ITermColorScheme.ITermColor {
        val components = parseDictValues(dict)

        val red = components["Red Component"]?.toFloatOrNull()
            ?: throw IllegalArgumentException("Missing 'Red Component'")
        val green = components["Green Component"]?.toFloatOrNull()
            ?: throw IllegalArgumentException("Missing 'Green Component'")
        val blue = components["Blue Component"]?.toFloatOrNull()
            ?: throw IllegalArgumentException("Missing 'Blue Component'")
        val alpha = components["Alpha Component"]?.toFloatOrNull() ?: 1.0f

        return ITermColorScheme.ITermColor(red, green, blue, alpha)
    }

    private fun parseDictValues(dict: Element): Map<String, String> {
        val values = mutableMapOf<String, String>()
        val children = dict.childNodes

        var i = 0
        while (i < children.length) {
            val node = children.item(i)

            if (node is Element && node.tagName == "key") {
                val key = node.textContent.trim()

                // Find next real/string/integer element
                var j = i + 1
                while (j < children.length) {
                    val valueNode = children.item(j)
                    if (valueNode is Element && valueNode.tagName in listOf("real", "string", "integer")) {
                        values[key] = valueNode.textContent.trim()
                        break
                    }
                    j++
                }
            }

            i++
        }

        return values
    }
}
