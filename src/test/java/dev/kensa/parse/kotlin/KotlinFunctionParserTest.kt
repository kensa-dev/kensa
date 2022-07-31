package dev.kensa.parse.kotlin

import dev.kensa.Kensa.konfigure
import dev.kensa.acceptance.example.*
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
import org.antlr.v4.runtime.atn.PredictionMode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KotlinFunctionParserTest {

    private val parser = KotlinFunctionParser()

    @BeforeEach
    internal fun setUp() {
        konfigure {
            antlrPredicationMode = PredictionMode.LL
        }
    }

    @Test
    internal fun `recognises when keyword properly`() {
        val javaClass = KotlinTestWithSimpleAction::class.java
        val method = javaClass.findMethod("simpleTest")
        val parsedMethod = parser.parse(method)

        val expectedSentence = Sentence(
            listOf(
                aKeywordOf("When"),
                aWordOf("some"),
                aWordOf("action"),
                aWordOf("is"),
                aWordOf("performed")
            )
        )

        with(parsedMethod) {
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
        }
    }

    @Test
    internal fun `recognises SentenceValue and Highlight on various Kotlin properties`() {
        val javaClass = KotlinTestWithVariousParameterCombinations::class.java
        val method = javaClass.findMethod("similarNameTest1")
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            with(properties) {
                assertSoftly(get("property1")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(javaClass.kotlin.propertyNamed("property1"))) }
                }
                assertSoftly(get("propertyWithGetter")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(javaClass.kotlin.propertyNamed("propertyWithGetter"))) }
                }
                assertSoftly(get("lazyProperty")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(javaClass.kotlin.propertyNamed("lazyProperty"))) }
                }
            }
        }
    }

    @Test
    internal fun `parses test expression function`() {
        val expectedSentence = Sentence(
            listOf(
                aKeywordOf("Given"),
                aWordOf("some"),
                aWordOf("action"),
                aWordOf("name"),
                aWordOf("is"),
                aWordOf("added"),
                aWordOf("to"),
                aWordOf("givens"),
            )
        )

        val method = KotlinTestWithExpressionFunction::class.java.findMethod("expressionTest")
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("expressionTest")
            parameters.descriptors.shouldBeEmpty()

            assertSoftly(methods["expressionTest"]) {
                shouldNotBeNull()
                asClue { shouldBe(MethodAccessor(method)) }
            }

            sentences.first().tokens.shouldBe(expectedSentence.tokens)
        }
    }

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

        val method = KotlinTestFromInterface::class.java.findMethod("interfaceTestMethod")
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("interfaceTestMethod")
            parameters.descriptors.shouldBeEmpty()

            assertSoftly(methods["interfaceTestMethod"]) {
                shouldNotBeNull()
                asClue { shouldBe(MethodAccessor(method)) }
            }
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
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

        val method = KotlinTestFromInterface::class.java.findMethod("classTestMethod")
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
                aKeywordOf("With"),
                aWordOf("something"),
                aNewline(),
                anIndent(),
                anIndent(),
                aWordOf("build")
            )
        )

        val method = KotlinTestWithVariousParameterCombinations::class.java.findMethod("similarNameTest1")
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("similarNameTest1")
            parameters.descriptors.shouldBeEmpty()
            assertPropertyDescriptors(properties, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)

            assertSoftly(nestedSentences["nested1"]) {
                shouldNotBeNull()
                shouldHaveSize(1)
                first().tokens.shouldBe(expectedNestedSentence.tokens)
            }
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
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

        val method = KotlinTestWithVariousParameterCombinations::class.java.findMethod("internalTest\$kensa")
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("internalTest")
            parameters.descriptors.shouldBeEmpty()
            assertPropertyDescriptors(properties, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)

            nestedSentences.shouldContainKey("nested1")
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
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

        val method = KotlinTestWithVariousParameterCombinations::class.java.findMethod("testWithNoParameters")
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("testWithNoParameters")
            parameters.descriptors.shouldBeEmpty()
            assertPropertyDescriptors(properties, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)

            nestedSentences.shouldContainKey("nested1")
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
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

        val method = KotlinTestWithVariousParameterCombinations::class.java.findMethod("testWithExtensionParameter")
        val firstParameter = method.parameters.first()
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("testWithExtensionParameter")

            assertSoftly(parameters.descriptors["first"]) {
                shouldNotBeNull()
                asClue { shouldBe(Accessor.ParameterAccessor(firstParameter, "first", 0, true)) }
            }
            assertPropertyDescriptors(properties, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)
            nestedSentences.shouldContainKey("nested1")
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
        }
    }

    @Test
    internal fun parsesParameterizedTestMethod() {
        val expectedSentence = Sentence(
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

        val method = KotlinTestWithVariousParameterCombinations::class.java.findMethod("parameterizedTest")
        val firstParameter = method.parameters.first()
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe("parameterizedTest")
            assertSoftly(parameters.descriptors["first"]) {
                shouldNotBeNull()
                asClue { shouldBe(Accessor.ParameterAccessor(firstParameter, "first", 0, true)) }
            }
            assertPropertyDescriptors(properties, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)
            nestedSentences.shouldContainKey("nested1")
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
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

        val method = KotlinTestWithVariousParameterCombinations::class.java.findMethod("parameterizedTestWithExtensionParameter")
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
            assertPropertyDescriptors(properties, KotlinTestWithVariousParameterCombinations::class.java)
            assertMethodDescriptors(methods, KotlinTestWithVariousParameterCombinations::class.java)
            nestedSentences.shouldContainKey("nested1")
            sentences.first().tokens.shouldBe(expectedSentence1.tokens)
            sentences.last().tokens.shouldBe(expectedSentence2.tokens)
        }
    }
}