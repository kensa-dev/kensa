package dev.kensa.tabs

import dev.kensa.Configuration
import dev.kensa.KensaTab
import dev.kensa.KensaTabVisibility
import dev.kensa.attachments.Attachments
import dev.kensa.context.TestContainer
import dev.kensa.fixture.Fixtures
import dev.kensa.output.json.JsonTransforms
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.service.logs.LogQueryService
import dev.kensa.service.logs.LogRecord
import dev.kensa.state.TestInvocation
import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState
import dev.kensa.tabs.logs.LogsTabRenderer
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readLines
import kotlin.io.path.readText

class TabArtifactManagerTest {

    private class ExampleTest {
        @KensaTab(
            id = "",
            name = "My Tab",
            renderer = CapturingRenderer::class,
            identifierProvider = CapturingIdentifierProvider::class,
            visibility = KensaTabVisibility.OnlyOnFailure
        )
        @Suppress("unused")
        fun exampleTestMethod() = Unit
    }

    object CapturingIdentifierProvider : InvocationIdentifierProvider {
        @Volatile
        var lastInvocationIdentifierInCtx: String? = "not-set"

        override fun identifier(ctx: KensaTabContext): String {
            lastInvocationIdentifierInCtx = ctx.invocationIdentifier
            return "inv-123"
        }
    }

    object CapturingRenderer : KensaTabRenderer {
        @Volatile
        var lastInvocationIdentifierInCtx: String? = "not-set"

        override fun render(ctx: KensaTabContext): String {
            lastInvocationIdentifierInCtx = ctx.invocationIdentifier
            return "rendered for ${ctx.invocationIdentifier}"
        }
    }

    private class HelloTest {
        @KensaTab(
            id = "",
            name = "Hello Tab",
            renderer = HelloRenderer::class,
        )
        @Suppress("unused")
        fun exampleTestMethod() = Unit
    }

    object HelloRenderer : KensaTabRenderer {
        override fun render(ctx: KensaTabContext): String = "hello"
    }

    private class NullTest {
        @KensaTab(
            id = "",
            name = "Null Tab",
            renderer = NullRenderer::class,
        )
        @Suppress("unused")
        fun exampleTestMethod() = Unit
    }

    object NullRenderer : KensaTabRenderer {
        override fun render(ctx: KensaTabContext): String? = null
    }

    private class LogsTest {
        @KensaTab(
            id = "",
            name = "Logs",
            renderer = LogsTabRenderer::class,
            identifierProvider = FixedIdentifierProvider::class,
            sourceId = "app"
        )
        @Suppress("unused")
        fun exampleTestMethod() = Unit
    }

    object FixedIdentifierProvider : InvocationIdentifierProvider {
        override fun identifier(ctx: KensaTabContext): String = "inv-9"
    }

    private class FakeLogQueryService(private val records: List<LogRecord>) : LogQueryService {
        override fun query(sourceId: String, identifier: String): List<LogRecord> =
            records.filter { it.sourceId == sourceId && it.identifier == identifier }

        override fun queryAll(sourceId: String): List<LogRecord> = records.filter { it.sourceId == sourceId }
    }

    private fun configurationWith(vararg records: LogRecord) = Configuration().apply {
        registerTabService(LogQueryService::class) { FakeLogQueryService(records.toList()) }
    }

    @TempDir
    lateinit var tempDir: Path

    private val manager = TabArtifactManager()

    @Test
    fun `generate passes baseCtx with null invocationIdentifier to provider and ctx with populated invocationIdentifier to renderer`() {
        CapturingIdentifierProvider.lastInvocationIdentifierInCtx = "not-set"
        CapturingRenderer.lastInvocationIdentifierInCtx = "not-set"

        val testClass = ExampleTest::class.java

        val invocation = mock<TestInvocation> {
            on { it.parameterizedTestDescription } doReturn (null)
            on { it.displayName } doReturn "display"
            on { it.fixtures } doReturn Fixtures()
            on { it.outputs } doReturn CapturedOutputs()
            on { it.attachments } doReturn Attachments()
            on { it.state } doReturn TestState.Failed
        }

        val methodContainer = mock<TestMethodContainer> {
            on { it.method } doReturn testClass.getDeclaredMethod("exampleTestMethod")
            on { it.invocations } doReturn listOf(invocation)
        }

        val container = mock<TestContainer> {
            on { it.testClass } doReturn testClass
            on { it.orderedMethodContainers } doReturn listOf(methodContainer)
        }

        val result = manager.generate(container = container, outputDir = tempDir, configuration = Configuration())

        result.entries shouldHaveSize 1
        val (key, tabs) = result.entries.single()
        key.testMethod shouldBe "exampleTestMethod"
        key.invocationIndex shouldBe 0

        tabs shouldHaveSize 1
        val tab: JsonTransforms.CustomTabContent = tabs.single()

        CapturingIdentifierProvider.lastInvocationIdentifierInCtx shouldBe null

        CapturingRenderer.lastInvocationIdentifierInCtx shouldBe "inv-123"

        tempDir.resolve(tab.file!!).should { outputFile ->
            outputFile.shouldExist()
            outputFile.readText() shouldBe "rendered for inv-123"
        }
    }

