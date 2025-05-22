package dev.kensa.parse.java

import dev.kensa.Configuration
import dev.kensa.example.JavaWithInterface
import dev.kensa.example.JavaWithParameters
import dev.kensa.example.JavaWithScenario
import dev.kensa.example.JavaWithVariousFields
import dev.kensa.kotest.asClue
import dev.kensa.kotest.shouldBe
import dev.kensa.parse.Accessor.ValueAccessor.*
import dev.kensa.parse.Java20Parser
import dev.kensa.parse.assertMethodDescriptors
import dev.kensa.parse.assertPropertyDescriptors
import dev.kensa.parse.propertyNamed
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aParameterValueOf
import dev.kensa.sentence.SentenceTokens.aScenarioValueOf
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
import org.junit.jupiter.api.Test

internal class JavaMethodParserTest {

    private val configuration = Configuration()

    @Test
    internal fun `parses a class method using a scenario`() {
        val methodName = "usingAScenario"
        val parser = JavaMethodParser(
            isClassTest = classMethodNamed(methodName),
            { false },
            configuration
        )

        val expectedSentence = Sentence(
            listOf(
                aWordOf("do"),
                aWordOf("something"),
                aScenarioValueOf("scenario.foo"),
            )
        )

        val method = JavaWithScenario::class.java.findMethod(methodName)
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            methodName.shouldBe("usingAScenario")

            with(properties) {
                assertSoftly(get("foo")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaWithScenario::class.propertyNamed("foo"))) }
                }
                assertSoftly(get("scenario")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaWithScenario::class.propertyNamed("scenario"))) }
                }
            }

            sentences.first().tokens shouldBe expectedSentence.tokens
        }
    }

    @Test
    internal fun `parses an interface method`() {
        val methodName = "methodOnInterface"
        val parser = JavaMethodParser(
            isClassTest = { false },
            isInterfaceTest = interfaceMethodNamed(methodName),
            configuration
        )

        val expectedSentence = Sentence(
            listOf(
                aWordOf("do"),
                aWordOf("something"),
            )
        )

        val method = JavaWithInterface::class.java.findMethod(methodName)
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            methodName.shouldBe(methodName)
            parameters.descriptors.shouldBeEmpty()

            with(properties) {
                assertSoftly(get("field4")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaWithInterface::class.propertyNamed("field4"))) }
                }
                assertSoftly(get("field5")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaWithInterface::class.propertyNamed("field5"))) }
                }
                assertSoftly(get("field6")) {
                    shouldNotBeNull()
                    asClue { shouldBe(PropertyAccessor(JavaWithInterface::class.propertyNamed("field6"))) }
                }
            }

            assertSoftly(methods[methodName]) {
                shouldNotBeNull()
                asClue { shouldBe(MethodAccessor(method)) }
            }

            sentences.first().tokens shouldBe expectedSentence.tokens
        }
    }

    @Test
    internal fun `parses method on class that implements interface`() {
        val methodName = "classTestMethod"
        val parser = JavaMethodParser(
            isClassTest = classMethodNamed(methodName),
            isInterfaceTest = { false },
            configuration
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

        val method = JavaWithInterface::class.java.findMethod(methodName)
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe(methodName)
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

            assertSoftly(methods[methodName]) {
                shouldNotBeNull()
                asClue { shouldBe(MethodAccessor(method)) }
            }

            sentences.first().tokens.shouldBe(expectedSentence.tokens)
        }
    }

    @Test
    internal fun `parses test method`() {
        val methodName = "simpleTest"
        val parser = JavaMethodParser(
            isClassTest = classMethodNamed(methodName),
            isInterfaceTest = { false },
            configuration
        )

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
                aWordOf("with"),
                aWordOf("something"),
                aNewline(),
                anIndent(),
                anIndent(),
                anIndent(),
                aWordOf("build")
            )
        )

        val parsedMethod =
            parser.parse(JavaWithVariousFields::class.java.findMethod(methodName))

        with(parsedMethod) {
            name.shouldBe(methodName)
            parameters.descriptors.shouldBeEmpty()

            assertPropertyDescriptors(properties, JavaWithVariousFields::class.java)
            assertMethodDescriptors(methods, JavaWithVariousFields::class.java)

            assertSoftly(nestedSentences["nested1"]) {
                shouldNotBeNull()
                shouldHaveSize(1)
                first().tokens.shouldBe(expectedNestedSentence.tokens)
            }
            sentences.first().tokens.shouldBe(expectedSentence.tokens)
        }
    }

    @Test
    internal fun `parses method with parameters`() {
        val methodName = "methodWithParameters"
        val parser = JavaMethodParser(
            isClassTest = classMethodNamed(methodName),
            isInterfaceTest = { false },
            configuration
        )

        val expectedSentences = listOf(
            Sentence(
                listOf(
                    aWordOf("assert"),
                    aWordOf("that"),
                    aWordOf("first"),
                    aWordOf("is"),
                    aWordOf("in"),
                    aStringLiteralOf("a"),
                    aStringLiteralOf("b")
                )
            ),
            Sentence(
                listOf(
                    aWordOf("assert"),
                    aWordOf("that"),
                    aParameterValueOf("second"),
                    aWordOf("is"),
                    aWordOf("equal"),
                    aWordOf("to"),
                    aWordOf("MY_PARAMETER_VALUE"),
                )
            )
        )

        val method = JavaWithParameters::class.java.findMethod(methodName)
        val firstParameter = method.parameters.first()
        val secondParameter = method.parameters.last()

        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe(methodName)
            assertSoftly(parameters.descriptors["first"]) {
                shouldNotBeNull()
                asClue { shouldBe(ParameterAccessor(firstParameter, "first", 0)) }
            }
            assertSoftly(parameters.descriptors["second"]) {
                shouldNotBeNull()
                asClue { shouldBe(ParameterAccessor(secondParameter, "second", 1)) }
            }

            sentences.map { it.tokens }.shouldBe(expectedSentences.map { it.tokens })
        }
    }

    private fun classMethodNamed(name: String): (Java20Parser.MethodDeclarationContext) -> Boolean = { it.methodHeader()?.methodDeclarator()?.identifier()?.text == name }
    private fun interfaceMethodNamed(name: String): (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean = { it.methodHeader()?.methodDeclarator()?.identifier()?.text == name }

}