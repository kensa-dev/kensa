package dev.kensa.parse.java

import dev.kensa.Configuration
import dev.kensa.ElementDescriptor
import dev.kensa.example.JavaWithInterface
import dev.kensa.example.JavaWithParameters
import dev.kensa.example.JavaWithScenario
import dev.kensa.example.JavaWithVariousFields
import dev.kensa.parse.Java20Parser
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceTokens.aFieldValueOf
import dev.kensa.sentence.SentenceTokens.aNewline
import dev.kensa.sentence.SentenceTokens.aParameterValueOf
import dev.kensa.sentence.SentenceTokens.aStringLiteralOf
import dev.kensa.sentence.SentenceTokens.aWordOf
import dev.kensa.sentence.SentenceTokens.anIndent
import dev.kensa.util.allProperties
import dev.kensa.util.findMethod
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.FieldsEqualityCheckConfig
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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
                aFieldValueOf("scenario:foo()")
            )
        )

        val method = JavaWithScenario::class.java.findMethod(methodName)
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            methodName.shouldBe("usingAScenario")

            with(properties) {
                get("foo")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithScenario::class.propertyNamed("foo")))
                get("scenario")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithScenario::class.propertyNamed("scenario")))
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
                get("field4")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field4")))
                get("field5")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field5")))
                get("field6")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field6")), ignoringHighlightProperty())
            }

            methods[methodName]
                .shouldNotBeNull()
                .shouldBeEqualToComparingFields(ElementDescriptor.forMethod(method))

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
                get("field1")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field1")))
                get("field2")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field2")))
                get("field3")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field3")), ignoringHighlightProperty())
            }

            methods[methodName]
                .shouldNotBeNull()
                .shouldBeEqualToComparingFields(ElementDescriptor.forMethod(method))

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

        val method = JavaWithVariousFields::class.java.findMethod(methodName)
        val parsedMethod = parser.parse(method)

        with(parsedMethod) {
            name.shouldBe(methodName)
            parameters.descriptors.shouldBeEmpty()

            with(properties) {
                get("field1")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field1")))
                get("field2")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field2")))
                get("field3")
                    .shouldNotBeNull()
                    .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field3")), ignoringHighlightProperty())
            }
            methods[methodName]
                .shouldNotBeNull()
                .shouldBeEqualToComparingFields(ElementDescriptor.forMethod(method))

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
                    aParameterValueOf("second:"),
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
            parameters.descriptors["first"]
                .shouldNotBeNull()
                .shouldBeEqualToComparingFields(ElementDescriptor.forParameter(firstParameter, "first", 0))
            parameters.descriptors["second"]
                .shouldNotBeNull()
                .shouldBeEqualToComparingFields(ElementDescriptor.forParameter(secondParameter, "second", 1))

            sentences.map { it.tokens }.shouldBe(expectedSentences.map { it.tokens })
        }
    }

    private fun ignoringHighlightProperty(): FieldsEqualityCheckConfig = FieldsEqualityCheckConfig(propertiesToExclude = listOf(ElementDescriptor.PropertyElementDescriptor::class.propertyNamed("highlight")))
    private fun KClass<*>.propertyNamed(name: String): KProperty<*> = allProperties.find { it.name == name } ?: throw IllegalArgumentException("Property $name not found in class ${this.qualifiedName}")
    private fun classMethodNamed(name: String): (Java20Parser.MethodDeclarationContext) -> Boolean = { it.methodHeader()?.methodDeclarator()?.identifier()?.text == name }
    private fun interfaceMethodNamed(name: String): (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean = { it.methodHeader()?.methodDeclarator()?.identifier()?.text == name }
}