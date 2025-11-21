@file:Suppress("DEPRECATION")

import java.util.*

/**
 * @deprecated Use Kotlin's native null safety instead of Optional pattern.
 * This file will be removed in Sprint 6.
 * See CODE_REVIEW_REPORT.md HIGH-003 for details.
 */
@Deprecated(
  message = "Use Kotlin's native null safety (?.let, ?:, etc.) instead of Optional",
  replaceWith = ReplaceWith("this"),
  level = DeprecationLevel.WARNING
)
fun <T> T?.toOptional() = Optional.ofNullable(this)

/**
 * @deprecated Use Kotlin's native null safety instead of Optional pattern.
 * This file will be removed in Sprint 6.
 * See CODE_REVIEW_REPORT.md HIGH-003 for details.
 */
@Deprecated(
  message = "Use Kotlin's native null safety (?.let {} ?: {}) instead of Optional",
  level = DeprecationLevel.WARNING
)
// This is needed to support Android Studio, because I think it
// Still runs on a Java 8 Runtime, so no fancy Optional interfaces...
fun <T> Optional<T>.doOrElse(present: (T) -> Unit, notThere: () -> Unit) =
  this.map {
    it to true
  }.map {
    it.toOptional()
  }.orElseGet {
    (null to false).toOptional()
  }.ifPresent {
    if (it.second) {
      present(it.first)
    } else {
      notThere()
    }
  }
