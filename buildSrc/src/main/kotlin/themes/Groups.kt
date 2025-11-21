package themes

/**
 * Represents groups of syntax elements for styling.
 */
enum class Groups(val value: String) {
  ATTRIBUTES("attributes"),
  COMMENTS("comments"),
  KEYWORDS("keywords");

  companion object {
    private val valueMap = values().associateBy { it.value }

    /**
     * Converts a string value to a Groups enum.
     *
     * @param value The string value to convert
     * @return The corresponding Groups enum
     * @throws IllegalArgumentException if the value is not recognized
     */
    fun fromValue(value: String): Groups =
      valueMap[value] ?: throw IllegalArgumentException("Unknown grouping: $value")

    /**
     * Converts a string value to a Groups enum, or null if not found.
     *
     * @param value The string value to convert
     * @return The corresponding Groups enum, or null if not found
     */
    fun fromValueOrNull(value: String): Groups? = valueMap[value]
  }
}

/**
 * Extension function to convert a String to a Groups enum.
 *
 * @throws IllegalArgumentException if the string does not match any Groups value
 */
fun String.toGroup(): Groups = Groups.fromValue(this)

/**
 * Extension function to safely convert a String to a Groups enum.
 *
 * @return The corresponding Groups enum, or null if not found
 */
fun String.toGroupOrNull(): Groups? = Groups.fromValueOrNull(this)
