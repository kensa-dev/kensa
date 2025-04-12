package dev.kensa.hamcrest

import dev.kensa.StateExtractor
import dev.kensa.context.TestContext
import org.awaitility.kotlin.await
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.SECONDS

object HamcrestThen {
    @JvmStatic
    fun <T> then(context: TestContext, extractor: StateExtractor<T>, matcher: Matcher<in T>) {
        MatcherAssert.assertThat(extractor.execute(context.interactions), matcher)
    }

    @JvmStatic
    @JvmOverloads
    fun <T> thenEventually(timeout: Long = 10, timeUnit: ChronoUnit = SECONDS, context: TestContext, extractor: StateExtractor<T>, matcher: Matcher<in T>) {
        await.atMost(Duration.of(timeout, timeUnit)).untilAsserted { MatcherAssert.assertThat(extractor.execute(context.interactions), matcher) }
        MatcherAssert.assertThat(extractor.execute(context.interactions), matcher)
    }
}