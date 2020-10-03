package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.TokenType.*
import dev.kensa.sentence.TokenType.Acronym
import dev.kensa.sentence.scanner.Index
import dev.kensa.sentence.scanner.TokenScanner
import java.util.*

class SentenceBuilder(private var lastLineNumber: Int, keywords: Set<String>, private val acronyms: Set<String>) {
    private val tokens: MutableList<SentenceToken> = ArrayList()
    private val scanner: TokenScanner = TokenScanner(keywords, acronyms)

    fun appendNested(lineNumber: Int, placeholder: String, sentences: List<Sentence>) {
        markLineNumber(lineNumber)

        val scannedPlaceholder = scanner.scan(placeholder).joinToString(separator = " ") { index -> placeholder.substring(index.start, index.end) }

        tokens.add(SentenceToken(scannedPlaceholder, setOf(Expandable), nestedTokens = sentences.map { it.tokens }))
    }

    fun appendLiteral(lineNumber: Int, value: String) {
        markLineNumber(lineNumber)
        append(value, Literal)
    }

    fun appendStringLiteral(lineNumber: Int, value: String) {
        markLineNumber(lineNumber)
        if (isAcronym(value)) {
            append(value, StringLiteral, Acronym)
        } else {
            append(value, StringLiteral)
        }
    }

    fun appendScenarioIdentifier(lineNumber: Int, value: String) {
        markLineNumber(lineNumber)
        tokens.add(SentenceToken(value, tokenTypes = arrayOf(ScenarioValue)))
    }

    fun appendFieldIdentifier(lineNumber: Int, value: String) {
        markLineNumber(lineNumber)
        tokens.add(SentenceToken(value, tokenTypes = arrayOf(FieldValue)))
    }

    fun appendParameterIdentifier(lineNumber: Int, value: String) {
        markLineNumber(lineNumber)
        tokens.add(SentenceToken(value, tokenTypes = arrayOf(ParameterValue)))
    }

    fun appendIdentifier(lineNumber: Int, value: String, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) {
        markLineNumber(lineNumber)
        scanner.scan(value).forEach { index: Index ->
            append(tokenValueFor(index, value.substring(index.start, index.end)), index.type, emphasisDescriptor = emphasisDescriptor)
        }
    }

    fun build(): Sentence = Sentence(tokens)

    private fun markLineNumber(thisLineNumber: Int) {
        if (thisLineNumber > lastLineNumber) {
            lastLineNumber = thisLineNumber
            appendNewLine()
        }
    }

    private fun append(value: String, vararg tokenTypes: TokenType, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) {
        tokens.add(SentenceToken(value, tokenTypes = tokenTypes, emphasisDescriptor = emphasisDescriptor))
    }

    private fun appendNewLine() {
        append("", NewLine)
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