package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.TokenType.Word
import java.util.*

class Sentence(val tokens: List<SentenceToken>) {

    val squashedTokens: List<SentenceToken>
        get() = ArrayList<SentenceToken>().apply {
            var currentTokenTypes: Set<TokenType> = emptySet()
            var currentValue = ""
            var currentEmphasis = EmphasisDescriptor.Default
            for (token in tokens) {
                if (token.hasType(Word)) {
                    currentValue += if (currentTokenTypes.contains(Word) && currentEmphasis == token.emphasis) {
                        " " + token.value
                    } else {
                        token.value
                    }
                } else {
                    if (currentTokenTypes.contains(Word)) {
                        add(Token(currentValue, currentTokenTypes, emphasis = currentEmphasis))
                        currentValue = ""
                    }
                    add(token)
                }
                currentTokenTypes = token.tokenTypes
                currentEmphasis = token.emphasis
            }
            if (currentTokenTypes.contains(Word)) {
                add(Token(currentValue, currentTokenTypes, emphasis = currentEmphasis))
            }
        }
}