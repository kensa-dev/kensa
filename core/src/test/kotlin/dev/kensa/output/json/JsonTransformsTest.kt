package dev.kensa.output.json

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonValue
import com.eclipsesource.json.WriterConfig
import dev.kensa.KensaException
import dev.kensa.fixture.FixtureSpec
import dev.kensa.parse.ParseError
import dev.kensa.parse.RenderError
import dev.kensa.render.Renderers
import dev.kensa.render.diagram.SequenceDiagram
import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.RenderedToken
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.TestState
import dev.kensa.util.Attributes
import dev.kensa.util.NamedValue
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.io.IOException
import java.io.Writer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class JsonTransformsTest {

    private class SampleTest {
        @Suppress("unused") fun alpha() = Unit
        @Suppress("unused") fun beta() = Unit
    }

    private val renderers = Renderers()
    private val sampleClass = SampleTest::class.java
    private val alpha: java.lang.reflect.Method = sampleClass.getDeclaredMethod("alpha")
    private val beta: java.lang.reflect.Method = sampleClass.getDeclaredMethod("beta")

    private fun render(container: dev.kensa.context.TestContainer) =
        JsonTransforms.toJsonWith(renderers)(container).asObject()

    @Nested
    inner class TopLevelShape {

        @Test
        fun `populates class-level fields`() {
            val container = fakeTestContainer(
                testClass = sampleClass,
                displayName = "Sample",
                state = TestState.Failed,
                notes = "a note",
                issues = listOf("X1", "X2"),
                methodContainers = emptyList(),
            )

            val json = render(container)

            json.getString("testClass", null) shouldBe sampleClass.name
            json.getString("displayName", null) shouldBe "Sample"
            json.getString("state", null) shouldBe TestState.Failed.description
            json.getString("notes", null) shouldBe "a note"
            json.getString("packageName", null) shouldBe sampleClass.packageName
            val issues = json.get("issues").asArray()
            issues.size() shouldBe 2
            issues[0].asString() shouldBe "X1"
            issues[1].asString() shouldBe "X2"
            json.get("tests").asArray().size() shouldBe 0
        }
    }

    @Nested
    inner class InvocationFields {

        @Test
        fun `populates invocation-level fields and aggregates elapsed time`() {
            val invocation1 = fakeTestInvocation(elapsed = 100.milliseconds, displayName = "inv-1")
            val invocation2 = fakeTestInvocation(elapsed = 250.milliseconds, displayName = "inv-2")
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation1, invocation2))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val tests = render(container).get("tests").asArray()
            tests.size() shouldBe 1
            val methodJson = tests[0].asObject()

            methodJson.getString("testMethod", null) shouldBe "alpha"
            methodJson.getString("elapsedTime", null) shouldBe "350 Ms"
            val invocations = methodJson.get("invocations").asArray()
            invocations.size() shouldBe 2
            invocations[0].asObject().getString("displayName", null) shouldBe "inv-1"
            invocations[1].asObject().getString("displayName", null) shouldBe "inv-2"
        }

        @Test
        fun `parameterizedTestDescription takes precedence over displayName`() {
            val invocation = fakeTestInvocation(
                displayName = "plain",
                parameterizedTestDescription = "a param run",
            )
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val invJson = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()

            invJson.getString("displayName", null) shouldBe "a param run"
        }

        @Test
        fun `serialises highlights outputs fixtures and parameters`() {
            val invocation = fakeTestInvocation(
                highlightedValues = setOf(NamedValue("h", "H-VAL")),
                parameters = listOf(NamedValue("p1", 42)),
                outputNamesAndValues = setOf(NamedValue("o1", "O")),
                fixturesNamesAndValues = listOf(NamedValue("f1", "F")),
                fixtureSpecs = listOf(FixtureSpec("f1", listOf("parentA"))),
            )
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val invJson = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()

            invJson.get("highlights").asArray()[0].asString() shouldBe "H-VAL"
            invJson.get("parameters").asArray()[0].asObject().getString("p1", null) shouldBe "42"
            invJson.get("capturedOutputs").asArray()[0].asObject().getString("o1", null) shouldBe "O"
            invJson.get("fixtures").asArray()[0].asObject().getString("f1", null) shouldBe "F"
            val spec = invJson.get("fixtureSpecs").asArray()[0].asObject()
            spec.getString("key", null) shouldBe "f1"
            spec.get("parents").asArray()[0].asString() shouldBe "parentA"
        }

        @Test
        fun `serialises sequence diagram via toString`() {
            val invocation = fakeTestInvocation(sequenceDiagram = SequenceDiagram("<svg/>"))
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .getString("sequenceDiagram", null) shouldBe "<svg/>"
        }

        @Test
        fun `serialises null sequence diagram as null`() {
            val invocation = fakeTestInvocation(sequenceDiagram = null)
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("sequenceDiagram").isNull shouldBe true
        }
    }

    @Nested
    inner class Interactions {

        @Test
        fun `parses from and to from interaction key`() {
            val invocation = fakeTestInvocation(
                interactions = setOf(interactionEntry(key = "Call from ClientA to ServiceB"))
            )
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val interaction = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("capturedInteractions").asArray()[0].asObject()

            interaction.getString("name", null) shouldBe "Call from ClientA to ServiceB"
            interaction.getString("from", null) shouldBe "ClientA"
            interaction.getString("to", null) shouldBe "ServiceB"
        }

        @Test
        fun `leaves from and to null when key does not match pattern`() {
            val invocation = fakeTestInvocation(
                interactions = setOf(interactionEntry(key = "just a plain key"))
            )
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val interaction = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("capturedInteractions").asArray()[0].asObject()

            interaction.get("from").isNull shouldBe true
            interaction.get("to").isNull shouldBe true
        }

        @Test
        fun `filters out entries with curly-brace-prefixed keys`() {
            val invocation = fakeTestInvocation(
                interactions = setOf(
                    interactionEntry(key = "{ignored} something"),
                    interactionEntry(key = "keep this"),
                )
            )
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val array = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("capturedInteractions").asArray()

            array.size() shouldBe 1
            array[0].asObject().getString("name", null) shouldBe "keep this"
        }

        @Test
        fun `filters out sdMarker entries`() {
            val invocation = fakeTestInvocation(
                interactions = setOf(
                    interactionEntry(key = CapturedInteractions.sdMarkerKey, value = "...marker..."),
                    interactionEntry(key = "a real one"),
                )
            )
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val array = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("capturedInteractions").asArray()

            array.size() shouldBe 1
            array[0].asObject().getString("name", null) shouldBe "a real one"
        }

        @Test
        fun `uses explicit InteractionId attribute when present`() {
            val invocation = fakeTestInvocation(
                interactions = setOf(
                    interactionEntry(
                        key = "the call",
                        attributes = Attributes.of(Attributes.Key.InteractionId, "id-42"),
                    )
                )
            )
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val interaction = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("capturedInteractions").asArray()[0].asObject()

            interaction.getString("id", null) shouldBe "id-42"
        }
    }

    @Nested
    inner class ExecutionException {

        @Test
        fun `serialises null exception as empty object`() {
            val invocation = fakeTestInvocation(executionException = null)
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val exJson = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("executionException").asObject()

            exJson.size() shouldBe 0
        }

        @Test
        fun `serialises populated exception with message and stackTrace`() {
            val invocation = fakeTestInvocation(executionException = RuntimeException("boom"))
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val exJson = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("executionException").asObject()

            exJson.getString("message", null) shouldBe "boom"
            exJson.getString("stackTrace", "") shouldContain "RuntimeException"
        }
    }

    @Nested
    inner class CustomTabs {

        @Test
        fun `includes custom tabs for an invocation`() {
            val invocation = fakeTestInvocation()
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val tab = JsonTransforms.CustomTabContent(tabId = "t1", label = "Tab 1", file = "t1.txt")
            val json = JsonTransforms.toJsonWith(renderers) { _, _, _ -> listOf(tab) }(container).asObject()

            val tabJson = json.get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("customTabContents").asArray()[0].asObject()

            tabJson.getString("tabId", null) shouldBe "t1"
            tabJson.getString("label", null) shouldBe "Tab 1"
            tabJson.getString("file", null) shouldBe "t1.txt"
            tabJson.getString("mediaType", null) shouldBe "text/plain"
        }

        @Test
        fun `respects custom mediaType`() {
            val invocation = fakeTestInvocation()
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val tab = JsonTransforms.CustomTabContent(tabId = "t1", label = "L", file = "t1.html", mediaType = "text/html")
            val json = JsonTransforms.toJsonWith(renderers) { _, _, _ -> listOf(tab) }(container).asObject()

            json.get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("customTabContents").asArray()[0].asObject()
                .getString("mediaType", null) shouldBe "text/html"
        }

        @Test
        fun `omits customTabContents when producer returns empty`() {
            val invocation = fakeTestInvocation()
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val invJson = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()

            invJson.get("customTabContents").shouldBeNull()
        }
    }

    @Nested
    inner class Errors {

        @Test
        fun `invocation includes renderErrors when non-empty`() {
            val errors = listOf(
                RenderError("ValueResolution", "field x not found"),
                RenderError("SentenceRender", "render failed"),
            )
            val invocation = fakeTestInvocation(renderErrors = errors)
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val arr = render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("renderErrors").asArray()

            arr.size() shouldBe 2
            arr[0].asObject().getString("type", null) shouldBe "ValueResolution"
            arr[0].asObject().getString("message", null) shouldBe "field x not found"
        }

        @Test
        fun `invocation omits renderErrors key when empty`() {
            val invocation = fakeTestInvocation(renderErrors = emptyList())
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("renderErrors")
                .shouldBeNull()
        }

        @Test
        fun `method includes parseErrors when non-empty`() {
            val errors = listOf(
                ParseError(lineNumber = 5, message = "unexpected token"),
                ParseError(lineNumber = 12, message = "missing semicolon"),
            )
            val method = fakeTestMethodContainer(method = alpha, parseErrors = errors, invocations = listOf(fakeTestInvocation()))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            val arr = render(container)
                .get("tests").asArray()[0].asObject()
                .get("parseErrors").asArray()

            arr.size() shouldBe 2
            arr[0].asObject().getInt("line", 0) shouldBe 5
            arr[0].asObject().getString("message", null) shouldBe "unexpected token"
            arr[1].asObject().getInt("line", 0) shouldBe 12
        }

        @Test
        fun `method omits parseErrors when empty`() {
            val method = fakeTestMethodContainer(method = alpha, parseErrors = emptyList(), invocations = listOf(fakeTestInvocation()))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            render(container)
                .get("tests").asArray()[0].asObject()
                .get("parseErrors")
                .shouldBeNull()
        }
    }

    @Nested
    inner class SentenceTokens {

        private fun firstTokenJson(token: RenderedToken): JsonValue {
            val invocation = fakeTestInvocation(sentences = listOf(RenderedSentence(listOf(token), lineNumber = 1)))
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))
            return render(container)
                .get("tests").asArray()[0].asObject()
                .get("invocations").asArray()[0].asObject()
                .get("sentences").asArray()[0].asObject()
                .get("tokens").asArray()[0]
        }

        @Test
        fun `simple RenderedValueToken serialises types value and hint`() {
            val token = RenderedToken.RenderedValueToken(value = "v", cssClasses = setOf("kw", "x"), hint = "a hint")

            val obj = firstTokenJson(token).asObject()

            obj.get("types").asArray().size() shouldBe 2
            obj.getString("value", null) shouldBe "v"
            obj.getString("hint", null) shouldBe "a hint"
        }

        @Test
        fun `simple RenderedValueToken omits hint when null`() {
            val token = RenderedToken.RenderedValueToken(value = "v", cssClasses = setOf("kw"))

            val obj = firstTokenJson(token).asObject()

            obj.get("hint").shouldBeNull()
        }

        @Test
        fun `ErrorToken nests error structure`() {
            val token = RenderedToken.ErrorToken("something went wrong")

            val obj = firstTokenJson(token).asObject()

            obj.get("types").asArray().size() shouldBe 0
            obj.getString("value", null) shouldBe "something went wrong"
            obj.getString("hint", null) shouldBe "something went wrong"
            val inner = obj.get("tokens").asArray()[0].asObject()
            inner.getString("type", null) shouldBe "error"
            inner.getString("text", null) shouldBe "⚠ parse error"
            inner.getString("hint", null) shouldBe "something went wrong"
        }

        @Test
        fun `RenderedExpandableToken serialises parameterTokens and nested tokens`() {
            val token = RenderedToken.RenderedExpandableToken(
                value = "expand",
                cssClasses = setOf("ex"),
                name = "exp",
                parameterTokens = listOf(RenderedToken.RenderedValueToken(value = "p", cssClasses = setOf("w"))),
                expandableTokens = listOf(
                    listOf(RenderedToken.RenderedValueToken(value = "a", cssClasses = setOf("w"))),
                    listOf(RenderedToken.RenderedValueToken(value = "b", cssClasses = setOf("w"))),
                ),
            )

            val obj = firstTokenJson(token).asObject()

            obj.get("parameterTokens").asArray().size() shouldBe 1
            val groups = obj.get("tokens").asArray()
            groups.size() shouldBe 2
            groups[0].asArray()[0].asObject().getString("value", null) shouldBe "a"
            groups[1].asArray()[0].asObject().getString("value", null) shouldBe "b"
        }

        @Test
        fun `RenderedExpandableTabularToken serialises as table with headers and rows`() {
            val token = RenderedToken.RenderedExpandableTabularToken(
                value = "tbl",
                cssClasses = setOf("ex"),
                name = "tbl",
                parameterTokens = emptyList(),
                rows = listOf(
                    listOf(RenderedToken.RenderedValueToken(value = "r1c1", cssClasses = setOf("w"))),
                    listOf(RenderedToken.RenderedValueToken(value = "r2c1", cssClasses = setOf("w"))),
                ),
                headers = listOf("col1"),
            )

            val obj = firstTokenJson(token).asObject()

            val table = obj.get("tokens").asArray()[0].asObject()
            table.getString("type", null) shouldBe "table"
            table.get("headers").asArray()[0].asString() shouldBe "col1"
            val rows = table.get("rows").asArray()
            rows.size() shouldBe 2
            rows[0].asArray()[0].asObject().getString("value", null) shouldBe "r1c1"
        }
    }

    @Nested
    inner class IndexJson {

        @Test
        fun `serialises class and methods with hasErrors absent when no errors`() {
            val methodA = fakeTestMethodContainer(method = alpha, invocations = listOf(fakeTestInvocation()))
            val methodB = fakeTestMethodContainer(method = beta, invocations = listOf(fakeTestInvocation()))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(methodA, methodB))

            val json = JsonTransforms.toIndexJson(id = "cls-1")(container).asObject()

            json.getString("id", null) shouldBe "cls-1"
            json.getString("testClass", null) shouldBe sampleClass.name
            val tests = json.get("tests").asArray()
            tests.size() shouldBe 2
            tests[0].asObject().get("hasErrors").shouldBeNull()
            tests[1].asObject().get("hasErrors").shouldBeNull()
        }

        @Test
        fun `sets hasErrors true on method with parseErrors`() {
            val methodA = fakeTestMethodContainer(method = alpha, invocations = listOf(fakeTestInvocation()))
            val methodB = fakeTestMethodContainer(
                method = beta,
                invocations = listOf(fakeTestInvocation()),
                parseErrors = listOf(ParseError(1, "oops")),
            )
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(methodA, methodB))

            val tests = JsonTransforms.toIndexJson(id = "cls-1")(container).asObject().get("tests").asArray()
            val byName = tests.map { it.asObject() }.associateBy { it.getString("testMethod", null) }

            byName["alpha"]!!.get("hasErrors").shouldBeNull()
            byName["beta"]!!.getBoolean("hasErrors", false).shouldBeTrue()
        }

        @Test
        fun `sets hasErrors true on method with renderErrors in any invocation`() {
            val invocationWithErrors = fakeTestInvocation(renderErrors = listOf(RenderError("R", "m")))
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocationWithErrors))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            JsonTransforms.toIndexJson(id = "cls-1")(container).asObject()
                .get("tests").asArray()[0].asObject()
                .getBoolean("hasErrors", false)
                .shouldBeTrue()
        }
    }

    @Nested
    inner class ModernIndexJson {

        @Test
        fun `omits class-level hasErrors when no children have errors`() {
            val methodA = fakeTestMethodContainer(method = alpha, invocations = listOf(fakeTestInvocation()))
            val methodB = fakeTestMethodContainer(method = beta, invocations = listOf(fakeTestInvocation()))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(methodA, methodB))

            val json = JsonTransforms.toModernIndexJson(id = "cls-1")(container).asObject()

            json.get("hasErrors").shouldBeNull()
            val children = json.get("children").asArray()
            children.size() shouldBe 2
            children[0].asObject().get("hasErrors").shouldBeNull()
            children[1].asObject().get("hasErrors").shouldBeNull()
        }

        @Test
        fun `propagates hasErrors from a child to the class level`() {
            val methodA = fakeTestMethodContainer(method = alpha, invocations = listOf(fakeTestInvocation()))
            val methodB = fakeTestMethodContainer(
                method = beta,
                invocations = listOf(fakeTestInvocation(renderErrors = listOf(RenderError("R", "m")))),
            )
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(methodA, methodB))

            val json = JsonTransforms.toModernIndexJson(id = "cls-1")(container).asObject()

            json.getBoolean("hasErrors", false).shouldBeTrue()
            val children = json.get("children").asArray()
            val byName = children.map { it.asObject() }.associateBy { it.getString("testMethod", null) }
            byName["alpha"]!!.get("hasErrors").shouldBeNull()
            byName["beta"]!!.getBoolean("hasErrors", false).shouldBeTrue()
        }

        @Test
        fun `child id is classId colon methodName`() {
            val method = fakeTestMethodContainer(method = alpha, invocations = listOf(fakeTestInvocation()))
            val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

            JsonTransforms.toModernIndexJson(id = "cls-1")(container).asObject()
                .get("children").asArray()[0].asObject()
                .getString("id", null) shouldBe "cls-1:alpha"
        }
    }

    @Nested
    inner class JsonString {

        @Test
        fun `serialises a JsonValue as compact string`() {
            val value = Json.`object`().add("x", 1).add("y", "hello")

            JsonTransforms.toJsonString()(value) shouldBe """{"x":1,"y":"hello"}"""
        }

        @Test
        fun `wraps IOException from writer in KensaException`() {
            val failing = spy(Json.`object`().add("x", 1))
            doAnswer { throw IOException("boom") }
                .whenever(failing).writeTo(any<Writer>(), any<WriterConfig>())

            val ex = assertThrows<KensaException> { JsonTransforms.toJsonString()(failing) }
            ex.message shouldStartWith "Unable to write Json string"
        }
    }
}
