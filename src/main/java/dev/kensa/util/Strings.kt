package dev.kensa.util

object Strings {
    private val CAMEL_SPLIT_REGEX = Regex("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[0-9])")

    fun unCamel(phrase: String): String {
        if (phrase.isBlank()) {
            return phrase
        }
        val replaced = phrase.replace("[^a-zA-Z0-9]".toRegex(), "")
        return CAMEL_SPLIT_REGEX.split(replaced).toTypedArray().let { elements ->
            elements[0] = Character.toUpperCase(elements[0][0]).toString() + elements[0].substring(1)
            elements.joinToString(" ")
        }
    }

    fun unCamelToSeparated(phrase: String, separator: String = "-"): String? {
        return if (phrase.isBlank()) {
            phrase
        } else {
            "(?=\\p{Lu})".toRegex().split(phrase.trim()).filterNot { it.isBlank() }.joinToString(separator).toLowerCase()
        }
    }
}