package dev.kensa.parse.java

import dev.kensa.Configuration
import dev.kensa.context.NestedInvocationContext
import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.example.*
import dev.kensa.parse.*
import dev.kensa.sentence.TemplateSentence
import dev.kensa.sentence.TemplateToken.Type.*
import dev.kensa.sentence.asTemplateToken
import dev.kensa.util.allProperties
import dev.kensa.util.findMethod
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.FieldsEqualityCheckConfig
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.*
import kotlin.io.path.Path
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

internal class JavaMethodParserTest {

    private val configuration = Configuration()

    @BeforeEach
    internal fun setUp() {
        configuration.apply {
            sourceLocations = listOf(Path("src/example/java"))
        }
    }

    @Nested
    inner class Parsing {

        @Test
        fun `parse method with blocks`() {
            val methodName = "blocksTest"
            val parser = createParserFor(classMethodNamed(methodName))

            val expectedSentences = listOf(
                TemplateSentence(
                    listOf(
                        Keyword.asTemplateToken("Given"),
                    )
                ),
                TemplateSentence(
                    listOf(
                        BlankLine.asTemplateToken(),
                        Keyword.asTemplateToken("When"),
                    )
                ),
                TemplateSentence(
                    listOf(
                        BlankLine.asTemplateToken(),
                        Keyword.asTemplateToken("Then"),
                        NewLine.asTemplateToken(),
                        Indent.asTemplateToken(),
                        Indent.asTemplateToken(),
                        Word.asTemplateToken("thing"),
                    )
                )
            )

            val method = JavaWithBlocks::class.java.findMethod(methodName)
            val parsedMethod = parser.parse(method)

            parsedMethod.sentences.forEachIndexed { index, sentence ->
                sentence.tokens shouldBe expectedSentences[index].tokens
            }
        }

        @Test
        internal fun `parses test method`() {
            val methodName = "simpleTest"
            val parser = createParserFor(classMethodNamed(methodName))

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    StringLiteral.asTemplateToken("string"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("not"),
                    Word.asTemplateToken("blank"),
                )
            )
            val expectedNestedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("some"),
                    Word.asTemplateToken("builder"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("with"),
                    Word.asTemplateToken("something"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("build")
                )
            )

            val method = JavaWithVariousFields::class.java.findMethod(methodName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(methodName)
                parameters.descriptors.shouldBeEmpty()

                with(properties) {
                    get("field1")
                        .shouldBeNull()
                    get("field2")
                        .shouldNotBeNull()
                        .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field2")))
                    get("field3")
                        .shouldNotBeNull()
                        .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field3")), ignoringHighlightProperty())
                }
                methods[methodName]
                    .shouldBeNull()

                assertSoftly(nestedMethods["nested1"]) {
                    shouldNotBeNull()
                    sentences.shouldHaveSize(1)
                    sentences.first().tokens.shouldBe(expectedNestedSentence.tokens)
                }
                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }
    }

    @Nested
    inner class Scenario {

        @Test
        internal fun `parses a class method using a scenario`() {
            val methodName = "usingAScenario"
            val parser = createParserFor(classMethodNamed(methodName))

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("do"),
                    Word.asTemplateToken("something"),
                    FieldValue.asTemplateToken("scenario:foo()")
                )
            )

