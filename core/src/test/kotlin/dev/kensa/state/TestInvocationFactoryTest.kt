package dev.kensa.state

import dev.kensa.KensaException
import dev.kensa.context.TestContext
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.parse.*
import dev.kensa.render.diagram.SequenceDiagramFactory
import dev.kensa.state.TestState.Passed
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.milliseconds

class TestInvocationFactoryTest {

    private val parser: MethodParser = mock()
    private val invocationParser: TestInvocationParser = mock()
    private val diagramFactory: SequenceDiagramFactory = mock()
    private val factory = TestInvocationFactory(invocationParser, parser, diagramFactory)

    @Test
    fun `returns invocation with error sentences when parsing fails`() {
        val method = TestInvocationFactoryTest::class.java.getDeclaredMethod("returns invocation with error sentences when parsing fails")
        val instance = Any()
        val context = TestInvocationContext(instance, method, emptyArray(), "display", 0L, Fixtures(), CapturedOutputs())
        val testContext = mock<TestContext> {
            on { interactions } doReturn mock()
            on { outputs } doReturn mock()
            on { fixtures } doReturn mock()
        }
        whenever(diagramFactory.create(any())).thenReturn(null)

        val cause = RuntimeException("root cause detail")
        whenever(invocationParser.parse(any(), any())).thenThrow(KensaException("parse failed", RuntimeException("parse failed", cause)))

        val (invocation) = factory.create(10.milliseconds, testContext, context, null, "display")

        invocation.state shouldBe Passed
        invocation.sentences.shouldHaveSize(5)
        invocation.sentences[0].tokens[0].value shouldContain "unable to parse"
        invocation.sentences[1].tokens[0].value shouldContain "https://github.com/kensa-dev/kensa/issues"
        invocation.sentences[3].tokens[0].value shouldContain "parse failed"
        invocation.sentences[3].tokens[0].value shouldContain "root cause detail"
    }

    @Test
    fun `returns normal invocation when parsing succeeds`() {
        val method = TestInvocationFactoryTest::class.java.getDeclaredMethod("returns normal invocation when parsing succeeds")
        val instance = Any()
        val context = TestInvocationContext(instance, method, emptyArray(), "display", 0L, Fixtures(), CapturedOutputs())
        val testContext = mock<TestContext> {
            on { interactions } doReturn mock()
            on { outputs } doReturn mock()
            on { fixtures } doReturn mock()
        }
        whenever(diagramFactory.create(any())).thenReturn(null)

        val parsedInvocation = mock<ParsedInvocation> {
            on { sentences } doReturn emptyList()
            on { namedParameterValues } doReturn emptyList()
            on { highlightedValues } doReturn emptySet()
            on { parameterizedTestDescription } doReturn null
            on { indexInSource } doReturn 0
            on { name } doReturn method.name
        }

        val parsedMethod = ParsedMethod(0, method.name, MethodParameters(emptyMap()), emptyList(), emptyMap(), emptyMap(), emptyMap())
        whenever(invocationParser.parse(any(), any())).thenReturn(parsedInvocation to emptyList<RenderError>())
        whenever(parser.parse(any())).thenReturn(parsedMethod)

        val (invocation) = factory.create(10.milliseconds, testContext, context, null, "display")

        invocation.sentences shouldBe emptyList()
    }
}
