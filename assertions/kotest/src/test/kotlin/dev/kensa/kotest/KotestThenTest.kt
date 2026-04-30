package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.kotest.KotestThen.thenEventually
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.Matcher
import io.kotest.matchers.be
import io.kotest.matchers.collections.shouldStartWith
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class KotestThenTest {

    private val testContext = TestContext(CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())
    private val stateCollector = mock<StateCollector<String>>()

    @AfterEach
    fun tearDown() {
        reset(stateCollector)
    }

    @Test
    fun `does not invoke collector more than once`() {
        whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

        KotestThen.then(testContext, stateCollector, be("result"))

        verify(stateCollector, times(1)).execute(anyOrNull())
    }

    @Test
    fun `does not retry onMatch failures and reports correct exception`() = runTest {
        try {
            TestContextHolder.bindToCurrentThread(testContext)

            whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

            var onMatchCallCount: Int = 0

            val spec = object : ThenSpec<String> {
                override val collector: StateCollector<String> = stateCollector
                override val matcher: Matcher<String> = be("result")
                override val onMatch: CollectorContext.(String) -> Unit = { onMatchCallCount++; throw IllegalStateException("onMatch throws") }
            }

            shouldThrow<IllegalStateException> { Dummy(spec).runThenEventually() }
                .shouldHaveMessage("onMatch throws")

            onMatchCallCount shouldBe 1
        } finally {
            TestContextHolder.clearFromThread()
        }
    }

    @Test
    fun `onMatch failures report correct exception`() {
        try {
            TestContextHolder.bindToCurrentThread(testContext)

            whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

            var onMatchCallCount: Int = 0

            val spec = object : ThenSpec<String> {
                override val collector: StateCollector<String> = stateCollector
                override val matcher: Matcher<String> = be("result")
                override val onMatch: CollectorContext.(String) -> Unit = { onMatchCallCount++; throw IllegalStateException("onMatch throws") }
            }

            shouldThrow<IllegalStateException> { Dummy(spec).runThen() }
                .shouldHaveMessage("onMatch throws")

            onMatchCallCount shouldBe 1
        } finally {
            TestContextHolder.clearFromThread()
        }
    }

    @Test
    fun `can configure initial, duration and interval for thenEventually`() = runTest {
        val initialDelay = 1.seconds
        val duration = 3.seconds
        val interval = 0.5.seconds

        // Rough expected poll count:
        //   - starts after 1s
        //   - polls every 0.5s
        //   - stops after ~3s total duration
        //   → ~ (3000 - 1000) / 500 + 1 = ~5 polls

        var pollCount = 0

        whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

        val error = shouldThrow<AssertionError> {
            thenEventually(initialDelay, duration, interval, testContext, stateCollector) {
                pollCount++
                this shouldBe "wrong"
            }
        }

        error shouldHaveMessage "expected:<wrong> but was:<result>"

        // In practice you'll see ~4–6 invocations (real time varies slightly)
        pollCount shouldBeGreaterThanOrEqual 4
        pollCount shouldBeLessThanOrEqual 7
    }

    @Test
    fun `WithKotest andEventually with spec retries until matcher passes`() {
        try {
            TestContextHolder.bindToCurrentThread(testContext)

            val callCount = AtomicInteger(0)
            val retryingCollector = StateCollector<String> {
                if (callCount.incrementAndGet() < 3) "wrong" else "result"
            }

            val spec = object : ThenSpec<String> {
                override val collector: StateCollector<String> = retryingCollector
                override val matcher: Matcher<String> = be("result")
                override val onMatch: CollectorContext.(String) -> Unit = {}
            }

            Dummy(spec).runAndEventually()

            callCount.get() shouldBe 3
        } finally {
            TestContextHolder.clearFromThread()
        }
    }

    @Test
    fun `WithKotest andEventually with collector and matcher retries until matcher passes`() {
        try {
            TestContextHolder.bindToCurrentThread(testContext)

            val callCount = AtomicInteger(0)
            val retryingCollector = StateCollector<String> {
                if (callCount.incrementAndGet() < 3) "wrong" else "result"
            }

            DummyCollector().runAndEventually(retryingCollector, be("result"))

            callCount.get() shouldBe 3
        } finally {
            TestContextHolder.clearFromThread()
        }
    }

    @Test
    fun `WithKotest andEventually with collector and block retries until block passes`() {
        try {
            TestContextHolder.bindToCurrentThread(testContext)

            val callCount = AtomicInteger(0)
            val retryingCollector = StateCollector<String> {
                if (callCount.incrementAndGet() < 3) "wrong" else "result"
            }

            DummyCollector().runAndEventuallyBlock(retryingCollector) { this shouldBe "result" }

            callCount.get() shouldBe 3
        } finally {
            TestContextHolder.clearFromThread()
        }
    }

    class Dummy(private val spec: ThenSpec<String>) : WithKotest {
        fun runThenEventually() {
            thenEventually(5.seconds, spec)
        }

        fun runThen() {
            then(spec)
        }

        fun runAndEventually() {
            andEventually(5.seconds, spec)
        }
    }

    class DummyCollector : WithKotest {
        fun <T> runAndEventually(collector: StateCollector<T>, matcher: Matcher<T>) {
            andEventually(5.seconds, collector, matcher)
        }

        fun <T> runAndEventuallyBlock(collector: StateCollector<T>, block: T.() -> Unit) {
            andEventually(5.seconds, collector, block)
        }
    }
}