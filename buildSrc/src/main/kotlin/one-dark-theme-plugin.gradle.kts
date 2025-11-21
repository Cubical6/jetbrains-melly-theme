tasks.register("patchReadMeHtml", PatchReadmeHTML::class.java)
tasks.register("createReleaseNotes", CreateReleaseNotes::class.java)
tasks.register("copyReadme", CopyReadme::class.java)
tasks.register("createThemes", themes.ThemeConstructor::class.java)

// Windows Terminal integration tasks
tasks.register("importWindowsTerminalSchemes", tasks.ImportWindowsTerminalSchemes::class.java)
tasks.register("generateThemesFromWindowsTerminal", tasks.GenerateThemesFromWindowsTerminal::class.java) {
    // Generate task depends on import task
    dependsOn("importWindowsTerminalSchemes")
}

// Make patchPluginXml depend on Windows Terminal theme generation
tasks.named("patchPluginXml") {
    dependsOn("generateThemesFromWindowsTerminal")
}