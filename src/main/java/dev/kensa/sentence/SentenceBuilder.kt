package dev.kensa.sentence

import dev.kensa.Kensa
import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.parse.Event.Location
import dev.kensa.sentence.TokenType.*
import dev.kensa.sentence.TokenType.Acronym
import dev.kensa.sentence.TokenType.Keyword
import dev.kensa.sentence.scanner.Index
import dev.kensa.sentence.scanner.TokenScanner
import java.util.*
import kotlin.collections.ArrayList

class SentenceBuilder(var lastLocation: Location, private val dictionary: Dictionary) {
    private val tokens: MutableList<SentenceToken> = ArrayList()
    private val scanner: TokenScanner = TokenScanner(dictionary)

    fun appendNested(location: Location, placeholder: String, sentences: List<Sentence>) {
        checkLineAndIndent(location)

        val (scanned, indices) = scanner.scan(placeholder, false)
        val scannedPlaceholder = indices.joinToString(separator = " ") { index -> scanned.substring(index.start, index.end) }

        tokens.add(SentenceToken(scannedPlaceholder, setOf(Expandable), nestedTokens = sentences.map { it.tokens }))
    }

    fun appendNumberLiteral(location: Location, value: String) {
        checkLineAndIndent(location)
        append(value, NumberLiteral)
    }

    fun appendNullLiteral(location: Location) {
        checkLineAndIndent(location)
        append("null", NullLiteral)
    }

    fun appendCharacterLiteral(location: Location, value: String) {
        checkLineAndIndent(location)
        append(value.trim('\''), CharacterLiteral)
    }

    fun appendBooleanLiteral(location: Location, value: String) {
        checkLineAndIndent(location)
        append(value, BooleanLiteral)
    }

    fun appendStringLiteral(location: Location, value: String) {
        checkLineAndIndent(location)
        if (dictionary.isAcronym(value)) {
            append(value, StringLiteral, Acronym)
        } else {
            append(value, StringLiteral)
        }
    }

    fun appendOperator(location: Location, value: String) {
        checkLineAndIndent(location)
        append(value, Operator)
    }

    fun appendScenarioIdentifier(location: Location, value: String) {
        checkLineAndIndent(location)
        tokens.add(SentenceToken(value, tokenTypes = setOf(ScenarioValue)))
    }

    fun appendMethodIdentifier(location: Location, value: String) {
        checkLineAndIndent(location)
        tokens.add(SentenceToken(value, tokenTypes = setOf(MethodValue)))
    }

    fun appendFieldIdentifier(location: Location, value: String) {
        checkLineAndIndent(location)
        tokens.add(SentenceToken(value, tokenTypes = setOf(FieldValue)))
    }

    fun appendParameterIdentifier(location: Location, value: String) {
        checkLineAndIndent(location)
        tokens.add(SentenceToken(value, tokenTypes = setOf(ParameterValue)))
    }

    fun appendIdentifier(location: Location, value: String, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) {
        checkLineAndIndent(location)

        val (scanned, indices) = scanner.scan(value, isFirstInSentence())
        indices.forEach { index: Index ->
            append(tokenValueFor(index, scanned.substring(index.start, index.end)), index.type, emphasis = index.emphasisDescriptor ?: emphasisDescriptor)
        }
    }

    fun build(): Sentence = Sentence(tokens)

    private fun isFirstInSentence() =
        tokens.find { it.tokenTypes.any { !it.isWhitespace } } == null

    private fun checkLineAndIndent(location: Location) {
        if (location.lineNumber - lastLocation.lineNumber > 1) {
            append("", BlankLine)
        }
        if (location.lineNumber > lastLocation.lineNumber && location.linePosition > lastLocation.linePosition) {
            append("", NewLine)
            repeat(location.linePosition / Kensa.configuration.tabSize) {
                append("", Indent)
            }
        }
        lastLocation = Location(location.lineNumber, lastLocation.linePosition)
    }

    private fun append(value: String, vararg tokenTypes: TokenType, emphasis: EmphasisDescriptor = EmphasisDescriptor.Default) {
        tokens.add(SentenceToken(value, tokenTypes = setOf(*tokenTypes), emphasis = emphasis))
    }

    private fun tokenValueFor(index: Index, rawToken: String): String =
        when (index.type) {
            Acronym -> rawToken.uppercase(Locale.getDefault())
            Keyword -> rawToken.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            Word -> if (rawToken.length > 1 && rawToken.matches(ALPHANUMERIC_UNDERSCORE)) rawToken else rawToken.replaceFirstChar { it.lowercase(Locale.getDefault()) }

            else -> rawToken
        }

    companion object {
        private val ALPHANUMERIC_UNDERSCORE = "^[A-Z0-9_]+$".toRegex()
    }
}