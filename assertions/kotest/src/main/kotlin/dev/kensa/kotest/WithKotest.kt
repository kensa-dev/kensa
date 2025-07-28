package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import dev.kensa.context.TestContextHolder.testContext
import io.kotest.matchers.Matcher
import io.kotest.matchers.invokeMatcher
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

interface WithKotest {

    fun <T> then(spec: CollectingThenSpec<T>) {
        with(spec) {
            KotestThen.then(testContext(), collector) {
                invokeMatcher(this, matcher)
                testContext().apply { onMatch(CollectorContext(fixtures, interactions, outputs), this@then) }
            }
        }
    }

    fun <T> and(spec: CollectingThenSpec<T>): Unit = then(spec)

    fun <T> then(extractor: StateCollector<T>, block: T.() -> Unit) {
        KotestThen.then(testContext(), extractor, block)
    }

    fun <T> then(extractor: StateCollector<T>, match: Matcher<T>) {
        KotestThen.then(testContext(), extractor, match)
    }

    fun <T> and(extractor: StateCollector<T>, block: T.() -> Unit) {
        then(extractor, block)
    }

    fun <T> and(extractor: StateCollector<T>, match: Matcher<T>) {
        then(extractor, match)
    }

    fun <T> thenEventually(spec: CollectingThenSpec<T>): Unit = thenEventually(10.seconds, spec)
    fun <T> andEventually(spec: CollectingThenSpec<T>): Unit = thenEventually(10.seconds, spec)

    fun <T> thenEventually(duration: Duration, spec: CollectingThenSpec<T>) {
        runBlocking {
            with(spec) {
                KotestThen.thenEventually(duration, testContext(), collector) {
                    invokeMatcher(this, matcher)
                    try {
                        testContext().apply { onMatch(CollectorContext(fixtures, interactions, outputs), this@thenEventually) }
                    } catch (e: Throwable) {
                        throw OnMatchException(e)
                    }
                }
            }
        }
    }

    fun <T> thenContinually(extractor: StateCollector<T>, match: Matcher<T>): Unit = thenContinually(10.seconds, extractor, match)

    fun <T> thenContinually(duration: Duration, extractor: StateCollector<T>, block: T.() -> Unit) {
        runBlocking {
            KotestThen.thenContinually(duration, testContext(), extractor, block)
        }
    }

    fun <T> thenContinually(duration: Duration, extractor: StateCollector<T>, match: Matcher<T>) {
        runBlocking {
            KotestThen.thenContinually(duration, testContext(), extractor, match)
        }
    }

    fun <T> thenEventually(extractor: StateCollector<T>, match: Matcher<T>): Unit = thenEventually(10.seconds, extractor, match)

    fun <T> thenEventually(duration: Duration, extractor: StateCollector<T>, match: Matcher<T>) {
        thenEventually(ZERO, duration, 25.milliseconds, extractor, match)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, extractor: StateCollector<T>, match: Matcher<T>) {
        runBlocking {
            KotestThen.thenEventually(initialDelay, duration, interval, testContext(), extractor, match)
        }
    }

    fun <T> thenEventually(extractor: StateCollector<T>, block: T.() -> Unit = {}): Unit = thenEventually(10.seconds, extractor, block)

    fun <T> thenEventually(duration: Duration, extractor: StateCollector<T>, block: T.() -> Unit = {}) {
        thenEventually(ZERO, duration, 25.milliseconds, extractor, block)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, extractor: StateCollector<T>, block: T.() -> Unit = {}) {
        runBlocking {
            KotestThen.thenEventually(initialDelay, duration, interval, testContext(), extractor, block)
        }
    }
}