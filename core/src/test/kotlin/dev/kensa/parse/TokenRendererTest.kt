package dev.kensa.parse

import dev.kensa.FixturesAndOutputs
import dev.kensa.Highlight
import dev.kensa.RenderedHintStrategy.HintFromProperty
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.RenderingDirective
import dev.kensa.TextStyle.Italic
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry
import dev.kensa.fixture.Fixtures
import dev.kensa.fixture.fixture
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.render.Renderers
import dev.kensa.render.ValueRenderer
import dev.kensa.sentence.TemplateToken.Type.*
import dev.kensa.sentence.aRenderedValueOf
import dev.kensa.sentence.asTemplateToken
import dev.kensa.util.NamedValue
import dev.kensa.util.findMethod
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.lang.reflect.Parameter

class TokenRendererTest {

    class Field(val path: String)

    @Suppress("unused")
    object HintedFields {
        val aStringField = Field("path/to/string")
    }

    @Suppress("unused")
    class Wrapper(val value: String) {
        val firstCharacter = value.first()
    }

    @Suppress("unused")
    class TheTestClass {
        @field:Highlight
        val prop1 = "prop1"
        val prop2 = null
        val prop3 = 20
        val prop4 = true
        val prop5 = Wrapper("MyString")

        @Highlight
        fun method1() = "method1"
        fun method2() = null
        fun method3() = 20
        fun method4() = true
        fun method5() = Wrapper("MyString")
    }

    private val renderers = Renderers().apply {
        addValueRenderer(String::class, ValueRenderer { value -> "***$value***" })
        addValueRenderer(Number::class, ValueRenderer { value -> "***$value***" })
    }
    private val arguments = arrayOf("arg1", null, 20, true, Wrapper("MyString"))
    private val parameters = mapOf(
        "param1" to ElementDescriptor.forParameter(parameterWithAnnotations(Highlight()), "param1", 0),
        "param2" to ElementDescriptor.forParameter(parameterWithAnnotations(), "param2", 1),
        "param3" to ElementDescriptor.forParameter(parameterWithAnnotations(), "param3", 2),
        "param4" to ElementDescriptor.forParameter(parameterWithAnnotations(), "param4", 3),
        "param5" to ElementDescriptor.forParameter(parameterWithAnnotations(), "param5", 4)
    )
    private val methods = mapOf(
        "method1" to ElementDescriptor.forMethod(TheTestClass::class.java.findMethod("method1")),
        "method2" to ElementDescriptor.forMethod(TheTestClass::class.java.findMethod("method2")),
        "method3" to ElementDescriptor.forMethod(TheTestClass::class.java.findMethod("method3")),
        "method4" to ElementDescriptor.forMethod(TheTestClass::class.java.findMethod("method4")),
        "method5" to ElementDescriptor.forMethod(TheTestClass::class.java.findMethod("method5"))
    )
    private val properties = mapOf(
        "prop1" to ElementDescriptor.forProperty(TheTestClass::prop1),
        "prop2" to ElementDescriptor.forProperty(TheTestClass::prop2),
        "prop3" to ElementDescriptor.forProperty(TheTestClass::prop3),
        "prop4" to ElementDescriptor.forProperty(TheTestClass::prop4),
        "prop5" to ElementDescriptor.forProperty(TheTestClass::prop5),
        "aStringField" to ElementDescriptor.forHintedProperty(HintedFields::aStringField, RenderingDirective(UseIdentifierName, "", HintFromProperty, "path"))
    )

    object TestFixtures : FixtureContainer {
        val Fixture1 = fixture("Fixture1") { "fixture1Value" }
        val Fixture2 = fixture("Fixture2") { Wrapper("fixture2Value") }
    }

    private val fixtures = Fixtures()
    private val outputs = CapturedOutputs()

    private val testInstance = TheTestClass()

