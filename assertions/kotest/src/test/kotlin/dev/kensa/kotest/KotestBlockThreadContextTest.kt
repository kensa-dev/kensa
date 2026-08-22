package dev.kensa.kotest

import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Collections
import kotlin.time.Duration.Companion.seconds

/**
 * Checks inside a `thenEventually { }` block are dispatched to `Dispatchers.IO`, so anything reaching
 * for Kensa's thread-bound test context (a `by fixtures(...)` delegate, `WithFixturesAndOutputs`, a
 * compiler-plugin capture hook) must still find it there.
 */
class KotestBlockThreadContextTest {

    private val testContext = TestContext(CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())
    private val harness = Harness()

    @BeforeEach
    fun setUp() = TestContextHolder.bindToCurrentThread(testContext)

    @AfterEach
    fun tearDown() = TestContextHolder.clearFromThread()

    @Test
    fun `the test context is bound on the thread running a block check`() {
        val seen = Collections.synchronizedList(mutableListOf<TestContext?>())

        with(harness) {
            thenEventually(2.seconds) {
                then(StateCollector { TestContextHolder.testContextOrNull().also(seen::add) }) { }
                and(StateCollector { TestContextHolder.testContextOrNull().also(seen::add) }) { }
            }
        }

        seen.toList().distinct() shouldBe listOf(testContext)
    }

    @Test
    fun `the single assertion form binds the test context too`() {
        val seen = Collections.synchronizedList(mutableListOf<TestContext?>())

        with(harness) {
            thenEventually(2.seconds, StateCollector { TestContextHolder.testContextOrNull().also(seen::add) }) { }
        }

        seen.toList().distinct() shouldBe listOf(testContext)
    }

    class Harness : WithKotest
}
