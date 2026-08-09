package dev.kensa.fixture

import dev.kensa.KensaException
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class FixtureDelegateTest {

    private val referenceFx = fixture("ReferenceFx") { "default-ref" }

    class UseCase(fx: Fixture<String>) {
        val reference: String by fixtures(fx)
    }

    @AfterEach
    fun tearDown() {
        TestContextHolder.clearFromThread()
    }

    @Test
    fun `resolves value from thread local test context`() {
        val context = newContext()
        context.fixtures.seed(referenceFx, "REF-1")
        TestContextHolder.bindToCurrentThread(context)

        UseCase(referenceFx).reference shouldBe "REF-1"
    }

    @Test
    fun `re-reads on every access so rebinding yields the current context value`() {
        val useCase = UseCase(referenceFx)

        val first = newContext().also { it.fixtures.seed(referenceFx, "REF-1") }
        TestContextHolder.bindToCurrentThread(first)
        useCase.reference shouldBe "REF-1"

        val second = newContext().also { it.fixtures.seed(referenceFx, "REF-2") }
        TestContextHolder.bindToCurrentThread(second)
        useCase.reference shouldBe "REF-2"
    }

    @Test
    fun `fails with a clear message when no test context is bound`() {
        TestContextHolder.clearFromThread()

        shouldThrow<KensaException> {
            UseCase(referenceFx).reference
        }.message shouldContain "active Kensa test"
    }

    private fun newContext() = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
}
