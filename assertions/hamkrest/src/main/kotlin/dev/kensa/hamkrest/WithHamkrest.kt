package dev.kensa.hamkrest

import dev.kensa.StateExtractor
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder.testContext
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat

interface WithHamkrest {

    fun <T> then(extractor: StateExtractor<T?>, matcher: Matcher<T?>) {
        testContext().then(extractor, matcher)
    }

    fun <T> and(extractor: StateExtractor<T?>, matcher: Matcher<T?>) {
        then(extractor, matcher)
    }
}

private fun <T> TestContext.then(extractor: StateExtractor<T?>, matcher: Matcher<T?>) {
    assertThat(extractor.execute(interactions), matcher)
}
