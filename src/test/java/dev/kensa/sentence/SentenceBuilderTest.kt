package dev.kensa.sentence

import dev.kensa.parse.Event.Location
import dev.kensa.sentence.Acronym.Companion.of
import dev.kensa.sentence.SentenceTokens.aFieldIdentifierOf
import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aLiteralOf
import dev.kensa.sentence.SentenceTokens.aMethodIdentifierOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aParameterValueOf
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

        builder = SentenceBuilder(Location(1, 0), dictionary)
    }

    @Test
    fun canConstructASentenceFromVariousValueTypes() {
        builder.apply {
            appendIdentifier(Location(1, 0), value = "givenFOOMooBarZOO")
            appendStringLiteral(Location(1, 0), "stringLiteral1")
            appendLiteral(Location(1, 0), "10")
            appendScenarioIdentifier(Location(2, 0), "scenario.call")
            appendFieldIdentifier(Location(2, 0), "fieldName")
            appendMethodIdentifier(Location(2, 0), "methodName")
            appendParameterIdentifier(Location(2, 0), "parameterName")
            appendIdentifier(Location(3, 25), value = "sendsAThing")
            appendIdentifier(Location(4, 0), value = "somethingA_CONSTANT_019")
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
                        aScenarioIdentifierOf("scenario.call"),
                        aFieldIdentifierOf("fieldName"),
                        aMethodIdentifierOf("methodName"),
                        aParameterValueOf("parameterName"),
                        aNewline(),
                        anIndent(),
                        anIndent(),
                        anIndent(),
                        anIndent(),
                        anIndent(),
                        aWordOf("sends"),
                        aWordOf("a"),
                        aWordOf("thing"),
                        aWordOf("something"),
                        aWordOf("A_CONSTANT_019")
                )
    }

    private fun simpleAcronymOf(acronym: String) = of(acronym, "")
}