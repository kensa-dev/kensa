package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor

fun Token(value: String, vararg tokenTypes: TokenType, nestedTokens: List<List<SentenceToken>> = listOf(emptyList()), emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) = SentenceToken(value, setOf(*tokenTypes), emphasisDescriptor, nestedTokens)
fun Token(value: String, tokenTypes: Set<TokenType>, nestedTokens: List<List<SentenceToken>> = listOf(emptyList()), emphasis: EmphasisDescriptor = EmphasisDescriptor.Default) = SentenceToken(value, tokenTypes, emphasis, nestedTokens)

data class SentenceToken(
        val value: String,
        val tokenTypes: Set<TokenType>,
        val emphasis: EmphasisDescriptor = EmphasisDescriptor.Default,
        val nestedTokens: List<List<SentenceToken>> = listOf(emptyList())
) {

    val cssClasses: Set<String> = (tokenTypes.map { it.asCss() } + emphasis.asCss()).toSet()

    fun hasType(type: TokenType) = tokenTypes.contains(type)
}