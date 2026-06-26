package dev.kensa.parse

import dev.kensa.Configuration
import dev.kensa.Highlight
import dev.kensa.ParameterizedTestDescription
import dev.kensa.context.ExpandableInvocationContext
import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.parse.ElementDescriptor.Companion.forParameter
import dev.kensa.state.TestInvocationContext
import dev.kensa.util.NamedValue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class TestInvocationParserTest {

    @Suppress("unused")
    private class Sample {
        fun aTest(p1: String, @Highlight hl: String, @ParameterizedTestDescription desc: String) {}
    }

    private val sampleMethod = Sample::class.java.getDeclaredMethod("aTest", String::class.java, String::class.java, String::class.java)

    private fun parsedMethodWithParameters(): ParsedMethod {
        val params = sampleMethod.parameters
        val descriptors = linkedMapOf<String, ElementDescriptor>(
            "p1" to forParameter(params[0], "p1", 0),
            "hl" to forParameter(params[1], "hl", 1),
            "desc" to forParameter(params[2], "desc", 2),
        )
        return ParsedMethod(
            indexInSource = 0,
            name = "aTest",
            parameters = MethodParameters(descriptors),
            sentences = emptyList(),
            expandableMethods = emptyMap(),
            properties = emptyMap(),
            methods = emptyMap(),
            parseErrors = emptyList()
        )
    }

    private fun parse(configuration: Configuration): ParsedInvocation {
        val methodParser: MethodParser = mock { on { parse(any()) } doReturn parsedMethodWithParameters() }
        val context = TestInvocationContext(
            instance = Sample(),
            method = sampleMethod,
            arguments = arrayOf("raw", "hot", "descval"),
            displayName = "test",
            startTimeMs = 0L,
            fixtures = Fixtures(),
            capturedOutputs = CapturedOutputs()
        )
        return TestInvocationParser(configuration).parse(context, methodParser).first
    }

    @Test
    fun `stores raw parameter values so they are rendered once at output`() {
        val configuration = Configuration().apply { renderers.addValueRenderer(String::class) { v -> "<$v>" } }

        val invocation = parse(configuration)

        invocation.namedParameterValues shouldContain NamedValue("p1", "raw")
    }

    @Test
    fun `renders highlighted parameter values so sentence highlighting still matches`() {
        val configuration = Configuration().apply { renderers.addValueRenderer(String::class) { v -> "<$v>" } }

        val invocation = parse(configuration)

        invocation.highlightedValues shouldContain NamedValue("hl", "<hot>")
    }

    @Test
    fun `renders the parameterized test description through registered renderers`() {
        val configuration = Configuration().apply { renderers.addValueRenderer(String::class) { v -> "<$v>" } }

        val invocation = parse(configuration)

        invocation.parameterizedTestDescription shouldBe "<descval>"
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
