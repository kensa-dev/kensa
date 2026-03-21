package dev.kensa.hamkrest

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder.testContext

interface WithHamkrest {

    fun <T> then(collector: StateCollector<T>, matcher: Matcher<T>) {
        testContext().then(collector, matcher)
    }

    fun <T> and(collector: StateCollector<T>, matcher: Matcher<T>) {
        then(collector, matcher)
    }
}

private fun <T> TestContext.then(collector: StateCollector<T>, matcher: Matcher<T>) {
    assertThat(collector.execute(collectorContext), matcher)
}
