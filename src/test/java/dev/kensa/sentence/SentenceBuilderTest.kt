package dev.kensa.sentence

import dev.kensa.sentence.Acronym.Companion.of
import dev.kensa.sentence.SentenceTokens.aFieldIdentifierOf
import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aLiteralOf
import dev.kensa.sentence.SentenceTokens.aMethodIdentifierOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aParameterIdentifierOf
import dev.kensa.sentence.SentenceTokens.aScenarioIdentifierOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anAcronymOf
import dev.kensa.sentence.SentenceTokens.anIndent
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
            appendIdentifier(Pair(1, 0), value = "givenFOOMooBarZOO")
            appendStringLiteral(Pair(1, 0), "stringLiteral1")
            appendLiteral(Pair(1, 0), "10")
            appendScenarioIdentifier(Pair(2, 0), "scenario.call")
            appendFieldIdentifier(Pair(2, 0), "fieldName")
            appendMethodIdentifier(Pair(2, 0), "methodName")
            appendParameterIdentifier(Pair(2, 0), "parameterName")
            appendIdentifier(Pair(3, 25), value = "sendsAThing")
            appendIdentifier(Pair(4, 0), value = "somethingA_CONSTANT_019")
        }

        assertThat(builder.build().tokens)
                .containsExactly(
                        aKeywordOf("Given"),
                        anAcronymOf("FOO"),
                        aWordOf("moo"),
                        anAcronymOf("BAR"),
                        aWordOf("ZOO"),
                        aStringLiteralOf("stringLiteral1"),
                        aLiteralOf("10"),
                        aNewline(),
                        aScenarioIdentifierOf("scenario.call"),
                        aFieldIdentifierOf("fieldName"),
                        aMethodIdentifierOf("methodName"),
                        aParameterIdentifierOf("parameterName"),
                        aNewline(),
                        anIndent(),
                        anIndent(),
                        anIndent(),
                        anIndent(),
                        anIndent(),
                        aWordOf("sends"),
                        aWordOf("a"),
                        aWordOf("thing"),
                        aNewline(),
                        aWordOf("something"),
                        aWordOf("A_CONSTANT_019")
                )
    }

    private fun simpleAcronymOf(acronym: String) = of(acronym, "")
}