    @Test
    fun `generate records a skipped entry for OnlyOnFailure tabs on passed invocations without rendering`() {
        CapturingIdentifierProvider.lastInvocationIdentifierInCtx = "not-set"
        CapturingRenderer.lastInvocationIdentifierInCtx = "not-set"

        val testClass = ExampleTest::class.java

        val invocation = mock<TestInvocation> {
            on { it.parameterizedTestDescription } doReturn (null)
            on { it.displayName } doReturn "display"
            on { it.fixtures } doReturn Fixtures()
            on { it.outputs } doReturn CapturedOutputs()
            on { it.state } doReturn TestState.Passed
        }

        val methodContainer = mock<TestMethodContainer> {
            on { it.method } doReturn testClass.getDeclaredMethod("exampleTestMethod")
            on { it.invocations } doReturn listOf(invocation)
        }

        val container = mock<TestContainer> {
            on { it.testClass } doReturn testClass
            on { it.orderedMethodContainers } doReturn listOf(methodContainer)
        }

        val result = manager.generate(container = container, outputDir = tempDir, configuration = Configuration())

        result.entries shouldHaveSize 1
        val tab: JsonTransforms.CustomTabContent = result.entries.single().value.single()

        tab.tabId.shouldNotBeBlank()
        tab.label shouldBe "My Tab"
        tab.sourceId.shouldBeNull()
        tab.visibility shouldBe "OnlyOnFailure"
        tab.file.shouldBeNull()
        tab.mediaType.shouldBeNull()
        tab.identifier.shouldBeNull()
        tab.entries.shouldBeNull()
        tab.records.shouldBeNull()

        CapturingIdentifierProvider.lastInvocationIdentifierInCtx shouldBe "not-set"
        CapturingRenderer.lastInvocationIdentifierInCtx shouldBe "not-set"

        Files.walk(tempDir).use { paths ->
            paths.filter { Files.isRegularFile(it) }.count() shouldBe 0
        }
    }

    @Test
    fun `generate writes the file and returns a single CustomTabContent when the renderer returns content`() {
        val testClass = HelloTest::class.java

        val invocation = mock<TestInvocation> {
            on { it.parameterizedTestDescription } doReturn (null)
            on { it.displayName } doReturn "display"
            on { it.fixtures } doReturn Fixtures()
            on { it.outputs } doReturn CapturedOutputs()
            on { it.attachments } doReturn Attachments()
            on { it.state } doReturn TestState.Passed
        }

        val methodContainer = mock<TestMethodContainer> {
            on { it.method } doReturn testClass.getDeclaredMethod("exampleTestMethod")
            on { it.invocations } doReturn listOf(invocation)
        }

        val container = mock<TestContainer> {
            on { it.testClass } doReturn testClass
            on { it.orderedMethodContainers } doReturn listOf(methodContainer)
        }

        val result = manager.generate(container = container, outputDir = tempDir, configuration = Configuration())

        result.entries shouldHaveSize 1
        val tabs = result.entries.single().value
        tabs shouldHaveSize 1
        val tab: JsonTransforms.CustomTabContent = tabs.single()

        tab.tabId.shouldNotBeBlank()
        tab.label shouldBe "Hello Tab"
        tab.mediaType shouldBe "text/plain"
        tab.sourceId.shouldBeNull()
        tab.identifier.shouldBeNull()
        tab.entries.shouldBeNull()

        tempDir.resolve(tab.file!!).should { outputFile ->
            outputFile.shouldExist()
            outputFile.readText() shouldBe "hello"
        }
    }

