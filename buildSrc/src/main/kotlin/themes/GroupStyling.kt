package themes

/**
 * Represents font styling options for syntax element groups.
 */
enum class GroupStyling(val value: String) {
  REGULAR("Regular"),
  ITALIC("Italic"),
  BOLD("Bold"),
  BOLD_ITALIC("Bold Italic");

  companion object {
    private val valueMap = values().associateBy { it.value }

    /**
     * Converts a string value to a GroupStyling enum.
     * Returns REGULAR as default if value is not recognized.
     *
     * @param value The string value to convert
     * @return The corresponding GroupStyling enum, or REGULAR if not found
     */
    fun fromValue(value: String): GroupStyling = valueMap[value] ?: REGULAR

    /**
     * Converts a string value to a GroupStyling enum, or null if not found.
     *
     * @param value The string value to convert
     * @return The corresponding GroupStyling enum, or null if not found
     */
    fun fromValueOrNull(value: String): GroupStyling? = valueMap[value]
  }
}

/**
 * Extension function to convert a String to a GroupStyling enum.
 * Returns REGULAR as default if value is not recognized.
 */
fun String.toGroupStyle(): GroupStyling = GroupStyling.fromValue(this)

/**
 * Extension function to safely convert a String to a GroupStyling enum.
 *
 * @return The corresponding GroupStyling enum, or null if not found
 */
fun String.toGroupStyleOrNull(): GroupStyling? = GroupStyling.fromValueOrNull(this)
