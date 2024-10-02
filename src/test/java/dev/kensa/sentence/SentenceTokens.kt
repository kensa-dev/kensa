package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.TokenType.*
import dev.kensa.sentence.TokenType.Acronym
import dev.kensa.sentence.TokenType.ProtectedPhrase
import dev.kensa.sentence.TokenType.Keyword

object SentenceTokens {

    fun aWordOf(value: String, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) = SentenceToken(value, tokenTypes = setOf(Word), emphasis = emphasisDescriptor)

    fun aNumberLiteralOf(value: String) = SentenceToken(value, tokenTypes = setOf(NumberLiteral))

    fun aBooleanLiteralOf(value: Boolean) = SentenceToken(value.toString(), tokenTypes = setOf(BooleanLiteral))

    fun aCharacterLiteralOf(value: Char) = SentenceToken(value.toString(), tokenTypes = setOf(CharacterLiteral))

    fun aNullLiteral() = SentenceToken("null", tokenTypes = setOf(NullLiteral))

    fun anExpandableOf(value: String, tokens: List<List<SentenceToken>>): SentenceToken = SentenceToken(value, tokenTypes = setOf(), nestedTokens = tokens)

    fun aProtectedPhraseOf(value: String, emphasisDescriptor: EmphasisDescriptor) : SentenceToken = SentenceToken(value = value, tokenTypes = setOf(ProtectedPhrase), emphasis = emphasisDescriptor)

    fun aNewline() = SentenceToken("", tokenTypes = setOf(NewLine))

    fun anIndent() = SentenceToken("", tokenTypes = setOf(Indent))

    fun aStringLiteralOf(value: String) = SentenceToken(value, tokenTypes = setOf(StringLiteral))

    fun aStringLiteralAcronymOf(value: String) = SentenceToken(value, tokenTypes = setOf(StringLiteral, Acronym))

    fun anAcronymOf(value: String) = SentenceToken(value, tokenTypes = setOf(Acronym))

    fun aKeywordOf(value: String) = SentenceToken(value, tokenTypes = setOf(Keyword))

    fun anIdentifierOf(value: String) = SentenceToken(value, tokenTypes = setOf(Identifier))

    fun aScenarioValueOf(value: String) = SentenceToken(value, tokenTypes = setOf(ScenarioValue))

    fun aFieldIdentifierOf(value: String) = SentenceToken(value, tokenTypes = setOf(FieldValue))

    fun aMethodIdentifierOf(value: String) = SentenceToken(value, tokenTypes = setOf(MethodValue))

    fun aParameterValueOf(value: String) = SentenceToken(value, tokenTypes = setOf(ParameterValue))
}