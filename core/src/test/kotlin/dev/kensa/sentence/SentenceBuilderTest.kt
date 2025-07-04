package dev.kensa.sentence

import dev.kensa.Colour.TextDanger
import dev.kensa.Configuration
import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.parse.Location
import dev.kensa.sentence.Acronym.Companion.of
import dev.kensa.sentence.SentenceTokens.aFieldValueOf
import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aMethodValueOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aNumberLiteralOf
import dev.kensa.sentence.SentenceTokens.aParameterValueOf
import dev.kensa.sentence.SentenceTokens.aProtectedPhraseOf
//import dev.kensa.sentence.SentenceTokens.aScenarioValueOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anAcronymOf
import dev.kensa.sentence.SentenceTokens.anIndent
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SentenceBuilderTest {
    private lateinit var builder: SentenceBuilder

    private val configuration = Configuration()

    @BeforeEach
    fun setUp() {
        configuration.apply {
            protectedPhrases(ProtectedPhrase("protectedPhrase", EmphasisDescriptor(textColour = TextDanger)))
            acronyms(
                simpleAcronymOf("FOO"),
                simpleAcronymOf("BAR"),
                simpleAcronymOf("LA1"),
                simpleAcronymOf("HA1")
            )
        }
        builder = SentenceBuilder(Location(1, 0), configuration.dictionary, configuration.tabSize)
    }

    @Test
    fun canConstructASentenceFromVariousValueTypes() {
        builder.apply {
            appendIdentifier(Location(1, 0), value = "")
            appendIdentifier(Location(2, 5), value = "givenFOOMooBarZOO")
            appendIdentifier(Location(2, 0), value = "andFOO")
            appendStringLiteral(Location(2, 0), "stringLiteral1")
            appendNumberLiteral(Location(2, 0), "10")
            appendFieldValue(Location(3, 0), "scenario", "call")
            appendFieldValue(Location(3, 0), "fieldName", "")
            appendMethodValue(Location(3, 0), "methodName", "")
            appendParameterValue(Location(3, 0), "parameterName", "")
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
            aFieldValueOf("scenario:call"),
            aFieldValueOf("fieldName:"),
            aMethodValueOf("methodName:"),
            aParameterValueOf("parameterName:"),
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