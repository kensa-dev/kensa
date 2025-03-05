package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.parse.EmphasisDescriptor.Companion.Default
import dev.kensa.parse.HighlightDescriptor

data class SentenceToken(
    val value: String,
    val tokenTypes: Set<TokenType>,
    val emphasis: EmphasisDescriptor = Default,
    val highlight: HighlightDescriptor? = null,
    val nestedTokens: List<List<SentenceToken>> = listOf(emptyList())) {

    val cssClasses: Set<String> = (tokenTypes.map { it.asCss() } + emphasis.asCss()).toSet() + (highlight?.asCss() ?: emptySet())

    val sourceHint: String? = highlight?.name
    
    fun hasType(type: TokenType) = tokenTypes.contains(type)

    companion object {
        operator fun invoke(
                value: String,
                tokenTypes: Set<TokenType>,
                nestedTokens: List<List<SentenceToken>> = listOf(emptyList()),
                emphasis: EmphasisDescriptor = Default,
                highlight: HighlightDescriptor? = null,
        ) = SentenceToken(value, tokenTypes, emphasis, highlight, nestedTokens)
    }
}