    private val renderer = TokenRenderer(
        testInstance,
        arguments,
        renderers,
        FixtureAndOutputAccessor(FixturesAndOutputs(fixtures, outputs)),
        parameters,
        properties,
        methods,
        setOf(NamedValue("any", "M"))
    )

    @Nested
    inner class Squashing {

        @Test
        fun squashesMultipleWordTemplateTokensIntoASingleRenderedToken() {
            val templates = listOf(
                Word.asTemplateToken("Word1"),
                Word.asTemplateToken("Word2"),
                Word.asTemplateToken("Word3"),
                Word.asTemplateToken("Word4")
            )

            val expected = listOf(aRenderedValueOf("Word1 Word2 Word3 Word4", setOf("tk-wd")))

            val renderedTokens = renderer.render(templates)

            renderedTokens shouldContainExactly expected
        }

        @Test
        fun squashesMultipleWordTemplateTokensIntoSingleRenderedTokenAndHonoursEmphasis() {
            val emphasis = EmphasisDescriptor(setOf(Italic))

            val templates = listOf(
                Word.asTemplateToken("Word1"),
                Word.asTemplateToken("Word2"),
                Word.asTemplateToken("Word3", emphasis),
                Word.asTemplateToken("Word4", emphasis)
            )

            val expected = listOf(
                aRenderedValueOf("Word1 Word2", setOf("tk-wd")),
                aRenderedValueOf("Word3 Word4", setOf("is-italic", "tk-wd"))
            )

            val renderedTokens = renderer.render(templates)

            renderedTokens shouldContainExactly expected
        }

        @Test
        fun squashesMultipleTemplateTokensContainingWordsAndOtherTypes() {
            val emphasis = EmphasisDescriptor(setOf(Italic))

            val expected = listOf(
                aRenderedValueOf("P1", setOf("tk-id")),
                aRenderedValueOf("Word1 Word2", setOf("is-italic", "tk-wd")),
                aRenderedValueOf("L1", setOf("tk-sl")),
                aRenderedValueOf("", setOf("tk-null")),
                aRenderedValueOf("K1", setOf("tk-kw")),
                aRenderedValueOf("LA1", setOf("tk-ac", "tk-sl")),
                aRenderedValueOf("", setOf("tk-nl")),
                aRenderedValueOf("FOO", setOf("tk-ac")),
                aRenderedValueOf("K2", setOf("tk-kw")),
                aRenderedValueOf("Word3 Word4", setOf("tk-wd")),
                aRenderedValueOf("BOO", setOf("tk-ac")),
                aRenderedValueOf("true", setOf("tk-bo")),
            )

            val templates =
                listOf(
                    Identifier.asTemplateToken("P1"),
                    Word.asTemplateToken("Word1", emphasis),
                    Word.asTemplateToken("Word2", emphasis),
                    StringLiteral.asTemplateToken("L1"),
                    NullLiteral.asTemplateToken(),
                    Keyword.asTemplateToken("K1"),
                    StringLiteral.asTemplateToken("LA1", emphasis = EmphasisDescriptor.Default, Acronym),
                    NewLine.asTemplateToken(),
                    Acronym.asTemplateToken("FOO"),
                    Keyword.asTemplateToken("K2"),
                    Word.asTemplateToken("Word3"),
                    Word.asTemplateToken("Word4"),
                    Acronym.asTemplateToken("BOO"),
                    BooleanLiteral.asTemplateToken("true")
                )

            val renderedTokens = renderer.render(templates)

            renderedTokens shouldContainExactly expected
        }
    }

