package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.kotest.KotestThen.thenEventually
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.Matcher
import io.kotest.matchers.be
import io.kotest.matchers.collections.shouldStartWith
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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class KotestThenTest {

    private val testContext = TestContext(Givens(), CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())
    private val stateCollector = mock<StateCollector<String>>()

    @AfterEach
    fun tearDown() {
        reset(stateCollector)
    }

    @Test
    fun `does not invoke extractor more than once`() {
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

            val spec = object : CollectingThenSpec<String> {
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

            val spec = object : CollectingThenSpec<String> {
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
        val expectedTimes = generateSequence(initialDelay) { it + interval }
            .takeWhile { it <= duration }
            .map { it.inWholeMilliseconds }
            .toList()

        whenever(stateCollector.execute(anyOrNull())).thenReturn("result")

        val actualTimes = mutableListOf<Long>()

        val error = shouldThrow<AssertionError> {
            thenEventually(initialDelay, duration, interval, testContext, stateCollector) {
                actualTimes.add(currentTime)
                this shouldBe "wrong"
            }
        }
        error shouldHaveMessage """expected:<wrong> but was:<result>"""

        // Kotest eventually doesn't play completely with `runTest` and keeps running for the full duration (probably due to the step calculation using a real time source)
        // so need to only check the first segment of the actualTimes
        actualTimes shouldStartWith expectedTimes
    }

    class Dummy(private val spec: CollectingThenSpec<String>) : WithKotest {
        fun runThenEventually() {
            thenEventually(5.seconds, spec)
        }

        fun runThen() {
            then(spec)
        }
    }
}