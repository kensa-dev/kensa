package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor

object SentenceTokens {

    fun aWordOf(value: String, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Companion.Default) = SentenceToken(value, tokenTypes = setOf(TokenType.Word), emphasis = emphasisDescriptor)

    fun aNumberLiteralOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.NumberLiteral))

    fun aBooleanLiteralOf(value: Boolean) = SentenceToken(value.toString(), tokenTypes = setOf(TokenType.BooleanLiteral))

    fun aCharacterLiteralOf(value: Char) = SentenceToken(value.toString(), tokenTypes = setOf(TokenType.CharacterLiteral))

    fun aNullLiteral() = SentenceToken("null", tokenTypes = setOf(TokenType.NullLiteral))

    fun anExpandableOf(value: String, tokens: List<List<SentenceToken>>): SentenceToken = SentenceToken(value, tokenTypes = setOf(), nestedTokens = tokens)

    fun aProtectedPhraseOf(value: String, emphasisDescriptor: EmphasisDescriptor) : SentenceToken = SentenceToken(value = value, tokenTypes = setOf(TokenType.ProtectedPhrase), emphasis = emphasisDescriptor)

    fun aNewline() = SentenceToken("", tokenTypes = setOf(TokenType.NewLine))

    fun anIndent() = SentenceToken("", tokenTypes = setOf(TokenType.Indent))

    fun aStringLiteralOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.StringLiteral))

    fun aStringLiteralAcronymOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.StringLiteral, TokenType.Acronym))

    fun anAcronymOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.Acronym))

    fun aKeywordOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.Keyword))

    fun anIdentifierOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.Identifier))

    fun aScenarioValueOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.ScenarioValue))

    fun aFixturesValueOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.FixturesValue))

    fun aFieldIdentifierOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.FieldValue))

    fun aMethodIdentifierOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.MethodValue))

    fun aParameterValueOf(value: String) = SentenceToken(value, tokenTypes = setOf(TokenType.ParameterValue))
}