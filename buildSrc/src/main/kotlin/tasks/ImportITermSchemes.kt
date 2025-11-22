package tasks

import converters.ITermToWindowsTerminalConverter
import parsers.ITermPlistParser
import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Gradle task to import iTerm2 .itermcolors files and convert to Windows Terminal JSON
 *
 * Usage: ./gradlew importITermSchemes
 *
 * Input: iterm-schemes/ directory (*.itermcolors files)
 * Output: windows-terminal-schemes/ directory (*.json files)
 */
open class ImportITermSchemes : DefaultTask() {

    init {
        group = "theme"
        description = "Import iTerm2 .itermcolors files to Windows Terminal JSON format"
    }

    @TaskAction
    fun importSchemes() {
        val iTermDir = project.file("iterm-schemes")
        val outputDir = project.file("windows-terminal-schemes")

        if (!iTermDir.exists()) {
            println("Creating iterm-schemes/ directory...")
            iTermDir.mkdirs()
            println("Place .itermcolors files in iterm-schemes/ and run this task again.")
            return
        }

        val iTermFiles = iTermDir.listFiles { file ->
            file.extension == "itermcolors"
        } ?: emptyArray()

        if (iTermFiles.isEmpty()) {
            println("No .itermcolors files found in iterm-schemes/")
            return
        }

        println("Found ${iTermFiles.size} iTerm scheme(s) to import")

        outputDir.mkdirs()
        val gson = GsonBuilder().setPrettyPrinting().create()

        var successCount = 0
        var errorCount = 0

        for (iTermFile in iTermFiles) {
            try {
                println("Importing: ${iTermFile.name}")

                // Parse iTerm scheme
                val iTermScheme = ITermPlistParser.parse(iTermFile)

                // Validate
                val errors = iTermScheme.validate()
                if (errors.isNotEmpty()) {
                    println("  Warning: Validation errors:")
                    errors.forEach { println("     - $it") }
                    errorCount++
                    continue
                }

                // Convert to Windows Terminal
                val wtScheme = ITermToWindowsTerminalConverter.convert(iTermScheme)

                // Write JSON
                val outputFile = File(outputDir, "${wtScheme.name}.json")
                outputFile.writeText(gson.toJson(wtScheme))

                println("  Success: Converted to: ${outputFile.name}")
                successCount++

            } catch (e: Exception) {
                println("  Error: ${e.message}")
                errorCount++
            }
        }

        println("\nImport complete:")
        println("  Success: $successCount")
        println("  Errors: $errorCount")
    }
}
