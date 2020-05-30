package dev.kensa.sentence

import dev.kensa.sentence.Acronym.Companion.of
import dev.kensa.sentence.SentenceTokens.aFieldIdentifierOf
import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aLiteralOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aParameterIdentifierOf
import dev.kensa.sentence.SentenceTokens.aScenarioIdentifierOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anAcronymOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SentenceBuilderTest {
    private lateinit var builder: SentenceBuilder

    @BeforeEach
    fun setUp() {
        val dictionary = Dictionary()
        dictionary.putAcronyms(
                simpleAcronymOf("FOO"),
                simpleAcronymOf("BAR"),
                simpleAcronymOf("LA1"),
                simpleAcronymOf("HA1")
        )

        builder = SentenceBuilder(1, dictionary.keywords, dictionary.acronymStrings)
    }

    @Test
    fun canConstructASentenceFromVariousValueTypes() {
        builder.apply {
            appendIdentifier(1, "givenFOOMooBar")
            appendStringLiteral(1, "stringLiteral1")
            appendLiteral(1, "10")
            appendScenarioIdentifier(2, "scenario.call")
            appendFieldIdentifier(2, "fieldName")
            appendParameterIdentifier(2, "parameterName")
        }

        assertThat(builder.build().tokens)
                .containsExactly(
                        aKeywordOf("Given"),
                        anAcronymOf("FOO"),
                        aWordOf("moo"),
                        anAcronymOf("BAR"),
                        aStringLiteralOf("stringLiteral1"),
                        aLiteralOf("10"),
                        aNewline(),
                        aScenarioIdentifierOf("scenario.call"),
                        aFieldIdentifierOf("fieldName"),
                        aParameterIdentifierOf("parameterName")
                )
    }

    private fun simpleAcronymOf(acronym: String) = of(acronym, "")
}