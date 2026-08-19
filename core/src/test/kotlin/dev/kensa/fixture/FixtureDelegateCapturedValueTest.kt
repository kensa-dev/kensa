package dev.kensa.fixture

import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

/**
 * A delegated fixture property is re-read per access, but a plain `val` that reads it is evaluated once.
 * A shared use-case object therefore freezes whichever invocation happened to construct it.
 */
class FixtureDelegateCapturedValueTest {

    private val referenceFx = fixture("CapturedReferenceFx") { "default-ref" }

    class UseCase(fx: Fixture<String>) {
        val reference: String by fixtures(fx)

        // Reads the delegate once, at construction.
        val eagerlyDerived: String = "derived-from-$reference"

        // Reads the delegate once, at first access.
        val lazilyDerived: String by lazy { "derived-from-$reference" }

        // Re-reads the delegate on every access.
        val computed: String get() = "derived-from-$reference"
    }

    @AfterEach
    fun tearDown() = TestContextHolder.clearFromThread()

    @Test
    fun `a val reading a delegated property freezes the constructing invocation's value`() {
        bind("REF-1")
        val sharedUseCase = UseCase(referenceFx)

        sharedUseCase.reference shouldBe "REF-1"
        sharedUseCase.eagerlyDerived shouldBe "derived-from-REF-1"
        sharedUseCase.lazilyDerived shouldBe "derived-from-REF-1"
        sharedUseCase.computed shouldBe "derived-from-REF-1"

        bind("REF-2")

        sharedUseCase.reference shouldBe "REF-2"
        sharedUseCase.computed shouldBe "derived-from-REF-2"

        // Stale: still the first invocation's fixture value, with no error raised.
        sharedUseCase.eagerlyDerived shouldBe "derived-from-REF-1"
        sharedUseCase.lazilyDerived shouldBe "derived-from-REF-1"
    }

    private fun bind(value: String) {
        TestContextHolder.bindToCurrentThread(
            TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
                .also { it.fixtures.seed(referenceFx, value) }
        )
    }
}
