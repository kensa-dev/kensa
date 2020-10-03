package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.parse.EmphasisDescriptor.Companion.Default

data class SentenceToken(
        val value: String,
        val tokenTypes: Set<TokenType>,
        val emphasis: EmphasisDescriptor = Default,
        val nestedTokens: List<List<SentenceToken>> = listOf(emptyList())) {

    val cssClasses: Set<String> = (tokenTypes.map { it.asCss() } + emphasis.asCss()).toSet()

    fun hasType(type: TokenType) = tokenTypes.contains(type)

    companion object {
        operator fun invoke(
                value: String,
                vararg tokenTypes: TokenType,
                nestedTokens: List<List<SentenceToken>> = listOf(emptyList()),
                emphasisDescriptor: EmphasisDescriptor = Default
        ): SentenceToken = SentenceToken(value, setOf(*tokenTypes), emphasisDescriptor, nestedTokens)

        operator fun invoke(
                value: String,
                tokenTypes: Set<TokenType>,
                nestedTokens: List<List<SentenceToken>> = listOf(emptyList()),
                emphasis: EmphasisDescriptor = Default
        ) = SentenceToken(value, tokenTypes, emphasis, nestedTokens)
    }
}