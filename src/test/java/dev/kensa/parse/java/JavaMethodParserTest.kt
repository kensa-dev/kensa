package dev.kensa.parse.java

import dev.kensa.acceptance.example.JavaTestFromInterface
import dev.kensa.acceptance.example.JavaTestWithVariousParameterCombinations
import dev.kensa.kotest.asClue
import dev.kensa.kotest.shouldBe
import dev.kensa.parse.Accessor
import dev.kensa.parse.Accessor.ValueAccessor.MethodAccessor
import dev.kensa.parse.Accessor.ValueAccessor.PropertyAccessor
import dev.kensa.parse.assertMethodDescriptors
import dev.kensa.parse.assertPropertyDescriptors
import dev.kensa.parse.propertyNamed
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aParameterValueOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anIndent
import dev.kensa.util.findMethod
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JavaMethodParserTest {

    private val parser = JavaMethodParser()

    @Test
    internal fun `parses interface method`() {
        val expectedSentence = Sentence(
            listOf(
                aWordOf("assert"),
                aWordOf("that"),
                aStringLiteralOf("abc"),
                aWordOf("contains"),
                aStringLiteralOf("a")
            )
        )

        val method = JavaTestFromInterface::class.java.findMethod("interfaceTestMethod")
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("interfaceTestMethod")
            parameters.descriptors.shouldBeEmpty()

            with(properties) {
                assertSoftly(get("field4")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaTestFromInterface::class.propertyNamed("field4"))) }
                }
                assertSoftly(get("field5")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaTestFromInterface::class.propertyNamed("field5"))) }
                }
                assertSoftly(get("field6")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaTestFromInterface::class.propertyNamed("field6"))) }
                }
            }

            assertSoftly(methods["interfaceTestMethod"]) {
                shouldNotBeNull()
                asClue { shouldBe(MethodAccessor(method)) }
            }

            assertThat(sentences.first().tokens).isEqualTo(expectedSentence.tokens)
        }
    }

    @Test
    internal fun `parses test method on class that has test interface`() {
        val expectedSentence = Sentence(
            listOf(
                aWordOf("assert"),
                aWordOf("that"),
                aStringLiteralOf("xyz"),
                aWordOf("contains"),
                aStringLiteralOf("x")
            )
        )

        val method = JavaTestFromInterface::class.java.findMethod("classTestMethod")
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("classTestMethod")
            parameters.descriptors.shouldBeEmpty()

            with(properties) {
                assertSoftly(get("field1")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaTestFromInterface::class.propertyNamed("field1"))) }
                }
                assertSoftly(get("field2")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaTestFromInterface::class.propertyNamed("field2"))) }
                }
                assertSoftly(get("field3")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaTestFromInterface::class.propertyNamed("field3"))) }
                }
            }

            assertSoftly(methods["classTestMethod"]) {
                shouldNotBeNull()
                asClue { shouldBe(MethodAccessor(method)) }
            }

            sentences.first().tokens.shouldBe(expectedSentence.tokens)
        }
    }

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
            parser.parse(JavaTestWithVariousParameterCombinations::class.java.findMethod("similarNameTest1"))

        with(parsedMethod) {
            name.shouldBe("similarNameTest1")
            parameters.descriptors.shouldBeEmpty()

            assertPropertyDescriptors(properties, JavaTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class.java)

            assertSoftly(nestedSentences["nested1"]) {
                shouldNotBeNull()
                shouldHaveSize(1)
                first().tokens.shouldBe(expectedNestedSentence.tokens)
            }
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
        }
    }

    @Test
    internal fun `parses test method with no parameters`() {
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
            parser.parse(JavaTestWithVariousParameterCombinations::class.java.findMethod("testWithNoParameters"))

        with(parsedMethod) {
            name.shouldBe("testWithNoParameters")
            parameters.descriptors.shouldBeEmpty()

            assertPropertyDescriptors(properties, JavaTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class.java)

            nestedSentences.shouldContainKey("nested1")
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
        }
    }

    @Test
    internal fun `parses test method with extension parameters`() {
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

        val method = JavaTestWithVariousParameterCombinations::class.java.findMethod("testWithExtensionParameter")
        val firstParameter = method.parameters.first()
        val parsedMethod = parser.parse(method)


        with(parsedMethod) {
            name.shouldBe("testWithExtensionParameter")

            assertSoftly(parameters.descriptors["first"]) {
                shouldNotBeNull()
                asClue { shouldBe(Accessor.ParameterAccessor(firstParameter, "first", 0, true)) }
            }

            assertPropertyDescriptors(properties, JavaTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class.java)

            nestedSentences.shouldContainKey("nested1")
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
        }
    }

    @Test
    internal fun `parses parameterized test method`() {
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

        val method = JavaTestWithVariousParameterCombinations::class.java.findMethod("parameterizedTest")
        val firstParameter = method.parameters.first()
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("parameterizedTest")
            assertSoftly(parameters.descriptors["first"]) {
                shouldNotBeNull()
                asClue { shouldBe(Accessor.ParameterAccessor(firstParameter, "first", 0, true)) }
            }

            assertPropertyDescriptors(properties, JavaTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class.java)

            nestedSentences.shouldContainKey("nested1")
            sentences.first().tokens.shouldBe(expectedSentence1.tokens)
        }
    }

    @Test
    internal fun `parses parameterized test method with extension parameters`() {
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

        val method = JavaTestWithVariousParameterCombinations::class.java.findMethod("parameterizedTestWithExtensionParameter")
        val firstParameter = method.parameters.first()
        val secondParameter = method.parameters.last()
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("parameterizedTestWithExtensionParameter")

            assertSoftly(parameters.descriptors["first"]) {
                shouldNotBeNull()
                asClue { shouldBe(Accessor.ParameterAccessor(firstParameter, "first", 0, true)) }
            }
            assertSoftly(parameters.descriptors["second"]) {
                shouldNotBeNull()
                asClue { shouldBe(Accessor.ParameterAccessor(secondParameter, "second", 1, true)) }
            }

            assertPropertyDescriptors(properties, JavaTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, JavaTestWithVariousParameterCombinations::class.java)

            nestedSentences.shouldContainKey("nested1")
            sentences.first().tokens.shouldBe(expectedSentence1.tokens)
            sentences.last().tokens.shouldBe(expectedSentence2.tokens)
        }
    }
}

