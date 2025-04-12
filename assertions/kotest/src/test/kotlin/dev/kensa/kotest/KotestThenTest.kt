package dev.kensa.kotest

import dev.kensa.StateExtractor
import dev.kensa.context.TestContext
import dev.kensa.kotest.KotestThen.thenEventually
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.be
import io.kotest.matchers.collections.shouldStartWith
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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