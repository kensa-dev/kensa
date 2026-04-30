package dev.kensa.parse

import dev.kensa.Configuration
import dev.kensa.attachments.Attachments
import dev.kensa.context.ExpandableInvocationContext
import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.context.TestContext
import dev.kensa.example.KotlinWithScenario
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.parse.kotlin.KotlinParser
import dev.kensa.parse.kotlin.KotlinParserDelegate
import dev.kensa.render.diagram.SequenceDiagramFactory
import dev.kensa.state.TestInvocationContext
import dev.kensa.state.TestInvocationFactory
import dev.kensa.state.TestState.Passed
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.milliseconds

class ParseAndRenderIntegrationTest {

    @Test
    fun `clean method produces empty parseErrors through full pipeline`() {
        val configuration = Configuration().apply {
            sourceLocations = listOf(Path("src/example/kotlin"))
        }

        val methodName = "testWithScenarioField"
        val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == methodName }

        val parser = MethodParser(
            ParserCache(),
            configuration,
            CompositeParserDelegate(
                configuration.sourceCode,
                listOf(KotlinParserDelegate(isTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode))
            )
        )

        val method = KotlinWithScenario::class.java.getDeclaredMethod(methodName)
        val parsedMethod = parser.parse(method)

        parsedMethod.parseErrors.shouldBeEmpty()
        parsedMethod.sentences.isNotEmpty() shouldBe true
    }

    @Test
    fun `parseErrors from ParsedMethod flow through TestInvocationParser into TestInvocation`() {
        val parseErrors = listOf(
            ParseError(lineNumber = 42, message = "Could not parse this statement"),
            ParseError(lineNumber = 57, message = "Could not parse this statement")
        )

        val parsedMethod = ParsedMethod(
            indexInSource = 0,
            name = "someMethod",
            parameters = MethodParameters(emptyMap()),
            sentences = emptyList(),
            expandableMethods = emptyMap(),
            properties = emptyMap(),
            methods = emptyMap(),
            parseErrors = parseErrors
        )

        val methodParser: MethodParser = mock {
            on { parse(any()) } doReturn parsedMethod
        }

        val configuration = Configuration()
        val parser = TestInvocationParser(configuration)

        val method = ParseAndRenderIntegrationTest::class.java.getDeclaredMethod(
            "parseErrors from ParsedMethod flow through TestInvocationParser into TestInvocation"
        )
        val context = TestInvocationContext(
            instance = this,
            method = method,
            arguments = emptyArray(),
            displayName = "test",
            startTimeMs = 0L,
            fixtures = Fixtures(),
            capturedOutputs = CapturedOutputs()
        )

        val (_, renderErrors) = parser.parse(context, methodParser)
        val returnedParseErrors = methodParser.parse(method).parseErrors

        returnedParseErrors shouldHaveSize 2
        returnedParseErrors[0].lineNumber shouldBe 42
        returnedParseErrors[1].lineNumber shouldBe 57
        renderErrors.shouldBeEmpty()
    }

    @Test
    fun `render errors from TestInvocationParser are captured in TestInvocation`() {
        val invocationParser: TestInvocationParser = mock()
        val methodParser: MethodParser = mock()
        val diagramFactory: SequenceDiagramFactory = mock()
        val factory = TestInvocationFactory(invocationParser, methodParser, diagramFactory)

        val method = ParseAndRenderIntegrationTest::class.java.getDeclaredMethod(
            "render errors from TestInvocationParser are captured in TestInvocation"
        )
        val context = TestInvocationContext(
            instance = this,
            method = method,
            arguments = emptyArray(),
            displayName = "test",
            startTimeMs = 0L,
            fixtures = Fixtures(),
            capturedOutputs = CapturedOutputs()
        )
        val testContext = mock<TestContext> {
            on { interactions } doReturn mock()
            on { outputs } doReturn mock()
            on { fixtures } doReturn mock()
            on { attachments } doReturn Attachments()
        }
        whenever(diagramFactory.create(any())).thenReturn(null)

        val renderErrors = listOf(
            RenderError("ValueResolution", "field x not found"),
            RenderError("SentenceRender", "token render failed")
        )
        val parseErrors = listOf(
            ParseError(lineNumber = 10, message = "Could not parse this statement")
        )
        val parsedInvocation: ParsedInvocation = mock {
            on { sentences } doReturn emptyList()
            on { namedParameterValues } doReturn emptyList()
            on { highlightedValues } doReturn emptySet()
            on { parameterizedTestDescription } doReturn null
            on { indexInSource } doReturn 0
            on { name } doReturn method.name
        }
        val parsedMethod = ParsedMethod(
            indexInSource = 0,
            name = method.name,
            parameters = MethodParameters(emptyMap()),
            sentences = emptyList(),
            expandableMethods = emptyMap(),
            properties = emptyMap(),
            methods = emptyMap(),
            parseErrors = parseErrors
        )

        whenever(invocationParser.parse(any(), any())).thenReturn(parsedInvocation to renderErrors)
        whenever(methodParser.parse(any())).thenReturn(parsedMethod)

        val (invocation, returnedParseErrors) = factory.create(10.milliseconds, testContext, context, null, "test")

        invocation.state shouldBe Passed

        invocation.renderErrors shouldHaveSize 2
        invocation.renderErrors[0].type shouldBe "ValueResolution"
        invocation.renderErrors[0].message shouldBe "field x not found"
        invocation.renderErrors[1].type shouldBe "SentenceRender"

        returnedParseErrors shouldHaveSize 1
        returnedParseErrors[0].lineNumber shouldBe 10
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            ExpandableInvocationContextHolder.bindToCurrentThread(ExpandableInvocationContext())
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            ExpandableInvocationContextHolder.clearFromThread()
        }
    }
}
