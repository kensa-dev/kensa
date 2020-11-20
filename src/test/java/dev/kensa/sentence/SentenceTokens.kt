package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.TokenType.*
import dev.kensa.sentence.TokenType.Acronym

object SentenceTokens {

    fun aWordOf(value: String, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) = SentenceToken(value, tokenTypes = arrayOf(Word), emphasisDescriptor = emphasisDescriptor)

    fun aLiteralOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(Literal))

    fun anExpandableOf(value: String, tokens: List<List<SentenceToken>>): SentenceToken = SentenceToken(value, tokenTypes = arrayOf(), nestedTokens = tokens)

    fun aNewline() = SentenceToken("", tokenTypes = arrayOf(NewLine))

    fun anIndent() = SentenceToken("", tokenTypes = arrayOf(Indent))

    fun aStringLiteralOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(StringLiteral))

    fun aStringLiteralAcronymOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(StringLiteral, Acronym))

    fun anAcronymOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(Acronym))

    fun aKeywordOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(Keyword))

    fun anIdentifierOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(Identifier))

    fun aScenarioIdentifierOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(ScenarioValue))

    fun aFieldIdentifierOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(FieldValue))

    fun aMethodIdentifierOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(MethodValue))

    fun aParameterIdentifierOf(value: String) = SentenceToken(value, tokenTypes = arrayOf(ParameterValue))
}