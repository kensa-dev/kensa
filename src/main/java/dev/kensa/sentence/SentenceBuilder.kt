package dev.kensa.sentence

import dev.kensa.Kensa
import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.TokenType.*
import dev.kensa.sentence.TokenType.Acronym
import dev.kensa.sentence.scanner.Index
import dev.kensa.sentence.scanner.TokenScanner
import java.util.*

class SentenceBuilder(private var lastLineNumber: Int, keywords: Set<String>, private val acronyms: Set<String>) {
    private val tokens: MutableList<SentenceToken> = ArrayList()
    private val scanner: TokenScanner = TokenScanner(keywords, acronyms)

    fun appendNested(location: Pair<Int, Int>, placeholder: String, sentences: List<Sentence>) {
        checkLineAndIndent(location)

        val scannedPlaceholder = scanner.scan(placeholder).joinToString(separator = " ") { index -> placeholder.substring(index.start, index.end) }

        tokens.add(SentenceToken(scannedPlaceholder, setOf(Expandable), nestedTokens = sentences.map { it.tokens }))
    }

    fun appendLiteral(location: Pair<Int, Int>, value: String) {
        checkLineAndIndent(location)
        append(value, Literal)
    }

    fun appendStringLiteral(location: Pair<Int, Int>, value: String) {
        checkLineAndIndent(location)
        if (isAcronym(value)) {
            append(value, StringLiteral, Acronym)
        } else {
            append(value, StringLiteral)
        }
    }

    fun appendScenarioIdentifier(location: Pair<Int, Int>, value: String) {
        checkLineAndIndent(location)
        tokens.add(SentenceToken(value, tokenTypes = arrayOf(ScenarioValue)))
    }

    fun appendFieldIdentifier(location: Pair<Int, Int>, value: String) {
        checkLineAndIndent(location)
        tokens.add(SentenceToken(value, tokenTypes = arrayOf(FieldValue)))
    }

    fun appendParameterIdentifier(location: Pair<Int, Int>, value: String) {
        checkLineAndIndent(location)
        tokens.add(SentenceToken(value, tokenTypes = arrayOf(ParameterValue)))
    }

    fun appendIdentifier(location: Pair<Int, Int>, value: String, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) {
        checkLineAndIndent(location)
        scanner.scan(value).forEach { index: Index ->
            append(tokenValueFor(index, value.substring(index.start, index.end)), index.type, emphasisDescriptor = emphasisDescriptor)
        }
    }

    fun build(): Sentence = Sentence(tokens)

    private fun checkLineAndIndent(location: Pair<Int, Int>) {
        if (location.first > lastLineNumber) {
            lastLineNumber = location.first
            append("", NewLine)
            repeat(location.second / Kensa.configuration.tabSize) {
                append("", Indent)
            }
        }
    }

    private fun append(value: String, vararg tokenTypes: TokenType, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) {
        tokens.add(SentenceToken(value, tokenTypes = tokenTypes, emphasisDescriptor = emphasisDescriptor))
    }

    private fun tokenValueFor(index: Index, rawToken: String): String {
        var tokenValue = rawToken
        if (index.type == Acronym) {
            tokenValue = tokenValue.toUpperCase()
        } else if (index.type === Keyword) {
            if (tokens.size == 0) {
                tokenValue = Character.toUpperCase(rawToken[0]).toString() + rawToken.substring(1)
            }
        } else if (index.type === Word) {
            tokenValue = if (tokenValue.length > 1 && tokenValue.matches("^[A-Z0-9_]+$".toRegex())) {
                tokenValue
            } else {
                Character.toLowerCase(rawToken[0]).toString() + rawToken.substring(1)
            }

        }
        return tokenValue
    }

    private fun isAcronym(value: String) = acronyms.any { it == value }
}