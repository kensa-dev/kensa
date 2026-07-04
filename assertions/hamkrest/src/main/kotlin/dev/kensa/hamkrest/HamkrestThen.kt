package dev.kensa.hamkrest

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.toJavaDuration

object HamkrestThen {

    fun <T> then(context: TestContext, collector: StateCollector<T>, matcher: Matcher<T>) {
        then(context, collector) { assertThat(this, matcher) }
    }

    fun <T> then(context: TestContext, collector: StateCollector<T>, block: T.() -> Unit) {
        block(collector.execute(context.collectorContext))
    }

    fun <T> thenContinually(duration: Duration = 10.seconds, context: TestContext, collector: StateCollector<T>, matcher: Matcher<T>) {
        thenContinually(duration, context, collector) { assertThat(this, matcher) }
    }

    fun <T> thenContinually(duration: Duration = 10.seconds, context: TestContext, collector: StateCollector<T>, block: T.() -> Unit) {
        val end = System.nanoTime() + duration.inWholeNanoseconds
        while (System.nanoTime() < end) {
            block(collector.execute(context.collectorContext))
            Thread.sleep(25)
        }
    }

    fun <T> thenEventually(duration: Duration = 10.seconds, context: TestContext, collector: StateCollector<T>, matcher: Matcher<T>) {
        thenEventually(ZERO, duration, 25.milliseconds, context, collector, matcher)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, context: TestContext, collector: StateCollector<T>, matcher: Matcher<T>) {
        thenEventually(initialDelay, duration, interval, context, collector) { assertThat(this, matcher) }
    }

    fun <T> thenEventually(duration: Duration = 10.seconds, context: TestContext, collector: StateCollector<T>, block: T.() -> Unit) {
        thenEventually(ZERO, duration, 25.milliseconds, context, collector, block)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, context: TestContext, collector: StateCollector<T>, block: T.() -> Unit) {
        await
            .pollDelay(initialDelay.toJavaDuration())
            .pollInterval(interval.toJavaDuration())
            .atMost(duration.toJavaDuration())
            .untilAsserted { block(collector.execute(context.collectorContext)) }
    }

    fun thenEventually(initialDelay: Duration, duration: Duration, interval: Duration, scope: PollingScope) {
        val results = runBlocking {
            scope.checks.map { check ->
                async(Dispatchers.IO) {
                    runCatching { pollEventually(initialDelay, duration, interval, check) }
                }
            }.awaitAll()
        }
        val failures = results.withIndex().mapNotNull { (index, result) -> result.exceptionOrNull()?.let { (index + 1) to it } }
        when {
            failures.isEmpty() -> Unit
            failures.size == 1 -> throw failures.single().second
            else -> {
                val aggregate = AssertionError(
                    failures.joinToString(
                        prefix = "${failures.size} of ${results.size} assertions did not pass within $duration:\n",
                        separator = "\n"
                    ) { (index, error) -> "  [$index] ${error.message}" },
                    failures.first().second
                )
                failures.drop(1).forEach { aggregate.addSuppressed(it.second) }
                throw aggregate
            }
        }
    }

    fun thenContinually(duration: Duration, scope: PollingScope) {
        try {
            runBlocking {
                scope.checks.forEach { check ->
                    launch(Dispatchers.IO) {
                        val mark = TimeSource.Monotonic.markNow()
                        while (mark.elapsedNow() < duration) {
                            check()
                            delay(25.milliseconds)
                        }
                    }
                }
            }
        } catch (e: OnMatchException) {
            throw e.unwrapOnMatchException()
        }
    }

    private tailrec fun OnMatchException.unwrapOnMatchException(): Throwable {
        val cause = cause!!
        return if (cause is OnMatchException) cause.unwrapOnMatchException() else cause
    }

    private suspend fun pollEventually(initialDelay: Duration, duration: Duration, interval: Duration, check: () -> Unit) {
        delay(initialDelay)
        val mark = TimeSource.Monotonic.markNow()
        while (true) {
            try {
                check()
                return
            } catch (e: OnMatchException) {
                throw e.cause!!
            } catch (e: Throwable) {
                if (mark.elapsedNow() + interval >= duration) throw e
            }
            delay(interval)
        }
    }
}
