package dev.kensa.parse.kotlin

import dev.kensa.Configuration
import dev.kensa.example.*
import dev.kensa.kotest.asClue
import dev.kensa.kotest.shouldBe
import dev.kensa.parse.Accessor
import dev.kensa.parse.Accessor.ValueAccessor
import dev.kensa.parse.Accessor.ValueAccessor.PropertyAccessor
import dev.kensa.parse.Accessor.ValueAccessor.MethodAccessor
import dev.kensa.parse.KotlinParser
import dev.kensa.parse.propertyNamed
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceTokens.aBooleanLiteralOf
import dev.kensa.sentence.SentenceTokens.aCharacterLiteralOf
import dev.kensa.sentence.SentenceTokens.aKeywordOf
import dev.kensa.sentence.SentenceTokens.aNullLiteral
import dev.kensa.sentence.SentenceTokens.aNumberLiteralOf
import dev.kensa.sentence.SentenceTokens.aScenarioValueOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.util.findMethod
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.antlr.v4.runtime.atn.PredictionMode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class KotlinFunctionParserTest {

    private val configuration = Configuration()

    @BeforeEach
    internal fun setUp() {
        configuration.apply {
            antlrPredicationMode = PredictionMode.LL
        }
    }

    @Nested
    inner class Literals {

        @Test
        fun `recognises various literals`() {
            val functionName = "literalTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val method = KotlinWithVariousLiterals::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentences = listOf(
                Sentence(listOf(aWordOf("assert"), aWordOf("that"), aWordOf("the"), aWordOf("null"), aWordOf("result"), aWordOf("is"), aWordOf("equal"), aWordOf("to"), aNullLiteral())),
                Sentence(listOf(aWordOf("assert"), aWordOf("that"), aWordOf("the"), aWordOf("hex"), aWordOf("result"), aWordOf("is"), aWordOf("equal"), aWordOf("to"), aNumberLiteralOf("0x123"))),
                Sentence(listOf(aWordOf("assert"), aWordOf("that"), aWordOf("the"), aWordOf("boolean"), aWordOf("result"), aWordOf("is"), aWordOf("equal"), aWordOf("to"), aBooleanLiteralOf(true))),
                Sentence(listOf(aWordOf("assert"), aWordOf("that"), aWordOf("the"), aWordOf("character"), aWordOf("result"), aWordOf("is"), aWordOf("equal"), aWordOf("to"), aCharacterLiteralOf('a')))
            )

            parsedMethod.sentences.forEachIndexed { index, sentence ->
                sentence.tokens shouldBe expectedSentences[index].tokens
            }
        }

    }

    @Nested
    inner class Scenario {

        @Test
        fun `replaces scenario value in sentence when using scenario holder`() {
            val functionName = "simpleTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithScenarioHolder::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = Sentence(
                listOf(
                    aWordOf("action"),
                    aWordOf("with"),
                    aScenarioValueOf("myScenario.value")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }
    }

    @Nested
    inner class Keywords {
        @Test
        internal fun `replaces 'whenever' with 'when' keyword`() {
            val functionName = "simpleTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithWhenever::class.java
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
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }
    }

    @Nested
    inner class Annotations {

        @Test
        internal fun `recognises SentenceValue and Highlight on various Kotlin properties and functions`() {
            val functionName = "simpleTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithAnnotations::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                with(properties) {
                    properties.size shouldBe 5
                    assertSoftly(get("field1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "field1"
                            it.isHighlight.shouldBeTrue()
                            it.isSentenceValue.shouldBeTrue()
                            it.isScenario.shouldBeFalse()
                            it.isScenarioHolder.shouldBeFalse()
                            it.isTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("property1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "property1"
                            it.isHighlight.shouldBeTrue()
                            it.isSentenceValue.shouldBeTrue()
                            it.isScenario.shouldBeFalse()
                            it.isScenarioHolder.shouldBeFalse()
                            it.isTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("propertyWithGetter")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "propertyWithGetter"
                            it.isHighlight.shouldBeTrue()
                            it.isSentenceValue.shouldBeTrue()
                            it.isScenario.shouldBeFalse()
                            it.isScenarioHolder.shouldBeFalse()
                            it.isTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("throwingGetter")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "throwingGetter"
                            it.isHighlight.shouldBeFalse()
                            it.isSentenceValue.shouldBeTrue()
                            it.isScenario.shouldBeFalse()
                            it.isScenarioHolder.shouldBeFalse()
                            it.isTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("lazyProperty")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "lazyProperty"
                            it.isHighlight.shouldBeTrue()
                            it.isSentenceValue.shouldBeFalse()
                            it.isScenario.shouldBeFalse()
                            it.isScenarioHolder.shouldBeFalse()
                            it.isTestDescription.shouldBeFalse()

                        }
                    }
                }
                with(methods) {
                    methods.size shouldBe 8
                    assertSoftly(get("method1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "method1"
                            it.isHighlight.shouldBeFalse()
                            it.isSentenceValue.shouldBeTrue()
                            it.isScenario.shouldBeFalse()
                            it.isScenarioHolder.shouldBeFalse()
                            it.isTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("nested1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "nested1"
                            it.isHighlight.shouldBeFalse()
                            it.isSentenceValue.shouldBeFalse()
                            it.isScenario.shouldBeFalse()
                            it.isScenarioHolder.shouldBeFalse()
                            it.isTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("internalNested\$core_example")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "internalNested\$core_example"
                            it.isHighlight.shouldBeFalse()
                            it.isSentenceValue.shouldBeFalse()
                            it.isScenario.shouldBeFalse()
                            it.isScenarioHolder.shouldBeFalse()
                            it.isTestDescription.shouldBeFalse()
                        }
                    }
                }
            }
        }
    }

    @Nested
    inner class Functions {

        @Test
        internal fun `parses expression function`() {
            val functionName = "simpleTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

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

            val method = KotlinWithExpressionFunction::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(functionName)
                parameters.descriptors.shouldBeEmpty()

                assertSoftly(methods[functionName]) {
                    shouldNotBeNull()
                    asClue { shouldBe(MethodAccessor(method)) }
                }

                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }

        @Test
        internal fun `parses interface function`() {
            val functionName = "interfaceTestMethod"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val expectedSentence = Sentence(
                listOf(
                    aWordOf("assert"),
                    aWordOf("that"),
                    aStringLiteralOf("abc"),
                    aWordOf("contains"),
                    aStringLiteralOf("a")
                )
            )

            val method = KotlinWithInterface::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(functionName)
                parameters.descriptors.shouldBeEmpty()

                assertSoftly(methods[functionName]) {
                    shouldNotBeNull()
                    asClue { shouldBe(MethodAccessor(method)) }
                }
                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }

        @Test
        internal fun `parses function on class that has test interface`() {
            val functionName = "simpleTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val expectedSentence = Sentence(
                listOf(
                    aWordOf("assert"),
                    aWordOf("that"),
                    aStringLiteralOf("xyz"),
                    aWordOf("contains"),
                    aStringLiteralOf("x")
                )
            )

            val method = KotlinWithInterface::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(functionName)
                parameters.descriptors.shouldBeEmpty()

                with(properties) {
                    assertSoftly(get("field1")) {
                        shouldNotBeNull()
                        asClue { shouldBe(PropertyAccessor(JavaWithInterface::class.propertyNamed("field1"))) }
                    }
                    assertSoftly(get("field2")) {
                        shouldNotBeNull()
                        asClue { shouldBe(PropertyAccessor(JavaWithInterface::class.propertyNamed("field2"))) }
                    }
                    assertSoftly(get("field3")) {
                        shouldNotBeNull()
                        asClue { shouldBe(PropertyAccessor(JavaWithInterface::class.propertyNamed("field3"))) }
                    }
                }

                assertSoftly(methods[functionName]) {
                    shouldNotBeNull()
                    asClue { shouldBe(MethodAccessor(method)) }
                }

                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }

        @Test
        internal fun `parses internal function`() {
            val functionName = "simpleTest\$core_example"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

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

            val method = KotlinWithInternalFunction::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe("simpleTest")
                parameters.descriptors.shouldBeEmpty()
                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }
    }

    @Nested
    inner class Parameters {
        @Test
        internal fun parsesTestMethodWithExtensionParameters() {
            val functionName = "parameterizedTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

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

            val method = KotlinWithParameters::class.java.findMethod(functionName)
            val firstParameter = method.parameters.first()
            val secondParameter = method.parameters.last()
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe("parameterizedTest")

                assertSoftly(parameters.descriptors["first"]) {
                    shouldNotBeNull()
                    asClue { shouldBe(ValueAccessor.ParameterAccessor(firstParameter, "first", 0)) }
                }
                assertSoftly(parameters.descriptors["second"]) {
                    shouldNotBeNull()
                    asClue { shouldBe(ValueAccessor.ParameterAccessor(secondParameter, "second", 1)) }
                }
                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }
    }

    private fun aFunctionNamed(functionName: String): (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == functionName.substringBefore("$") }
}