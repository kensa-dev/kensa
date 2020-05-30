package dev.kensa.sentence

import dev.kensa.sentence.TokenType.*
import dev.kensa.sentence.TokenType.Acronym

object SentenceTokens {

    fun aWordOf(value: String) = Token(value, Word)

    fun aLiteralOf(value: String) = Token(value, Literal)

    fun anExpandableOf(value: String, tokens: List<List<SentenceToken>>): SentenceToken = Token(value, nestedTokens = tokens)

    fun aNewline() = Token("", NewLine)

    fun aStringLiteralOf(value: String) = Token(value, StringLiteral)

    fun aStringLiteralAcronymOf(value: String) = Token(value, StringLiteral, Acronym)

    fun anAcronymOf(value: String) = Token(value, Acronym)

    fun aKeywordOf(value: String) = Token(value, Keyword)

    fun anIdentifierOf(value: String) = Token(value, Identifier)

    fun aScenarioIdentifierOf(value: String) = Token(value, ScenarioValue)

    fun aFieldIdentifierOf(value: String) = Token(value, FieldValue)

    fun aParameterIdentifierOf(value: String) = Token(value, ParameterValue)
}