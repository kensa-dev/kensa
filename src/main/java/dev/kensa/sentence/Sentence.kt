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

            fun finishCurrentWord() {
                if (currentTokenTypes.contains(Word)) {
                    add(Token(currentValue, currentTokenTypes, emphasis = currentEmphasis))
                    currentValue = ""
                    currentEmphasis = EmphasisDescriptor.Default
                }
            }

            tokens.forEach { token ->
                if (token.hasType(Word)) {
                    currentValue = if (currentTokenTypes.contains(Word)) {
                        if (currentEmphasis == token.emphasis) {
                            "$currentValue ${token.value}"
                        } else {
                            finishCurrentWord()
                            token.value
                        }
                    } else {
                        token.value
                    }
                } else {
                    finishCurrentWord()
                    add(token)
                }
                currentTokenTypes = token.tokenTypes
                currentEmphasis = token.emphasis
            }
            finishCurrentWord()
        }
}