package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.parse.HighlightDescriptor
import dev.kensa.sentence.TokenType.Word
import java.util.*

class Sentence(val tokens: List<SentenceToken>) {

    val squashedTokens: List<SentenceToken>
        get() = ArrayList<SentenceToken>().apply {
            var currentTokenTypes: Set<TokenType> = emptySet()
            var currentValue = ""
            var currentEmphasis = EmphasisDescriptor.Default
            var currentHighlight: HighlightDescriptor? = null

            fun finishCurrentWord() {
                if (currentTokenTypes.contains(Word)) {
                    add(SentenceToken(currentValue, currentTokenTypes, emphasis = currentEmphasis, highlight = currentHighlight))
                    currentValue = ""
                    currentEmphasis = EmphasisDescriptor.Default
                    currentHighlight = null
                }
            }

            tokens.forEach { token ->
                if (token.hasType(Word)) {
                    currentValue = if (currentTokenTypes.contains(Word)) {
                        if (currentEmphasis == token.emphasis && currentHighlight == token.highlight) {
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
                currentHighlight = token.highlight
            }
            finishCurrentWord()
        }
}