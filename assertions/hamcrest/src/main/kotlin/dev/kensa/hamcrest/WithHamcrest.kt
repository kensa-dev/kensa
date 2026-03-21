package dev.kensa.hamcrest

import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder.testContext
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert

interface WithHamcrest {

    fun <T> then(collector: StateCollector<T?>, matcher: Matcher<in T?>) {
        testContext().then(collector, matcher)
    }

    fun <T> and(collector: StateCollector<T?>, matcher: Matcher<in T?>) {
        then(collector, matcher)
    }
}

private fun <T> TestContext.then(collector: StateCollector<T?>, matcher: Matcher<in T?>) {
    MatcherAssert.assertThat(collector.execute(collectorContext), matcher)
}
