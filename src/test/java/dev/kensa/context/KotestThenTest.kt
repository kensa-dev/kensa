package dev.kensa.context

import dev.kensa.StateExtractor
import dev.kensa.context.KotestThen.thenEventually
import dev.kensa.state.CapturedInteractions
import io.kotest.assertions.AssertionFailedError
import io.kotest.assertions.nondeterministic.EventuallyConfiguration
import io.kotest.assertions.nondeterministic.EventuallyConfigurationBuilder
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.nondeterministic.eventuallyConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.be
import io.kotest.matchers.collections.shouldStartWith
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class KotestThenTest {

    private val testContext = mock<TestContext>()
    private val stateExtractor = mock<StateExtractor<String>>()

    @Test
    fun `does not invoke extractor more than once`() {
        whenever(stateExtractor.execute(anyOrNull())).thenReturn("result")

        KotestThen.then(testContext, stateExtractor, be("result"))

        verify(stateExtractor, times(1)).execute(anyOrNull())
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

        whenever(stateExtractor.execute(anyOrNull())).thenReturn("result")

        val actualTimes = mutableListOf<Long>()

        val error = shouldThrow<AssertionError> {
            thenEventually(initialDelay, duration, interval, testContext, stateExtractor) {
                actualTimes.add(currentTime)
                this shouldBe "wrong"
            }
        }
        error shouldHaveMessage """expected:<"wrong"> but was:<"result">"""

        // Kotest eventually doesn't play completely with `runTest` and keeps running for the full duration (probably due to the step calculation using a real time source)
        // so need to only check the first segment of the actualTimes
        actualTimes shouldStartWith expectedTimes
    }
}