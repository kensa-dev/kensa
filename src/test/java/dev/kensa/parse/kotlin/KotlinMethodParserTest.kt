package dev.kensa.parse.kotlin

import dev.kensa.Kensa.konfigure
import dev.kensa.acceptance.example.KotlinTestWithVariousParameterCombinations
import dev.kensa.parse.MethodParserAssertions.assertFieldDescriptors
import dev.kensa.parse.MethodParserAssertions.assertMethodDescriptors
import dev.kensa.parse.ParameterDescriptor
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceTokens.aParameterValueOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.util.Reflect
import org.antlr.v4.runtime.atn.PredictionMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KotlinMethodParserTest {

    private val parser = KotlinFunctionParser()


    @BeforeEach
    internal fun setUp() {
        konfigure {
            antlrPredicationMode = PredictionMode.LL
        }
    }

    @Test
    internal fun parsesTestMethodWithNoParameters() {
        val expectedSentence = Sentence(
            listOf(
                aWordOf("assert"),
                aWordOf("that"),
                aStringLiteralOf("true"),
                aWordOf("is"),
                aWordOf("equal"),
                aWordOf("to"),
                aStringLiteralOf("true")
            )
        )

        val parsedMethod =
            parser.parse(Reflect.findMethod("testWithNoParameters", KotlinTestWithVariousParameterCombinations::class))

        assertThat(parsedMethod).satisfies {
            assertThat(it.name).isEqualTo("testWithNoParameters")
            assertThat(it.parameters.descriptors).isEmpty()
            assertFieldDescriptors(it.fields, KotlinTestWithVariousParameterCombinations::class)
            assertMethodDescriptors(it.methods, KotlinTestWithVariousParameterCombinations::class)
            assertThat(it.nestedSentences).containsKey("nested1")
            assertThat(it.sentences.first().tokens).isEqualTo(expectedSentence.tokens)
        }
    }

    @Test
    internal fun parsesTestMethodWithExtensionParameters() {
        val expectedSentence = Sentence(
            listOf(
                aWordOf("assert"),
                aWordOf("that"),
                aWordOf("first"),
                aWordOf("value"),
                aWordOf("is"),
                aWordOf("equal"),
                aWordOf("to"),
                aWordOf("MY_PARAMETER_VALUE")
            )
        )

        val parsedMethod =
            parser.parse(Reflect.findMethod("testWithExtensionParameter", KotlinTestWithVariousParameterCombinations::class))

        assertThat(parsedMethod).satisfies {
            assertThat(it.name).isEqualTo("testWithExtensionParameter")
            assertThat(it.parameters.descriptors).containsEntry(
                "first",
                ParameterDescriptor("first", 0, false, false)
            )
            assertFieldDescriptors(it.fields, KotlinTestWithVariousParameterCombinations::class)
            assertMethodDescriptors(it.methods, KotlinTestWithVariousParameterCombinations::class)
            assertThat(it.nestedSentences).containsKey("nested1")
            assertThat(it.sentences.first().tokens).isEqualTo(expectedSentence.tokens)
        }
    }

    @Test
    internal fun parsesParameterizedTestMethod() {
        val expectedSentence1 = Sentence(
            listOf(
                aWordOf("assert"),
                aWordOf("that"),
                aWordOf("first"),
                aWordOf("is"),
                aWordOf("in"),
                aStringLiteralOf("a"),
                aStringLiteralOf("b")
            )
        )

        val parsedMethod =
            parser.parse(Reflect.findMethod("parameterizedTest", KotlinTestWithVariousParameterCombinations::class))

        assertThat(parsedMethod).satisfies {
            assertThat(it.name).isEqualTo("parameterizedTest")
            assertThat(it.parameters.descriptors).containsEntry(
                "first",
                ParameterDescriptor("first", 0, false, false)
            )
            assertFieldDescriptors(it.fields, KotlinTestWithVariousParameterCombinations::class)
            assertMethodDescriptors(it.methods, KotlinTestWithVariousParameterCombinations::class)
            assertThat(it.nestedSentences).containsKey("nested1")
            assertThat(it.sentences.first().tokens).isEqualTo(expectedSentence1.tokens)
        }
    }

    @Test
    internal fun parsesParameterizedTestMethodWithExtensionParameters() {
        val expectedSentence1 = Sentence(
            listOf(
                aWordOf("assert"),
                aWordOf("that"),
                aWordOf("first"),
                aWordOf("is"),
                aWordOf("in"),
                aStringLiteralOf("a"),
                aStringLiteralOf("b")
            )
        )
        val expectedSentence2 = Sentence(
            listOf(
                aWordOf("assert"),
                aWordOf("that"),
                aParameterValueOf("second"),
                aWordOf("value"),
                aWordOf("is"),
                aWordOf("equal"),
                aWordOf("to"),
                aWordOf("MY_PARAMETER_VALUE"),
            )
        )

        val parsedMethod =
            parser.parse(
                Reflect.findMethod(
                    "parameterizedTestWithExtensionParameter",
                    KotlinTestWithVariousParameterCombinations::class
                )
            )

        assertThat(parsedMethod).satisfies {
            assertThat(it.name).isEqualTo("parameterizedTestWithExtensionParameter")
            assertThat(it.parameters.descriptors).containsEntry(
                "first",
                ParameterDescriptor("first", 0, false, false)
            )
            assertThat(it.parameters.descriptors).containsEntry(
                "second",
                ParameterDescriptor("second", 1, true, false)
            )
            assertFieldDescriptors(it.fields, KotlinTestWithVariousParameterCombinations::class)
            assertMethodDescriptors(it.methods, KotlinTestWithVariousParameterCombinations::class)
            assertThat(it.nestedSentences).containsKey("nested1")
            assertThat(it.sentences.first().tokens).isEqualTo(expectedSentence1.tokens)
            assertThat(it.sentences.last().tokens).isEqualTo(expectedSentence2.tokens)
        }
    }
}

