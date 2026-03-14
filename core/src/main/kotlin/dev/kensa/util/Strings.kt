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

fun String.unCamel(protectedPhrases: Collection<String>): String {
    if (isBlank()) return this
    if (protectedPhrases.isEmpty()) return unCamel()
    val cleaned = replace(ALPHA_NUMERIC_REGEX, "")
    if (cleaned.isEmpty()) return this

    data class Span(val start: Int, val end: Int)

    val spans = mutableListOf<Span>()
    for (phrase in protectedPhrases) {
        Regex(Regex.escape(phrase), RegexOption.IGNORE_CASE).findAll(cleaned)
            .forEach { spans.add(Span(it.range.first, it.range.last + 1)) }
    }
    spans.sortBy { it.start }

    // Remove overlapping spans, keeping first match at each position
    val merged = mutableListOf<Span>()
    for (span in spans) {
        if (merged.isEmpty() || span.start >= merged.last().end) merged.add(span)
    }

    val tokens = mutableListOf<String>()
    var pos = 0
    for (span in merged) {
        if (pos < span.start) tokens.addAll(CAMEL_SPLIT_REGEX.split(cleaned.substring(pos, span.start)).filter { it.isNotEmpty() })
        tokens.add(cleaned.substring(span.start, span.end))
        pos = span.end
    }
    if (pos < cleaned.length) tokens.addAll(CAMEL_SPLIT_REGEX.split(cleaned.substring(pos)).filter { it.isNotEmpty() })

    if (tokens.isEmpty()) return this
    tokens[0] = tokens[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    return tokens.joinToString(" ")
}

fun String.unCamelToSeparated(separator: String = "-") =
    if (this.isBlank()) this
    else UPPER_CASE_LETTER_REGEX.split(trim()).filterNot { it.isBlank() }.joinToString(separator).lowercase(Locale.getDefault())

private fun String?.onOneLine() =
    this?.replace("\r", "\\r")?.replace("\n", "\\n")
