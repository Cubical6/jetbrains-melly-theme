package variants

/**
 * Represents different UI style variants for themes
 */
sealed class ThemeVariant(
    val suffix: String,
    val displayName: String,
    val arcValues: ArcValues
) {
    /**
     * Standard variant with sharp corners (arc = 0)
     */
    object Standard : ThemeVariant(
        suffix = "",
        displayName = "",
        arcValues = ArcValues(
            component = 0,
            button = 0,
            tabbedPane = 0,
            progressBar = 0,
            comboBox = 0,
            textField = 0,
            checkBox = 0,
            tree = 0,
            table = 0,
            popup = 0
        )
    )

    /**
     * Rounded variant with modern rounded corners (arc = 6-12)
     */
    object Rounded : ThemeVariant(
        suffix = " Rounded",
        displayName = "Rounded",
        arcValues = ArcValues(
            component = 8,
            button = 6,
            tabbedPane = 8,
            progressBar = 4,
            comboBox = 4,
            textField = 4,
            checkBox = 3,
            tree = 4,
            table = 0,
            popup = 12
        )
    )

    companion object {
        fun all(): List<ThemeVariant> = listOf(Standard, Rounded)
    }
}

/**
 * Arc (border radius) values for different UI components
 */
data class ArcValues(
    val component: Int,      // General component arc
    val button: Int,         // Button arc
    val tabbedPane: Int,     // Tab arc
    val progressBar: Int,    // Progress bar arc
    val comboBox: Int,       // Combo box arc
    val textField: Int,      // Text field arc
    val checkBox: Int,       // Checkbox arc
    val tree: Int,           // Tree row arc
    val table: Int,          // Table cell arc
    val popup: Int           // Popup window arc
) {
    /**
     * Convert to template placeholder map
     */
    fun toPlaceholders(): Map<String, String> = mapOf(
        "\$arc_component$" to component.toString(),
        "\$arc_button$" to button.toString(),
        "\$arc_tabbed_pane$" to tabbedPane.toString(),
        "\$arc_progress_bar$" to progressBar.toString(),
        "\$arc_combobox$" to comboBox.toString(),
        "\$arc_text_field$" to textField.toString(),
        "\$arc_checkbox$" to checkBox.toString(),
        "\$arc_tree$" to tree.toString(),
        "\$arc_table$" to table.toString(),
        "\$arc_popup$" to popup.toString()
    )
}
