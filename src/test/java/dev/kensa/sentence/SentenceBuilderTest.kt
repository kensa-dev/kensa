package dev.kensa.sentence

import dev.kensa.Colour.TextDanger
import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.parse.Event.Location
import dev.kensa.sentence.Acronym.Companion.of
import dev.kensa.sentence.SentenceTokens.aFieldIdentifierOf
import dev.kensa.sentence.SentenceTokens.aProtectedPhraseOf
import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aNumberLiteralOf
import dev.kensa.sentence.SentenceTokens.aMethodIdentifierOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aParameterValueOf
import dev.kensa.sentence.SentenceTokens.aScenarioValueOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anAcronymOf
import dev.kensa.sentence.SentenceTokens.anIndent
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SentenceBuilderTest {
    private lateinit var builder: SentenceBuilder

    @BeforeEach
    fun setUp() {
        val dictionary = Dictionary().apply {
            putProtectedPhrases(ProtectedPhrase("protectedPhrase", EmphasisDescriptor(textColour = TextDanger)))
            putAcronyms(
                simpleAcronymOf("FOO"),
                simpleAcronymOf("BAR"),
                simpleAcronymOf("LA1"),
                simpleAcronymOf("HA1")
            )
        }

        builder = SentenceBuilder(Location(1, 0), dictionary)
    }

    @Test
    fun canConstructASentenceFromVariousValueTypes() {
        builder.apply {
            appendIdentifier(Location(1, 0), value = "")
            appendIdentifier(Location(2, 5), value = "givenFOOMooBarZOO")
            appendIdentifier(Location(2, 0), value = "andFOO")
            appendStringLiteral(Location(2, 0), "stringLiteral1")
            appendNumberLiteral(Location(2, 0), "10")
            appendScenarioIdentifier(Location(3, 0), "scenario.call")
            appendFieldIdentifier(Location(3, 0), "fieldName")
            appendMethodIdentifier(Location(3, 0), "methodName")
            appendParameterIdentifier(Location(3, 0), "parameterName")
            appendIdentifier(Location(4, 25), value = "sendsAThing")
            appendIdentifier(Location(5, 0), value = "somethingA_CONSTANT_019")
            appendIdentifier(Location(6, 0), value = "protectedPhrase")
        }

        builder.build().tokens.shouldContainExactly(
            aNewline(),
            anIndent(),
            aKeywordOf("Given"),
            anAcronymOf("FOO"),
            aWordOf("moo"),
            anAcronymOf("BAR"),
            aWordOf("ZOO"),
            aWordOf("and"),
            anAcronymOf("FOO"),
            aStringLiteralOf("stringLiteral1"),
            aNumberLiteralOf("10"),
            aScenarioValueOf("scenario.call"),
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
            aWordOf("A_CONSTANT_019"),
            aProtectedPhraseOf("protectedPhrase", EmphasisDescriptor(textColour = TextDanger))
        )
    }

    private fun simpleAcronymOf(acronym: String) = of(acronym, "")
}