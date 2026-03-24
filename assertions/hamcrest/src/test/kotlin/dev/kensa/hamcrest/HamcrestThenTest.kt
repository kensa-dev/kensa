package dev.kensa.hamcrest

import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class HamcrestThenTest {

    private val testContext = TestContext(Givens(), CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())
    private val stateCollector = mock<StateCollector<String>>()

    @AfterEach
    fun tearDown() {
        reset(stateCollector)
    }

    @Test
    fun `does not invoke extractor more than once`() {
        whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

        HamcrestThen.then(testContext, stateCollector, equalTo("result"))

        verify(stateCollector, times(1)).execute(anyOrNull())
    }

    @Test
    fun `thenEventually retries until matcher passes`() {
        val callCount = AtomicInteger(0)
        val successAfter = 3

        val retryingCollector = StateCollector {
            if (callCount.incrementAndGet() < successAfter) "wrong" else "result"
        }

        HamcrestThen.thenEventually(Duration.ofSeconds(5), testContext, retryingCollector, equalTo("result"))

        callCount.get() shouldBe successAfter
    }

    @Test
    fun `thenEventually respects initialDelay and interval`() {
        val callCount = AtomicInteger(0)

        val retryingCollector = StateCollector {
            if (callCount.incrementAndGet() < 3) "wrong" else "result"
        }

        HamcrestThen.thenEventually(
            Duration.ofMillis(100),
            Duration.ofSeconds(5),
            Duration.ofMillis(200),
            testContext,
            retryingCollector,
            equalTo("result")
        )

        callCount.get() shouldBe 3
    }

    @Test
    fun `thenContinually passes when assertion holds for entire duration`() {
        whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

        HamcrestThen.thenContinually(Duration.ofMillis(200), testContext, stateCollector, equalTo("result"))
    }

    @Test
    fun `thenContinually fails when assertion fails mid-duration`() {
        val callCount = AtomicInteger(0)

        val flakeyCollector = StateCollector {
            if (callCount.incrementAndGet() < 3) "result" else "wrong"
        }

        shouldThrow<AssertionError> {
            HamcrestThen.thenContinually(Duration.ofSeconds(5), testContext, flakeyCollector, equalTo("result"))
        }
    }

    @Test
    fun `WithHamcrest then delegates to HamcrestThen`() {
        try {
            TestContextHolder.bindToCurrentThread(testContext)

            whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

            Dummy().runThen(stateCollector, equalTo("result"))

            verify(stateCollector, times(1)).execute(anyOrNull())
        } finally {
            TestContextHolder.clearFromThread()
        }
    }

    @Test
    fun `WithHamcrest thenEventually retries until matcher passes`() {
        try {
            TestContextHolder.bindToCurrentThread(testContext)

            val callCount = AtomicInteger(0)
            val retryingCollector = StateCollector {
                if (callCount.incrementAndGet() < 3) "wrong" else "result"
            }

            Dummy().runThenEventually(Duration.ofSeconds(5), retryingCollector, equalTo("result"))

            callCount.get() shouldBe 3
        } finally {
            TestContextHolder.clearFromThread()
        }
    }

    class Dummy : WithHamcrest {
        fun <T> runThen(extractor: StateCollector<T>, matcher: org.hamcrest.Matcher<in T>) {
            then(extractor, matcher)
        }

        fun <T> runThenEventually(duration: Duration, extractor: StateCollector<T>, matcher: org.hamcrest.Matcher<in T>) {
            thenEventually(duration, extractor, matcher)
        }
    }
}
