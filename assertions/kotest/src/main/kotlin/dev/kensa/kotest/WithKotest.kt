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

    fun <T> then(spec: ThenSpec<T>) {
        with(spec) {
            KotestThen.then(testContext(), collector) {
                invokeMatcher(this, matcher)
                testContext().apply { onMatch(CollectorContext(fixtures, interactions, outputs), this@then) }
            }
        }
    }

    fun <T> and(spec: ThenSpec<T>): Unit = then(spec)

    fun <T> then(collector: StateCollector<T>, block: T.() -> Unit) {
        KotestThen.then(testContext(), collector, block)
    }

    fun <T> then(collector: StateCollector<T>, match: Matcher<T>) {
        KotestThen.then(testContext(), collector, match)
    }

    fun <T> and(collector: StateCollector<T>, block: T.() -> Unit) {
        then(collector, block)
    }

    fun <T> and(collector: StateCollector<T>, match: Matcher<T>) {
        then(collector, match)
    }

    fun <T> thenEventually(spec: ThenSpec<T>): Unit = thenEventually(10.seconds, spec)
    fun <T> andEventually(spec: ThenSpec<T>): Unit = thenEventually(10.seconds, spec)

    fun <T> thenEventually(duration: Duration, spec: ThenSpec<T>) {
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

    fun <T> thenContinually(collector: StateCollector<T>, match: Matcher<T>): Unit = thenContinually(10.seconds, collector, match)

    fun <T> thenContinually(duration: Duration, collector: StateCollector<T>, block: T.() -> Unit) {
        runBlocking {
            KotestThen.thenContinually(duration, testContext(), collector, block)
        }
    }

    fun <T> thenContinually(duration: Duration, collector: StateCollector<T>, match: Matcher<T>) {
        runBlocking {
            KotestThen.thenContinually(duration, testContext(), collector, match)
        }
    }

    fun <T> thenEventually(collector: StateCollector<T>, match: Matcher<T>): Unit = thenEventually(10.seconds, collector, match)

    fun <T> thenEventually(duration: Duration, collector: StateCollector<T>, match: Matcher<T>) {
        thenEventually(ZERO, duration, 25.milliseconds, collector, match)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, collector: StateCollector<T>, match: Matcher<T>) {
        runBlocking {
            KotestThen.thenEventually(initialDelay, duration, interval, testContext(), collector, match)
        }
    }

    fun <T> thenEventually(collector: StateCollector<T>, block: T.() -> Unit = {}): Unit = thenEventually(10.seconds, collector, block)

    fun <T> thenEventually(duration: Duration, collector: StateCollector<T>, block: T.() -> Unit = {}) {
        thenEventually(ZERO, duration, 25.milliseconds, collector, block)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, collector: StateCollector<T>, block: T.() -> Unit = {}) {
        runBlocking {
            KotestThen.thenEventually(initialDelay, duration, interval, testContext(), collector, block)
        }
    }
}