    @Test
    fun `generate returns no entry and writes no file when the renderer returns null`() {
        val testClass = NullTest::class.java

        val invocation = mock<TestInvocation> {
            on { it.parameterizedTestDescription } doReturn (null)
            on { it.displayName } doReturn "display"
            on { it.fixtures } doReturn Fixtures()
            on { it.outputs } doReturn CapturedOutputs()
            on { it.attachments } doReturn Attachments()
            on { it.state } doReturn TestState.Passed
        }

        val methodContainer = mock<TestMethodContainer> {
            on { it.method } doReturn testClass.getDeclaredMethod("exampleTestMethod")
            on { it.invocations } doReturn listOf(invocation)
        }

        val container = mock<TestContainer> {
            on { it.testClass } doReturn testClass
            on { it.orderedMethodContainers } doReturn listOf(methodContainer)
        }

        val result = manager.generate(container = container, outputDir = tempDir, configuration = Configuration())

        result.entries.shouldBeEmpty()

        Files.walk(tempDir).use { paths ->
            paths.filter { Files.isRegularFile(it) }.count() shouldBe 0
        }
    }

    @Test
    fun `generate writes a records sidecar and records the entry count when the log query finds records`() {
        val configuration = configurationWith(
            LogRecord("app", "inv-9", "first"),
            LogRecord("app", "inv-9", "second"),
            LogRecord("app", "inv-9", "third"),
            LogRecord("app", "inv-9", "boom\nat Foo.bar(\"x\")")
        )

        val result = manager.generate(container = containerFor(LogsTest::class.java), outputDir = tempDir, configuration = configuration)

        val tab: JsonTransforms.CustomTabContent = result.entries.single().value.single()

        tab.label shouldBe "Logs"
        tab.sourceId shouldBe "app"
        tab.identifier shouldBe "inv-9"
        tab.entries shouldBe 4
        tab.mediaType shouldBe "text/plain"

        tempDir.resolve(tab.file!!).should { outputFile ->
            outputFile.shouldExist()
            outputFile.readText() shouldBe "first\nsecond\nthird\nboom\nat Foo.bar(\"x\")"
        }

        tab.records shouldBe tab.file!!.removeSuffix(".txt") + ".jsonl"

        tempDir.resolve(tab.records!!).should { sidecar ->
            sidecar.shouldExist()
            val lines = sidecar.readLines()
            lines shouldHaveSize 4
            lines shouldBe listOf(
                """{"identifier":"inv-9","text":"first"}""",
                """{"identifier":"inv-9","text":"second"}""",
                """{"identifier":"inv-9","text":"third"}""",
                """{"identifier":"inv-9","text":"boom\nat Foo.bar(\"x\")"}"""
            )
        }
    }

    @Test
    fun `generate records a zero entry log tab without writing any file`() {
        val result = manager.generate(container = containerFor(LogsTest::class.java), outputDir = tempDir, configuration = configurationWith())

        val tab: JsonTransforms.CustomTabContent = result.entries.single().value.single()

        tab.label shouldBe "Logs"
        tab.sourceId shouldBe "app"
        tab.identifier shouldBe "inv-9"
        tab.entries shouldBe 0
        tab.file.shouldBeNull()
        tab.records.shouldBeNull()
        tab.mediaType.shouldBeNull()

        Files.walk(tempDir).use { paths ->
            paths.filter { Files.isRegularFile(it) }.count() shouldBe 0
        }
    }

    private fun containerFor(testClass: Class<*>): TestContainer {
        val invocation = mock<TestInvocation> {
            on { it.parameterizedTestDescription } doReturn (null)
            on { it.displayName } doReturn "display"
            on { it.fixtures } doReturn Fixtures()
            on { it.outputs } doReturn CapturedOutputs()
            on { it.attachments } doReturn Attachments()
            on { it.state } doReturn TestState.Passed
        }

        val methodContainer = mock<TestMethodContainer> {
            on { it.method } doReturn testClass.getDeclaredMethod("exampleTestMethod")
            on { it.invocations } doReturn listOf(invocation)
        }

        return mock {
            on { it.testClass } doReturn testClass
            on { it.orderedMethodContainers } doReturn listOf(methodContainer)
        }
    }
}