package dev.kensa.sentence

import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aLiteralOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aStringLiteralAcronymOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anAcronymOf
import dev.kensa.sentence.SentenceTokens.anIdentifierOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SentenceTest {

    @Test
    fun squashesTokensContainingWordsOnly() {
        val expected: List<SentenceToken> = listOf(aWordOf("Word1 Word2 Word3 Word4"))
        val sentence = Sentence(
                listOf(
                        aWordOf("Word1"),
                        aWordOf("Word2"),
                        aWordOf("Word3"),
                        aWordOf("Word4")
                )
        )

        assertThat(sentence.squashedTokens).containsExactlyElementsOf(expected)
    }

    @Test
    fun squashesTokensContainingWordsAndOtherTypes() {
        val expected: List<SentenceToken> = listOf(
                anIdentifierOf("P1"),
                aWordOf("Word1 Word2"),
                aStringLiteralOf("L1"),
                aLiteralOf("null"),
                aKeywordOf("K1"),
                aStringLiteralAcronymOf("LA1"),
                aNewline(),
                anAcronymOf("FOO"),
                aKeywordOf("K2"),
                aWordOf("Word3 Word4"),
                anAcronymOf("BOO"),
                aLiteralOf("true")
        )
        val sentence = Sentence(
                listOf(
                        anIdentifierOf("P1"),
                        aWordOf("Word1"),
                        aWordOf("Word2"),
                        aStringLiteralOf("L1"),
                        aLiteralOf("null"),
                        aKeywordOf("K1"),
                        aStringLiteralAcronymOf("LA1"),
                        aNewline(),
                        anAcronymOf("FOO"),
                        aKeywordOf("K2"),
                        aWordOf("Word3"),
                        aWordOf("Word4"),
                        anAcronymOf("BOO"),
                        aLiteralOf("true")
                ))

        assertThat(sentence.squashedTokens).containsExactlyElementsOf(expected)
    }
}