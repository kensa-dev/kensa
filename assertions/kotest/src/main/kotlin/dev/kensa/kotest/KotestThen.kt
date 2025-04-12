package dev.kensa.kotest

import dev.kensa.StateExtractor
import dev.kensa.context.TestContext
import io.kotest.assertions.failure
import io.kotest.assertions.nondeterministic.EventuallyConfiguration
import io.kotest.assertions.nondeterministic.continually
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.nondeterministic.eventuallyConfig
import io.kotest.matchers.Matcher
import io.kotest.matchers.invokeMatcher
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object KotestThen {
    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        then(testContext, extractor) {
            invokeMatcher(this, match)
        }
    }

    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        block(extractor.execute(testContext.interactions))
    }

    suspend fun <T> thenContinually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        thenContinually(duration, testContext, extractor) {
            invokeMatcher(extractor.execute(testContext.interactions), match)
        }
    }

    suspend fun <T> thenContinually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        continually(duration) {
            block(extractor.execute(testContext.interactions))
        }
    }

    suspend fun <T> thenEventually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        thenEventually(ZERO, duration, 25.milliseconds, testContext, extractor, match)
    }

    suspend fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        thenEventually(initialDelay, duration, interval, testContext, extractor) {
            invokeMatcher(extractor.execute(testContext.interactions), match)
        }
    }

    suspend fun <T> thenEventually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        thenEventually(ZERO, duration, testContext = testContext, extractor = extractor, block = block)
    }

    suspend fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        thenEventually(
            eventuallyConfig {
                this.duration = duration
                this.initialDelay = initialDelay
                this.interval = interval
                listener = LastThrowableListener()
            },
            testContext,
            extractor,
            block
        )
    }

    suspend fun <T> thenEventually(config: EventuallyConfiguration, testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        try {
            eventually(config) {
                block(extractor.execute(testContext.interactions))
            }
        } catch (e: Throwable) {
            val listener = config.listener
            if (listener is LastThrowableListener) {
                listener.lastThrowable?.let {
                    if (it is AssertionError) throw it
                    else throw failure(it.message ?: "eventually block failed", it)
                } ?: throw e
            }
        }
    }
}