    @Nested
    inner class Arguments {

        @Test
        fun `renders arguments and honours renderers`() {
            val templates = listOf(
                ParameterValue.asTemplateToken("param1:"),
                Word.asTemplateToken("Word"),
                ParameterValue.asTemplateToken("param2:"),
                ParameterValue.asTemplateToken("param3:"),
                ParameterValue.asTemplateToken("param4:"),
                ParameterValue.asTemplateToken("param5:firstCharacter"),
            )

            val expected = listOf(
                aRenderedValueOf("***arg1***", setOf("tk-pv", "tk-hl")),
                aRenderedValueOf("Word", setOf("tk-wd")),
                aRenderedValueOf("NULL", setOf("tk-pv")),
                aRenderedValueOf("***20***", setOf("tk-pv")),
                aRenderedValueOf("true", setOf("tk-pv")),
                aRenderedValueOf("M", setOf("tk-pv", "tk-hl")),
            )

            val renderedTokens = renderer.render(templates)

            renderedTokens shouldContainExactly expected
        }
    }

    @Nested
    inner class Methods {

        @Test
        fun `renders methods and honours renderers`() {
            val templates = listOf(
                MethodValue.asTemplateToken("method1:"),
                Word.asTemplateToken("Word"),
                MethodValue.asTemplateToken("method2:"),
                MethodValue.asTemplateToken("method3:"),
                MethodValue.asTemplateToken("method4:"),
                MethodValue.asTemplateToken("method5:firstCharacter"),
            )

            val expected = listOf(
                aRenderedValueOf("***method1***", setOf("tk-mv", "tk-hl")),
                aRenderedValueOf("Word", setOf("tk-wd")),
                aRenderedValueOf("NULL", setOf("tk-mv")),
                aRenderedValueOf("***20***", setOf("tk-mv")),
                aRenderedValueOf("true", setOf("tk-mv")),
                aRenderedValueOf("M", setOf("tk-mv", "tk-hl")),
            )

            val renderedTokens = renderer.render(templates)

            renderedTokens shouldContainExactly expected
        }
    }

    @Nested
    inner class Properties {

        @Test
        fun `renders properties and honours renderers`() {
            val templates = listOf(
                FieldValue.asTemplateToken("prop1:"),
                Word.asTemplateToken("Word"),
                FieldValue.asTemplateToken("prop2:"),
                FieldValue.asTemplateToken("prop3:"),
                FieldValue.asTemplateToken("prop4:"),
                FieldValue.asTemplateToken("prop5:firstCharacter"),
            )

            val expected = listOf(
                aRenderedValueOf("***prop1***", setOf("tk-fv", "tk-hl")),
                aRenderedValueOf("Word", setOf("tk-wd")),
                aRenderedValueOf("NULL", setOf("tk-fv")),
                aRenderedValueOf("***20***", setOf("tk-fv")),
                aRenderedValueOf("true", setOf("tk-fv")),
                aRenderedValueOf("M", setOf("tk-fv", "tk-hl")),
            )

            val renderedTokens = renderer.render(templates)

            renderedTokens shouldContainExactly expected
        }
    }

    @Nested
    inner class Hinted {

        @Test
        fun `renders hinted properties and honours renderers`() {
            val templates = listOf(
                FieldValue.asTemplateToken("aStringField:")
            )

            val renderedTokens = renderer.render(templates)

            val expected = listOf(
                aRenderedValueOf("***aStringField***", setOf("tk-fv", "tk-hi"), "path/to/string")
            )

            renderedTokens shouldContainExactly expected
        }
    }

    @Nested
    inner class Fixture {

        @BeforeEach
        fun setUp() {
            FixtureRegistry.registerFixtures(TestFixtures)
        }

        @Test
        fun `renders fixtures`() {
            val templates = listOf(
                FixturesValue.asTemplateToken("Fixture1:"),
                FixturesValue.asTemplateToken("Fixture2:value")
            )

            val expected = listOf(
                aRenderedValueOf("***fixture1Value***", setOf("tk-fv")),
                aRenderedValueOf("***fixture2Value***", setOf("tk-fv"))
            )

            val renderedTokens = renderer.render(templates)

            renderedTokens shouldContainExactly expected
        }
    }

    private fun parameterWithAnnotations(vararg theAnnotations: Highlight) = mock<Parameter> {
        on { annotations }.thenReturn(theAnnotations)
    }
}