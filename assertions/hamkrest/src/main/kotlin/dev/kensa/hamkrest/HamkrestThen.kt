package dev.kensa.hamkrest

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import org.awaitility.kotlin.await
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
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
}
