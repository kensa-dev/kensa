package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import io.kotest.assertions.AssertionErrorBuilder
import io.kotest.assertions.nondeterministic.EventuallyConfiguration
import io.kotest.assertions.nondeterministic.continually
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.nondeterministic.eventuallyConfig
import io.kotest.matchers.Matcher
import io.kotest.matchers.should
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object KotestThen {
    fun <T> then(testContext: TestContext, collector: StateCollector<T>, match: Matcher<T>) {
        then(testContext, collector) {
            invokeMatcher(this, match)
        }
    }

    fun <T> then(testContext: TestContext, collector: StateCollector<T>, block: T.() -> Unit) {
        block(collector.execute(CollectorContext(testContext.fixtures, testContext.interactions, testContext.outputs)))
    }

    suspend fun <T> thenContinually(duration: Duration = 10.seconds, testContext: TestContext, collector: StateCollector<T>, match: Matcher<T>) {
        thenContinually(duration, testContext, collector) {
            invokeMatcher(collector.execute(CollectorContext(testContext.fixtures, testContext.interactions, testContext.outputs)), match)
        }
    }

    suspend fun <T> thenContinually(duration: Duration = 10.seconds, testContext: TestContext, collector: StateCollector<T>, block: T.() -> Unit) {
        continually(duration) {
            block(collector.execute(CollectorContext(testContext.fixtures, testContext.interactions, testContext.outputs)))
        }
    }

    suspend fun <T> thenEventually(duration: Duration = 10.seconds, testContext: TestContext, collector: StateCollector<T>, match: Matcher<T>) {
        thenEventually(ZERO, duration, 25.milliseconds, testContext, collector, match)
    }

    suspend fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, testContext: TestContext, collector: StateCollector<T>, match: Matcher<T>) {
        thenEventually(initialDelay, duration, interval, testContext, collector) {
            invokeMatcher(collector.execute(CollectorContext(testContext.fixtures, testContext.interactions, testContext.outputs)), match)
        }
    }

    suspend fun <T> thenEventually(duration: Duration = 10.seconds, testContext: TestContext, collector: StateCollector<T>, block: T.() -> Unit) {
        thenEventually(ZERO, duration, testContext = testContext, collector = collector, block = block)
    }

    suspend fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, testContext: TestContext, collector: StateCollector<T>, block: T.() -> Unit) {
        thenEventually(
            eventuallyConfig {
                this.duration = duration
                this.initialDelay = initialDelay
                this.interval = interval
                listener = LastThrowableListener()
                shortCircuit = { it is OnMatchException }
            },
            testContext,
            collector,
            block
        )
    }

    suspend fun <T> thenEventually(config: EventuallyConfiguration, testContext: TestContext, collector: StateCollector<T>, block: T.() -> Unit) {
        try {
            eventually(config) {
                block(collector.execute(CollectorContext(testContext.fixtures, testContext.interactions, testContext.outputs)))
            }
        } catch (e: Throwable) {
            val listener = config.listener
            if (listener is LastThrowableListener) {
                when (val lastThrowable = listener.lastThrowable) {
                    is OnMatchException -> throw lastThrowable.cause!!
                    is AssertionError -> throw lastThrowable
                    else -> throw failure(lastThrowable)
                }
            }
            throw e
        }
    }

    private fun <T> invokeMatcher(t: T, matcher: Matcher<T>): T = t.apply { should(matcher) }

    private fun failure(throwable: Throwable): AssertionError = AssertionErrorBuilder.create()
        .withMessage(throwable.message ?: "eventually block failed")
        .withCause(throwable)
        .build()
}