package dev.kensa.hamkrest

import dev.kensa.Kensa
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.asContextElement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Collections
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * A user-owned `ThreadLocal` (e.g. a tracking id holder) set on the test thread must be visible on the
 * `Dispatchers.IO` thread running a `thenEventually { }` check, when the user registers a coroutine
 * context provider for it.
 */
class HamkrestBlockUserThreadLocalTest {

    private val testContext = TestContext(CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())
    private val harness = Harness()
    private val userThreadLocal = ThreadLocal<String>()
    private val provider: () -> CoroutineContext = { userThreadLocal.asContextElement() }

    @BeforeEach
    fun setUp() {
        TestContextHolder.bindToCurrentThread(testContext)
        Kensa.configure().withCoroutineContextProviders(provider)
        userThreadLocal.set("tracking-id")
    }

    @AfterEach
    fun tearDown() {
        userThreadLocal.remove()
        Kensa.konfigure { coroutineContextProviders.remove(provider) }
        TestContextHolder.clearFromThread()
    }

    @Test
    fun `a registered provider carries a user thread local into block checks`() {
        val seen = Collections.synchronizedList(mutableListOf<String?>())

        with(harness) {
            thenEventually(2.seconds) {
                then(StateCollector { userThreadLocal.get().also(seen::add) }) { }
                and(StateCollector { userThreadLocal.get().also(seen::add) }) { }
            }
        }

        seen.toList().distinct() shouldBe listOf("tracking-id")
    }

    class Harness : WithHamkrest
}
