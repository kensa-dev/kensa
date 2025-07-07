package dev.kensa.parse.kotlin

import dev.kensa.Configuration
import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.context.NestedInvocationContext
import dev.kensa.example.*
import dev.kensa.parse.KotlinParser
import dev.kensa.sentence.TemplateSentence
import dev.kensa.sentence.TemplateToken.Type.*
import dev.kensa.sentence.asTemplateToken
import dev.kensa.util.allProperties
import dev.kensa.util.findMethod
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.antlr.v4.runtime.atn.PredictionMode
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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
                TemplateSentence(listOf(Word.asTemplateToken("assert"), Word.asTemplateToken("that"), Word.asTemplateToken("the"), Word.asTemplateToken("null"), Word.asTemplateToken("result"), Word.asTemplateToken("is"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), NullLiteral.asTemplateToken("null"))),
                TemplateSentence(listOf(Word.asTemplateToken("assert"), Word.asTemplateToken("that"), Word.asTemplateToken("the"), Word.asTemplateToken("hex"), Word.asTemplateToken("result"), Word.asTemplateToken("is"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), NumberLiteral.asTemplateToken("0x123"))),
                TemplateSentence(listOf(Word.asTemplateToken("assert"), Word.asTemplateToken("that"), Word.asTemplateToken("the"), Word.asTemplateToken("boolean"), Word.asTemplateToken("result"), Word.asTemplateToken("is"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), BooleanLiteral.asTemplateToken("true"))),
                TemplateSentence(listOf(Word.asTemplateToken("assert"), Word.asTemplateToken("that"), Word.asTemplateToken("the"), Word.asTemplateToken("character"), Word.asTemplateToken("result"), Word.asTemplateToken("is"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), CharacterLiteral.asTemplateToken("a")))
            )

            parsedMethod.sentences.forEachIndexed { index, sentence ->
                sentence.tokens shouldBe expectedSentences[index].tokens
            }
        }

    }

    @Nested
    inner class Scenario {

        @Test
        fun `replaces scenario value in sentence when using scenario with infix function`() {
            val functionName = "testWithInfixScenario"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithScenario::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("the"),
                    Word.asTemplateToken("extracted"),
                    Word.asTemplateToken("value"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    FieldValue.asTemplateToken("myScenario:value")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using scenario field`() {
            val functionName = "testWithScenarioField"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithScenario::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("with"),
                    FieldValue.asTemplateToken("myScenario:value")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using scenario function`() {
            val functionName = "testWithScenarioFunction"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithScenario::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("with"),
                    MethodValue.asTemplateToken("myScenarioFun:value")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using scenario holder`() {
            val functionName = "testWithScenarioHolder"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithScenario::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("with"),
                    FieldValue.asTemplateToken("myScenario:value"),
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }
    }

    @Nested
    inner class Fixtures {

        @Test
        fun `replaces fixture value in sentence when using fixtures inside lambda function`() {
            val functionName = "testWithFixturesInLambda"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithFixtures::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Keyword.asTemplateToken("When"),
                    Word.asTemplateToken("something"),
                    Word.asTemplateToken("with"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("a"),
                    Word.asTemplateToken("data"),
                    Word.asTemplateToken("item"),
                    Operator.asTemplateToken("="),
                    FixturesValue.asTemplateToken("MyFixture:")
                )
            )

            with(parsedMethod) {
                sentences.size shouldBe 1
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces fixture value in sentence when using fixtures with infix function`() {
            val functionName = "testWithInfixFixtures"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithFixtures::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("the"),
                    Word.asTemplateToken("extracted"),
                    Word.asTemplateToken("value"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    FixturesValue.asTemplateToken("MyFixture:")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using fixture`() {
            val functionName = "testWithFixtures"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithFixtures::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("with"),
                    FixturesValue.asTemplateToken("MyFixture:")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using chained fixture`() {
            val functionName = "testWithChainedFixture"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithFixtures::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("the"),
                    Word.asTemplateToken("extracted"),
                    Word.asTemplateToken("character"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    FixturesValue.asTemplateToken("MyFixture:toString().last()")
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

            val expectedSentence = TemplateSentence(
                listOf(
                    Keyword.asTemplateToken("When"),
                    Word.asTemplateToken("some"),
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("performed")
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
                    size shouldBe 5

                    assertSoftly(get("field1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "field1"
                            it.isHighlight.shouldBeTrue()
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("property1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "property1"
                            it.isHighlight.shouldBeTrue()
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("propertyWithGetter")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "propertyWithGetter"
                            it.isHighlight.shouldBeTrue()
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("throwingGetter")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "throwingGetter"
                            it.isHighlight.shouldBeFalse()
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("lazyProperty")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "lazyProperty"
                            it.isHighlight.shouldBeTrue()
                            it.isRenderedValue.shouldBeFalse()
                            it.isParameterizedTestDescription.shouldBeFalse()

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
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("nested1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "nested1"
                            it.isHighlight.shouldBeFalse()
                            it.isRenderedValue.shouldBeFalse()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("internalNested\$core_example")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "internalNested\$core_example"
                            it.isHighlight.shouldBeFalse()
                            it.isRenderedValue.shouldBeFalse()
                            it.isParameterizedTestDescription.shouldBeFalse()
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

            val expectedSentence = TemplateSentence(
                listOf(
                    Keyword.asTemplateToken("Given"),
                    Word.asTemplateToken("some"),
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("name"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("added"),
                    Word.asTemplateToken("to"),
                    Word.asTemplateToken("givens"),
                )
            )

            val method = KotlinWithExpressionFunction::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(functionName)
                parameters.descriptors.shouldBeEmpty()

                assertSoftly(methods[functionName]) {
                    shouldNotBeNull()
//                    asClue { shouldBe(MethodAccessor(method)) }
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

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    StringLiteral.asTemplateToken("abc"),
                    Word.asTemplateToken("contains"),
                    StringLiteral.asTemplateToken("a")
                )
            )

            val method = KotlinWithInterface::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(functionName)
                parameters.descriptors.shouldBeEmpty()

                assertSoftly(methods[functionName]) {
                    shouldNotBeNull()
//                    asClue { shouldBe(MethodAccessor(method)) }
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

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    StringLiteral.asTemplateToken("xyz"),
                    Word.asTemplateToken("contains"),
                    StringLiteral.asTemplateToken("x")
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
//                        asClue { shouldBe(PropertyAccessor(JavaWithInterface::class.propertyNamed("field1"))) }
                    }
                    assertSoftly(get("field2")) {
                        shouldNotBeNull()
//                        asClue { shouldBe(PropertyAccessor(JavaWithInterface::class.propertyNamed("field2"))) }
                    }
                    assertSoftly(get("field3")) {
                        shouldNotBeNull()
//                        asClue { shouldBe(PropertyAccessor(JavaWithInterface::class.propertyNamed("field3"))) }
                    }
                }

                assertSoftly(methods[functionName]) {
                    shouldNotBeNull()
//                    asClue { shouldBe(MethodAccessor(method)) }
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

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    StringLiteral.asTemplateToken("true"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    StringLiteral.asTemplateToken("true")
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

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("first"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("in"),
                    StringLiteral.asTemplateToken("a"),
                    StringLiteral.asTemplateToken("b")
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

//                    asClue { shouldBe(ValueAccessor.ParameterAccessor(firstParameter, "first", 0)) }
                }
                assertSoftly(parameters.descriptors["second"]) {
                    shouldNotBeNull()
//                    asClue { shouldBe(ValueAccessor.ParameterAccessor(secondParameter, "second", 1)) }
                }
                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }
    }

    private fun KClass<*>.propertyNamed(name: String): KProperty<*> = allProperties.find { it.name == name } ?: throw IllegalArgumentException("Property $name not found in class ${this.qualifiedName}")
    private fun aFunctionNamed(functionName: String): (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == functionName.substringBefore("$") }

    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            NestedInvocationContextHolder.bindToCurrentThread(NestedInvocationContext())
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            NestedInvocationContextHolder.clearFromThread()
        }
    }
}