package dev.kensa.hamkrest

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import dev.kensa.StateCollector
import dev.kensa.context.TestContextHolder.testContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

interface WithHamkrest {

    fun <T> then(collector: StateCollector<T>, matcher: Matcher<T>) {
        HamkrestThen.then(testContext(), collector, matcher)
    }

    fun <T> then(collector: StateCollector<T>, block: T.() -> Unit) {
        HamkrestThen.then(testContext(), collector, block)
    }

    fun <T> and(collector: StateCollector<T>, matcher: Matcher<T>) {
        then(collector, matcher)
    }

    fun <T> and(collector: StateCollector<T>, block: T.() -> Unit) {
        then(collector, block)
    }

    fun <T> then(spec: ThenSpec<T>) {
        with(spec) {
            val ctx = testContext()
            HamkrestThen.then(ctx, collector) {
                assertThat(this, matcher)
                ctx.collectorContext.onMatch(this)
            }
        }
    }

    fun <T> and(spec: ThenSpec<T>): Unit = then(spec)

    fun <T> thenContinually(collector: StateCollector<T>, matcher: Matcher<T>): Unit = thenContinually(10.seconds, collector, matcher)

    fun <T> thenContinually(duration: Duration, collector: StateCollector<T>, matcher: Matcher<T>) {
        HamkrestThen.thenContinually(duration, testContext(), collector, matcher)
    }

    fun <T> thenContinually(duration: Duration, collector: StateCollector<T>, block: T.() -> Unit) {
        HamkrestThen.thenContinually(duration, testContext(), collector, block)
    }

    fun <T> thenEventually(spec: ThenSpec<T>): Unit = thenEventually(10.seconds, spec)
    fun <T> andEventually(spec: ThenSpec<T>): Unit = thenEventually(10.seconds, spec)

    fun <T> thenEventually(duration: Duration, spec: ThenSpec<T>) {
        with(spec) {
            val ctx = testContext()
            HamkrestThen.thenEventually(duration, ctx, collector) {
                assertThat(this, matcher)
            }
            ctx.collectorContext.onMatch(collector.execute(ctx.collectorContext))
        }
    }

    fun <T> andEventually(duration: Duration, spec: ThenSpec<T>): Unit = thenEventually(duration, spec)

    fun <T> thenEventually(collector: StateCollector<T>, matcher: Matcher<T>): Unit = thenEventually(10.seconds, collector, matcher)
    fun <T> andEventually(collector: StateCollector<T>, matcher: Matcher<T>): Unit = thenEventually(collector, matcher)

    fun <T> thenEventually(duration: Duration, collector: StateCollector<T>, matcher: Matcher<T>) {
        thenEventually(ZERO, duration, 25.milliseconds, collector, matcher)
    }

    fun <T> andEventually(duration: Duration, collector: StateCollector<T>, matcher: Matcher<T>): Unit = thenEventually(duration, collector, matcher)

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, collector: StateCollector<T>, matcher: Matcher<T>) {
        HamkrestThen.thenEventually(initialDelay, duration, interval, testContext(), collector, matcher)
    }

    fun <T> andEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, collector: StateCollector<T>, matcher: Matcher<T>): Unit =
        thenEventually(initialDelay, duration, interval, collector, matcher)

    fun <T> thenEventually(collector: StateCollector<T>, block: T.() -> Unit = {}): Unit = thenEventually(10.seconds, collector, block)
    fun <T> andEventually(collector: StateCollector<T>, block: T.() -> Unit = {}): Unit = thenEventually(collector, block)

    fun <T> thenEventually(duration: Duration, collector: StateCollector<T>, block: T.() -> Unit = {}) {
        thenEventually(ZERO, duration, 25.milliseconds, collector, block)
    }

    fun <T> andEventually(duration: Duration, collector: StateCollector<T>, block: T.() -> Unit = {}): Unit = thenEventually(duration, collector, block)

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, collector: StateCollector<T>, block: T.() -> Unit = {}) {
        HamkrestThen.thenEventually(initialDelay, duration, interval, testContext(), collector, block)
    }

    fun <T> andEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, collector: StateCollector<T>, block: T.() -> Unit = {}): Unit =
        thenEventually(initialDelay, duration, interval, collector, block)
}
