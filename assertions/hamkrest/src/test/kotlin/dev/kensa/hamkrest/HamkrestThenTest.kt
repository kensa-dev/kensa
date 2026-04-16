package dev.kensa.hamkrest

import com.natpryce.hamkrest.equalTo
import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
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

class HamkrestThenTest {

    private val testContext = TestContext(CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())
    private val stateCollector = mock<StateCollector<String>>()

    @AfterEach
    fun tearDown() {
        reset(stateCollector)
    }

    @Test
    fun `does not invoke extractor more than once`() {
        whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

        HamkrestThen.then(testContext, stateCollector, equalTo("result"))

        verify(stateCollector, times(1)).execute(anyOrNull())
    }

    @Test
    fun `block-based then invokes block with extracted value`() {
        whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

        var captured: String? = null
        HamkrestThen.then(testContext, stateCollector) { captured = this }

        captured shouldBe "result"
        verify(stateCollector, times(1)).execute(anyOrNull())
    }

    @Test
    fun `onMatch failures report correct exception`() {
        try {
            TestContextHolder.bindToCurrentThread(testContext)

            whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

            var onMatchCallCount = 0

            val spec = object : ThenSpec<String> {
                override val collector: StateCollector<String> = stateCollector
                override val matcher = equalTo("result")
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
    fun `does not retry onMatch failures and reports correct exception`() {
        try {
            TestContextHolder.bindToCurrentThread(testContext)

            whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

            var onMatchCallCount = 0

            val spec = object : ThenSpec<String> {
                override val collector: StateCollector<String> = stateCollector
                override val matcher = equalTo("result")
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
    fun `thenEventually retries until matcher passes`() {
        val callCount = AtomicInteger(0)
        val successAfter = 3

        val retryingCollector = StateCollector<String> {
            if (callCount.incrementAndGet() < successAfter) "wrong" else "result"
        }

        HamkrestThen.thenEventually(5.seconds, testContext, retryingCollector, equalTo("result"))

        callCount.get() shouldBe successAfter
    }

    @Test
    fun `thenEventually respects initialDelay and interval`() {
        val callCount = AtomicInteger(0)

        val retryingCollector = StateCollector<String> {
            if (callCount.incrementAndGet() < 3) "wrong" else "result"
        }

        HamkrestThen.thenEventually(
            initialDelay = 100.milliseconds,
            duration = 5.seconds,
            interval = 200.milliseconds,
            context = testContext,
            extractor = retryingCollector,
            matcher = equalTo("result")
        )

        callCount.get() shouldBe 3
    }

    @Test
    fun `thenContinually passes when assertion holds for entire duration`() {
        whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

        HamkrestThen.thenContinually(200.milliseconds, testContext, stateCollector, equalTo("result"))
    }

    @Test
    fun `thenContinually fails when assertion fails mid-duration`() {
        val callCount = AtomicInteger(0)

        val flakeyCollector = StateCollector<String> {
            if (callCount.incrementAndGet() < 3) "result" else "wrong"
        }

        shouldThrow<AssertionError> {
            HamkrestThen.thenContinually(5.seconds, testContext, flakeyCollector, equalTo("result"))
        }
    }

    @Test
    fun `block-based thenContinually passes for full duration`() {
        whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

        var blockCallCount = 0
        HamkrestThen.thenContinually(200.milliseconds, testContext, stateCollector) {
            blockCallCount++
            this shouldBe "result"
        }

        blockCallCount shouldBe (blockCallCount.coerceAtLeast(1))
    }

    class Dummy(private val spec: ThenSpec<String>) : WithHamkrest {
        fun runThenEventually() {
            thenEventually(5.seconds, spec)
        }

        fun runThen() {
            then(spec)
        }
    }
}
