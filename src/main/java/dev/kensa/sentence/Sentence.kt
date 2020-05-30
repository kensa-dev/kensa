package dev.kensa.sentence

import dev.kensa.sentence.TokenType.Word
import java.util.*

class Sentence(val tokens: List<SentenceToken>) {

    val squashedTokens: List<SentenceToken>
        get() = ArrayList<SentenceToken>().apply {
            var currentTokenTypes: Set<TokenType> = emptySet()
            var currentValue = ""
            for (token in tokens) {
                if (token.hasType(Word)) {
                    currentValue += if (currentTokenTypes.contains(Word)) {
                        " " + token.value
                    } else {
                        token.value
                    }
                } else {
                    if (currentTokenTypes.contains(Word)) {
                        add(Token(currentValue, currentTokenTypes))
                        currentValue = ""
                    }
                    add(token)
                }
                currentTokenTypes = token.tokenTypes
            }
            if (currentTokenTypes.contains(Word)) {
                add(Token(currentValue, currentTokenTypes))
            }
        }
}