            val method = JavaWithScenario::class.java.findMethod(methodName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                methodName.shouldBe("usingAScenario")

                with(properties) {
                    get("foo")
                        .shouldBeNull()
                    get("scenario")
                        .shouldNotBeNull()
                        .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithScenario::class.propertyNamed("scenario")))
                }

                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }
    }

    @Nested
    inner class Notes {

        @Test
        fun `recognises valid notes`() {
            val functionName = "notesTest"
            val parser = createParserFor(classMethodNamed(functionName))

            val method = JavaWithNotes::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentences = listOf(
                TemplateSentence(
                    listOf(
                        Note.asTemplateToken("Note before Given")
                    )
                ),
                TemplateSentence(
                    listOf(
                        Keyword.asTemplateToken("Given"),
                        NewLine.asTemplateToken(),
                        Indent.asTemplateToken(),
                        Indent.asTemplateToken(),
                        Indent.asTemplateToken(),
                        Note.asTemplateToken("Note after closing brace")
                    )
                ),
                TemplateSentence(
                    listOf(
                        BlankLine.asTemplateToken(),
                        Note.asTemplateToken("Note before When")
                    )
                ),
                TemplateSentence(
                    listOf(
                        Keyword.asTemplateToken("When")
                    )
                ),
                TemplateSentence(
                    listOf(
                        BlankLine.asTemplateToken(),
                        Note.asTemplateToken("Note before Then")
                    )
                ),
                TemplateSentence(
                    listOf(
                        Keyword.asTemplateToken("Then"),
                        NewLine.asTemplateToken(),
                        Indent.asTemplateToken(),
                        Indent.asTemplateToken(),
                        Word.asTemplateToken("thing"),
                        Note.asTemplateToken("Note after closing bracket")
                    )
                )

            )

            parsedMethod.sentences should {
                it.shouldHaveSize(expectedSentences.size)
                it.forEachIndexed { index, sentence ->
                    sentence.tokens shouldBe expectedSentences[index].tokens
                }
            }
        }

        @Test
        fun `ignores invalid notes`() {
            val functionName = "ignoredNotesTest"
            val parser = createParserFor(classMethodNamed(functionName))

            val method = JavaWithNotes::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentences = listOf(
                TemplateSentence(
                    listOf(
                        Keyword.asTemplateToken("Given")
                    )
                ),
                TemplateSentence(
                    listOf(
                        BlankLine.asTemplateToken(),
                        Note.asTemplateToken("Note before When")
                    )
                ),
                TemplateSentence(
                    listOf(
                        Keyword.asTemplateToken("When")
                    )
                )
            )

            parsedMethod.sentences should {
                it.shouldHaveSize(expectedSentences.size)
                it.forEachIndexed { index, sentence ->
                    sentence.tokens shouldBe expectedSentences[index].tokens
                }
            }
        }
    }

    @Nested
    inner class Interfaces {

        @Test
        internal fun `parses an interface method`() {
            val methodName = "methodOnInterface"
            val parser = createParserFor({ false }, interfaceMethodNamed(methodName))

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("do"),
                    Word.asTemplateToken("something"),
                )
            )

            val method = JavaWithInterface::class.java.findMethod(methodName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                methodName.shouldBe(methodName)
                parameters.descriptors.shouldBeEmpty()

                with(properties) {
                    get("field4")
                        .shouldBeNull()
                    get("field5")
                        .shouldNotBeNull()
                        .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field5")))
                    get("field6")
                        .shouldNotBeNull()
                        .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field6")), ignoringHighlightProperty())
                }

                methods[methodName]
                    .shouldBeNull()

                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        internal fun `parses method on class that implements interface`() {
            val methodName = "classTestMethod"
            val parser = createParserFor(classMethodNamed(methodName))

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    StringLiteral.asTemplateToken("xyz"),
                    Word.asTemplateToken("contains"),
                    StringLiteral.asTemplateToken("x")
                )
            )

            val method = JavaWithInterface::class.java.findMethod(methodName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(methodName)
                parameters.descriptors.shouldBeEmpty()

                with(properties) {
                    assertSoftly(get("field1")) {
                        shouldBeNull()
                    }
                    get("field2")
                        .shouldNotBeNull()
                        .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field2")))
                    get("field3")
                        .shouldNotBeNull()
                        .shouldBeEqualToComparingFields(ElementDescriptor.forProperty(JavaWithInterface::class.propertyNamed("field3")), ignoringHighlightProperty())
                }

                methods[methodName]
                    .shouldBeNull()

                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }
    }

    @Nested
    inner class NestedSentences {

        @Test
        internal fun `parses method with parameters`() {
            val methodName = "methodWithParameters"
            val parser = createParserFor(classMethodNamed(methodName))

            val expectedSentences = listOf(
                TemplateSentence(
                    listOf(
                        Word.asTemplateToken("assert"),
                        Word.asTemplateToken("that"),
                        Word.asTemplateToken("first"),
                        Word.asTemplateToken("is"),
                        Word.asTemplateToken("in"),
                        StringLiteral.asTemplateToken("a"),
                        StringLiteral.asTemplateToken("b")
                    )
                ),
                TemplateSentence(
                    listOf(
                        Word.asTemplateToken("assert"),
                        Word.asTemplateToken("that"),
                        ParameterValue.asTemplateToken("second:"),
                        Word.asTemplateToken("is"),
                        Word.asTemplateToken("equal"),
                        Word.asTemplateToken("to"),
                        Word.asTemplateToken("MY_PARAMETER_VALUE"),
                    )
                )
            )

            val method = JavaWithParameters::class.java.findMethod(methodName, String::class.java, String::class.java)
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
    }

    private fun ignoringHighlightProperty(): FieldsEqualityCheckConfig = FieldsEqualityCheckConfig(propertiesToExclude = listOf(ElementDescriptor.PropertyElementDescriptor::class.propertyNamed("highlight")))
    private fun KClass<*>.propertyNamed(name: String): KProperty<*> = allProperties.find { it.name == name } ?: throw IllegalArgumentException("Property $name not found in class ${this.qualifiedName}")
    private fun classMethodNamed(name: String): (Java20Parser.MethodDeclarationContext) -> Boolean = { it.methodHeader()?.methodDeclarator()?.identifier()?.text == name }
    private fun interfaceMethodNamed(name: String): (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean = { it.methodHeader()?.methodDeclarator()?.identifier()?.text == name }

    private fun createParserFor(isClassTest: (Java20Parser.MethodDeclarationContext) -> Boolean, isInterfaceTest: (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean = { false }): MethodParser =
        MethodParser(
            ParserCache(),
            configuration,
            CompositeParserDelegate(
                configuration.sourceCode,
                listOf(
                    JavaParserDelegate(
                        isClassTest = isClassTest,
                        isInterfaceTest = isInterfaceTest,
                        configuration.antlrErrorListenerDisabled,
                        configuration.antlrPredicationMode,
                        configuration.sourceCode
                    )
                )
            )
        )

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