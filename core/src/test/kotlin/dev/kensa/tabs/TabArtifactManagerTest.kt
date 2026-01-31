package dev.kensa.tabs

import dev.kensa.Configuration
import dev.kensa.KensaTab
import dev.kensa.KensaTabVisibility
import dev.kensa.context.TestContainer
import dev.kensa.fixture.Fixtures
import dev.kensa.output.json.JsonTransforms
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.TestInvocation
import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.nio.file.Path
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

        tempDir.resolve(tab.file).should { outputFile ->
            outputFile.shouldExist()
            outputFile.readText() shouldBe "rendered for inv-123"
        }
    }

    @Test
    fun `generate skips OnlyOnFailure tabs for passed invocations`() {
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

        result.entries.shouldBeEmpty()

        CapturingIdentifierProvider.lastInvocationIdentifierInCtx shouldBe "not-set"
        CapturingRenderer.lastInvocationIdentifierInCtx shouldBe "not-set"
    }
}