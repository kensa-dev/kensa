package dev.kensa.parse.java

import dev.kensa.acceptance.example.JavaTestWithVariousParameterCombinations
import dev.kensa.parse.MethodParserAssertions.assertFieldDescriptors
import dev.kensa.parse.MethodParserAssertions.assertMethodDescriptors
import dev.kensa.parse.ParameterDescriptor
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aParameterValueOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anIndent
import dev.kensa.util.Reflect
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JavaMethodParserTest {

    private val parser = JavaMethodParser()

    @Test
    internal fun `parses test method when other methods have similar names`() {
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
                anIndent(),
                aKeywordOf("With"),
                aWordOf("something"),
                aNewline(),
                anIndent(),
                anIndent(),
                anIndent(),
                aWordOf("build")
            )
        )

        val parsedMethod =
            parser.parse(Reflect.findMethod("similarNameTest1", JavaTestWithVariousParameterCombinations::class))

        with(parsedMethod) {
            assertThat(name).isEqualTo("similarNameTest1")
            assertThat(parameters.descriptors).isEmpty()
            assertFieldDescriptors(fields, JavaTestWithVariousParameterCombinations::class)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class)
            assertThat(nestedSentences).containsKey("nested1")
            assertThat(nestedSentences["nested1"]?.first()?.tokens).isEqualTo(expectedNestedSentence.tokens)
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
            parser.parse(Reflect.findMethod("testWithNoParameters", JavaTestWithVariousParameterCombinations::class))

        with(parsedMethod) {
            assertThat(name).isEqualTo("testWithNoParameters")
            assertThat(parameters.descriptors).isEmpty()
            assertFieldDescriptors(fields, JavaTestWithVariousParameterCombinations::class)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class)
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
                aWordOf("get"),
                aWordOf("value"),
                aWordOf("is"),
                aWordOf("equal"),
                aWordOf("to"),
                aWordOf("MY_PARAMETER_VALUE")
            )
        )

        val parsedMethod =
            parser.parse(Reflect.findMethod("testWithExtensionParameter", JavaTestWithVariousParameterCombinations::class))

        with(parsedMethod) {
            assertThat(name).isEqualTo("testWithExtensionParameter")
            assertThat(parameters.descriptors).containsEntry(
                "first",
                ParameterDescriptor("first", 0, false, false, true)
            )
            assertFieldDescriptors(fields, JavaTestWithVariousParameterCombinations::class)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class)
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
            parser.parse(Reflect.findMethod("parameterizedTest", JavaTestWithVariousParameterCombinations::class))

        with(parsedMethod) {
            assertThat(name).isEqualTo("parameterizedTest")
            assertThat(parameters.descriptors).containsEntry(
                "first",
                ParameterDescriptor("first", 0, false, false, true)
            )
            assertFieldDescriptors(fields, JavaTestWithVariousParameterCombinations::class)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class)
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
                aWordOf("get"),
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
                    JavaTestWithVariousParameterCombinations::class
                )
            )

        with(parsedMethod) {
            assertThat(name).isEqualTo("parameterizedTestWithExtensionParameter")
            assertThat(parameters.descriptors).containsEntry(
                "first",
                ParameterDescriptor("first", 0, false, false, true)
            )
            assertThat(parameters.descriptors).containsEntry(
                "second",
                ParameterDescriptor("second", 1, true, false, true)
            )
            assertFieldDescriptors(fields, JavaTestWithVariousParameterCombinations::class)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class)
            assertThat(nestedSentences).containsKey("nested1")
            assertThat(sentences.first().tokens).isEqualTo(expectedSentence1.tokens)
            assertThat(sentences.last().tokens).isEqualTo(expectedSentence2.tokens)
        }
    }
}

