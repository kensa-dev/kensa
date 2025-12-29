package dev.kensa.util

import java.util.*

private val CAMEL_SPLIT_REGEX = Regex("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[0-9])")
private val UPPER_CASE_LETTER_REGEX = Regex("(?=\\p{Lu})")
private val ALPHA_NUMERIC_REGEX = Regex("[^a-zA-Z0-9]")

fun String.unCamel() =
    when {
        this.isBlank() -> this
        else -> replace(ALPHA_NUMERIC_REGEX, "").let { p ->
            CAMEL_SPLIT_REGEX.split(p).toTypedArray().let { elements ->
                elements[0] = elements[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                elements.joinToString(" ")
            }
        }
    }

fun String.unCamelToSeparated(separator: String = "-") =
    if (this.isBlank()) this
    else UPPER_CASE_LETTER_REGEX.split(trim()).filterNot { it.isBlank() }.joinToString(separator).lowercase(Locale.getDefault())

private fun String?.onOneLine() =
    this?.replace("\r", "\\r")?.replace("\n", "\\n")
