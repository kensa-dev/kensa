package dev.kensa.parse.kotlin

import dev.kensa.Kensa.konfigure
import dev.kensa.acceptance.example.KotlinTestWithVariousParameterCombinations
import dev.kensa.parse.MethodParserAssertions.assertFieldDescriptors
import dev.kensa.parse.MethodParserAssertions.assertMethodDescriptors
import dev.kensa.parse.ParameterDescriptor
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceTokens
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aParameterValueOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anIndent
import dev.kensa.util.findMethod
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
    internal fun parsesTestMethodWhenOtherMethodsHaveSimilarNames() {
        val expectedSentence = Sentence(
            listOf(
                aWordOf("assert"),
                aWordOf("that"),
                aStringLiteralOf("string"),
                aWordOf("is"),
                aWordOf("not"),
                aWordOf("blank"),
            )
        )

        val expectedNestedSentence = Sentence(
            listOf(
                aWordOf("some"),
                aWordOf("builder"),
                aNewline(),
                anIndent(),
                anIndent(),
                SentenceTokens.aKeywordOf("With"),
                aWordOf("something"),
                aNewline(),
                anIndent(),
                anIndent(),
                aWordOf("build")
            )
        )

        val parsedMethod =
            parser.parse(KotlinTestWithVariousParameterCombinations::class.java.findMethod("similarNameTest1"))

        with(parsedMethod) {
            assertThat(name).isEqualTo("similarNameTest1")
            assertThat(parameters.descriptors).isEmpty()
            assertFieldDescriptors(fields, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)
            assertThat(nestedSentences).containsKey("nested1")
            assertThat(nestedSentences["nested1"]?.first()?.tokens).isEqualTo(expectedNestedSentence.tokens)
            assertThat(sentences.first().tokens).isEqualTo(expectedSentence.tokens)
        }
    }

    @Test
    internal fun parsesInternalTestMethod() {
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
            parser.parse(KotlinTestWithVariousParameterCombinations::class.java.findMethod("internalTest\$kensa"))

        with(parsedMethod) {
            assertThat(name).isEqualTo("internalTest")
            assertThat(parameters.descriptors).isEmpty()
            assertFieldDescriptors(fields, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)
            assertThat(nestedSentences).containsKey("nested1")
            assertThat(sentences.first().tokens).isEqualTo(expectedSentence.tokens)
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
            parser.parse(KotlinTestWithVariousParameterCombinations::class.java.findMethod("testWithNoParameters"))

        with(parsedMethod) {
            assertThat(name).isEqualTo("testWithNoParameters")
            assertThat(parameters.descriptors).isEmpty()
            assertFieldDescriptors(fields, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)
            assertThat(nestedSentences).containsKey("nested1")
            assertThat(sentences.first().tokens).isEqualTo(expectedSentence.tokens)
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
            parser.parse(KotlinTestWithVariousParameterCombinations::class.java.findMethod("testWithExtensionParameter"))

        with(parsedMethod) {
            assertThat(name).isEqualTo("testWithExtensionParameter")
            assertThat(parameters.descriptors).containsEntry(
                "first",
                ParameterDescriptor("first", 0, isSentenceValue = false, isHighlighted = false, isCaptured = true)
            )
            assertFieldDescriptors(fields, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)
            assertThat(nestedSentences).containsKey("nested1")
            assertThat(sentences.first().tokens).isEqualTo(expectedSentence.tokens)
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
            parser.parse(KotlinTestWithVariousParameterCombinations::class.java.findMethod("parameterizedTest"))

        with(parsedMethod) {
            assertThat(name).isEqualTo("parameterizedTest")
            assertThat(parameters.descriptors).containsEntry(
                "first",
                ParameterDescriptor("first", 0, isSentenceValue = false, isHighlighted = false, isCaptured = true)
            )
            assertFieldDescriptors(fields, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)
            assertThat(nestedSentences).containsKey("nested1")
            assertThat(sentences.first().tokens).isEqualTo(expectedSentence1.tokens)
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
            parser.parse(KotlinTestWithVariousParameterCombinations::class.java.findMethod("parameterizedTestWithExtensionParameter"))

        with(parsedMethod) {
            assertThat(name).isEqualTo("parameterizedTestWithExtensionParameter")
            assertThat(parameters.descriptors).containsEntry(
                "first",
                ParameterDescriptor("first", 0, isSentenceValue = false, isHighlighted = false, isCaptured = true)
            )
            assertThat(parameters.descriptors).containsEntry(
                "second",
                ParameterDescriptor("second", 1, isSentenceValue = true, isHighlighted = false, isCaptured = true)
            )
            assertFieldDescriptors(fields, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)
            assertThat(nestedSentences).containsKey("nested1")
            assertThat(sentences.first().tokens).isEqualTo(expectedSentence1.tokens)
            assertThat(sentences.last().tokens).isEqualTo(expectedSentence2.tokens)
        }
    }
}