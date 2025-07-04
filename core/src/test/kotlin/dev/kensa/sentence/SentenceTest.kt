package dev.kensa.sentence

import dev.kensa.TextStyle.Italic
import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.SentenceTokens.aBooleanLiteralOf
import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aNullLiteral
import dev.kensa.sentence.SentenceTokens.aStringLiteralAcronymOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anAcronymOf
import dev.kensa.sentence.SentenceTokens.anIdentifierOf
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

internal class SentenceTest {

    @Test
    internal fun squashesTokensContainingMultipleWordsInSingleValues() {
        val expected = listOf(
                aWordOf("Word1 Word2", EmphasisDescriptor.Default),
                aWordOf("Word3 Word4", EmphasisDescriptor(setOf(Italic)))
        )

        val sentence = Sentence(
                listOf(
                        aWordOf("Word1", EmphasisDescriptor.Default),
                        aWordOf("Word2", EmphasisDescriptor.Default),
                        aWordOf("Word3",EmphasisDescriptor(setOf(Italic))),
                        aWordOf("Word4",EmphasisDescriptor(setOf(Italic)))
                )
        )

        sentence.squashedTokens shouldContainExactly expected
    }

    @Test
    fun squashesTokensContainingWordsOnly() {
        val expected: List<SentenceToken> = listOf(aWordOf("Word1 Word2 Word3 Word4", EmphasisDescriptor(setOf(Italic))))
        val sentence = Sentence(
                listOf(
                        aWordOf("Word1",EmphasisDescriptor(setOf(Italic))),
                        aWordOf("Word2",EmphasisDescriptor(setOf(Italic))),
                        aWordOf("Word3",EmphasisDescriptor(setOf(Italic))),
                        aWordOf("Word4",EmphasisDescriptor(setOf(Italic)))
                )
        )

        sentence.squashedTokens shouldContainExactly expected
    }

    @Test
    fun squashesTokensContainingWordsAndOtherTypes() {
        val expected: List<SentenceToken> = listOf(
                anIdentifierOf("P1"),
                aWordOf("Word1 Word2", EmphasisDescriptor(setOf(Italic))),
                aStringLiteralOf("L1"),
                aNullLiteral(),
                aKeywordOf("K1"),
                aStringLiteralAcronymOf("LA1"),
                aNewline(),
                anAcronymOf("FOO"),
                aKeywordOf("K2"),
                aWordOf("Word3 Word4"),
                anAcronymOf("BOO"),
                aBooleanLiteralOf(true)
        )
        val sentence = Sentence(
                listOf(
                        anIdentifierOf("P1"),
                        aWordOf("Word1",EmphasisDescriptor(setOf(Italic))),
                        aWordOf("Word2",EmphasisDescriptor(setOf(Italic))),
                        aStringLiteralOf("L1"),
                        aNullLiteral(),
                        aKeywordOf("K1"),
                        aStringLiteralAcronymOf("LA1"),
                        aNewline(),
                        anAcronymOf("FOO"),
                        aKeywordOf("K2"),
                        aWordOf("Word3"),
                        aWordOf("Word4"),
                        anAcronymOf("BOO"),
                        aBooleanLiteralOf(true)
                ))

        sentence.squashedTokens shouldContainExactly expected
    }
}