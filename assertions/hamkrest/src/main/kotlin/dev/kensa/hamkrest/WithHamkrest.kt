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

    fun <T> then(extractor: StateCollector<T>, matcher: Matcher<T>) {
        HamkrestThen.then(testContext(), extractor, matcher)
    }

    fun <T> then(extractor: StateCollector<T>, block: T.() -> Unit) {
        HamkrestThen.then(testContext(), extractor, block)
    }

    fun <T> and(extractor: StateCollector<T>, matcher: Matcher<T>) {
        then(extractor, matcher)
    }

    fun <T> and(extractor: StateCollector<T>, block: T.() -> Unit) {
        then(extractor, block)
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

    fun <T> thenContinually(extractor: StateCollector<T>, matcher: Matcher<T>): Unit = thenContinually(10.seconds, extractor, matcher)

    fun <T> thenContinually(duration: Duration, extractor: StateCollector<T>, matcher: Matcher<T>) {
        HamkrestThen.thenContinually(duration, testContext(), extractor, matcher)
    }

    fun <T> thenContinually(duration: Duration, extractor: StateCollector<T>, block: T.() -> Unit) {
        HamkrestThen.thenContinually(duration, testContext(), extractor, block)
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

    fun <T> thenEventually(extractor: StateCollector<T>, matcher: Matcher<T>): Unit = thenEventually(10.seconds, extractor, matcher)

    fun <T> thenEventually(duration: Duration, extractor: StateCollector<T>, matcher: Matcher<T>) {
        thenEventually(ZERO, duration, 25.milliseconds, extractor, matcher)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, extractor: StateCollector<T>, matcher: Matcher<T>) {
        HamkrestThen.thenEventually(initialDelay, duration, interval, testContext(), extractor, matcher)
    }

    fun <T> thenEventually(extractor: StateCollector<T>, block: T.() -> Unit = {}): Unit = thenEventually(10.seconds, extractor, block)

    fun <T> thenEventually(duration: Duration, extractor: StateCollector<T>, block: T.() -> Unit = {}) {
        thenEventually(ZERO, duration, 25.milliseconds, extractor, block)
    }

    fun <T> thenEventually(initialDelay: Duration = ZERO, duration: Duration = 10.seconds, interval: Duration = 25.milliseconds, extractor: StateCollector<T>, block: T.() -> Unit = {}) {
        HamkrestThen.thenEventually(initialDelay, duration, interval, testContext(), extractor, block)
    }
}
