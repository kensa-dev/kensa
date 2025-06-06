package dev.kensa.hamkrest

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import dev.kensa.StateExtractor
import dev.kensa.StateExtractorWithFixtures
import dev.kensa.context.TestContext
import org.awaitility.kotlin.await
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.SECONDS

object HamkrestThen {
    fun <T> then(context: TestContext, extractor: StateExtractor<T>, matcher: Matcher<T>) {
        assertThat(extractor.execute(context.interactions), matcher)
    }

    fun <T> thenEventually(timeout: Long = 10, timeUnit: ChronoUnit = SECONDS, context: TestContext, extractor: StateExtractor<T>, matcher: Matcher<T>) {
        await.atMost(Duration.of(timeout, timeUnit)).untilAsserted { assertThat(extractor.execute(context.interactions), matcher) }
        assertThat(extractor.execute(context.interactions), matcher)
    }

    fun <T> then(context: TestContext, extractor: StateExtractorWithFixtures<T>, matcher: Matcher<T>) {
        assertThat(extractor.execute(context.fixtures, context.interactions), matcher)
    }

    fun <T> thenEventually(timeout: Long = 10, timeUnit: ChronoUnit = SECONDS, context: TestContext, extractor: StateExtractorWithFixtures<T>, matcher: Matcher<T>) {
        await.atMost(Duration.of(timeout, timeUnit)).untilAsserted { assertThat(extractor.execute(context.fixtures, context.interactions), matcher) }
        assertThat(extractor.execute(context.fixtures, context.interactions), matcher)
    }
}