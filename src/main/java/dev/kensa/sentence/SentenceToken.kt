package dev.kensa.sentence

fun Token(value: String, vararg tokenTypes: TokenType, nestedTokens: List<List<SentenceToken>> = listOf(emptyList())) = SentenceToken(value, setOf(*tokenTypes), nestedTokens)
fun Token(value: String, tokenTypes: Set<TokenType>, nestedTokens: List<List<SentenceToken>> = listOf(emptyList())) = SentenceToken(value, tokenTypes, nestedTokens)

data class SentenceToken(
        val value: String,
        val tokenTypes: Set<TokenType>,
        val nestedTokens: List<List<SentenceToken>> = listOf(emptyList())
) {

    val tokenTypeNames: Set<String> = tokenTypes.map { it.name }.toSet()

    fun hasType(type: TokenType) = tokenTypes.contains(type)
}