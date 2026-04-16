package dev.kensa.hamcrest

import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import org.awaitility.kotlin.await
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import java.time.Duration

object HamcrestThen {

    @JvmStatic
    fun <T> then(context: TestContext, collector: StateCollector<T>, matcher: Matcher<in T>) {
        assertThat(collector.execute(context.collectorContext), matcher)
    }

    @JvmStatic
    fun <T> thenContinually(context: TestContext, collector: StateCollector<T>, matcher: Matcher<in T>) {
        thenContinually(Duration.ofSeconds(10), context, collector, matcher)
    }

    @JvmStatic
    fun <T> thenContinually(duration: Duration, context: TestContext, collector: StateCollector<T>, matcher: Matcher<in T>) {
        val end = System.nanoTime() + duration.toNanos()
        while (System.nanoTime() < end) {
            assertThat(collector.execute(context.collectorContext), matcher)
            Thread.sleep(25)
        }
    }

    @JvmStatic
    fun <T> thenEventually(context: TestContext, collector: StateCollector<T>, matcher: Matcher<in T>) {
        thenEventually(Duration.ZERO, Duration.ofSeconds(10), Duration.ofMillis(25), context, collector, matcher)
    }

    @JvmStatic
    fun <T> thenEventually(duration: Duration, context: TestContext, collector: StateCollector<T>, matcher: Matcher<in T>) {
        thenEventually(Duration.ZERO, duration, Duration.ofMillis(25), context, collector, matcher)
    }

    @JvmStatic
    fun <T> thenEventually(initialDelay: Duration, duration: Duration, interval: Duration, context: TestContext, collector: StateCollector<T>, matcher: Matcher<in T>) {
        await
            .pollDelay(initialDelay)
            .pollInterval(interval)
            .atMost(duration)
            .untilAsserted { assertThat(collector.execute(context.collectorContext), matcher) }
    }
}
